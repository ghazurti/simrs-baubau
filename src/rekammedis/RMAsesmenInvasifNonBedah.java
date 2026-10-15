/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariPetugas;


/**
 *
 * @author perpustakaan
 */
public final class RMAsesmenInvasifNonBedah extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;    
    private DlgCariPetugas petugas;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private String finger="";
    private StringBuilder htmlContent;
    private String TANGGALMUNDUR="yes";
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public RMAsesmenInvasifNonBedah(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        // Instansiasi field baru SEBELUM initComponents() agar tidak NullPointerException
        JamTiba = new widget.TextBox();
        LokasiPungsi = new widget.TextBox();
        JumlahPungsi = new widget.TextBox();
        LokasiSheath = new widget.TextBox();
        PulsasiRadialisKanan = new widget.TextBox();
        PulsasiRadialisKiri = new widget.TextBox();
        PulsasiDorsalisKanan = new widget.TextBox();
        PulsasiDorsalisKiri = new widget.TextBox();
        AlatTerpasang = new widget.TextBox();
        JenisAnestesi = new widget.TextBox();
        Sedasi = new widget.TextBox();
        DosisPre = new widget.TextBox();
        DosisIntra = new widget.TextBox();
        DosisPost = new widget.TextBox();
        Komplikasi = new widget.TextBox();
        Intake = new widget.TextBox();
        Output = new widget.TextBox();
        InstruksiPaska = new widget.TextBox();
        MasalahKeperawatan = new widget.TextBox();
        RencanaKeperawatan = new widget.TextBox();
        TerpasangSheath = new widget.ComboBox();
        TerpasangSheath.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        KeputusanRawat = new widget.ComboBox();
        KeputusanRawat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Rawat Inap", "Pulang", "Rujuk", "Meninggal" }));
        initComponents();

        this.setLocation(8,1);
        setSize(628,674);

        
        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat","No.R.M.","Nama Pasien","Tgl.Lahir","Umur","NIP","Petugas","Tanggal","Jam Tiba","Lokasi Pungsi","Jml Pungsi","Tpsg Sheath",
            "Lokasi Sheath","Radialis Kanan","Radialis Kiri","Dorsalis Kanan","Dorsalis Kiri","Alat","Anestesi","Sedasi","Dosis Pre","Dosis Intra",
            "Dosis Post","Komplikasi","Intake","Output","Instruksi Paska","Masalah Kep","Rencana Kep","Keputusan Rawat"
        }){

              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 26; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(150);
            }else if(i==3){
                column.setPreferredWidth(65);
            }else if(i==4){
                column.setPreferredWidth(40);
            }else if(i==5){
                column.setPreferredWidth(90);
            }else if(i==6){
                column.setPreferredWidth(150);
            }else if(i==7){
                column.setPreferredWidth(115);
            }else if(i==8){
                column.setPreferredWidth(118);
            }else if(i==9){
                column.setPreferredWidth(45);
            }else if(i==10){
                column.setPreferredWidth(118);
            }else if(i==11){
                column.setPreferredWidth(45);
            }else if(i==12){
                column.setPreferredWidth(118);
            }else if(i==13){
                column.setPreferredWidth(45);
            }else if(i==14){
                column.setPreferredWidth(118);
            }else if(i==15){
                column.setPreferredWidth(45);
            }else if(i==16){
                column.setPreferredWidth(118);
            }else if(i==17){
                column.setPreferredWidth(45);
            }else if(i==18){
                column.setPreferredWidth(118);
            }else if(i==19){
                column.setPreferredWidth(45);
            }else if(i==20){
                column.setPreferredWidth(118);
            }else if(i==21){
                column.setPreferredWidth(45);
            }else if(i==22){
                column.setPreferredWidth(118);
            }else if(i==23){
                column.setPreferredWidth(45);
            }else if(i==24){
                column.setPreferredWidth(60);
            }else if(i==25){
                column.setPreferredWidth(230);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        KdPetugas.setDocument(new batasInput((byte)20).getKata(KdPetugas));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
        
        ChkInput.setSelected(false);
        isForm();
        
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
        );
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);
        
        try {
            TANGGALMUNDUR=koneksiDB.TANGGALMUNDUR();
        } catch (Exception e) {
            TANGGALMUNDUR="yes";
        }
        
        jam();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnSkriningKekerasanPadaPerempuan = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();
        LoadHTML = new widget.editorpane();
        Umur = new widget.TextBox();
        TanggalRegistrasi = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnObservasi = new widget.Button();
        MnObservasi = new javax.swing.JMenuItem();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        Tanggal = new widget.Tanggal();
        TNoRM = new widget.TextBox();
        jLabel16 = new widget.Label();
        Jam = new widget.ComboBox();
        Menit = new widget.ComboBox();
        Detik = new widget.ComboBox();
        ChkKejadian = new widget.CekBox();
        jLabel18 = new widget.Label();
        KdPetugas = new widget.TextBox();
        NmPetugas = new widget.TextBox();
        BtnPetugas = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        PertanyaanAwal1 = new widget.ComboBox();
        jLabel92 = new widget.Label();
        PertanyaanAwal2 = new widget.ComboBox();
        jLabel69 = new widget.Label();
        jLabel73 = new widget.Label();
        TotalHasil = new widget.TextBox();
        NilaiPertanyaanAwal1 = new widget.TextBox();
        NilaiPertanyaanAwal2 = new widget.TextBox();
        PertanyaanLanjutan1 = new widget.ComboBox();
        jLabel70 = new widget.Label();
        NilaiPertanyaanLanjutan1 = new widget.TextBox();
        jLabel71 = new widget.Label();
        NilaiPertanyaanLanjutan2 = new widget.TextBox();
        PertanyaanLanjutan2 = new widget.ComboBox();
        jLabel148 = new widget.Label();
        HasilSkrining = new widget.TextBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel99 = new widget.Label();
        jLabel75 = new widget.Label();
        jLabel76 = new widget.Label();
        jLabel77 = new widget.Label();
        jLabel78 = new widget.Label();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel100 = new widget.Label();
        jLabel79 = new widget.Label();
        jLabel80 = new widget.Label();
        jLabel81 = new widget.Label();
        jLabel82 = new widget.Label();
        jLabel83 = new widget.Label();
        jLabel84 = new widget.Label();
        PertanyaanLanjutan3 = new widget.ComboBox();
        jLabel72 = new widget.Label();
        NilaiPertanyaanLanjutan3 = new widget.TextBox();
        jLabel85 = new widget.Label();
        jLabel86 = new widget.Label();
        PertanyaanLanjutan4 = new widget.ComboBox();
        jLabel74 = new widget.Label();
        NilaiPertanyaanLanjutan4 = new widget.TextBox();
        jLabel87 = new widget.Label();
        jLabel88 = new widget.Label();
        PertanyaanLanjutan5 = new widget.ComboBox();
        jLabel89 = new widget.Label();
        NilaiPertanyaanLanjutan5 = new widget.TextBox();
        jLabel90 = new widget.Label();
        PertanyaanLanjutan6 = new widget.ComboBox();
        jLabel91 = new widget.Label();
        NilaiPertanyaanLanjutan6 = new widget.TextBox();
        jLabel93 = new widget.Label();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel101 = new widget.Label();
        jLabel149 = new widget.Label();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnSkriningKekerasanPadaPerempuan.setBackground(new java.awt.Color(255, 255, 254));
        MnSkriningKekerasanPadaPerempuan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnSkriningKekerasanPadaPerempuan.setForeground(new java.awt.Color(50, 50, 50));
        MnSkriningKekerasanPadaPerempuan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnSkriningKekerasanPadaPerempuan.setText("Formulir Asesmen Invasif Non Bedah");
        MnSkriningKekerasanPadaPerempuan.setName("MnSkriningKekerasanPadaPerempuan"); // NOI18N
        MnSkriningKekerasanPadaPerempuan.setPreferredSize(new java.awt.Dimension(280, 26));
        MnSkriningKekerasanPadaPerempuan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnSkriningKekerasanPadaPerempuanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnSkriningKekerasanPadaPerempuan);

        MnObservasi.setBackground(new java.awt.Color(255, 255, 254));
        MnObservasi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnObservasi.setForeground(new java.awt.Color(50, 50, 50));
        MnObservasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnObservasi.setText("Observasi Tanda Vital Intra & Paska Tindakan");
        MnObservasi.setName("MnObservasi"); // NOI18N
        MnObservasi.setPreferredSize(new java.awt.Dimension(280, 26));
        MnObservasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnObservasiAction();
            }
        });
        jPopupMenu1.add(MnObservasi);

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        Umur.setEditable(false);
        Umur.setFocusTraversalPolicyProvider(true);
        Umur.setName("Umur"); // NOI18N

        TanggalRegistrasi.setHighlighter(null);
        TanggalRegistrasi.setName("TanggalRegistrasi"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Asesmen Invasif Non Bedah ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        BtnSimpan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal"); // NOI18N
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalActionPerformed(evt);
            }
        });
        BtnBatal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBatalKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        BtnHapus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapusKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setName("BtnEdit"); // NOI18N
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        BtnEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnEditKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnEdit);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnPrint);

        BtnObservasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnObservasi.setMnemonic('O');
        BtnObservasi.setText("Observasi");
        BtnObservasi.setToolTipText("Alt+O");
        BtnObservasi.setName("BtnObservasi"); // NOI18N
        BtnObservasi.setPreferredSize(new java.awt.Dimension(110, 30));
        BtnObservasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnObservasiAction();
            }
        });
        panelGlass8.add(BtnObservasi);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass8.add(LCount);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnKeluar);

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tanggal :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "07-02-2026" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "07-02-2026" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(310, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Alt+3");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnAll);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 426));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('I');
        ChkInput.setText(".: Input Data");
        ChkInput.setToolTipText("Alt+I");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 557));

        FormInput.setBackground(new java.awt.Color(250, 255, 245));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(920, 430));
        FormInput.setLayout(null);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 75, 23);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(79, 10, 141, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPasienKeyPressed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(336, 10, 285, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "07-02-2026" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(79, 40, 90, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(222, 10, 112, 23);

        jLabel16.setText("Tanggal :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 40, 75, 23);

        Jam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        Jam.setName("Jam"); // NOI18N
        Jam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JamKeyPressed(evt);
            }
        });
        FormInput.add(Jam);
        Jam.setBounds(173, 40, 62, 23);

        Menit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Menit.setName("Menit"); // NOI18N
        Menit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenitKeyPressed(evt);
            }
        });
        FormInput.add(Menit);
        Menit.setBounds(238, 40, 62, 23);

        Detik.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Detik.setName("Detik"); // NOI18N
        Detik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DetikKeyPressed(evt);
            }
        });
        FormInput.add(Detik);
        Detik.setBounds(303, 40, 62, 23);

        ChkKejadian.setBorder(null);
        ChkKejadian.setSelected(true);
        ChkKejadian.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ChkKejadian.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setName("ChkKejadian"); // NOI18N
        FormInput.add(ChkKejadian);
        ChkKejadian.setBounds(368, 40, 23, 23);

        jLabel18.setText("Petugas :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(400, 40, 70, 23);

        KdPetugas.setEditable(false);
        KdPetugas.setHighlighter(null);
        KdPetugas.setName("KdPetugas"); // NOI18N
        FormInput.add(KdPetugas);
        KdPetugas.setBounds(474, 40, 94, 23);

        NmPetugas.setEditable(false);
        NmPetugas.setName("NmPetugas"); // NOI18N
        FormInput.add(NmPetugas);
        NmPetugas.setBounds(570, 40, 187, 23);

        BtnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPetugas.setMnemonic('2');
        BtnPetugas.setToolTipText("ALt+2");
        BtnPetugas.setName("BtnPetugas"); // NOI18N
        BtnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPetugasActionPerformed(evt);
            }
        });
        BtnPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPetugasKeyPressed(evt);
            }
        });
        FormInput.add(BtnPetugas);
        BtnPetugas.setBounds(761, 40, 28, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(625, 10, 60, 23);

        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(689, 10, 100, 23);

        // =========================================================
        // SECTION I: DATA TINDAKAN
        // =========================================================
        jSeparator1.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator1.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(0, 70, 807, 1);

        jLabel99.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel99.setText("I. DATA TINDAKAN");
        jLabel99.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel99.setName("jLabel99"); // NOI18N
        FormInput.add(jLabel99);
        jLabel99.setBounds(10, 70, 200, 23);

        // Jam Tiba RR
        jLabel75.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel75.setText("Jam Tiba RR :");
        jLabel75.setName("jLabel75"); // NOI18N
        FormInput.add(jLabel75);
        jLabel75.setBounds(10, 95, 100, 23);

        JamTiba.setName("JamTiba"); // NOI18N
        FormInput.add(JamTiba);
        JamTiba.setBounds(115, 95, 80, 23);

        // Lokasi Pungsi
        jLabel76.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel76.setText("Lokasi Pungsi :");
        jLabel76.setName("jLabel76"); // NOI18N
        FormInput.add(jLabel76);
        jLabel76.setBounds(210, 95, 100, 23);

        LokasiPungsi.setName("LokasiPungsi"); // NOI18N
        FormInput.add(LokasiPungsi);
        LokasiPungsi.setBounds(315, 95, 150, 23);

        // Jumlah Pungsi
        jLabel77.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel77.setText("Jumlah Pungsi :");
        jLabel77.setName("jLabel77"); // NOI18N
        FormInput.add(jLabel77);
        jLabel77.setBounds(480, 95, 100, 23);

        JumlahPungsi.setName("JumlahPungsi"); // NOI18N
        FormInput.add(JumlahPungsi);
        JumlahPungsi.setBounds(585, 95, 80, 23);

        // Terpasang Sheath
        jLabel78.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel78.setText("Terpasang Sheath :");
        jLabel78.setName("jLabel78"); // NOI18N
        FormInput.add(jLabel78);
        jLabel78.setBounds(10, 125, 120, 23);

        TerpasangSheath.setName("TerpasangSheath"); // NOI18N
        FormInput.add(TerpasangSheath);
        TerpasangSheath.setBounds(135, 125, 80, 23);

        // Lokasi Sheath
        jLabel79.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel79.setText("Lokasi Sheath :");
        jLabel79.setName("jLabel79"); // NOI18N
        FormInput.add(jLabel79);
        jLabel79.setBounds(230, 125, 100, 23);

        LokasiSheath.setName("LokasiSheath"); // NOI18N
        FormInput.add(LokasiSheath);
        LokasiSheath.setBounds(335, 125, 150, 23);

        // Alat Terpasang
        jLabel80.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel80.setText("Alat Terpasang :");
        jLabel80.setName("jLabel80"); // NOI18N
        FormInput.add(jLabel80);
        jLabel80.setBounds(500, 125, 110, 23);

        AlatTerpasang.setName("AlatTerpasang"); // NOI18N
        FormInput.add(AlatTerpasang);
        AlatTerpasang.setBounds(615, 125, 180, 23);

        // =========================================================
        // SECTION II: PULSASI
        // =========================================================
        jSeparator2.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator2.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(0, 155, 807, 1);

        jLabel100.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel100.setText("II. PULSASI");
        jLabel100.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel100.setName("jLabel100"); // NOI18N
        FormInput.add(jLabel100);
        jLabel100.setBounds(10, 155, 200, 23);

        // Pulsasi Radialis Kanan
        jLabel81.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel81.setText("Radialis Kanan :");
        jLabel81.setName("jLabel81"); // NOI18N
        FormInput.add(jLabel81);
        jLabel81.setBounds(10, 180, 110, 23);

        PulsasiRadialisKanan.setName("PulsasiRadialisKanan"); // NOI18N
        FormInput.add(PulsasiRadialisKanan);
        PulsasiRadialisKanan.setBounds(125, 180, 100, 23);

        // Pulsasi Radialis Kiri
        jLabel82.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel82.setText("Radialis Kiri :");
        jLabel82.setName("jLabel82"); // NOI18N
        FormInput.add(jLabel82);
        jLabel82.setBounds(240, 180, 100, 23);

        PulsasiRadialisKiri.setName("PulsasiRadialisKiri"); // NOI18N
        FormInput.add(PulsasiRadialisKiri);
        PulsasiRadialisKiri.setBounds(345, 180, 100, 23);

        // Pulsasi Dorsalis Kanan
        jLabel83.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel83.setText("Dorsalis Kanan :");
        jLabel83.setName("jLabel83"); // NOI18N
        FormInput.add(jLabel83);
        jLabel83.setBounds(460, 180, 110, 23);

        PulsasiDorsalisKanan.setName("PulsasiDorsalisKanan"); // NOI18N
        FormInput.add(PulsasiDorsalisKanan);
        PulsasiDorsalisKanan.setBounds(575, 180, 100, 23);

        // Pulsasi Dorsalis Kiri
        jLabel84.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel84.setText("Dorsalis Kiri :");
        jLabel84.setName("jLabel84"); // NOI18N
        FormInput.add(jLabel84);
        jLabel84.setBounds(690, 180, 95, 23);

        PulsasiDorsalisKiri.setName("PulsasiDorsalisKiri"); // NOI18N
        FormInput.add(PulsasiDorsalisKiri);
        PulsasiDorsalisKiri.setBounds(790, 180, 100, 23);

        // =========================================================
        // SECTION III: ANESTESI & ANTIKOAGULAN
        // =========================================================
        jSeparator3.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator3.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator3.setName("jSeparator3"); // NOI18N
        FormInput.add(jSeparator3);
        jSeparator3.setBounds(0, 210, 907, 1);

        jLabel101.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel101.setText("III. ANESTESI & ANTIKOAGULAN");
        jLabel101.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel101.setName("jLabel101"); // NOI18N
        FormInput.add(jLabel101);
        jLabel101.setBounds(10, 210, 250, 23);

        // Jenis Anestesi
        jLabel85.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel85.setText("Jenis Anestesi :");
        jLabel85.setName("jLabel85"); // NOI18N
        FormInput.add(jLabel85);
        jLabel85.setBounds(10, 235, 110, 23);

        JenisAnestesi.setName("JenisAnestesi"); // NOI18N
        FormInput.add(JenisAnestesi);
        JenisAnestesi.setBounds(125, 235, 150, 23);

        // Sedasi
        jLabel86.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel86.setText("Sedasi :");
        jLabel86.setName("jLabel86"); // NOI18N
        FormInput.add(jLabel86);
        jLabel86.setBounds(290, 235, 60, 23);

        Sedasi.setName("Sedasi"); // NOI18N
        FormInput.add(Sedasi);
        Sedasi.setBounds(355, 235, 120, 23);

        // Dosis Pre
        jLabel87.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel87.setText("Antikoagulan Pre :");
        jLabel87.setName("jLabel87"); // NOI18N
        FormInput.add(jLabel87);
        jLabel87.setBounds(10, 265, 125, 23);

        DosisPre.setName("DosisPre"); // NOI18N
        FormInput.add(DosisPre);
        DosisPre.setBounds(140, 265, 130, 23);

        // Dosis Intra
        jLabel88.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel88.setText("Antikoagulan Intra :");
        jLabel88.setName("jLabel88"); // NOI18N
        FormInput.add(jLabel88);
        jLabel88.setBounds(285, 265, 130, 23);

        DosisIntra.setName("DosisIntra"); // NOI18N
        FormInput.add(DosisIntra);
        DosisIntra.setBounds(420, 265, 130, 23);

        // Dosis Post
        jLabel89.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel89.setText("Antikoagulan Post :");
        jLabel89.setName("jLabel89"); // NOI18N
        FormInput.add(jLabel89);
        jLabel89.setBounds(565, 265, 125, 23);

        DosisPost.setName("DosisPost"); // NOI18N
        FormInput.add(DosisPost);
        DosisPost.setBounds(695, 265, 130, 23);

        // =========================================================
        // SECTION IV: BALANCE CAIRAN & LAINNYA
        // =========================================================
        jLabel90.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel90.setText("Komplikasi :");
        jLabel90.setName("jLabel90"); // NOI18N
        FormInput.add(jLabel90);
        jLabel90.setBounds(10, 295, 100, 23);

        Komplikasi.setName("Komplikasi"); // NOI18N
        FormInput.add(Komplikasi);
        Komplikasi.setBounds(115, 295, 250, 23);

        jLabel91.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel91.setText("Balance Cairan Intake :");
        jLabel91.setName("jLabel91"); // NOI18N
        FormInput.add(jLabel91);
        jLabel91.setBounds(380, 295, 140, 23);

        Intake.setName("Intake"); // NOI18N
        FormInput.add(Intake);
        Intake.setBounds(525, 295, 100, 23);

        jLabel92.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel92.setText("Output :");
        jLabel92.setName("jLabel92"); // NOI18N
        FormInput.add(jLabel92);
        jLabel92.setBounds(640, 295, 60, 23);

        Output.setName("Output"); // NOI18N
        FormInput.add(Output);
        Output.setBounds(705, 295, 100, 23);

        // =========================================================
        // SECTION V: INSTRUKSI & RENCANA
        // =========================================================
        jLabel93.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel93.setText("Instruksi Paska Tindakan :");
        jLabel93.setName("jLabel93"); // NOI18N
        FormInput.add(jLabel93);
        jLabel93.setBounds(10, 325, 165, 23);

        InstruksiPaska.setName("InstruksiPaska"); // NOI18N
        FormInput.add(InstruksiPaska);
        InstruksiPaska.setBounds(180, 325, 250, 23);

        jLabel148.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel148.setText("Masalah Keperawatan :");
        jLabel148.setName("jLabel148"); // NOI18N
        FormInput.add(jLabel148);
        jLabel148.setBounds(10, 355, 145, 23);

        MasalahKeperawatan.setName("MasalahKeperawatan"); // NOI18N
        FormInput.add(MasalahKeperawatan);
        MasalahKeperawatan.setBounds(160, 355, 300, 23);

        jLabel149.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel149.setText("Rencana Keperawatan :");
        jLabel149.setName("jLabel149"); // NOI18N
        FormInput.add(jLabel149);
        jLabel149.setBounds(475, 355, 145, 23);

        RencanaKeperawatan.setName("RencanaKeperawatan"); // NOI18N
        FormInput.add(RencanaKeperawatan);
        RencanaKeperawatan.setBounds(625, 355, 300, 23);

        jLabel73.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel73.setText("Keputusan Rawat :");
        jLabel73.setName("jLabel73"); // NOI18N
        FormInput.add(jLabel73);
        jLabel73.setBounds(10, 385, 120, 23);

        KeputusanRawat.setName("KeputusanRawat"); // NOI18N
        FormInput.add(KeputusanRawat);
        KeputusanRawat.setBounds(135, 385, 150, 23);

        scrollInput.setViewportView(FormInput);

        PanelInput.add(scrollInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed

        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{
            if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                isRawat();
            }
            Valid.pindah(evt,TCari,Tanggal);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPasienKeyPressed
        Valid.pindah(evt,TCari,BtnSimpan);
}//GEN-LAST:event_TPasienKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
        }else if(KdPetugas.getText().trim().equals("")||NmPetugas.getText().trim().equals("")){
            Valid.textKosong(KdPetugas,"Petugas");
        }else{
            if(akses.getkode().equals("Admin Utama")){
                simpan();
            }else{
                if(TanggalRegistrasi.getText().equals("")){
                    TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                }
                if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem())==true){
                    simpan();
                }
            }
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,KeputusanRawat,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        ChkInput.setSelected(true);
        isForm(); 
        emptTeks();
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(tbObat.getSelectedRow()>-1){
            if(akses.getkode().equals("Admin Utama")){
                hapus();
            }else{
                if(KdPetugas.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString())){
                    if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString(),Sequel.ambiltanggalsekarang())==true){
                        hapus();
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"Hanya bisa dihapus oleh petugas yang bersangkutan..!!");
                }
            }
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
        }  
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
        }else if(KdPetugas.getText().trim().equals("")||NmPetugas.getText().trim().equals("")){
            Valid.textKosong(KdPetugas,"Petugas");
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else{
                    if(KdPetugas.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString())){
                        if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString(),Sequel.ambiltanggalsekarang())==true){
                            if(TanggalRegistrasi.getText().equals("")){
                                TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                            }
                            if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem())==true){
                                ganti();
                            }
                        }
                    }else{
                        JOptionPane.showMessageDialog(null,"Hanya bisa diganti oleh petugas yang bersangkutan..!!");
                    }
                }
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
            }
        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnKeluarActionPerformed(null);
        }else{Valid.pindah(evt,BtnEdit,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tbObat.getSelectedRow() < 0 && tabMode.getRowCount() == 0){
            JOptionPane.showMessageDialog(null,"Maaf, tidak ada data yang bisa dicetak...!!!!");
            this.setCursor(Cursor.getDefaultCursor());
            return;
        }
        try {
            // Ambil data dari baris yang dipilih atau baris pertama
            int row = tbObat.getSelectedRow() >= 0 ? tbObat.getSelectedRow() : 0;
            String noRawat        = tbObat.getValueAt(row, 0).toString();
            String noRM           = tbObat.getValueAt(row, 1).toString();
            String namaPasien     = tbObat.getValueAt(row, 2).toString();
            String tglLahir       = tbObat.getValueAt(row, 3).toString();
            String umur           = tbObat.getValueAt(row, 4).toString();
            String nip            = tbObat.getValueAt(row, 5).toString();
            String petugas        = tbObat.getValueAt(row, 6).toString();
            String tanggal        = tbObat.getValueAt(row, 7).toString();
            String jamTiba        = tbObat.getValueAt(row, 8).toString();
            String lokasiPungsi   = tbObat.getValueAt(row, 9).toString();
            String jumlahPungsi   = tbObat.getValueAt(row, 10).toString();
            String terpSheath     = tbObat.getValueAt(row, 11).toString();
            String lokasiSheath   = tbObat.getValueAt(row, 12).toString();
            String radKanan       = tbObat.getValueAt(row, 13).toString();
            String radKiri        = tbObat.getValueAt(row, 14).toString();
            String dorKanan       = tbObat.getValueAt(row, 15).toString();
            String dorKiri        = tbObat.getValueAt(row, 16).toString();
            String alat           = tbObat.getValueAt(row, 17).toString();
            String anestesi       = tbObat.getValueAt(row, 18).toString();
            String sedasi         = tbObat.getValueAt(row, 19).toString();
            String dosisPre       = tbObat.getValueAt(row, 20).toString();
            String dosisIntra     = tbObat.getValueAt(row, 21).toString();
            String dosisPost      = tbObat.getValueAt(row, 22).toString();
            String komplikasi     = tbObat.getValueAt(row, 23).toString();
            String intake         = tbObat.getValueAt(row, 24).toString();
            String output         = tbObat.getValueAt(row, 25).toString();
            String instruksi      = tbObat.getValueAt(row, 26).toString();
            String masalah        = tbObat.getValueAt(row, 27).toString();
            String rencana        = tbObat.getValueAt(row, 28).toString();
            String keputusan      = tbObat.getValueAt(row, 29).toString();

            String chkYa    = terpSheath.equalsIgnoreCase("Ya")    ? "&#9745;" : "&#9744;";
            String chkTidak = terpSheath.equalsIgnoreCase("Tidak") ? "&#9745;" : "&#9744;";
            String chkRKAd  = radKanan.equalsIgnoreCase("Adekuat")       ? "&#9745;" : "&#9744;";
            String chkRKTd  = radKanan.equalsIgnoreCase("Tidak Adekuat") ? "&#9745;" : "&#9744;";
            String chkRKiAd = radKiri.equalsIgnoreCase("Adekuat")        ? "&#9745;" : "&#9744;";
            String chkRKiTd = radKiri.equalsIgnoreCase("Tidak Adekuat")  ? "&#9745;" : "&#9744;";
            String chkDKAd  = dorKanan.equalsIgnoreCase("Adekuat")       ? "&#9745;" : "&#9744;";
            String chkDKTd  = dorKanan.equalsIgnoreCase("Tidak Adekuat") ? "&#9745;" : "&#9744;";
            String chkDKiAd = dorKiri.equalsIgnoreCase("Adekuat")        ? "&#9745;" : "&#9744;";
            String chkDKiTd = dorKiri.equalsIgnoreCase("Tidak Adekuat")  ? "&#9745;" : "&#9744;";
            String chkUmum  = anestesi.equalsIgnoreCase("Umum")  ? "&#9745;" : "&#9744;";
            String chkLokal = anestesi.equalsIgnoreCase("Lokal") ? "&#9745;" : "&#9744;";

            // Query data observasi tanda vital
            java.util.List<java.util.Map<String, String>> listObs = new java.util.ArrayList<>();
            try {
                String tglObs = tanggal.length() >= 10 ? tanggal.substring(0, 10) : tanggal;
                PreparedStatement psObs = koneksi.prepareStatement(
                    "select jam_obs, bp_sistolik, bp_diastolik, nadi, pernapasan, suhu, saturasi_o2, pulsasi_distal, reflek_menelan " +
                    "from asesmen_invasif_observasi where no_rawat = ? and tanggal = ? order by jam_obs"
                );
                psObs.setString(1, noRawat);
                psObs.setString(2, tglObs);
                ResultSet rsObs = psObs.executeQuery();
                while (rsObs.next()) {
                    java.util.Map<String, String> m = new java.util.HashMap<>();
                    m.put("jam", rsObs.getString("jam_obs") == null ? "" : rsObs.getString("jam_obs"));
                    m.put("sis", rsObs.getString("bp_sistolik") == null ? "" : rsObs.getString("bp_sistolik"));
                    m.put("dia", rsObs.getString("bp_diastolik") == null ? "" : rsObs.getString("bp_diastolik"));
                    m.put("nadi", rsObs.getString("nadi") == null ? "" : rsObs.getString("nadi"));
                    m.put("rr", rsObs.getString("pernapasan") == null ? "" : rsObs.getString("pernapasan"));
                    m.put("suhu", rsObs.getString("suhu") == null ? "" : rsObs.getString("suhu"));
                    m.put("spo2", rsObs.getString("saturasi_o2") == null ? "" : rsObs.getString("saturasi_o2"));
                    m.put("pulsasi", rsObs.getString("pulsasi_distal") == null ? "" : rsObs.getString("pulsasi_distal"));
                    m.put("reflek", rsObs.getString("reflek_menelan") == null ? "" : rsObs.getString("reflek_menelan"));
                    listObs.add(m);
                }
                rsObs.close();
                psObs.close();
            } catch (Exception ex) {
                System.out.println("Error query observasi cetak: " + ex);
            }

            int numCols = Math.max(12, listObs.size());
            StringBuilder trJam = new StringBuilder();
            StringBuilder[] trLevels = new StringBuilder[10];
            for (int r = 0; r < 10; r++) trLevels[r] = new StringBuilder();
            StringBuilder trTD = new StringBuilder();
            StringBuilder trHR = new StringBuilder();
            StringBuilder trRR = new StringBuilder();
            StringBuilder trSuhu = new StringBuilder();
            StringBuilder trSpO2 = new StringBuilder();
            StringBuilder trPulsasi = new StringBuilder();
            StringBuilder trReflek = new StringBuilder();

            int[] bpLevels = {200, 180, 160, 140, 120, 100, 80, 60, 40, 20};

            for (int c = 0; c < numCols; c++) {
                if (c < listObs.size()) {
                    java.util.Map<String, String> obs = listObs.get(c);
                    trJam.append("<td style='font-weight:bold;background:#eef4fa;'>").append(obs.get("jam")).append("</td>");

                    int sisVal = -1;
                    int diaVal = -1;
                    try { sisVal = Integer.parseInt(obs.get("sis")); } catch (Exception ignored) {}
                    try { diaVal = Integer.parseInt(obs.get("dia")); } catch (Exception ignored) {}

                    for (int r = 0; r < bpLevels.length; r++) {
                        int lvl = bpLevels[r];
                        StringBuilder cellContent = new StringBuilder();
                        if (sisVal >= lvl - 10 && sisVal < lvl + 10) {
                            cellContent.append("<span style='color:red;font-weight:bold;'>&#9660;").append(sisVal).append("</span>");
                        }
                        if (diaVal >= lvl - 10 && diaVal < lvl + 10) {
                            if (cellContent.length() > 0) cellContent.append(" ");
                            cellContent.append("<span style='color:red;font-weight:bold;'>&#9650;").append(diaVal).append("</span>");
                        }
                        trLevels[r].append("<td>").append(cellContent.toString()).append("</td>");
                    }

                    String tdVal = (obs.get("sis").isEmpty() ? "" : obs.get("sis")) + (obs.get("dia").isEmpty() ? "" : "/" + obs.get("dia"));
                    trTD.append("<td style='font-weight:bold;'>").append(tdVal).append("</td>");
                    trHR.append("<td>").append(obs.get("nadi")).append("</td>");
                    trRR.append("<td>").append(obs.get("rr")).append("</td>");
                    trSuhu.append("<td>").append(obs.get("suhu")).append("</td>");
                    trSpO2.append("<td>").append(obs.get("spo2")).append("</td>");
                    trPulsasi.append("<td style='font-size:7px;'>").append(obs.get("pulsasi")).append("</td>");
                    trReflek.append("<td style='font-size:7px;'>").append(obs.get("reflek")).append("</td>");
                } else {
                    trJam.append("<td></td>");
                    for (int r = 0; r < 10; r++) trLevels[r].append("<td></td>");
                    trTD.append("<td></td>");
                    trHR.append("<td></td>");
                    trRR.append("<td></td>");
                    trSuhu.append("<td></td>");
                    trSpO2.append("<td></td>");
                    trPulsasi.append("<td></td>");
                    trReflek.append("<td></td>");
                }
            }

            String html =
            "<!DOCTYPE html><html><head><meta charset='UTF-8'>"+
            "<style>"+
            "  body{font-family:Arial,sans-serif;font-size:10px;margin:10px;}"+
            "  .page{width:750px;margin:auto;border:1px solid #000;padding:8px;}"+
            "  .header-top{display:flex;justify-content:space-between;border-bottom:2px solid #000;padding-bottom:6px;margin-bottom:4px;}"+
            "  .rs-name{font-size:11px;font-weight:bold;}"+
            "  .form-no{font-size:8px;text-align:right;}"+
            "  .label-box{border:1px solid #000;padding:4px 8px;min-width:220px;}"+
            "  .label-row{display:flex;gap:4px;margin-bottom:2px;}"+
            "  .label-row label{width:95px;}"+
            "  .label-row span{border-bottom:1px solid #000;flex:1;min-width:100px;display:inline-block;}"+
            "  .section-title{background:#4a90d9;color:#fff;text-align:center;font-weight:bold;font-size:11px;padding:3px;margin:6px 0 4px 0;}"+
            "  .obs-note{font-size:8px;margin-bottom:4px;font-style:italic;}"+
            "  table.vital{width:100%;border-collapse:collapse;font-size:8px;}"+
            "  table.vital td,table.vital th{border:1px solid #000;text-align:center;padding:1px 2px;height:13px;}"+
            "  table.vital .rot{writing-mode:vertical-rl;transform:rotate(180deg);width:14px;font-weight:bold;font-size:9px;}"+
            "  .field-section{margin-top:6px;}"+
            "  .field-row{display:flex;align-items:center;margin-bottom:3px;gap:4px;}"+
            "  .field-row .lbl{min-width:130px;font-weight:bold;}"+
            "  .field-row .val{border-bottom:1px solid #000;flex:1;min-height:12px;}"+
            "  .chk{font-size:12px;}"+
            "  .sig-area{margin-top:12px;display:flex;justify-content:flex-end;}"+
            "  .sig-box{text-align:center;width:200px;}"+
            "  .sig-line{border-top:1px solid #000;margin-top:40px;}"+
            "  hr.sec{border:0;border-top:1px solid #999;margin:5px 0;}"+
            "  .two-col{display:flex;gap:8px;}"+
            "  .two-col > div{flex:1;}"+
            "  b.sec-lbl{display:block;font-size:10px;font-weight:bold;margin:5px 0 2px 0;}"+
            "</style></head><body>"+
            "<div class='page'>"+

            // HEADER
            "  <div class='header-top'>"+
            "    <div>"+
            "      <div class='rs-name'>"+akses.getnamars()+"</div>"+
            "      <div style='font-size:9px;'>"+akses.getalamatrs()+"</div>"+
            "      <div style='font-size:9px;'>Telp. "+akses.getkontakrs()+"</div>"+
            "      <div style='margin-top:6px;font-weight:bold;font-size:11px;'>ASESMEN AWAL KEPERAWATAN<br>TINDAKAN INVASIF NON BEDAH</div>"+
            "    </div>"+
            "    <div>"+
            "      <div class='form-no'>F.V.3.1/029/B/2015</div>"+
            "      <div class='label-box' style='margin-top:4px;'>"+
            "        <div class='label-row'><label>Nama Pasien</label><span>&nbsp;"+namaPasien+"</span></div>"+
            "        <div class='label-row'><label>Tanggal Lahir</label><span>&nbsp;"+tglLahir+"</span></div>"+
            "        <div class='label-row'><label>No Rekam Medis</label><span>&nbsp;"+noRM+"</span></div>"+
            "        <div style='font-size:8px;font-style:italic;margin-top:4px;'>(Mohon lengkapi/tempelkan label pasien)</div>"+
            "      </div>"+
            "      <div style='font-size:8px;margin-top:3px;'>No. Rawat : <b>"+noRawat+"</b> &nbsp; Tgl: <b>"+tanggal+"</b></div>"+
            "    </div>"+
            "  </div>"+

            // OBSERVASI INTRA DAN PASKA TINDAKAN
            "  <div class='section-title'>OBSERVASI INTRA DAN PASKA TINDAKAN</div>"+
            "  <div class='obs-note'>Observasi dimulai dari intra sampai paska tindakan (Observasi setiap 5 sd 15 menit).<br>"+
            "  Observasi paska tindakan 15 menit selama 1 jam pertama, dilanjutkan 30 menit selama 1 jam kedua, dan setiap 1 jam selama 2 jam. (sesuai kebutuhan)</div>"+

            // Vital sign grid
            "  <table class='vital'>"+
            "    <tr>"+
            "      <td rowspan='18' class='rot'>T A N D A &nbsp; V I T A L</td>"+
            "      <td style='font-weight:bold;'>BP</td><td style='font-weight:bold;'>N</td><td style='font-weight:bold;'>P</td><td style='font-weight:bold;'>S</td>"+
            trJam.toString() +
            "    </tr>"+
            "    <tr><td style='color:red;font-weight:bold;'>200</td><td>200</td><td>44</td><td>40</td>" + trLevels[0].toString() + "</tr>"+
            "    <tr><td style='color:red;font-weight:bold;'>180</td><td>180</td><td>40</td><td>39</td>" + trLevels[1].toString() + "</tr>"+
            "    <tr><td style='color:red;font-weight:bold;'>160</td><td>160</td><td>32</td><td>38</td>" + trLevels[2].toString() + "</tr>"+
            "    <tr><td style='color:red;font-weight:bold;'>140</td><td>148</td><td>28</td><td>37</td>" + trLevels[3].toString() + "</tr>"+
            "    <tr><td style='color:red;font-weight:bold;'>120</td><td>120</td><td>24</td><td>36</td>" + trLevels[4].toString() + "</tr>"+
            "    <tr><td style='color:red;font-weight:bold;'>100</td><td>100</td><td>20</td><td>35</td>" + trLevels[5].toString() + "</tr>"+
            "    <tr><td style='color:red;font-weight:bold;'>80</td><td>80</td><td>16</td><td>34</td>" + trLevels[6].toString() + "</tr>"+
            "    <tr><td style='color:red;font-weight:bold;'>60</td><td>60</td><td>12</td><td>33</td>" + trLevels[7].toString() + "</tr>"+
            "    <tr><td style='color:red;font-weight:bold;'>40</td><td>40</td><td>8</td><td>32</td>" + trLevels[8].toString() + "</tr>"+
            "    <tr><td style='color:red;font-weight:bold;'>20</td><td>20</td><td>4</td><td>31</td>" + trLevels[9].toString() + "</tr>"+
            "    <tr style='background:#f9f9f9;'><td colspan='4' style='font-weight:bold;'>TD (mmHg)</td>" + trTD.toString() + "</tr>"+
            "    <tr style='background:#f9f9f9;'><td colspan='4' style='font-weight:bold;'>HR (x/m)</td>" + trHR.toString() + "</tr>"+
            "    <tr style='background:#f9f9f9;'><td colspan='4' style='font-weight:bold;'>RR (x/m)</td>" + trRR.toString() + "</tr>"+
            "    <tr style='background:#f9f9f9;'><td colspan='4' style='font-weight:bold;'>Suhu (°C)</td>" + trSuhu.toString() + "</tr>"+
            "    <tr><td colspan='4'>Saturasi O2 (%)</td>" + trSpO2.toString() + "</tr>"+
            "    <tr><td colspan='4'>Pulsasi distal</td>" + trPulsasi.toString() + "</tr>"+
            "    <tr><td colspan='4'>Reflek menelan</td>" + trReflek.toString() + "</tr>"+
            "  </table>"+

            // DATA TINDAKAN
            "  <div class='field-section'>"+
            "    <div class='field-row'>"+
            "      <span class='lbl'>Pasien tiba di RR jam :</span><span class='val'>&nbsp;"+jamTiba+"</span>"+
            "      <span class='lbl' style='min-width:110px;margin-left:10px;'>Lokasi pungsi/sayatan :</span><span class='val'>&nbsp;"+lokasiPungsi+"</span>"+
            "      <span class='lbl' style='min-width:60px;margin-left:6px;'>Jumlah :</span><span class='val' style='min-width:60px;'>&nbsp;"+jumlahPungsi+"</span>"+
            "    </div>"+
            "    <div class='field-row'>"+
            "      <span class='lbl'>Terpasang sheath :</span>"+
            "      <span class='chk'>"+chkYa+"</span> Ya &nbsp;&nbsp;"+
            "      <span class='chk'>"+chkTidak+"</span> Tidak &nbsp;&nbsp;"+
            "      <span style='min-width:50px;'>Lokasi : </span><span class='val'>&nbsp;"+lokasiSheath+"</span>"+
            "    </div>"+
            "    <div class='field-row'>"+
            "      <span class='lbl'>Pulsasi A. Radialis :</span> Kanan &nbsp;"+
            "      <span class='chk'>"+chkRKAd+"</span> Adekuat &nbsp;"+
            "      <span class='chk'>"+chkRKTd+"</span> Tidak adekuat &nbsp;&nbsp;&nbsp; Kiri &nbsp;"+
            "      <span class='chk'>"+chkRKiAd+"</span> Adekuat &nbsp;"+
            "      <span class='chk'>"+chkRKiTd+"</span> Tidak adekuat"+
            "    </div>"+
            "    <div class='field-row'>"+
            "      <span class='lbl'>Pulsasi A. Dorsalis Pedis :</span> Kanan &nbsp;"+
            "      <span class='chk'>"+chkDKAd+"</span> Adekuat &nbsp;"+
            "      <span class='chk'>"+chkDKTd+"</span> Tidak adekuat &nbsp;&nbsp;&nbsp; Kiri &nbsp;"+
            "      <span class='chk'>"+chkDKiAd+"</span> Adekuat &nbsp;"+
            "      <span class='chk'>"+chkDKiTd+"</span> Tidak adekuat"+
            "    </div>"+
            "    <div class='field-row'>"+
            "      <span class='lbl'>Alat yang terpasang :</span>"+
            "      <span class='val'>&nbsp;"+alat+"</span>"+
            "    </div>"+
            "    <div class='field-row'>"+
            "      <span class='lbl'>Jenis anestesi :</span>"+
            "      <span class='chk'>"+chkUmum+"</span> Umum &nbsp;"+
            "      <span class='chk'>"+chkLokal+"</span> Lokal &nbsp;&nbsp;&nbsp;"+
            "      Dosis pemberian : &nbsp; Pre prosedur : <span class='val' style='min-width:80px;'>&nbsp;"+dosisPre+"</span>"+
            "    </div>"+
            "    <div class='field-row'>"+
            "      <span style='min-width:280px;'></span>"+
            "      Intra prosedur : <span class='val' style='min-width:80px;'>&nbsp;"+dosisIntra+"</span>"+
            "    </div>"+
            "    <div class='field-row'>"+
            "      <span class='lbl'>Sedasi :</span><span class='val'>&nbsp;"+sedasi+"</span>"+
            "    </div>"+
            "    <div class='field-row'>"+
            "      <span class='lbl'>Antikoagulan :</span>"+
            "      <span class='val' style='min-width:120px;'></span>&nbsp;&nbsp;"+
            "      Dosis pemberian : &nbsp; Pre prosedur : <span class='val' style='min-width:80px;'>&nbsp;"+dosisPre+"</span>"+
            "    </div>"+
            "    <div class='field-row'>"+
            "      <span style='min-width:280px;'></span>"+
            "      Intra prosedur : <span class='val' style='min-width:80px;'>&nbsp;"+dosisIntra+"</span>"+
            "    </div>"+
            "    <div class='field-row'>"+
            "      <span style='min-width:280px;'></span>"+
            "      Post prosedur : <span class='val' style='min-width:80px;'>&nbsp;"+dosisPost+"</span>"+
            "    </div>"+
            "  </div>"+

            // KOMPLIKASI
            "  <div class='field-row' style='margin-top:4px;'>"+
            "    <span class='lbl'>Komplikasi :</span>"+
            "    <span class='val'>&nbsp;"+komplikasi+"</span>"+
            "  </div>"+

            // KESEIMBANGAN CAIRAN
            "  <b class='sec-lbl'>Keseimbangan cairan</b>"+
            "  <div class='two-col'>"+
            "    <div>"+
            "      <div class='field-row'><span class='lbl'>Intake &nbsp;: Minum</span><span class='val'>&nbsp;"+intake+"</span> ml</div>"+
            "      <div class='field-row'><span class='lbl' style='min-width:90px;'>IV Line</span><span class='val'></span> ml</div>"+
            "    </div>"+
            "    <div>"+
            "      <div class='field-row'><b>Output :</b> &nbsp; Perdarahan <span class='val'>&nbsp;"+output+"</span> ml</div>"+
            "      <div class='field-row'><span style='min-width:80px;'></span>Urin <span class='val'></span> ml</div>"+
            "    </div>"+
            "  </div>"+

            // INSTRUKSI PASKA
            "  <b class='sec-lbl'>Instruksi paska tindakan</b>"+
            "  <div class='field-row'><span class='val'>&nbsp;"+instruksi+"</span></div>"+

            // MASALAH KEPERAWATAN
            "  <b class='sec-lbl'>Masalah keperawatan</b>"+
            "  <div class='field-row'><span class='val'>&nbsp;"+masalah+"</span></div>"+

            // RENCANA KEPERAWATAN
            "  <b class='sec-lbl'>Rencana tindakan keperawatan (mandiri)</b>"+
            "  <div class='field-row'><span class='val'>&nbsp;"+rencana+"</span></div>"+

            // KEPUTUSAN RAWAT
            "  <b class='sec-lbl'>Keputusan rawat :</b>"+
            "  <div class='field-row'><span class='val'>&nbsp;"+keputusan+"</span></div>"+

            // TANDA TANGAN
            "  <div class='sig-area'>"+
            "    <div class='sig-box'>"+
            "      <div>"+akses.getkabupatenrs()+", "+tanggal.substring(0,10)+"</div>"+
            "      <div>Yang mengobservasi,</div>"+
            "      <div class='sig-line'>"+
            "        <div>"+petugas+"</div>"+
            "        <div style='font-size:8px;'>NIP. "+nip+"</div>"+
            "      </div>"+
            "    </div>"+
            "  </div>"+

            "</div></body></html>";

            File f = new File("AsesmenInvasifNonBedah.html");
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(html);
            bw.close();
            Desktop.getDesktop().browse(f.toURI());

        } catch(Exception e) {
            System.out.println("Notifikasi Print : " + e);
            JOptionPane.showMessageDialog(null, "Gagal membuka laporan: " + e.getMessage());
        }
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void btnObservasiAction() {
        if (tbObat.getSelectedRow() > -1) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            RMObservasiInvasifNonBedah formObs = new RMObservasiInvasifNonBedah(null, false);
            formObs.isCek();
            formObs.setNoRm(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString(), Valid.SetTgl2(tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString()));
            formObs.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            formObs.setLocationRelativeTo(internalFrame1);
            formObs.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        } else if (!TNoRw.getText().trim().equals("")) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            RMObservasiInvasifNonBedah formObs = new RMObservasiInvasifNonBedah(null, false);
            formObs.isCek();
            formObs.setNoRm(TNoRw.getText(), Tanggal.getDate());
            formObs.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            formObs.setLocationRelativeTo(internalFrame1);
            formObs.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        } else {
            JOptionPane.showMessageDialog(null, "Silahkan pilih data pada tabel atau masukkan No. Rawat terlebih dahulu...!");
        }
    }



    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        runBackground(() ->tampil());
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() ->tampil());
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            runBackground(() ->tampil());
            TCari.setText("");
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah(evt,TCari,Jam);
}//GEN-LAST:event_TanggalKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void JamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JamKeyPressed
        Valid.pindah(evt,Tanggal,Menit);
    }//GEN-LAST:event_JamKeyPressed

    private void MenitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MenitKeyPressed
        Valid.pindah(evt,Jam,Detik);
    }//GEN-LAST:event_MenitKeyPressed

    private void DetikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DetikKeyPressed
        Valid.pindah(evt,Menit,BtnPetugas);
    }//GEN-LAST:event_DetikKeyPressed

    private void BtnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPetugasActionPerformed
        if (petugas == null || !petugas.isDisplayable()) {
            petugas=new DlgCariPetugas(null,false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(petugas.getTable().getSelectedRow()!= -1){                   
                        KdPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                        NmPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());
                    }  
                    BtnPetugas.requestFocus();
                    petugas=null;
                }
            });

            petugas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            petugas.setLocationRelativeTo(internalFrame1);
        }
        if (petugas == null) return;
        if (!petugas.isVisible()) {
            petugas.isCek();    
            petugas.emptTeks();
        }
        
        if (petugas.isVisible()) {
            petugas.toFront();
            return;
        }
        petugas.setVisible(true); 
    }//GEN-LAST:event_BtnPetugasActionPerformed

    private void BtnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPetugasKeyPressed
        Valid.pindah(evt,TCari,JamTiba);
    }//GEN-LAST:event_BtnPetugasKeyPressed

    private void MnSkriningKekerasanPadaPerempuanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnSkriningKekerasanPadaPerempuanActionPerformed
        if(tbObat.getSelectedRow()>-1){
            getData();
            BtnPrintActionPerformed(null);
        }
    }//GEN-LAST:event_MnSkriningKekerasanPadaPerempuanActionPerformed

    private void PertanyaanAwal1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PertanyaanAwal1ItemStateChanged
        NilaiPertanyaanAwal1.setText((PertanyaanAwal1.getSelectedIndex()+1)+"");
        isTotal();
    }//GEN-LAST:event_PertanyaanAwal1ItemStateChanged

    private void PertanyaanAwal1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PertanyaanAwal1KeyPressed
        Valid.pindah(evt,TCari,PertanyaanAwal2);
    }//GEN-LAST:event_PertanyaanAwal1KeyPressed

    private void PertanyaanAwal2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PertanyaanAwal2ItemStateChanged
        NilaiPertanyaanAwal2.setText((PertanyaanAwal2.getSelectedIndex()+1)+"");
        isTotal();
    }//GEN-LAST:event_PertanyaanAwal2ItemStateChanged

    private void PertanyaanAwal2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PertanyaanAwal2KeyPressed
        Valid.pindah(evt,PertanyaanAwal1,PertanyaanLanjutan1);
    }//GEN-LAST:event_PertanyaanAwal2KeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void PertanyaanLanjutan1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan1ItemStateChanged
        NilaiPertanyaanLanjutan1.setText((PertanyaanLanjutan1.getSelectedIndex()+1)+"");
        isTotal();
    }//GEN-LAST:event_PertanyaanLanjutan1ItemStateChanged

    private void PertanyaanLanjutan1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan1KeyPressed
        Valid.pindah(evt,PertanyaanAwal2,PertanyaanLanjutan2);
    }//GEN-LAST:event_PertanyaanLanjutan1KeyPressed

    private void PertanyaanLanjutan2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan2ItemStateChanged
        NilaiPertanyaanLanjutan2.setText((PertanyaanLanjutan2.getSelectedIndex()+1)+"");
        isTotal();
    }//GEN-LAST:event_PertanyaanLanjutan2ItemStateChanged

    private void PertanyaanLanjutan2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan2KeyPressed
        Valid.pindah(evt,PertanyaanLanjutan1,PertanyaanLanjutan3);
    }//GEN-LAST:event_PertanyaanLanjutan2KeyPressed

    private void HasilSkriningKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HasilSkriningKeyPressed
        //Valid.pindah(evt,Lapor,SG1);
    }//GEN-LAST:event_HasilSkriningKeyPressed

    private void PertanyaanLanjutan3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan3ItemStateChanged
        NilaiPertanyaanLanjutan3.setText((PertanyaanLanjutan3.getSelectedIndex()+1)+"");
        isTotal();
    }//GEN-LAST:event_PertanyaanLanjutan3ItemStateChanged

    private void PertanyaanLanjutan3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan3KeyPressed
        Valid.pindah(evt,PertanyaanLanjutan2,PertanyaanLanjutan4);
    }//GEN-LAST:event_PertanyaanLanjutan3KeyPressed

    private void PertanyaanLanjutan4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan4ItemStateChanged
        NilaiPertanyaanLanjutan4.setText((PertanyaanLanjutan4.getSelectedIndex()+1)+"");
        isTotal();
    }//GEN-LAST:event_PertanyaanLanjutan4ItemStateChanged

    private void PertanyaanLanjutan4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan4KeyPressed
        Valid.pindah(evt,PertanyaanLanjutan3,PertanyaanLanjutan5);
    }//GEN-LAST:event_PertanyaanLanjutan4KeyPressed

    private void PertanyaanLanjutan5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan5ItemStateChanged
        NilaiPertanyaanLanjutan5.setText((PertanyaanLanjutan5.getSelectedIndex()+1)+"");
        isTotal();
    }//GEN-LAST:event_PertanyaanLanjutan5ItemStateChanged

    private void PertanyaanLanjutan5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan5KeyPressed
        Valid.pindah(evt,PertanyaanLanjutan4,PertanyaanLanjutan6);
    }//GEN-LAST:event_PertanyaanLanjutan5KeyPressed

    private void PertanyaanLanjutan6ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan6ItemStateChanged
        NilaiPertanyaanLanjutan6.setText((PertanyaanLanjutan6.getSelectedIndex()+1)+"");
        isTotal();
    }//GEN-LAST:event_PertanyaanLanjutan6ItemStateChanged

    private void PertanyaanLanjutan6KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PertanyaanLanjutan6KeyPressed
        Valid.pindah(evt,PertanyaanLanjutan5,BtnSimpan);
    }//GEN-LAST:event_PertanyaanLanjutan6KeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
            });
        }
    }//GEN-LAST:event_formWindowOpened

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMAsesmenInvasifNonBedah dialog = new RMAsesmenInvasifNonBedah(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPetugas;
    private widget.Button BtnPrint;
    private widget.Button BtnObservasi;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.CekBox ChkKejadian;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.ComboBox Detik;
    private widget.PanelBiasa FormInput;
    private widget.TextBox HasilSkrining;
    private widget.ComboBox Jam;
    private widget.TextBox KdPetugas;
    private widget.Label LCount;
    private widget.editorpane LoadHTML;
    private widget.ComboBox Menit;
    private javax.swing.JMenuItem MnSkriningKekerasanPadaPerempuan;
    private javax.swing.JMenuItem MnObservasi;
    private widget.TextBox NilaiPertanyaanAwal1;
    private widget.TextBox NilaiPertanyaanAwal2;
    private widget.TextBox NilaiPertanyaanLanjutan1;
    private widget.TextBox NilaiPertanyaanLanjutan2;
    private widget.TextBox NilaiPertanyaanLanjutan3;
    private widget.TextBox NilaiPertanyaanLanjutan4;
    private widget.TextBox NilaiPertanyaanLanjutan5;
    private widget.TextBox NilaiPertanyaanLanjutan6;
    private widget.TextBox NmPetugas;
    private javax.swing.JPanel PanelInput;
    private widget.ComboBox PertanyaanAwal1;
    private widget.ComboBox PertanyaanAwal2;
    private widget.ComboBox PertanyaanLanjutan1;
    private widget.ComboBox PertanyaanLanjutan2;
    private widget.ComboBox PertanyaanLanjutan3;
    private widget.ComboBox PertanyaanLanjutan4;
    private widget.ComboBox PertanyaanLanjutan5;
    private widget.ComboBox PertanyaanLanjutan6;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal Tanggal;
    private widget.TextBox TanggalRegistrasi;
    private widget.TextBox TglLahir;
    private widget.TextBox TotalHasil;
    private widget.TextBox Umur;
    private javax.swing.ButtonGroup buttonGroup1;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel100;
    private widget.Label jLabel101;
    private widget.Label jLabel148;
    private widget.Label jLabel149;
    private widget.Label jLabel16;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel4;
    private widget.Label jLabel6;
    private widget.Label jLabel69;
    private widget.Label jLabel7;
    private widget.Label jLabel70;
    private widget.Label jLabel71;
    private widget.Label jLabel72;
    private widget.Label jLabel73;
    private widget.Label jLabel74;
    private widget.Label jLabel75;
    private widget.Label jLabel76;
    private widget.Label jLabel77;
    private widget.Label jLabel78;
    private widget.Label jLabel79;
    private widget.Label jLabel8;
    private widget.Label jLabel80;
    private widget.Label jLabel81;
    private widget.Label jLabel82;
    private widget.Label jLabel83;
    private widget.Label jLabel84;
    private widget.Label jLabel85;
    private widget.Label jLabel86;
    private widget.Label jLabel87;
    private widget.Label jLabel88;
    private widget.Label jLabel89;
    private widget.Label jLabel90;
    private widget.Label jLabel91;
    private widget.Label jLabel92;
    private widget.Label jLabel93;
    private widget.Label jLabel99;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables
    
    
    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            String query = "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,reg_periksa.umurdaftar,reg_periksa.sttsumur,asesmen_keperawatan_invasif_non_bedah.nip,"+
                           "petugas.nama,asesmen_keperawatan_invasif_non_bedah.tanggal,asesmen_keperawatan_invasif_non_bedah.jam_tiba_rr,asesmen_keperawatan_invasif_non_bedah.lokasi_pungsi,"+
                           "asesmen_keperawatan_invasif_non_bedah.jumlah_pungsi,asesmen_keperawatan_invasif_non_bedah.terpasang_sheath,asesmen_keperawatan_invasif_non_bedah.lokasi_sheath,"+
                           "asesmen_keperawatan_invasif_non_bedah.pulsasi_radialis_kanan,asesmen_keperawatan_invasif_non_bedah.pulsasi_radialis_kiri,asesmen_keperawatan_invasif_non_bedah.pulsasi_dorsalis_kanan,"+
                           "asesmen_keperawatan_invasif_non_bedah.pulsasi_dorsalis_kiri,asesmen_keperawatan_invasif_non_bedah.alat_terpasang,asesmen_keperawatan_invasif_non_bedah.jenis_anestesi,"+
                           "asesmen_keperawatan_invasif_non_bedah.sedasi,asesmen_keperawatan_invasif_non_bedah.antikoagulan_dosis_pre,asesmen_keperawatan_invasif_non_bedah.antikoagulan_dosis_intra,"+
                           "asesmen_keperawatan_invasif_non_bedah.antikoagulan_dosis_post,asesmen_keperawatan_invasif_non_bedah.komplikasi,asesmen_keperawatan_invasif_non_bedah.balance_cairan_intake,"+
                           "asesmen_keperawatan_invasif_non_bedah.balance_cairan_output,asesmen_keperawatan_invasif_non_bedah.instruksi_paska_tindakan,asesmen_keperawatan_invasif_non_bedah.masalah_keperawatan,"+
                           "asesmen_keperawatan_invasif_non_bedah.rencana_keperawatan,asesmen_keperawatan_invasif_non_bedah.keputusan_rawat "+
                           "from asesmen_keperawatan_invasif_non_bedah inner join reg_periksa on asesmen_keperawatan_invasif_non_bedah.no_rawat=reg_periksa.no_rawat "+
                           "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join petugas on asesmen_keperawatan_invasif_non_bedah.nip=petugas.nip "+
                           "where asesmen_keperawatan_invasif_non_bedah.tanggal between ? and ? "+
                           (TCari.getText().trim().equals("") ? "" : "and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or asesmen_keperawatan_invasif_non_bedah.nip like ? or petugas.nama like ?) ") +
                           "order by asesmen_keperawatan_invasif_non_bedah.tanggal";
                           
            ps=koneksi.prepareStatement(query);
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                if(!TCari.getText().trim().equals("")){
                    ps.setString(3,"%"+TCari.getText().trim()+"%");
                    ps.setString(4,"%"+TCari.getText().trim()+"%");
                    ps.setString(5,"%"+TCari.getText().trim()+"%");
                    ps.setString(6,"%"+TCari.getText().trim()+"%");
                    ps.setString(7,"%"+TCari.getText().trim()+"%");
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new String[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("tgl_lahir"),rs.getString("umurdaftar")+" "+rs.getString("sttsumur"),
                        rs.getString("nip"),rs.getString("nama"),rs.getString("tanggal"),rs.getString("jam_tiba_rr"),rs.getString("lokasi_pungsi"),rs.getString("jumlah_pungsi"),
                        rs.getString("terpasang_sheath"),rs.getString("lokasi_sheath"),rs.getString("pulsasi_radialis_kanan"),rs.getString("pulsasi_radialis_kiri"),
                        rs.getString("pulsasi_dorsalis_kanan"),rs.getString("pulsasi_dorsalis_kiri"),rs.getString("alat_terpasang"),rs.getString("jenis_anestesi"),
                        rs.getString("sedasi"),rs.getString("antikoagulan_dosis_pre"),rs.getString("antikoagulan_dosis_intra"),rs.getString("antikoagulan_dosis_post"),
                        rs.getString("komplikasi"),rs.getString("balance_cairan_intake"),rs.getString("balance_cairan_output"),rs.getString("instruksi_paska_tindakan"),
                        rs.getString("masalah_keperawatan"),rs.getString("rencana_keperawatan"),rs.getString("keputusan_rawat")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
    }

    
    public void emptTeks() {
        Tanggal.setDate(new Date());
        JamTiba.setText("");
        LokasiPungsi.setText("");
        JumlahPungsi.setText("");
        TerpasangSheath.setSelectedIndex(0);
        LokasiSheath.setText("");
        PulsasiRadialisKanan.setText("");
        PulsasiRadialisKiri.setText("");
        PulsasiDorsalisKanan.setText("");
        PulsasiDorsalisKiri.setText("");
        AlatTerpasang.setText("");
        JenisAnestesi.setText("");
        Sedasi.setText("");
        DosisPre.setText("");
        DosisIntra.setText("");
        DosisPost.setText("");
        Komplikasi.setText("");
        Intake.setText("");
        Output.setText("");
        InstruksiPaska.setText("");
        MasalahKeperawatan.setText("");
        RencanaKeperawatan.setText("");
        KeputusanRawat.setSelectedIndex(0);
        JamTiba.requestFocus();
    } 

    
    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
            JamTiba.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
            LokasiPungsi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            JumlahPungsi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            TerpasangSheath.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            LokasiSheath.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            PulsasiRadialisKanan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            PulsasiRadialisKiri.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            PulsasiDorsalisKanan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            PulsasiDorsalisKiri.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
            AlatTerpasang.setText(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());
            JenisAnestesi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString());
            Sedasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),19).toString());
            DosisPre.setText(tbObat.getValueAt(tbObat.getSelectedRow(),20).toString());
            DosisIntra.setText(tbObat.getValueAt(tbObat.getSelectedRow(),21).toString());
            DosisPost.setText(tbObat.getValueAt(tbObat.getSelectedRow(),22).toString());
            Komplikasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),23).toString());
            Intake.setText(tbObat.getValueAt(tbObat.getSelectedRow(),24).toString());
            Output.setText(tbObat.getValueAt(tbObat.getSelectedRow(),25).toString());
            InstruksiPaska.setText(tbObat.getValueAt(tbObat.getSelectedRow(),26).toString());
            MasalahKeperawatan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),27).toString());
            RencanaKeperawatan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),28).toString());
            KeputusanRawat.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),29).toString());
            
            Valid.SetTgl(Tanggal,tbObat.getValueAt(tbObat.getSelectedRow(),7).toString());
        }
    }

    
    private void isRawat() {
        try {
            koneksi=koneksiDB.condb();
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,"+
                    "reg_periksa.tgl_registrasi,reg_periksa.jam_reg,reg_periksa.umurdaftar,reg_periksa.sttsumur "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1,TNoRw.getText());
                rs=ps.executeQuery();
                if(rs.next()){
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    TanggalRegistrasi.setText(rs.getString("tgl_registrasi")+" "+rs.getString("jam_reg"));
                    Umur.setText(rs.getString("umurdaftar")+" "+rs.getString("sttsumur"));
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }
    }
 
    public void setNoRm(String norwt,Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);    
        isRawat(); 
        ChkInput.setSelected(true);
        isForm();
        runBackground(() ->tampil());
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            if(internalFrame1.getHeight()>598){
                ChkInput.setVisible(false);
                PanelInput.setPreferredSize(new Dimension(WIDTH,426));
                FormInput.setVisible(true);      
                ChkInput.setVisible(true);
            }else{
                ChkInput.setVisible(false);
                PanelInput.setPreferredSize(new Dimension(WIDTH,internalFrame1.getHeight()-175));
                FormInput.setVisible(true);      
                ChkInput.setVisible(true);
            }
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
    }
    
    public void isCek(){
        BtnSimpan.setEnabled(true);
        BtnHapus.setEnabled(true);
        BtnEdit.setEnabled(true);
        BtnPrint.setEnabled(true); 
        if(akses.getjml2()>=1){
            KdPetugas.setEditable(false);
            BtnPetugas.setEnabled(false);
            KdPetugas.setText(akses.getkode());
            NmPetugas.setText(Sequel.CariPetugas(KdPetugas.getText()));
            if(NmPetugas.getText().equals("")){
                KdPetugas.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan petugas...!!");
            }
        }  
        
        if(TANGGALMUNDUR.equals("no")){
            if(!akses.getkode().equals("Admin Utama")){
                Tanggal.setEditable(false);
                Tanggal.setEnabled(false);
                ChkKejadian.setEnabled(false);
                Jam.setEnabled(false);
                Menit.setEnabled(false);
                Detik.setEnabled(false);
            }
        }
    }

    private void jam(){
        ActionListener taskPerformer = new ActionListener(){
            private int nilai_jam;
            private int nilai_menit;
            private int nilai_detik;
            public void actionPerformed(ActionEvent e) {
                String nol_jam = "";
                String nol_menit = "";
                String nol_detik = "";
                
                Date now = Calendar.getInstance().getTime();

                // Mengambil nilaj JAM, MENIT, dan DETIK Sekarang
                if(ChkKejadian.isSelected()==true){
                    nilai_jam = now.getHours();
                    nilai_menit = now.getMinutes();
                    nilai_detik = now.getSeconds();
                }else if(ChkKejadian.isSelected()==false){
                    nilai_jam =Jam.getSelectedIndex();
                    nilai_menit =Menit.getSelectedIndex();
                    nilai_detik =Detik.getSelectedIndex();
                }

                // Jika nilai JAM lebih kecil dari 10 (hanya 1 digit)
                if (nilai_jam <= 9) {
                    // Tambahkan "0" didepannya
                    nol_jam = "0";
                }
                // Jika nilai MENIT lebih kecil dari 10 (hanya 1 digit)
                if (nilai_menit <= 9) {
                    // Tambahkan "0" didepannya
                    nol_menit = "0";
                }
                // Jika nilai DETIK lebih kecil dari 10 (hanya 1 digit)
                if (nilai_detik <= 9) {
                    // Tambahkan "0" didepannya
                    nol_detik = "0";
                }
                // Membuat String JAM, MENIT, DETIK
                String jam = nol_jam + Integer.toString(nilai_jam);
                String menit = nol_menit + Integer.toString(nilai_menit);
                String detik = nol_detik + Integer.toString(nilai_detik);
                // Menampilkan pada Layar
                //tampil_jam.setText("  " + jam + " : " + menit + " : " + detik + "  ");
                Jam.setSelectedItem(jam);
                Menit.setSelectedItem(menit);
                Detik.setSelectedItem(detik);
            }
        };
        // Timer
        new Timer(1000, taskPerformer).start();
    }

    
    private void ganti() {
        if(Sequel.mengedittf("asesmen_keperawatan_invasif_non_bedah","no_rawat=? and tanggal=?","jam_tiba_rr=?,lokasi_pungsi=?,jumlah_pungsi=?,terpasang_sheath=?,lokasi_sheath=?,pulsasi_radialis_kanan=?,pulsasi_radialis_kiri=?,pulsasi_dorsalis_kanan=?,pulsasi_dorsalis_kiri=?,alat_terpasang=?,jenis_anestesi=?,sedasi=?,antikoagulan_dosis_pre=?,antikoagulan_dosis_intra=?,antikoagulan_dosis_post=?,komplikasi=?,balance_cairan_intake=?,balance_cairan_output=?,instruksi_paska_tindakan=?,masalah_keperawatan=?,rencana_keperawatan=?,keputusan_rawat=?,nip=?",25,new String[]{
            JamTiba.getText(),LokasiPungsi.getText(),JumlahPungsi.getText(),TerpasangSheath.getSelectedItem().toString(),LokasiSheath.getText(),
            PulsasiRadialisKanan.getText(),PulsasiRadialisKiri.getText(),PulsasiDorsalisKanan.getText(),PulsasiDorsalisKiri.getText(),
            AlatTerpasang.getText(),JenisAnestesi.getText(),Sedasi.getText(),DosisPre.getText(),DosisIntra.getText(),DosisPost.getText(),
            Komplikasi.getText(),Intake.getText(),Output.getText(),InstruksiPaska.getText(),MasalahKeperawatan.getText(),
            RencanaKeperawatan.getText(),KeputusanRawat.getSelectedItem().toString(),KdPetugas.getText(),
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),7).toString()
        })){
            tampil();
            emptTeks();
        }
    }


    private void hapus() {
        if(Sequel.queryu2tf("delete from asesmen_keperawatan_invasif_non_bedah where no_rawat=?",1,new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })==true){
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText(""+tabMode.getRowCount());
            emptTeks();
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
        }
    }

    private void isTotal() {
        try {
            TotalHasil.setText(""+(Integer.parseInt(NilaiPertanyaanAwal1.getText())+Integer.parseInt(NilaiPertanyaanAwal2.getText())+Integer.parseInt(NilaiPertanyaanLanjutan1.getText())+Integer.parseInt(NilaiPertanyaanLanjutan2.getText())+Integer.parseInt(NilaiPertanyaanLanjutan3.getText())+Integer.parseInt(NilaiPertanyaanLanjutan4.getText())+Integer.parseInt(NilaiPertanyaanLanjutan5.getText())+Integer.parseInt(NilaiPertanyaanLanjutan6.getText())));
            if(Integer.parseInt(TotalHasil.getText())>12){
                HasilSkrining.setText("Pasien Terindikasi Mengalami Kekerasan");
            }else{
                HasilSkrining.setText("Pasien Tidak Terindikasi Mengalami Kekerasan");
            }
        } catch (Exception e) {
            HasilSkrining.setText("Pasien Tidak Terindikasi Mengalami Kekerasan");
        }
    }

    
    private void simpan() {
        if(Sequel.menyimpantf("asesmen_keperawatan_invasif_non_bedah","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","Data",25,new String[]{
            TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem(),
            JamTiba.getText(),LokasiPungsi.getText(),JumlahPungsi.getText(),TerpasangSheath.getSelectedItem().toString(),LokasiSheath.getText(),
            PulsasiRadialisKanan.getText(),PulsasiRadialisKiri.getText(),PulsasiDorsalisKanan.getText(),PulsasiDorsalisKiri.getText(),
            AlatTerpasang.getText(),JenisAnestesi.getText(),Sedasi.getText(),DosisPre.getText(),DosisIntra.getText(),DosisPost.getText(),
            Komplikasi.getText(),Intake.getText(),Output.getText(),InstruksiPaska.getText(),MasalahKeperawatan.getText(),
            RencanaKeperawatan.getText(),KeputusanRawat.getSelectedItem().toString(),KdPetugas.getText()
        })==true){
            tampil();
            emptTeks();
        }
    }

    
    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) return;

        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            ceksukses = false;
        }
    }
    
    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }

    private widget.TextBox JamTiba;
    private widget.TextBox LokasiPungsi;
    private widget.TextBox JumlahPungsi;
    private widget.TextBox LokasiSheath;
    private widget.TextBox PulsasiRadialisKanan;
    private widget.TextBox PulsasiRadialisKiri;
    private widget.TextBox PulsasiDorsalisKanan;
    private widget.TextBox PulsasiDorsalisKiri;
    private widget.TextBox AlatTerpasang;
    private widget.TextBox JenisAnestesi;
    private widget.TextBox Sedasi;
    private widget.TextBox DosisPre;
    private widget.TextBox DosisIntra;
    private widget.TextBox DosisPost;
    private widget.TextBox Komplikasi;
    private widget.TextBox Intake;
    private widget.TextBox Output;
    private widget.TextBox InstruksiPaska;
    private widget.TextBox MasalahKeperawatan;
    private widget.TextBox RencanaKeperawatan;
    private widget.ComboBox TerpasangSheath;
    private widget.ComboBox KeputusanRawat;

}