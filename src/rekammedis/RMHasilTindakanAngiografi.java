/*
 * Formulir Hasil Tindakan Koroner Angiografi (Cathlab)
 * Laporan hasil per pembuluh koroner + LV Grafi (EDV/ESV/EF/CO),
 * kesimpulan, anjuran, dan gambar hasil.
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

/**
 *
 * @author perpustakaan
 */
public final class RMHasilTindakanAngiografi extends javax.swing.JDialog {
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

    /** Creates new form RMHasilTindakanAngiografi
     * @param parent
     * @param modal */
    public RMHasilTindakanAngiografi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        tabMode=new DefaultTableModel(null,new String[]{
            "No.Rawat","No.RM","Nama Pasien","Tanggal","Dokter","No.Tindakan","Kesimpulan"}){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,400));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 7; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0||i==1){
                column.setPreferredWidth(115);
            }else if(i==2){
                column.setPreferredWidth(180);
            }else if(i==3){
                column.setPreferredWidth(120);
            }else if(i==4){
                column.setPreferredWidth(170);
            }else if(i==5){
                column.setPreferredWidth(90);
            }else{
                column.setPreferredWidth(250);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        NoTindakan.setDocument(new batasInput((int)20).getKata(NoTindakan));
        LM.setDocument(new batasInput((int)200).getKata(LM));
        LAD.setDocument(new batasInput((int)200).getKata(LAD));
        D1.setDocument(new batasInput((int)200).getKata(D1));
        D2.setDocument(new batasInput((int)200).getKata(D2));
        LCx.setDocument(new batasInput((int)200).getKata(LCx));
        OM1.setDocument(new batasInput((int)200).getKata(OM1));
        OM2.setDocument(new batasInput((int)200).getKata(OM2));
        PL.setDocument(new batasInput((int)200).getKata(PL));
        RCA.setDocument(new batasInput((int)200).getKata(RCA));
        RV.setDocument(new batasInput((int)200).getKata(RV));
        AM.setDocument(new batasInput((int)200).getKata(AM));
        PD.setDocument(new batasInput((int)200).getKata(PD));
        LVBranch.setDocument(new batasInput((int)200).getKata(LVBranch));
        EDV.setDocument(new batasInput((int)10).getKata(EDV));
        ESV.setDocument(new batasInput((int)10).getKata(ESV));
        EF.setDocument(new batasInput((int)10).getKata(EF));
        CO.setDocument(new batasInput((int)10).getKata(CO));
        Photo.setDocument(new batasInput((int)500).getKata(Photo));
        PhotoLV.setDocument(new batasInput((int)500).getKata(PhotoLV));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
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
        jLabel30 = new widget.Label();
        NoTindakan = new widget.TextBox();
        jLabel31 = new widget.Label();
        LM = new widget.TextBox();
        jLabel32 = new widget.Label();
        LAD = new widget.TextBox();
        jLabel33 = new widget.Label();
        D1 = new widget.TextBox();
        jLabel34 = new widget.Label();
        D2 = new widget.TextBox();
        jLabel35 = new widget.Label();
        LCx = new widget.TextBox();
        jLabel36 = new widget.Label();
        OM1 = new widget.TextBox();
        jLabel37 = new widget.Label();
        OM2 = new widget.TextBox();
        jLabel38 = new widget.Label();
        PL = new widget.TextBox();
        jLabel39 = new widget.Label();
        RCA = new widget.TextBox();
        jLabel40 = new widget.Label();
        RV = new widget.TextBox();
        jLabel41 = new widget.Label();
        AM = new widget.TextBox();
        jLabel42 = new widget.Label();
        PD = new widget.TextBox();
        jLabel43 = new widget.Label();
        LVBranch = new widget.TextBox();
        jLabel44 = new widget.Label();
        EDV = new widget.TextBox();
        jLabel45 = new widget.Label();
        ESV = new widget.TextBox();
        jLabel46 = new widget.Label();
        EF = new widget.TextBox();
        jLabel47 = new widget.Label();
        CO = new widget.TextBox();
        jLabel48 = new widget.Label();
        scrollPane17 = new widget.ScrollPane();
        Kesimpulan = new widget.TextArea();
        jLabel49 = new widget.Label();
        scrollPane18 = new widget.ScrollPane();
        Anjuran = new widget.TextArea();
        jLabel50 = new widget.Label();
        Photo = new widget.TextBox();
        jLabel51 = new widget.Label();
        PhotoLV = new widget.TextBox();
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
        MnCetakFormulir.setText("Formulir Hasil Tindakan Koroner Angiografi");
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Hasil Tindakan Koroner Angiografi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
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
        FormInput.setPreferredSize(new java.awt.Dimension(1150, 790));
        FormInput.setLayout(null);

        javax.swing.JLabel lblKeteranganAngio = new javax.swing.JLabel();
        lblKeteranganAngio.setFont(new java.awt.Font("Tahoma", 0, 11));
        lblKeteranganAngio.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblKeteranganAngio.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180,180,180)),
                "KETERANGAN"));
        lblKeteranganAngio.setText("<html><table cellpadding='2'>"
                + "<tr><td><b>LCA/LM</b></td><td>: Left Coronary Artery /</td></tr>"
                + "<tr><td></td><td>&nbsp;&nbsp;Left Main Coronary Artery</td></tr>"
                + "<tr><td><b>LAD</b></td><td>: Left Anterior Descending</td></tr>"
                + "<tr><td></td><td>&nbsp;&nbsp;Coronary Artery</td></tr>"
                + "<tr><td><b>D1</b></td><td>: Diagonal 1</td></tr>"
                + "<tr><td><b>D2</b></td><td>: Diagonal 2</td></tr>"
                + "<tr><td><b>Cx</b></td><td>: Circumflex Coronary Artery</td></tr>"
                + "<tr><td><b>OM1</b></td><td>: Obtuse Marginal 1</td></tr>"
                + "<tr><td><b>OM2</b></td><td>: Obtuse Marginal 2</td></tr>"
                + "<tr><td><b>PL</b></td><td>: Postero Lateral</td></tr>"
                + "<tr><td><b>RCA</b></td><td>: Right Coronary Artery</td></tr>"
                + "<tr><td><b>RV</b></td><td>: Right Ventrikel</td></tr>"
                + "<tr><td><b>AM</b></td><td>: Acute Marginal</td></tr>"
                + "<tr><td><b>PD</b></td><td>: Postero Desending</td></tr>"
                + "<tr><td><b>LV Branch</b></td><td>: Left Ventrikel Branch</td></tr>"
                + "</table></html>");
        FormInput.add(lblKeteranganAngio);
        lblKeteranganAngio.setBounds(760, 10, 370, 430);

        javax.swing.JLabel lblGambarJantung = new javax.swing.JLabel();
        lblGambarJantung.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblGambarJantung.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180,180,180)),
                "ANATOMI JANTUNG"));
        try {
            java.net.URL urlJantung = getClass().getResource("/picture/jantung_koroner.png");
            if (urlJantung != null) {
                javax.swing.ImageIcon icoJantung = new javax.swing.ImageIcon(urlJantung);
                java.awt.Image imgSc = icoJantung.getImage().getScaledInstance(340, 300, java.awt.Image.SCALE_SMOOTH);
                lblGambarJantung.setIcon(new javax.swing.ImageIcon(imgSc));
            } else {
                lblGambarJantung.setText("<html><center>(gambar jantung<br>belum tersedia)</center></html>");
            }
        } catch(Exception ex){}
        FormInput.add(lblGambarJantung);
        lblGambarJantung.setBounds(760, 450, 370, 330);

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

        jLabel30.setText("Nomor Tindakan :");
        FormInput.add(jLabel30);
        jLabel30.setBounds(0, 80, 135, 23);
        NoTindakan.setName("NoTindakan");
        FormInput.add(NoTindakan);
        NoTindakan.setBounds(139, 80, 200, 23);

        jLabel31.setText("LCA/LM :");
        FormInput.add(jLabel31);
        jLabel31.setBounds(0, 110, 135, 23);
        LM.setName("LM");
        FormInput.add(LM);
        LM.setBounds(139, 110, 585, 23);

        jLabel32.setText("LAD :");
        FormInput.add(jLabel32);
        jLabel32.setBounds(0, 140, 135, 23);
        LAD.setName("LAD");
        FormInput.add(LAD);
        LAD.setBounds(139, 140, 585, 23);

        jLabel33.setText("- D1 :");
        FormInput.add(jLabel33);
        jLabel33.setBounds(20, 170, 115, 23);
        D1.setName("D1");
        FormInput.add(D1);
        D1.setBounds(139, 170, 585, 23);

        jLabel34.setText("- D2 :");
        FormInput.add(jLabel34);
        jLabel34.setBounds(20, 200, 115, 23);
        D2.setName("D2");
        FormInput.add(D2);
        D2.setBounds(139, 200, 585, 23);

        jLabel35.setText("LCx :");
        FormInput.add(jLabel35);
        jLabel35.setBounds(0, 230, 135, 23);
        LCx.setName("LCx");
        FormInput.add(LCx);
        LCx.setBounds(139, 230, 585, 23);

        jLabel36.setText("- OM1 :");
        FormInput.add(jLabel36);
        jLabel36.setBounds(20, 260, 115, 23);
        OM1.setName("OM1");
        FormInput.add(OM1);
        OM1.setBounds(139, 260, 585, 23);

        jLabel37.setText("- OM2 :");
        FormInput.add(jLabel37);
        jLabel37.setBounds(20, 290, 115, 23);
        OM2.setName("OM2");
        FormInput.add(OM2);
        OM2.setBounds(139, 290, 585, 23);

        jLabel38.setText("- PL :");
        FormInput.add(jLabel38);
        jLabel38.setBounds(20, 320, 115, 23);
        PL.setName("PL");
        FormInput.add(PL);
        PL.setBounds(139, 320, 585, 23);

        jLabel39.setText("RCA :");
        FormInput.add(jLabel39);
        jLabel39.setBounds(0, 350, 135, 23);
        RCA.setName("RCA");
        FormInput.add(RCA);
        RCA.setBounds(139, 350, 585, 23);

        jLabel40.setText("- RV :");
        FormInput.add(jLabel40);
        jLabel40.setBounds(20, 380, 115, 23);
        RV.setName("RV");
        FormInput.add(RV);
        RV.setBounds(139, 380, 585, 23);

        jLabel41.setText("- AM :");
        FormInput.add(jLabel41);
        jLabel41.setBounds(20, 410, 115, 23);
        AM.setName("AM");
        FormInput.add(AM);
        AM.setBounds(139, 410, 585, 23);

        jLabel42.setText("- PD :");
        FormInput.add(jLabel42);
        jLabel42.setBounds(20, 440, 115, 23);
        PD.setName("PD");
        FormInput.add(PD);
        PD.setBounds(139, 440, 585, 23);

        jLabel43.setText("- LV Branch :");
        FormInput.add(jLabel43);
        jLabel43.setBounds(20, 470, 115, 23);
        LVBranch.setName("LVBranch");
        FormInput.add(LVBranch);
        LVBranch.setBounds(139, 470, 585, 23);

        jLabel44.setText("LV Grafi - EDV (ml) :");
        FormInput.add(jLabel44);
        jLabel44.setBounds(0, 500, 135, 23);
        EDV.setName("EDV");
        FormInput.add(EDV);
        EDV.setBounds(139, 500, 80, 23);

        jLabel45.setText("ESV (ml) :");
        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        FormInput.add(jLabel45);
        jLabel45.setBounds(225, 500, 70, 23);
        ESV.setName("ESV");
        FormInput.add(ESV);
        ESV.setBounds(299, 500, 80, 23);

        jLabel46.setText("EF (%) :");
        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        FormInput.add(jLabel46);
        jLabel46.setBounds(385, 500, 60, 23);
        EF.setName("EF");
        FormInput.add(EF);
        EF.setBounds(449, 500, 80, 23);

        jLabel47.setText("CO (ml/min) :");
        jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        FormInput.add(jLabel47);
        jLabel47.setBounds(535, 500, 85, 23);
        CO.setName("CO");
        FormInput.add(CO);
        CO.setBounds(624, 500, 80, 23);

        jLabel48.setText("Kesimpulan :");
        FormInput.add(jLabel48);
        jLabel48.setBounds(0, 530, 135, 23);
        scrollPane17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Kesimpulan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Kesimpulan.setColumns(20);
        Kesimpulan.setRows(5);
        Kesimpulan.setName("Kesimpulan");
        scrollPane17.setViewportView(Kesimpulan);
        FormInput.add(scrollPane17);
        scrollPane17.setBounds(139, 530, 585, 73);

        jLabel49.setText("Anjuran :");
        FormInput.add(jLabel49);
        jLabel49.setBounds(0, 610, 135, 23);
        scrollPane18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Anjuran.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Anjuran.setColumns(20);
        Anjuran.setRows(5);
        Anjuran.setName("Anjuran");
        scrollPane18.setViewportView(Anjuran);
        FormInput.add(scrollPane18);
        scrollPane18.setBounds(139, 610, 585, 73);

        jLabel50.setText("File Gambar Koroner :");
        FormInput.add(jLabel50);
        jLabel50.setBounds(0, 690, 135, 23);
        Photo.setName("Photo");
        FormInput.add(Photo);
        Photo.setBounds(139, 690, 585, 23);

        jLabel51.setText("File Gambar LV Grafi :");
        FormInput.add(jLabel51);
        jLabel51.setBounds(0, 720, 135, 23);
        PhotoLV.setName("PhotoLV");
        FormInput.add(PhotoLV);
        PhotoLV.setBounds(139, 720, 585, 23);

        scrollInput.setViewportView(FormInput);
        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);
        TabRawat.addTab("Input Hasil Tindakan Angiografi", internalFrame2);

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
        TabRawat.addTab("Data Hasil Tindakan Angiografi", internalFrame3);

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
        try {
            // satu hasil per no_rawat : jika sudah ada, timpa
            Sequel.queryu2tf("delete from hasil_tindakan_angiografi where no_rawat=?",1,new String[]{TNoRw.getText()});
            boolean sukses=Sequel.menyimpantf2("hasil_tindakan_angiografi","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?",23,new String[]{
                TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19),
                KdDokter.getText(),NoTindakan.getText(),
                LM.getText(),LAD.getText(),D1.getText(),D2.getText(),
                LCx.getText(),OM1.getText(),OM2.getText(),PL.getText(),
                RCA.getText(),RV.getText(),AM.getText(),PD.getText(),LVBranch.getText(),
                EDV.getText(),ESV.getText(),EF.getText(),CO.getText(),
                Kesimpulan.getText(),Anjuran.getText()
            });
            if(sukses){
                if(!Photo.getText().trim().equals("")||!PhotoLV.getText().trim().equals("")){
                    Sequel.menyimpantf2("hasil_tindakan_angiografi_gambar","?,?,?",3,new String[]{
                        TNoRw.getText(),Photo.getText(),PhotoLV.getText()
                    });
                }
                JOptionPane.showMessageDialog(null,"Data hasil tindakan angiografi berhasil disimpan...");
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
        if(JOptionPane.showConfirmDialog(null,"Yakin data hasil tindakan pasien ini dihapus?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            Sequel.queryu2tf("delete from hasil_tindakan_angiografi where no_rawat=?",1,new String[]{TNoRw.getText()});
            emptTeks();
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
        try { param.put("gbrjantung", getClass().getResourceAsStream("/picture/Gambar2.png")); } catch(Exception ex){}
        try { param.put("gbr", getClass().getResourceAsStream("/picture/jantung_koroner.png")); } catch(Exception ex){}
        try { param.put("gbrlv", getClass().getResourceAsStream("/picture/Gambar2.png")); } catch(Exception ex){}
        Valid.MyReportqrypdf("rptHasilTindakanAngiografi.jasper","report","::[ Hasil Tindakan Koroner Angiografi ]::",
            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,date_format(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir,"+
            "date_format(hasil_tindakan_angiografi.tanggal,'%d-%m-%Y') as tanggal,dokter.nm_dokter,hasil_tindakan_angiografi.nomor_tindakan,"+
            "hasil_tindakan_angiografi.lm,hasil_tindakan_angiografi.lad,hasil_tindakan_angiografi.d1,hasil_tindakan_angiografi.d2,"+
            "hasil_tindakan_angiografi.lcx,hasil_tindakan_angiografi.om1,hasil_tindakan_angiografi.om2,hasil_tindakan_angiografi.pl,"+
            "hasil_tindakan_angiografi.rca,hasil_tindakan_angiografi.rv,hasil_tindakan_angiografi.am,hasil_tindakan_angiografi.pd,"+
            "hasil_tindakan_angiografi.lv_branch,hasil_tindakan_angiografi.edv,hasil_tindakan_angiografi.esv,hasil_tindakan_angiografi.ef,"+
            "hasil_tindakan_angiografi.co,hasil_tindakan_angiografi.kesimpulan,hasil_tindakan_angiografi.anjuran "+
            "from hasil_tindakan_angiografi inner join reg_periksa on reg_periksa.no_rawat=hasil_tindakan_angiografi.no_rawat "+
            "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
            "inner join dokter on dokter.kd_dokter=hasil_tindakan_angiografi.kd_dokter "+
            "where hasil_tindakan_angiografi.no_rawat='"+norawat+"'",param);
        this.setCursor(Cursor.getDefaultCursor());
    }

    public void tampil(){
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(tabMode);
            koneksi=koneksiDB.condb();
            ps=koneksi.prepareStatement(
                "select hasil_tindakan_angiografi.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,hasil_tindakan_angiografi.tanggal,"+
                "dokter.nm_dokter,hasil_tindakan_angiografi.nomor_tindakan,hasil_tindakan_angiografi.kesimpulan "+
                "from hasil_tindakan_angiografi inner join reg_periksa on reg_periksa.no_rawat=hasil_tindakan_angiografi.no_rawat "+
                "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                "inner join dokter on dokter.kd_dokter=hasil_tindakan_angiografi.kd_dokter "+
                "where (hasil_tindakan_angiografi.tanggal between ? and ? or hasil_tindakan_angiografi.no_rawat=?) "+
                "and (hasil_tindakan_angiografi.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or dokter.nm_dokter like ?) "+
                "order by hasil_tindakan_angiografi.tanggal");
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
                        rs.getString("tanggal"),rs.getString("nm_dokter"),rs.getString("nomor_tindakan"),rs.getString("kesimpulan")
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
            PreparedStatement psd=koneksi.prepareStatement(
                "select hasil_tindakan_angiografi.*,ifnull(gambar.photo,'') as photo,ifnull(gambar.photo_lv,'') as photo_lv "+
                "from hasil_tindakan_angiografi left join hasil_tindakan_angiografi_gambar gambar "+
                "on gambar.no_rawat=hasil_tindakan_angiografi.no_rawat where hasil_tindakan_angiografi.no_rawat=?");
                psd.setString(1,norawat);
                ResultSet rsd=psd.executeQuery();
                if(rsd.next()){
                    setNoRm(norawat,DTPCari2.getDate());
                    NoTindakan.setText(rsd.getString("nomor_tindakan"));
                    KdDokter.setText(rsd.getString("kd_dokter"));
                    Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", NmDokter,KdDokter.getText());
                    LM.setText(rsd.getString("lm"));
                    LAD.setText(rsd.getString("lad"));
                    D1.setText(rsd.getString("d1"));
                    D2.setText(rsd.getString("d2"));
                    LCx.setText(rsd.getString("lcx"));
                    OM1.setText(rsd.getString("om1"));
                    OM2.setText(rsd.getString("om2"));
                    PL.setText(rsd.getString("pl"));
                    RCA.setText(rsd.getString("rca"));
                    RV.setText(rsd.getString("rv"));
                    AM.setText(rsd.getString("am"));
                    PD.setText(rsd.getString("pd"));
                    LVBranch.setText(rsd.getString("lv_branch"));
                    EDV.setText(rsd.getString("edv"));
                    ESV.setText(rsd.getString("esv"));
                    EF.setText(rsd.getString("ef"));
                    CO.setText(rsd.getString("co"));
                    Kesimpulan.setText(rsd.getString("kesimpulan"));
                    Anjuran.setText(rsd.getString("anjuran"));
                    Photo.setText(rsd.getString("photo"));
                    PhotoLV.setText(rsd.getString("photo_lv"));
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
        NoTindakan.setText("");
        LM.setText("");LAD.setText("");D1.setText("");D2.setText("");
        LCx.setText("");OM1.setText("");OM2.setText("");PL.setText("");
        RCA.setText("");RV.setText("");AM.setText("");PD.setText("");LVBranch.setText("");
        EDV.setText("");ESV.setText("");EF.setText("");CO.setText("");
        Kesimpulan.setText("");Anjuran.setText("");
        Photo.setText("");PhotoLV.setText("");
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMHasilTindakanAngiografi dialog = new RMHasilTindakanAngiografi(new javax.swing.JFrame(), true);
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
    private widget.TextBox AM;
    private widget.TextArea Anjuran;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnDokter;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.TextBox CO;
    private widget.TextBox D1;
    private widget.TextBox D2;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.TextBox EDV;
    private widget.TextBox EF;
    private widget.TextBox ESV;
    private widget.PanelBiasa FormInput;
    private widget.TextBox KdDokter;
    private widget.TextArea Kesimpulan;
    private widget.TextBox LAD;
    private widget.TextBox LCx;
    private widget.TextBox LM;
    private widget.TextBox LVBranch;
    private widget.TextBox NmDokter;
    private widget.TextBox NoTindakan;
    private widget.TextBox OM1;
    private widget.TextBox OM2;
    private widget.TextBox PD;
    private widget.TextBox PL;
    private widget.TextBox Photo;
    private widget.TextBox PhotoLV;
    private widget.TextBox RCA;
    private widget.TextBox RV;
    private widget.ScrollPane Scroll;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal Tanggal;
    private widget.TextBox TCari;
    private widget.TextBox TglLahir;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel35;
    private widget.Label jLabel36;
    private widget.Label jLabel37;
    private widget.Label jLabel38;
    private widget.Label jLabel39;
    private widget.Label jLabel40;
    private widget.Label jLabel41;
    private widget.Label jLabel42;
    private widget.Label jLabel43;
    private widget.Label jLabel44;
    private widget.Label jLabel45;
    private widget.Label jLabel46;
    private widget.Label jLabel47;
    private widget.Label jLabel48;
    private widget.Label jLabel49;
    private widget.Label jLabel50;
    private widget.Label jLabel51;
    private widget.Label jLabel6;
    private widget.Label jLabel8;
    private javax.swing.JSeparator jSeparator1;
    private widget.Label label11;
    private widget.Label label14;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane17;
    private widget.ScrollPane scrollPane18;
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
