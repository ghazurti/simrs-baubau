package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Pastikan Encounter SATUSEHAT ada untuk sebuah no_rawat, dipakai saat kirim
 * permintaan radiologi ke RIS supaya RIS bisa refer ke Encounter yang SAMA
 * (tidak bikin duplikat). Encounter dibuat dengan status "arrived" — sesuai
 * lifecycle SATUSEHAT (pasien datang), tanpa nunggu tutup billing.
 *
 * Meniru payload non-ANC dari SatuSehatKirimEncounter, memakai helper yang
 * sama (SatuSehatCekNIK untuk resolve IHS, ApiSatuSehat untuk token).
 *
 * Best-effort: kalau ada prasyarat yang kurang (IHS belum resolve / poli belum
 * dipetakan / error) → return "" dan isi {@link #lastError}, TANPA melempar
 * exception. Caller (radiologi) tetap bisa lanjut kirim worklist tanpa encounter.
 */
public class AlatRSEncounter {

    private final ApiSatuSehat api = new ApiSatuSehat();
    private final SatuSehatCekNIK cekNIK = new SatuSehatCekNIK();
    private final ObjectMapper mapper = new ObjectMapper();

    public String lastError = "";

    /**
     * @return id_encounter (baru atau yang sudah ada), atau "" kalau gagal.
     *         Cek {@link #lastError} untuk alasan kalau "".
     */
    public String pastikan(String noRawat) {
        lastError = "";
        if (noRawat == null || noRawat.isEmpty()) {
            lastError = "No.Rawat kosong";
            return "";
        }

        // 1. Sudah ada?
        String existing = getExisting(noRawat);
        if (!existing.isEmpty()) {
            return existing;
        }

        // 2. Ambil data kunjungan (ralan) untuk bangun Encounter.
        String nmPasien = "", nikPasien = "", nmDokter = "", nikDokter = "",
               nmPoli = "", idLokasi = "", statusLanjut = "", periodStart = "";
        try (Connection kon = koneksiDB.condb();
             PreparedStatement ps = kon.prepareStatement(
                "select pasien.nm_pasien, pasien.no_ktp as nik_pasien, "+
                "pegawai.nama as nm_dokter, pegawai.no_ktp as nik_dokter, "+
                "poliklinik.nm_poli, ifnull(satu_sehat_mapping_lokasi_ralan.id_lokasi_satusehat,'') as id_lokasi, "+
                "reg_periksa.status_lanjut, "+
                "concat(reg_periksa.tgl_registrasi,'T',reg_periksa.jam_reg,'+07:00') as period_start "+
                "from reg_periksa "+
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join pegawai on pegawai.nik=reg_periksa.kd_dokter "+
                "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                "left join satu_sehat_mapping_lokasi_ralan on satu_sehat_mapping_lokasi_ralan.kd_poli=poliklinik.kd_poli "+
                "where reg_periksa.no_rawat=?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nmPasien   = nz(rs.getString("nm_pasien"));
                    nikPasien  = nz(rs.getString("nik_pasien"));
                    nmDokter   = nz(rs.getString("nm_dokter"));
                    nikDokter  = nz(rs.getString("nik_dokter"));
                    nmPoli     = nz(rs.getString("nm_poli"));
                    idLokasi   = nz(rs.getString("id_lokasi"));
                    statusLanjut = nz(rs.getString("status_lanjut"));
                    periodStart  = nz(rs.getString("period_start"));
                } else {
                    lastError = "Data kunjungan (reg_periksa) tidak ditemukan untuk No.Rawat " + noRawat;
                    return "";
                }
            }
        } catch (Exception e) {
            lastError = "Gagal query data kunjungan: " + e.getMessage();
            return "";
        }

        // 3. Prasyarat SATUSEHAT.
        if (idLokasi.isEmpty()) {
            lastError = "Poli \"" + nmPoli + "\" belum dipetakan ke lokasi SATUSEHAT (satu_sehat_mapping_lokasi_ralan)";
            return "";
        }
        String idPasien = "", idDokter = "";
        try {
            idDokter = nz(cekNIK.tampilIDParktisi(nikDokter));
            idPasien = nz(cekNIK.tampilIDPasien(nikPasien));
        } catch (Exception e) {
            lastError = "Gagal resolve IHS: " + e.getMessage();
            return "";
        }
        if (idPasien.isEmpty()) {
            lastError = "IHS pasien belum ada (NIK " + nikPasien + "). Resolusi Patient SATUSEHAT dulu.";
            return "";
        }
        if (idDokter.isEmpty()) {
            lastError = "IHS praktisi belum ada (NIK dokter " + nikDokter + "). Resolusi Practitioner SATUSEHAT dulu.";
            return "";
        }

        // 4. Bangun & POST Encounter (status arrived), mengikuti payload non-ANC Khanza.
        String kelasKode = "Ranap".equalsIgnoreCase(statusLanjut) ? "IMP" : "AMB";
        String kelasDisplay = "Ranap".equalsIgnoreCase(statusLanjut) ? "inpatient encounter" : "ambulatory";
        String link = koneksiDB.URLFHIRSATUSEHAT();
        String org = koneksiDB.IDSATUSEHAT();
        String json = "{" +
            "\"resourceType\": \"Encounter\"," +
            "\"status\": \"arrived\"," +
            "\"class\": {" +
                "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ActCode\"," +
                "\"code\": \"" + kelasKode + "\"," +
                "\"display\": \"" + kelasDisplay + "\"" +
            "}," +
            "\"subject\": { \"reference\": \"Patient/" + idPasien + "\", \"display\": \"" + esc(nmPasien) + "\" }," +
            "\"participant\": [{" +
                "\"type\": [{ \"coding\": [{ \"system\": \"http://terminology.hl7.org/CodeSystem/v3-ParticipationType\", \"code\": \"ATND\", \"display\": \"attender\" }] }]," +
                "\"individual\": { \"reference\": \"Practitioner/" + idDokter + "\", \"display\": \"" + esc(nmDokter) + "\" }" +
            "}]," +
            "\"period\": { \"start\": \"" + periodStart + "\" }," +
            "\"location\": [{ \"location\": { \"reference\": \"Location/" + idLokasi + "\", \"display\": \"" + esc(nmPoli) + "\" } }]," +
            "\"statusHistory\": [{ \"status\": \"arrived\", \"period\": { \"start\": \"" + periodStart + "\", \"end\": \"" + periodStart + "\" } }]," +
            "\"serviceProvider\": { \"reference\": \"Organization/" + org + "\" }," +
            "\"identifier\": [{ \"system\": \"http://sys-ids.kemkes.go.id/encounter/" + org + "\", \"value\": \"" + esc(noRawat) + "\" }]" +
        "}";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
            HttpEntity req = new HttpEntity(json, headers);
            System.out.println("alat_rs ENCOUNTER REQ  : " + json);
            String body = api.getRest().exchange(link + "/Encounter", HttpMethod.POST, req, String.class).getBody();
            System.out.println("alat_rs ENCOUNTER RESP : " + body);
            JsonNode root = mapper.readTree(body);
            String id = root.path("id").asText();
            if (id == null || id.isEmpty()) {
                lastError = "SATUSEHAT tidak mengembalikan id Encounter: " + body;
                return "";
            }
            simpanEncounter(noRawat, id);
            return id;
        } catch (org.springframework.web.client.HttpStatusCodeException he) {
            lastError = "Encounter ditolak SATUSEHAT (" + he.getStatusCode() + "): " + he.getResponseBodyAsString();
            System.out.println("alat_rs ENCOUNTER HTTP " + he.getStatusCode() + " : " + he.getResponseBodyAsString());
            return "";
        } catch (Exception e) {
            lastError = "Gagal kirim Encounter: " + e.getMessage();
            System.out.println("alat_rs ENCOUNTER ERR : " + e);
            return "";
        }
    }

    private String getExisting(String noRawat) {
        try (Connection kon = koneksiDB.condb();
             PreparedStatement ps = kon.prepareStatement(
                "select ifnull(id_encounter,'') as id_encounter from satu_sehat_encounter where no_rawat=?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return nz(rs.getString("id_encounter"));
            }
        } catch (Exception e) {
            System.out.println("Cek satu_sehat_encounter gagal: " + e);
        }
        return "";
    }

    private void simpanEncounter(String noRawat, String idEncounter) {
        try (Connection kon = koneksiDB.condb();
             PreparedStatement ps = kon.prepareStatement(
                "insert into satu_sehat_encounter (no_rawat, id_encounter) values (?,?) "+
                "on duplicate key update id_encounter=values(id_encounter)")) {
            ps.setString(1, noRawat);
            ps.setString(2, idEncounter);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Gagal simpan satu_sehat_encounter: " + e);
        }
    }

    private String nz(String s) { return s == null ? "" : s; }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
