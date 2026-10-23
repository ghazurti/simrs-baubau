package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fungsi.koneksiDB;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Viewer PACS alat_rs (viewer-v3) lewat iframe + postMessage, sesuai
 * "Panduan Integrasi Embed Viewer PACS" Serova.
 *
 * Khanza menjalankan server HTTP kecil di http://localhost:8081 (origin yang
 * di-whitelist PACS). Browser dibuka ke halaman induk berisi iframe
 * URLVIEWERALATRS (default URLALATRS:3000)/viewer-v3/{accession}; saat viewer kirim ris:embed-ready /
 * ris:embed-unauthorized, halaman mengambil token JWT alat_rs dari server lokal ini
 * lalu membalas ris:embed-auth. Token tidak pernah ada di URL; URL hanya membawa
 * tiket acak yang berlaku 15 menit dan hanya bisa ditukar di localhost.
 */
public final class PacsEmbedViewer {
    public static final int PORT = 8081;
    private static final long UMUR_TIKET = 15L * 60 * 1000;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SecureRandom ACAK = new SecureRandom();
    private static final Map<String, Tiket> TIKET = new ConcurrentHashMap<>();
    private static HttpServer server;

    private static final class Tiket {
        final String accession, judul;
        final long dibuat = System.currentTimeMillis();
        Tiket(String accession, String judul) { this.accession = accession; this.judul = judul; }
    }

    private PacsEmbedViewer() {}

    /** buka study di browser (id = ID worklist RIS, judul = teks judul halaman); melempar Exception kalau gagal */
    public static void buka(String accession, String judul) throws Exception {
        if (accession == null || accession.trim().isEmpty()) throw new IllegalArgumentException("Accession kosong");
        byte[] b = new byte[24];
        ACAK.nextBytes(b);
        StringBuilder t = new StringBuilder();
        for (byte x : b) t.append(String.format("%02x", x));
        if (setting("MODEVIEWERALATRS").equalsIgnoreCase("webapps")) {
            bukaWebapps(t.toString(), accession.trim(), judul == null ? "" : judul);
            return;
        }
        mulai();
        bersihkan();
        TIKET.put(t.toString(), new Tiket(accession.trim(), judul == null ? "" : judul));
        java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:" + PORT + "/pacs?t=" + t));
    }

    /**
     * Mode webapps (MODEVIEWERALATRS=webapps): tiket disimpan di tabel pacs_embed_tiket (2 menit),
     * browser dibuka ke webapps/pacs/viewer.php yang menukar tiket dengan sesi lalu mengambil token
     * dari server (setting di webapps/pacs/conf_pacs.php). Origin webapps harus di-whitelist Serova.
     */
    private static void bukaWebapps(String tiket, String accession, String judul) throws Exception {
        java.sql.Connection kon = koneksiDB.condb();
        try (java.sql.Statement st = kon.createStatement()) {
            st.executeUpdate("create table if not exists pacs_embed_tiket (tiket char(48) not null,accession varchar(50) not null,"
                    + "judul varchar(200) not null default '',username varchar(60) not null default '',dibuat datetime not null,"
                    + "kedaluwarsa datetime not null,primary key (tiket),key kedaluwarsa (kedaluwarsa)) engine=InnoDB");
            st.executeUpdate("delete from pacs_embed_tiket where kedaluwarsa<now()");
        }
        try (java.sql.PreparedStatement ps = kon.prepareStatement(
                "insert into pacs_embed_tiket (tiket,accession,judul,username,dibuat,kedaluwarsa) values (?,?,?,?,now(),now()+interval 2 minute)")) {
            ps.setString(1, tiket);
            ps.setString(2, accession);
            ps.setString(3, judul.length() > 200 ? judul.substring(0, 200) : judul);
            ps.setString(4, String.valueOf(fungsi.akses.getkode()));
            ps.executeUpdate();
        }
        java.awt.Desktop.getDesktop().browse(new java.net.URI("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB()
                + "/" + koneksiDB.HYBRIDWEB() + "/pacs/viewer.php?t=" + tiket));
    }

    /**
     * cadangan kalau server lokal gagal: viewer-v3 langsung tanpa auth embed (bisa minta login).
     * Route lama API /viewer?accession= tidak ada (404 "Route tidak ditemukan").
     */
    public static String urlViewerLama(String accession) throws Exception {
        return viewerBase() + "/viewer-v3/" + URLEncoder.encode(accession.trim(), "UTF-8").replace("+", "%20");
    }

    private static String baseUrl() {
        String url = koneksiDB.URLALATRS(), port = koneksiDB.PORTALATRS();
        return (port == null || port.isEmpty()) ? url : url + ":" + port;
    }

    /**
     * Alamat aplikasi viewer (frontend RIS), beda port dengan API alat_rs.
     * Bisa diatur di setting/database.xml: <entry key="URLVIEWERALATRS">http://172.17.10.3:3000</entry>;
     * kosong -> URLALATRS port 3000.
     */
    private static String viewerBase() {
        String v = setting("URLVIEWERALATRS");
        if (v.isEmpty()) v = koneksiDB.URLALATRS() + ":3000";
        return v.endsWith("/") ? v.substring(0, v.length() - 1) : v;
    }

    /**
     * backendHost yang dikirim ke viewer (base URL API PACS). Bisa diatur di
     * setting/database.xml: <entry key="BACKENDVIEWERALATRS">...</entry>; kosong -> URLALATRS:PORTALATRS/api.
     */
    private static String backendHost() {
        String v = setting("BACKENDVIEWERALATRS");
        if (v.isEmpty()) v = baseUrl() + "/api";
        return v.endsWith("/") ? v.substring(0, v.length() - 1) : v;
    }

    private static String setting(String kunci) {
        try (java.io.FileInputStream fis = new java.io.FileInputStream("setting/database.xml")) {
            java.util.Properties p = new java.util.Properties();
            p.loadFromXML(fis);
            return p.getProperty(kunci, "").trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static synchronized void mulai() throws Exception {
        if (server != null) return;
        HttpServer s = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), PORT), 0);
        s.createContext("/pacs", PacsEmbedViewer::layani);
        s.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread th = new Thread(r, "pacs-embed");
            th.setDaemon(true);
            return th;
        }));
        s.start();
        server = s;
    }

    private static void bersihkan() {
        long batas = System.currentTimeMillis() - UMUR_TIKET;
        for (Iterator<Tiket> it = TIKET.values().iterator(); it.hasNext();) {
            if (it.next().dibuat < batas) it.remove();
        }
    }

    private static void layani(HttpExchange ex) {
        try {
            // tolak DNS rebinding: hanya Host localhost / 127.0.0.1
            String host = String.valueOf(ex.getRequestHeaders().getFirst("Host"));
            if (!host.equals("localhost:" + PORT) && !host.equals("127.0.0.1:" + PORT)) {
                kirim(ex, 403, "text/plain", "Forbidden");
                return;
            }
            String q = ex.getRequestURI().getRawQuery(), t = "";
            if (q != null) for (String p : q.split("&")) if (p.startsWith("t=")) t = p.substring(2);
            Tiket tk = TIKET.get(t);
            if (tk == null || tk.dibuat < System.currentTimeMillis() - UMUR_TIKET) {
                kirim(ex, 410, "text/html; charset=utf-8",
                        "<!doctype html><meta charset=utf-8><body style='font-family:sans-serif;padding:24px'>"
                        + "Tautan viewer sudah kedaluwarsa. Silahkan buka lagi dari SIMRS.</body>");
                return;
            }
            String path = ex.getRequestURI().getPath();
            if (path.equals("/pacs/token")) {
                if (!"POST".equals(ex.getRequestMethod())) { kirim(ex, 405, "text/plain", "POST only"); return; }
                kirim(ex, 200, "application/json", token());
            } else if (path.equals("/pacs")) {
                kirim(ex, 200, "text/html; charset=utf-8", halaman(t, tk));
            } else {
                kirim(ex, 404, "text/plain", "Not found");
            }
        } catch (Exception e) {
            System.out.println("PACS embed : " + e);
            try { kirim(ex, 500, "application/json", "{\"error\":\"" + e.getClass().getSimpleName() + "\"}"); } catch (Exception ignore) {}
        }
    }

    /** login ulang tiap permintaan supaya token selalu baru (juga untuk ris:embed-unauthorized) */
    private static String token() throws Exception {
        ObjectNode o = MAPPER.createObjectNode();
        String tok;
        JsonNode d;
        String urlLogin = setting("TOKENLOGINVIEWERALATRS");
        if (urlLogin.isEmpty()) {
            ApiAlatRS api = new ApiAlatRS();
            tok = api.Login();
            if (tok == null || tok.isEmpty()) {
                o.put("error", "Login alat_rs gagal: " + api.lastLoginError);
                return MAPPER.writeValueAsString(o);
            }
            d = api.loginData == null ? MAPPER.createObjectNode() : api.loginData;
        } else {
            // login ke backend viewer lain (mis. cloud api-mantap) dengan user/pass alat_rs
            try {
                d = loginLain(urlLogin);
            } catch (Exception e) {
                o.put("error", "Login PACS gagal: " + e.getMessage());
                return MAPPER.writeValueAsString(o);
            }
            tok = teks(d, "token", "access_token", "accessToken", "jwt");
            if (tok.isEmpty()) {
                o.put("error", "Balasan login PACS tanpa token");
                return MAPPER.writeValueAsString(o);
            }
        }
        JsonNode u = d.path("user").isObject() ? d.path("user") : d;
        ObjectNode user = o.putObject("user");
        user.put("id", teks(u, "id", "user_id", "id_user"));
        user.put("username", teks(u, "username", "user_name", "nama_user").isEmpty() ? koneksiDB.USERALATRS() : teks(u, "username", "user_name", "nama_user"));
        user.put("role", teks(u, "role", "role_name", "level"));
        o.put("token", tok);
        o.put("backendHost", backendHost());
        return MAPPER.writeValueAsString(o);
    }

    /** POST {username,password} ke URL login; kembalikan node yang berisi token (data[0] / data / root) */
    private static JsonNode loginLain(String urlLogin) throws Exception {
        java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(urlLogin).openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(10000);
        c.setReadTimeout(15000);
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");
        ObjectNode body = MAPPER.createObjectNode();
        body.put("username", koneksiDB.USERALATRS());
        body.put("password", koneksiDB.PASSALATRS());
        try (OutputStream os = c.getOutputStream()) { os.write(MAPPER.writeValueAsBytes(body)); }
        int kode = c.getResponseCode();
        java.io.InputStream in = kode < 400 ? c.getInputStream() : c.getErrorStream();
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        if (in != null) {
            byte[] b = new byte[4096];
            for (int n; (n = in.read(b)) > 0;) buf.write(b, 0, n);
            in.close();
        }
        String isi = new String(buf.toByteArray(), StandardCharsets.UTF_8);
        if (kode >= 400) throw new Exception("HTTP " + kode + " " + (isi.length() > 150 ? isi.substring(0, 150) : isi));
        JsonNode r = MAPPER.readTree(isi);
        JsonNode data = r.path("data");
        if (data.isArray() && data.size() > 0) return data.get(0);
        if (data.isObject()) return data;
        return r;
    }

    private static String teks(JsonNode n, String... kunci) {
        for (String k : kunci) {
            JsonNode v = n.path(k);
            if (v.isValueNode() && !v.asText().isEmpty()) return v.asText();
            if (v.isObject() && v.path("name").isValueNode()) return v.path("name").asText();
        }
        return "";
    }

    private static String halaman(String t, Tiket tk) throws Exception {
        String viewer = viewerBase() + "/viewer-v3/" + URLEncoder.encode(tk.accession, "UTF-8").replace("+", "%20");
        ObjectNode cfg = MAPPER.createObjectNode();
        cfg.put("viewer", viewer);
        cfg.put("tiket", t);
        String json = MAPPER.writeValueAsString(cfg).replace("</", "<\\/");
        String judul = html(tk.judul.isEmpty() ? tk.accession : tk.judul);
        return "<!doctype html><html><head><meta charset=utf-8>"
            + "<meta name=referrer content=no-referrer>"
            + "<title>Viewer Radiologi - " + judul + "</title>"
            + "<style>html,body{margin:0;height:100%;background:#111;color:#eee;font-family:Tahoma,sans-serif}"
            + "#bar{height:32px;line-height:32px;padding:0 12px;font-size:13px;background:#1f3b57;display:flex;justify-content:space-between}"
            + "#st{color:#ffd27f}iframe{width:100%;height:calc(100% - 32px);border:0;display:block}</style></head><body>"
            + "<div id=bar><span>Viewer Radiologi - " + judul + "</span><span id=st>Menghubungkan ke PACS...</span></div>"
            + "<iframe id=pacs allow=fullscreen allowfullscreen referrerpolicy=no-referrer></iframe>"
            + "<script>(function(){var C=" + json + ";"
            + "var O=new URL(C.viewer).origin,f=document.getElementById('pacs'),st=document.getElementById('st'),retry=0;"
            + "function auth(){fetch('/pacs/token?t='+C.tiket,{method:'POST'}).then(function(r){return r.json();}).then(function(d){"
            + "if(!d.token){st.textContent=d.error||'Gagal ambil token';return;}"
            + "f.contentWindow.postMessage({type:'ris:embed-auth',token:d.token,user:d.user,backendHost:d.backendHost},O);"
            + "st.textContent='';}).catch(function(e){st.textContent='Gagal ambil token: '+e;});}"
            + "window.addEventListener('message',function(e){"
            + "if(e.origin!==O||e.source!==f.contentWindow)return;var ty=e.data&&e.data.type;"
            + "if(ty==='ris:embed-ready'){retry=0;auth();}"
            + "else if(ty==='ris:embed-unauthorized'){if(retry++<2)auth();else st.textContent='Token ditolak PACS';}});"
            + "window.addEventListener('pagehide',function(){f.src='about:blank';});"
            + "f.src=C.viewer;})();</script></body></html>";
    }

    private static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static void kirim(HttpExchange ex, int kode, String tipe, String isi) throws Exception {
        byte[] b = isi.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", tipe);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.getResponseHeaders().set("Referrer-Policy", "no-referrer");
        ex.sendResponseHeaders(kode, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
}
