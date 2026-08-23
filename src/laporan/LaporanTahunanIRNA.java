package laporan;

import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * Laporan Tahunan Instalasi Rawat Inap.
 * Dibangun mengikuti pola {@link LaporanTahunanIRJ} — 8 tab
 * (bangsal / kelas / DPJP / lama-baru / L-P / jenis pasien /
 * jenis pasien x bangsal / status pulang) dengan breakdown
 * per bulan + triwulan + total tahunan.
 *
 * Sumber data: {@code kamar_inap} (satu pasien = satu episode
 * kamar) di-join ke {@code reg_periksa}, {@code kamar},
 * {@code bangsal}, {@code dokter}, {@code penjab}. Filter dasar:
 * {@code kamar_inap.tgl_masuk} berada di bulan yang dihitung.
 */
public class LaporanTahunanIRNA extends javax.swing.JDialog {
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private StringBuilder htmlContent;
    private int i = 0;

    private static final String[] KODE_BULAN = {
        "01","02","03","04","05","06","07","08","09","10","11","12"};
    private static final String[] NAMA_BULAN = {
        "Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agus","Sep","Okt","Nov","Des"};

    public LaporanTahunanIRNA(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        HTMLEditorKit kit = new HTMLEditorKit();
        widget.editorpane[] panes = {LoadHTML,LoadHTML1,LoadHTML2,LoadHTML3,
                                      LoadHTML4,LoadHTML5,LoadHTML6,LoadHTML7};
        for (widget.editorpane p : panes) {
            p.setEditable(true);
            p.setEditorKit(kit);
        }
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
            ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
            ".isi2 td{font: 8.5px tahoma;height:12px;background: #ffffff;color:#323232;}"+
            ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
            ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
            ".isi5 td{border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
        );
        Document doc = kit.createDefaultDocument();
        for (widget.editorpane p : panes) {
            p.setDocument(doc);
        }
        Valid.LoadTahun(ThnCari);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        panelisi1 = new widget.panelisi();
        label11 = new widget.Label();
        ThnCari = new widget.ComboBox();
        btnCari = new widget.Button();
        label9 = new widget.Label();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        Scroll = new widget.ScrollPane();  LoadHTML  = new widget.editorpane();
        Scroll1 = new widget.ScrollPane(); LoadHTML1 = new widget.editorpane();
        Scroll2 = new widget.ScrollPane(); LoadHTML2 = new widget.editorpane();
        Scroll3 = new widget.ScrollPane(); LoadHTML3 = new widget.editorpane();
        Scroll4 = new widget.ScrollPane(); LoadHTML4 = new widget.editorpane();
        Scroll5 = new widget.ScrollPane(); LoadHTML5 = new widget.editorpane();
        Scroll6 = new widget.ScrollPane(); LoadHTML6 = new widget.editorpane();
        Scroll7 = new widget.ScrollPane(); LoadHTML7 = new widget.editorpane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240,245,235)),
            "::[ Laporan Tahunan Instalasi Rawat Inap ]::",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new java.awt.Font("Tahoma",0,11), new java.awt.Color(50,50,50)));
        internalFrame1.setName("internalFrame1");
        internalFrame1.setLayout(new java.awt.BorderLayout(1,1));

        panelisi1.setName("panelisi1");
        panelisi1.setPreferredSize(new java.awt.Dimension(100,56));
        panelisi1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,9));

        label11.setText("Tahun Pelayanan :");
        label11.setPreferredSize(new java.awt.Dimension(105,23));
        panelisi1.add(label11);

        ThnCari.setPreferredSize(new java.awt.Dimension(80,23));
        panelisi1.add(ThnCari);

        btnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        btnCari.setMnemonic('2');
        btnCari.setToolTipText("Alt+2");
        btnCari.setPreferredSize(new java.awt.Dimension(28,23));
        btnCari.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) { btnCariActionPerformed(); }
        });
        btnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent evt) { btnCariKeyPressed(evt); }
        });
        panelisi1.add(btnCari);

        label9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label9.setPreferredSize(new java.awt.Dimension(100,30));
        panelisi1.add(label9);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setPreferredSize(new java.awt.Dimension(100,30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) { BtnPrintActionPerformed(); }
        });
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent evt) { BtnPrintKeyPressed(evt); }
        });
        panelisi1.add(BtnPrint);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100,30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) { dispose(); }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode()==KeyEvent.VK_SPACE) dispose();
            }
        });
        panelisi1.add(BtnKeluar);

        internalFrame1.add(panelisi1, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(255,255,253));
        TabRawat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241,246,236)));
        TabRawat.setForeground(new java.awt.Color(50,50,50));
        TabRawat.setFont(new java.awt.Font("Tahoma",0,11));
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent evt) { btnCariActionPerformed(); }
        });

        String[] judulTab = {"Pasien Per Bangsal","Pasien Per Kelas","Pasien Per DPJP",
                              "Pasien Lama & Baru","Pasien Laki-laki & Perempuan",
                              "Jenis Pasien","Jenis Pasien Per Bangsal","Status Pulang"};
        widget.ScrollPane[] scrolls = {Scroll,Scroll1,Scroll2,Scroll3,Scroll4,Scroll5,Scroll6,Scroll7};
        widget.editorpane[] panes = {LoadHTML,LoadHTML1,LoadHTML2,LoadHTML3,LoadHTML4,LoadHTML5,LoadHTML6,LoadHTML7};
        for (int t=0; t<scrolls.length; t++) {
            scrolls[t].setBorder(null);
            scrolls[t].setOpaque(true);
            panes[t].setBorder(null);
            scrolls[t].setViewportView(panes[t]);
            TabRawat.addTab(judulTab[t], scrolls[t]);
        }

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        pack();
    }

    // ================================================================
    // HELPER
    // ================================================================

    /** Hitung 12 bulan sekaligus dari SQL bertemplate {BULAN}. */
    private int[] hitungBulanan(String sqlTemplate) {
        int[] hasil = new int[12];
        String thn = ThnCari.getSelectedItem().toString();
        for (int b = 0; b < 12; b++) {
            String sql = sqlTemplate.replace("{BULAN}", thn + "-" + KODE_BULAN[b])
                                     .replace("{TAHUN}", thn);
            hasil[b] = Sequel.cariInteger(sql);
        }
        return hasil;
    }

    /** Header tabel (2 baris): No, Label kolom kiri, 12 bulan + 4 TW + Total. */
    private String htmlHeader(String labelKiri) {
        String thn = ThnCari.getSelectedItem().toString();
        StringBuilder h = new StringBuilder();
        h.append("<tr class='isi'>")
         .append("<td valign='middle' bgcolor='#FFFAFA' align='center' width='2%' rowspan='2'>No</td>")
         .append("<td valign='middle' bgcolor='#FFFAFA' align='center' width='20%' rowspan='2'>").append(labelKiri).append("</td>")
         .append("<td valign='middle' bgcolor='#FFFAFA' align='center' width='78%' colspan='17'>Jumlah Pasien</td>")
         .append("</tr>")
         .append("<tr class='isi'>");
        String[] label = {NAMA_BULAN[0],NAMA_BULAN[1],NAMA_BULAN[2],"TW I",
                          NAMA_BULAN[3],NAMA_BULAN[4],NAMA_BULAN[5],"TW II",
                          NAMA_BULAN[6],NAMA_BULAN[7],NAMA_BULAN[8],"TW III",
                          NAMA_BULAN[9],NAMA_BULAN[10],NAMA_BULAN[11],"TW IV","Total"};
        for (String l : label) {
            h.append("<td valign='middle' bgcolor='#FFFAFA' align='center'>").append(l).append(" ").append(thn).append("</td>");
        }
        h.append("</tr>");
        return h.toString();
    }

    /** Baris data 12 bulan + TW + total. */
    private String htmlRow(String no, String label, int[] bulan, boolean sorotAtas) {
        String bg = sorotAtas ? " bgcolor='#FFFFF8'" : "";
        StringBuilder r = new StringBuilder();
        r.append("<tr class='isi'>")
         .append("<td valign='middle'").append(bg).append(" align='center'>").append(no).append("</td>")
         .append("<td valign='middle'").append(bg).append(" align='left'>").append(label).append("</td>");
        int total = 0;
        for (int i = 0; i < 12; i++) {
            r.append("<td valign='middle'").append(bg).append(" align='center'>").append(bulan[i]).append("</td>");
            total += bulan[i];
            if (i == 2 || i == 5 || i == 8 || i == 11) {
                int tw = bulan[i-2] + bulan[i-1] + bulan[i];
                r.append("<td valign='middle'").append(bg).append(" align='center'>").append(tw).append("</td>");
            }
        }
        r.append("<td valign='middle'").append(bg).append(" align='center'>").append(total).append("</td>")
         .append("</tr>");
        return r.toString();
    }

    /** Baris JUMLAH terakhir. */
    private String htmlRowTotal(int[][] semuaBaris) {
        int[] jumlah = new int[12];
        for (int[] b : semuaBaris) {
            for (int m = 0; m < 12; m++) jumlah[m] += b[m];
        }
        StringBuilder r = new StringBuilder();
        r.append("<tr class='isi'>")
         .append("<td valign='middle' bgcolor='#FFFFF8' align='right' colspan='2'>JUMLAH : </td>");
        int total = 0;
        for (int i = 0; i < 12; i++) {
            r.append("<td valign='middle' bgcolor='#FFFFF8' align='center'>").append(jumlah[i]).append("</td>");
            total += jumlah[i];
            if (i == 2 || i == 5 || i == 8 || i == 11) {
                int tw = jumlah[i-2] + jumlah[i-1] + jumlah[i];
                r.append("<td valign='middle' bgcolor='#FFFFF8' align='center'>").append(tw).append("</td>");
            }
        }
        r.append("<td valign='middle' bgcolor='#FFFFF8' align='center'>").append(total).append("</td>")
         .append("</tr>");
        return r.toString();
    }

    /** Render html final ke pane tertentu. */
    private void tampilkanKe(widget.editorpane pane, String isi) {
        pane.setText(
            "<html>" +
              "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>" +
              isi +
              "</table>" +
            "</html>");
    }

    /**
     * SQL dasar: kunjungan rawat inap unik per bulan.
     * Satu pasien yang pindah kamar dalam bulan yang sama tetap
     * dihitung satu (distinct no_rawat).
     */
    private String sqlBulananRanap(String filterExtra) {
        return "select count(distinct kamar_inap.no_rawat) " +
               "from kamar_inap " +
               "inner join reg_periksa on reg_periksa.no_rawat=kamar_inap.no_rawat " +
               "inner join pasien     on pasien.no_rkm_medis=reg_periksa.no_rkm_medis " +
               "inner join kamar      on kamar.kd_kamar=kamar_inap.kd_kamar " +
               "inner join bangsal    on bangsal.kd_bangsal=kamar.kd_bangsal " +
               "left join dokter      on dokter.kd_dokter=reg_periksa.kd_dokter " +
               "left join penjab      on penjab.kd_pj=reg_periksa.kd_pj " +
               "where DATE_FORMAT(kamar_inap.tgl_masuk, '%Y-%m')='{BULAN}' " + filterExtra;
    }

    // ================================================================
    // TAB 1 - Pasien Per Bangsal
    // ================================================================
    private void prosesCari1() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            htmlContent = new StringBuilder(htmlHeader("Bangsal"));
            java.util.List<int[]> semua = new java.util.ArrayList<>();
            i = 1;
            ps = koneksi.prepareStatement(
                "select distinct bangsal.kd_bangsal, bangsal.nm_bangsal " +
                "from kamar_inap " +
                "inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar " +
                "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal " +
                "where year(kamar_inap.tgl_masuk)=? " +
                "order by bangsal.nm_bangsal");
            try {
                ps.setString(1, ThnCari.getSelectedItem().toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    int[] bulan = hitungBulanan(sqlBulananRanap(
                        "and bangsal.kd_bangsal='" + esc(rs.getString("kd_bangsal")) + "'"));
                    htmlContent.append(htmlRow(String.valueOf(i), esc(rs.getString("nm_bangsal")), bulan, true));
                    semua.add(bulan);
                    i++;
                }
            } finally { closeRs(); }
            if (!semua.isEmpty()) htmlContent.append(htmlRowTotal(semua.toArray(new int[0][])));
            tampilkanKe(LoadHTML, htmlContent.toString());
        } catch (Exception e) {
            System.out.println("Notif LaporanTahunanIRNA.prosesCari1 : " + e);
        }
        setCursor(Cursor.getDefaultCursor());
    }

    // ================================================================
    // TAB 2 - Pasien Per Kelas
    // ================================================================
    private void prosesCari2() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            htmlContent = new StringBuilder(htmlHeader("Kelas"));
            java.util.List<int[]> semua = new java.util.ArrayList<>();
            String[] kelas = {"Kelas 1","Kelas 2","Kelas 3","Kelas Utama","Kelas VIP","Kelas VVIP"};
            for (int k = 0; k < kelas.length; k++) {
                int[] bulan = hitungBulanan(sqlBulananRanap(
                    "and kamar.kelas='" + kelas[k] + "'"));
                htmlContent.append(htmlRow(String.valueOf(k+1), kelas[k], bulan, true));
                semua.add(bulan);
            }
            htmlContent.append(htmlRowTotal(semua.toArray(new int[0][])));
            tampilkanKe(LoadHTML1, htmlContent.toString());
        } catch (Exception e) {
            System.out.println("Notif LaporanTahunanIRNA.prosesCari2 : " + e);
        }
        setCursor(Cursor.getDefaultCursor());
    }

    // ================================================================
    // TAB 3 - Pasien Per DPJP (dokter penanggung jawab reg_periksa)
    // ================================================================
    private void prosesCari3() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            htmlContent = new StringBuilder(htmlHeader("Dokter Penanggung Jawab"));
            java.util.List<int[]> semua = new java.util.ArrayList<>();
            i = 1;
            ps = koneksi.prepareStatement(
                "select distinct reg_periksa.kd_dokter, dokter.nm_dokter " +
                "from kamar_inap " +
                "inner join reg_periksa on reg_periksa.no_rawat=kamar_inap.no_rawat " +
                "inner join dokter on dokter.kd_dokter=reg_periksa.kd_dokter " +
                "where year(kamar_inap.tgl_masuk)=? " +
                "order by dokter.nm_dokter");
            try {
                ps.setString(1, ThnCari.getSelectedItem().toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    int[] bulan = hitungBulanan(sqlBulananRanap(
                        "and reg_periksa.kd_dokter='" + esc(rs.getString("kd_dokter")) + "'"));
                    htmlContent.append(htmlRow(String.valueOf(i), esc(rs.getString("nm_dokter")), bulan, true));
                    semua.add(bulan);
                    i++;
                }
            } finally { closeRs(); }
            if (!semua.isEmpty()) htmlContent.append(htmlRowTotal(semua.toArray(new int[0][])));
            tampilkanKe(LoadHTML2, htmlContent.toString());
        } catch (Exception e) {
            System.out.println("Notif LaporanTahunanIRNA.prosesCari3 : " + e);
        }
        setCursor(Cursor.getDefaultCursor());
    }

    // ================================================================
    // TAB 4 - Pasien Lama & Baru per Bangsal
    // ================================================================
    private void prosesCari4() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            htmlContent = new StringBuilder(htmlHeader("Bangsal / Kategori"));
            java.util.List<int[]> semua = new java.util.ArrayList<>();
            i = 1;
            ps = koneksi.prepareStatement(
                "select distinct bangsal.kd_bangsal, bangsal.nm_bangsal " +
                "from kamar_inap " +
                "inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar " +
                "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal " +
                "where year(kamar_inap.tgl_masuk)=? " +
                "order by bangsal.nm_bangsal");
            try {
                ps.setString(1, ThnCari.getSelectedItem().toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    String kd = esc(rs.getString("kd_bangsal"));
                    int[] totalBangsal = hitungBulanan(sqlBulananRanap(
                        "and bangsal.kd_bangsal='" + kd + "'"));
                    htmlContent.append(htmlRow(String.valueOf(i), esc(rs.getString("nm_bangsal")),
                        totalBangsal, true));
                    int[] lama = hitungBulanan(sqlBulananRanap(
                        "and bangsal.kd_bangsal='" + kd + "' and reg_periksa.stts_daftar='Lama'"));
                    htmlContent.append(htmlRow("", "Lama", lama, false));
                    int[] baru = hitungBulanan(sqlBulananRanap(
                        "and bangsal.kd_bangsal='" + kd + "' and reg_periksa.stts_daftar='Baru'"));
                    htmlContent.append(htmlRow("", "Baru", baru, false));
                    semua.add(totalBangsal);
                    i++;
                }
            } finally { closeRs(); }
            if (!semua.isEmpty()) htmlContent.append(htmlRowTotal(semua.toArray(new int[0][])));
            tampilkanKe(LoadHTML3, htmlContent.toString());
        } catch (Exception e) {
            System.out.println("Notif LaporanTahunanIRNA.prosesCari4 : " + e);
        }
        setCursor(Cursor.getDefaultCursor());
    }

    // ================================================================
    // TAB 5 - Pasien L & P per Bangsal
    // ================================================================
    private void prosesCari5() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            htmlContent = new StringBuilder(htmlHeader("Bangsal / Jenis Kelamin"));
            java.util.List<int[]> semua = new java.util.ArrayList<>();
            i = 1;
            ps = koneksi.prepareStatement(
                "select distinct bangsal.kd_bangsal, bangsal.nm_bangsal " +
                "from kamar_inap " +
                "inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar " +
                "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal " +
                "where year(kamar_inap.tgl_masuk)=? " +
                "order by bangsal.nm_bangsal");
            try {
                ps.setString(1, ThnCari.getSelectedItem().toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    String kd = esc(rs.getString("kd_bangsal"));
                    int[] ttl = hitungBulanan(sqlBulananRanap(
                        "and bangsal.kd_bangsal='" + kd + "'"));
                    htmlContent.append(htmlRow(String.valueOf(i), esc(rs.getString("nm_bangsal")), ttl, true));
                    int[] laki = hitungBulanan(sqlBulananRanap(
                        "and bangsal.kd_bangsal='" + kd + "' and pasien.jk='L'"));
                    htmlContent.append(htmlRow("", "Laki-laki", laki, false));
                    int[] perp = hitungBulanan(sqlBulananRanap(
                        "and bangsal.kd_bangsal='" + kd + "' and pasien.jk='P'"));
                    htmlContent.append(htmlRow("", "Perempuan", perp, false));
                    semua.add(ttl);
                    i++;
                }
            } finally { closeRs(); }
            if (!semua.isEmpty()) htmlContent.append(htmlRowTotal(semua.toArray(new int[0][])));
            tampilkanKe(LoadHTML4, htmlContent.toString());
        } catch (Exception e) {
            System.out.println("Notif LaporanTahunanIRNA.prosesCari5 : " + e);
        }
        setCursor(Cursor.getDefaultCursor());
    }

    // ================================================================
    // TAB 6 - Jenis Pasien (per penjab)
    // ================================================================
    private void prosesCari6() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            htmlContent = new StringBuilder(htmlHeader("Jenis Pembayaran"));
            java.util.List<int[]> semua = new java.util.ArrayList<>();
            i = 1;
            ps = koneksi.prepareStatement(
                "select distinct penjab.kd_pj, penjab.png_jawab " +
                "from kamar_inap " +
                "inner join reg_periksa on reg_periksa.no_rawat=kamar_inap.no_rawat " +
                "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj " +
                "where year(kamar_inap.tgl_masuk)=? " +
                "order by penjab.png_jawab");
            try {
                ps.setString(1, ThnCari.getSelectedItem().toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    int[] bulan = hitungBulanan(sqlBulananRanap(
                        "and reg_periksa.kd_pj='" + esc(rs.getString("kd_pj")) + "'"));
                    htmlContent.append(htmlRow(String.valueOf(i), esc(rs.getString("png_jawab")), bulan, true));
                    semua.add(bulan);
                    i++;
                }
            } finally { closeRs(); }
            if (!semua.isEmpty()) htmlContent.append(htmlRowTotal(semua.toArray(new int[0][])));
            tampilkanKe(LoadHTML5, htmlContent.toString());
        } catch (Exception e) {
            System.out.println("Notif LaporanTahunanIRNA.prosesCari6 : " + e);
        }
        setCursor(Cursor.getDefaultCursor());
    }

    // ================================================================
    // TAB 7 - Jenis Pasien Per Bangsal
    // ================================================================
    private void prosesCari7() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            htmlContent = new StringBuilder(htmlHeader("Bangsal / Jenis Pembayaran"));
            java.util.List<int[]> semua = new java.util.ArrayList<>();
            i = 1;
            ps = koneksi.prepareStatement(
                "select distinct bangsal.kd_bangsal, bangsal.nm_bangsal " +
                "from kamar_inap " +
                "inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar " +
                "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal " +
                "where year(kamar_inap.tgl_masuk)=? " +
                "order by bangsal.nm_bangsal");
            try {
                ps.setString(1, ThnCari.getSelectedItem().toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    String kd = esc(rs.getString("kd_bangsal"));
                    int[] ttl = hitungBulanan(sqlBulananRanap(
                        "and bangsal.kd_bangsal='" + kd + "'"));
                    htmlContent.append(htmlRow(String.valueOf(i), esc(rs.getString("nm_bangsal")), ttl, true));
                    // Ambil penjab yang dipakai di bangsal ini
                    try (PreparedStatement psp = koneksi.prepareStatement(
                            "select distinct penjab.kd_pj, penjab.png_jawab " +
                            "from kamar_inap " +
                            "inner join reg_periksa on reg_periksa.no_rawat=kamar_inap.no_rawat " +
                            "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj " +
                            "inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar " +
                            "where year(kamar_inap.tgl_masuk)=? and kamar.kd_bangsal=? " +
                            "order by penjab.png_jawab")) {
                        psp.setString(1, ThnCari.getSelectedItem().toString());
                        psp.setString(2, rs.getString("kd_bangsal"));
                        try (ResultSet rsp = psp.executeQuery()) {
                            while (rsp.next()) {
                                int[] bp = hitungBulanan(sqlBulananRanap(
                                    "and bangsal.kd_bangsal='" + kd +
                                    "' and reg_periksa.kd_pj='" + esc(rsp.getString("kd_pj")) + "'"));
                                htmlContent.append(htmlRow("", esc(rsp.getString("png_jawab")), bp, false));
                            }
                        }
                    }
                    semua.add(ttl);
                    i++;
                }
            } finally { closeRs(); }
            if (!semua.isEmpty()) htmlContent.append(htmlRowTotal(semua.toArray(new int[0][])));
            tampilkanKe(LoadHTML6, htmlContent.toString());
        } catch (Exception e) {
            System.out.println("Notif LaporanTahunanIRNA.prosesCari7 : " + e);
        }
        setCursor(Cursor.getDefaultCursor());
    }

    // ================================================================
    // TAB 8 - Status Pulang (kamar_inap.stts_pulang)
    // ================================================================
    private void prosesCari8() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            htmlContent = new StringBuilder(htmlHeader("Status Pulang"));
            String[] status = {"Sehat","Sembuh","Membaik","Rujuk","APS",
                                "Atas Persetujuan Dokter","Atas Permintaan Sendiri",
                                "Pulang Paksa","Meninggal","Pindah Kamar",
                                "Status Belum Lengkap","Isoman","Lain-lain","+","-"};
            java.util.List<int[]> semua = new java.util.ArrayList<>();
            i = 1;
            for (String s : status) {
                int[] bulan = hitungBulanan(sqlBulananRanap(
                    "and kamar_inap.stts_pulang='" + s + "'"));
                int total = 0;
                for (int m : bulan) total += m;
                if (total == 0) continue;                     // skip status yang kosong
                htmlContent.append(htmlRow(String.valueOf(i), s, bulan, true));
                semua.add(bulan);
                i++;
            }
            if (!semua.isEmpty()) htmlContent.append(htmlRowTotal(semua.toArray(new int[0][])));
            tampilkanKe(LoadHTML7, htmlContent.toString());
        } catch (Exception e) {
            System.out.println("Notif LaporanTahunanIRNA.prosesCari8 : " + e);
        }
        setCursor(Cursor.getDefaultCursor());
    }

    // ================================================================
    // ACTION HANDLERS
    // ================================================================
    private void btnCariActionPerformed() {
        switch (TabRawat.getSelectedIndex()) {
            case 0: prosesCari1(); break;
            case 1: prosesCari2(); break;
            case 2: prosesCari3(); break;
            case 3: prosesCari4(); break;
            case 4: prosesCari5(); break;
            case 5: prosesCari6(); break;
            case 6: prosesCari7(); break;
            case 7: prosesCari8(); break;
        }
    }

    private void btnCariKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) btnCariActionPerformed();
        else Valid.pindah(evt, ThnCari, BtnPrint);
    }

    private void BtnPrintActionPerformed() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            File g = new File("file2.css");
            try (BufferedWriter bg = new BufferedWriter(new FileWriter(g))) {
                bg.write(
                    ".isi td{border-right: 1px solid #e2e7dd;font: 11px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                    ".isi2 td{font: 11px tahoma;height:12px;background: #ffffff;color:#323232;}"+
                    ".isi3 td{border-right: 1px solid #e2e7dd;font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                    ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                    ".isi5 td{font: 11px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                );
            }

            widget.editorpane[] panes = {LoadHTML,LoadHTML1,LoadHTML2,LoadHTML3,LoadHTML4,LoadHTML5,LoadHTML6,LoadHTML7};
            int idx = TabRawat.getSelectedIndex();
            if (idx < 0 || idx >= panes.length) return;

            String kop =
                "<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />" +
                "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>" +
                    "<tr class='isi5'>" +
                        "<td valign='top' align='center'>" +
                            "<font size='4' face='Tahoma'>" + akses.getnamars() + "</font><br>" +
                            akses.getalamatrs() + ", " + akses.getkabupatenrs() + ", " + akses.getpropinsirs() + "<br>" +
                            akses.getkontakrs() + ", E-mail : " + akses.getemailrs() + "<br><br>" +
                            "<font size='2' face='Tahoma'>LAPORAN TAHUNAN INSTALASI RAWAT INAP PERIODE " +
                            ThnCari.getSelectedItem() + "<br><br></font>" +
                        "</td>" +
                    "</tr>" +
                "</table>";

            File f = new File("LaporanTahunanIRNA.html");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                bw.write(panes[idx].getText().replaceAll("<head>", kop));
            }
            Desktop.getDesktop().browse(f.toURI());
        } catch (Exception e) {
            System.out.println("Notif LaporanTahunanIRNA.BtnPrint : " + e);
        }
        setCursor(Cursor.getDefaultCursor());
    }

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) BtnPrintActionPerformed();
        else Valid.pindah(evt, btnCari, BtnKeluar);
    }

    public void isCek() {
        BtnPrint.setEnabled(akses.getlaporan_tahunan_irj());
    }

    // ================================================================
    // UTILITAS
    // ================================================================
    private void closeRs() {
        try { if (rs != null) rs.close(); } catch (Exception ignore) {}
        try { if (ps != null) ps.close(); } catch (Exception ignore) {}
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("'", "''");
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            LaporanTahunanIRNA dialog = new LaporanTahunanIRNA(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
            });
            dialog.setVisible(true);
        });
    }

    // Variables
    private widget.Button BtnKeluar, BtnPrint, btnCari;
    private widget.editorpane LoadHTML, LoadHTML1, LoadHTML2, LoadHTML3, LoadHTML4, LoadHTML5, LoadHTML6, LoadHTML7;
    private widget.ScrollPane Scroll, Scroll1, Scroll2, Scroll3, Scroll4, Scroll5, Scroll6, Scroll7;
    private javax.swing.JTabbedPane TabRawat;
    private widget.ComboBox ThnCari;
    private widget.InternalFrame internalFrame1;
    private widget.Label label11, label9;
    private widget.panelisi panelisi1;
}
