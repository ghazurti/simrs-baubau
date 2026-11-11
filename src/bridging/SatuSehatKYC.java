package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.URI;
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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Verifikasi Profil / KYC pasien SATUSEHAT Mobile di fasyankes.
 *
 * Port Java dari "kyc-library-php v2.0.0" Kemenkes (function.php):
 * - RSA 2048 milik fasyankes (public key dikirim ke SATUSEHAT, private key untuk membuka respon)
 * - pesan dienkripsi AES-256-GCM (iv 12 byte | ciphertext | tag 16 byte)
 * - kunci AES dibungkus RSA-OAEP (SHA-256, MGF1 SHA-256) dengan public key SATUSEHAT
 * - format: -----BEGIN ENCRYPTED MESSAGE-----\r\n base64 (76 kolom, \r\n) -----END ENCRYPTED MESSAGE-----
 * Petugas (agent) diambil dari pegawai yang sedang login.
 */
public class SatuSehatKYC extends JDialog {

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

    private final JFXPanel panelWeb = new JFXPanel();
    private WebEngine engine;
    private String urlKYC = "";
    private final JLabel status = new JLabel(" ");

    public SatuSehatKYC(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Verifikasi Profil (KYC) Pasien SATUSEHAT Mobile ]::");
        setLayout(new BorderLayout(1, 1));
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnMuat = new JButton("Muat Ulang");
        JButton btnBrowser = new JButton("Buka di Browser");
        JButton btnTutup = new JButton("Tutup");
        atas.add(btnMuat);
        atas.add(btnBrowser);
        atas.add(btnTutup);
        atas.add(status);
        add(atas, BorderLayout.NORTH);
        add(panelWeb, BorderLayout.CENTER);
        setSize(new Dimension(1100, 720));
        setLocationRelativeTo(parent);

        btnMuat.addActionListener(e -> mulai());
        btnTutup.addActionListener(e -> dispose());
        btnBrowser.addActionListener(e -> {
            try {
                if (!urlKYC.equals("")) {
                    Desktop.getDesktop().browse(new URI(urlKYC));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal membuka browser : " + ex.getMessage());
            }
        });

        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            WebView view = new WebView();
            engine = view.getEngine();
            engine.setJavaScriptEnabled(true);
            engine.setCreatePopupHandler(p -> engine);
            engine.setOnAlert(e -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, e.getData())));
            panelWeb.setScene(new Scene(view));
        });
    }

    /** Ambil URL validasi KYC untuk petugas yang login lalu tampilkan. */
    public void mulai() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        status.setText("Meminta halaman verifikasi ke SATUSEHAT...");
        new Thread(() -> {
            try {
                sekuel Sequel = new sekuel();
                String nik = Sequel.cariIsi("select pegawai.no_ktp from pegawai where pegawai.nik=?", akses.getkode()).trim();
                String nama = Sequel.cariIsi("select pegawai.nama from pegawai where pegawai.nik=?", akses.getkode()).trim();
                if (nik.equals("") || nik.equals("-")) {
                    throw new Exception("No. KTP petugas yang login belum diisi di Data Pegawai.\nVerifikasi KYC harus dilakukan oleh petugas (bukan akun Admin) dengan NIK KTP terisi.");
                }
                ApiSatuSehat api = new ApiSatuSehat();
                String url = generateUrl(nama, nik, api.TokenSatuSehat(), urlGenerate(koneksiDB.URLAUTHSATUSEHAT()), isProduction(koneksiDB.URLAUTHSATUSEHAT()), api.getRest());
                urlKYC = url;
                Platform.runLater(() -> engine.load(url));
                SwingUtilities.invokeLater(() -> status.setText("Petugas : " + nama));
            } catch (Exception e) {
                System.out.println("Notifikasi KYC SATUSEHAT : " + e);
                SwingUtilities.invokeLater(() -> {
                    status.setText("Gagal");
                    JOptionPane.showMessageDialog(this, "Gagal memuat verifikasi KYC : " + e.getMessage());
                });
            } finally {
                SwingUtilities.invokeLater(() -> setCursor(Cursor.getDefaultCursor()));
            }
        }).start();
    }

    // ---------------- API & kriptografi (sesuai kyc-library-php Kemenkes) ----------------

    /** https://.../oauth2/v1  ->  https://.../kyc/v1/generate-url */
    public static String urlGenerate(String urlAuth) {
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

    public static boolean isProduction(String urlAuth) {
        return urlAuth == null || !(urlAuth.contains("-stg") || urlAuth.contains("-dev"));
    }

    /** Panggil generate-url, kembalikan data.url hasil dekripsi. */
    public static String generateUrl(String namaPetugas, String nikPetugas, String token, String apiUrl, boolean production, RestTemplate rest) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048, ACAK);
        KeyPair kp = kpg.generateKeyPair();

        ObjectMapper mapper = new ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode isi = mapper.createObjectNode();
        isi.put("agent_name", namaPetugas);
        isi.put("agent_nik", nikPetugas);
        isi.put("public_key", publicKeyPem(kp.getPublic()));
        String payload = enkripsi(mapper.writeValueAsString(isi), production ? PUBKEY_PRODUCTION : PUBKEY_DEVELOPMENT);

        HttpHeaders hdr = new HttpHeaders();
        hdr.setContentType(MediaType.TEXT_PLAIN);
        hdr.add("X-Debug-Mode", "0");
        hdr.add("Authorization", "Bearer " + token);
        String respon;
        try {
            respon = rest.exchange(apiUrl, HttpMethod.POST, new HttpEntity<>(payload, hdr), String.class).getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            respon = ex.getResponseBodyAsString();
        } catch (org.springframework.web.client.ResourceAccessException ex) {
            throw new Exception("Akses ke SATUSEHAT ditolak/gagal (cek client_id, client_secret & token) : " + ex.getMessage());
        }
        if (respon == null || !respon.trim().startsWith(BEGIN)) {
            throw new Exception("Respon SATUSEHAT : " + respon);
        }
        String json = dekripsi(respon.trim(), kp.getPrivate());
        JsonNode r = mapper.readTree(json);
        String url = r.path("data").path("url").asText();
        if (url.equals("")) {
            String pesanError = r.path("metadata").path("message").asText();
            throw new Exception(pesanError.equals("") ? json : pesanError);
        }
        return url;
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

        // sama dengan chunk_split(base64_encode(...)) PHP: baris 76 kolom, tiap baris diakhiri \r\n
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
