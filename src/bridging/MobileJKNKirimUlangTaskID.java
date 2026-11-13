package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Kirim ulang Task ID antrean (updatewaktu) ke BPJS untuk satu kode booking.
 * Waktu task: yang dulu tercatat di referensi_mobilejkn_bpjs_taskid, bila tidak ada diambil
 * dari sumber yang sama dengan KhanzaHMSServiceMobileJKN. Waktu dipaksa naik berurutan.
 */
public final class MobileJKNKirimUlangTaskID {

    private static final String[][] SUMBERTASK = {
        {"3", "select mutasi_berkas.dikirim from mutasi_berkas where mutasi_berkas.no_rawat=? and mutasi_berkas.dikirim<>'0000-00-00 00:00:00'"},
        {"4", "select mutasi_berkas.diterima from mutasi_berkas where mutasi_berkas.no_rawat=? and mutasi_berkas.diterima<>'0000-00-00 00:00:00'"},
        {"5", "select concat(pemeriksaan_ralan.tgl_perawatan,' ',pemeriksaan_ralan.jam_rawat) from pemeriksaan_ralan where pemeriksaan_ralan.no_rawat=? order by pemeriksaan_ralan.tgl_perawatan,pemeriksaan_ralan.jam_rawat limit 1"},
        {"6", "select concat(resep_obat.tgl_perawatan,' ',resep_obat.jam) from resep_obat where resep_obat.tgl_perawatan<>'0000-00-00' and resep_obat.status='ralan' and resep_obat.no_rawat=? order by resep_obat.tgl_perawatan,resep_obat.jam limit 1"},
        {"7", "select concat(resep_obat.tgl_penyerahan,' ',resep_obat.jam_penyerahan) from resep_obat where resep_obat.status='ralan' and resep_obat.no_rawat=? and concat(resep_obat.tgl_penyerahan,' ',resep_obat.jam_penyerahan)<>'0000-00-00 00:00:00' order by resep_obat.tgl_penyerahan,resep_obat.jam_penyerahan limit 1"}
    };

    private MobileJKNKirimUlangTaskID() {
    }

    /** @return laporan teks hasil pengiriman per task */
    public static String kirim(String kodebooking) {
        StringBuilder sb = new StringBuilder("Kode Booking " + kodebooking);
        try {
            Connection k = koneksiDB.condb();
            String norawat = cari(k, "select referensi_mobilejkn_bpjs.no_rawat from referensi_mobilejkn_bpjs where referensi_mobilejkn_bpjs.nobooking=?", kodebooking);
            if (norawat.equals("")) {
                norawat = kodebooking; // antrean onsite: kodebooking = no_rawat
            }
            String stts = cari(k, "select reg_periksa.stts from reg_periksa where reg_periksa.no_rawat=?", norawat);
            if (stts.equals("")) {
                return sb.append(" : No.Rawat tidak ditemukan di SIMRS\n").toString();
            }
            String sep = cari(k, "select bridging_sep.no_sep from bridging_sep where bridging_sep.no_rawat=? order by bridging_sep.tglsep desc limit 1", norawat);
            sb.append(" | No.Rawat ").append(norawat).append(sep.equals("") ? "" : " | SEP " + sep).append("\n");
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ApiMobileJKN api = new ApiMobileJKN();
            String link = koneksiDB.URLAPIMOBILEJKN();

            if (stts.equals("Batal")) {
                String w = cari(k, "select referensi_mobilejkn_bpjs_taskid.waktu from referensi_mobilejkn_bpjs_taskid where referensi_mobilejkn_bpjs_taskid.no_rawat=? and referensi_mobilejkn_bpjs_taskid.taskid='99'", norawat);
                long ms = w.length() < 19 ? System.currentTimeMillis() : fmt.parse(w.substring(0, 19)).getTime();
                return sb.append(kirimTask(api, link, k, kodebooking, norawat, "99", ms, fmt)).toString();
            }
            long sebelumnya = 0;
            int terkirim = 0;
            for (String[] t : SUMBERTASK) {
                String w = cari(k, "select referensi_mobilejkn_bpjs_taskid.waktu from referensi_mobilejkn_bpjs_taskid where referensi_mobilejkn_bpjs_taskid.no_rawat=? and referensi_mobilejkn_bpjs_taskid.taskid='" + t[0] + "'", norawat);
                if (w.equals("")) {
                    w = cari(k, t[1], norawat);
                }
                if (w.length() < 19) {
                    sb.append("  Task ").append(t[0]).append(" : dilewati, data waktu belum ada di SIMRS\n");
                    continue;
                }
                long ms = fmt.parse(w.substring(0, 19)).getTime();
                if (ms <= sebelumnya) {
                    ms = sebelumnya + 60000; // BPJS menolak waktu task yang tidak lebih besar dari task sebelumnya
                }
                sebelumnya = ms;
                sb.append(kirimTask(api, link, k, kodebooking, norawat, t[0], ms, fmt));
                terkirim++;
            }
            if (terkirim == 0) {
                sb.append("  Tidak ada task yang bisa dikirim\n");
            }
        } catch (Exception e) {
            sb.append(" : Gagal ").append(e.getMessage()).append("\n");
        }
        return sb.toString();
    }

    private static String kirimTask(ApiMobileJKN api, String link, Connection k, String kodebooking, String norawat, String taskid, long ms, SimpleDateFormat fmt) {
        String waktu = fmt.format(new Date(ms));
        try {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.add("x-cons-id", koneksiDB.CONSIDAPIMOBILEJKN());
            String utc = String.valueOf(api.GetUTCdatetimeAsString());
            h.add("x-timestamp", utc);
            h.add("x-signature", api.getHmac(utc));
            h.add("user_key", koneksiDB.USERKEYAPIMOBILEJKN());
            String json = "{\"kodebooking\": \"" + kodebooking + "\",\"taskid\": \"" + taskid + "\",\"waktu\": \"" + ms + "\"}";
            System.out.println("Kirim Ulang Task ID : " + json);
            JsonNode meta = new ObjectMapper().readTree(api.getRest().exchange(link + "/antrean/updatewaktu", HttpMethod.POST,
                    new HttpEntity<>(json, h), String.class).getBody()).path("metadata");
            String code = meta.path("code").asText();
            if (code.equals("200") || code.equals("208")) {
                try (PreparedStatement p = k.prepareStatement(
                        "insert into referensi_mobilejkn_bpjs_taskid values(?,?,?) on duplicate key update waktu=values(waktu)")) {
                    p.setString(1, norawat);
                    p.setString(2, taskid);
                    p.setString(3, waktu);
                    p.executeUpdate();
                }
            }
            return "  Task " + taskid + " (" + waktu + ") : " + code + " " + meta.path("message").asText() + "\n";
        } catch (Exception e) {
            return "  Task " + taskid + " (" + waktu + ") : Gagal " + e.getMessage() + "\n";
        }
    }

    private static String cari(Connection k, String sql, String param) {
        try (PreparedStatement p = k.prepareStatement(sql)) {
            p.setString(1, param);
            try (ResultSet r = p.executeQuery()) {
                return r.next() && r.getString(1) != null ? r.getString(1) : "";
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
            return "";
        }
    }
}
