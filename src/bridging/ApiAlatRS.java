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

    // Info klinis per-order (diagnosa klinis & informasi tambahan) yang ikut
    // dikirim di payload worklist. Di-set sekali sebelum loop kirim.
    private String diagnosaKlinisWorklist = "";
    private String informasiTambahanWorklist = "";

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
        return RegisterTenagaMedis("", nama, nik, "", "");
    }

    public JsonNode RegisterTenagaMedis(String nama, String nik, String jenisKelamin, String tanggalLahir) {
        return RegisterTenagaMedis("", nama, nik, jenisKelamin, tanggalLahir);
    }

    /**
     * Register tenaga medis dengan id dari FE (mis. kd_dokter Khanza). Kalau id
     * diisi, RIS memakai id itu (mapping stabil) — mirip rm.id di register_with_rm.
     */
    public JsonNode RegisterTenagaMedis(String id, String nama, String nik, String jenisKelamin, String tanggalLahir) {
        StringBuilder sb = new StringBuilder("{");
        if (id != null && !id.isEmpty()) {
            sb.append("\"id\":\"").append(escape(id)).append("\",");
        }
        sb.append("\"nama_tenaga_medis\":\"").append(escape(nama)).append("\",")
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
        return RegisterPemeriksaan("", namaPemeriksaan, kodeModality, loincCode, "");
    }

    public JsonNode RegisterPemeriksaan(String namaPemeriksaan, String kodeModality, String loincCode, String loincDisplay) {
        return RegisterPemeriksaan("", namaPemeriksaan, kodeModality, loincCode, loincDisplay);
    }

    /**
     * Register jenis pemeriksaan dengan id dari FE (mis. kd_jenis_prw Khanza).
     * Kalau id diisi, RIS memakai id itu (mapping stabil, idempotent) — mirip rm.id
     * di register_with_rm. Kalau id sudah ada → RIS balas 409 (dianggap OK oleh caller).
     */
    public JsonNode RegisterPemeriksaan(String id, String namaPemeriksaan, String kodeModality, String loincCode, String loincDisplay) {
        StringBuilder sb = new StringBuilder("{");
        if (id != null && !id.isEmpty()) {
            sb.append("\"id\":\"").append(escape(id)).append("\",");
        }
        sb.append("\"nama_pemeriksaan\":\"").append(escape(namaPemeriksaan)).append("\",")
          .append("\"kode_modality\":\"").append(escape(kodeModality)).append("\",")
          .append("\"loinc_code\":\"").append(escape(loincCode)).append("\",")
          .append("\"loinc_display\":\"").append(escape(loincDisplay)).append("\"}");
        return postJsonAuth("REG_PEMERIKSAAN", baseUrl + "/api/ms_pemeriksaan/register", sb.toString());
    }

    /**
     * Cari AE Title alat scanner di master RIS berdasarkan kode modality
     * (POST /api/ms_alat/list, filter "kode_modality"). Dipakai supaya Khanza
     * bisa auto-isi scheduled_station_aet worklist tanpa tabel mapping sendiri —
     * RIS jadi sumber kebenaran daftar alat. Return ae_title atau "".
     */
    public String CariAlatAeTitleByModality(String kodeModality) {
        return CariAlatAeTitleByModality(kodeModality, "");
    }

    /**
     * Informasi 1 alat dari RIS (untuk ditampilkan di dialog pilihan).
     */
    public static class InfoAlat {
        public String id = "";
        public String namaAlat = "";
        public String aeTitle = "";
        public String kodeModality = "";
        @Override public String toString() {
            return namaAlat + " (" + aeTitle + ") [" + kodeModality + "]";
        }
    }

    /**
     * Ambil semua alat dari RIS. Return list InfoAlat.
     * Dipakai untuk menampilkan dialog pilihan alat ke user.
     */
    public java.util.List<InfoAlat> ListSemuaAlat() {
        java.util.List<InfoAlat> result = new java.util.ArrayList<>();
        JsonNode resp = postJsonAuth("LIST_SEMUA_ALAT",
                baseUrl + "/api/ms_alat/list",
                "{\"halaman\":0,\"jumlah\":100}");
        JsonNode arr = extractArray(resp);
        if (arr == null || !arr.isArray()) return result;
        for (JsonNode it : arr) {
            InfoAlat a = new InfoAlat();
            a.id = it.path("id").asText();
            a.namaAlat = it.path("nama_alat").asText();
            a.aeTitle = it.path("ae_title").asText();
            a.kodeModality = it.path("kode_modality").asText();
            result.add(a);
        }
        return result;
    }

    /**
     * Versi dengan hint nama pemeriksaan — dipakai kalau di RIS ada >1 alat
     * modality sama (mis. 2 mesin DX: X-ray umum + Panoramic Dental).
     *
     * Strategi:
     *   - 1 alat → pakai itu.
     *   - >1 alat → scoring by nama pemeriksaan vs nama_alat/ae_title:
     *       nama pemeriksaan mengandung "panoramic/pano/dental/gigi"
     *         → cari alat yang nama/ae-nya mengandung "pan"/"dental";
     *       selain itu → pilih alat yang TIDAK mengandung "pan"/"dental"
     *         (mesin general X-ray).
     *     Fallback: alat pertama.
     */
    public String CariAlatAeTitleByModality(String kodeModality, String namaPemeriksaan) {
        if (kodeModality == null || kodeModality.isEmpty()) return "";
        JsonNode resp = postJsonAuth("CARI_ALAT_MODALITY",
                baseUrl + "/api/ms_alat/list",
                "{\"halaman\":0,\"jumlah\":20,\"kode_modality\":\"" + escape(kodeModality) + "\"}");
        JsonNode arr = extractArray(resp);
        if (arr == null || !arr.isArray() || arr.size() == 0) return "";

        // Kumpulkan hanya alat yang benar modality-nya (kalau server juga
        // kirim modality lain, kita filter di sisi kita).
        java.util.List<JsonNode> cocok = new java.util.ArrayList<>();
        for (JsonNode it : arr) {
            String m = it.path("kode_modality").asText();
            if (m == null || m.isEmpty() || kodeModality.equals(m)) cocok.add(it);
        }
        if (cocok.isEmpty()) return "";
        if (cocok.size() == 1) return cocok.get(0).path("ae_title").asText();

        String namaLower = (namaPemeriksaan == null ? "" : namaPemeriksaan).toLowerCase();
        boolean butuhDental = namaLower.contains("panoramic") || namaLower.contains("panoramik")
                || namaLower.contains("pano") || namaLower.contains("dental")
                || namaLower.contains("gigi") || namaLower.contains("cephalometric")
                || namaLower.contains("cephalometri");

        JsonNode dentalHit = null, generalHit = null;
        for (JsonNode it : cocok) {
            String tag = (it.path("nama_alat").asText() + " " + it.path("ae_title").asText()).toLowerCase();
            boolean isDental = tag.contains("pan") || tag.contains("dental");
            if (isDental && dentalHit == null) dentalHit = it;
            if (!isDental && generalHit == null) generalHit = it;
        }
        JsonNode pick;
        if (butuhDental) {
            pick = (dentalHit != null) ? dentalHit : cocok.get(0);
        } else {
            pick = (generalHit != null) ? generalHit : cocok.get(0);
        }
        return pick.path("ae_title").asText();
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
    /**
     * Set info klinis (diagnosa klinis & informasi tambahan) yang ikut dikirim
     * pada payload worklist berikutnya. Cukup dipanggil sekali per order,
     * sebelum loop kirim pemeriksaan.
     */
    public void setInfoKlinis(String diagnosaKlinis, String informasiTambahan) {
        this.diagnosaKlinisWorklist = diagnosaKlinis == null ? "" : diagnosaKlinis;
        this.informasiTambahanWorklist = informasiTambahan == null ? "" : informasiTambahan;
    }

    public JsonNode RegisterWorklistWithRm(
            String rmId, String namaPasien, String nik, String jenisKelamin, String tanggalLahir,
            String patientIHS, String tenagaMedisId, String pemeriksaanId,
            String aeTitle, String tanggal, String jam, String encounterId) {
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
            // Patient IHS dari Khanza (satu_sehat_ihs_patient) → RIS tidak perlu
            // sync ulang, langsung bisa bikin ServiceRequest/ImagingStudy.
            if (patientIHS != null && !patientIHS.isEmpty()) {
                sb.append(",\"satu_sehat_id\":\"").append(escape(patientIHS)).append("\"");
            }
            sb.append("},")
                    .append("\"tenaga_medis_id\":\"").append(escape(tenagaMedisId)).append("\",")
                    .append("\"ms_pemeriksaan_id\":\"").append(escape(pemeriksaanId)).append("\",")
                    .append("\"scheduled_step_start_date\":\"").append(escape(tanggal)).append("\",")
                    .append("\"scheduled_step_start_time\":\"").append(escape(jam)).append("\"");
            if (aeTitle != null && !aeTitle.isEmpty()) {
                sb.append(",\"scheduled_station_aet\":\"").append(escape(aeTitle)).append("\"");
            }
            // Encounter dari Khanza (satu_sehat_encounter) — supaya RIS TIDAK bikin
            // Encounter baru, tapi pakai yang sama dengan kunjungan Khanza.
            if (encounterId != null && !encounterId.isEmpty()) {
                sb.append(",\"encounter_id\":\"").append(escape(encounterId)).append("\"");
            }
            // Info klinis: diagnosa klinis + informasi tambahan dari permintaan
            // radiologi Khanza. Berguna untuk radiolog & jadi bahan Condition/
            // ClinicalImpression di SATUSEHAT. Konfirmasi nama field ke vendor RIS.
            if (diagnosaKlinisWorklist != null && !diagnosaKlinisWorklist.isEmpty()) {
                sb.append(",\"diagnosa_klinis\":\"").append(escape(diagnosaKlinisWorklist)).append("\"");
            }
            if (informasiTambahanWorklist != null && !informasiTambahanWorklist.isEmpty()) {
                sb.append(",\"informasi_tambahan\":\"").append(escape(informasiTambahanWorklist)).append("\"");
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

    /**
     * Ambil detail 1 worklist by id di alat_rs (GET /api/worklist/details_by_id/:id).
     * Dipakai untuk menarik hasil bacaan yang diinput dokter/radiolog di RIS.
     */
    public JsonNode GetWorklistDetail(String worklistId) {
        return getJsonAuth("WORKLIST_DETAIL",
                baseUrl + "/api/worklist/details_by_id/" + urlEncode(worklistId));
    }

    /**
     * Hasil bacaan radiologi yang ditarik dari RIS.
     */
    public static class HasilBacaan {
        public boolean ada;          // true kalau ada teks hasil (kesimpulan/temuan)
        public boolean ok;           // true kalau call sukses (walau hasil belum ada)
        public String kesimpulan = "";
        public String temuan = "";
        public String status = "";           // final/preliminary/amended
        public String issuedAt = "";         // waktu hasil dikeluarkan
        public String radiologId = "";
        public String diagnosticReportId = "";
        public String namaPemeriksaan = "";   // nama pemeriksaan (untuk judul kalau >1 worklist)
        public String pesan = "";
        public String responseRaw = "";
    }

    /**
     * Tarik hasil bacaan (input dokter/radiolog) dari RIS untuk 1 worklist.
     * Parsing defensif: dukung beberapa varian nama field.
     */
    public HasilBacaan AmbilHasilBacaan(String worklistId) {
        HasilBacaan hb = new HasilBacaan();
        if (worklistId == null || worklistId.isEmpty()) {
            hb.pesan = "worklist_id kosong (permintaan belum pernah dikirim ke RIS?).";
            return hb;
        }
        JsonNode resp = GetWorklistDetail(worklistId);
        if (resp == null) {
            hb.pesan = "Tidak ada respons dari RIS saat ambil detail worklist.";
            return hb;
        }
        hb.responseRaw = resp.toString();
        // ambil node worklist: data (object) / data[0] / root
        JsonNode w = resp.path("data");
        if (w.isArray()) {
            w = w.size() > 0 ? w.get(0) : resp;
        }
        if (w.isMissingNode() || w.isNull()) {
            w = resp;
        }
        hb.ok = true;
        hb.kesimpulan = firstNonEmpty(w, "hasil_kesimpulan", "kesimpulan", "conclusion");
        hb.temuan     = firstNonEmpty(w, "hasil_temuan", "temuan", "findings", "hasil");
        hb.status     = firstNonEmpty(w, "hasil_status", "status_hasil", "result_status");
        hb.issuedAt   = firstNonEmpty(w, "hasil_issued_at", "issued_at", "tgl_hasil");
        hb.radiologId = firstNonEmpty(w, "radiolog_id", "radiologist_id", "radiolog");
        hb.diagnosticReportId = firstNonEmpty(w, "satu_sehat_diagnosticreport_id", "diagnosticreport_id", "diagnostic_report_id");
        hb.namaPemeriksaan = firstNonEmpty(w, "nama_pemeriksaan", "pemeriksaan", "ms_pemeriksaan_nama",
                "study_description", "nama_study", "deskripsi");
        hb.ada = !hb.kesimpulan.isEmpty() || !hb.temuan.isEmpty();
        if (!hb.ada) {
            hb.pesan = "Hasil bacaan belum diinput dokter/radiolog di RIS.";
        } else {
            hb.pesan = "Hasil ditemukan (status: " + (hb.status.isEmpty() ? "-" : hb.status) + ").";
        }
        return hb;
    }

    /**
     * Tarik & gabung hasil bacaan dari BEBERAPA worklist (dipisah koma), untuk
     * kasus 1 permintaan berisi >1 pemeriksaan yang gambarnya beda di RIS.
     * Tiap pemeriksaan diberi judul supaya jelas mana temuan yang mana.
     *
     * Field HasilBacaan yang dikembalikan:
     *   - temuan  = teks gabungan lengkap (siap ditaruh ke kolom hasil)
     *   - ada     = true kalau minimal 1 worklist punya hasil
     *   - ok      = true kalau minimal 1 call sukses
     *   - status  = ringkasan "n dari m pemeriksaan sudah ada hasil"
     */
    public HasilBacaan AmbilHasilBacaanGabungan(String worklistIdsCsv) {
        HasilBacaan gab = new HasilBacaan();
        if (worklistIdsCsv == null || worklistIdsCsv.trim().isEmpty()) {
            gab.pesan = "worklist_id kosong (permintaan belum pernah dikirim ke RIS?).";
            return gab;
        }
        String[] ids = worklistIdsCsv.split(",");
        // buang duplikat & kosong, jaga urutan
        java.util.LinkedHashSet<String> unik = new java.util.LinkedHashSet<>();
        for (String id : ids) { if (id != null && !id.trim().isEmpty()) unik.add(id.trim()); }
        if (unik.isEmpty()) {
            gab.pesan = "worklist_id kosong.";
            return gab;
        }
        // Kalau cuma 1 worklist, tidak perlu judul — perilaku sama seperti dulu.
        if (unik.size() == 1) {
            HasilBacaan hb = AmbilHasilBacaan(unik.iterator().next());
            hb.temuan = gabungTemuanKesimpulan(hb);
            hb.kesimpulan = "";
            return hb;
        }
        StringBuilder sb = new StringBuilder();
        int adaCount = 0, idx = 0, total = unik.size();
        for (String id : unik) {
            idx++;
            HasilBacaan hb = AmbilHasilBacaan(id);
            if (hb.ok) gab.ok = true;
            String judul = hb.namaPemeriksaan.isEmpty()
                    ? ("PEMERIKSAAN " + idx) : hb.namaPemeriksaan.toUpperCase();
            sb.append("=== ").append(judul).append(" ===\n");
            if (hb.ada) {
                adaCount++;
                sb.append(gabungTemuanKesimpulan(hb)).append("\n");
            } else {
                sb.append("(hasil belum diinput dokter/radiolog di RIS)\n");
            }
            if (idx < total) sb.append("\n");
        }
        gab.temuan = sb.toString().trim();
        gab.ada = adaCount > 0;
        gab.status = adaCount + " dari " + total + " pemeriksaan sudah ada hasil";
        gab.pesan = gab.ada
                ? ("Hasil ditemukan (" + gab.status + ").")
                : "Hasil bacaan belum diinput dokter/radiolog di RIS.";
        return gab;
    }

    /** Rangkai TEMUAN + KESIMPULAN dari 1 HasilBacaan jadi 1 blok teks. */
    private String gabungTemuanKesimpulan(HasilBacaan hb) {
        StringBuilder t = new StringBuilder();
        if (hb.temuan != null && !hb.temuan.isEmpty()) {
            t.append("TEMUAN:\n").append(hb.temuan);
        }
        if (hb.kesimpulan != null && !hb.kesimpulan.isEmpty()) {
            if (t.length() > 0) t.append("\n\n");
            t.append("KESIMPULAN:\n").append(hb.kesimpulan);
        }
        return t.toString();
    }

    /**
     * Cari order id (PK worklist) di RIS dari accession number.
     * Endpoint: GET /api/worklist/list?accession_number=...
     * Return id (mis. "22") atau "" kalau tidak ketemu.
     */
    public String CariOrderIdByAccession(String accessionNumber) {
        if (accessionNumber == null || accessionNumber.isEmpty()) return "";
        JsonNode resp = getJsonAuth("CARI_ORDER_BY_ACC",
                baseUrl + "/api/worklist/list?accession_number=" + urlEncode(accessionNumber));
        JsonNode arr = extractArray(resp);
        if (arr != null && arr.isArray() && arr.size() > 0) {
            return arr.get(0).path("id").asText();
        }
        // fallback: data berupa object tunggal
        if (resp != null) {
            String id = resp.path("data").path("id").asText();
            if (id != null && !id.isEmpty()) return id;
        }
        return "";
    }

    /**
     * Download SEMUA gambar radiologi 1 order dari RIS sebagai ZIP berisi PNG.
     * Endpoint: GET /api/orthanc/order/:id/images.zip
     * Return byte[] isi ZIP (atau gambar tunggal), null kalau gagal.
     *
     * Catatan klinis dari vendor: PNG hanya untuk preview/lampiran,
     * bukan pembacaan diagnostik (grayscale 12-16 bit hilang).
     * Diagnosis tetap lewat DICOM viewer RIS.
     */
    public byte[] DownloadPreviewGambar(String orderId) {
        if (orderId == null || orderId.isEmpty()) return null;
        try {
            ensureToken();
            String url = baseUrl + "/api/orthanc/order/" + urlEncode(orderId) + "/images.zip";
            HttpHeaders h = bearerHeaders();
            h.setAccept(java.util.Arrays.asList(MediaType.ALL));
            HttpEntity<String> re = new HttpEntity<>(h);
            org.springframework.http.ResponseEntity<byte[]> resp =
                getRest().exchange(url, HttpMethod.GET, re, byte[].class);
            byte[] imgBytes = resp.getBody();
            if (imgBytes == null || imgBytes.length == 0) {
                System.out.println("alat_rs PREVIEW : kosong / 0 bytes");
                return null;
            }
            System.out.println("alat_rs PREVIEW : downloaded " + imgBytes.length + " bytes");
            return imgBytes;
        } catch (org.springframework.web.client.HttpStatusCodeException he) {
            System.out.println("alat_rs PREVIEW HTTP " + he.getStatusCode() + " : " + he.getResponseBodyAsString());
            return null;
        } catch (Exception ex) {
            System.out.println("alat_rs PREVIEW ERR : " + ex);
            return null;
        }
    }

    /** Ambil nilai text pertama yang tidak kosong dari beberapa nama field. */
    private String firstNonEmpty(JsonNode node, String... fields) {
        if (node == null) return "";
        for (String f : fields) {
            String v = node.path(f).asText();
            if (v != null && !v.isEmpty() && !"null".equalsIgnoreCase(v)) return v;
        }
        return "";
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
        public String aeTitle = "";
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
            String namaPasien, String nikPasien, String jkPasien, String tglLahirPasien, String patientIHS,
            String kdDokter, String namaDokter, String nikDokter, String jkDokter, String tglLahirDokter,
            String kdJenisPrw, String loincCode, String loincDisplay, String namaPemeriksaan,
            String tanggal, String jamHHmmss, String encounterId) {
        return KirimPermintaanRadiologi(noRkmMedisKhanza,
                namaPasien, nikPasien, jkPasien, tglLahirPasien, patientIHS,
                kdDokter, namaDokter, nikDokter, jkDokter, tglLahirDokter,
                kdJenisPrw, loincCode, loincDisplay, namaPemeriksaan,
                tanggal, jamHHmmss, encounterId, null);
    }

    /**
     * Overload dengan aeTitleOverride — kalau diisi, pakai AE Title itu langsung
     * tanpa auto-detect dari nama pemeriksaan. Null/"" = auto-detect seperti biasa.
     */
    public HasilKirim KirimPermintaanRadiologi(
            String noRkmMedisKhanza,
            String namaPasien, String nikPasien, String jkPasien, String tglLahirPasien, String patientIHS,
            String kdDokter, String namaDokter, String nikDokter, String jkDokter, String tglLahirDokter,
            String kdJenisPrw, String loincCode, String loincDisplay, String namaPemeriksaan,
            String tanggal, String jamHHmmss, String encounterId,
            String aeTitleOverride) {

        HasilKirim h = new HasilKirim();
        try {
            ensureToken();
            if (token == null || token.isEmpty()) {
                h.pesan = "Gagal login ke alat_rs. Cek setting URL/PORT/USER/PASS.";
                return h;
            }

            // 1. Tenaga medis: pakai kd_dokter Khanza sebagai id (stabil).
            //    Resolve dulu by NIK (kalau sudah ada — apapun idnya — dipakai ulang,
            //    tidak duplikat). Belum ada → register dengan id = kd_dokter.
            String tmid = "";
            if (nikDokter != null && !nikDokter.isEmpty()) {
                tmid = CariTenagaMedisIdByNik(nikDokter);
            }
            if (tmid.isEmpty()) {
                JsonNode rt = RegisterTenagaMedis(kdDokter, namaDokter, nikDokter, jkDokter, tglLahirDokter);
                int st = (rt == null) ? -1 : rt.path("status").asInt();
                if (st == 200 || st == 201) {
                    tmid = kdDokter;                          // dibuat dengan id kd_dokter
                } else {
                    tmid = CariTenagaMedisIdByNik(nikDokter); // 409/lainnya → ambil id yang sudah ada
                }
                if (tmid.isEmpty()) {
                    String m = rt == null ? "tidak ada respons" : rt.path("message").asText();
                    h.pesan = "Gagal daftarkan dokter \"" + namaDokter + "\" (NIK " + nikDokter + ") ke RIS: "
                            + (m == null || m.isEmpty() ? (rt == null ? "tidak ada respons" : rt.toString()) : m);
                    return h;
                }
                SyncTenagaMedisSatuSehat(tmid);
            }

            // 2. Pemeriksaan: pakai kd_jenis_prw Khanza sebagai id ms_pemeriksaan di RIS
            //    (mapping stabil & idempotent — tidak perlu search yang rawan salah cocok).
            //    Register dengan id itu; kalau sudah ada RIS balas 409 → id tetap dipakai.
            String idPemeriksaan = kdJenisPrw;
            String kodeModality = deriveModality(loincDisplay, namaPemeriksaan);
            JsonNode rp = RegisterPemeriksaan(kdJenisPrw, namaPemeriksaan, kodeModality,
                    loincCode == null ? "" : loincCode, loincDisplay == null ? "" : loincDisplay);
            int st = (rp == null) ? -1 : rp.path("status").asInt();
            // 200/201 = baru dibuat, 409 = sudah ada. Dua-duanya berarti id kd_jenis_prw valid dipakai.
            if (st != 200 && st != 201 && st != 409) {
                // gagal lain (mis. modality tidak ada di RIS) → coba fallback resolve by nama/LOINC
                String fb = "";
                if (loincCode != null && !loincCode.isEmpty()) fb = CariPemeriksaanIdByLoinc(loincCode);
                if (fb.isEmpty()) fb = CariPemeriksaanIdByNama(namaPemeriksaan);
                if (!fb.isEmpty()) {
                    idPemeriksaan = fb;
                } else {
                    String m = (rp == null) ? "tidak ada respons" : rp.path("message").asText();
                    h.pesan = "Gagal daftarkan pemeriksaan \"" + namaPemeriksaan + "\" (" + kdJenisPrw + ") ke RIS: "
                            + (m == null || m.isEmpty() ? "cek modality " + kodeModality + " di master RIS" : m);
                    return h;
                }
            }

            // 3. Resolve alat scanner (AE Title).
            //    Kalau aeTitleOverride diisi → pakai langsung (user sudah pilih).
            //    Kalau kosong → auto-detect by modality + nama pemeriksaan.
            String aeTitle = "";
            if (aeTitleOverride != null && !aeTitleOverride.trim().isEmpty()) {
                aeTitle = aeTitleOverride.trim();
            } else {
                try {
                    String hint = (namaPemeriksaan == null ? "" : namaPemeriksaan)
                            + " " + (loincDisplay == null ? "" : loincDisplay);
                    aeTitle = CariAlatAeTitleByModality(kodeModality, hint);
                } catch (Exception ig) {
                    aeTitle = "";
                }
            }
            h.aeTitle = aeTitle;

            // 4. Register worklist + RM inline dalam 1 call.
            //    rm.id = no_rkm_medis Khanza → mapping stabil, alat_rs auto-create
            //    rm kalau belum ada.
            JsonNode rw = RegisterWorklistWithRm(
                    noRkmMedisKhanza, namaPasien, nikPasien, jkPasien, tglLahirPasien, patientIHS,
                    tmid, idPemeriksaan, aeTitle, tanggal, jamHHmmss, encounterId);
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
                h.pesan = "Berhasil. Accession: " + h.accessionNumber + " | Status: " + h.satuSehatStatus
                        + " | Alat: " + (h.aeTitle.isEmpty() ? "(diatur RIS)" : h.aeTitle);
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

    /**
     * GET JSON (Bearer auth) terpusat. Kembalikan node hasil parse (sukses ATAU
     * error body), atau null kalau gagal total.
     */
    private JsonNode getJsonAuth(String logName, String url) {
        try {
            ensureToken();
            HttpEntity re = new HttpEntity(bearerHeaders());
            String body = getRest().exchange(url, HttpMethod.GET, re, String.class).getBody();
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
