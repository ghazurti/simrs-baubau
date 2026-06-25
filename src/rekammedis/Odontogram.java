package rekammedis;

import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.validasi;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import kepegawaian.DlgCariDokter;

public final class Odontogram extends javax.swing.JDialog {

    private Connection koneksi;
    private final validasi Valid = new validasi();
    private final fungsi.sekuel Sequel = new fungsi.sekuel();
    private PreparedStatement ps;
    private ResultSet rs;
    private DlgCariDokter dokterDlg;

    // FDI rows – permanent (perm) and deciduous (dec)
    private static final String[] PERM_UPPER = {
        "18","17","16","15","14","13","12","11","21","22","23","24","25","26","27","28"};
    private static final String[] DEC_UPPER  = {
        "55","54","53","52","51","61","62","63","64","65"};
    private static final String[] DEC_LOWER  = {
        "85","84","83","82","81","71","72","73","74","75"};
    private static final String[] PERM_LOWER = {
        "48","47","46","45","44","43","42","41","31","32","33","34","35","36","37","38"};

    private static final String[] HASIL_LIST = {
        "","Erupsi","Karies","Missing","Filled","Mahkota","Implant",
        "Fraktur","Abrasi","Erosi","Hipoplasi","Normal","Lainnya"};
    private static final String[] RAHANG_LIST = {"-","Rahang Atas","Rahang Bawah"};

    private final java.util.Set<String> selectedGigi = new java.util.LinkedHashSet<>();
    private final Map<String, JCheckBox> cbMap = new HashMap<>();

    private widget.TextBox TNoRw, TPasien, TNoRM, KdDokter, NmDokter, NoPermintaan;
    private widget.TextBox TDiagnosa, TKdICD;
    private widget.Tanggal DTPTgl, DTPDari, DTPSampai;
    private widget.ComboBox CBHasil, CBRahang;
    private JTextArea TACatatan;
    private widget.Button BtnDokter, BtnSimpan, BtnBaru, BtnHapus, BtnGanti, BtnCetak, BtnKeluar;
    private JLabel lblSelectedGigi;
    private widget.TextBox TKeyWord;
    private widget.Table tbData;
    private DefaultTableModel tabMode;
    private ToothPanel toothPanel;
    private int editId = -1;

    public Odontogram(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        tampilData();
    }

    public void setNoRawat(String noRawat) {
        TNoRw.setText(noRawat);
        isRawat();
    }

    // =========================================================
    //  UI BUILD
    // =========================================================
    private void initComponents() {
        setTitle("::[ Pemeriksaan Odontogram ]::");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1100, 740);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(1, 1));

        Font fontTahoma = new Font("Tahoma", Font.PLAIN, 11);
        Color khanzaBg  = new Color(255, 255, 255);
        Color khanzaBorder = new Color(240, 245, 235);
        Color khanzaTitle  = new Color(50, 50, 50);

        // ---- ROOT INTERNAL FRAME (Khanza wrapper) ----
        widget.InternalFrame root = new widget.InternalFrame();
        root.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(khanzaBorder),
            "::[ Pemeriksaan Odontogram ]::",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Tahoma", Font.BOLD, 12), khanzaTitle));
        root.setLayout(new BorderLayout(1, 1));
        root.setBackground(khanzaBg);
        add(root, BorderLayout.CENTER);

        // ---- HEADER (Data Pasien & Dokter) ----
        widget.PanelBiasa pHeader = new widget.PanelBiasa();
        pHeader.setLayout(null);
        pHeader.setBackground(khanzaBg);
        pHeader.setPreferredSize(new Dimension(1100, 72));
        pHeader.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(khanzaBorder),
            "Data Pasien & Dokter Gigi",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            fontTahoma, khanzaTitle));

        lblK(pHeader, "No.Rawat :",   8, 22, 70, 23);
        TNoRw = new widget.TextBox();
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        TNoRw.setBounds(82, 22, 140, 23); pHeader.add(TNoRw);

        TNoRM = new widget.TextBox(); TNoRM.setEditable(false);
        TNoRM.setBounds(226, 22, 90, 23); pHeader.add(TNoRM);

        TPasien = new widget.TextBox(); TPasien.setEditable(false);
        TPasien.setBounds(320, 22, 200, 23); pHeader.add(TPasien);

        lblK(pHeader, "Tanggal :", 528, 22, 60, 23);
        DTPTgl = new widget.Tanggal();
        DTPTgl.setDisplayFormat("dd-MM-yyyy");
        DTPTgl.setBounds(590, 22, 130, 23); pHeader.add(DTPTgl);

        lblK(pHeader, "No.Permintaan :", 730, 22, 100, 23);
        NoPermintaan = new widget.TextBox(); NoPermintaan.setEditable(false);
        NoPermintaan.setBounds(834, 22, 160, 23); pHeader.add(NoPermintaan);

        lblK(pHeader, "Dokter Gigi :",  8, 46, 70, 23);
        KdDokter = new widget.TextBox(); KdDokter.setEditable(false);
        KdDokter.setBounds(82, 46, 100, 23); pHeader.add(KdDokter);
        BtnDokter = new widget.Button(); BtnDokter.setText("...");
        BtnDokter.setBounds(184, 46, 30, 23); pHeader.add(BtnDokter);
        NmDokter = new widget.TextBox(); NmDokter.setEditable(false);
        NmDokter.setBounds(218, 46, 302, 23); pHeader.add(NmDokter);

        root.add(pHeader, BorderLayout.NORTH);

        // ---- CENTER: tooth diagram (left) + diagnosis (right) ----
        widget.PanelBiasa pCenter = new widget.PanelBiasa();
        pCenter.setLayout(new BorderLayout(4, 0));
        pCenter.setBackground(khanzaBg);

        toothPanel = new ToothPanel();
        widget.PanelBiasa toothWrap = new widget.PanelBiasa();
        toothWrap.setLayout(new BorderLayout());
        toothWrap.setBackground(khanzaBg);
        toothWrap.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(khanzaBorder),
            "Diagram Gigi (FDI)",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            fontTahoma, khanzaTitle));
        toothWrap.add(toothPanel, BorderLayout.NORTH);
        pCenter.add(toothWrap, BorderLayout.CENTER);
        pCenter.add(buildRightPanel(), BorderLayout.EAST);

        root.add(pCenter, BorderLayout.CENTER);

        // ---- BOTTOM: table + search + buttons ----
        widget.PanelBiasa pBottom = new widget.PanelBiasa();
        pBottom.setLayout(new BorderLayout(1, 1));
        pBottom.setBackground(khanzaBg);
        pBottom.setPreferredSize(new Dimension(1100, 220));
        pBottom.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(khanzaBorder),
            "Riwayat Pemeriksaan Odontogram",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            fontTahoma, khanzaTitle));

        tabMode = new DefaultTableModel(null, new Object[]{
            "Tgl.Rawat","Status","No.Rawat","No.RM","Nama Pasien",
            "Kode Dokter","Dokter Gigi","Bagian Gigi","Diagnosa Gigi","ICD 10"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbData = new widget.Table();
        tbData.setModel(tabMode);
        tbData.setAutoCreateRowSorter(true);
        tbData.setFont(fontTahoma);
        tbData.setRowHeight(20);
        tbData.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 11));
        tbData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbData.setToolTipText("Klik untuk memilih data yang akan diedit/dihapus");
        int[] cw = {90,60,110,80,160,80,140,70,160,80};
        for (int i = 0; i < cw.length; i++) tbData.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);
        widget.ScrollPane spData = new widget.ScrollPane();
        spData.setViewportView(tbData);
        spData.setPreferredSize(new Dimension(1080, 130));
        pBottom.add(spData, BorderLayout.CENTER);

        // Search bar — Khanza style panelisi
        widget.panelisi pSearch = new widget.panelisi();
        pSearch.setPreferredSize(new Dimension(1080, 34));
        pSearch.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        widget.Label lDari = new widget.Label(); lDari.setText("Tgl.Rawat :"); lDari.setPreferredSize(new Dimension(70, 23));
        pSearch.add(lDari);
        DTPDari = new widget.Tanggal(); DTPDari.setDisplayFormat("dd-MM-yyyy");
        DTPDari.setPreferredSize(new Dimension(120, 23)); pSearch.add(DTPDari);
        widget.Label lSd = new widget.Label(); lSd.setText("s.d."); lSd.setPreferredSize(new Dimension(28, 23));
        pSearch.add(lSd);
        DTPSampai = new widget.Tanggal(); DTPSampai.setDisplayFormat("dd-MM-yyyy");
        DTPSampai.setPreferredSize(new Dimension(120, 23)); pSearch.add(DTPSampai);
        widget.Label lKw = new widget.Label(); lKw.setText("Key Word :"); lKw.setPreferredSize(new Dimension(68, 23));
        pSearch.add(lKw);
        TKeyWord = new widget.TextBox();
        TKeyWord.setPreferredSize(new Dimension(220, 23)); pSearch.add(TKeyWord);
        widget.Button BtnTerapkan = new widget.Button();
        BtnTerapkan.setText("Refresh");
        try { BtnTerapkan.setIcon(new ImageIcon(getClass().getResource("/picture/refresh.png"))); } catch (Exception ignored) {}
        BtnTerapkan.setPreferredSize(new Dimension(90, 23));
        pSearch.add(BtnTerapkan);
        widget.Button BtnCariKw = new widget.Button();
        BtnCariKw.setText("Cari");
        try { BtnCariKw.setIcon(new ImageIcon(getClass().getResource("/picture/find.png"))); } catch (Exception ignored) {}
        BtnCariKw.setPreferredSize(new Dimension(80, 23));
        pSearch.add(BtnCariKw);
        BtnTerapkan.addActionListener(e -> tampilData());
        BtnCariKw.addActionListener(e -> cariData());
        pBottom.add(pSearch, BorderLayout.NORTH);

        // Buttons — Khanza style panelisi
        widget.panelisi pBtn = new widget.panelisi();
        pBtn.setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        BtnSimpan = btn("Simpan", "/picture/save-16x16.png");
        BtnBaru   = btn("Baru",   "/picture/new.png");
        BtnHapus  = btn("Hapus",  "/picture/stop_f2.png");
        BtnGanti  = btn("Ganti",  "/picture/edit.png");
        BtnCetak  = btn("Cetak",  "/picture/print.png");
        BtnKeluar = btn("Keluar", "/picture/exit.png");
        pBtn.add(BtnSimpan); pBtn.add(BtnBaru); pBtn.add(BtnGanti);
        pBtn.add(BtnHapus);  pBtn.add(BtnCetak); pBtn.add(BtnKeluar);
        pBottom.add(pBtn, BorderLayout.SOUTH);

        root.add(pBottom, BorderLayout.SOUTH);

        // ---- EVENTS ----
        TNoRw.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) isRawat();
            }
        });
        BtnDokter.addActionListener(e -> pilihDokter());
        BtnSimpan.addActionListener(e -> simpan());
        BtnBaru.addActionListener(e -> baru());
        BtnHapus.addActionListener(e -> hapus());
        BtnGanti.addActionListener(e -> ganti());
        BtnCetak.addActionListener(e -> cetak());
        BtnKeluar.addActionListener(e -> dispose());
        tbData.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { pilihBaris(); }
        });

        KdDokter.setText(akses.getkode());
        NmDokter.setText(akses.getnamauser());
        generateNoPermintaan();
    }

    private JPanel buildRightPanel() {
        Color khanzaBg = new Color(255, 255, 255);
        Color khanzaBorder = new Color(240, 245, 235);
        Color khanzaTitle  = new Color(50, 50, 50);
        Font fontTahoma = new Font("Tahoma", Font.PLAIN, 11);

        widget.PanelBiasa p = new widget.PanelBiasa();
        p.setLayout(null);
        p.setBackground(khanzaBg);
        p.setPreferredSize(new Dimension(360, 280));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(khanzaBorder),
            "Detail Pemeriksaan",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            fontTahoma, khanzaTitle));

        // Highlighted "Gigi terpilih" banner
        widget.PanelBiasa banner = new widget.PanelBiasa();
        banner.setLayout(null);
        banner.setBackground(new Color(232, 244, 252));
        banner.setBorder(BorderFactory.createLineBorder(new Color(176, 213, 240)));
        banner.setBounds(8, 22, 340, 28);
        p.add(banner);

        lblSelectedGigi = new JLabel("Gigi terpilih: -");
        lblSelectedGigi.setForeground(new Color(13, 71, 161));
        lblSelectedGigi.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblSelectedGigi.setBounds(8, 4, 322, 20);
        banner.add(lblSelectedGigi);

        lblK(p, "Diagnosa Gigi :", 8, 60, 100, 23);
        TDiagnosa = new widget.TextBox();
        TDiagnosa.setBounds(112, 60, 232, 23); p.add(TDiagnosa);

        lblK(p, "Kode ICD :", 8, 88, 78, 23);
        TKdICD = new widget.TextBox();
        TKdICD.setBounds(88, 88, 100, 23); p.add(TKdICD);

        lblK(p, "Hasil Pemeriksaan :", 8, 118, 140, 23);
        CBHasil = new widget.ComboBox();
        for (String s : HASIL_LIST) CBHasil.addItem(s);
        CBHasil.setBounds(8, 140, 170, 23); p.add(CBHasil);

        lblK(p, "Rahang :", 188, 118, 70, 23);
        CBRahang = new widget.ComboBox();
        for (String s : RAHANG_LIST) CBRahang.addItem(s);
        CBRahang.setBounds(188, 140, 156, 23); p.add(CBRahang);

        lblK(p, "Catatan Pemeriksaan :", 8, 172, 160, 23);
        TACatatan = new JTextArea(3, 20);
        TACatatan.setLineWrap(true);
        TACatatan.setWrapStyleWord(true);
        TACatatan.setFont(fontTahoma);
        TACatatan.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        widget.ScrollPane sc = new widget.ScrollPane();
        sc.setViewportView(TACatatan);
        sc.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        sc.setBounds(8, 194, 336, 78);
        p.add(sc);

        return p;
    }

    // =========================================================
    //  TOOTH PANEL
    // =========================================================
    private class ToothPanel extends JPanel {
        private static final int PW = 28, PH = 28; // permanent tooth size
        private static final int DW = 22, DH = 22; // deciduous tooth size
        private static final int RBS = 16;          // radio button size (16×16 renders full circle on macOS)
        private static final int GAP = 2;
        private static final int LM = 15;

        // Layout per row (upper): [8px margin] [number baseline=17] [4px] [rb=21..37] [5px] [tooth]
        // Layout per row (lower): [tooth] [5px] [rb] [5px+ascent] [number baseline]
        private static final int Y_PERM_UP  = 42;  // perm-upper tooth top
        private static final int Y_DEC_UP   = 120; // dec-upper tooth top
        private static final int Y_DEC_LO   = 170; // dec-lower tooth top
        private static final int Y_PERM_LO  = 250; // perm-lower tooth top

        public ToothPanel() {
            setBackground(Color.WHITE);
            setLayout(null);
            setPreferredSize(new Dimension(730, 325));
            buildRadioButtons();
        }

        private void buildRadioButtons() {
            // Upper rows: rb between number (above) and tooth (below)
            // rb_top = tooth_top - RBS - 5
            for (int i = 0; i < PERM_UPPER.length; i++)
                addRb(PERM_UPPER[i], xPerm(i) + PW/2 - RBS/2, Y_PERM_UP - RBS - 5);
            for (int i = 0; i < DEC_UPPER.length; i++)
                addRb(DEC_UPPER[i], xDec(DEC_UPPER, i) + DW/2 - RBS/2, Y_DEC_UP - RBS - 5);
            // Lower rows: rb between tooth (above) and number (below)
            // rb_top = tooth_bottom + 5
            for (int i = 0; i < DEC_LOWER.length; i++)
                addRb(DEC_LOWER[i], xDec(DEC_LOWER, i) + DW/2 - RBS/2, Y_DEC_LO + DH + 5);
            for (int i = 0; i < PERM_LOWER.length; i++)
                addRb(PERM_LOWER[i], xPerm(i) + PW/2 - RBS/2, Y_PERM_LO + PH + 5);
        }

        private void addRb(String gigi, int rx, int ry) {
            JCheckBox cb = new JCheckBox();
            cb.setBounds(rx, ry, RBS, RBS);
            cb.setBackground(Color.WHITE);
            cb.setOpaque(true);
            cb.addActionListener(e -> {
                if (cb.isSelected()) selectedGigi.add(gigi);
                else                  selectedGigi.remove(gigi);
                updateSelectedLabel();
                repaint();
            });
            cbMap.put(gigi, cb);
            add(cb);
        }

        // X coordinate for permanent tooth at index i (10px gap in the middle)
        private int xPerm(int i) {
            return LM + i * (PW + GAP) + (i >= 8 ? 10 : 0);
        }

        // X coordinate for deciduous tooth (10 teeth, aligned under permanents 14-25)
        private int xDec(String[] row, int i) {
            int base = LM + 3 * (PW + GAP) + (PW - DW) / 2;
            int mid  = row.length / 2; // 5
            return base + i * (DW + GAP) + (i >= mid ? 10 : 0);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Quadrant labels (Khanza-style subtle text)
            g2.setFont(new Font("Tahoma", Font.BOLD, 10));
            g2.setColor(new Color(120, 120, 140));
            g2.drawString("Kanan Atas (1)", LM, 14);
            g2.drawString("Kiri Atas (2)",  getWidth() / 2 + 12, 14);
            g2.drawString("Kanan Bawah (4)", LM, getHeight() - 4);
            g2.drawString("Kiri Bawah (3)",  getWidth() / 2 + 12, getHeight() - 4);

            drawRow(g2, PERM_UPPER, Y_PERM_UP, PW, PH, true);
            drawRow(g2, DEC_UPPER,  Y_DEC_UP,  DW, DH, true);

            // Dashed horizontal midline
            g2.setColor(new Color(200, 210, 220));
            float[] dash = {5f};
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dash, 0));
            int midY = (Y_DEC_UP + DH + Y_DEC_LO) / 2;
            g2.drawLine(LM, midY, getWidth() - 10, midY);

            // Dashed vertical midline (kiri/kanan)
            int midX = getWidth() / 2 + 5;
            g2.drawLine(midX, 18, midX, getHeight() - 18);
            g2.setStroke(new BasicStroke(1f));

            drawRow(g2, DEC_LOWER,  Y_DEC_LO,  DW, DH, false);
            drawRow(g2, PERM_LOWER, Y_PERM_LO, PW, PH, false);
        }

        private void drawRow(Graphics2D g2, String[] teeth, int ty, int tw, int th, boolean numAbove) {
            Font f = new Font("SansSerif", Font.BOLD, 9);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < teeth.length; i++) {
                int x = (tw == PW) ? xPerm(i) : xDec(teeth, i);
                drawTooth(g2, x, ty, tw, th, selectedGigi.contains(teeth[i]));
                g2.setColor(new Color(40, 40, 120));
                int nx = x + (tw - fm.stringWidth(teeth[i])) / 2;
                // upper: baseline above rb (rb_top = ty - RBS - 5 → baseline = ty - RBS - 9)
                // lower: baseline below rb (rb_bottom = ty + th + 5 + RBS → baseline = rb_bottom + 10)
                int ny = numAbove ? (ty - RBS - 9) : (ty + th + 5 + RBS + 14);
                g2.drawString(teeth[i], nx, ny);
            }
        }

        private void drawTooth(Graphics2D g2, int x, int y, int tw, int th, boolean selected) {
            // Fill (Khanza accent blue when selected)
            g2.setColor(selected ? new Color(187, 222, 251) : new Color(252, 252, 252));
            g2.fillRect(x, y, tw, th);
            // Outer border
            g2.setColor(selected ? new Color(13, 71, 161) : new Color(90, 100, 120));
            g2.setStroke(new BasicStroke(selected ? 1.6f : 1f));
            g2.drawRect(x, y, tw, th);
            // Cross + diagonals dividing into 5 surfaces (standard odontogram notation)
            g2.setColor(new Color(160, 170, 180));
            g2.setStroke(new BasicStroke(0.8f));
            int cx = x + tw / 2, cy = y + th / 2;
            g2.drawLine(x, y, x + tw, y + th);
            g2.drawLine(x + tw, y, x, y + th);
            g2.drawLine(cx, y, cx, y + th);
            g2.drawLine(x, cy, x + tw, cy);
            g2.setStroke(new BasicStroke(1f));
        }
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private void lbl(JPanel p, String t, int x, int y, int w, int h) {
        JLabel l = new JLabel(t); l.setBounds(x, y, w, h); p.add(l);
    }

    private void lblK(JPanel p, String t, int x, int y, int w, int h) {
        widget.Label l = new widget.Label();
        l.setText(t);
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        l.setBounds(x, y, w, h);
        p.add(l);
    }

    private widget.Button btn(String text, String icon) {
        widget.Button b = new widget.Button();
        b.setText(text);
        try { b.setIcon(new ImageIcon(getClass().getResource(icon))); } catch (Exception ignored) {}
        return b;
    }

    private void generateNoPermintaan() {
        try {
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String prefix = "OD" + today;
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(
                "select count(*) from odontogram where no_permintaan like ?");
            ps.setString(1, prefix + "%");
            rs = ps.executeQuery();
            int seq = rs.next() ? rs.getInt(1) + 1 : 1;
            rs.close(); ps.close(); koneksi.close();
            NoPermintaan.setText(String.format("%s%04d", prefix, seq));
        } catch (Exception ex) {
            NoPermintaan.setText("OD" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "0001");
        }
    }

    // =========================================================
    //  EVENTS
    // =========================================================
    private void pilihDokter() {
        dokterDlg = new DlgCariDokter(null, true);
        dokterDlg.setVisible(true);
        if (dokterDlg.getTable().getSelectedRow() != -1) {
            KdDokter.setText(dokterDlg.getTable().getValueAt(dokterDlg.getTable().getSelectedRow(), 0).toString());
            NmDokter.setText(dokterDlg.getTable().getValueAt(dokterDlg.getTable().getSelectedRow(), 1).toString());
        }
    }

    private void baru() {
        TNoRw.setText(""); TNoRM.setText(""); TPasien.setText("");
        TDiagnosa.setText(""); TKdICD.setText(""); TACatatan.setText("");
        CBHasil.setSelectedIndex(0); CBRahang.setSelectedIndex(0);
        selectedGigi.clear();
        for (JCheckBox cb : cbMap.values()) cb.setSelected(false);
        editId = -1;
        updateSelectedLabel();
        toothPanel.repaint();
        generateNoPermintaan();
        TNoRw.requestFocus();
    }

    private void updateSelectedLabel() {
        if (selectedGigi.isEmpty()) {
            lblSelectedGigi.setText("Gigi terpilih: -");
        } else {
            lblSelectedGigi.setText("Gigi terpilih: " + String.join(", ", selectedGigi));
        }
    }

    private void simpan() {
        if (TNoRM.getText().trim().isEmpty()) {
            Valid.textKosong(TNoRw, "No. Rawat");
            return;
        }
        if (selectedGigi.isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "Pilih nomor gigi terlebih dahulu.");
            return;
        }
        if (KdDokter.getText().trim().isEmpty()) {
            Valid.textKosong(BtnDokter, "Dokter");
            return;
        }
        String gigiList = String.join(", ", selectedGigi);
        int reply = JOptionPane.showConfirmDialog(rootPane,
            "Simpan data pemeriksaan gigi: " + gigiList + "?",
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (reply != JOptionPane.YES_OPTION) return;
        try {
            koneksi = koneksiDB.condb();
            String tgl = Valid.SetTgl(DTPTgl.getSelectedItem() + "");
            if (editId > 0) {
                // Mode edit: update satu record (gigi pertama dari set)
                String gigi = selectedGigi.iterator().next();
                ps = koneksi.prepareStatement(
                    "update odontogram set no_gigi=?,hasil_pemeriksaan=?,rahang=?," +
                    "diagnosa_gigi=?,kd_icd=?,catatan=?,tanggal=?,kd_dokter=?,nm_dokter=? where id=?");
                ps.setString(1, gigi);
                ps.setString(2, CBHasil.getSelectedItem() + "");
                ps.setString(3, CBRahang.getSelectedItem() + "");
                ps.setString(4, TDiagnosa.getText().trim());
                ps.setString(5, TKdICD.getText().trim());
                ps.setString(6, TACatatan.getText().trim());
                ps.setString(7, tgl);
                ps.setString(8, KdDokter.getText());
                ps.setString(9, NmDokter.getText());
                ps.setInt(10, editId);
                ps.executeUpdate();
                ps.close();
            } else {
                // Mode baru: insert satu record per gigi yang dipilih
                ps = koneksi.prepareStatement(
                    "insert into odontogram(no_rawat,no_permintaan,no_gigi,hasil_pemeriksaan," +
                    "rahang,diagnosa_gigi,kd_icd,catatan,tanggal,kd_dokter,nm_dokter) values(?,?,?,?,?,?,?,?,?,?,?)");
                for (String gigi : selectedGigi) {
                    ps.setString(1, TNoRw.getText());
                    ps.setString(2, NoPermintaan.getText());
                    ps.setString(3, gigi);
                    ps.setString(4, CBHasil.getSelectedItem() + "");
                    ps.setString(5, CBRahang.getSelectedItem() + "");
                    ps.setString(6, TDiagnosa.getText().trim());
                    ps.setString(7, TKdICD.getText().trim());
                    ps.setString(8, TACatatan.getText().trim());
                    ps.setString(9, tgl);
                    ps.setString(10, KdDokter.getText());
                    ps.setString(11, NmDokter.getText());
                    ps.addBatch();
                }
                ps.executeBatch();
                ps.close();
            }
            koneksi.close();
            JOptionPane.showMessageDialog(rootPane, "Data berhasil disimpan (" + selectedGigi.size() + " gigi).");
            editId = -1;
            tampilData();
        } catch (Exception ex) {
            System.out.println("simpan odontogram: " + ex);
            JOptionPane.showMessageDialog(rootPane, "Gagal menyimpan: " + ex.getMessage());
        }
    }

    private void hapus() {
        if (editId < 0) { JOptionPane.showMessageDialog(rootPane, "Pilih data terlebih dahulu."); return; }
        int reply = JOptionPane.showConfirmDialog(rootPane, "Hapus data pemeriksaan gigi ini?",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (reply != JOptionPane.YES_OPTION) return;
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement("delete from odontogram where id=?");
            ps.setInt(1, editId);
            ps.executeUpdate();
            ps.close(); koneksi.close();
            JOptionPane.showMessageDialog(rootPane, "Data berhasil dihapus.");
            baru();
            tampilData();
        } catch (Exception ex) { System.out.println("hapus odontogram: " + ex); }
    }

    private void ganti() {
        if (tbData.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(rootPane, "Pilih data terlebih dahulu.");
            return;
        }
        pilihBaris();
    }

    private void pilihBaris() {
        int row = tbData.getSelectedRow();
        if (row < 0) return;
        String noRawat = tabMode.getValueAt(row, 2).toString();
        String noGigi  = tabMode.getValueAt(row, 7).toString();
        TNoRw.setText(noRawat);
        isRawat();
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(
                "select id,no_permintaan,no_gigi,hasil_pemeriksaan,rahang," +
                "diagnosa_gigi,kd_icd,catatan,tanggal,kd_dokter,nm_dokter " +
                "from odontogram where no_rawat=? and no_gigi=? order by id desc limit 1");
            ps.setString(1, noRawat);
            ps.setString(2, noGigi);
            rs = ps.executeQuery();
            if (rs.next()) {
                editId = rs.getInt("id");
                NoPermintaan.setText(rs.getString("no_permintaan"));
                selectedGigi.clear();
                for (JCheckBox cb : cbMap.values()) cb.setSelected(false);
                String g = rs.getString("no_gigi");
                selectedGigi.add(g);
                if (cbMap.containsKey(g)) cbMap.get(g).setSelected(true);
                updateSelectedLabel();
                CBHasil.setSelectedItem(rs.getString("hasil_pemeriksaan"));
                CBRahang.setSelectedItem(rs.getString("rahang"));
                TDiagnosa.setText(rs.getString("diagnosa_gigi"));
                TKdICD.setText(rs.getString("kd_icd"));
                TACatatan.setText(rs.getString("catatan"));
                String tglDb = rs.getString("tanggal");
                if (tglDb != null) Valid.SetTgl(DTPTgl, tglDb);
                KdDokter.setText(rs.getString("kd_dokter"));
                NmDokter.setText(rs.getString("nm_dokter"));
            }
            rs.close(); ps.close(); koneksi.close();
            toothPanel.repaint();
        } catch (Exception ex) { System.out.println("pilihBaris odontogram: " + ex); }
    }

    // =========================================================
    //  DATABASE
    // =========================================================
    private void isRawat() {
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(
                "select rp.no_rkm_medis, p.nm_pasien, rp.tgl_registrasi " +
                "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis " +
                "where rp.no_rawat=?");
            ps.setString(1, TNoRw.getText());
            rs = ps.executeQuery();
            if (rs.next()) {
                TNoRM.setText(rs.getString("no_rkm_medis"));
                TPasien.setText(rs.getString("nm_pasien"));
                String tgl = rs.getString("tgl_registrasi");
                if (tgl != null) Valid.SetTgl(DTPTgl, tgl);
            } else {
                TNoRM.setText(""); TPasien.setText("");
                JOptionPane.showMessageDialog(rootPane, "No. Rawat tidak ditemukan.");
            }
            rs.close(); ps.close(); koneksi.close();
        } catch (Exception ex) { System.out.println("isRawat odontogram: " + ex); }
    }

    private void tampilData() {
        tabMode.setRowCount(0);
        try {
            koneksi = koneksiDB.condb();
            String tglDari   = Valid.SetTgl(DTPDari.getSelectedItem() + "");
            String tglSampai = Valid.SetTgl(DTPSampai.getSelectedItem() + "");
            ps = koneksi.prepareStatement(
                "select o.id, rp.tgl_registrasi, rp.stts, o.no_rawat, rp.no_rkm_medis, " +
                "p.nm_pasien, o.kd_dokter, o.nm_dokter, o.no_gigi, o.diagnosa_gigi, o.kd_icd " +
                "from odontogram o " +
                "inner join reg_periksa rp on o.no_rawat=rp.no_rawat " +
                "inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis " +
                "where o.tanggal between ? and ? " +
                "order by o.tanggal desc, o.id desc limit 300");
            ps.setString(1, tglDari);
            ps.setString(2, tglSampai);
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    rs.getString("tgl_registrasi"),
                    rs.getString("stts"),
                    rs.getString("no_rawat"),
                    rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"),
                    rs.getString("kd_dokter"),
                    rs.getString("nm_dokter"),
                    rs.getString("no_gigi"),
                    rs.getString("diagnosa_gigi"),
                    rs.getString("kd_icd")
                });
            }
            rs.close(); ps.close(); koneksi.close();
        } catch (Exception ex) { System.out.println("tampilData odontogram: " + ex); }
    }

    private void cetak() {
        String noRawat = TNoRw.getText().trim();
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "Pilih No. Rawat terlebih dahulu.");
            return;
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            java.util.Map param = new java.util.HashMap();
            param.put("namars",   akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars",   akses.getkabupatenrs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("logo",     Sequel.cariGambar("select setting.logo from setting"));
            param.put("no_rawat", noRawat);
            Valid.MyReportqry(
                "rptOdontogram.jasper", "report",
                "::[ Laporan Pemeriksaan Odontogram ]::",
                "SELECT o.id, o.no_rawat, o.no_permintaan, o.no_gigi, o.hasil_pemeriksaan, " +
                "o.rahang, o.diagnosa_gigi, o.kd_icd, o.catatan, o.status_gigi, o.tanggal, " +
                "o.kd_dokter, o.nm_dokter, rp.no_rkm_medis, p.nm_pasien, " +
                "TIMESTAMPDIFF(YEAR, p.tgl_lahir, CURDATE()) AS umur, p.jk, p.alamat " +
                "FROM odontogram o " +
                "INNER JOIN reg_periksa rp ON o.no_rawat = rp.no_rawat " +
                "INNER JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis " +
                "WHERE o.no_rawat = '" + noRawat + "' ORDER BY o.no_gigi",
                param);
        } catch (Exception ex) {
            System.out.println("cetak odontogram: " + ex);
            JOptionPane.showMessageDialog(rootPane, "Gagal mencetak: " + ex.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void cariData() {
        String kw = TKeyWord.getText().trim();
        if (kw.isEmpty()) { tampilData(); return; }
        tabMode.setRowCount(0);
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(
                "select o.id, rp.tgl_registrasi, rp.stts, o.no_rawat, rp.no_rkm_medis, " +
                "p.nm_pasien, o.kd_dokter, o.nm_dokter, o.no_gigi, o.diagnosa_gigi, o.kd_icd " +
                "from odontogram o " +
                "inner join reg_periksa rp on o.no_rawat=rp.no_rawat " +
                "inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis " +
                "where o.no_rawat=? or rp.no_rkm_medis=? or p.nm_pasien like ? " +
                "order by o.tanggal desc limit 300");
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, "%" + kw + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    rs.getString("tgl_registrasi"),
                    rs.getString("stts"),
                    rs.getString("no_rawat"),
                    rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"),
                    rs.getString("kd_dokter"),
                    rs.getString("nm_dokter"),
                    rs.getString("no_gigi"),
                    rs.getString("diagnosa_gigi"),
                    rs.getString("kd_icd")
                });
            }
            rs.close(); ps.close(); koneksi.close();
        } catch (Exception ex) { System.out.println("cariData odontogram: " + ex); }
    }
}
