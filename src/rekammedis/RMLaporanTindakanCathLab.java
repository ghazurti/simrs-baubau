/*
 * Formulir Laporan Tindakan Cath Lab
 * Berisi log prosedur cathlab: cara masuk, tempat masuk, jenis tindakan,
 * obat selama tindakan, penyinaran/kontras, observasi pasca kateterisasi,
 * dan instruksi perawatan pasca.
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
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;

public final class RMLaporanTindakanCathLab extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private String norawatPilih="";
    private DlgCariDokter dokter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private int i=0;

    public RMLaporanTindakanCathLab(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        tabMode=new DefaultTableModel(null,new String[]{
            "No.Rawat","No.RM","Nama Pasien","Tanggal","Dokter","No.Tindakan","Cara Masuk"}){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,400));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 7; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0||i==1){ column.setPreferredWidth(115);
            }else if(i==2){ column.setPreferredWidth(180);
            }else if(i==3){ column.setPreferredWidth(120);
            }else if(i==4){ column.setPreferredWidth(170);
            }else if(i==5){ column.setPreferredWidth(90);
            }else{ column.setPreferredWidth(200); }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        NoTindakan.setDocument(new batasInput((int)20).getKata(NoTindakan));
        JamMulai.setDocument(new batasInput((int)5).getKata(JamMulai));
        JamSelesai.setDocument(new batasInput((int)5).getKata(JamSelesai));
        JtLain.setDocument(new batasInput((int)200).getKata(JtLain));
        LamaPenyinaran.setDocument(new batasInput((int)50).getKata(LamaPenyinaran));
        JenisKontras.setDocument(new batasInput((int)50).getKata(JenisKontras));
        VolumeKontras.setDocument(new batasInput((int)20).getKata(VolumeKontras));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnPrint = new widget.Button();
        BtnGanti = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetakFormulir = new javax.swing.JMenuItem();
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
        jLabel30 = new widget.Label();
        NoTindakan = new widget.TextBox();
        jLabel31 = new widget.Label();
        JamMulai = new widget.TextBox();
        jLabelSD = new widget.Label();
        JamSelesai = new widget.TextBox();
        jLabelCara = new widget.Label();
        CaraMasuk = new widget.ComboBox();
        jLabelTempat = new widget.Label();
        TempatMasuk = new widget.ComboBox();
        jSepJT = new javax.swing.JSeparator();
        jLabelJT = new widget.Label();
        CkJKanan = new JCheckBox();
        CkJKiri = new JCheckBox();
        CkKoro = new JCheckBox();
        CkTrans = new JCheckBox();
        CkLV = new JCheckBox();
        CkRV = new JCheckBox();
        CkPA = new JCheckBox();
        CkOA = new JCheckBox();
        CkRenal = new JCheckBox();
        CkArt = new JCheckBox();
        jLabelJTLain = new widget.Label();
        JtLain = new widget.TextBox();
        jLabelObat = new widget.Label();
        jLabelObatHint = new widget.Label();
        scObat = new widget.ScrollPane();
        ObatSelama = new widget.TextArea();
        jLabelPenyinaran = new widget.Label();
        LamaPenyinaran = new widget.TextBox();
        jLabelKontras = new widget.Label();
        JenisKontras = new widget.TextBox();
        jLabelVol = new widget.Label();
        VolumeKontras = new widget.TextBox();
        jLabelObs = new widget.Label();
        jLabelObsHint = new widget.Label();
        scObs = new widget.ScrollPane();
        ObservasiPasca = new widget.TextArea();
        jLabelInst = new widget.Label();
        scInst = new widget.ScrollPane();
        InstruksiPasca = new widget.TextArea();
        lblKet = new javax.swing.JLabel();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        panelGlass9 = new widget.panelisi();
        jLabel7 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel9 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();

        jPopupMenu1.setName("jPopupMenu1");
        MnCetakFormulir.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakFormulir.setFont(new java.awt.Font("Tahoma", 0, 11));
        MnCetakFormulir.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakFormulir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png")));
        MnCetakFormulir.setText("Formulir Laporan Tindakan Cath Lab");
        MnCetakFormulir.setName("MnCetakFormulir");
        MnCetakFormulir.setPreferredSize(new java.awt.Dimension(320, 26));
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Laporan Tindakan Cath Lab ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
        internalFrame1.setName("internalFrame1");
        internalFrame1.setPreferredSize(new java.awt.Dimension(467, 500));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8");
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setForeground(new java.awt.Color(50, 70, 50));
        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan");
        BtnSimpan.addActionListener(evt -> BtnSimpanActionPerformed(evt));
        panelGlass8.add(BtnSimpan);

        BtnBatal.setForeground(new java.awt.Color(50, 70, 50));
        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png")));
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal");
        BtnBatal.addActionListener(evt -> BtnBatalActionPerformed(evt));
        panelGlass8.add(BtnBatal);

        BtnHapus.setForeground(new java.awt.Color(50, 70, 50));
        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus");
        BtnHapus.addActionListener(evt -> BtnHapusActionPerformed(evt));
        panelGlass8.add(BtnHapus);

        BtnGanti.setForeground(new java.awt.Color(50, 70, 50));
        BtnGanti.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png")));
        BtnGanti.setMnemonic('G');
        BtnGanti.setText("Ganti");
        BtnGanti.setToolTipText("Alt+G");
        BtnGanti.setName("BtnGanti");
        BtnGanti.addActionListener(evt -> BtnGantiActionPerformed(evt));
        panelGlass8.add(BtnGanti);

        BtnPrint.setForeground(new java.awt.Color(50, 70, 50));
        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setMnemonic('C');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+C");
        BtnPrint.setName("BtnPrint");
        BtnPrint.addActionListener(evt -> BtnPrintActionPerformed(evt));
        panelGlass8.add(BtnPrint);

        BtnAll.setForeground(new java.awt.Color(50, 70, 50));
        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setMnemonic('E');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+E");
        BtnAll.setName("BtnAll");
        BtnAll.addActionListener(evt -> BtnAllActionPerformed(evt));
        panelGlass8.add(BtnAll);

        BtnKeluar.setForeground(new java.awt.Color(50, 70, 50));
        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar");
        BtnKeluar.addActionListener(evt -> BtnKeluarActionPerformed(evt));
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
        FormInput.setPreferredSize(new java.awt.Dimension(750, 740));
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
        FormInput.add(label14);
        label14.setBounds(0, 40, 70, 23);

        KdDokter.setEditable(false);
        KdDokter.setName("KdDokter");
        FormInput.add(KdDokter);
        KdDokter.setBounds(74, 40, 110, 23);

        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter");
        FormInput.add(NmDokter);
        NmDokter.setBounds(186, 40, 295, 23);

        BtnDokter.setBorder(null);
        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnDokter.setMnemonic('1');
        BtnDokter.setToolTipText("Alt+1");
        BtnDokter.setName("BtnDokter");
        BtnDokter.addActionListener(evt -> BtnDokterActionPerformed(evt));
        FormInput.add(BtnDokter);
        BtnDokter.setBounds(484, 40, 28, 23);

        label11.setText("Tanggal :");
        FormInput.add(label11);
        label11.setBounds(538, 40, 52, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        Tanggal.setName("Tanggal");
        Tanggal.setOpaque(false);
        FormInput.add(Tanggal);
        Tanggal.setBounds(594, 40, 130, 23);

        jSeparator1.setName("jSeparator1");
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(0, 70, 750, 1);

        jLabel30.setText("Nomor Tindakan :");
        FormInput.add(jLabel30);
        jLabel30.setBounds(0, 80, 135, 23);

        NoTindakan.setName("NoTindakan");
        FormInput.add(NoTindakan);
        NoTindakan.setBounds(139, 80, 200, 23);

        jLabel31.setText("Jam Mulai :");
        FormInput.add(jLabel31);
        jLabel31.setBounds(350, 80, 90, 23);

        JamMulai.setName("JamMulai");
        FormInput.add(JamMulai);
        JamMulai.setBounds(442, 80, 70, 23);

        jLabelSD.setText("s/d");
        FormInput.add(jLabelSD);
        jLabelSD.setBounds(516, 80, 30, 23);

        JamSelesai.setName("JamSelesai");
        FormInput.add(JamSelesai);
        JamSelesai.setBounds(544, 80, 70, 23);

        jLabelCara.setText("Cara Masuk :");
        FormInput.add(jLabelCara);
        jLabelCara.setBounds(0, 110, 135, 23);

        CaraMasuk.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Perkutan","Cut-down","Lain-lain","-"}));
        CaraMasuk.setName("CaraMasuk");
        FormInput.add(CaraMasuk);
        CaraMasuk.setBounds(139, 110, 200, 23);

        jLabelTempat.setText("Tempat Masuk :");
        FormInput.add(jLabelTempat);
        jLabelTempat.setBounds(350, 110, 90, 23);

        TempatMasuk.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            "Art/Vena Radialis Kanan","Art/Vena Radialis Kiri",
            "Art/Vena Brachialis Kanan","Art/Vena Brachialis Kiri",
            "Art/Vena Femoralis Kanan","Art/Vena Femoralis Kiri",
            "Lain-lain","-"}));
        TempatMasuk.setName("TempatMasuk");
        FormInput.add(TempatMasuk);
        TempatMasuk.setBounds(442, 110, 285, 23);

        jSepJT.setName("jSepJT");
        FormInput.add(jSepJT);
        jSepJT.setBounds(0, 140, 750, 1);

        jLabelJT.setText("Jenis Tindakan :");
        FormInput.add(jLabelJT);
        jLabelJT.setBounds(0, 148, 135, 23);

        CkJKanan.setText("Penyadapan Jantung Kanan");
        CkJKanan.setBackground(new java.awt.Color(255,255,255));
        FormInput.add(CkJKanan);
        CkJKanan.setBounds(139, 148, 240, 20);

        CkJKiri.setText("Penyadapan Jantung Kiri");
        CkJKiri.setBackground(new java.awt.Color(255,255,255));
        FormInput.add(CkJKiri);
        CkJKiri.setBounds(139, 170, 240, 20);

        CkKoro.setText("Koronarografi");
        CkKoro.setBackground(new java.awt.Color(255,255,255));
        FormInput.add(CkKoro);
        CkKoro.setBounds(139, 192, 240, 20);

        CkTrans.setText("Transeptal");
        CkTrans.setBackground(new java.awt.Color(255,255,255));
        FormInput.add(CkTrans);
        CkTrans.setBounds(139, 214, 240, 20);

        CkLV.setText("LV-grafi");
        CkLV.setBackground(new java.awt.Color(255,255,255));
        FormInput.add(CkLV);
        CkLV.setBounds(139, 236, 240, 20);

        CkRV.setText("RV-grafi");
        CkRV.setBackground(new java.awt.Color(255,255,255));
        FormInput.add(CkRV);
        CkRV.setBounds(400, 148, 240, 20);

        CkPA.setText("PA-grafi");
        CkPA.setBackground(new java.awt.Color(255,255,255));
        FormInput.add(CkPA);
        CkPA.setBounds(400, 170, 240, 20);

        CkOA.setText("OA-grafi");
        CkOA.setBackground(new java.awt.Color(255,255,255));
        FormInput.add(CkOA);
        CkOA.setBounds(400, 192, 240, 20);

        CkRenal.setText("Renal-grafi");
        CkRenal.setBackground(new java.awt.Color(255,255,255));
        FormInput.add(CkRenal);
        CkRenal.setBounds(400, 214, 240, 20);

        CkArt.setText("Arterio-grafi");
        CkArt.setBackground(new java.awt.Color(255,255,255));
        FormInput.add(CkArt);
        CkArt.setBounds(400, 236, 240, 20);

        jLabelJTLain.setText("Lain-lain :");
        FormInput.add(jLabelJTLain);
        jLabelJTLain.setBounds(0, 262, 135, 23);

        JtLain.setName("JtLain");
        FormInput.add(JtLain);
        JtLain.setBounds(139, 262, 585, 23);

        jLabelObat.setText("Obat Selama Tindakan :");
        FormInput.add(jLabelObat);
        jLabelObat.setBounds(0, 295, 155, 23);

        jLabelObatHint.setForeground(new java.awt.Color(120,120,120));
        jLabelObatHint.setText("(format tiap baris: Jam | Obat | Dosis | Cara Pemberian | Keluhan/Alasan)");
        FormInput.add(jLabelObatHint);
        jLabelObatHint.setBounds(160, 295, 570, 23);

        scObat.setName("scObat");
        ObatSelama.setColumns(20);
        ObatSelama.setRows(5);
        ObatSelama.setName("ObatSelama");
        scObat.setViewportView(ObatSelama);
        FormInput.add(scObat);
        scObat.setBounds(0, 320, 724, 90);

        jLabelPenyinaran.setText("Lama Penyinaran :");
        FormInput.add(jLabelPenyinaran);
        jLabelPenyinaran.setBounds(0, 420, 135, 23);

        LamaPenyinaran.setName("LamaPenyinaran");
        FormInput.add(LamaPenyinaran);
        LamaPenyinaran.setBounds(139, 420, 130, 23);

        jLabelKontras.setText("Jenis Kontras :");
        FormInput.add(jLabelKontras);
        jLabelKontras.setBounds(280, 420, 90, 23);

        JenisKontras.setName("JenisKontras");
        FormInput.add(JenisKontras);
        JenisKontras.setBounds(372, 420, 160, 23);

        jLabelVol.setText("Volume :");
        FormInput.add(jLabelVol);
        jLabelVol.setBounds(542, 420, 60, 23);

        VolumeKontras.setName("VolumeKontras");
        FormInput.add(VolumeKontras);
        VolumeKontras.setBounds(604, 420, 120, 23);

        jLabelObs.setText("Observasi Pasca :");
        FormInput.add(jLabelObs);
        jLabelObs.setBounds(0, 452, 155, 23);

        jLabelObsHint.setForeground(new java.awt.Color(120,120,120));
        jLabelObsHint.setText("(tiap baris: Jam | Kesadaran | Tensi | Nadi | RR | Urine | Reaksi | Pulsasi | Hematom)");
        FormInput.add(jLabelObsHint);
        jLabelObsHint.setBounds(160, 452, 570, 23);

        scObs.setName("scObs");
        ObservasiPasca.setColumns(20);
        ObservasiPasca.setRows(5);
        ObservasiPasca.setName("ObservasiPasca");
        scObs.setViewportView(ObservasiPasca);
        FormInput.add(scObs);
        scObs.setBounds(0, 477, 724, 100);

        jLabelInst.setText("Instruksi Perawatan Pasca :");
        FormInput.add(jLabelInst);
        jLabelInst.setBounds(0, 585, 175, 23);

        scInst.setName("scInst");
        InstruksiPasca.setColumns(20);
        InstruksiPasca.setRows(5);
        InstruksiPasca.setName("InstruksiPasca");
        scInst.setViewportView(InstruksiPasca);
        FormInput.add(scInst);
        scInst.setBounds(0, 610, 724, 90);

        lblKet.setFont(new java.awt.Font("Tahoma",0,11));
        lblKet.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblKet.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180,180,180)),
                "KETERANGAN"));
        lblKet.setText("<html><table cellpadding='2'>"
                + "<tr><td colspan='2'><b>Cara Masuk:</b></td></tr>"
                + "<tr><td>Perkutan</td><td>: tusukan jarum</td></tr>"
                + "<tr><td>Cut-down</td><td>: sayatan kecil</td></tr>"
                + "<tr><td colspan='2' style='padding-top:4px'><b>Jenis Tindakan:</b></td></tr>"
                + "<tr><td>LV-grafi</td><td>: Left Ventriculografi</td></tr>"
                + "<tr><td>RV-grafi</td><td>: Right Ventriculografi</td></tr>"
                + "<tr><td>PA-grafi</td><td>: Pulmonary Angiografi</td></tr>"
                + "<tr><td>OA-grafi</td><td>: Ostium Aorta grafi</td></tr>"
                + "<tr><td>Koronarografi</td><td>: pencitraan a. koroner</td></tr>"
                + "<tr><td>Transeptal</td><td>: tusukan septum</td></tr>"
                + "</table></html>");
        FormInput.add(lblKet);
        lblKet.setBounds(740, 80, 380, 620);

        FormInput.setPreferredSize(new java.awt.Dimension(1140, 720));

        scrollInput.setViewportView(FormInput);
        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Laporan Tindakan Cath Lab", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3");
        internalFrame3.setPreferredSize(new java.awt.Dimension(102, 480));
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll");
        Scroll.setPreferredSize(new java.awt.Dimension(102, 557));

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

        jLabel7.setText("Tanggal :");
        panelGlass9.add(jLabel7);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1");
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(100, 23));
        panelGlass9.add(DTPCari1);

        jLabel9.setText("s.d.");
        panelGlass9.add(jLabel9);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2");
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(100, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        panelGlass9.add(jLabel6);

        TCari.setToolTipText("Alt+C");
        TCari.setName("TCari");
        TCari.setPreferredSize(new java.awt.Dimension(160, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) { TCariKeyPressed(evt); }
        });
        panelGlass9.add(TCari);

        BtnCari.setBorder(null);
        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setMnemonic('C');
        BtnCari.setToolTipText("Alt+C");
        BtnCari.setName("BtnCari");
        BtnCari.addActionListener(evt -> BtnCariActionPerformed(evt));
        panelGlass9.add(BtnCari);

        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

        TabRawat.addTab("Data Laporan Tindakan Cath Lab", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        setSize(1200, 674);
        pack();
        // default filter tanggal: 1 tahun ke belakang s.d. hari ini
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, -1);
        DTPCari1.setDate(cal.getTime());
        DTPCari2.setDate(new Date());
    }

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {
        if(TNoRw.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"No.Rawat"); return;
        }
        if(KdDokter.getText().trim().equals("")){
            Valid.textKosong(KdDokter,"Operator"); return;
        }
        String tanggal = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Sequel.queryu2tf("delete from laporan_tindakan_cathlab where no_rawat=?",1,new String[]{TNoRw.getText()});
        if(Sequel.menyimpantf2("laporan_tindakan_cathlab",
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?",25,
                new String[]{
                    TNoRw.getText(), tanggal, KdDokter.getText(), NoTindakan.getText(),
                    JamMulai.getText(), JamSelesai.getText(),
                    CaraMasuk.getSelectedItem().toString(), TempatMasuk.getSelectedItem().toString(),
                    b(CkJKanan), b(CkJKiri), b(CkKoro), b(CkTrans), b(CkLV),
                    b(CkRV), b(CkPA), b(CkOA), b(CkRenal), b(CkArt),
                    JtLain.getText(), ObatSelama.getText(),
                    LamaPenyinaran.getText(), JenisKontras.getText(), VolumeKontras.getText(),
                    ObservasiPasca.getText(), InstruksiPasca.getText()
                })==true){
            java.util.Calendar cal2 = java.util.Calendar.getInstance();
            cal2.add(java.util.Calendar.YEAR, -1);
            DTPCari1.setDate(cal2.getTime());
            DTPCari2.setDate(new Date());
            tampil(); emptyTeks();
            JOptionPane.showMessageDialog(null,"Data laporan tindakan Cath Lab berhasil disimpan.");
        }else{
            JOptionPane.showMessageDialog(null,"Data gagal disimpan, cek koneksi/kolom.");
        }
    }

    private String b(JCheckBox c){ return c.isSelected()?"Ya":"Tidak"; }
    private void setChk(JCheckBox c, String v){ c.setSelected("Ya".equalsIgnoreCase(v)); }

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) { emptyTeks(); }

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {
        if(TNoRw.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"No.Rawat"); return;
        }
        if(JOptionPane.showConfirmDialog(null,"Yakin data mau dihapus?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            if(Sequel.queryu2tf("delete from laporan_tindakan_cathlab where no_rawat=?",1,new String[]{TNoRw.getText()})==true){
                tampil(); emptyTeks();
            }
        }
    }

    private void BtnGantiActionPerformed(java.awt.event.ActionEvent evt) { BtnSimpanActionPerformed(evt); }

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {
        if(TNoRw.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"No.Rawat"); return;
        }
        cetakNoRawat(TNoRw.getText());
    }

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) { tampil(); }

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) { dispose(); }

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
        if (dokter.isVisible()) { dokter.toFront(); return; }
        dokter.setVisible(true);
    }

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) { tampil(); }

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {
        if(evt.getKeyCode()==java.awt.event.KeyEvent.VK_ENTER){ tampil(); }
    }

    public void isCek(){
        BtnSimpan.setEnabled(akses.gethasil_pemeriksaan_echo());
        BtnHapus.setEnabled(akses.gethasil_pemeriksaan_echo());
        BtnPrint.setEnabled(akses.gethasil_pemeriksaan_echo());
        if(akses.getjml2()>=1){
            KdDokter.setEditable(false);
            BtnDokter.setEnabled(false);
            KdDokter.setText(akses.getkode());
            Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", NmDokter, KdDokter.getText());
            if(NmDokter.getText().equals("")){
                KdDokter.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan Dokter...!!");
            }
        }
    }

    public void setNoRm(String norawat, Date tgl2) {
        TNoRw.setText(norawat);
        // paksa filter tanggal ke rentang lebar supaya semua data terlihat
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, -1);
        DTPCari1.setDate(cal.getTime());
        DTPCari2.setDate(new Date());
        Sequel.cariIsi("select pasien.no_rkm_medis from reg_periksa inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis where reg_periksa.no_rawat=?", TNoRM, norawat);
        Sequel.cariIsi("select pasien.nm_pasien from reg_periksa inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis where reg_periksa.no_rawat=?", TPasien, norawat);
        Sequel.cariIsi("select date_format(pasien.tgl_lahir,'%d-%m-%Y') from reg_periksa inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis where reg_periksa.no_rawat=?", TglLahir, norawat);
        tampil();
    }

    public void emptTeks() { emptyTeks(); }

    private void ambilData(String norawat){
        PreparedStatement psd = null;
        ResultSet rsd = null;
        try{
            psd = koneksi.prepareStatement(
                "select l.*, pasien.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir, dokter.nm_dokter "
              + "from laporan_tindakan_cathlab l "
              + "inner join reg_periksa on l.no_rawat=reg_periksa.no_rawat "
              + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
              + "left join dokter on l.kd_dokter=dokter.kd_dokter "
              + "where l.no_rawat=?");
            psd.setString(1, norawat);
            rsd = psd.executeQuery();
            if(rsd.next()){
                TNoRw.setText(rsd.getString("no_rawat"));
                TNoRM.setText(rsd.getString("no_rkm_medis"));
                TPasien.setText(rsd.getString("nm_pasien"));
                TglLahir.setText(rsd.getString("tgl_lahir"));
                KdDokter.setText(rsd.getString("kd_dokter"));
                NmDokter.setText(rsd.getString("nm_dokter"));
                String tgl = rsd.getString("tanggal");
                if(tgl!=null){
                    try { Tanggal.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(tgl)); } catch(Exception ex){}
                }
                NoTindakan.setText(rsd.getString("nomor_tindakan"));
                JamMulai.setText(rsd.getString("jam_mulai"));
                JamSelesai.setText(rsd.getString("jam_selesai"));
                CaraMasuk.setSelectedItem(rsd.getString("cara_masuk"));
                TempatMasuk.setSelectedItem(rsd.getString("tempat_masuk"));
                setChk(CkJKanan, rsd.getString("jt_jantung_kanan"));
                setChk(CkJKiri, rsd.getString("jt_jantung_kiri"));
                setChk(CkKoro, rsd.getString("jt_koronarografi"));
                setChk(CkTrans, rsd.getString("jt_transeptal"));
                setChk(CkLV, rsd.getString("jt_lv_grafi"));
                setChk(CkRV, rsd.getString("jt_rv_grafi"));
                setChk(CkPA, rsd.getString("jt_pa_grafi"));
                setChk(CkOA, rsd.getString("jt_oa_grafi"));
                setChk(CkRenal, rsd.getString("jt_renal_grafi"));
                setChk(CkArt, rsd.getString("jt_arterio_grafi"));
                JtLain.setText(rsd.getString("jt_lainnya"));
                ObatSelama.setText(rsd.getString("obat_selama_tindakan"));
                LamaPenyinaran.setText(rsd.getString("lama_penyinaran"));
                JenisKontras.setText(rsd.getString("jenis_kontras"));
                VolumeKontras.setText(rsd.getString("volume_kontras"));
                ObservasiPasca.setText(rsd.getString("observasi_pasca"));
                InstruksiPasca.setText(rsd.getString("instruksi_pasca"));
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Notifikasi : "+e);
        }finally{
            try { if(rsd!=null) rsd.close(); } catch(Exception e){}
            try { if(psd!=null) psd.close(); } catch(Exception e){}
        }
    }

    private void emptyTeks() {
        NoTindakan.setText(""); JamMulai.setText(""); JamSelesai.setText("");
        CaraMasuk.setSelectedIndex(0); TempatMasuk.setSelectedIndex(0);
        CkJKanan.setSelected(false); CkJKiri.setSelected(false); CkKoro.setSelected(false);
        CkTrans.setSelected(false); CkLV.setSelected(false); CkRV.setSelected(false);
        CkPA.setSelected(false); CkOA.setSelected(false); CkRenal.setSelected(false);
        CkArt.setSelected(false); JtLain.setText("");
        ObatSelama.setText(""); LamaPenyinaran.setText(""); JenisKontras.setText("");
        VolumeKontras.setText(""); ObservasiPasca.setText(""); InstruksiPasca.setText("");
        norawatPilih="";
    }

    private void tampil(){
        try{
            for(i=tabMode.getRowCount()-1;i>=0;i--){ tabMode.removeRow(i); }
            String awal = Valid.SetTgl(DTPCari1.getSelectedItem()+"");
            String akhir = Valid.SetTgl(DTPCari2.getSelectedItem()+"");
            String cari = "%"+TCari.getText().trim()+"%";
            ps = koneksi.prepareStatement(
                "select l.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, l.tanggal, dokter.nm_dokter, l.nomor_tindakan, l.cara_masuk "
              + "from laporan_tindakan_cathlab l "
              + "inner join reg_periksa on l.no_rawat=reg_periksa.no_rawat "
              + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
              + "left join dokter on l.kd_dokter=dokter.kd_dokter "
              + "where date(l.tanggal) between ? and ? "
              + "and (l.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or dokter.nm_dokter like ? or l.nomor_tindakan like ?) "
              + "order by l.tanggal desc");
            ps.setString(1, awal); ps.setString(2, akhir);
            ps.setString(3, cari); ps.setString(4, cari); ps.setString(5, cari);
            ps.setString(6, cari); ps.setString(7, cari);
            rs = ps.executeQuery();
            while(rs.next()){
                tabMode.addRow(new Object[]{
                    rs.getString("no_rawat"), rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"), rs.getString("tanggal"),
                    rs.getString("nm_dokter"), rs.getString("nomor_tindakan"),
                    rs.getString("cara_masuk")
                });
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Notifikasi : "+e);
        }finally{
            try { if(rs!=null) rs.close(); } catch(Exception e){}
            try { if(ps!=null) ps.close(); } catch(Exception e){}
        }
    }

    private void cetakNoRawat(String norawat){
        try{
            Cursor curs = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Map<String,Object> param = new HashMap<>();
            param.put("norawat", norawat);
            param.put("namars", Sequel.cariIsi("select nama_instansi from setting"));
            param.put("alamatrs", Sequel.cariIsi("select alamat_instansi from setting"));
            param.put("kotars", Sequel.cariIsi("select kabupaten from setting"));
            param.put("propinsirs", Sequel.cariIsi("select propinsi from setting"));
            param.put("kontakrs", Sequel.cariIsi("select kontak from setting"));
            param.put("emailrs", Sequel.cariIsi("select email from setting"));
            param.put("logo", Sequel.cariGambar("select logo from setting"));
            Valid.MyReportqrypdf("rptLaporanTindakanCathLab.jasper","report","::[ Laporan Tindakan Cath Lab ]::",
                "select l.no_rawat, date_format(l.tanggal,'%d-%m-%Y %H:%i') as tanggal, l.nomor_tindakan, "
              + "l.jam_mulai, l.jam_selesai, l.cara_masuk, l.tempat_masuk, "
              + "l.jt_jantung_kanan, l.jt_jantung_kiri, l.jt_koronarografi, l.jt_transeptal, l.jt_lv_grafi, "
              + "l.jt_rv_grafi, l.jt_pa_grafi, l.jt_oa_grafi, l.jt_renal_grafi, l.jt_arterio_grafi, l.jt_lainnya, "
              + "l.obat_selama_tindakan, l.lama_penyinaran, l.jenis_kontras, l.volume_kontras, "
              + "l.observasi_pasca, l.instruksi_pasca, "
              + "pasien.no_rkm_medis, pasien.nm_pasien, date_format(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir, pasien.jk, dokter.nm_dokter "
              + "from laporan_tindakan_cathlab l "
              + "inner join reg_periksa on l.no_rawat=reg_periksa.no_rawat "
              + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
              + "left join dokter on l.kd_dokter=dokter.kd_dokter "
              + "where l.no_rawat='"+norawat+"'", param);
            setCursor(curs);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Notifikasi : "+e);
        }
    }

    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private javax.swing.JTabbedPane TabRawat;
    private widget.ScrollPane scrollInput;
    private widget.PanelBiasa FormInput;
    private widget.ScrollPane Scroll;
    private widget.Table tbObat;
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnPrint, BtnGanti, BtnAll, BtnKeluar, BtnDokter, BtnCari;
    private widget.Label jLabel10, jLabel8, label14, label11, jLabel30, jLabel31, jLabelSD, jLabelCara, jLabelTempat, jLabelJT, jLabelJTLain, jLabelObat, jLabelObatHint, jLabelPenyinaran, jLabelKontras, jLabelVol, jLabelObs, jLabelObsHint, jLabelInst, jLabel7, jLabel9, jLabel6;
    private widget.TextBox TNoRw, TNoRM, TPasien, TglLahir, KdDokter, NmDokter, NoTindakan, JamMulai, JamSelesai, JtLain, LamaPenyinaran, JenisKontras, VolumeKontras, TCari;
    private widget.TextArea ObatSelama, ObservasiPasca, InstruksiPasca;
    private widget.ScrollPane scObat, scObs, scInst;
    private widget.ComboBox CaraMasuk, TempatMasuk;
    private widget.Tanggal Tanggal, DTPCari1, DTPCari2;
    private javax.swing.JSeparator jSeparator1, jSepJT;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JMenuItem MnCetakFormulir;
    private JCheckBox CkJKanan, CkJKiri, CkKoro, CkTrans, CkLV, CkRV, CkPA, CkOA, CkRenal, CkArt;
    private javax.swing.JLabel lblKet;
}
