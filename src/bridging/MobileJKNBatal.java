package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.koneksiDB;
import fungsi.sekuel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Pembatalan antrean BPJS (Antrean RS / Mobile JKN) + pembatalan booking di SIMRS.
 * Dipakai menu Antrean Per Tanggal Mobile JKN dan tombol Batal Antrean JKN di Registrasi.
 */
public final class MobileJKNBatal {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MobileJKNBatal() {}

    /** hasil panggilan BPJS: code "200" = berhasil; code "" = error koneksi (pesan berisi exception) */
    public static final class Hasil {
        public final String code, message;
        Hasil(String code, String message) { this.code = code; this.message = message; }
        public boolean ok() { return "200".equals(code); }
    }

    /** POST {URLAPIMOBILEJKN}/antrean/batal */
    public static Hasil batalBPJS(String kodebooking, String keterangan) {
        try {
            ApiMobileJKN api = new ApiMobileJKN();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-cons-id", koneksiDB.CONSIDAPIMOBILEJKN());
            String utc = String.valueOf(api.GetUTCdatetimeAsString());
            headers.add("x-timestamp", utc);
            headers.add("x-signature", api.getHmac(utc));
            headers.add("user_key", koneksiDB.USERKEYAPIMOBILEJKN());
            ObjectNode body = MAPPER.createObjectNode();
            body.put("kodebooking", kodebooking);
            body.put("keterangan", keterangan);
            String url = koneksiDB.URLAPIMOBILEJKN() + "/antrean/batal";
            System.out.println("[BatalJKN] URL: " + url + " JSON: " + body);
            JsonNode root = MAPPER.readTree(api.getRest().exchange(url, HttpMethod.POST,
                    new HttpEntity<>(MAPPER.writeValueAsString(body), headers), String.class).getBody());
            Hasil h = new Hasil(root.path("metadata").path("code").asText(), root.path("metadata").path("message").asText());
            System.out.println("[BatalJKN] Response code=" + h.code + " message=" + h.message);
            return h;
        } catch (Exception e) {
            System.out.println("[BatalJKN] Error: " + e);
            return new Hasil("", e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    /** registrasi dibatalkan (stts Batal) -> booking Mobile JKN yang masih Belum/Checkin ikut Batal */
    public static void batalBookingRegistrasi(String norawat) {
        new sekuel().mengedittf("referensi_mobilejkn_bpjs", "no_rawat=? and status in ('Belum','Checkin')",
                "status='Batal',validasi=now()", 1, new String[]{norawat});
    }

    /**
     * Batalkan booking Mobile JKN di SIMRS: booking Batal (registrasi tidak diubah),
     * dicatat di referensi_mobilejkn_bpjs_batal (statusKirim "Belum" = service Mobile JKN mencoba batal
     * ke BPJS lagi). Mengembalikan false kalau kodebooking bukan booking Mobile JKN di SIMRS.
     */
    public static boolean batalLokal(String kodebooking, String keterangan, String statusKirim) {
        sekuel Sequel = new sekuel();
        String norawat = Sequel.cariIsi("select no_rawat from referensi_mobilejkn_bpjs where nobooking=?", kodebooking);
        if (norawat.isEmpty() && Sequel.cariInteger("select count(*) from referensi_mobilejkn_bpjs where nobooking=?", kodebooking) == 0) {
            return false;
        }
        // status registrasi (reg_periksa.stts) sengaja tidak diubah: dokter kadang tidak menekan "Sudah"
        Sequel.mengedittf("referensi_mobilejkn_bpjs", "nobooking=?", "status='Batal',validasi=now()", 1, new String[]{kodebooking});
        Sequel.meghapus("referensi_mobilejkn_bpjs_batal", "nobooking", kodebooking);
        Sequel.menyimpan2("referensi_mobilejkn_bpjs_batal", "?,?,?,now(),?,?,?", 6, new String[]{
            Sequel.cariIsi("select norm from referensi_mobilejkn_bpjs where nobooking=?", kodebooking), norawat,
            Sequel.cariIsi("select nomorreferensi from referensi_mobilejkn_bpjs where nobooking=?", kodebooking),
            keterangan, statusKirim, kodebooking
        });
        return true;
    }
}
