package khanzacetakantrianloket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

/**
 * Verifikasi Profil (KYC) pasien SATUSEHAT Mobile untuk anjungan antrian loket.
 * Port dari bridging.SatuSehatKYC (SIMRS Khanza utama) = port Java kyc-library-php v2.0.0 Kemenkes.
 * Tanpa Spring/JavaFX: HTTP pakai HttpURLConnection, halaman KYC dibuka di browser bawaan
 * (butuh kamera untuk liveness, yang tidak didukung WebView).
 * Kompatibel Java 8.
 */
public final class KYCSatuSehat {

    private static final String PUBKEY_PRODUCTION =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxLwvebfOrPLIODIxAwFp"
            + "4Qhksdtn7bEby5OhkQNLTdClGAbTe2tOO5Tiib9pcdruKxTodo481iGXTHR5033I"
            + "A5X55PegFeoY95NH5Noj6UUhyTFfRuwnhtGJgv9buTeBa4pLgHakfebqzKXr0Lce"
            + "/Ff1MnmQAdJTlvpOdVWJggsb26fD3cXyxQsbgtQYntmek2qvex/gPM9Nqa5qYrXx"
            + "8KuGuqHIFQa5t7UUH8WcxlLVRHWOtEQ3+Y6TQr8sIpSVszfhpjh9+Cag1EgaMzk+"
            + "HhAxMtXZgpyHffGHmPJ9eXbBO008tUzrE88fcuJ5pMF0LATO6ayXTKgZVU0WO/4e"
            + "iQIDAQAB";

    private static final String PUBKEY_DEVELOPMENT =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwqoicEXIYWYV3PvLIdvB"
            + "qFkHn2IMhPGKTiB2XA56enpPb0UbI9oHoetRF41vfwMqfFsy5Yd5LABxMGyHJBbP"
            + "+3fk2/PIfv+7+9/dKK7h1CaRTeT4lzJBiUM81hkCFlZjVFyHUFtaNfvQeO2OYb7U"
            + "kK5JrdrB4sgf50gHikeDsyFUZD1o5JspdlfqDjANYAhfz3aam7kCjfYvjgneqkV8"
            + "pZDVqJpQA3MHAWBjGEJ+R8y03hs0aafWRfFG9AcyaA5Ct5waUOKHWWV9sv5DQXmb"
            + "EAoqcx0ZPzmHJDQYlihPW4FIvb93fMik+eW8eZF3A920DzuuFucpblWU9J9o5w+2"
            + "oQIDAQAB";

    private static final String BEGIN = "-----BEGIN ENCRYPTED MESSAGE-----";
    private static final String END = "-----END ENCRYPTED MESSAGE-----";
    private static final SecureRandom ACAK = new SecureRandom();
    private static final OAEPParameterSpec OAEP = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    private KYCSatuSehat() {
    }

    /** Alur lengkap: token -> generate-url -> URL halaman KYC. */
    public static String ambilUrl(String urlAuth, String clientId, String secret, String namaPetugas, String nikPetugas) throws Exception {
        if (urlAuth == null || urlAuth.trim().equals("")) {
            throw new Exception("URLAUTHSATUSEHAT belum diisi di setting/database.xml");
        }
        if (clientId.equals("") || secret.equals("")) {
            throw new Exception("CLIENTIDSATUSEHAT / SECRETKEYSATUSEHAT belum diisi di setting/database.xml\n(salin dari setting/database.xml SIMRS Khanza)");
        }
        String token = token(urlAuth.trim(), clientId, secret);
        return generateUrl(namaPetugas, nikPetugas, token, urlGenerate(urlAuth), isProduction(urlAuth));
    }

    static String token(String urlAuth, String clientId, String secret) throws Exception {
        String body = "client_id=" + URLEncoder.encode(clientId, "UTF-8") + "&client_secret=" + URLEncoder.encode(secret, "UTF-8");
        String[] r = post(urlAuth + "/accesstoken?grant_type=client_credentials", "application/x-www-form-urlencoded", null, body);
        String tok = new ObjectMapper().readTree(r[1]).path("access_token").asText();
        if (tok.equals("")) {
            throw new Exception("Gagal mengambil token SATUSEHAT (HTTP " + r[0] + ") : " + r[1]);
        }
        return tok;
    }

    /** https://.../oauth2/v1  ->  https://.../kyc/v1/generate-url */
    static String urlGenerate(String urlAuth) {
        String dasar = urlAuth == null ? "" : urlAuth.trim();
        int i = dasar.indexOf("/oauth2");
        if (i > 0) {
            dasar = dasar.substring(0, i);
        }
        if (dasar.equals("")) {
            dasar = "https://api-satusehat.kemkes.go.id";
        }
        return dasar + "/kyc/v1/generate-url";
    }

    static boolean isProduction(String urlAuth) {
        return urlAuth == null || !(urlAuth.contains("-stg") || urlAuth.contains("-dev"));
    }

    static String generateUrl(String namaPetugas, String nikPetugas, String token, String apiUrl, boolean production) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048, ACAK);
        KeyPair kp = kpg.generateKeyPair();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode isi = mapper.createObjectNode();
        isi.put("agent_name", namaPetugas);
        isi.put("agent_nik", nikPetugas);
        isi.put("public_key", publicKeyPem(kp.getPublic()));
        String payload = enkripsi(mapper.writeValueAsString(isi), production ? PUBKEY_PRODUCTION : PUBKEY_DEVELOPMENT);

        String[] r = post(apiUrl, "text/plain", token, payload);
        String respon = r[1] == null ? "" : r[1].trim();
        if (!respon.startsWith(BEGIN)) {
            throw new Exception("Respon SATUSEHAT (HTTP " + r[0] + ") : " + respon);
        }
        String json = dekripsi(respon, kp.getPrivate());
        JsonNode hasil = mapper.readTree(json);
        String url = hasil.path("data").path("url").asText();
        if (url.equals("")) {
            String pesan = hasil.path("metadata").path("message").asText();
            throw new Exception(pesan.equals("") ? json : pesan);
        }
        return url;
    }

    /** @return {kode HTTP, isi respon} — isi respon error juga dikembalikan. */
    private static String[] post(String alamat, String contentType, String bearer, String body) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(alamat).openConnection();
        try {
            c.setConnectTimeout(20000);
            c.setReadTimeout(30000);
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", contentType);
            c.setRequestProperty("X-Debug-Mode", "0");
            if (bearer != null) {
                c.setRequestProperty("Authorization", "Bearer " + bearer);
            }
            OutputStream os = c.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.close();
            int kode = c.getResponseCode();
            InputStream in = kode >= 400 ? c.getErrorStream() : c.getInputStream();
            String isi = "";
            if (in != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) > 0) {
                    bos.write(buf, 0, n);
                }
                in.close();
                isi = new String(bos.toByteArray(), StandardCharsets.UTF_8);
            }
            return new String[]{String.valueOf(kode), isi};
        } finally {
            c.disconnect();
        }
    }

    static String publicKeyPem(PublicKey pk) {
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(pk.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + b64 + "\n-----END PUBLIC KEY-----";
    }

    static String enkripsi(String pesan, String pubKeyServerB64) throws Exception {
        PublicKey server = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pubKeyServerB64)));
        byte[] aesKey = new byte[32];
        ACAK.nextBytes(aesKey);

        Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPPadding");
        rsa.init(Cipher.ENCRYPT_MODE, server, OAEP);
        byte[] kunciTerbungkus = rsa.doFinal(aesKey);

        byte[] iv = new byte[12];
        ACAK.nextBytes(iv);
        Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
        aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new GCMParameterSpec(128, iv));
        byte[] ctTag = aes.doFinal(pesan.getBytes(StandardCharsets.UTF_8));

        byte[] semua = new byte[kunciTerbungkus.length + iv.length + ctTag.length];
        System.arraycopy(kunciTerbungkus, 0, semua, 0, kunciTerbungkus.length);
        System.arraycopy(iv, 0, semua, kunciTerbungkus.length, iv.length);
        System.arraycopy(ctTag, 0, semua, kunciTerbungkus.length + iv.length, ctTag.length);

        // sama dengan chunk_split(base64_encode(...)) PHP: baris 76 kolom diakhiri \r\n
        String b64 = Base64.getEncoder().encodeToString(semua);
        StringBuilder sb = new StringBuilder(BEGIN).append("\r\n");
        for (int i = 0; i < b64.length(); i += 76) {
            sb.append(b64, i, Math.min(b64.length(), i + 76)).append("\r\n");
        }
        return sb.append(END).toString();
    }

    static String dekripsi(String pesan, PrivateKey milikKita) throws Exception {
        int a = pesan.indexOf(BEGIN), b = pesan.lastIndexOf(END);
        if (a < 0 || b < 0) {
            throw new Exception("Format pesan terenkripsi tidak dikenali");
        }
        byte[] data = Base64.getMimeDecoder().decode(pesan.substring(a + BEGIN.length(), b).trim());
        byte[] kunciTerbungkus = Arrays.copyOfRange(data, 0, 256);
        byte[] sisa = Arrays.copyOfRange(data, 256, data.length);

        Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPPadding");
        rsa.init(Cipher.DECRYPT_MODE, milikKita, OAEP);
        byte[] aesKey = rsa.doFinal(kunciTerbungkus);

        Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
        aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new GCMParameterSpec(128, Arrays.copyOfRange(sisa, 0, 12)));
        return new String(aes.doFinal(Arrays.copyOfRange(sisa, 12, sisa.length)), StandardCharsets.UTF_8);
    }
}
