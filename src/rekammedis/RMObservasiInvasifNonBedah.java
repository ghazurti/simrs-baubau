package rekammedis;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariPetugas;

/**
 * Observasi Tanda Vital Intra & Paska Tindakan Invasif Non Bedah
 * RSUD Kota Baubau
 */
public final class RMObservasiInvasifNonBedah extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private DlgCariPetugas petugas;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;

    // GUI Components
    private widget.InternalFrame internalFrame1;
    private JPanel PanelInput;
    private widget.ScrollPane scrollInput;
    private widget.PanelBiasa FormInput;
    private widget.Label jLabel4, jLabel16, jLabel18, jLabel8, jLabelJam;
    private widget.Label jLabelBP, jLabelNadi, jLabelResp, jLabelSuhu, jLabelSpO2, jLabelPulsasi, jLabelReflek;
    private widget.TextBox TNoRw, TNoRM, TPasien, TglLahir;
    private widget.Tanggal Tanggal;
    private widget.ComboBox Jam, Menit, Detik;
    private widget.CekBox ChkKejadian;
    private widget.TextBox KdPetugas, NmPetugas;
    private widget.Button BtnPetugas;
    private widget.TextBox BPSistolik, BPDiastolik, Nadi, Pernapasan, Suhu, SpO2;
    private widget.ComboBox PulsasiDistal, ReflekMenelan;
    private widget.ScrollPane Scroll;
    private widget.Table tbObat;
    private JPanel jPanel3;
    private widget.panelisi panelGlass8, panelGlass9;
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnKeluar, BtnCari, BtnAll;
    private widget.Label jLabel7, LCount, jLabel19, jLabel21;
    private widget.Tanggal DTPCari1, DTPCari2;
    private widget.TextBox TCari;
    private widget.CekBox ChkInput;
    private String selectedId = "";

    public RMObservasiInvasifNonBedah(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        tabMode = new DefaultTableModel(null, new Object[]{
            "No", "ID", "No.Rawat", "No.R.M.", "Nama Pasien", "Tanggal", "Jam", "TD (mmHg)", "Sistolik", "Diastolik",
            "Nadi (x/m)", "RR (x/m)", "Suhu (°C)", "SpO2 (%)", "Pulsasi Distal", "Reflek Menelan", "NIP", "Petugas"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {35, 40, 105, 65, 160, 75, 55, 75, 0, 0, 65, 65, 60, 60, 95, 95, 80, 150};
        for (int i = 0; i < widths.length; i++) {
            TableColumn col = tbObat.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
            if (widths[i] == 0) {
                col.setMinWidth(0);
                col.setMaxWidth(0);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte) 17).getKata(TNoRw));
        KdPetugas.setDocument(new batasInput((byte) 20).getKata(KdPetugas));
        BPSistolik.setDocument(new batasInput((byte) 5).getKata(BPSistolik));
        BPDiastolik.setDocument(new batasInput((byte) 5).getKata(BPDiastolik));
        Nadi.setDocument(new batasInput((byte) 5).getKata(Nadi));
        Pernapasan.setDocument(new batasInput((byte) 5).getKata(Pernapasan));
        Suhu.setDocument(new batasInput((byte) 5).getKata(Suhu));
        SpO2.setDocument(new batasInput((byte) 5).getKata(SpO2));
        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));

        petugas = new DlgCariPetugas(null, false);
        petugas.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (petugas.getTable().getSelectedRow() != -1) {
                    KdPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                    NmPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                }
                KdPetugas.requestFocus();
            }
        });

        ChkInput.setSelected(false);
        isForm();
        jam();
    }

    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        PanelInput = new JPanel(new BorderLayout());
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new JPanel(new BorderLayout());
        panelGlass8 = new widget.panelisi();
        panelGlass9 = new widget.panelisi();

        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();

        jLabel16 = new widget.Label();
        Tanggal = new widget.Tanggal();
        jLabelJam = new widget.Label();
        Jam = new widget.ComboBox();
        Menit = new widget.ComboBox();
        Detik = new widget.ComboBox();
        ChkKejadian = new widget.CekBox();

        jLabel18 = new widget.Label();
        KdPetugas = new widget.TextBox();
        NmPetugas = new widget.TextBox();
        BtnPetugas = new widget.Button();

        jLabelBP = new widget.Label();
        BPSistolik = new widget.TextBox();
        BPDiastolik = new widget.TextBox();
        jLabelNadi = new widget.Label();
        Nadi = new widget.TextBox();
        jLabelResp = new widget.Label();
        Pernapasan = new widget.TextBox();
        jLabelSuhu = new widget.Label();
        Suhu = new widget.TextBox();
        jLabelSpO2 = new widget.Label();
        SpO2 = new widget.TextBox();

        jLabelPulsasi = new widget.Label();
        PulsasiDistal = new widget.ComboBox();
        jLabelReflek = new widget.Label();
        ReflekMenelan = new widget.ComboBox();

        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnKeluar = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();

        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        ChkInput = new widget.CekBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(240, 245, 235)),
            "::[ Observasi Tanda Vital Intra & Paska Tindakan Invasif Non Bedah ]::",
            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
            new Font("Tahoma", 0, 11), new Color(50, 50, 50)
        ));
        internalFrame1.setLayout(new BorderLayout(1, 1));

        // FormInput setup
        FormInput.setBackground(new Color(250, 255, 245));
        FormInput.setPreferredSize(new Dimension(900, 140));
        FormInput.setLayout(null);

        // Row 1: Pasien Info
        jLabel4.setText("No.Rawat :");
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 75, 23);

        TNoRw.setEditable(false);
        FormInput.add(TNoRw);
        TNoRw.setBounds(79, 10, 130, 23);

        TNoRM.setEditable(false);
        FormInput.add(TNoRM);
        TNoRM.setBounds(212, 10, 90, 23);

        TPasien.setEditable(false);
        FormInput.add(TPasien);
        TPasien.setBounds(305, 10, 250, 23);

        jLabel8.setText("Tgl.Lahir :");
        FormInput.add(jLabel8);
        jLabel8.setBounds(565, 10, 60, 23);

        TglLahir.setEditable(false);
        FormInput.add(TglLahir);
        TglLahir.setBounds(630, 10, 90, 23);

        // Row 2: Tanggal, Jam, Petugas
        jLabel16.setText("Tanggal :");
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 40, 75, 23);

        Tanggal.setModel(new DefaultComboBoxModel(new String[]{"30-09-2026"}));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        FormInput.add(Tanggal);
        Tanggal.setBounds(79, 40, 90, 23);

        jLabelJam.setText("Jam :");
        FormInput.add(jLabelJam);
        jLabelJam.setBounds(172, 40, 40, 23);

        Jam.setModel(new DefaultComboBoxModel(new String[]{"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23"}));
        Jam.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                ChkKejadian.setSelected(false);
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });
        FormInput.add(Jam);
        Jam.setBounds(214, 40, 66, 23);

        Menit.setModel(new DefaultComboBoxModel(new String[]{"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59"}));
        Menit.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                ChkKejadian.setSelected(false);
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });
        FormInput.add(Menit);
        Menit.setBounds(283, 40, 66, 23);

        Detik.setModel(new DefaultComboBoxModel(new String[]{"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59"}));
        Detik.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                ChkKejadian.setSelected(false);
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });
        FormInput.add(Detik);
        Detik.setBounds(352, 40, 66, 23);

        ChkKejadian.setBorder(null);
        ChkKejadian.setSelected(true);
        ChkKejadian.setToolTipText("Centang untuk jam otomatis saat ini");
        ChkKejadian.setHorizontalAlignment(SwingConstants.CENTER);
        ChkKejadian.setHorizontalTextPosition(SwingConstants.CENTER);
        FormInput.add(ChkKejadian);
        ChkKejadian.setBounds(421, 40, 23, 23);

        jLabel18.setText("Petugas :");
        FormInput.add(jLabel18);
        jLabel18.setBounds(448, 40, 60, 23);

        KdPetugas.setEditable(false);
        FormInput.add(KdPetugas);
        KdPetugas.setBounds(511, 40, 85, 23);

        NmPetugas.setEditable(false);
        FormInput.add(NmPetugas);
        NmPetugas.setBounds(599, 40, 150, 23);

        BtnPetugas.setIcon(new ImageIcon(getClass().getResource("/picture/190.png")));
        BtnPetugas.setToolTipText("Cari Petugas");
        BtnPetugas.addActionListener(e -> {
            petugas.emptTeks();
            petugas.isCek();
            petugas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            petugas.setLocationRelativeTo(internalFrame1);
            petugas.setVisible(true);
        });
        FormInput.add(BtnPetugas);
        BtnPetugas.setBounds(752, 40, 28, 23);

        // Row 3: TD, Nadi, RR, Suhu, SpO2
        jLabelBP.setText("TD (Sis/Dia) :");
        FormInput.add(jLabelBP);
        jLabelBP.setBounds(0, 70, 75, 23);

        BPSistolik.setToolTipText("Sistolik (mmHg)");
        FormInput.add(BPSistolik);
        BPSistolik.setBounds(79, 70, 50, 23);

        BPDiastolik.setToolTipText("Diastolik (mmHg)");
        FormInput.add(BPDiastolik);
        BPDiastolik.setBounds(132, 70, 50, 23);

        jLabelNadi.setText("HR (x/m) :");
        FormInput.add(jLabelNadi);
        jLabelNadi.setBounds(185, 70, 60, 23);

        Nadi.setToolTipText("Denyut Nadi (x/menit)");
        FormInput.add(Nadi);
        Nadi.setBounds(248, 70, 55, 23);

        jLabelResp.setText("RR (x/m) :");
        FormInput.add(jLabelResp);
        jLabelResp.setBounds(308, 70, 60, 23);

        Pernapasan.setToolTipText("Pernapasan (x/menit)");
        FormInput.add(Pernapasan);
        Pernapasan.setBounds(371, 70, 55, 23);

        jLabelSuhu.setText("Suhu (°C) :");
        FormInput.add(jLabelSuhu);
        jLabelSuhu.setBounds(431, 70, 65, 23);

        Suhu.setToolTipText("Suhu (°C)");
        FormInput.add(Suhu);
        Suhu.setBounds(499, 70, 55, 23);

        jLabelSpO2.setText("SpO2 (%) :");
        FormInput.add(jLabelSpO2);
        jLabelSpO2.setBounds(559, 70, 65, 23);

        SpO2.setToolTipText("Saturasi O2 (%)");
        FormInput.add(SpO2);
        SpO2.setBounds(627, 70, 55, 23);

        // Row 4: Pulsasi Distal, Reflek Menelan
        jLabelPulsasi.setText("Pulsasi Distal :");
        FormInput.add(jLabelPulsasi);
        jLabelPulsasi.setBounds(0, 100, 75, 23);

        PulsasiDistal.setModel(new DefaultComboBoxModel(new String[]{"Adekuat", "Tidak Adekuat", "Kuat", "Lemah", "Tidak Teraba"}));
        FormInput.add(PulsasiDistal);
        PulsasiDistal.setBounds(79, 100, 120, 23);

        jLabelReflek.setText("Reflek Menelan :");
        FormInput.add(jLabelReflek);
        jLabelReflek.setBounds(210, 100, 95, 23);

        ReflekMenelan.setModel(new DefaultComboBoxModel(new String[]{"Ada", "Tidak Ada", "Baik", "Menurun"}));
        FormInput.add(ReflekMenelan);
        ReflekMenelan.setBounds(310, 100, 100, 23);

        scrollInput.setViewportView(FormInput);
        PanelInput.add(scrollInput, BorderLayout.CENTER);

        ChkInput.setIcon(new ImageIcon(getClass().getResource("/picture/143.png")));
        ChkInput.setRolloverIcon(new ImageIcon(getClass().getResource("/picture/143.png")));
        ChkInput.setSelectedIcon(new ImageIcon(getClass().getResource("/picture/145.png")));
        ChkInput.setRolloverSelectedIcon(new ImageIcon(getClass().getResource("/picture/145.png")));
        ChkInput.setText(".: Input Data Observasi :.");
        ChkInput.setHorizontalAlignment(SwingConstants.LEFT);
        ChkInput.addActionListener(e -> isForm());
        PanelInput.add(ChkInput, BorderLayout.PAGE_END);

        internalFrame1.add(PanelInput, BorderLayout.PAGE_START);

        // Table
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (tabMode.getRowCount() != 0) {
                    getData();
                }
            }
        });
        Scroll.setViewportView(tbObat);
        internalFrame1.add(Scroll, BorderLayout.CENTER);

        // Bottom panel
        jPanel3.setLayout(new BorderLayout(1, 1));

        panelGlass8.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 6));

        BtnSimpan.setIcon(new ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setText("Simpan");
        BtnSimpan.setPreferredSize(new Dimension(95, 30));
        BtnSimpan.addActionListener(e -> simpan());
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png")));
        BtnBatal.setText("Baru");
        BtnBatal.setPreferredSize(new Dimension(85, 30));
        BtnBatal.addActionListener(e -> emptTeks());
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setText("Hapus");
        BtnHapus.setPreferredSize(new Dimension(85, 30));
        BtnHapus.addActionListener(e -> hapus());
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new ImageIcon(getClass().getResource("/picture/inventaris.png")));
        BtnEdit.setText("Ganti");
        BtnEdit.setPreferredSize(new Dimension(85, 30));
        BtnEdit.addActionListener(e -> ganti());
        panelGlass8.add(BtnEdit);

        jLabel7.setText("Record :");
        jLabel7.setPreferredSize(new Dimension(55, 23));
        panelGlass8.add(jLabel7);

        LCount.setText("0");
        LCount.setPreferredSize(new Dimension(45, 23));
        panelGlass8.add(LCount);

        BtnKeluar.setIcon(new ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setText("Keluar");
        BtnKeluar.setPreferredSize(new Dimension(85, 30));
        BtnKeluar.addActionListener(e -> dispose());
        panelGlass8.add(BtnKeluar);

        jPanel3.add(panelGlass8, BorderLayout.PAGE_START);

        panelGlass9.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 6));

        jLabel19.setText("Tanggal :");
        jLabel19.setPreferredSize(new Dimension(55, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setPreferredSize(new Dimension(90, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setText("s.d.");
        panelGlass9.add(jLabel21);

        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setPreferredSize(new Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        TCari.setPreferredSize(new Dimension(170, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                }
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setText("Cari");
        BtnCari.setPreferredSize(new Dimension(75, 23));
        BtnCari.addActionListener(e -> tampil());
        panelGlass9.add(BtnCari);

        BtnAll.setIcon(new ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setText("Semua");
        BtnAll.setPreferredSize(new Dimension(85, 23));
        BtnAll.addActionListener(e -> {
            TCari.setText("");
            tampil();
        });
        panelGlass9.add(BtnAll);

        jPanel3.add(panelGlass9, BorderLayout.PAGE_END);

        internalFrame1.add(jPanel3, BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, BorderLayout.CENTER);
        pack();
    }

    private void isForm() {
        if (ChkInput.isSelected()) {
            PanelInput.setPreferredSize(new Dimension(WIDTH, 160));
            FormInput.setVisible(true);
        } else {
            PanelInput.setPreferredSize(new Dimension(WIDTH, 20));
            FormInput.setVisible(false);
        }
    }

    public void emptTeks() {
        selectedId = "";
        BPSistolik.setText("");
        BPDiastolik.setText("");
        Nadi.setText("");
        Pernapasan.setText("");
        Suhu.setText("");
        SpO2.setText("");
        PulsasiDistal.setSelectedIndex(0);
        ReflekMenelan.setSelectedIndex(0);
        ChkKejadian.setSelected(true);
        BPSistolik.requestFocus();
    }

    private void getData() {
        if (tbObat.getSelectedRow() != -1) {
            ChkKejadian.setSelected(false);
            selectedId = tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString();
            Valid.SetTgl(Tanggal, tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
            String jamVal = tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString();
            String[] splitJam = jamVal.split(":");
            if (splitJam.length >= 2) {
                Jam.setSelectedItem(splitJam[0]);
                Menit.setSelectedItem(splitJam[1]);
            }
            BPSistolik.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString());
            BPDiastolik.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 9).toString());
            Nadi.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 10).toString());
            Pernapasan.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 11).toString());
            Suhu.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 12).toString());
            SpO2.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 13).toString());
            PulsasiDistal.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 14).toString());
            ReflekMenelan.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 15).toString());
            KdPetugas.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 16).toString());
            NmPetugas.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 17).toString());
        }
    }

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            String sql = "select o.id, o.no_rawat, p.no_rkm_medis, p.nm_pasien, o.tanggal, o.jam_obs, " +
                         "o.bp_sistolik, o.bp_diastolik, o.nadi, o.pernapasan, o.suhu, o.saturasi_o2, " +
                         "o.pulsasi_distal, o.reflek_menelan, o.nip, ifnull(pt.nama,'') as nama_petugas " +
                         "from asesmen_invasif_observasi o " +
                         "inner join reg_periksa rp on o.no_rawat = rp.no_rawat " +
                         "inner join pasien p on rp.no_rkm_medis = p.no_rkm_medis " +
                         "left join petugas pt on o.nip = pt.nip " +
                         "where o.no_rawat = ? and o.tanggal between ? and ? " +
                         "order by o.tanggal, o.jam_obs";
            ps = koneksi.prepareStatement(sql);
            try {
                ps.setString(1, TNoRw.getText().trim());
                ps.setString(2, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
                ps.setString(3, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
                rs = ps.executeQuery();
                int no = 1;
                while (rs.next()) {
                    String td = "";
                    String sis = rs.getString("bp_sistolik");
                    String dia = rs.getString("bp_diastolik");
                    if (sis != null && !sis.isEmpty() && dia != null && !dia.isEmpty()) {
                        td = sis + "/" + dia;
                    } else if (sis != null && !sis.isEmpty()) {
                        td = sis;
                    }

                    tabMode.addRow(new String[]{
                        String.valueOf(no++),
                        rs.getString("id"),
                        rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"),
                        rs.getString("tanggal"),
                        rs.getString("jam_obs"),
                        td,
                        sis,
                        dia,
                        rs.getString("nadi"),
                        rs.getString("pernapasan"),
                        rs.getString("suhu"),
                        rs.getString("saturasi_o2"),
                        rs.getString("pulsasi_distal"),
                        rs.getString("reflek_menelan"),
                        rs.getString("nip"),
                        rs.getString("nama_petugas")
                    });
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Observasi tampil() : " + e);
        }
        LCount.setText(String.valueOf(tabMode.getRowCount()));
    }

    private void simpan() {
        if (TNoRw.getText().trim().isEmpty()) {
            Valid.textKosong(TNoRw, "No. Rawat");
        } else if (KdPetugas.getText().trim().isEmpty()) {
            Valid.textKosong(KdPetugas, "Petugas");
        } else {
            String jamVal = Jam.getSelectedItem() + ":" + Menit.getSelectedItem();
            if (Sequel.menyimpantf("asesmen_invasif_observasi", "?,?,?,?,?,?,?,?,?,?,?,?,?", "Data Observasi", 13, new String[]{
                null,
                TNoRw.getText(),
                Valid.SetTgl(Tanggal.getSelectedItem() + ""),
                jamVal,
                BPSistolik.getText(),
                BPDiastolik.getText(),
                Nadi.getText(),
                Pernapasan.getText(),
                Suhu.getText(),
                SpO2.getText(),
                PulsasiDistal.getSelectedItem().toString(),
                ReflekMenelan.getSelectedItem().toString(),
                KdPetugas.getText()
            })) {
                tampil();
                emptTeks();
            }
        }
    }

    private void ganti() {
        if (selectedId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Silahkan pilih data pada tabel yang akan diganti!");
        } else if (KdPetugas.getText().trim().isEmpty()) {
            Valid.textKosong(KdPetugas, "Petugas");
        } else {
            String jamVal = Jam.getSelectedItem() + ":" + Menit.getSelectedItem();
            if (Sequel.mengedittf("asesmen_invasif_observasi", "id=?", "tanggal=?,jam_obs=?,bp_sistolik=?,bp_diastolik=?,nadi=?,pernapasan=?,suhu=?,saturasi_o2=?,pulsasi_distal=?,reflek_menelan=?,nip=?", 12, new String[]{
                Valid.SetTgl(Tanggal.getSelectedItem() + ""),
                jamVal,
                BPSistolik.getText(),
                BPDiastolik.getText(),
                Nadi.getText(),
                Pernapasan.getText(),
                Suhu.getText(),
                SpO2.getText(),
                PulsasiDistal.getSelectedItem().toString(),
                ReflekMenelan.getSelectedItem().toString(),
                KdPetugas.getText(),
                selectedId
            })) {
                tampil();
                emptTeks();
            }
        }
    }

    private void hapus() {
        if (selectedId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Silahkan pilih data pada tabel yang akan dihapus!");
        } else {
            if (Sequel.queryu2tf("delete from asesmen_invasif_observasi where id=?", 1, new String[]{selectedId})) {
                tampil();
                emptTeks();
            } else {
                JOptionPane.showMessageDialog(null, "Gagal menghapus data!");
            }
        }
    }

    public void setNoRm(String norwt, Date tgl) {
        TNoRw.setText(norwt);
        DTPCari1.setDate(tgl);
        DTPCari2.setDate(tgl);
        Tanggal.setDate(tgl);
        try {
            ps = koneksi.prepareStatement(
                "select rp.no_rkm_medis, p.nm_pasien, p.tgl_lahir " +
                "from reg_periksa rp inner join pasien p on rp.no_rkm_medis = p.no_rkm_medis " +
                "where rp.no_rawat = ?"
            );
            try {
                ps.setString(1, norwt);
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif setNoRm : " + e);
        }

        ChkInput.setSelected(true);
        isForm();
        tampil();
    }

    public void isCek() {
        BtnSimpan.setEnabled(true);
        BtnHapus.setEnabled(true);
        BtnEdit.setEnabled(true);
        if (akses.getjml2() >= 1) {
            KdPetugas.setEditable(false);
            BtnPetugas.setEnabled(false);
            KdPetugas.setText(akses.getkode());
            NmPetugas.setText(Sequel.CariPetugas(KdPetugas.getText()));
            if (NmPetugas.getText().isEmpty()) {
                KdPetugas.setText("");
                JOptionPane.showMessageDialog(null, "User login bukan petugas!");
            }
        }
    }

    private void jam() {
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Date now = Calendar.getInstance().getTime();
                if (ChkKejadian.isSelected()) {
                    int h = now.getHours();
                    int m = now.getMinutes();
                    int s = now.getSeconds();
                    Jam.setSelectedItem(h < 10 ? "0" + h : String.valueOf(h));
                    Menit.setSelectedItem(m < 10 ? "0" + m : String.valueOf(m));
                    Detik.setSelectedItem(s < 10 ? "0" + s : String.valueOf(s));
                }
            }
        };
        new Timer(1000, taskPerformer).start();
    }
}
