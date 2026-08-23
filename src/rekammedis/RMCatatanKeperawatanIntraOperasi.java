/*
 * Catatan Keperawatan Intra Operasi (RM 22.OK-02.02 + balutan/spesimen RM 22.OK-02.03)
 * Diisi oleh staf perawat ruang operasi : posisi operasi, kateter, diathermy,
 * tourniquet, laser, implant, drain, cairan irigasi, balutan, dan spesimen.
 */

package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariPetugas;

/**
 *
 * @author perpustakaan
 */
public final class RMCatatanKeperawatanIntraOperasi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private String norawatPilih="";
    private DlgCariPetugas petugas;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private int i=0;

    /** Creates new form RMCatatanKeperawatanIntraOperasi
     * @param parent
     * @param modal */
    public RMCatatanKeperawatanIntraOperasi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        tabMode=new DefaultTableModel(null,new String[]{
            "No.Rawat","No.RM","Nama Pasien","Tanggal","Tindakan Operasi","Perawat Sirkuler"}){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbData.setModel(tabMode);
        tbData.setPreferredScrollableViewportSize(new Dimension(500,400));
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 6; i++) {
            TableColumn column = tbData.getColumnModel().getColumn(i);
            if(i==0||i==1){
                column.setPreferredWidth(115);
            }else if(i==2||i==4){
                column.setPreferredWidth(180);
            }else{
                column.setPreferredWidth(130);
            }
        }
        tbData.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        JamMulai.setDocument(new batasInput((int)8).getKata(JamMulai));
        JamSelesai.setDocument(new batasInput((int)8).getKata(JamSelesai));
        TindakanOperasi.setDocument(new batasInput((int)200).getKata(TindakanOperasi));
        GcsE.setDocument(new batasInput((int)5).getKata(GcsE));
        GcsM.setDocument(new batasInput((int)5).getKata(GcsM));
        GcsV.setDocument(new batasInput((int)5).getKata(GcsV));
        KanulaKet.setDocument(new batasInput((int)100).getKata(KanulaKet));
        PosisiDiawasi.setDocument(new batasInput((int)100).getKata(PosisiDiawasi));
        SkorBraden.setDocument(new batasInput((int)20).getKata(SkorBraden));
        KateterOleh.setDocument(new batasInput((int)100).getKata(KateterOleh));
        JenisKateter.setDocument(new batasInput((int)60).getKata(JenisKateter));
        NoKateter.setDocument(new batasInput((int)15).getKata(NoKateter));
        IsiBalon.setDocument(new batasInput((int)15).getKata(IsiBalon));
        PersiapanKulitKet.setDocument(new batasInput((int)100).getKata(PersiapanKulitKet));
        ElektrodaOleh.setDocument(new batasInput((int)100).getKata(ElektrodaOleh));
        KodeElektrosurgical.setDocument(new batasInput((int)50).getKata(KodeElektrosurgical));
        SuhuPemanas.setDocument(new batasInput((int)15).getKata(SuhuPemanas));
        TorniketOleh.setDocument(new batasInput((int)100).getKata(TorniketOleh));
        TorLkaMulai.setDocument(new batasInput((int)8).getKata(TorLkaMulai));
        TorLkaSelesai.setDocument(new batasInput((int)8).getKata(TorLkaSelesai));
        TorLkaTekanan.setDocument(new batasInput((int)15).getKata(TorLkaTekanan));
        TorLkiMulai.setDocument(new batasInput((int)8).getKata(TorLkiMulai));
        TorLkiSelesai.setDocument(new batasInput((int)8).getKata(TorLkiSelesai));
        TorLkiTekanan.setDocument(new batasInput((int)15).getKata(TorLkiTekanan));
        TorPkaMulai.setDocument(new batasInput((int)8).getKata(TorPkaMulai));
        TorPkaSelesai.setDocument(new batasInput((int)8).getKata(TorPkaSelesai));
        TorPkaTekanan.setDocument(new batasInput((int)15).getKata(TorPkaTekanan));
        TorPkiMulai.setDocument(new batasInput((int)8).getKata(TorPkiMulai));
        TorPkiSelesai.setDocument(new batasInput((int)8).getKata(TorPkiSelesai));
        TorPkiTekanan.setDocument(new batasInput((int)15).getKata(TorPkiTekanan));
        LaserDiawasi.setDocument(new batasInput((int)100).getKata(LaserDiawasi));
        Implant.setDocument(new batasInput((int)300).getKata(Implant));
        JenisDrain.setDocument(new batasInput((int)100).getKata(JenisDrain));
        CairanGlycine.setDocument(new batasInput((int)15).getKata(CairanGlycine));
        CairanWfi.setDocument(new batasInput((int)15).getKata(CairanWfi));
        CairanNacl.setDocument(new batasInput((int)15).getKata(CairanNacl));
        CairanBss.setDocument(new batasInput((int)15).getKata(CairanBss));
        CairanLain.setDocument(new batasInput((int)100).getKata(CairanLain));
        JenisBalutan.setDocument(new batasInput((int)100).getKata(JenisBalutan));
        SpesimenKet.setDocument(new batasInput((int)200).getKata(SpesimenKet));
        JumlahTotalJaringan.setDocument(new batasInput((int)100).getKata(JumlahTotalJaringan));
        JenisJaringan.setDocument(new batasInput((int)100).getKata(JenisJaringan));
        JumlahJaringan.setDocument(new batasInput((int)60).getKata(JumlahJaringan));
        NamaPenerima.setDocument(new batasInput((int)100).getKata(NamaPenerima));
        Keterangan.setDocument(new batasInput((int)400).getKata(Keterangan));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
    }

    private widget.ComboBox combo(String... isi){
        widget.ComboBox c=new widget.ComboBox();
        c.setModel(new javax.swing.DefaultComboBoxModel(isi));
        c.setName("combo");
        return c;
    }

    private widget.Label labelKiri(String teks, int x, int y, int w){
        widget.Label l=new widget.Label();
        l.setText(teks);
        FormInput.add(l);
        l.setBounds(x, y, w, 23);
        return l;
    }

    private widget.Label labelSeksi(String teks, int y){
        widget.Label l=new widget.Label();
        l.setText(teks);
        l.setFont(new java.awt.Font("Tahoma", 1, 11));
        l.setForeground(new java.awt.Color(50, 70, 50));
        FormInput.add(l);
        l.setBounds(0, y, 724, 23);
        return l;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetakFormulir = new javax.swing.JMenuItem();
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        jLabel10 = new widget.Label();
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        label11 = new widget.Label();
        Tanggal = new widget.Tanggal();
        jSeparator1 = new javax.swing.JSeparator();
        KdPerawatSteril = new widget.TextBox();
        NmPerawatSteril = new widget.TextBox();
        BtnPerawatSteril = new widget.Button();
        KdPerawatSirkuler = new widget.TextBox();
        NmPerawatSirkuler = new widget.TextBox();
        BtnPerawatSirkuler = new widget.Button();
        JamMulai = new widget.TextBox();
        JamSelesai = new widget.TextBox();
        TindakanOperasi = new widget.TextBox();
        TipeOperasi = combo("Elektif","Darurat","Operasi Pulang Hari");
        TipePembiusan = combo("Umum","Lokal","Regional");
        GcsE = new widget.TextBox();
        GcsM = new widget.TextBox();
        GcsV = new widget.TextBox();
        StatusEmosi = combo("Tenang","Gelisah","Tidak Ada Respon");
        PosisiKanula = combo("Tangan Kanan","Tangan Kiri","Kaki Kanan","Kaki Kiri","Arteri Line","CVC","Lain-lain");
        KanulaKet = new widget.TextBox();
        PosisiOperasi = combo("Supine","Lithotomi","Prone","Lateral Kanan","Lateral Kiri","Lain-lain");
        PosisiDiawasi = new widget.TextBox();
        RisikoLukaTekan = combo("Rendah","Sedang","Tinggi");
        SkorBraden = new widget.TextBox();
        StockingVte = combo("Tidak","Ya");
        PosisiLengan = combo("Terentang Ki/Ka","Terentang Kanan","Terentang Kiri","Terlipat Ki/Ka","Terlipat Kanan","Terlipat Kiri");
        AlatBantu = combo("Tidak Ada","Penyanggah Lengan","Penyanggah Kaki/Sanggundi","Penyanggah Lengan & Kaki");
        Kateter = combo("Tidak","Diruangan","Di Ruang Operasi");
        KateterOleh = new widget.TextBox();
        JenisKateter = new widget.TextBox();
        NoKateter = new widget.TextBox();
        IsiBalon = new widget.TextBox();
        KateterArah = combo("Ke Urimeter","Kantong Urine Tertutup");
        KateterTraksi = combo("Tidak","Ya");
        PersiapanKulit = combo("Povidone Iodine 10%","Chlorhexidine Gluconate 2%","Chlorhexidine Gluconate 4%","Povidone Alkohol 1%","Lain-lain");
        PersiapanKulitKet = new widget.TextBox();
        Diathermy = combo("Tidak","Monopolar","Bipolar","Lain-lain");
        ElektrodaLokasi = combo("Bokong Kanan","Bokong Kiri","Paha Kanan","Paha Kiri","Lain-lain");
        ElektrodaOleh = new widget.TextBox();
        KulitSebelum = combo("Utuh","Kemerahan","Bula","Lain-lain");
        KulitSesudah = combo("Utuh","Kemerahan","Bula","Lain-lain");
        KodeElektrosurgical = new widget.TextBox();
        UnitPemanas = combo("Tidak","Ya");
        SuhuPemanas = new widget.TextBox();
        TorniketOleh = new widget.TextBox();
        TorLkaMulai = new widget.TextBox();
        TorLkaSelesai = new widget.TextBox();
        TorLkaTekanan = new widget.TextBox();
        TorLkiMulai = new widget.TextBox();
        TorLkiSelesai = new widget.TextBox();
        TorLkiTekanan = new widget.TextBox();
        TorPkaMulai = new widget.TextBox();
        TorPkaSelesai = new widget.TextBox();
        TorPkaTekanan = new widget.TextBox();
        TorPkiMulai = new widget.TextBox();
        TorPkiSelesai = new widget.TextBox();
        TorPkiTekanan = new widget.TextBox();
        Laser = combo("Tidak","Ya");
        LaserDiawasi = new widget.TextBox();
        Implant = new widget.TextBox();
        Drain = combo("Tidak","Ya");
        JenisDrain = new widget.TextBox();
        SifatDrain = combo("Aktif (Vacum)","Pasif");
        CairanGlycine = new widget.TextBox();
        CairanWfi = new widget.TextBox();
        CairanNacl = new widget.TextBox();
        CairanBss = new widget.TextBox();
        CairanLain = new widget.TextBox();
        Balutan = combo("Tidak Ada","Ada");
        JenisBalutan = new widget.TextBox();
        Spesimen = combo("Tidak Ada","Histology","Cytology","Kulture","Frozen Section","Lain-lain");
        SpesimenKet = new widget.TextBox();
        JumlahTotalJaringan = new widget.TextBox();
        SpesimenDiberikan = combo("Tidak","Ya");
        JenisJaringan = new widget.TextBox();
        JumlahJaringan = new widget.TextBox();
        NamaPenerima = new widget.TextBox();
        Keterangan = new widget.TextBox();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbData = new widget.Table();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();

        jPopupMenu1.setName("jPopupMenu1");
        MnCetakFormulir.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakFormulir.setFont(new java.awt.Font("Tahoma", 0, 11));
        MnCetakFormulir.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakFormulir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png")));
        MnCetakFormulir.setText("Formulir Catatan Keperawatan Intra Operasi");
        MnCetakFormulir.setName("MnCetakFormulir");
        MnCetakFormulir.setPreferredSize(new java.awt.Dimension(340, 26));
        MnCetakFormulir.addActionListener(evt -> {
            String nr=norawatPilih;
            if(nr==null || nr.isEmpty()){
                if(tbData.getSelectedRow()>-1){
                    nr=tbData.getValueAt(tbData.getSelectedRow(),0).toString();
                }
            }
            if(nr!=null && !nr.isEmpty()){
                cetakNoRawat(nr);
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih dulu data yang mau dicetak...!!");
            }
        });
        jPopupMenu1.add(MnCetakFormulir);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Catatan Keperawatan Intra Operasi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
        internalFrame1.setName("internalFrame1");
        internalFrame1.setPreferredSize(new java.awt.Dimension(467, 500));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8");
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan");
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(evt -> simpan());
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png")));
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal");
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(evt -> emptTeks());
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus");
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(evt -> hapus());
        panelGlass8.add(BtnHapus);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint");
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(evt -> cetak());
        panelGlass8.add(BtnPrint);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll");
        BtnAll.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnAll.addActionListener(evt -> {
            TCari.setText("");
            runBackground(() ->tampil());
        });
        panelGlass8.add(BtnAll);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar");
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(evt -> dispose());
        panelGlass8.add(BtnKeluar);

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11));
        TabRawat.setName("TabRawat");
        TabRawat.setPreferredSize(new java.awt.Dimension(457, 480));

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2");
        internalFrame2.setPreferredSize(new java.awt.Dimension(102, 480));
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput");
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 557));
        scrollInput.getVerticalScrollBar().setUnitIncrement(16);

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput");
        FormInput.setPreferredSize(new java.awt.Dimension(750, 2130));
        FormInput.setLayout(null);

        jLabel10.setText("No.Rawat :");
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 10, 70, 23);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw");
        FormInput.add(TNoRw);
        TNoRw.setBounds(74, 10, 131, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM");
        FormInput.add(TNoRM);
        TNoRM.setBounds(207, 10, 100, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien");
        FormInput.add(TPasien);
        TPasien.setBounds(309, 10, 260, 23);

        jLabel8.setText("Tgl.Lahir :");
        FormInput.add(jLabel8);
        jLabel8.setBounds(580, 10, 60, 23);

        TglLahir.setEditable(false);
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir");
        FormInput.add(TglLahir);
        TglLahir.setBounds(644, 10, 80, 23);

        labelKiri("Perawat Steril :", 0, 40, 100);
        KdPerawatSteril.setEditable(false);
        KdPerawatSteril.setName("KdPerawatSteril");
        FormInput.add(KdPerawatSteril);
        KdPerawatSteril.setBounds(104, 40, 90, 23);
        NmPerawatSteril.setEditable(false);
        NmPerawatSteril.setName("NmPerawatSteril");
        FormInput.add(NmPerawatSteril);
        NmPerawatSteril.setBounds(196, 40, 255, 23);
        BtnPerawatSteril.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnPerawatSteril.setToolTipText("Cari perawat steril");
        BtnPerawatSteril.setName("BtnPerawatSteril");
        BtnPerawatSteril.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPerawatSteril.addActionListener(evt -> BtnPerawatSterilActionPerformed(evt));
        FormInput.add(BtnPerawatSteril);
        BtnPerawatSteril.setBounds(453, 40, 28, 23);

        label11.setText("Tanggal :");
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label11);
        label11.setBounds(538, 40, 52, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "14-07-2026 08:00:00" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        Tanggal.setName("Tanggal");
        Tanggal.setOpaque(false);
        FormInput.add(Tanggal);
        Tanggal.setBounds(594, 40, 130, 23);

        labelKiri("Perawat Sirkuler :", 0, 70, 100);
        KdPerawatSirkuler.setEditable(false);
        KdPerawatSirkuler.setName("KdPerawatSirkuler");
        FormInput.add(KdPerawatSirkuler);
        KdPerawatSirkuler.setBounds(104, 70, 90, 23);
        NmPerawatSirkuler.setEditable(false);
        NmPerawatSirkuler.setName("NmPerawatSirkuler");
        FormInput.add(NmPerawatSirkuler);
        NmPerawatSirkuler.setBounds(196, 70, 255, 23);
        BtnPerawatSirkuler.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnPerawatSirkuler.setToolTipText("Cari perawat sirkuler");
        BtnPerawatSirkuler.setName("BtnPerawatSirkuler");
        BtnPerawatSirkuler.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPerawatSirkuler.addActionListener(evt -> BtnPerawatSirkulerActionPerformed(evt));
        FormInput.add(BtnPerawatSirkuler);
        BtnPerawatSirkuler.setBounds(453, 70, 28, 23);

        jSeparator1.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator1.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(0, 100, 750, 1);

        labelSeksi("IDENTITAS OPERASI", 115);
        labelKiri("Jam mulai (hh:mm) :", 0, 145, 420);
        JamMulai.setName("JamMulai");
        FormInput.add(JamMulai);
        JamMulai.setBounds(424, 145, 100, 23);
        labelKiri("Jam selesai (hh:mm) :", 530, 145, 120);
        JamSelesai.setName("JamSelesai");
        FormInput.add(JamSelesai);
        JamSelesai.setBounds(654, 145, 70, 23);
        labelKiri("Tindakan operasi :", 0, 175, 420);
        TindakanOperasi.setName("TindakanOperasi");
        FormInput.add(TindakanOperasi);
        TindakanOperasi.setBounds(424, 175, 300, 23);
        labelKiri("Tipe operasi :", 0, 205, 420);
        FormInput.add(TipeOperasi);
        TipeOperasi.setBounds(424, 205, 300, 23);
        labelKiri("Tipe pembiusan :", 0, 235, 420);
        FormInput.add(TipePembiusan);
        TipePembiusan.setBounds(424, 235, 300, 23);
        labelKiri("Tingkat kesadaran masuk ruang operasi, GCS  E / M / V :", 0, 265, 420);
        GcsE.setName("GcsE");
        FormInput.add(GcsE);
        GcsE.setBounds(424, 265, 60, 23);
        GcsM.setName("GcsM");
        FormInput.add(GcsM);
        GcsM.setBounds(490, 265, 60, 23);
        GcsV.setName("GcsV");
        FormInput.add(GcsV);
        GcsV.setBounds(556, 265, 60, 23);
        labelKiri("Status emosi saat masuk ruang operasi :", 0, 295, 420);
        FormInput.add(StatusEmosi);
        StatusEmosi.setBounds(424, 295, 300, 23);

        labelSeksi("POSISI & PERSIAPAN PASIEN", 330);
        labelKiri("Posisi kanula infus :", 0, 360, 420);
        FormInput.add(PosisiKanula);
        PosisiKanula.setBounds(424, 360, 300, 23);
        labelKiri("Keterangan kanula (jika lain-lain/CVC) :", 0, 390, 420);
        KanulaKet.setName("KanulaKet");
        FormInput.add(KanulaKet);
        KanulaKet.setBounds(424, 390, 300, 23);
        labelKiri("Posisi operasi :", 0, 420, 420);
        FormInput.add(PosisiOperasi);
        PosisiOperasi.setBounds(424, 420, 300, 23);
        labelKiri("Posisi diawasi oleh :", 0, 450, 420);
        PosisiDiawasi.setName("PosisiDiawasi");
        FormInput.add(PosisiDiawasi);
        PosisiDiawasi.setBounds(424, 450, 300, 23);
        labelKiri("Risiko luka tekan :", 0, 480, 420);
        FormInput.add(RisikoLukaTekan);
        RisikoLukaTekan.setBounds(424, 480, 300, 23);
        labelKiri("Skor Braden / Glamorgan :", 0, 510, 420);
        SkorBraden.setName("SkorBraden");
        FormInput.add(SkorBraden);
        SkorBraden.setBounds(424, 510, 300, 23);
        labelKiri("Pemakaian stocking anti-VTE :", 0, 540, 420);
        FormInput.add(StockingVte);
        StockingVte.setBounds(424, 540, 300, 23);
        labelKiri("Posisi lengan :", 0, 570, 420);
        FormInput.add(PosisiLengan);
        PosisiLengan.setBounds(424, 570, 300, 23);
        labelKiri("Posisi alat bantu yang digunakan :", 0, 600, 420);
        FormInput.add(AlatBantu);
        AlatBantu.setBounds(424, 600, 300, 23);

        labelSeksi("PEMAKAIAN KATETER URINE", 635);
        labelKiri("Pemakaian kateter urine :", 0, 665, 420);
        FormInput.add(Kateter);
        Kateter.setBounds(424, 665, 300, 23);
        labelKiri("Dipasang oleh :", 0, 695, 420);
        KateterOleh.setName("KateterOleh");
        FormInput.add(KateterOleh);
        KateterOleh.setBounds(424, 695, 300, 23);
        labelKiri("Jenis kateter :", 0, 725, 420);
        JenisKateter.setName("JenisKateter");
        FormInput.add(JenisKateter);
        JenisKateter.setBounds(424, 725, 300, 23);
        labelKiri("No. kateter :", 0, 755, 420);
        NoKateter.setName("NoKateter");
        FormInput.add(NoKateter);
        NoKateter.setBounds(424, 755, 300, 23);
        labelKiri("Isi balon (ml) :", 0, 785, 420);
        IsiBalon.setName("IsiBalon");
        FormInput.add(IsiBalon);
        IsiBalon.setBounds(424, 785, 300, 23);
        labelKiri("Aliran urine :", 0, 815, 420);
        FormInput.add(KateterArah);
        KateterArah.setBounds(424, 815, 300, 23);
        labelKiri("Kateter ditraksi ke paha pasien (irigasi blass) :", 0, 845, 420);
        FormInput.add(KateterTraksi);
        KateterTraksi.setBounds(424, 845, 300, 23);

        labelSeksi("PERSIAPAN KULIT & DIATHERMY", 880);
        labelKiri("Persiapan kulit :", 0, 910, 420);
        FormInput.add(PersiapanKulit);
        PersiapanKulit.setBounds(424, 910, 300, 23);
        labelKiri("Keterangan persiapan kulit (jika lain-lain) :", 0, 940, 420);
        PersiapanKulitKet.setName("PersiapanKulitKet");
        FormInput.add(PersiapanKulitKet);
        PersiapanKulitKet.setBounds(424, 940, 300, 23);
        labelKiri("Pemakaian diathermy :", 0, 970, 420);
        FormInput.add(Diathermy);
        Diathermy.setBounds(424, 970, 300, 23);
        labelKiri("Lokasi elektroda :", 0, 1000, 420);
        FormInput.add(ElektrodaLokasi);
        ElektrodaLokasi.setBounds(424, 1000, 300, 23);
        labelKiri("Elektroda dipasang oleh :", 0, 1030, 420);
        ElektrodaOleh.setName("ElektrodaOleh");
        FormInput.add(ElektrodaOleh);
        ElektrodaOleh.setBounds(424, 1030, 300, 23);
        labelKiri("Kondisi kulit sebelum operasi :", 0, 1060, 420);
        FormInput.add(KulitSebelum);
        KulitSebelum.setBounds(424, 1060, 300, 23);
        labelKiri("Kondisi kulit sesudah operasi :", 0, 1090, 420);
        FormInput.add(KulitSesudah);
        KulitSesudah.setBounds(424, 1090, 300, 23);
        labelKiri("Kode unit elektrosurgical :", 0, 1120, 420);
        KodeElektrosurgical.setName("KodeElektrosurgical");
        FormInput.add(KodeElektrosurgical);
        KodeElektrosurgical.setBounds(424, 1120, 300, 23);
        labelKiri("Unit pemanas/pendingin operasi :", 0, 1150, 420);
        FormInput.add(UnitPemanas);
        UnitPemanas.setBounds(424, 1150, 300, 23);
        labelKiri("Pengaturan temperatur (°C) :", 0, 1180, 420);
        SuhuPemanas.setName("SuhuPemanas");
        FormInput.add(SuhuPemanas);
        SuhuPemanas.setBounds(424, 1180, 300, 23);

        labelSeksi("PEMAKAIAN TOURNIQUET  (jam mulai - jam selesai - tekanan mmHg)", 1215);
        labelKiri("Dipasang oleh :", 0, 1245, 420);
        TorniketOleh.setName("TorniketOleh");
        FormInput.add(TorniketOleh);
        TorniketOleh.setBounds(424, 1245, 300, 23);
        labelKiri("Lengan kanan :", 0, 1275, 420);
        TorLkaMulai.setName("TorLkaMulai");
        FormInput.add(TorLkaMulai);
        TorLkaMulai.setBounds(424, 1275, 95, 23);
        TorLkaSelesai.setName("TorLkaSelesai");
        FormInput.add(TorLkaSelesai);
        TorLkaSelesai.setBounds(525, 1275, 95, 23);
        TorLkaTekanan.setName("TorLkaTekanan");
        FormInput.add(TorLkaTekanan);
        TorLkaTekanan.setBounds(626, 1275, 98, 23);
        labelKiri("Lengan kiri :", 0, 1305, 420);
        TorLkiMulai.setName("TorLkiMulai");
        FormInput.add(TorLkiMulai);
        TorLkiMulai.setBounds(424, 1305, 95, 23);
        TorLkiSelesai.setName("TorLkiSelesai");
        FormInput.add(TorLkiSelesai);
        TorLkiSelesai.setBounds(525, 1305, 95, 23);
        TorLkiTekanan.setName("TorLkiTekanan");
        FormInput.add(TorLkiTekanan);
        TorLkiTekanan.setBounds(626, 1305, 98, 23);
        labelKiri("Paha kanan :", 0, 1335, 420);
        TorPkaMulai.setName("TorPkaMulai");
        FormInput.add(TorPkaMulai);
        TorPkaMulai.setBounds(424, 1335, 95, 23);
        TorPkaSelesai.setName("TorPkaSelesai");
        FormInput.add(TorPkaSelesai);
        TorPkaSelesai.setBounds(525, 1335, 95, 23);
        TorPkaTekanan.setName("TorPkaTekanan");
        FormInput.add(TorPkaTekanan);
        TorPkaTekanan.setBounds(626, 1335, 98, 23);
        labelKiri("Paha kiri :", 0, 1365, 420);
        TorPkiMulai.setName("TorPkiMulai");
        FormInput.add(TorPkiMulai);
        TorPkiMulai.setBounds(424, 1365, 95, 23);
        TorPkiSelesai.setName("TorPkiSelesai");
        FormInput.add(TorPkiSelesai);
        TorPkiSelesai.setBounds(525, 1365, 95, 23);
        TorPkiTekanan.setName("TorPkiTekanan");
        FormInput.add(TorPkiTekanan);
        TorPkiTekanan.setBounds(626, 1365, 98, 23);

        labelSeksi("LASER, IMPLANT, DRAIN & CAIRAN", 1400);
        labelKiri("Pemakaian laser :", 0, 1430, 420);
        FormInput.add(Laser);
        Laser.setBounds(424, 1430, 300, 23);
        labelKiri("Laser diawasi oleh :", 0, 1460, 420);
        LaserDiawasi.setName("LaserDiawasi");
        FormInput.add(LaserDiawasi);
        LaserDiawasi.setBounds(424, 1460, 300, 23);
        labelKiri("Pemakaian implant (label implant & manufacture) :", 0, 1490, 420);
        Implant.setName("Implant");
        FormInput.add(Implant);
        Implant.setBounds(424, 1490, 300, 23);
        labelKiri("Pemakaian drain :", 0, 1520, 420);
        FormInput.add(Drain);
        Drain.setBounds(424, 1520, 300, 23);
        labelKiri("Jenis drain :", 0, 1550, 420);
        JenisDrain.setName("JenisDrain");
        FormInput.add(JenisDrain);
        JenisDrain.setBounds(424, 1550, 300, 23);
        labelKiri("Sifat drain :", 0, 1580, 420);
        FormInput.add(SifatDrain);
        SifatDrain.setBounds(424, 1580, 300, 23);
        labelKiri("Cairan Glycine (ml) :", 0, 1610, 420);
        CairanGlycine.setName("CairanGlycine");
        FormInput.add(CairanGlycine);
        CairanGlycine.setBounds(424, 1610, 300, 23);
        labelKiri("Water for irigation (ml) :", 0, 1640, 420);
        CairanWfi.setName("CairanWfi");
        FormInput.add(CairanWfi);
        CairanWfi.setBounds(424, 1640, 300, 23);
        labelKiri("Sodium Chloride 0,9% (ml) :", 0, 1670, 420);
        CairanNacl.setName("CairanNacl");
        FormInput.add(CairanNacl);
        CairanNacl.setBounds(424, 1670, 300, 23);
        labelKiri("BSS/Balanced Salt Solution, khusus operasi mata (ml) :", 0, 1700, 420);
        CairanBss.setName("CairanBss");
        FormInput.add(CairanBss);
        CairanBss.setBounds(424, 1700, 300, 23);
        labelKiri("Cairan lain-lain (jenis & ml) :", 0, 1730, 420);
        CairanLain.setName("CairanLain");
        FormInput.add(CairanLain);
        CairanLain.setBounds(424, 1730, 300, 23);

        labelSeksi("BALUTAN & SPESIMEN", 1765);
        labelKiri("Balutan :", 0, 1795, 420);
        FormInput.add(Balutan);
        Balutan.setBounds(424, 1795, 300, 23);
        labelKiri("Jenis balutan :", 0, 1825, 420);
        JenisBalutan.setName("JenisBalutan");
        FormInput.add(JenisBalutan);
        JenisBalutan.setBounds(424, 1825, 300, 23);
        labelKiri("Spesimen :", 0, 1855, 420);
        FormInput.add(Spesimen);
        Spesimen.setBounds(424, 1855, 300, 23);
        labelKiri("Keterangan spesimen :", 0, 1885, 420);
        SpesimenKet.setName("SpesimenKet");
        FormInput.add(SpesimenKet);
        SpesimenKet.setBounds(424, 1885, 300, 23);
        labelKiri("Jumlah total jaringan / cairan pemeriksaan :", 0, 1915, 420);
        JumlahTotalJaringan.setName("JumlahTotalJaringan");
        FormInput.add(JumlahTotalJaringan);
        JumlahTotalJaringan.setBounds(424, 1915, 300, 23);
        labelKiri("Spesimen diberikan kepada pasien/keluarga :", 0, 1945, 420);
        FormInput.add(SpesimenDiberikan);
        SpesimenDiberikan.setBounds(424, 1945, 300, 23);
        labelKiri("Jenis jaringan :", 0, 1975, 420);
        JenisJaringan.setName("JenisJaringan");
        FormInput.add(JenisJaringan);
        JenisJaringan.setBounds(424, 1975, 300, 23);
        labelKiri("Jumlah jaringan :", 0, 2005, 420);
        JumlahJaringan.setName("JumlahJaringan");
        FormInput.add(JumlahJaringan);
        JumlahJaringan.setBounds(424, 2005, 300, 23);
        labelKiri("Nama penerima :", 0, 2035, 420);
        NamaPenerima.setName("NamaPenerima");
        FormInput.add(NamaPenerima);
        NamaPenerima.setBounds(424, 2035, 300, 23);
        labelKiri("Keterangan :", 0, 2065, 420);
        Keterangan.setName("Keterangan");
        FormInput.add(Keterangan);
        Keterangan.setBounds(424, 2065, 300, 23);

        scrollInput.setViewportView(FormInput);
        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);
        TabRawat.addTab("Input Catatan Intra Operasi", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3");
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll");
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbData.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbData.setComponentPopupMenu(jPopupMenu1);
        tbData.setName("tbData");
        tbData.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if(tbData.getSelectedRow()>-1){
                    norawatPilih=tbData.getValueAt(tbData.getSelectedRow(),0).toString();
                    ambilData(norawatPilih);
                    if(evt.getButton()==java.awt.event.MouseEvent.BUTTON1){
                        jPopupMenu1.show(tbData, evt.getX(), evt.getY());
                    }
                }
            }
        });
        Scroll.setViewportView(tbData);
        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9");
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tanggal :");
        jLabel19.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1");
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2");
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6");
        jLabel6.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari");
        TCari.setPreferredSize(new java.awt.Dimension(155, 23));
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setMnemonic('1');
        BtnCari.setToolTipText("Alt+1");
        BtnCari.setName("BtnCari");
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(evt -> runBackground(() ->tampil()));
        panelGlass9.add(BtnCari);

        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);
        TabRawat.addTab("Data Catatan Intra Operasi", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }

    private void BtnPerawatSterilActionPerformed(java.awt.event.ActionEvent evt) {
        if (petugas == null || !petugas.isDisplayable()) {
            petugas=new DlgCariPetugas(null,false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(petugas.getTable().getSelectedRow()!= -1){
                        KdPerawatSteril.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                        NmPerawatSteril.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());
                    }
                    BtnPerawatSteril.requestFocus();
                    petugas=null;
                }
            });
            petugas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            petugas.setLocationRelativeTo(internalFrame1);
        }
        if (petugas == null) return;
        petugas.isCek();
        if (petugas.isVisible()) {
            petugas.toFront();
            return;
        }
        petugas.setVisible(true);
    }

    private void BtnPerawatSirkulerActionPerformed(java.awt.event.ActionEvent evt) {
        if (petugas == null || !petugas.isDisplayable()) {
            petugas=new DlgCariPetugas(null,false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(petugas.getTable().getSelectedRow()!= -1){
                        KdPerawatSirkuler.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                        NmPerawatSirkuler.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());
                    }
                    BtnPerawatSirkuler.requestFocus();
                    petugas=null;
                }
            });
            petugas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            petugas.setLocationRelativeTo(internalFrame1);
        }
        if (petugas == null) return;
        petugas.isCek();
        if (petugas.isVisible()) {
            petugas.toFront();
            return;
        }
        petugas.setVisible(true);
    }

    private String v(widget.ComboBox c){
        return c.getSelectedItem()==null?"":c.getSelectedItem().toString();
    }

    private void pilih(widget.ComboBox c, String nilai){
        for(int k=0;k<c.getItemCount();k++){
            if(c.getItemAt(k).toString().equals(nilai)){
                c.setSelectedIndex(k);
                return;
            }
        }
        if(c.getItemCount()>0){
            c.setSelectedIndex(0);
        }
    }

    private void simpan(){
        if(TNoRw.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, belum ada pasien yang dipilih...!!");
            return;
        }
        if(NmPerawatSirkuler.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih perawat sirkuler...!!");
            BtnPerawatSirkuler.requestFocus();
            return;
        }
        try {
            // satu catatan per no_rawat : jika sudah ada, timpa
            Sequel.queryu2tf("delete from catatan_keperawatan_intra_operasi where no_rawat=?",1,new String[]{TNoRw.getText()});
            boolean sukses=Sequel.menyimpantf2("catatan_keperawatan_intra_operasi",
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?",73,new String[]{
                TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19),
                JamMulai.getText(),JamSelesai.getText(),TindakanOperasi.getText(),v(TipeOperasi),v(TipePembiusan),
                GcsE.getText(),GcsM.getText(),GcsV.getText(),v(StatusEmosi),
                v(PosisiKanula),KanulaKet.getText(),v(PosisiOperasi),PosisiDiawasi.getText(),
                v(RisikoLukaTekan),SkorBraden.getText(),v(StockingVte),v(PosisiLengan),v(AlatBantu),
                v(Kateter),KateterOleh.getText(),JenisKateter.getText(),NoKateter.getText(),IsiBalon.getText(),v(KateterArah),v(KateterTraksi),
                v(PersiapanKulit),PersiapanKulitKet.getText(),v(Diathermy),v(ElektrodaLokasi),ElektrodaOleh.getText(),
                v(KulitSebelum),v(KulitSesudah),KodeElektrosurgical.getText(),v(UnitPemanas),SuhuPemanas.getText(),
                TorniketOleh.getText(),
                TorLkaMulai.getText(),TorLkaSelesai.getText(),TorLkaTekanan.getText(),
                TorLkiMulai.getText(),TorLkiSelesai.getText(),TorLkiTekanan.getText(),
                TorPkaMulai.getText(),TorPkaSelesai.getText(),TorPkaTekanan.getText(),
                TorPkiMulai.getText(),TorPkiSelesai.getText(),TorPkiTekanan.getText(),
                v(Laser),LaserDiawasi.getText(),Implant.getText(),
                v(Drain),JenisDrain.getText(),v(SifatDrain),
                CairanGlycine.getText(),CairanWfi.getText(),CairanNacl.getText(),CairanBss.getText(),CairanLain.getText(),
                v(Balutan),JenisBalutan.getText(),
                v(Spesimen),SpesimenKet.getText(),JumlahTotalJaringan.getText(),
                v(SpesimenDiberikan),JenisJaringan.getText(),JumlahJaringan.getText(),NamaPenerima.getText(),
                Keterangan.getText(),NmPerawatSteril.getText(),NmPerawatSirkuler.getText()
            });
            if(sukses){
                JOptionPane.showMessageDialog(null,"Catatan keperawatan intra operasi berhasil disimpan...");
                runBackground(() ->tampil());
            }
        } catch (Exception e) {
            System.out.println("Notifikasi simpan : "+e);
        }
    }

    private void hapus(){
        if(TNoRw.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, belum ada data yang dipilih...!!");
            return;
        }
        if(JOptionPane.showConfirmDialog(null,"Yakin catatan intra operasi pasien ini dihapus?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            Sequel.queryu2tf("delete from catatan_keperawatan_intra_operasi where no_rawat=?",1,new String[]{TNoRw.getText()});
            runBackground(() ->tampil());
        }
    }

    private void cetak(){
        String norawat=TNoRw.getText().trim();
        if(TabRawat.getSelectedIndex()==1 && tbData.getSelectedRow()>-1){
            norawat=tbData.getValueAt(tbData.getSelectedRow(),0).toString();
        }
        if(norawat.equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih dulu data yang mau dicetak...!!");
            return;
        }
        cetakNoRawat(norawat);
    }

    private void cetakNoRawat(String norawat){
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Map<String, Object> param = new HashMap<>();
        param.put("namars",akses.getnamars());
        param.put("alamatrs",akses.getalamatrs());
        param.put("kotars",akses.getkabupatenrs());
        param.put("propinsirs",akses.getpropinsirs());
        param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
        Valid.MyReportqrypdf("rptCatatanKeperawatanIntraOperasi.jasper","report","::[ Catatan Keperawatan Intra Operasi ]::",
            "select c.*,pasien.no_rkm_medis,pasien.nm_pasien,date_format(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir,pasien.jk,"+
            "date_format(c.tanggal,'%d-%m-%Y') as tgl_operasi "+
            "from catatan_keperawatan_intra_operasi c inner join reg_periksa on reg_periksa.no_rawat=c.no_rawat "+
            "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
            "where c.no_rawat='"+norawat+"'",param);
        this.setCursor(Cursor.getDefaultCursor());
    }

    public void tampil(){
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(tabMode);
            koneksi=koneksiDB.condb();
            ps=koneksi.prepareStatement(
                "select c.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,c.tanggal,c.tindakan_operasi,c.perawat_sirkuler "+
                "from catatan_keperawatan_intra_operasi c inner join reg_periksa on reg_periksa.no_rawat=c.no_rawat "+
                "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                "where (c.tanggal between ? and ? or c.no_rawat=?) "+
                "and (c.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or c.tindakan_operasi like ? or c.perawat_sirkuler like ?) "+
                "order by c.tanggal");
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                ps.setString(3,TNoRw.getText());
                ps.setString(4,"%"+TCari.getText().trim()+"%");
                ps.setString(5,"%"+TCari.getText().trim()+"%");
                ps.setString(6,"%"+TCari.getText().trim()+"%");
                ps.setString(7,"%"+TCari.getText().trim()+"%");
                ps.setString(8,"%"+TCari.getText().trim()+"%");
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                        rs.getString("tanggal"),rs.getString("tindakan_operasi"),rs.getString("perawat_sirkuler")
                    });
                }
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }

    private void ambilData(String norawat){
        try{
            koneksi=koneksiDB.condb();
            PreparedStatement psd=koneksi.prepareStatement("select * from catatan_keperawatan_intra_operasi where no_rawat=?");
                psd.setString(1,norawat);
                ResultSet rsd=psd.executeQuery();
                if(rsd.next()){
                    setNoRm(norawat,DTPCari2.getDate());
                    JamMulai.setText(rsd.getString("jam_mulai"));
                    JamSelesai.setText(rsd.getString("jam_selesai"));
                    TindakanOperasi.setText(rsd.getString("tindakan_operasi"));
                    pilih(TipeOperasi,rsd.getString("tipe_operasi"));
                    pilih(TipePembiusan,rsd.getString("tipe_pembiusan"));
                    GcsE.setText(rsd.getString("gcs_e"));
                    GcsM.setText(rsd.getString("gcs_m"));
                    GcsV.setText(rsd.getString("gcs_v"));
                    pilih(StatusEmosi,rsd.getString("status_emosi"));
                    pilih(PosisiKanula,rsd.getString("posisi_kanula"));
                    KanulaKet.setText(rsd.getString("kanula_ket"));
                    pilih(PosisiOperasi,rsd.getString("posisi_operasi"));
                    PosisiDiawasi.setText(rsd.getString("posisi_diawasi"));
                    pilih(RisikoLukaTekan,rsd.getString("risiko_luka_tekan"));
                    SkorBraden.setText(rsd.getString("skor_braden"));
                    pilih(StockingVte,rsd.getString("stocking_vte"));
                    pilih(PosisiLengan,rsd.getString("posisi_lengan"));
                    pilih(AlatBantu,rsd.getString("alat_bantu"));
                    pilih(Kateter,rsd.getString("kateter"));
                    KateterOleh.setText(rsd.getString("kateter_oleh"));
                    JenisKateter.setText(rsd.getString("jenis_kateter"));
                    NoKateter.setText(rsd.getString("no_kateter"));
                    IsiBalon.setText(rsd.getString("isi_balon"));
                    pilih(KateterArah,rsd.getString("kateter_arah"));
                    pilih(KateterTraksi,rsd.getString("kateter_traksi"));
                    pilih(PersiapanKulit,rsd.getString("persiapan_kulit"));
                    PersiapanKulitKet.setText(rsd.getString("persiapan_kulit_ket"));
                    pilih(Diathermy,rsd.getString("diathermy"));
                    pilih(ElektrodaLokasi,rsd.getString("elektroda_lokasi"));
                    ElektrodaOleh.setText(rsd.getString("elektroda_oleh"));
                    pilih(KulitSebelum,rsd.getString("kulit_sebelum"));
                    pilih(KulitSesudah,rsd.getString("kulit_sesudah"));
                    KodeElektrosurgical.setText(rsd.getString("kode_elektrosurgical"));
                    pilih(UnitPemanas,rsd.getString("unit_pemanas"));
                    SuhuPemanas.setText(rsd.getString("suhu_pemanas"));
                    TorniketOleh.setText(rsd.getString("torniket_oleh"));
                    TorLkaMulai.setText(rsd.getString("tor_lka_mulai"));
                    TorLkaSelesai.setText(rsd.getString("tor_lka_selesai"));
                    TorLkaTekanan.setText(rsd.getString("tor_lka_tekanan"));
                    TorLkiMulai.setText(rsd.getString("tor_lki_mulai"));
                    TorLkiSelesai.setText(rsd.getString("tor_lki_selesai"));
                    TorLkiTekanan.setText(rsd.getString("tor_lki_tekanan"));
                    TorPkaMulai.setText(rsd.getString("tor_pka_mulai"));
                    TorPkaSelesai.setText(rsd.getString("tor_pka_selesai"));
                    TorPkaTekanan.setText(rsd.getString("tor_pka_tekanan"));
                    TorPkiMulai.setText(rsd.getString("tor_pki_mulai"));
                    TorPkiSelesai.setText(rsd.getString("tor_pki_selesai"));
                    TorPkiTekanan.setText(rsd.getString("tor_pki_tekanan"));
                    pilih(Laser,rsd.getString("laser"));
                    LaserDiawasi.setText(rsd.getString("laser_diawasi"));
                    Implant.setText(rsd.getString("implant"));
                    pilih(Drain,rsd.getString("drain"));
                    JenisDrain.setText(rsd.getString("jenis_drain"));
                    pilih(SifatDrain,rsd.getString("sifat_drain"));
                    CairanGlycine.setText(rsd.getString("cairan_glycine"));
                    CairanWfi.setText(rsd.getString("cairan_wfi"));
                    CairanNacl.setText(rsd.getString("cairan_nacl"));
                    CairanBss.setText(rsd.getString("cairan_bss"));
                    CairanLain.setText(rsd.getString("cairan_lain"));
                    pilih(Balutan,rsd.getString("balutan"));
                    JenisBalutan.setText(rsd.getString("jenis_balutan"));
                    pilih(Spesimen,rsd.getString("spesimen"));
                    SpesimenKet.setText(rsd.getString("spesimen_ket"));
                    JumlahTotalJaringan.setText(rsd.getString("jumlah_total_jaringan"));
                    pilih(SpesimenDiberikan,rsd.getString("spesimen_diberikan"));
                    JenisJaringan.setText(rsd.getString("jenis_jaringan"));
                    JumlahJaringan.setText(rsd.getString("jumlah_jaringan"));
                    NamaPenerima.setText(rsd.getString("nama_penerima"));
                    Keterangan.setText(rsd.getString("keterangan"));
                    NmPerawatSteril.setText(rsd.getString("perawat_steril"));
                    KdPerawatSteril.setText(Sequel.cariIsi("select nip from petugas where nama=? limit 1",rsd.getString("perawat_steril")));
                    NmPerawatSirkuler.setText(rsd.getString("perawat_sirkuler"));
                    KdPerawatSirkuler.setText(Sequel.cariIsi("select nip from petugas where nama=? limit 1",rsd.getString("perawat_sirkuler")));
                }
                rsd.close();
                psd.close();
        }catch(Exception e){
            System.out.println("Notifikasi ambil data : "+e);
        }
    }

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        if(tgl2!=null){
            DTPCari2.setDate(tgl2);
        }
        Sequel.cariIsi("select pasien.no_rkm_medis from reg_periksa inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis where reg_periksa.no_rawat=?", TNoRM, norwt);
        Sequel.cariIsi("select pasien.nm_pasien from reg_periksa inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis where reg_periksa.no_rawat=?", TPasien, norwt);
        Sequel.cariIsi("select date_format(pasien.tgl_lahir,'%d-%m-%Y') from reg_periksa inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis where reg_periksa.no_rawat=?", TglLahir, norwt);
        runBackground(() ->tampil());
    }

    public void setNoRm(String norwt, Date tgl2, String tindakan) {
        setNoRm(norwt,tgl2);
        if(TindakanOperasi.getText().trim().equals("") && tindakan!=null){
            TindakanOperasi.setText(tindakan);
        }
    }

    public void isCek(){
        BtnSimpan.setEnabled(akses.gethasil_pemeriksaan_echo());
        BtnHapus.setEnabled(akses.gethasil_pemeriksaan_echo());
        BtnPrint.setEnabled(akses.gethasil_pemeriksaan_echo());
    }

    public void emptTeks() {
        KdPerawatSteril.setText("");NmPerawatSteril.setText("");
        KdPerawatSirkuler.setText("");NmPerawatSirkuler.setText("");
        JamMulai.setText("");JamSelesai.setText("");TindakanOperasi.setText("");
        GcsE.setText("");GcsM.setText("");GcsV.setText("");
        KanulaKet.setText("");PosisiDiawasi.setText("");SkorBraden.setText("");
        KateterOleh.setText("");JenisKateter.setText("");NoKateter.setText("");IsiBalon.setText("");
        PersiapanKulitKet.setText("");ElektrodaOleh.setText("");KodeElektrosurgical.setText("");SuhuPemanas.setText("");
        TorniketOleh.setText("");
        TorLkaMulai.setText("");TorLkaSelesai.setText("");TorLkaTekanan.setText("");
        TorLkiMulai.setText("");TorLkiSelesai.setText("");TorLkiTekanan.setText("");
        TorPkaMulai.setText("");TorPkaSelesai.setText("");TorPkaTekanan.setText("");
        TorPkiMulai.setText("");TorPkiSelesai.setText("");TorPkiTekanan.setText("");
        LaserDiawasi.setText("");Implant.setText("");JenisDrain.setText("");
        CairanGlycine.setText("");CairanWfi.setText("");CairanNacl.setText("");CairanBss.setText("");CairanLain.setText("");
        JenisBalutan.setText("");SpesimenKet.setText("");JumlahTotalJaringan.setText("");
        JenisJaringan.setText("");JumlahJaringan.setText("");NamaPenerima.setText("");Keterangan.setText("");
        TipeOperasi.setSelectedIndex(0);TipePembiusan.setSelectedIndex(0);StatusEmosi.setSelectedIndex(0);
        PosisiKanula.setSelectedIndex(0);PosisiOperasi.setSelectedIndex(0);RisikoLukaTekan.setSelectedIndex(0);
        StockingVte.setSelectedIndex(0);PosisiLengan.setSelectedIndex(0);AlatBantu.setSelectedIndex(0);
        Kateter.setSelectedIndex(0);KateterArah.setSelectedIndex(0);KateterTraksi.setSelectedIndex(0);
        PersiapanKulit.setSelectedIndex(0);Diathermy.setSelectedIndex(0);ElektrodaLokasi.setSelectedIndex(0);
        KulitSebelum.setSelectedIndex(0);KulitSesudah.setSelectedIndex(0);UnitPemanas.setSelectedIndex(0);
        Laser.setSelectedIndex(0);Drain.setSelectedIndex(0);SifatDrain.setSelectedIndex(0);
        Balutan.setSelectedIndex(0);Spesimen.setSelectedIndex(0);SpesimenDiberikan.setSelectedIndex(0);
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMCatatanKeperawatanIntraOperasi dialog = new RMCatatanKeperawatanIntraOperasi(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.Button BtnPerawatSteril;
    private widget.Button BtnPerawatSirkuler;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.PanelBiasa FormInput;
    private widget.TextBox TNoRw;
    private widget.TextBox TNoRM;
    private widget.TextBox TPasien;
    private widget.TextBox TglLahir;
    private widget.TextBox KdPerawatSteril;
    private widget.TextBox NmPerawatSteril;
    private widget.TextBox KdPerawatSirkuler;
    private widget.TextBox NmPerawatSirkuler;
    private widget.TextBox JamMulai;
    private widget.TextBox JamSelesai;
    private widget.TextBox TindakanOperasi;
    private widget.ComboBox TipeOperasi;
    private widget.ComboBox TipePembiusan;
    private widget.TextBox GcsE;
    private widget.TextBox GcsM;
    private widget.TextBox GcsV;
    private widget.ComboBox StatusEmosi;
    private widget.ComboBox PosisiKanula;
    private widget.TextBox KanulaKet;
    private widget.ComboBox PosisiOperasi;
    private widget.TextBox PosisiDiawasi;
    private widget.ComboBox RisikoLukaTekan;
    private widget.TextBox SkorBraden;
    private widget.ComboBox StockingVte;
    private widget.ComboBox PosisiLengan;
    private widget.ComboBox AlatBantu;
    private widget.ComboBox Kateter;
    private widget.TextBox KateterOleh;
    private widget.TextBox JenisKateter;
    private widget.TextBox NoKateter;
    private widget.TextBox IsiBalon;
    private widget.ComboBox KateterArah;
    private widget.ComboBox KateterTraksi;
    private widget.ComboBox PersiapanKulit;
    private widget.TextBox PersiapanKulitKet;
    private widget.ComboBox Diathermy;
    private widget.ComboBox ElektrodaLokasi;
    private widget.TextBox ElektrodaOleh;
    private widget.ComboBox KulitSebelum;
    private widget.ComboBox KulitSesudah;
    private widget.TextBox KodeElektrosurgical;
    private widget.ComboBox UnitPemanas;
    private widget.TextBox SuhuPemanas;
    private widget.TextBox TorniketOleh;
    private widget.TextBox TorLkaMulai;
    private widget.TextBox TorLkaSelesai;
    private widget.TextBox TorLkaTekanan;
    private widget.TextBox TorLkiMulai;
    private widget.TextBox TorLkiSelesai;
    private widget.TextBox TorLkiTekanan;
    private widget.TextBox TorPkaMulai;
    private widget.TextBox TorPkaSelesai;
    private widget.TextBox TorPkaTekanan;
    private widget.TextBox TorPkiMulai;
    private widget.TextBox TorPkiSelesai;
    private widget.TextBox TorPkiTekanan;
    private widget.ComboBox Laser;
    private widget.TextBox LaserDiawasi;
    private widget.TextBox Implant;
    private widget.ComboBox Drain;
    private widget.TextBox JenisDrain;
    private widget.ComboBox SifatDrain;
    private widget.TextBox CairanGlycine;
    private widget.TextBox CairanWfi;
    private widget.TextBox CairanNacl;
    private widget.TextBox CairanBss;
    private widget.TextBox CairanLain;
    private widget.ComboBox Balutan;
    private widget.TextBox JenisBalutan;
    private widget.ComboBox Spesimen;
    private widget.TextBox SpesimenKet;
    private widget.TextBox JumlahTotalJaringan;
    private widget.ComboBox SpesimenDiberikan;
    private widget.TextBox JenisJaringan;
    private widget.TextBox JumlahJaringan;
    private widget.TextBox NamaPenerima;
    private widget.TextBox Keterangan;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal Tanggal;
    private widget.TextBox TCari;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel6;
    private widget.Label jLabel8;
    private javax.swing.JSeparator jSeparator1;
    private widget.Label label11;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane scrollInput;
    private widget.Table tbData;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JMenuItem MnCetakFormulir;
    // End of variables declaration

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
}
