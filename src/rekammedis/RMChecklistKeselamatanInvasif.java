/*
 * Lembar Checklist Keselamatan Pasien di Ruang Tindakan Invasif (Cathlab)
 * Sign In (sebelum sedasi/anestesi), Time Out (sebelum insisi/memasukkan alat),
 * Sign Out (sebelum meninggalkan ruang tindakan).
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
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPetugas;

/**
 *
 * @author perpustakaan
 */
public final class RMChecklistKeselamatanInvasif extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private String norawatPilih="";
    private DlgCariDokter dokter;
    private DlgCariPetugas petugas;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private int i=0;

    /** Creates new form RMChecklistKeselamatanInvasif
     * @param parent
     * @param modal */
    public RMChecklistKeselamatanInvasif(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        tabMode=new DefaultTableModel(null,new String[]{
            "No.Rawat","No.RM","Nama Pasien","Tanggal","Operator","Perawat"}){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,400));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 6; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0||i==1){
                column.setPreferredWidth(115);
            }else if(i==2||i==4){
                column.setPreferredWidth(180);
            }else{
                column.setPreferredWidth(130);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        JamSignIn.setDocument(new batasInput((int)8).getKata(JamSignIn));
        SiKontras.setDocument(new batasInput((int)50).getKata(SiKontras));
        JamTimeOut.setDocument(new batasInput((int)8).getKata(JamTimeOut));
        ToPosisi.setDocument(new batasInput((int)100).getKata(ToPosisi));
        ToProfilaksis.setDocument(new batasInput((int)100).getKata(ToProfilaksis));
        ToKritis.setDocument(new batasInput((int)100).getKata(ToKritis));
        ToLama.setDocument(new batasInput((int)50).getKata(ToLama));
        ToAlat.setDocument(new batasInput((int)100).getKata(ToAlat));
        JamSignOut.setDocument(new batasInput((int)8).getKata(JamSignOut));
        SoTindakan.setDocument(new batasInput((int)100).getKata(SoTindakan));
        SoMasalah.setDocument(new batasInput((int)200).getKata(SoMasalah));
        SoOp.setDocument(new batasInput((int)200).getKata(SoOp));
        SoAnes.setDocument(new batasInput((int)200).getKata(SoAnes));
        SoPerawat.setDocument(new batasInput((int)200).getKata(SoPerawat));
        SoRad.setDocument(new batasInput((int)200).getKata(SoRad));
        SoKontras.setDocument(new batasInput((int)50).getKata(SoKontras));
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
        label14 = new widget.Label();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        label11 = new widget.Label();
        Tanggal = new widget.Tanggal();
        jSeparator1 = new javax.swing.JSeparator();
        KdPerawat = new widget.TextBox();
        NmPerawat = new widget.TextBox();
        BtnPerawat = new widget.Button();
        JamSignIn = new widget.TextBox();
        SiIdentitas = combo("Ya","Tidak");
        SiArea = combo("Ya","Tidak Perlu");
        SiAlergi = combo("Tidak","Ya");
        SiMonitor = combo("Ya","Tidak");
        SiAirway = combo("Tidak","Ya, alat bantu tersedia");
        SiAlat = combo("Ya","Tidak");
        SiAnestesi = combo("Ya","Tidak","Tidak Perlu");
        SiKontras = new widget.TextBox();
        SiPerdarahan = combo("Tidak","Ya, siap infus 2 line");
        JamTimeOut = new widget.TextBox();
        ToPerkenalan = combo("Ya","Tidak");
        ToIdentitas = combo("Ya","Tidak");
        ToPosisi = new widget.TextBox();
        ToProfilaksis = new widget.TextBox();
        ToPenunjang = combo("Ya","Tidak Perlu");
        ToKritis = new widget.TextBox();
        ToPerdarahan = combo("Ya","Tidak Perlu");
        ToKhusus = combo("Tidak","Ya");
        ToLama = new widget.TextBox();
        ToSteril = combo("Ya","Tidak");
        ToAlat = new widget.TextBox();
        JamSignOut = new widget.TextBox();
        SoTindakan = new widget.TextBox();
        SoSpesimen = combo("Tidak Ada Spesimen","Ya");
        SoAlat = combo("Lengkap","Tidak Lengkap");
        SoMasalah = new widget.TextBox();
        SoOp = new widget.TextBox();
        SoAnes = new widget.TextBox();
        SoPerawat = new widget.TextBox();
        SoRad = new widget.TextBox();
        SoKontras = new widget.TextBox();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
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
        MnCetakFormulir.setText("Formulir Checklist Keselamatan Tindakan Invasif");
        MnCetakFormulir.setName("MnCetakFormulir");
        MnCetakFormulir.setPreferredSize(new java.awt.Dimension(340, 26));
        MnCetakFormulir.addActionListener(evt -> {
            String nr=norawatPilih;
            if(nr==null || nr.isEmpty()){
                if(tbObat.getSelectedRow()>-1){
                    nr=tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Checklist Keselamatan Pasien Ruang Tindakan Invasif ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
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
        FormInput.setPreferredSize(new java.awt.Dimension(750, 1180));
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

        label14.setText("Operator :");
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label14);
        label14.setBounds(0, 40, 70, 23);

        KdDokter.setEditable(false);
        KdDokter.setName("KdDokter");
        KdDokter.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(KdDokter);
        KdDokter.setBounds(74, 40, 110, 23);

        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter");
        NmDokter.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmDokter);
        NmDokter.setBounds(186, 40, 295, 23);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnDokter.setMnemonic('2');
        BtnDokter.setToolTipText("Alt+2");
        BtnDokter.setName("BtnDokter");
        BtnDokter.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDokter.addActionListener(evt -> BtnDokterActionPerformed(evt));
        FormInput.add(BtnDokter);
        BtnDokter.setBounds(484, 40, 28, 23);

        label11.setText("Tanggal :");
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label11);
        label11.setBounds(538, 40, 52, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "06-02-2026 17:42:45" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        Tanggal.setName("Tanggal");
        Tanggal.setOpaque(false);
        FormInput.add(Tanggal);
        Tanggal.setBounds(594, 40, 130, 23);

        jSeparator1.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator1.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(0, 70, 750, 1);

        labelKiri("Perawat :", 0, 80, 135);
        KdPerawat.setEditable(false);
        KdPerawat.setName("KdPerawat");
        FormInput.add(KdPerawat);
        KdPerawat.setBounds(139, 80, 110, 23);
        NmPerawat.setEditable(false);
        NmPerawat.setName("NmPerawat");
        FormInput.add(NmPerawat);
        NmPerawat.setBounds(251, 80, 295, 23);
        BtnPerawat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnPerawat.setToolTipText("Cari petugas/perawat");
        BtnPerawat.setName("BtnPerawat");
        BtnPerawat.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPerawat.addActionListener(evt -> BtnPerawatActionPerformed(evt));
        FormInput.add(BtnPerawat);
        BtnPerawat.setBounds(548, 80, 28, 23);

        labelSeksi("SIGN IN  (Sebelum Sedasi/Anastesi)", 115);
        labelKiri("Jam Sign In (hh:mm) :", 0, 145, 420);
        JamSignIn.setName("JamSignIn");
        FormInput.add(JamSignIn);
        JamSignIn.setBounds(424, 145, 100, 23);
        labelKiri("Identitas benar, rencana tindakan jelas, ada informed consent :", 0, 175, 420);
        FormInput.add(SiIdentitas);
        SiIdentitas.setBounds(424, 175, 300, 23);
        labelKiri("Area yang akan dioperasi/tindakan sudah diberi tanda :", 0, 205, 420);
        FormInput.add(SiArea);
        SiArea.setBounds(424, 205, 300, 23);
        labelKiri("Pasien memiliki riwayat alergi :", 0, 235, 420);
        FormInput.add(SiAlergi);
        SiAlergi.setBounds(424, 235, 300, 23);
        labelKiri("Pulse oksimetri, EKG, tensimeter terpasang & berfungsi baik :", 0, 265, 420);
        FormInput.add(SiMonitor);
        SiMonitor.setBounds(424, 265, 300, 23);
        labelKiri("Risiko masalah airway atau aspirasi :", 0, 295, 420);
        FormInput.add(SiAirway);
        SiAirway.setBounds(424, 295, 300, 23);
        labelKiri("Alat berfungsi dengan baik :", 0, 325, 420);
        FormInput.add(SiAlat);
        SiAlat.setBounds(424, 325, 300, 23);
        labelKiri("Mesin anastesi dan obat-obatan sudah lengkap :", 0, 355, 420);
        FormInput.add(SiAnestesi);
        SiAnestesi.setBounds(424, 355, 300, 23);
        labelKiri("Jumlah kontras yang dibutuhkan (ml) :", 0, 385, 420);
        SiKontras.setName("SiKontras");
        FormInput.add(SiKontras);
        SiKontras.setBounds(424, 385, 300, 23);
        labelKiri("Risiko perdarahan > 500ml (7ml/kg bagi anak-anak) :", 0, 415, 420);
        FormInput.add(SiPerdarahan);
        SiPerdarahan.setBounds(424, 415, 300, 23);

        labelSeksi("TIME OUT  (Sebelum Insisi/Memasukkan Alat atau Obat)", 450);
        labelKiri("Jam Time Out (hh:mm) :", 0, 480, 420);
        JamTimeOut.setName("JamTimeOut");
        FormInput.add(JamTimeOut);
        JamTimeOut.setBounds(424, 480, 100, 23);
        labelKiri("Semua anggota tim memperkenalkan diri (nama dan peran) :", 0, 510, 420);
        FormInput.add(ToPerkenalan);
        ToPerkenalan.setBounds(424, 510, 300, 23);
        labelKiri("Baca ulang identitas pasien, tindakan medis & area tindakan :", 0, 540, 420);
        FormInput.add(ToIdentitas);
        ToIdentitas.setBounds(424, 540, 300, 23);
        labelKiri("Posisi pasien sudah benar (keterangan) :", 0, 570, 420);
        ToPosisi.setName("ToPosisi");
        FormInput.add(ToPosisi);
        ToPosisi.setBounds(424, 570, 300, 23);
        labelKiri("Profilaksis 1 jam sebelumnya (jenis obat / tidak perlu) :", 0, 600, 420);
        ToProfilaksis.setName("ToProfilaksis");
        FormInput.add(ToProfilaksis);
        ToProfilaksis.setBounds(424, 600, 300, 23);
        labelKiri("Hasil radiologi & pemeriksaan penunjang lain sudah ada :", 0, 630, 420);
        FormInput.add(ToPenunjang);
        ToPenunjang.setBounds(424, 630, 300, 23);
        labelKiri("Kondisi kejadian yang perlu diantisipasi/kritis (isi jika ada) :", 0, 660, 420);
        ToKritis.setName("ToKritis");
        FormInput.add(ToKritis);
        ToKritis.setBounds(424, 660, 300, 23);
        labelKiri("Sudah antisipasi perdarahan :", 0, 690, 420);
        FormInput.add(ToPerdarahan);
        ToPerdarahan.setBounds(424, 690, 300, 23);
        labelKiri("Ada hal khusus untuk pasien ini :", 0, 720, 420);
        FormInput.add(ToKhusus);
        ToKhusus.setBounds(424, 720, 300, 23);
        labelKiri("Perkiraan lama tindakan :", 0, 750, 420);
        ToLama.setName("ToLama");
        FormInput.add(ToLama);
        ToLama.setBounds(424, 750, 300, 23);
        labelKiri("Alat lengkap & steril (ada indikator sterilitas) :", 0, 780, 420);
        FormInput.add(ToSteril);
        ToSteril.setBounds(424, 780, 300, 23);
        labelKiri("Perhatian khusus terkait alat (isi jika ada) :", 0, 810, 420);
        ToAlat.setName("ToAlat");
        FormInput.add(ToAlat);
        ToAlat.setBounds(424, 810, 300, 23);

        labelSeksi("SIGN OUT  (Sebelum Meninggalkan Ruang Tindakan)", 845);
        labelKiri("Jam Sign Out (hh:mm) :", 0, 875, 420);
        JamSignOut.setName("JamSignOut");
        FormInput.add(JamSignOut);
        JamSignOut.setBounds(424, 875, 100, 23);
        labelKiri("Nama tindakan :", 0, 905, 420);
        SoTindakan.setName("SoTindakan");
        FormInput.add(SoTindakan);
        SoTindakan.setBounds(424, 905, 300, 23);
        labelKiri("Pelabelan spesimen (baca label & nama pasien dengan keras) :", 0, 935, 420);
        FormInput.add(SoSpesimen);
        SoSpesimen.setBounds(424, 935, 300, 23);
        labelKiri("Kelengkapan alat :", 0, 965, 420);
        FormInput.add(SoAlat);
        SoAlat.setBounds(424, 965, 300, 23);
        labelKiri("Masalah peralatan yang perlu disampaikan :", 0, 995, 420);
        SoMasalah.setName("SoMasalah");
        FormInput.add(SoMasalah);
        SoMasalah.setBounds(424, 995, 300, 23);
        labelKiri("Catatan untuk Operator :", 0, 1025, 420);
        SoOp.setName("SoOp");
        FormInput.add(SoOp);
        SoOp.setBounds(424, 1025, 300, 23);
        labelKiri("Catatan untuk Anastesi :", 0, 1055, 420);
        SoAnes.setName("SoAnes");
        FormInput.add(SoAnes);
        SoAnes.setBounds(424, 1055, 300, 23);
        labelKiri("Catatan untuk Perawat :", 0, 1085, 420);
        SoPerawat.setName("SoPerawat");
        FormInput.add(SoPerawat);
        SoPerawat.setBounds(424, 1085, 300, 23);
        labelKiri("Catatan Radiografer/petugas lain :", 0, 1115, 420);
        SoRad.setName("SoRad");
        FormInput.add(SoRad);
        SoRad.setBounds(424, 1115, 300, 23);
        labelKiri("Jumlah kontras yang telah digunakan (ml) :", 0, 1145, 420);
        SoKontras.setName("SoKontras");
        FormInput.add(SoKontras);
        SoKontras.setBounds(424, 1145, 300, 23);

        scrollInput.setViewportView(FormInput);
        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);
        TabRawat.addTab("Input Checklist Keselamatan", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3");
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll");
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat");
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if(tbObat.getSelectedRow()>-1){
                    norawatPilih=tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();
                    ambilData(norawatPilih);
                    if(evt.getButton()==java.awt.event.MouseEvent.BUTTON1){
                        jPopupMenu1.show(tbObat, evt.getX(), evt.getY());
                    }
                }
            }
        });
        Scroll.setViewportView(tbObat);
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
        TabRawat.addTab("Data Checklist Keselamatan", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {
        if (dokter == null || !dokter.isDisplayable()) {
            dokter=new DlgCariDokter(null,false);
            dokter.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dokter.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(dokter.getTable().getSelectedRow()!= -1){
                         KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                         NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                    }
                    BtnDokter.requestFocus();
                    dokter=null;
                }
            });
            dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dokter.setLocationRelativeTo(internalFrame1);
        }
        if (dokter == null) return;
        dokter.isCek();
        if (dokter.isVisible()) {
            dokter.toFront();
            return;
        }
        dokter.setVisible(true);
    }

    private void BtnPerawatActionPerformed(java.awt.event.ActionEvent evt) {
        if (petugas == null || !petugas.isDisplayable()) {
            petugas=new DlgCariPetugas(null,false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(petugas.getTable().getSelectedRow()!= -1){
                        KdPerawat.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                        NmPerawat.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());
                    }
                    BtnPerawat.requestFocus();
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
        if(KdDokter.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih dokter operator...!!");
            BtnDokter.requestFocus();
            return;
        }
        if(NmPerawat.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih perawat...!!");
            BtnPerawat.requestFocus();
            return;
        }
        try {
            // satu checklist per no_rawat : jika sudah ada, timpa
            Sequel.queryu2tf("delete from checklist_keselamatan_invasif where no_rawat=?",1,new String[]{TNoRw.getText()});
            boolean sukses=Sequel.menyimpantf2("checklist_keselamatan_invasif",
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?",36,new String[]{
                TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19),
                KdDokter.getText(),NmPerawat.getText(),
                JamSignIn.getText(),v(SiIdentitas),v(SiArea),v(SiAlergi),v(SiMonitor),
                v(SiAirway),v(SiAlat),v(SiAnestesi),SiKontras.getText(),v(SiPerdarahan),
                JamTimeOut.getText(),v(ToPerkenalan),v(ToIdentitas),ToPosisi.getText(),ToProfilaksis.getText(),
                v(ToPenunjang),ToKritis.getText(),v(ToPerdarahan),v(ToKhusus),ToLama.getText(),
                v(ToSteril),ToAlat.getText(),
                JamSignOut.getText(),SoTindakan.getText(),v(SoSpesimen),v(SoAlat),SoMasalah.getText(),
                SoOp.getText(),SoAnes.getText(),SoPerawat.getText(),SoRad.getText(),SoKontras.getText()
            });
            if(sukses){
                JOptionPane.showMessageDialog(null,"Checklist keselamatan berhasil disimpan...");
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
        if(JOptionPane.showConfirmDialog(null,"Yakin checklist pasien ini dihapus?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            Sequel.queryu2tf("delete from checklist_keselamatan_invasif where no_rawat=?",1,new String[]{TNoRw.getText()});
            runBackground(() ->tampil());
        }
    }

    private void cetak(){
        String norawat=TNoRw.getText().trim();
        if(TabRawat.getSelectedIndex()==1 && tbObat.getSelectedRow()>-1){
            norawat=tbObat.getValueAt(tbObat.getSelectedRow(),0).toString();
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
        Valid.MyReportqrypdf("rptChecklistKeselamatanInvasif.jasper","report","::[ Checklist Keselamatan Pasien Tindakan Invasif ]::",
            "select c.*,pasien.no_rkm_medis,pasien.nm_pasien,date_format(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir,pasien.jk,"+
            "date_format(c.tanggal,'%d-%m-%Y') as tgl_tindakan,dokter.nm_dokter "+
            "from checklist_keselamatan_invasif c inner join reg_periksa on reg_periksa.no_rawat=c.no_rawat "+
            "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
            "inner join dokter on dokter.kd_dokter=c.kd_dokter "+
            "where c.no_rawat='"+norawat+"'",param);
        this.setCursor(Cursor.getDefaultCursor());
    }

    public void tampil(){
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(tabMode);
            koneksi=koneksiDB.condb();
            ps=koneksi.prepareStatement(
                "select c.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,c.tanggal,dokter.nm_dokter,c.perawat "+
                "from checklist_keselamatan_invasif c inner join reg_periksa on reg_periksa.no_rawat=c.no_rawat "+
                "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                "inner join dokter on dokter.kd_dokter=c.kd_dokter "+
                "where (c.tanggal between ? and ? or c.no_rawat=?) "+
                "and (c.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or dokter.nm_dokter like ?) "+
                "order by c.tanggal");
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                ps.setString(3,TNoRw.getText());
                ps.setString(4,"%"+TCari.getText().trim()+"%");
                ps.setString(5,"%"+TCari.getText().trim()+"%");
                ps.setString(6,"%"+TCari.getText().trim()+"%");
                ps.setString(7,"%"+TCari.getText().trim()+"%");
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                        rs.getString("tanggal"),rs.getString("nm_dokter"),rs.getString("perawat")
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
            PreparedStatement psd=koneksi.prepareStatement("select * from checklist_keselamatan_invasif where no_rawat=?");
                psd.setString(1,norawat);
                ResultSet rsd=psd.executeQuery();
                if(rsd.next()){
                    setNoRm(norawat,DTPCari2.getDate());
                    KdDokter.setText(rsd.getString("kd_dokter"));
                    Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", NmDokter,KdDokter.getText());
                    NmPerawat.setText(rsd.getString("perawat"));
                    KdPerawat.setText(Sequel.cariIsi("select nip from petugas where nama=? limit 1",rsd.getString("perawat")));
                    JamSignIn.setText(rsd.getString("jam_signin"));
                    pilih(SiIdentitas,rsd.getString("si_identitas"));
                    pilih(SiArea,rsd.getString("si_area"));
                    pilih(SiAlergi,rsd.getString("si_alergi"));
                    pilih(SiMonitor,rsd.getString("si_monitor"));
                    pilih(SiAirway,rsd.getString("si_airway"));
                    pilih(SiAlat,rsd.getString("si_alat"));
                    pilih(SiAnestesi,rsd.getString("si_anestesi"));
                    SiKontras.setText(rsd.getString("si_kontras"));
                    pilih(SiPerdarahan,rsd.getString("si_perdarahan"));
                    JamTimeOut.setText(rsd.getString("jam_timeout"));
                    pilih(ToPerkenalan,rsd.getString("to_perkenalan"));
                    pilih(ToIdentitas,rsd.getString("to_identitas"));
                    ToPosisi.setText(rsd.getString("to_posisi"));
                    ToProfilaksis.setText(rsd.getString("to_profilaksis"));
                    pilih(ToPenunjang,rsd.getString("to_penunjang"));
                    ToKritis.setText(rsd.getString("to_kritis"));
                    pilih(ToPerdarahan,rsd.getString("to_perdarahan"));
                    pilih(ToKhusus,rsd.getString("to_khusus"));
                    ToLama.setText(rsd.getString("to_lama"));
                    pilih(ToSteril,rsd.getString("to_steril"));
                    ToAlat.setText(rsd.getString("to_alat"));
                    JamSignOut.setText(rsd.getString("jam_signout"));
                    SoTindakan.setText(rsd.getString("so_tindakan"));
                    pilih(SoSpesimen,rsd.getString("so_spesimen"));
                    pilih(SoAlat,rsd.getString("so_alat"));
                    SoMasalah.setText(rsd.getString("so_masalah"));
                    SoOp.setText(rsd.getString("so_op"));
                    SoAnes.setText(rsd.getString("so_anes"));
                    SoPerawat.setText(rsd.getString("so_perawat"));
                    SoRad.setText(rsd.getString("so_rad"));
                    SoKontras.setText(rsd.getString("so_kontras"));
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

    public void isCek(){
        BtnSimpan.setEnabled(akses.gethasil_pemeriksaan_echo());
        BtnHapus.setEnabled(akses.gethasil_pemeriksaan_echo());
        BtnPrint.setEnabled(akses.gethasil_pemeriksaan_echo());
        if(akses.getjml2()>=1){
            KdDokter.setEditable(false);
            BtnDokter.setEnabled(false);
            KdDokter.setText(akses.getkode());
            Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", NmDokter,KdDokter.getText());
            if(NmDokter.getText().equals("")){
                KdDokter.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan Dokter...!!");
            }
        }
    }

    public void emptTeks() {
        KdPerawat.setText("");NmPerawat.setText("");
        JamSignIn.setText("");SiKontras.setText("");
        JamTimeOut.setText("");ToPosisi.setText("");ToProfilaksis.setText("");
        ToKritis.setText("");ToLama.setText("");ToAlat.setText("");
        JamSignOut.setText("");SoTindakan.setText("");SoMasalah.setText("");
        SoOp.setText("");SoAnes.setText("");SoPerawat.setText("");SoRad.setText("");SoKontras.setText("");
        SiIdentitas.setSelectedIndex(0);SiArea.setSelectedIndex(0);SiAlergi.setSelectedIndex(0);
        SiMonitor.setSelectedIndex(0);SiAirway.setSelectedIndex(0);SiAlat.setSelectedIndex(0);
        SiAnestesi.setSelectedIndex(0);SiPerdarahan.setSelectedIndex(0);
        ToPerkenalan.setSelectedIndex(0);ToIdentitas.setSelectedIndex(0);ToPenunjang.setSelectedIndex(0);
        ToPerdarahan.setSelectedIndex(0);ToKhusus.setSelectedIndex(0);ToSteril.setSelectedIndex(0);
        SoSpesimen.setSelectedIndex(0);SoAlat.setSelectedIndex(0);
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMChecklistKeselamatanInvasif dialog = new RMChecklistKeselamatanInvasif(new javax.swing.JFrame(), true);
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
    private widget.Button BtnDokter;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.PanelBiasa FormInput;
    private widget.TextBox JamSignIn;
    private widget.TextBox JamSignOut;
    private widget.TextBox JamTimeOut;
    private widget.TextBox KdDokter;
    private widget.TextBox NmDokter;
    private widget.TextBox KdPerawat;
    private widget.TextBox NmPerawat;
    private widget.Button BtnPerawat;
    private widget.ScrollPane Scroll;
    private widget.ComboBox SiAirway;
    private widget.ComboBox SiAlat;
    private widget.ComboBox SiAlergi;
    private widget.ComboBox SiAnestesi;
    private widget.ComboBox SiArea;
    private widget.ComboBox SiIdentitas;
    private widget.TextBox SiKontras;
    private widget.ComboBox SiMonitor;
    private widget.ComboBox SiPerdarahan;
    private widget.ComboBox SoAlat;
    private widget.TextBox SoAnes;
    private widget.TextBox SoKontras;
    private widget.TextBox SoMasalah;
    private widget.TextBox SoOp;
    private widget.TextBox SoPerawat;
    private widget.TextBox SoRad;
    private widget.ComboBox SoSpesimen;
    private widget.TextBox SoTindakan;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal Tanggal;
    private widget.TextBox TCari;
    private widget.TextBox TglLahir;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.TextBox ToAlat;
    private widget.ComboBox ToIdentitas;
    private widget.ComboBox ToKhusus;
    private widget.TextBox ToKritis;
    private widget.TextBox ToLama;
    private widget.ComboBox ToPenunjang;
    private widget.ComboBox ToPerdarahan;
    private widget.ComboBox ToPerkenalan;
    private widget.TextBox ToPosisi;
    private widget.TextBox ToProfilaksis;
    private widget.ComboBox ToSteril;
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
    private widget.Label label14;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.Table tbObat;
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
