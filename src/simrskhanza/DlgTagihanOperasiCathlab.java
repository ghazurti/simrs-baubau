package simrskhanza;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPetugas;
import rekammedis.MasterCariTemplateLaporanOperasi;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Form Tagihan Operasi Cathlab.
 * Tabel target: {@code operasi_cathlab} (terpisah dari {@code operasi}).
 * Menampung: identitas, prosedur cathlab (jenis, kateter, alat khusus),
 * safety radiasi (dosis, waktu fluoro, kontras), tim dokter operator,
 * dokter anestesi, dan tim cathlab (2 scrub + 2 circulate + 1 radiographer)
 * beserta biaya masing-masing.
 */
public final class DlgTagihanOperasiCathlab extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private final NumberFormat fmt = NumberFormat.getInstance(new Locale("id","ID"));
    private int i = 0;

    // ==== label sesuai enum di tabel operasi_cathlab.jenis_prosedur ====
    private static final String[] JENIS_PROSEDUR = {
        "Angiografi Diagnostik",
        "PCI (Percutaneous Coronary Intervention)",
        "TAVI (Transcatheter Aortic Valve Implantation)",
        "Ablasi Aritmia",
        "Pacemaker/ICD",
        "Ballooning",
        "IABP (Intra-Aortic Balloon Pump)",
        "Peripheral Angiography",
        "Lainnya"
    };
    private static final String[] KATEGORI = {"-","Khusus","Besar","Sedang","Kecil","Elektive","Emergency"};

    public DlgTagihanOperasiCathlab(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setSize(1200, 720);
        setLocationRelativeTo(parent);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat","Tgl Operasi","Kd Paket","Nama Paket","Jenis Prosedur","Kategori",
            "Nama Pasien","Operator 1","Scrub 1","Circulate 1","Radiographer",
            "Dosis (mGy)","Fluoro (mnt)","Kontras (ml)","Total Biaya","Status"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbData.setModel(tabMode);
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbData.setPreferredScrollableViewportSize(new Dimension(600, 200));
        int[] widths = {110,140,80,180,180,80,180,140,120,120,120,80,80,80,110,60};
        for (i = 0; i < widths.length; i++) {
            TableColumn c = tbData.getColumnModel().getColumn(i);
            c.setPreferredWidth(widths[i]);
        }
        tbData.setDefaultRenderer(Object.class, new WarnaTable());

        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(KATEGORI));
        cmbJenisProsedur.setModel(new javax.swing.DefaultComboBoxModel<>(JENIS_PROSEDUR));
        cmbAnestesi.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "-","General","Spinal","Lokal","Regional","Sedasi","Tanpa Anestesi"
        }));
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Ranap","Ralan"}));

        tglOperasi.setDate(new Date());
        tglSelesai.setDate(new Date());
        tglCari1.setDate(new Date());
        tglCari2.setDate(new Date());

        emptTeks();
        tampil();
    }

    // =====================================================================
    // initComponents — hand-coded karena tidak pakai .form NetBeans.
    // Layout: absolute (setBounds) untuk konsistensi dengan form Khanza lain.
    // =====================================================================
    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        panelTop      = new widget.panelisi();
        panelInput    = new widget.PanelBiasa();
        panelToolbar  = new widget.panelisi();
        Scroll        = new widget.ScrollPane();
        tbData        = new widget.Table();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setTitle("Tagihan Operasi Cathlab");

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240,245,235)),
            "::[ Tagihan Operasi Cathlab ]::",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new java.awt.Font("Tahoma",0,11), new java.awt.Color(50,50,50)));
        internalFrame1.setLayout(new java.awt.BorderLayout(1,1));

        // ---- TOP CARI ----
        panelTop.setPreferredSize(new Dimension(100, 40));
        panelTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 8));
        widget.Label lblCari = new widget.Label(); lblCari.setText("Tgl. :");
        lblCari.setPreferredSize(new Dimension(35,23));
        lblCari.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tglCari1 = new widget.Tanggal(); tglCari1.setPreferredSize(new Dimension(100,23)); tglCari1.setDisplayFormat("dd-MM-yyyy");
        widget.Label lblSd = new widget.Label(); lblSd.setText("s/d");
        lblSd.setPreferredSize(new Dimension(25,23)); lblSd.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tglCari2 = new widget.Tanggal(); tglCari2.setPreferredSize(new Dimension(100,23)); tglCari2.setDisplayFormat("dd-MM-yyyy");
        widget.Label lblKey = new widget.Label(); lblKey.setText("Kata Kunci :");
        lblKey.setPreferredSize(new Dimension(75,23));
        lblKey.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        TCari = new widget.TextBox(); TCari.setPreferredSize(new Dimension(200,23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER) tampil();
            }
        });
        BtnCari = new widget.Button();
        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setToolTipText("Alt+C");
        BtnCari.setMnemonic('C');
        BtnCari.setPreferredSize(new Dimension(28,23));
        BtnCari.addActionListener(e -> tampil());
        BtnAll  = new widget.Button();
        BtnAll.setText("Semua"); BtnAll.setMnemonic('U');
        BtnAll.setToolTipText("Alt+U");
        BtnAll.setPreferredSize(new Dimension(90,23));
        BtnAll.addActionListener(e -> { TCari.setText(""); tampil(); });
        panelTop.add(lblCari); panelTop.add(tglCari1); panelTop.add(lblSd); panelTop.add(tglCari2);
        panelTop.add(lblKey);  panelTop.add(TCari);    panelTop.add(BtnCari); panelTop.add(BtnAll);
        internalFrame1.add(panelTop, java.awt.BorderLayout.PAGE_START);

        // ---- CENTER: tabel + form input ----
        Scroll.setViewportView(tbData);
        tbData.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (tbData.getSelectedRow() > -1) loadDataDariTabel();
            }
        });

        panelInput.setLayout(null);
        panelInput.setPreferredSize(new Dimension(100, 380));
        buildFormFields(panelInput);

        // Wrap panelInput dengan JScrollPane supaya field di bawah bisa di-scroll
        javax.swing.JScrollPane spInput = new javax.swing.JScrollPane(panelInput);
        spInput.setBorder(null);
        spInput.getVerticalScrollBar().setUnitIncrement(24);
        // Auto-scroll bila mouse wheel di atas panelInput (JScrollPane parent kadang
        // tidak menerima event kalau kursor di child kosong)
        panelInput.addMouseWheelListener(e -> {
            javax.swing.JScrollBar bar = spInput.getVerticalScrollBar();
            bar.setValue(bar.getValue() + e.getUnitsToScroll() * bar.getUnitIncrement());
            e.consume();
        });

        javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT, Scroll, spInput);
        split.setResizeWeight(0.30);
        split.setDividerLocation(210);
        internalFrame1.add(split, java.awt.BorderLayout.CENTER);

        // ---- TOOLBAR BOTTOM ---- (ikon standar Khanza)
        panelToolbar.setPreferredSize(new Dimension(100, 40));
        panelToolbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 8));

        BtnSimpan = new widget.Button();
        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setText("Simpan"); BtnSimpan.setMnemonic('S'); BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setPreferredSize(new Dimension(100,25));
        BtnSimpan.addActionListener(e -> simpan());

        BtnBaru = new widget.Button();
        BtnBaru.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png")));
        BtnBaru.setText("Baru"); BtnBaru.setMnemonic('B'); BtnBaru.setToolTipText("Alt+B");
        BtnBaru.setPreferredSize(new Dimension(90,25));
        BtnBaru.addActionListener(e -> emptTeks());

        BtnEdit = new widget.Button();
        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/edit.png")));
        BtnEdit.setText("Ganti"); BtnEdit.setMnemonic('G'); BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setPreferredSize(new Dimension(90,25));
        BtnEdit.addActionListener(e -> edit());

        BtnHapus = new widget.Button();
        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setText("Hapus"); BtnHapus.setMnemonic('H'); BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setPreferredSize(new Dimension(95,25));
        BtnHapus.addActionListener(e -> hapus());

        BtnPrint = new widget.Button();
        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setText("Cetak"); BtnPrint.setMnemonic('T'); BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setPreferredSize(new Dimension(95,25));
        BtnPrint.addActionListener(e -> cetak());

        BtnKeluar = new widget.Button();
        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setText("Keluar"); BtnKeluar.setMnemonic('K'); BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setPreferredSize(new Dimension(95,25));
        BtnKeluar.addActionListener(e -> dispose());

        LblTotal = new widget.Label();
        LblTotal.setText("Total Biaya : 0");
        LblTotal.setPreferredSize(new Dimension(320,25));
        LblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        LblTotal.setFont(new java.awt.Font("Tahoma",1,13));
        LblTotal.setForeground(new java.awt.Color(180,60,0));

        panelToolbar.add(BtnSimpan); panelToolbar.add(BtnBaru);
        panelToolbar.add(BtnEdit);   panelToolbar.add(BtnHapus);
        panelToolbar.add(BtnPrint);  panelToolbar.add(LblTotal);
        panelToolbar.add(BtnKeluar);
        internalFrame1.add(panelToolbar, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        pack();
    }

    /**
     * Bangun field-field input di panelInput. Layout absolute (setBounds)
     * dengan 2 kolom (kiri-kanan) supaya mirip pola DlgTagihanOperasi.
     * Semua label kanan-align, field lookup pakai icon Search-16x16.
     */
    private void buildFormFields(widget.PanelBiasa p) {
        // Coord kolom kiri:  label 0-105 (right), input 110-...
        // Coord kolom kanan: label 620-720 (right), input 725-...
        int y = 8, h = 22, dy = 26;

        // ============ IDENTITAS PASIEN ============
        addLabel(p, "No.Rawat :", 0, y, 100);
        TNoRw = new widget.TextBox(); TNoRw.setBounds(110, y, 160, h); p.add(TNoRw);
        BtnCariRawat = new widget.Button();
        BtnCariRawat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnCariRawat.setBounds(272, y, 24, h);
        BtnCariRawat.setToolTipText("Cari pasien (Enter juga bisa)");
        BtnCariRawat.addActionListener(e -> loadPasienDariNoRawat());
        p.add(BtnCariRawat);
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER) loadPasienDariNoRawat();
            }
        });

        addLabel(p, "Nama Pasien :", 300, y, 100);
        TPasien = new widget.TextBox(); TPasien.setBounds(405, y, 210, h); TPasien.setEditable(false); p.add(TPasien);

        addLabel(p, "Status :", 620, y, 100);
        cmbStatus = new widget.ComboBox(); cmbStatus.setBounds(725, y, 130, h); p.add(cmbStatus);

        // ============ KATEGORI / ANESTESI / TANGGAL ============
        y += dy;
        addLabel(p, "Kategori :", 0, y, 100);
        cmbKategori = new widget.ComboBox(); cmbKategori.setBounds(110, y, 186, h); p.add(cmbKategori);

        addLabel(p, "Jenis Anestesi :", 300, y, 100);
        cmbAnestesi = new widget.ComboBox(); cmbAnestesi.setBounds(405, y, 210, h); p.add(cmbAnestesi);

        addLabel(p, "Tanggal :", 620, y, 100);
        tglOperasi = new widget.Tanggal(); tglOperasi.setBounds(725, y, 180, h);
        tglOperasi.setDisplayFormat("dd-MM-yyyy HH:mm:ss"); p.add(tglOperasi);

        // ============ KD/NM PAKET & TGL SELESAI ============
        y += dy;
        addLabel(p, "Kd Paket :", 0, y, 100);
        TKdPaket = new widget.TextBox(); TKdPaket.setBounds(110, y, 160, h); p.add(TKdPaket);
        BtnCariPaket = new widget.Button();
        BtnCariPaket.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnCariPaket.setBounds(272, y, 24, h);
        BtnCariPaket.setToolTipText("Cari paket operasi");
        BtnCariPaket.addActionListener(e -> pilihPaket());
        p.add(BtnCariPaket);

        addLabel(p, "Nm Paket :", 300, y, 100);
        TNmPaket = new widget.TextBox(); TNmPaket.setBounds(405, y, 210, h);
        TNmPaket.setEditable(false); p.add(TNmPaket);

        addLabel(p, "Tgl Selesai :", 620, y, 100);
        tglSelesai = new widget.Tanggal(); tglSelesai.setBounds(725, y, 180, h);
        tglSelesai.setDisplayFormat("dd-MM-yyyy HH:mm:ss"); p.add(tglSelesai);

        // ============ JENIS PROSEDUR (khusus cathlab) ============
        y += dy;
        addLabel(p, "Jenis Prosedur :", 0, y, 100);
        cmbJenisProsedur = new widget.ComboBox(); cmbJenisProsedur.setBounds(110, y, 505, h);
        p.add(cmbJenisProsedur);

        addLabel(p, "No Kateter :", 620, y, 100);
        TNoKateter = new widget.TextBox(); TNoKateter.setBounds(725, y, 180, h); p.add(TNoKateter);

        // ============ DIAGNOSA & LAPORAN ============
        y += dy;
        addLabel(p, "Dx Pre-operatif :", 0, y, 100);
        TDxPre = new widget.TextBox(); TDxPre.setBounds(110, y, 505, h); p.add(TDxPre);
        addLabel(p, "Alat Khusus :", 620, y, 100);
        TAlatKhusus = new widget.TextBox(); TAlatKhusus.setBounds(725, y, 180, h); p.add(TAlatKhusus);

        y += dy;
        addLabel(p, "Dx Post-operatif :", 0, y, 100);
        TDxPost = new widget.TextBox(); TDxPost.setBounds(110, y, 795, h); p.add(TDxPost);

        // ---- LAPORAN OPERASI (multi-line, template DB + tombol perbesar) ----
        y += dy + 4;
        addLabel(p, "Lap. Operasi :", 0, y, 100);

        BtnTemplate = new widget.Button();
        BtnTemplate.setText("Cari Template");
        BtnTemplate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnTemplate.setBounds(110, y, 170, h);
        BtnTemplate.setToolTipText("Cari template laporan operasi dari master template");
        BtnTemplate.addActionListener(e -> cariTemplate());
        p.add(BtnTemplate);

        BtnPerbesarLaporan = new widget.Button();
        BtnPerbesarLaporan.setText("Perbesar Editor");
        BtnPerbesarLaporan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnPerbesarLaporan.setBounds(285, y, 170, h);
        BtnPerbesarLaporan.setToolTipText("Buka editor besar untuk laporan operasi (F11)");
        BtnPerbesarLaporan.addActionListener(e -> perbesarLaporan());
        p.add(BtnPerbesarLaporan);

        y += dy;
        TLaporan = new javax.swing.JTextArea();
        TLaporan.setLineWrap(true);
        TLaporan.setWrapStyleWord(true);
        TLaporan.setFont(new java.awt.Font("Tahoma", 0, 11));
        TLaporan.setMargin(new java.awt.Insets(4, 6, 4, 6));
        javax.swing.JScrollPane spLaporan = new javax.swing.JScrollPane(TLaporan);
        spLaporan.setBounds(0, y, 905, 140);
        p.add(spLaporan);
        // F11 shortcut untuk buka editor besar
        TLaporan.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("F11"), "perbesar");
        TLaporan.getActionMap().put("perbesar", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { perbesarLaporan(); }
        });
        y += 140 - dy; // kompensasi tinggi text area supaya section berikut tetap rapi

        // ============ SAFETY RADIASI (khusus cathlab) ============
        y += dy + 8;
        addSection(p, "SAFETY RADIASI", 0, y, 905);
        y += dy;
        addLabel(p, "Dosis (mGy) :", 0, y, 100);
        TDosis = new widget.TextBox(); TDosis.setBounds(110, y, 120, h); p.add(TDosis);
        addLabel(p, "Waktu Fluoroskopi (menit) :", 240, y, 160);
        TFluoro = new widget.TextBox(); TFluoro.setBounds(405, y, 120, h); p.add(TFluoro);
        addLabel(p, "Kontras (ml) :", 620, y, 100);
        TKontras = new widget.TextBox(); TKontras.setBounds(725, y, 180, h); p.add(TKontras);

        // ============ TIM DOKTER (2 kolom: Operator kiri, Anestesi kanan) ============
        y += dy + 8;
        addSection(p, "TIM DOKTER", 0, y, 905);
        y += dy;
        buildRowPetugas2Col(p, "Operator 1", y, true,  "op1",   "dr Anestesi",         true,  "anes");  y += dy;
        buildRowPetugas2Col(p, "Operator 2", y, true,  "op2",   "Asisten Operator 1",  false, "asop1"); y += dy;
        buildRowPetugas2Col(p, "Operator 3", y, true,  "op3",   "Asisten Operator 2",  false, "asop2"); y += dy;
        buildRowPetugas2Col(p, "Asisten Operator 3", y, false, "asop3", null, false, null);            y += dy;

        // ============ TIM CATHLAB (Scrub / Circulate / Radiographer) ============
        y += 8;
        addSection(p, "TIM CATHLAB", 0, y, 905);
        y += dy;
        buildRowPetugas2Col(p, "Scrub Nurse 1", y, false, "scrub1", "Circulate Nurse 1", false, "circ1"); y += dy;
        buildRowPetugas2Col(p, "Scrub Nurse 2", y, false, "scrub2", "Circulate Nurse 2", false, "circ2"); y += dy;
        buildRowPetugas2Col(p, "Radiographer",  y, false, "rad1",   "Monitoring 1",      false, "mon1");  y += dy;

        // ============ TIM MONITORING & NURSE ============
        y += 8;
        addSection(p, "TIM MONITORING & NURSE", 0, y, 905);
        y += dy;
        buildRowPetugas2Col(p, "Monitoring 2", y, false, "mon2",   "Monitoring 3", false, "mon3");   y += dy;
        buildRowPetugas2Col(p, "Nurse 1",      y, false, "nurse1", "Nurse 2",      false, "nurse2"); y += dy;
        buildRowPetugas2Col(p, "Nurse 3",      y, false, "nurse3", null,           false, null);      y += dy;

        // Auto-recalculate total (safety radiasi tidak masuk total, hanya info klinis)
        addTotalListener(TDosis); addTotalListener(TFluoro); addTotalListener(TKontras);

        p.setPreferredSize(new Dimension(915, y + 40));
    }

    private void addLabel(widget.PanelBiasa p, String txt, int x, int y, int w) {
        widget.Label l = new widget.Label();
        l.setText(txt);
        l.setBounds(x, y, w, 22);
        l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        p.add(l);
    }

    /**
     * Section header: label bold + garis pemisah horizontal (gaya rapi
     * seperti DlgTagihanOperasi standar, tanpa kotak siku merah).
     */
    private void addSection(widget.PanelBiasa p, String title, int x, int y, int w) {
        widget.Label l = new widget.Label();
        l.setText(" " + title + " ");
        l.setBounds(x, y, w, 22);
        l.setFont(new java.awt.Font("Tahoma", 1, 11));
        l.setForeground(new java.awt.Color(255, 255, 255));
        l.setBackground(new java.awt.Color(178, 91, 32));
        l.setOpaque(true);
        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        p.add(l);
    }

    /**
     * Bikin 1 baris 2-kolom: petugas kiri + petugas kanan (opsional).
     * Setiap kolom = label + kd + nama + tombol lookup + label "Jasa" + biaya.
     */
    private void buildRowPetugas2Col(widget.PanelBiasa p,
                                       String labelKiri, int y, boolean dokterKiri, String keyKiri,
                                       String labelKanan, boolean dokterKanan, String keyKanan) {
        buildRowPetugas(p, labelKiri, 0, y, dokterKiri, keyKiri);
        if (labelKanan != null && keyKanan != null) {
            buildRowPetugas(p, labelKanan, 620, y, dokterKanan, keyKanan);
        }
    }

    /**
     * Bikin 1 kolom petugas mulai di posisi xBase.
     * Layout per kolom (lebar ~285 px):
     *   label 0-100 (right) | kd 110-190 | nama 195-... | btn 25px | jasa 90px
     */
    private void buildRowPetugas(widget.PanelBiasa p, String label, int xBase, int y, boolean dokter, String key) {
        int labX = xBase,       labW = 100;
        int kdX  = xBase + 110, kdW  = 80;
        int nmX  = xBase + 192, nmW  = 140;
        int btnX = xBase + 334, btnW = 24;
        int biayaLX = xBase + 360, biayaLW = 30;
        int biayaX  = xBase + 395, biayaW = 90;

        addLabel(p, label + " :", labX, y, labW);

        widget.TextBox kd = new widget.TextBox(); kd.setBounds(kdX, y, kdW, 22);
        widget.TextBox nm = new widget.TextBox(); nm.setBounds(nmX, y, nmW, 22); nm.setEditable(false);
        widget.Button  bt = new widget.Button();
        bt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        bt.setBounds(btnX, y, btnW, 22);
        bt.setToolTipText("Cari " + label);
        widget.Label   bl = new widget.Label();
        bl.setText("Rp");
        bl.setBounds(biayaLX, y, biayaLW, 22);
        bl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        widget.TextBox bi = new widget.TextBox(); bi.setBounds(biayaX, y, biayaW, 22);
        bi.setToolTipText("Jasa/tarif");
        p.add(kd); p.add(nm); p.add(bt); p.add(bl); p.add(bi);
        addTotalListener(bi);

        final boolean isDokter = dokter;
        bt.addActionListener(e -> lookupPetugas(kd, nm, isDokter));
        kd.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER) resolveNamaPetugas(kd.getText(), nm, isDokter);
            }
        });

        petugasKd.put(key, kd);
        petugasNm.put(key, nm);
        petugasBi.put(key, bi);
    }

    /** Listener untuk auto-recalculate Total Biaya saat field biaya diubah */
    private void addTotalListener(widget.TextBox t) {
        t.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { recalcTotal(); }
        });
    }

    // =====================================================================
    // LOOKUP PETUGAS/DOKTER
    // =====================================================================
    private void lookupPetugas(widget.TextBox kd, widget.TextBox nm, boolean dokter) {
        try {
            if (dokter) {
                DlgCariDokter dlg = new DlgCariDokter(null, false);
                dlg.isCek();
                dlg.setLocationRelativeTo(this);
                dlg.setVisible(true);
                if (dlg.getTable().getSelectedRow() > -1) {
                    kd.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),0).toString());
                    nm.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),1).toString());
                }
            } else {
                DlgCariPetugas dlg = new DlgCariPetugas(null, false);
                dlg.isCek();
                dlg.setLocationRelativeTo(this);
                dlg.setVisible(true);
                if (dlg.getTable().getSelectedRow() > -1) {
                    kd.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),0).toString());
                    nm.setText(dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(),1).toString());
                }
            }
        } catch (Exception e) {
            System.out.println("Notif lookupPetugas : " + e);
        }
    }

    private void resolveNamaPetugas(String kode, widget.TextBox nm, boolean dokter) {
        if (kode == null || kode.trim().isEmpty()) { nm.setText(""); return; }
        String q = dokter ? "select nm_dokter from dokter where kd_dokter=?"
                          : "select nama from petugas where nip=?";
        nm.setText(Sequel.cariIsi(q, kode.trim()));
    }

    private void loadPasienDariNoRawat() {
        if (TNoRw.getText().trim().isEmpty()) return;
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(
                "select pasien.nm_pasien, reg_periksa.status_lanjut from reg_periksa "+
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "where reg_periksa.no_rawat=?");
            ps.setString(1, TNoRw.getText().trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                TPasien.setText(rs.getString("nm_pasien"));
                String stlanjut = rs.getString("status_lanjut");
                if ("Ranap".equalsIgnoreCase(stlanjut)) cmbStatus.setSelectedItem("Ranap");
                else                                     cmbStatus.setSelectedItem("Ralan");
            } else {
                TPasien.setText("");
                JOptionPane.showMessageDialog(this, "No.Rawat tidak ditemukan.");
            }
        } catch (Exception e) {
            System.out.println("Notif loadPasienDariNoRawat : " + e);
        } finally { closeRs(); }
    }

    /**
     * Pilih paket operasi. Kalau paket ada tarif default cathlab (5 kolom
     * baru di paket_operasi), auto-isi ke field jasa.
     */
    /**
     * Dialog pilih paket operasi. Belum ada DlgCariPaketOperasi tersendiri
     * di project, jadi pakai JOptionPane input manual dulu.
     * Nanti kalau perlu bisa dibuat form pencarian tersendiri.
     */
    private void pilihPaket() {
        String kd = JOptionPane.showInputDialog(this,
            "Masukkan Kode Paket Operasi:\n(cek daftar paket di menu Master Paket Operasi)",
            TKdPaket.getText());
        if (kd != null && !kd.trim().isEmpty()) {
            TKdPaket.setText(kd.trim());
            String nm = Sequel.cariIsi("select nm_perawatan from paket_operasi where kode_paket=?", kd.trim());
            if (nm == null || nm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Kode paket tidak ditemukan.");
                return;
            }
            TNmPaket.setText(nm);
            loadTarifDariPaket(kd.trim());
        }
    }

    /** Isi tarif default petugas cathlab dari paket_operasi bila tersedia. */
    private void loadTarifDariPaket(String kdPaket) {
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(
                "select operator1,operator2,operator3,dokter_anestesi,"+
                "ifnull(asisten_operator1,0) as asisten_operator1,"+
                "ifnull(asisten_operator2,0) as asisten_operator2,"+
                "ifnull(asisten_operator3,0) as asisten_operator3,"+
                "ifnull(scrub_nurse1,0) as scrub_nurse1, ifnull(scrub_nurse2,0) as scrub_nurse2,"+
                "ifnull(circulate_nurse1,0) as circulate_nurse1, ifnull(circulate_nurse2,0) as circulate_nurse2,"+
                "ifnull(radiographer1,0) as radiographer1 "+
                "from paket_operasi where kode_paket=?");
            ps.setString(1, kdPaket);
            rs = ps.executeQuery();
            if (rs.next()) {
                petugasBi.get("op1").setText(numStr(rs.getDouble("operator1")));
                petugasBi.get("op2").setText(numStr(rs.getDouble("operator2")));
                petugasBi.get("op3").setText(numStr(rs.getDouble("operator3")));
                petugasBi.get("asop1").setText(numStr(rs.getDouble("asisten_operator1")));
                petugasBi.get("asop2").setText(numStr(rs.getDouble("asisten_operator2")));
                petugasBi.get("asop3").setText(numStr(rs.getDouble("asisten_operator3")));
                petugasBi.get("anes").setText(numStr(rs.getDouble("dokter_anestesi")));
                petugasBi.get("scrub1").setText(numStr(rs.getDouble("scrub_nurse1")));
                petugasBi.get("scrub2").setText(numStr(rs.getDouble("scrub_nurse2")));
                petugasBi.get("circ1").setText(numStr(rs.getDouble("circulate_nurse1")));
                petugasBi.get("circ2").setText(numStr(rs.getDouble("circulate_nurse2")));
                petugasBi.get("rad1").setText(numStr(rs.getDouble("radiographer1")));
                // Monitoring & Nurse: tidak ada default di paket_operasi (dientri manual).
                recalcTotal();
            }
        } catch (Exception e) {
            System.out.println("Notif loadTarifDariPaket : " + e);
        } finally { closeRs(); }
    }

    // =====================================================================
    // SIMPAN / EDIT / HAPUS
    // =====================================================================
    private void simpan() {
        if (TNoRw.getText().trim().isEmpty()) { warn("No.Rawat wajib diisi."); return; }
        if (TKdPaket.getText().trim().isEmpty()) { warn("Kode Paket wajib diisi."); return; }
        try {
            koneksi = koneksiDB.condb();
            String sql = "insert into operasi_cathlab (" +
                "no_rawat,tgl_operasi,kode_paket,jenis_anasthesi,kategori,jam_selesai,diagnosis_pre,diagnosis_post,laporan_operasi,"+
                "jenis_prosedur,nomor_kateter,alat_khusus,dosis_radiasi_mgy,waktu_fluoroskopi_min,kontras_ml,"+
                "operator1,operator2,operator3,biaya_operator1,biaya_operator2,biaya_operator3,"+
                "asisten_operator1,asisten_operator2,asisten_operator3,biaya_asisten_operator1,biaya_asisten_operator2,biaya_asisten_operator3,"+
                "dokter_anestesi,biaya_dokter_anestesi,"+
                "scrub_nurse1,scrub_nurse2,circulate_nurse1,circulate_nurse2,radiographer1,"+
                "biaya_scrub_nurse1,biaya_scrub_nurse2,biaya_circulate_nurse1,biaya_circulate_nurse2,biaya_radiographer1,"+
                "monitoring1,monitoring2,monitoring3,biaya_monitoring1,biaya_monitoring2,biaya_monitoring3,"+
                "nurse1,nurse2,nurse3,biaya_nurse1,biaya_nurse2,biaya_nurse3,"+
                "biayaalat,biayasewaok,akomodasi,bagian_rs,biayasarpras,status) values ("+
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"+ // s/d kontras_ml (15)
                "?,?,?,?,?,?,"+                   // operator 1-3 + biaya (6)
                "?,?,?,?,?,?,"+                   // asisten operator 1-3 + biaya (6)
                "?,?,"+                           // anestesi + biaya (2)
                "?,?,?,?,?,?,?,?,?,?,"+           // scrub/circ/rad kd(5)+biaya(5) = 10
                "?,?,?,?,?,?,"+                   // monitoring 1-3 + biaya (6)
                "?,?,?,?,?,?,"+                   // nurse 1-3 + biaya (6)
                "?,?,?,?,?,?)";                   // biaya lain(5) + status = 6
            ps = koneksi.prepareStatement(sql);
            bindParams(ps);
            int n = ps.executeUpdate();
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "Data tersimpan.");
                tampil();
                emptTeks();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal simpan : " + e.getMessage());
            System.out.println("Notif simpan : " + e);
        } finally { closeRs(); }
    }

    private void edit() {
        if (tbData.getSelectedRow() < 0) { warn("Pilih baris data yang mau diedit."); return; }
        try {
            koneksi = koneksiDB.condb();
            String sql = "update operasi_cathlab set jenis_anasthesi=?,kategori=?,jam_selesai=?,diagnosis_pre=?,diagnosis_post=?,laporan_operasi=?,"+
                "jenis_prosedur=?,nomor_kateter=?,alat_khusus=?,dosis_radiasi_mgy=?,waktu_fluoroskopi_min=?,kontras_ml=?,"+
                "operator1=?,operator2=?,operator3=?,biaya_operator1=?,biaya_operator2=?,biaya_operator3=?,"+
                "asisten_operator1=?,asisten_operator2=?,asisten_operator3=?,biaya_asisten_operator1=?,biaya_asisten_operator2=?,biaya_asisten_operator3=?,"+
                "dokter_anestesi=?,biaya_dokter_anestesi=?,"+
                "scrub_nurse1=?,scrub_nurse2=?,circulate_nurse1=?,circulate_nurse2=?,radiographer1=?,"+
                "biaya_scrub_nurse1=?,biaya_scrub_nurse2=?,biaya_circulate_nurse1=?,biaya_circulate_nurse2=?,biaya_radiographer1=?,"+
                "monitoring1=?,monitoring2=?,monitoring3=?,biaya_monitoring1=?,biaya_monitoring2=?,biaya_monitoring3=?,"+
                "nurse1=?,nurse2=?,nurse3=?,biaya_nurse1=?,biaya_nurse2=?,biaya_nurse3=?,"+
                "biayaalat=?,biayasewaok=?,akomodasi=?,bagian_rs=?,biayasarpras=?,status=? "+
                "where no_rawat=? and tgl_operasi=? and kode_paket=?";
            ps = koneksi.prepareStatement(sql);
            bindParamsEdit(ps);
            int n = ps.executeUpdate();
            if (n > 0) { JOptionPane.showMessageDialog(this, "Data diperbarui."); tampil(); emptTeks(); }
            else       JOptionPane.showMessageDialog(this, "Tidak ada baris yang diperbarui.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal edit : " + e.getMessage());
        } finally { closeRs(); }
    }

    private void hapus() {
        if (tbData.getSelectedRow() < 0) { warn("Pilih baris yang mau dihapus."); return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus data operasi cathlab ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement("delete from operasi_cathlab where no_rawat=? and tgl_operasi=? and kode_paket=?");
            ps.setString(1, tbData.getValueAt(tbData.getSelectedRow(),0).toString());
            ps.setString(2, tbData.getValueAt(tbData.getSelectedRow(),1).toString());
            ps.setString(3, tbData.getValueAt(tbData.getSelectedRow(),2).toString());
            int n = ps.executeUpdate();
            if (n > 0) { JOptionPane.showMessageDialog(this, "Data dihapus."); tampil(); emptTeks(); }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus : " + e.getMessage());
        } finally { closeRs(); }
    }

    private void bindParams(PreparedStatement ps) throws Exception {
        int i = 1;
        ps.setString(i++, TNoRw.getText().trim());
        ps.setString(i++, Valid.SetTgl(tglOperasi.getSelectedItem()+"")+" "+tglOperasi.getSelectedItem().toString().substring(11,19));
        ps.setString(i++, TKdPaket.getText().trim());
        i = bindCommonFields(ps, i);
    }
    private void bindParamsEdit(PreparedStatement ps) throws Exception {
        int i = 1;
        i = bindCommonFields(ps, i);
        // where clause
        int row = tbData.getSelectedRow();
        ps.setString(i++, tbData.getValueAt(row,0).toString());
        ps.setString(i++, tbData.getValueAt(row,1).toString());
        ps.setString(i++, tbData.getValueAt(row,2).toString());
    }
    private int bindCommonFields(PreparedStatement ps, int i) throws Exception {
        ps.setString(i++, valStr(cmbAnestesi));                    // jenis_anasthesi
        ps.setString(i++, valStr(cmbKategori));                    // kategori
        ps.setString(i++, Valid.SetTgl(tglSelesai.getSelectedItem()+"")+" "+tglSelesai.getSelectedItem().toString().substring(11,19));
        ps.setString(i++, TDxPre.getText());
        ps.setString(i++, TDxPost.getText());
        ps.setString(i++, TLaporan.getText());
        ps.setString(i++, valStr(cmbJenisProsedur));               // jenis_prosedur
        ps.setString(i++, TNoKateter.getText());
        ps.setString(i++, TAlatKhusus.getText());
        ps.setDouble(i++, numVal(TDosis));
        ps.setDouble(i++, numVal(TFluoro));
        ps.setDouble(i++, numVal(TKontras));
        // operator 1-3
        ps.setString(i++, textNullable(petugasKd.get("op1")));
        ps.setString(i++, textNullable(petugasKd.get("op2")));
        ps.setString(i++, textNullable(petugasKd.get("op3")));
        ps.setDouble(i++, numVal(petugasBi.get("op1")));
        ps.setDouble(i++, numVal(petugasBi.get("op2")));
        ps.setDouble(i++, numVal(petugasBi.get("op3")));
        // asisten operator 1-3
        ps.setString(i++, textNullable(petugasKd.get("asop1")));
        ps.setString(i++, textNullable(petugasKd.get("asop2")));
        ps.setString(i++, textNullable(petugasKd.get("asop3")));
        ps.setDouble(i++, numVal(petugasBi.get("asop1")));
        ps.setDouble(i++, numVal(petugasBi.get("asop2")));
        ps.setDouble(i++, numVal(petugasBi.get("asop3")));
        // anestesi
        ps.setString(i++, textNullable(petugasKd.get("anes")));
        ps.setDouble(i++, numVal(petugasBi.get("anes")));
        // cathlab team
        ps.setString(i++, textNullable(petugasKd.get("scrub1")));
        ps.setString(i++, textNullable(petugasKd.get("scrub2")));
        ps.setString(i++, textNullable(petugasKd.get("circ1")));
        ps.setString(i++, textNullable(petugasKd.get("circ2")));
        ps.setString(i++, textNullable(petugasKd.get("rad1")));
        ps.setDouble(i++, numVal(petugasBi.get("scrub1")));
        ps.setDouble(i++, numVal(petugasBi.get("scrub2")));
        ps.setDouble(i++, numVal(petugasBi.get("circ1")));
        ps.setDouble(i++, numVal(petugasBi.get("circ2")));
        ps.setDouble(i++, numVal(petugasBi.get("rad1")));
        // monitoring 1-3
        ps.setString(i++, textNullable(petugasKd.get("mon1")));
        ps.setString(i++, textNullable(petugasKd.get("mon2")));
        ps.setString(i++, textNullable(petugasKd.get("mon3")));
        ps.setDouble(i++, numVal(petugasBi.get("mon1")));
        ps.setDouble(i++, numVal(petugasBi.get("mon2")));
        ps.setDouble(i++, numVal(petugasBi.get("mon3")));
        // nurse 1-3
        ps.setString(i++, textNullable(petugasKd.get("nurse1")));
        ps.setString(i++, textNullable(petugasKd.get("nurse2")));
        ps.setString(i++, textNullable(petugasKd.get("nurse3")));
        ps.setDouble(i++, numVal(petugasBi.get("nurse1")));
        ps.setDouble(i++, numVal(petugasBi.get("nurse2")));
        ps.setDouble(i++, numVal(petugasBi.get("nurse3")));
        // biaya lain (tidak dipakai di cathlab; simpan 0 agar kolom tetap valid)
        ps.setDouble(i++, 0d); // biayaalat
        ps.setDouble(i++, 0d); // biayasewaok
        ps.setDouble(i++, 0d); // akomodasi
        ps.setDouble(i++, 0d); // bagian_rs
        ps.setDouble(i++, 0d); // biayasarpras
        ps.setString(i++, valStr(cmbStatus));
        return i;
    }

    // =====================================================================
    // TAMPIL / LOAD
    // =====================================================================
    private void tampil() {
        Valid.tabelKosong(tabMode);
        String kata = "%" + TCari.getText().trim() + "%";
        String sql = "select oc.*, ifnull(pasien.nm_pasien,'') as nm_pasien, "+
                     "ifnull(paket_operasi.nm_perawatan,'') as nm_paket, "+
                     "ifnull(op1.nm_dokter,'') as nm_op1, "+
                     "ifnull(sc1.nama,'') as nm_scrub1, "+
                     "ifnull(cir1.nama,'') as nm_circ1, "+
                     "ifnull(rad1.nama,'') as nm_rad1 "+
                     "from operasi_cathlab oc "+
                     "left join reg_periksa on reg_periksa.no_rawat=oc.no_rawat "+
                     "left join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                     "left join paket_operasi on paket_operasi.kode_paket=oc.kode_paket "+
                     "left join dokter  op1 on op1.kd_dokter=oc.operator1 "+
                     "left join petugas sc1 on sc1.nip=oc.scrub_nurse1 "+
                     "left join petugas cir1 on cir1.nip=oc.circulate_nurse1 "+
                     "left join petugas rad1 on rad1.nip=oc.radiographer1 "+
                     "where oc.tgl_operasi between ? and ? and ("+
                     "oc.no_rawat like ? or ifnull(pasien.nm_pasien,'') like ? or "+
                     "ifnull(paket_operasi.nm_perawatan,'') like ? or oc.jenis_prosedur like ?) "+
                     "order by oc.tgl_operasi desc";
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, Valid.SetTgl(tglCari1.getSelectedItem()+"")+" 00:00:00");
            ps.setString(2, Valid.SetTgl(tglCari2.getSelectedItem()+"")+" 23:59:59");
            ps.setString(3, kata); ps.setString(4, kata); ps.setString(5, kata); ps.setString(6, kata);
            rs = ps.executeQuery();
            double totalGlobal = 0;
            while (rs.next()) {
                double totalRow = rs.getDouble("biaya_operator1")+rs.getDouble("biaya_operator2")+rs.getDouble("biaya_operator3")+
                                  rs.getDouble("biaya_asisten_operator1")+rs.getDouble("biaya_asisten_operator2")+rs.getDouble("biaya_asisten_operator3")+
                                  rs.getDouble("biaya_dokter_anestesi")+
                                  rs.getDouble("biaya_scrub_nurse1")+rs.getDouble("biaya_scrub_nurse2")+
                                  rs.getDouble("biaya_circulate_nurse1")+rs.getDouble("biaya_circulate_nurse2")+
                                  rs.getDouble("biaya_radiographer1")+
                                  rs.getDouble("biaya_monitoring1")+rs.getDouble("biaya_monitoring2")+rs.getDouble("biaya_monitoring3")+
                                  rs.getDouble("biaya_nurse1")+rs.getDouble("biaya_nurse2")+rs.getDouble("biaya_nurse3")+
                                  rs.getDouble("biayaalat")+rs.getDouble("biayasewaok")+
                                  rs.getDouble("akomodasi")+rs.getDouble("bagian_rs")+rs.getDouble("biayasarpras");
                totalGlobal += totalRow;
                tabMode.addRow(new Object[]{
                    rs.getString("no_rawat"), rs.getString("tgl_operasi"), rs.getString("kode_paket"),
                    rs.getString("nm_paket"), rs.getString("jenis_prosedur"), rs.getString("kategori"),
                    rs.getString("nm_pasien"), rs.getString("nm_op1"),
                    rs.getString("nm_scrub1"), rs.getString("nm_circ1"), rs.getString("nm_rad1"),
                    fmt.format(rs.getDouble("dosis_radiasi_mgy")),
                    fmt.format(rs.getDouble("waktu_fluoroskopi_min")),
                    fmt.format(rs.getDouble("kontras_ml")),
                    fmt.format(totalRow), rs.getString("status")
                });
            }
            LblTotal.setText("Total Semua : " + fmt.format(totalGlobal));
        } catch (Exception e) {
            System.out.println("Notif tampil : " + e);
        } finally { closeRs(); }
    }

    /** Muat detail 1 record dari tabel ke form input (untuk edit/hapus). */
    private void loadDataDariTabel() {
        int row = tbData.getSelectedRow();
        if (row < 0) return;
        loadEditData(
            tbData.getValueAt(row,0).toString(),
            tbData.getValueAt(row,1).toString(),
            tbData.getValueAt(row,2).toString()
        );
    }

    /**
     * Muat 1 record ke form input berdasarkan 3-column key
     * (no_rawat + tgl_operasi + kode_paket). Bisa dipanggil dari luar
     * (mis. dari DlgCariTagihanOperasiCathlab saat user memilih Edit).
     */
    public void loadEditData(String noRawat, String tglOp, String kodePaket) {
        // Baca dulu semua field ke Map lokal, TUTUP ResultSet, baru apply ke UI.
        // Alasan: helper seperti loadPasienDariNoRawat(), Sequel.cariIsi(),
        // dan resolveNamaPetugas() pakai shared ps/rs/koneksi — kalau
        // dipanggil di dalam rs.next() loop, ResultSet outer akan closed.
        java.util.Map<String,Object> row = new java.util.HashMap<>();
        try (Connection kon = koneksiDB.condb();
             PreparedStatement psLocal = kon.prepareStatement(
                 "select * from operasi_cathlab where no_rawat=? and tgl_operasi=? and kode_paket=?")) {
            psLocal.setString(1, noRawat);
            psLocal.setString(2, tglOp);
            psLocal.setString(3, kodePaket);
            try (ResultSet rsLocal = psLocal.executeQuery()) {
                if (!rsLocal.next()) return;
                java.sql.ResultSetMetaData md = rsLocal.getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    row.put(md.getColumnLabel(i), rsLocal.getObject(i));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif loadEditData : " + e);
            return;
        }

        TNoRw.setText(s(row.get("no_rawat")));
        loadPasienDariNoRawat();
        Valid.SetTgl(tglOperasi, s(row.get("tgl_operasi")));
        Valid.SetTgl(tglSelesai, s(row.get("jam_selesai")));
        TKdPaket.setText(s(row.get("kode_paket")));
        TNmPaket.setText(Sequel.cariIsi("select nm_perawatan from paket_operasi where kode_paket=?", s(row.get("kode_paket"))));
        cmbAnestesi.setSelectedItem(s(row.get("jenis_anasthesi")));
        cmbKategori.setSelectedItem(s(row.get("kategori")));
        cmbJenisProsedur.setSelectedItem(s(row.get("jenis_prosedur")));
        cmbStatus.setSelectedItem(s(row.get("status")));
        TDxPre.setText(s(row.get("diagnosis_pre")));
        TDxPost.setText(s(row.get("diagnosis_post")));
        TLaporan.setText(s(row.get("laporan_operasi")));
        TNoKateter.setText(s(row.get("nomor_kateter")));
        TAlatKhusus.setText(s(row.get("alat_khusus")));
        TDosis.setText(numStr(d(row.get("dosis_radiasi_mgy"))));
        TFluoro.setText(numStr(d(row.get("waktu_fluoroskopi_min"))));
        TKontras.setText(numStr(d(row.get("kontras_ml"))));
        setPetugas("op1",  s(row.get("operator1")),        d(row.get("biaya_operator1")), true);
        setPetugas("op2",  s(row.get("operator2")),        d(row.get("biaya_operator2")), true);
        setPetugas("op3",  s(row.get("operator3")),        d(row.get("biaya_operator3")), true);
        setPetugas("asop1", s(row.get("asisten_operator1")), d(row.get("biaya_asisten_operator1")), false);
        setPetugas("asop2", s(row.get("asisten_operator2")), d(row.get("biaya_asisten_operator2")), false);
        setPetugas("asop3", s(row.get("asisten_operator3")), d(row.get("biaya_asisten_operator3")), false);
        setPetugas("anes", s(row.get("dokter_anestesi")),  d(row.get("biaya_dokter_anestesi")), true);
        setPetugas("scrub1", s(row.get("scrub_nurse1")),   d(row.get("biaya_scrub_nurse1")), false);
        setPetugas("scrub2", s(row.get("scrub_nurse2")),   d(row.get("biaya_scrub_nurse2")), false);
        setPetugas("circ1",  s(row.get("circulate_nurse1")), d(row.get("biaya_circulate_nurse1")), false);
        setPetugas("circ2",  s(row.get("circulate_nurse2")), d(row.get("biaya_circulate_nurse2")), false);
        setPetugas("rad1",   s(row.get("radiographer1")),  d(row.get("biaya_radiographer1")), false);
        setPetugas("mon1",   s(row.get("monitoring1")),    d(row.get("biaya_monitoring1")), false);
        setPetugas("mon2",   s(row.get("monitoring2")),    d(row.get("biaya_monitoring2")), false);
        setPetugas("mon3",   s(row.get("monitoring3")),    d(row.get("biaya_monitoring3")), false);
        setPetugas("nurse1", s(row.get("nurse1")),         d(row.get("biaya_nurse1")), false);
        setPetugas("nurse2", s(row.get("nurse2")),         d(row.get("biaya_nurse2")), false);
        setPetugas("nurse3", s(row.get("nurse3")),         d(row.get("biaya_nurse3")), false);
        recalcTotal();
    }

    private static String s(Object o) { return o == null ? "" : o.toString(); }
    private static double d(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number)o).doubleValue();
        try { return Double.parseDouble(o.toString().trim()); } catch (Exception e) { return 0; }
    }

    private void setPetugas(String key, String kd, double biaya, boolean dokter) {
        petugasKd.get(key).setText(kd==null?"":kd);
        petugasBi.get(key).setText(numStr(biaya));
        resolveNamaPetugas(kd, petugasNm.get(key), dokter);
    }

    // =====================================================================
    // CETAK
    // =====================================================================
    private void cetak() {
        if (tbData.getSelectedRow() < 0) { warn("Pilih baris data yang mau dicetak."); return; }
        String no = tbData.getValueAt(tbData.getSelectedRow(),0).toString();
        String tg = tbData.getValueAt(tbData.getSelectedRow(),1).toString();
        cetakLaporan(no, tg);
    }

    /**
     * Cetak laporan operasi cathlab. Pola sama dengan rptLaporanOperasi
     * (standar) — pakai MyReport() + named params, bukan MyReportqry().
     * Query internal jrxml difilter dengan $P{norawat} + $P{tanggaloperasi}.
     * Info vital pasien diambil dari pemeriksaan_ranap terdekat sebelum tgl operasi.
     */
    private void cetakLaporan(String noRawat, String tglOperasi) {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("namars",     akses.getnamars());
            param.put("alamatrs",   akses.getalamatrs());
            param.put("kotars",     akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs",   akses.getkontakrs());
            param.put("emailrs",    akses.getemailrs());
            param.put("logo",       Sequel.cariGambar("select setting.logo from setting"));
            param.put("norawat",       noRawat);
            param.put("tanggaloperasi",tglOperasi);
            param.put("tindakan", "");
            param.put("finger",   "");
            // Vitals dari pemeriksaan_ranap terdekat (sama pola DlgCariTagihanOperasi.MnLaporanOperasi)
            String kosong = "";
            param.put("tgl_perawatan", null);
            param.put("jam_rawat",     kosong);
            param.put("alergi",        kosong);
            param.put("keluhan",       kosong);
            param.put("pemeriksaan",   kosong);
            param.put("penilaian",     kosong);
            param.put("rtl",           kosong);
            param.put("ruang",         kosong);
            param.put("suhu_tubuh",    kosong);
            param.put("tensi",         kosong);
            param.put("tinggi",        kosong);
            param.put("berat",         kosong);
            param.put("nadi",          kosong);
            param.put("respirasi",     kosong);
            param.put("gcs",           kosong);
            try {
                koneksi = koneksiDB.condb();
                ps = koneksi.prepareStatement(
                    "select tgl_perawatan,jam_rawat,suhu_tubuh,tensi,nadi,respirasi,tinggi,berat,gcs,"+
                    "keluhan,pemeriksaan,alergi,rtl,penilaian "+
                    "from pemeriksaan_ranap where no_rawat=? and concat(tgl_perawatan,' ',jam_rawat) <= ? "+
                    "order by tgl_perawatan desc, jam_rawat desc limit 1");
                ps.setString(1, noRawat);
                ps.setString(2, tglOperasi);
                rs = ps.executeQuery();
                if (rs.next()) {
                    param.put("tgl_perawatan", rs.getDate("tgl_perawatan"));
                    param.put("jam_rawat",     rs.getString("jam_rawat"));
                    param.put("alergi",        rs.getString("alergi"));
                    param.put("keluhan",       rs.getString("keluhan"));
                    param.put("pemeriksaan",   rs.getString("pemeriksaan"));
                    param.put("penilaian",     rs.getString("penilaian"));
                    param.put("rtl",           rs.getString("rtl"));
                    param.put("suhu_tubuh",    rs.getString("suhu_tubuh"));
                    param.put("tensi",         rs.getString("tensi"));
                    param.put("tinggi",        rs.getString("tinggi"));
                    param.put("berat",         rs.getString("berat"));
                    param.put("nadi",          rs.getString("nadi"));
                    param.put("respirasi",     rs.getString("respirasi"));
                    param.put("gcs",           rs.getString("gcs"));
                    param.put("ruang", Sequel.cariIsi(
                        "select nm_bangsal from bangsal inner join kamar inner join kamar_inap "+
                        "on bangsal.kd_bangsal=kamar.kd_bangsal and kamar_inap.kd_kamar=kamar.kd_kamar "+
                        "where no_rawat=? order by tgl_masuk desc limit 1", noRawat));
                }
            } catch (Exception ignore) {} finally { closeRs(); }

            Valid.MyReport("rptTagihanOperasiCathlab.jasper", "report",
                "::[ Laporan Operasi Cathlab ]::", param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal cetak : " + e.getMessage());
        }
    }

    // =====================================================================
    // UTILITAS
    // =====================================================================
    public void isCek() {
        BtnSimpan.setEnabled(akses.getoperasi());
        BtnHapus.setEnabled(akses.getoperasi());
        BtnEdit.setEnabled(akses.getoperasi());
        BtnPrint.setEnabled(akses.getoperasi());
    }

    /**
     * Set no_rawat + nama pasien saat dialog dibuka dari form lain
     * (mis. dari sidebar DlgBookingOperasi). Otomatis load data pasien
     * dan filter tabel berdasarkan no_rawat itu.
     */
    public void setNoRawat(String noRawat, String namaPasien) {
        if (noRawat == null) return;
        TNoRw.setText(noRawat.trim());
        if (namaPasien != null) TPasien.setText(namaPasien);
        loadPasienDariNoRawat();
        // Filter tabel data ke no_rawat ini saja
        TCari.setText(noRawat.trim());
        tampil();
    }

    /**
     * Overload lengkap: dipanggil dari sidebar DlgBookingOperasi.
     * Auto-populate:
     *   - No.Rawat + Nama Pasien
     *   - Kode Paket + Nama Paket + kategori (dari master paket_operasi)
     *   - Tarif default petugas (operator, anestesi, scrub, circulate, radiographer)
     *   - Operator 1 = dokter yang dipilih di booking (kalau ada)
     */
    public void setNoRawat(String noRawat, String namaPasien,
                           String kdPaket, String kdDokter, String nmDokter) {
        setNoRawat(noRawat, namaPasien);
        // Auto-load paket
        if (kdPaket != null && !kdPaket.trim().isEmpty()) {
            String kd = kdPaket.trim();
            String nm = Sequel.cariIsi("select nm_perawatan from paket_operasi where kode_paket=?", kd);
            if (nm != null && !nm.isEmpty()) {
                TKdPaket.setText(kd);
                TNmPaket.setText(nm);
                // Kategori paket ikut dimuat kalau match dengan combobox
                String kategori = Sequel.cariIsi("select kategori from paket_operasi where kode_paket=?", kd);
                if (kategori != null && !kategori.isEmpty()) {
                    cmbKategori.setSelectedItem(kategori);
                }
                loadTarifDariPaket(kd);
            }
        }
        // Auto-set operator 1 = dokter booking
        if (kdDokter != null && !kdDokter.trim().isEmpty() && petugasKd.get("op1") != null) {
            petugasKd.get("op1").setText(kdDokter.trim());
            if (nmDokter != null && petugasNm.get("op1") != null) {
                petugasNm.get("op1").setText(nmDokter);
            }
        }
    }

    public void emptTeks() {
        TNoRw.setText(""); TPasien.setText(""); TKdPaket.setText(""); TNmPaket.setText("");
        TDxPre.setText(""); TDxPost.setText(""); TLaporan.setText("");
        TNoKateter.setText(""); TAlatKhusus.setText("");
        TDosis.setText("0"); TFluoro.setText("0"); TKontras.setText("0");
        cmbKategori.setSelectedIndex(0); cmbJenisProsedur.setSelectedIndex(0); cmbAnestesi.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        tglOperasi.setDate(new Date()); tglSelesai.setDate(new Date());
        for (widget.TextBox t : petugasKd.values()) t.setText("");
        for (widget.TextBox t : petugasNm.values()) t.setText("");
        for (widget.TextBox t : petugasBi.values()) t.setText("0");
        recalcTotal();
    }

    private void recalcTotal() {
        double total = 0;
        for (widget.TextBox t : petugasBi.values()) total += numVal(t);
        LblTotal.setText("Total Biaya : " + fmt.format(total));
    }

    private double numVal(widget.TextBox t) {
        try { return Double.parseDouble(t.getText().trim().replaceAll("[^0-9.\\-]", "")); }
        catch (Exception e) { return 0; }
    }
    private String numStr(double d) { return String.valueOf((long) d); }
    private String textNullable(widget.TextBox t) {
        String s = t.getText().trim();
        return s.isEmpty() ? null : s;
    }
    private String valStr(widget.ComboBox c) {
        Object o = c.getSelectedItem();
        return o == null ? "" : o.toString();
    }
    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void closeRs() {
        try { if (rs != null) rs.close(); } catch (Exception ignore) {}
        try { if (ps != null) ps.close(); } catch (Exception ignore) {}
    }

    // =====================================================================
    // TEMPLATE LAPORAN OPERASI + EDITOR PERBESAR
    // Template diambil dari tabel `template_laporan_operasi` (master
    // template global — bisa dikelola lewat menu Master Template Laporan
    // Operasi). Kolom yang dipakai: diagnosa_preop, diagnosa_postop,
    // laporan_operasi. Kolom jaringan_dieksisi & permintaan_pa tidak
    // relevan untuk tindakan cathlab, jadi diabaikan.
    // =====================================================================

    /** Buka dialog cari template & isi Dx Pre/Post + Laporan bila dipilih. */
    private void cariTemplate() {
        final MasterCariTemplateLaporanOperasi tpl = new MasterCariTemplateLaporanOperasi(null, false);
        tpl.addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosing(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e) {
                javax.swing.JTable tb = tpl.getTable();
                if (tb.getSelectedRow() != -1) {
                    int r = tb.getSelectedRow();
                    String preOp = safeStr(tb.getValueAt(r, 2));
                    String postOp = safeStr(tb.getValueAt(r, 3));
                    String laporan = safeStr(tb.getValueAt(r, 6));
                    // Kalau field sudah berisi, tanya user
                    if (!TLaporan.getText().trim().isEmpty()) {
                        int ans = JOptionPane.showConfirmDialog(DlgTagihanOperasiCathlab.this,
                            "Isi laporan sudah ada.\nYa = ganti dengan template\nTidak = tambahkan template di bawah\nBatal = tidak melakukan apa-apa",
                            "Konfirmasi Template", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (ans == JOptionPane.CANCEL_OPTION || ans == JOptionPane.CLOSED_OPTION) return;
                        if (ans == JOptionPane.NO_OPTION) {
                            TLaporan.setText(TLaporan.getText() + "\n\n" + laporan);
                        } else {
                            TLaporan.setText(laporan);
                        }
                    } else {
                        TLaporan.setText(laporan);
                    }
                    // Kalau Dx Pre/Post masih kosong, isi juga dari template
                    if (TDxPre.getText().trim().isEmpty() && !preOp.isEmpty())   TDxPre.setText(preOp);
                    if (TDxPost.getText().trim().isEmpty() && !postOp.isEmpty()) TDxPost.setText(postOp);
                    TLaporan.setCaretPosition(0);
                    TLaporan.requestFocus();
                }
            }
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });
        tpl.emptTeks();
        tpl.isCek();
        tpl.setSize(900, 500);
        tpl.setLocationRelativeTo(this);
        tpl.setVisible(true);
    }

    private String safeStr(Object o) { return o == null ? "" : o.toString(); }


    private void perbesarLaporan() {
        final javax.swing.JDialog d = new javax.swing.JDialog(this, "Editor Laporan Operasi Cathlab", true);
        d.setSize(900, 600);
        d.setLocationRelativeTo(this);
        d.setLayout(new java.awt.BorderLayout(6, 6));

        // toolbar template di atas — tombol Cari Template dari master DB
        javax.swing.JPanel top = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 4));
        final javax.swing.JTextArea[] taRef = new javax.swing.JTextArea[1];
        javax.swing.JButton bTpl = new javax.swing.JButton("Cari Template");
        bTpl.setToolTipText("Cari template dari master template laporan operasi");
        bTpl.addActionListener(e -> {
            final MasterCariTemplateLaporanOperasi tpl = new MasterCariTemplateLaporanOperasi(null, false);
            tpl.addWindowListener(new WindowListener() {
                @Override public void windowOpened(WindowEvent e2) {}
                @Override public void windowClosing(WindowEvent e2) {}
                @Override public void windowClosed(WindowEvent e2) {
                    javax.swing.JTable tb = tpl.getTable();
                    if (tb.getSelectedRow() != -1) {
                        String laporan = safeStr(tb.getValueAt(tb.getSelectedRow(), 6));
                        if (taRef[0].getText().trim().isEmpty()) {
                            taRef[0].setText(laporan);
                        } else {
                            int ans = JOptionPane.showConfirmDialog(d,
                                "Isi editor sudah ada.\nYa = ganti\nTidak = tambahkan di bawah",
                                "Konfirmasi Template", JOptionPane.YES_NO_OPTION);
                            if (ans == JOptionPane.YES_OPTION) taRef[0].setText(laporan);
                            else taRef[0].setText(taRef[0].getText() + "\n\n" + laporan);
                        }
                        taRef[0].setCaretPosition(0);
                    }
                }
                @Override public void windowIconified(WindowEvent e2) {}
                @Override public void windowDeiconified(WindowEvent e2) {}
                @Override public void windowActivated(WindowEvent e2) {}
                @Override public void windowDeactivated(WindowEvent e2) {}
            });
            tpl.emptTeks();
            tpl.isCek();
            tpl.setSize(900, 500);
            tpl.setLocationRelativeTo(d);
            tpl.setVisible(true);
        });
        top.add(bTpl);
        d.add(top, java.awt.BorderLayout.NORTH);

        final javax.swing.JTextArea ta = new javax.swing.JTextArea(TLaporan.getText());
        taRef[0] = ta;
        ta.setLineWrap(true); ta.setWrapStyleWord(true);
        ta.setFont(new java.awt.Font("Consolas", 0, 13));
        ta.setMargin(new java.awt.Insets(10, 12, 10, 12));
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(ta);
        d.add(sp, java.awt.BorderLayout.CENTER);

        // tombol bawah
        javax.swing.JPanel btnP = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 6));
        javax.swing.JButton bOk     = new javax.swing.JButton("Simpan (Ctrl+S)");
        javax.swing.JButton bCancel = new javax.swing.JButton("Batal (Esc)");
        bOk.addActionListener(a -> { TLaporan.setText(ta.getText()); d.dispose(); });
        bCancel.addActionListener(a -> d.dispose());
        btnP.add(bOk); btnP.add(bCancel);
        d.add(btnP, java.awt.BorderLayout.SOUTH);

        // shortcut
        ta.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("control S"), "save");
        ta.getActionMap().put("save", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { bOk.doClick(); }
        });
        javax.swing.JRootPane rp = d.getRootPane();
        rp.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
          .put(javax.swing.KeyStroke.getKeyStroke("ESCAPE"), "esc");
        rp.getActionMap().put("esc", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { bCancel.doClick(); }
        });

        ta.requestFocusInWindow();
        d.setVisible(true);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgTagihanOperasiCathlab d = new DlgTagihanOperasiCathlab(new javax.swing.JFrame(), true);
            d.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
            });
            d.setVisible(true);
        });
    }

    // Variables
    private widget.InternalFrame internalFrame1;
    private widget.panelisi panelTop, panelToolbar;
    private widget.PanelBiasa panelInput;
    private widget.ScrollPane Scroll;
    private widget.Table tbData;
    private widget.Tanggal tglCari1, tglCari2, tglOperasi, tglSelesai;
    private widget.TextBox TCari, TNoRw, TPasien, TKdPaket, TNmPaket, TDxPre, TDxPost,
                            TNoKateter, TAlatKhusus, TDosis, TFluoro, TKontras;
    private javax.swing.JTextArea TLaporan;
    private widget.Button BtnTemplate, BtnPerbesarLaporan;
    private widget.ComboBox cmbKategori, cmbJenisProsedur, cmbAnestesi, cmbStatus;
    private widget.Button BtnSimpan, BtnEdit, BtnHapus, BtnBaru, BtnPrint, BtnKeluar,
                          BtnCari, BtnAll, BtnCariRawat, BtnCariPaket;
    private widget.Label LblTotal;

    // Petugas registry — key: op1/op2/op3/anes/scrub1/scrub2/circ1/circ2/rad1
    private final java.util.Map<String,widget.TextBox> petugasKd = new java.util.LinkedHashMap<>();
    private final java.util.Map<String,widget.TextBox> petugasNm = new java.util.LinkedHashMap<>();
    private final java.util.Map<String,widget.TextBox> petugasBi = new java.util.LinkedHashMap<>();
}
