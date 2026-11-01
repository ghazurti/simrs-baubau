package fungsi;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Dikte suara Bahasa Indonesia lewat Chrome (Web Speech API, id-ID), untuk Windows yang dikte
 * bawaannya (Win+H) tidak mendukung Bahasa Indonesia.
 *
 * Khanza menjalankan server HTTP kecil di http://localhost:8082 (hanya dari PC ini). Tombol mic
 * membuka jendela kecil Chrome; setiap kalimat yang selesai diucapkan dikirim ke server ini lalu
 * disisipkan ke kotak teks yang dipilih. Suara diproses oleh layanan pengenal suara Chrome (Google),
 * jadi butuh internet.
 */
public final class DikteSuara {
    public static final int PORT = 8082;
    private static final SecureRandom ACAK = new SecureRandom();
    private static final Map<String, JTextComponent> SESI = new ConcurrentHashMap<>();
    private static HttpServer server;

    private DikteSuara() {}

    /**
     * Dipakai di Windows, kecuali setting/database.xml berisi
     * &lt;entry key="MODEDIKTE"&gt;os&lt;/entry&gt; (kembali ke dikte Windows Win+H).
     */
    public static boolean dipakai() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) return false;
        try (java.io.FileInputStream fis = new java.io.FileInputStream("setting/database.xml")) {
            java.util.Properties p = new java.util.Properties();
            p.loadFromXML(fis);
            return !"os".equalsIgnoreCase(p.getProperty("MODEDIKTE", "").trim());
        } catch (Exception e) {
            return true;
        }
    }

    /** buka jendela dikte untuk kotak teks ini */
    public static void mulai(JTextComponent target) throws Exception {
        if (target == null) return;
        mulaiServer();
        // satu kotak satu sesi: buka ulang di kotak yang sama memakai sesi yang sama
        String sesi = null;
        for (Map.Entry<String, JTextComponent> e : SESI.entrySet()) if (e.getValue() == target) sesi = e.getKey();
        if (sesi == null) {
            byte[] b = new byte[16];
            ACAK.nextBytes(b);
            StringBuilder s = new StringBuilder();
            for (byte x : b) s.append(String.format("%02x", x));
            sesi = s.toString();
            SESI.put(sesi, target);
        }
        bukaChrome("http://localhost:" + PORT + "/dikte?s=" + sesi);
    }

    /** Chrome mode aplikasi (jendela kecil tanpa tab); kalau Chrome tidak ketemu pakai browser default */
    private static void bukaChrome(String url) throws Exception {
        String[] kandidat = {
            System.getenv("ProgramFiles") + "\\Google\\Chrome\\Application\\chrome.exe",
            System.getenv("ProgramFiles(x86)") + "\\Google\\Chrome\\Application\\chrome.exe",
            System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe",
            System.getenv("ProgramFiles(x86)") + "\\Microsoft\\Edge\\Application\\msedge.exe",
            System.getenv("ProgramFiles") + "\\Microsoft\\Edge\\Application\\msedge.exe",
            "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
        };
        for (String k : kandidat) {
            if (k != null && !k.startsWith("null") && new File(k).isFile()) {
                new ProcessBuilder(k, "--app=" + url, "--window-size=460,420").start();
                return;
            }
        }
        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
    }

    private static synchronized void mulaiServer() throws Exception {
        if (server != null) return;
        HttpServer s = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), PORT), 0);
        s.createContext("/dikte", DikteSuara::layani);
        s.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "dikte-suara");
            t.setDaemon(true);
            return t;
        }));
        s.start();
        server = s;
    }

    private static void layani(HttpExchange ex) {
        try {
            String host = String.valueOf(ex.getRequestHeaders().getFirst("Host"));
            if (!host.equals("localhost:" + PORT) && !host.equals("127.0.0.1:" + PORT)) {
                kirim(ex, 403, "text/plain", "Forbidden");
                return;
            }
            String q = ex.getRequestURI().getRawQuery(), sesi = "";
            if (q != null) for (String p : q.split("&")) if (p.startsWith("s=")) sesi = p.substring(2);
            JTextComponent target = SESI.get(sesi);
            if (target == null) {
                kirim(ex, 410, "text/html; charset=utf-8", "<!doctype html><meta charset=utf-8><body style='font-family:sans-serif;padding:20px'>"
                        + "Sesi dikte sudah tidak berlaku. Klik lagi tombol mic di SIMRS.</body>");
                return;
            }
            String path = ex.getRequestURI().getPath();
            if (path.equals("/dikte/teks") && "POST".equals(ex.getRequestMethod())) {
                String teks = baca(ex.getRequestBody()).replaceAll("^[ \\t]+|[ \\t]+$", "");
                if (!teks.trim().isEmpty()) SwingUtilities.invokeLater(() -> sisipkan(target, teks));
                kirim(ex, 200, "text/plain", "ok");
            } else if (path.equals("/dikte")) {
                kirim(ex, 200, "text/html; charset=utf-8", halaman(sesi));
            } else {
                kirim(ex, 404, "text/plain", "Not found");
            }
        } catch (Exception e) {
            System.out.println("Dikte suara : " + e);
            try { kirim(ex, 500, "text/plain", "error"); } catch (Exception ignore) {}
        }
    }

    /** sisipkan di posisi kursor; spasi otomatis, huruf besar di awal kalimat */
    static void sisipkan(JTextComponent target, String teks) {
        if (!target.isEditable() || !target.isEnabled()) return;
        String isi = target.getText();
        int pos = Math.max(0, Math.min(target.getCaretPosition(), isi.length()));
        String sebelum = isi.substring(0, pos).replaceAll("[ \\t]+$", "");
        boolean awalKalimat = sebelum.isEmpty() || sebelum.endsWith(".") || sebelum.endsWith("?") || sebelum.endsWith("!") || sebelum.endsWith("\n");
        int h = 0;
        while (h < teks.length() && teks.charAt(h) == '\n') h++; // "baris baru" di depan = awal kalimat juga
        if ((awalKalimat || h > 0) && h < teks.length() && Character.isLetter(teks.charAt(h))) {
            teks = teks.substring(0, h) + Character.toUpperCase(teks.charAt(h)) + teks.substring(h + 1);
        }
        boolean tandaBaca = teks.startsWith(".") || teks.startsWith(",") || teks.startsWith("?") || teks.startsWith("!") || teks.startsWith("\n");
        String sisip = (sebelum.isEmpty() || sebelum.endsWith("\n") || tandaBaca ? "" : " ") + teks;
        int hapusSpasi = isi.substring(0, pos).length() - sebelum.length();
        try {
            target.getDocument().remove(pos - hapusSpasi, hapusSpasi);
            target.getDocument().insertString(pos - hapusSpasi, sisip, null);
            target.setCaretPosition(pos - hapusSpasi + sisip.length());
        } catch (Exception e) {
            target.setText(isi + sisip);
        }
    }

    private static String halaman(String sesi) {
        return "<!doctype html><html><head><meta charset=utf-8><title>Dikte Suara SIMRS</title>"
            + "<style>body{margin:0;font-family:Tahoma,sans-serif;background:#f4f8f4;color:#223}"
            + "#bar{background:#1e7a3c;color:#fff;padding:10px 14px;font-size:14px}"
            + "#isi{padding:14px}#mic{width:96px;height:96px;border-radius:50%;border:0;font-size:40px;cursor:pointer;"
            + "background:#1e7a3c;color:#fff;display:block;margin:6px auto 10px}#mic.on{background:#c62828;animation:d 1.2s infinite}"
            + "@keyframes d{50%{box-shadow:0 0 0 14px rgba(198,40,40,.15)}}"
            + "#st{text-align:center;font-size:13px;color:#555;min-height:18px}#sementara{color:#888;font-style:italic;min-height:20px;margin:8px 0}"
            + "#log{background:#fff;border:1px solid #cdd;border-radius:6px;padding:8px;height:120px;overflow:auto;font-size:13px}"
            + "small{color:#777;display:block;margin-top:8px;line-height:1.4}</style></head><body>"
            + "<div id=bar>Dikte Suara &middot; Bahasa Indonesia</div><div id=isi>"
            + "<button id=mic title='Mulai / berhenti'>&#127908;</button><div id=st>Klik mic lalu bicara</div>"
            + "<div id=sementara></div><div id=log></div>"
            + "<small>Ucapkan <b>titik</b>, <b>koma</b>, <b>tanda tanya</b>, atau <b>baris baru</b> untuk tanda baca. "
            + "Teks langsung masuk ke kotak yang dipilih di SIMRS. Tutup jendela ini kalau sudah selesai.</small></div>"
            + "<script>(function(){var S='" + sesi + "',R=window.SpeechRecognition||window.webkitSpeechRecognition,"
            + "mic=document.getElementById('mic'),st=document.getElementById('st'),tmp=document.getElementById('sementara'),log=document.getElementById('log'),on=false,r;"
            + "if(!R){st.textContent='Browser ini tidak mendukung dikte. Gunakan Google Chrome.';mic.disabled=true;return;}"
            + "function rapi(t){return t.replace(/\\s*\\btanda tanya\\b/gi,'?').replace(/\\s*\\btanda seru\\b/gi,'!').replace(/\\s*\\btitik dua\\b/gi,':')"
            + ".replace(/\\s*\\btitik\\b/gi,'.').replace(/\\s*\\bkoma\\b/gi,',').replace(/[ \\t]*\\bbaris baru\\b[ \\t]*/gi,'\\n').replace(/^[ \\t]+|[ \\t]+$/g,'');}"
            + "function kirim(t){fetch('/dikte/teks?s='+S,{method:'POST',body:t}).then(function(x){if(!x.ok)st.textContent='SIMRS tidak menerima teks (sesi habis?)';})"
            + ".catch(function(){st.textContent='SIMRS tidak terhubung';});var d=document.createElement('div');d.textContent=t;log.appendChild(d);log.scrollTop=log.scrollHeight;}"
            + "function buat(){r=new R();r.lang='id-ID';r.continuous=true;r.interimResults=true;"
            + "r.onresult=function(e){var s='';for(var i=e.resultIndex;i<e.results.length;i++){var t=e.results[i][0].transcript;"
            + "if(e.results[i].isFinal){t=rapi(t);if(t)kirim(t);}else s+=t;}tmp.textContent=s;};"
            + "r.onerror=function(e){if(e.error=='not-allowed'){st.textContent='Izin mikrofon ditolak. Izinkan mikrofon untuk localhost.';on=false;mic.className='';}"
            + "else if(e.error=='network'){st.textContent='Tidak ada internet, dikte butuh koneksi.';}};"
            + "r.onend=function(){if(on){try{r.start();}catch(x){}}else{mic.className='';st.textContent='Berhenti. Klik mic untuk lanjut';tmp.textContent='';}};}"
            + "mic.onclick=function(){if(on){on=false;r.stop();return;}buat();on=true;mic.className='on';st.textContent='Mendengarkan...';r.start();};"
            + "mic.click();})();</script></body></html>";
    }

    private static String baca(InputStream in) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        for (int n; (n = in.read(buf)) > 0;) b.write(buf, 0, n);
        return new String(b.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void kirim(HttpExchange ex, int kode, String tipe, String isi) throws Exception {
        byte[] b = isi.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", tipe);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(kode, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
}
