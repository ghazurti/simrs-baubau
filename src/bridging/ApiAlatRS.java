package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Bridging SIMRS Khanza ke sistem alat_rs (RIS + DICOM + Orthanc/MinIO + SATUSEHAT).
 * Pola: Khanza push order ke alat_rs, alat_rs handle MWL + DICOM Router + SATUSEHAT.
 *
 * Base URL diambil dari koneksiDB.URLALATRS() + ":" + koneksiDB.PORTALATRS().
 * Login JWT dengan USERALATRS / PASSALATRS, token disimpan dan dipakai
 * sebagai Authorization: Bearer untuk semua endpoint kecuali /api/user/login.
 */
public class ApiAlatRS {

    private final ObjectMapper mapper = new ObjectMapper();
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private JsonNode root;
    private String requestJson;
    private String baseUrl;
    private String token;

    private SSLContext sslContext;
    private SSLSocketFactory sslFactory;
    private Scheme scheme;
    private HttpComponentsClientHttpRequestFactory factory;

    public ApiAlatRS() {
        try {
            String url = koneksiDB.URLALATRS();
            String port = koneksiDB.PORTALATRS();
            if (url == null || url.isEmpty()) {
                baseUrl = "";
            } else if (port == null || port.isEmpty()) {
                baseUrl = url;
            } else {
                baseUrl = url + ":" + port;
            }
        } catch (Exception ex) {
            System.out.println("Notifikasi alat_rs init : " + ex);
            baseUrl = "";
        }
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String token() {
        return token;
    }

    /**
     * Login petugas → JWT token.
     * Username dan password diambil dari koneksiDB (encrypted AES).
     */
    public String lastLoginError = "";

    public String Login() {
        lastLoginError = "";
        try {
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            requestJson = "{"
                    + "\"username\":\"" + koneksiDB.USERALATRS() + "\","
                    + "\"password\":\"" + koneksiDB.PASSALATRS() + "\""
                    + "}";
            requestEntity = new HttpEntity(requestJson, headers);
            System.out.println("alat_rs LOGIN URL  : " + baseUrl + "/api/user/login");
            String body = getRest().exchange(baseUrl + "/api/user/login",
                    HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println("alat_rs LOGIN RESP : " + body);
            root = mapper.readTree(body);
            if (root.path("data").isArray() && root.path("data").size() > 0) {
                token = root.path("data").get(0).path("token").asText();
            } else {
                lastLoginError = "Response tanpa token: " + body;
            }
        } catch (Exception ex) {
            lastLoginError = ex.toString();
            System.out.println("Notifikasi alat_rs Login : " + ex);
        }
        return token;
    }

    /**
     * Pendaftaran pasien (rekam medis) ke alat_rs.
     * Mapping dari pasien Khanza: nama, NIK, jenis kelamin (M/F),
     * tanggal lahir (yyyy-MM-dd).
     */
    public JsonNode RegisterPasien(String namaPasien, String nik, String jenisKelamin, String tanggalLahir) {
        StringBuilder sb = new StringBuilder("{")
                .append("\"nama_pasien\":\"").append(escape(namaPasien)).append("\",")
                .append("\"nik\":\"").append(escape(nik)).append("\"");
        if (jenisKelamin != null && !jenisKelamin.isEmpty()) {
            sb.append(",\"jenis_kelamin\":\"").append(escape(jenisKelamin)).append("\"");
        }
        if (validDate(tanggalLahir)) {
            sb.append(",\"tanggal_lahir_pasien\":\"").append(escape(tanggalLahir)).append("\"");
        }
        sb.append("}");
        return postJsonAuth("REG_PASIEN", baseUrl + "/api/rm/register", sb.toString());
    }

    /**
     * Resolve IHS Patient id (satu_sehat_id) dari NIK untuk pasien tertentu.
     * id = id rekam medis di alat_rs (bukan no_rkm_medis Khanza).
     */
    public JsonNode SyncPasienSatuSehat(String idRm) {
        String url = baseUrl + "/api/rm/sync_satu_sehat" + (idRm == null || idRm.isEmpty() ? "" : "/" + idRm);
        return postJsonAuth("SYNC_PASIEN", url, "{}");
    }

    /**
     * Cari pasien di alat_rs by NIK persis (filter server-side "nik", sesuai
     * POST /api/rm/list). Return id rm di alat_rs atau "" kalau tidak ada.
     */
    public String CariPasienIdByNik(String nik) {
        if (nik == null || nik.isEmpty()) return "";
        JsonNode resp = postJsonAuth("CARI_PASIEN",
                baseUrl + "/api/rm/list",
                "{\"halaman\":0,\"jumlah\":20,\"search\":\"" + escape(nik) + "\"}");
        return findIdByField(resp, "nik", nik);
    }

    /**
     * Cari pasien di alat_rs berdasarkan NIK / nama (paginasi default 1/20).
     */
    public JsonNode CariPasien(String search) {
        try {
            ensureToken();
            headers = jsonAuthHeaders();
            requestJson = "{\"search\":\"" + escape(search) + "\",\"halaman\":0,\"jumlah\":20}";
            requestEntity = new HttpEntity(requestJson, headers);
            String body = getRest().exchange(baseUrl + "/api/rm/list",
                    HttpMethod.POST, requestEntity, String.class).getBody();
            root = mapper.readTree(body);
        } catch (Exception ex) {
            System.out.println("Notifikasi alat_rs CariPasien : " + ex);
        }
        return root;
    }

    /**
     * Cari tenaga medis di alat_rs by NIK persis (filter server-side
     * "nik_tenaga_medis", sesuai POST /api/tenaga_medis/list). Return id atau "".
     */
    public String CariTenagaMedisIdByNik(String nik) {
        if (nik == null || nik.isEmpty()) return "";
        // Pakai field "search" (cari ke nama ATAU nik) — lebih toleran dari filter kolom.
        JsonNode resp = postJsonAuth("CARI_TENAGA_MEDIS",
                baseUrl + "/api/tenaga_medis/list",
                "{\"halaman\":0,\"jumlah\":20,\"search\":\"" + escape(nik) + "\"}");
        return findIdByField(resp, "nik_tenaga_medis", nik);
    }

    /**
     * Register tenaga medis. Sesuai POST /api/tenaga_medis/register:
     * nama_tenaga_medis, nik_tenaga_medis wajib; jenis_kelamin_tenaga_medis
     * dan tanggal_lahir_tenaga_medis opsional.
     */
    public JsonNode RegisterTenagaMedis(String nama, String nik) {
        return RegisterTenagaMedis(nama, nik, "", "");
    }

    public JsonNode RegisterTenagaMedis(String nama, String nik, String jenisKelamin, String tanggalLahir) {
        StringBuilder sb = new StringBuilder("{")
                .append("\"nama_tenaga_medis\":\"").append(escape(nama)).append("\",")
                .append("\"nik_tenaga_medis\":\"").append(escape(nik)).append("\"");
        if (jenisKelamin != null && !jenisKelamin.isEmpty()) {
            sb.append(",\"jenis_kelamin_tenaga_medis\":\"").append(escape(jenisKelamin)).append("\"");
        }
        if (validDate(tanggalLahir)) {
            sb.append(",\"tanggal_lahir_tenaga_medis\":\"").append(escape(tanggalLahir)).append("\"");
        }
        sb.append("}");
        return postJsonAuth("REG_TENAGA_MEDIS", baseUrl + "/api/tenaga_medis/register", sb.toString());
    }

    public JsonNode SyncTenagaMedisSatuSehat(String idTenagaMedis) {
        String url = baseUrl + "/api/tenaga_medis/sync_satu_sehat"
                + (idTenagaMedis == null || idTenagaMedis.isEmpty() ? "" : "/" + idTenagaMedis);
        return postJsonAuth("SYNC_TENAGA_MEDIS", url, "{}");
    }

    /**
     * Register jenis pemeriksaan (LOINC). Sesuai POST /api/ms_pemeriksaan/register:
     * nama_pemeriksaan, loinc_code, loinc_display, kode_modality.
     * loinc_code dipakai untuk ServiceRequest.code di SATUSEHAT.
     */
    public JsonNode RegisterPemeriksaan(String namaPemeriksaan, String kodeModality, String loincCode) {
        return RegisterPemeriksaan(namaPemeriksaan, kodeModality, loincCode, "");
    }

    public JsonNode RegisterPemeriksaan(String namaPemeriksaan, String kodeModality, String loincCode, String loincDisplay) {
        String json = "{"
                + "\"nama_pemeriksaan\":\"" + escape(namaPemeriksaan) + "\","
                + "\"kode_modality\":\"" + escape(kodeModality) + "\","
                + "\"loinc_code\":\"" + escape(loincCode) + "\","
                + "\"loinc_display\":\"" + escape(loincDisplay) + "\""
                + "}";
        return postJsonAuth("REG_PEMERIKSAAN", baseUrl + "/api/ms_pemeriksaan/register", json);
    }

    public JsonNode RegisterAlat(String namaAlat, String aeTitle, String kodeModality) {
        try {
            ensureToken();
            headers = jsonAuthHeaders();
            requestJson = "{"
                    + "\"nama_alat\":\"" + escape(namaAlat) + "\","
                    + "\"ae_title\":\"" + escape(aeTitle) + "\","
                    + "\"kode_modality\":\"" + escape(kodeModality) + "\""
                    + "}";
            requestEntity = new HttpEntity(requestJson, headers);
            String body = getRest().exchange(baseUrl + "/api/ms_alat/register",
                    HttpMethod.POST, requestEntity, String.class).getBody();
            root = mapper.readTree(body);
        } catch (Exception ex) {
            System.out.println("Notifikasi alat_rs RegisterAlat : " + ex);
        }
        return root;
    }

    /**
     * Cari ms_pemeriksaan di alat_rs by LOINC code (filter server-side "loinc_code",
     * sesuai POST /api/ms_pemeriksaan/list). Return id atau "".
     *
     * LOINC adalah kunci penghubung: RIS sudah punya master pemeriksaan+modality
     * sendiri, Khanza punya LOINC di satu_sehat_mapping_radiologi. Tidak perlu
     * mapping manual — cukup samakan LOINC code.
     */
    public String CariPemeriksaanIdByLoinc(String loincCode) {
        if (loincCode == null || loincCode.isEmpty()) return "";
        JsonNode resp = postJsonAuth("CARI_PEMERIKSAAN_LOINC",
                baseUrl + "/api/ms_pemeriksaan/list",
                "{\"halaman\":0,\"jumlah\":20,\"loinc_code\":\"" + escape(loincCode) + "\"}");
        return findIdByField(resp, "loinc_code", loincCode);
    }

    /**
     * Cari ms_pemeriksaan di alat_rs by nama (filter server-side "nama_pemeriksaan").
     * Dipakai kalau LOINC belum tersedia. Return id atau "".
     */
    public String CariPemeriksaanIdByNama(String nama) {
        if (nama == null || nama.isEmpty()) return "";
        JsonNode resp = postJsonAuth("CARI_PEMERIKSAAN_NAMA",
                baseUrl + "/api/ms_pemeriksaan/list",
                "{\"halaman\":0,\"jumlah\":20,\"search\":\"" + escape(nama) + "\"}");
        return findIdByField(resp, "nama_pemeriksaan", nama);
    }

    /**
     * Buat order pemeriksaan (worklist). Sistem alat_rs otomatis bikin
     * Encounter + ServiceRequest di SATUSEHAT dan generate Accession Number.
     *
     * @param patientId      id rekam medis di alat_rs (bukan no_rkm_medis)
     * @param tenagaMedisId  id tenaga_medis di alat_rs
     * @param pemeriksaanId  id ms_pemeriksaan di alat_rs
     * @param aeTitle        ScheduledStationAETitle (opsional, "" jika tidak diisi)
     * @param tanggal        yyyy-MM-dd
     * @param jam            HHmmss (6 digit)
     */
    public JsonNode RegisterWorklist(String patientId, String tenagaMedisId, String pemeriksaanId,
                                     String aeTitle, String tanggal, String jam) {
        try {
            ensureToken();
            headers = jsonAuthHeaders();
            StringBuilder sb = new StringBuilder("{")
                    .append("\"patient_id\":\"").append(escape(patientId)).append("\",")
                    .append("\"tenaga_medis_id\":\"").append(escape(tenagaMedisId)).append("\",")
                    .append("\"ms_pemeriksaan_id\":\"").append(escape(pemeriksaanId)).append("\",")
                    .append("\"scheduled_step_start_date\":\"").append(escape(tanggal)).append("\",")
                    .append("\"scheduled_step_start_time\":\"").append(escape(jam)).append("\"");
            if (aeTitle != null && !aeTitle.isEmpty()) {
                sb.append(",\"scheduled_station_aet\":\"").append(escape(aeTitle)).append("\"");
            }
            sb.append("}");
            requestJson = sb.toString();
            requestEntity = new HttpEntity(requestJson, headers);
            String body = getRest().exchange(baseUrl + "/api/worklist/register",
                    HttpMethod.POST, requestEntity, String.class).getBody();
            root = mapper.readTree(body);
        } catch (Exception ex) {
            System.out.println("Notifikasi alat_rs RegisterWorklist : " + ex);
        }
        return root;
    }

    /**
     * Buat worklist SEKALIGUS register/pakai-ulang RM (rekam medis) dalam 1 call.
     * Endpoint POST /api/worklist/register_with_rm.
     *
     * Kalau rm.id sudah ada di alat_rs → pakai yang ada (tidak dibuat ulang).
     * Kalau belum ada → dibuat baru DENGAN id yang kita kirim (bukan generate).
     * Sangat cocok untuk Khanza: pakai no_rkm_medis Khanza sebagai rm.id
     * → mapping stabil, idempotent, tanpa perlu lookup by NIK.
     *
     * @param rmId              no_rkm_medis Khanza (dipakai sebagai id di alat_rs)
     * @param namaPasien        wajib kalau rm belum ada
     * @param nik               NIK pasien
     * @param jenisKelamin      "M" / "F"
     * @param tanggalLahir      yyyy-MM-dd
     * @param tenagaMedisId     id tenaga_medis di alat_rs (WAJIB — resolve dulu)
     * @param pemeriksaanId     id ms_pemeriksaan di alat_rs
     * @param aeTitle           opsional
     * @param tanggal           yyyy-MM-dd
     * @param jam               HHmmss
     */
    public JsonNode RegisterWorklistWithRm(
            String rmId, String namaPasien, String nik, String jenisKelamin, String tanggalLahir,
            String tenagaMedisId, String pemeriksaanId,
            String aeTitle, String tanggal, String jam) {
        try {
            ensureToken();
            headers = jsonAuthHeaders();
            StringBuilder sb = new StringBuilder("{")
                    .append("\"rm\":{")
                    .append("\"id\":\"").append(escape(rmId)).append("\",")
                    .append("\"nama_pasien\":\"").append(escape(namaPasien)).append("\",")
                    .append("\"nik\":\"").append(escape(nik)).append("\"");
            if (jenisKelamin != null && !jenisKelamin.isEmpty()) {
                sb.append(",\"jenis_kelamin\":\"").append(escape(jenisKelamin)).append("\"");
            }
            if (validDate(tanggalLahir)) {
                sb.append(",\"tanggal_lahir_pasien\":\"").append(escape(tanggalLahir)).append("\"");
            }
            sb.append("},")
                    .append("\"tenaga_medis_id\":\"").append(escape(tenagaMedisId)).append("\",")
                    .append("\"ms_pemeriksaan_id\":\"").append(escape(pemeriksaanId)).append("\",")
                    .append("\"scheduled_step_start_date\":\"").append(escape(tanggal)).append("\",")
                    .append("\"scheduled_step_start_time\":\"").append(escape(jam)).append("\"");
            if (aeTitle != null && !aeTitle.isEmpty()) {
                sb.append(",\"scheduled_station_aet\":\"").append(escape(aeTitle)).append("\"");
            }
            sb.append("}");
            requestJson = sb.toString();
            requestEntity = new HttpEntity(requestJson, headers);
            System.out.println("alat_rs WORKLIST REQ  : " + requestJson);
            String body = getRest().exchange(baseUrl + "/api/worklist/register_with_rm",
                    HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println("alat_rs WORKLIST RESP : " + body);
            return mapper.readTree(body);
        } catch (org.springframework.web.client.HttpStatusCodeException he) {
            String errBody = he.getResponseBodyAsString();
            System.out.println("alat_rs WORKLIST HTTP " + he.getStatusCode() + " : " + errBody);
            try {
                return mapper.readTree(errBody);
            } catch (Exception ig) {
                return null;
            }
        } catch (Exception ex) {
            System.out.println("Notifikasi alat_rs RegisterWorklistWithRm : " + ex);
        }
        return null;
    }

    /**
     * Retry push ke SATUSEHAT untuk order tertentu (bangun ulang ServiceRequest
     * + ImagingStudy). Endpoint POST /api/satusehat/push/:accession.
     *
     * @param accession accession_number order
     * @param dryRun    true = inspect payload tanpa kirim
     */
    public JsonNode PushSatuSehatByAccession(String accession, boolean dryRun) {
        try {
            ensureToken();
            headers = bearerHeaders();
            requestEntity = new HttpEntity(headers);
            String url = baseUrl + "/api/satusehat/push/" + accession + "?dry_run=" + dryRun;
            String body = getRest().exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();
            root = mapper.readTree(body);
        } catch (Exception ex) {
            System.out.println("Notifikasi alat_rs PushSatuSehatByAccession : " + ex);
        }
        return root;
    }

    /**
     * Kirim hasil bacaan radiolog. Otomatis dibuat Observation + DiagnosticReport
     * di SATUSEHAT.
     *
     * @param worklistId  id worklist di alat_rs
     * @param kesimpulan  wajib (kalau kosong → 400)
     * @param temuan      opsional
     * @param radiologId  wajib, id tenaga_medis radiolog
     * @param status      default "final"
     */
    public JsonNode KirimHasilBacaan(String worklistId, String kesimpulan, String temuan,
                                     String radiologId, String status) {
        try {
            ensureToken();
            headers = jsonAuthHeaders();
            requestJson = "{"
                    + "\"kesimpulan\":\"" + escape(kesimpulan) + "\","
                    + "\"temuan\":\"" + escape(temuan == null ? "" : temuan) + "\","
                    + "\"radiolog_id\":\"" + escape(radiologId) + "\","
                    + "\"status\":\"" + escape(status == null || status.isEmpty() ? "final" : status) + "\""
                    + "}";
            requestEntity = new HttpEntity(requestJson, headers);
            String body = getRest().exchange(baseUrl + "/api/worklist/" + worklistId + "/hasil",
                    HttpMethod.POST, requestEntity, String.class).getBody();
            root = mapper.readTree(body);
        } catch (Exception ex) {
            System.out.println("Notifikasi alat_rs KirimHasilBacaan : " + ex);
        }
        return root;
    }

    /**
     * Ambil daftar worklist (filter opsional). GET dengan query params.
     * Cocok untuk polling status IMG_SENT / FAILED dari SIMRS bila tidak pakai
     * webhook receiver.
     */
    public JsonNode ListWorklist(String patientName, String tanggal, String orderStatus) {
        try {
            ensureToken();
            headers = bearerHeaders();
            requestEntity = new HttpEntity(headers);
            StringBuilder qs = new StringBuilder("?");
            if (patientName != null && !patientName.isEmpty()) {
                qs.append("patient_name=").append(urlEncode(patientName)).append("&");
            }
            if (tanggal != null && !tanggal.isEmpty()) {
                qs.append("scheduled_step_start_date=").append(urlEncode(tanggal)).append("&");
            }
            if (orderStatus != null && !orderStatus.isEmpty()) {
                qs.append("order_status=").append(urlEncode(orderStatus)).append("&");
            }
            String url = baseUrl + "/api/worklist/list" + (qs.length() == 1 ? "" : qs.substring(0, qs.length() - 1));
            String body = getRest().exchange(url, HttpMethod.GET, requestEntity, String.class).getBody();
            root = mapper.readTree(body);
        } catch (Exception ex) {
            System.out.println("Notifikasi alat_rs ListWorklist : " + ex);
        }
        return root;
    }

    /**
     * Dashboard: jumlah worklist per status SATUSEHAT (PENDING/SR_SENT/IMG_SENT/FAILED)
     * pada rentang tanggal.
     */
    public JsonNode DashboardWorklist(String tanggalAwal, String tanggalAkhir) {
        try {
            ensureToken();
            headers = bearerHeaders();
            requestEntity = new HttpEntity(headers);
            String url = baseUrl + "/api/dashboard/worklist?tanggal_awal=" + tanggalAwal + "&tanggal_akhir=" + tanggalAkhir;
            String body = getRest().exchange(url, HttpMethod.GET, requestEntity, String.class).getBody();
            root = mapper.readTree(body);
        } catch (Exception ex) {
            System.out.println("Notifikasi alat_rs DashboardWorklist : " + ex);
        }
        return root;
    }

    // ---------------- orchestrator Khanza ----------------

    /**
     * Hasil pengiriman 1 permintaan radiologi dari Khanza ke alat_rs.
     */
    public static class HasilKirim {
        public boolean ok;
        public String accessionNumber = "";
        public String worklistId = "";
        public String patientIdAlatRS = "";
        public String satuSehatStatus = "";
        public String pesan = "";
        public String responseRaw = "";
    }

    /**
     * Orchestrator: kirim 1 permintaan radiologi Khanza ke alat_rs (RIS).
     * Fully otomatis, tanpa mapping manual. Flow:
     *  1. Resolve tenaga medis by NIK → register + sync IHS kalau belum ada.
     *  2. Resolve ms_pemeriksaan by LOINC code (RIS sudah punya master LOINC-nya).
     *     LOINC diambil Khanza dari satu_sehat_mapping_radiologi.
     *  3. POST /api/worklist/register_with_rm — pasien (rm) auto-create by no_rkm_medis.
     *
     * @param loincCode      LOINC code dari satu_sehat_mapping_radiologi (kunci penghubung)
     * @param loincDisplay   LOINC display (mis. "CT Chest") — dipakai derive modality saat auto-register
     * @param namaPemeriksaan nama perawatan Khanza (dikirim saat auto-register pemeriksaan ke RIS)
     */
    public HasilKirim KirimPermintaanRadiologi(
            String noRkmMedisKhanza,
            String namaPasien, String nikPasien, String jkPasien, String tglLahirPasien,
            String namaDokter, String nikDokter, String jkDokter, String tglLahirDokter,
            String loincCode, String loincDisplay, String namaPemeriksaan,
            String tanggal, String jamHHmmss) {

        HasilKirim h = new HasilKirim();
        try {
            ensureToken();
            if (token == null || token.isEmpty()) {
                h.pesan = "Gagal login ke alat_rs. Cek setting URL/PORT/USER/PASS.";
                return h;
            }

            // 1. Resolve tenaga medis by NIK (register kalau belum ada)
            String tmid = "";
            if (nikDokter != null && !nikDokter.isEmpty()) {
                tmid = CariTenagaMedisIdByNik(nikDokter);
            }
            if (tmid.isEmpty()) {
                JsonNode rt = RegisterTenagaMedis(namaDokter, nikDokter, jkDokter, tglLahirDokter);
                tmid = extractId(rt);
                if (tmid.isEmpty()) {
                    // register gagal → tampilkan pesan error dari RIS
                    String m = rt == null ? "tidak ada respons" : rt.path("message").asText();
                    h.pesan = "Gagal daftarkan dokter \"" + namaDokter + "\" (NIK " + nikDokter + ") ke RIS: "
                            + (m == null || m.isEmpty() ? (rt == null ? "tidak ada respons" : rt.toString()) : m);
                    return h;
                }
                SyncTenagaMedisSatuSehat(tmid);
            }
            if (tmid.isEmpty()) {
                h.pesan = "Gagal mendapatkan ID tenaga medis di alat_rs (NIK: " + nikDokter + ").";
                return h;
            }

            // 2. Resolve pemeriksaan. LOINC OPSIONAL:
            //    - kalau ada LOINC → cari by LOINC dulu
            //    - kalau tidak ada / tidak ketemu → cari by nama
            //    - kalau tetap tidak ada → auto-register ke RIS (LOINC boleh kosong)
            String idPemeriksaan = "";
            if (loincCode != null && !loincCode.isEmpty()) {
                idPemeriksaan = CariPemeriksaanIdByLoinc(loincCode);
            }
            if (idPemeriksaan.isEmpty()) {
                idPemeriksaan = CariPemeriksaanIdByNama(namaPemeriksaan);
            }
            if (idPemeriksaan.isEmpty()) {
                // Auto-register pemeriksaan ke RIS. Modality diturunkan dari
                // LOINC display / nama (mis. "CT Chest" -> CT, "THORAX" -> DX).
                String kodeModality = deriveModality(loincDisplay, namaPemeriksaan);
                JsonNode rp = RegisterPemeriksaan(namaPemeriksaan, kodeModality,
                        loincCode == null ? "" : loincCode, loincDisplay == null ? "" : loincDisplay);
                idPemeriksaan = extractId(rp);
                if (idPemeriksaan.isEmpty()) {
                    idPemeriksaan = CariPemeriksaanIdByNama(namaPemeriksaan);
                }
            }
            if (idPemeriksaan.isEmpty()) {
                h.pesan = "Gagal mendaftarkan pemeriksaan \"" + namaPemeriksaan + "\" ke RIS. "
                        + "Cek apakah modality " + deriveModality(loincDisplay, namaPemeriksaan) + " sudah ada di master modality RIS.";
                return h;
            }

            // 3. Register worklist + RM inline dalam 1 call.
            //    rm.id = no_rkm_medis Khanza → mapping stabil, alat_rs auto-create
            //    rm kalau belum ada. AE Title dikosongkan (RIS/radiografer yang atur).
            JsonNode rw = RegisterWorklistWithRm(
                    noRkmMedisKhanza, namaPasien, nikPasien, jkPasien, tglLahirPasien,
                    tmid, idPemeriksaan, "", tanggal, jamHHmmss);
            if (rw == null) {
                h.pesan = "Tidak ada respons dari alat_rs saat register worklist.";
                return h;
            }
            h.responseRaw = rw.toString();
            JsonNode data = rw.path("data");
            h.accessionNumber = data.path("accession_number").asText();
            h.satuSehatStatus = data.path("satu_sehat_status").asText();
            h.worklistId = data.path("id").asText();
            h.patientIdAlatRS = noRkmMedisKhanza; // sama dengan yang kita kirim

            if (!h.accessionNumber.isEmpty()) {
                h.ok = true;
                h.pesan = "Berhasil. Accession: " + h.accessionNumber + " | Status: " + h.satuSehatStatus;
            } else {
                String msg = rw.path("message").asText();
                h.pesan = "Worklist gagal: " + (msg == null || msg.isEmpty() ? rw.toString() : msg);
            }
        } catch (Exception ex) {
            h.pesan = "Error: " + ex.getMessage();
            System.out.println("Notifikasi alat_rs KirimPermintaanRadiologi : " + ex);
        }
        return h;
    }

    // ---------------- helpers ----------------

    /**
     * Turunkan kode modality DICOM dari LOINC display / nama pemeriksaan.
     * LOINC display biasanya berawalan modality: "CT Chest", "XR Chest",
     * "US Abdomen", "MR Brain". DICOM: X-ray -> DX (digital radiography,
     * default RS), USG -> US, dst. RS bisa sesuaikan master modality RIS.
     */
    private String deriveModality(String loincDisplay, String namaPemeriksaan) {
        String s = ((loincDisplay == null ? "" : loincDisplay) + " "
                 + (namaPemeriksaan == null ? "" : namaPemeriksaan)).toUpperCase().trim();
        if (s.startsWith("CT") || s.contains(" CT ") || s.contains("CT ")) return "CT";
        if (s.startsWith("MR") || s.contains("MRI")) return "MR";
        if (s.startsWith("US") || s.contains("USG") || s.contains("ULTRASOUND")) return "US";
        if (s.contains("ANGIO") || s.startsWith("XA")) return "XA";
        if (s.contains("FLUORO") || s.startsWith("RF")) return "RF";
        if (s.contains("MAMMO") || s.startsWith("MG")) return "MG";
        if (s.contains("PET") || s.contains("SCINTIG") || s.startsWith("NM")) return "NM";
        // Default: X-ray konvensional/digital -> DX
        return "DX";
    }

    private void ensureToken() {
        if (token == null || token.isEmpty()) {
            Login();
        }
    }

    private HttpHeaders jsonAuthHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add("Authorization", "Bearer " + token);
        return h;
    }

    /**
     * POST JSON (Bearer auth) terpusat. Selalu kembalikan node hasil parse dari
     * response (sukses ATAU error body dari server), atau null kalau gagal total.
     * TIDAK memakai field {@code root} bersama supaya tidak ada data basi antar-call.
     */
    private JsonNode postJsonAuth(String logName, String url, String json) {
        try {
            ensureToken();
            HttpHeaders h = jsonAuthHeaders();
            HttpEntity re = new HttpEntity(json, h);
            System.out.println("alat_rs " + logName + " REQ  : " + json);
            String body = getRest().exchange(url, HttpMethod.POST, re, String.class).getBody();
            System.out.println("alat_rs " + logName + " RESP : " + body);
            return mapper.readTree(body);
        } catch (org.springframework.web.client.HttpStatusCodeException he) {
            String errBody = he.getResponseBodyAsString();
            System.out.println("alat_rs " + logName + " HTTP " + he.getStatusCode() + " : " + errBody);
            try { return mapper.readTree(errBody); } catch (Exception ig) { return null; }
        } catch (Exception ex) {
            System.out.println("alat_rs " + logName + " ERR  : " + ex);
            return null;
        }
    }

    /** Ambil array item dari berbagai bentuk response list. */
    private JsonNode extractArray(JsonNode resp) {
        if (resp == null) return null;
        JsonNode data = resp.path("data");
        if (data.isArray()) return data;
        // bentuk paginasi: data.data / data.rows / data.list / data.items / data.result
        for (String k : new String[]{"data", "rows", "list", "items", "result"}) {
            if (data.path(k).isArray()) return data.path(k);
        }
        return null;
    }

    /**
     * Cari id di response list: cocokkan item yang {field}=={value}.
     * Kalau tidak ada yang cocok persis tapi hasil cuma 1, ambil yang itu.
     */
    private String findIdByField(JsonNode resp, String field, String value) {
        JsonNode arr = extractArray(resp);
        if (arr == null || !arr.isArray()) return "";
        for (JsonNode it : arr) {
            if (value.equals(it.path(field).asText())) {
                return it.path("id").asText();
            }
        }
        if (arr.size() == 1) return arr.get(0).path("id").asText();
        return "";
    }

    /** Ekstrak id dari response register (data.id ATAU data[0].id). */
    private String extractId(JsonNode resp) {
        if (resp == null) return "";
        String id = resp.path("data").path("id").asText();
        if ((id == null || id.isEmpty()) && resp.path("data").isArray() && resp.path("data").size() > 0) {
            id = resp.path("data").get(0).path("id").asText();
        }
        return id == null ? "" : id;
    }

    private HttpHeaders bearerHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.add("Authorization", "Bearer " + token);
        return h;
    }

    /** true kalau format yyyy-MM-dd valid & bukan 0000-00-00 (invalid MySQL date). */
    private boolean validDate(String s) {
        return s != null && s.matches("\\d{4}-\\d{2}-\\d{2}") && !s.startsWith("0000");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s == null ? "" : s, "UTF-8");
        } catch (Exception ex) {
            return "";
        }
    }

    public RestTemplate getRest() throws NoSuchAlgorithmException, KeyManagementException {
        sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManager[] trustManagers = {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
            }
        };
        sslContext.init(null, trustManagers, new SecureRandom());
        sslFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        scheme = new Scheme("https", 443, sslFactory);
        factory = new HttpComponentsClientHttpRequestFactory();
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        return new RestTemplate(factory);
    }
}
