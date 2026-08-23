/*
 * Formulir Penilaian Risiko Jatuh Pasien Cath Lab
 * Pengamatan kondisi, pertanyaan diagnosis, obat-obatan yang meningkatkan
 * risiko jatuh, serta kesimpulan tingkat risiko.
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
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariPetugas;

public final class RMPenilaianRisikoJatuhCathLab extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private String norawatPilih="";
    private DlgCariPetugas petugas;
    private int i=0;

    public RMPenilaianRisikoJatuhCathLab(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        tabMode=new DefaultTableModel(null,new String[]{
            "No.Rawat","No.RM","Nama Pasien","Tanggal","Petugas","Tingkat Risiko"}){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbData.setModel(tabMode);
        tbData.setPreferredScrollableViewportSize(new Dimension(500,400));
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 6; i++) {
            TableColumn column = tbData.getColumnModel().getColumn(i);
            if(i==0||i==1){ column.setPreferredWidth(115);
            }else if(i==2){ column.setPreferredWidth(180);
            }else if(i==3){ column.setPreferredWidth(130);
            }else if(i==4){ column.setPreferredWidth(170);
            }else{ column.setPreferredWidth(120); }
        }
        tbData.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
    }

    private widget.ComboBox combo(String... items){
        widget.ComboBox c = new widget.ComboBox();
        c.setModel(new javax.swing.DefaultComboBoxModel(items));
        return c;
    }

    private widget.Label labelKiri(String teks, int y, int w){
        widget.Label l = new widget.Label();
        l.setText(teks);
        FormInput.add(l);
        l.setBounds(0, y, w, 23);
        return l;
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnCetakFormulir = new javax.swing.JMenuItem();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        TglLahir = new widget.TextBox();
        KdPetugas = new widget.TextBox();
        NmPetugas = new widget.TextBox();
        BtnPetugas = new widget.Button();
        Tanggal = new widget.Tanggal();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbData = new widget.Table();
        panelGlass9 = new widget.panelisi();
        DTPCari1 = new widget.Tanggal();
        DTPCari2 = new widget.Tanggal();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();

        PPenglihatan = combo("Tidak","Ya");
        PKeseimbangan = combo("Tidak","Ya");
        PMobilitas = combo("Tidak","Ya");
        PAlatBantu = combo("Tidak","Ya");
        QUmur = combo("Tidak","Ya");
        QHipertensi = combo("Tidak","Ya");
        QInfark = combo("Tidak","Ya");
        QPenglihatan = combo("Tidak","Ya");
        QDm = combo("Tidak","Ya");
        ONarkotik = combo("Tidak","Ya");
        OAntihipertensi = combo("Tidak","Ya");
        ODiuretik = combo("Tidak","Ya");
        OJantung = combo("Tidak","Ya");
        OPengencer = combo("Tidak","Ya");
        OTetesMata = combo("Tidak","Ya");
        OAntidiabetes = combo("Tidak","Ya");
        TingkatRisiko = combo("Tidak Berisiko","Risiko Sedang","Risiko Tinggi");

        jPopupMenu1.setName("jPopupMenu1");
        MnCetakFormulir.setBackground(new java.awt.Color(255, 255, 254));
        MnCetakFormulir.setFont(new java.awt.Font("Tahoma", 0, 11));
        MnCetakFormulir.setForeground(new java.awt.Color(50, 50, 50));
        MnCetakFormulir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png")));
        MnCetakFormulir.setText("Formulir Penilaian Risiko Jatuh Cath Lab");
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Penilaian Risiko Jatuh Pasien Cath Lab ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
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
        BtnBatal.addActionListener(evt -> emptyTeks());
        panelGlass8.add(BtnBatal);

        BtnHapus.setForeground(new java.awt.Color(50, 70, 50));
        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus");
        BtnHapus.addActionListener(evt -> BtnHapusActionPerformed(evt));
        panelGlass8.add(BtnHapus);

        BtnPrint.setForeground(new java.awt.Color(50, 70, 50));
        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setMnemonic('C');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+C");
        BtnPrint.setName("BtnPrint");
        BtnPrint.addActionListener(evt -> {
            if(TNoRw.getText().trim().equals("")){
                Valid.textKosong(TNoRw,"No.Rawat");
            }else{
                cetakNoRawat(TNoRw.getText());
            }
        });
        panelGlass8.add(BtnPrint);

        BtnAll.setForeground(new java.awt.Color(50, 70, 50));
        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setMnemonic('E');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+E");
        BtnAll.setName("BtnAll");
        BtnAll.addActionListener(evt -> tampil());
        panelGlass8.add(BtnAll);

        BtnKeluar.setForeground(new java.awt.Color(50, 70, 50));
        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar");
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
        FormInput.setPreferredSize(new java.awt.Dimension(750, 810));
        FormInput.setLayout(null);

        labelKiri("No.Rawat :",10,70);
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

        widget.Label lblLahir = new widget.Label();
        lblLahir.setText("Tgl.Lahir :");
        FormInput.add(lblLahir);
        lblLahir.setBounds(580, 10, 60, 23);

        TglLahir.setEditable(false);
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir");
        FormInput.add(TglLahir);
        TglLahir.setBounds(644, 10, 80, 23);

        labelKiri("Petugas :",40,70);
        KdPetugas.setEditable(false);
        KdPetugas.setName("KdPetugas");
        FormInput.add(KdPetugas);
        KdPetugas.setBounds(74, 40, 110, 23);

        NmPetugas.setEditable(false);
        NmPetugas.setName("NmPetugas");
        FormInput.add(NmPetugas);
        NmPetugas.setBounds(186, 40, 295, 23);

        BtnPetugas.setBorder(null);
        BtnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnPetugas.setToolTipText("Cari petugas");
        BtnPetugas.setName("BtnPetugas");
        BtnPetugas.addActionListener(evt -> BtnPetugasActionPerformed(evt));
        FormInput.add(BtnPetugas);
        BtnPetugas.setBounds(484, 40, 28, 23);

        widget.Label lblTgl = new widget.Label();
        lblTgl.setText("Tanggal :");
        FormInput.add(lblTgl);
        lblTgl.setBounds(538, 40, 52, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        Tanggal.setName("Tanggal");
        Tanggal.setOpaque(false);
        FormInput.add(Tanggal);
        Tanggal.setBounds(594, 40, 130, 23);

        javax.swing.JSeparator sep1 = new javax.swing.JSeparator();
        FormInput.add(sep1);
        sep1.setBounds(0, 70, 750, 1);

        int y=80;
        widget.Label s1 = labelKiri("1. PENGAMATAN KONDISI PASIEN",y,420);
        s1.setFont(new java.awt.Font("Tahoma", 1, 11)); y+=28;
        labelKiri("Gangguan penglihatan :",y,420); FormInput.add(PPenglihatan); PPenglihatan.setBounds(424,y,300,23); y+=28;
        labelKiri("Gangguan keseimbangan :",y,420); FormInput.add(PKeseimbangan); PKeseimbangan.setBounds(424,y,300,23); y+=28;
        labelKiri("Gangguan mobilitas fungsional / kelemahan otot :",y,420); FormInput.add(PMobilitas); PMobilitas.setBounds(424,y,300,23); y+=28;
        labelKiri("Menggunakan alat bantu jalan (tongkat, kursi roda) :",y,420); FormInput.add(PAlatBantu); PAlatBantu.setBounds(424,y,300,23); y+=34;

        widget.Label s2 = labelKiri("2. PERTANYAAN",y,420);
        s2.setFont(new java.awt.Font("Tahoma", 1, 11)); y+=28;
        labelKiri("Umur > 60 tahun atau anak < 3 tahun :",y,420); FormInput.add(QUmur); QUmur.setBounds(424,y,300,23); y+=28;
        labelKiri("Hipertensi / hipotensi :",y,420); FormInput.add(QHipertensi); QHipertensi.setBounds(424,y,300,23); y+=28;
        labelKiri("Infark jantung :",y,420); FormInput.add(QInfark); QInfark.setBounds(424,y,300,23); y+=28;
        labelKiri("Gangguan penglihatan belum terkoreksi (glaucoma/katarak) :",y,420); FormInput.add(QPenglihatan); QPenglihatan.setBounds(424,y,300,23); y+=28;
        labelKiri("Diabetes Melitus (polidipsi, poliuri) :",y,420); FormInput.add(QDm); QDm.setBounds(424,y,300,23); y+=34;

        widget.Label s3 = labelKiri("3. OBAT-OBATAN YANG DIMINUM SAAT INI",y,420);
        s3.setFont(new java.awt.Font("Tahoma", 1, 11)); y+=28;
        labelKiri("Narkotik :",y,420); FormInput.add(ONarkotik); ONarkotik.setBounds(424,y,300,23); y+=28;
        labelKiri("Anti hipertensi :",y,420); FormInput.add(OAntihipertensi); OAntihipertensi.setBounds(424,y,300,23); y+=28;
        labelKiri("Diuretik :",y,420); FormInput.add(ODiuretik); ODiuretik.setBounds(424,y,300,23); y+=28;
        labelKiri("Obat penyakit jantung :",y,420); FormInput.add(OJantung); OJantung.setBounds(424,y,300,23); y+=28;
        labelKiri("Pengencer darah :",y,420); FormInput.add(OPengencer); OPengencer.setBounds(424,y,300,23); y+=28;
        labelKiri("Tetes mata yang menyebabkan midriasis :",y,420); FormInput.add(OTetesMata); OTetesMata.setBounds(424,y,300,23); y+=28;
        labelKiri("Obat anti diabetes / insulin :",y,420); FormInput.add(OAntidiabetes); OAntidiabetes.setBounds(424,y,300,23); y+=34;

        widget.Label s4 = labelKiri("TINGKAT RISIKO :",y,420);
        s4.setFont(new java.awt.Font("Tahoma", 1, 11));
        FormInput.add(TingkatRisiko); TingkatRisiko.setBounds(424,y,300,23); y+=30;

        widget.Label cat = labelKiri("Catatan: 1 jawaban YA = risiko sedang, 2 atau lebih jawaban YA = risiko tinggi.",y,724);
        cat.setForeground(new java.awt.Color(120,120,120));

        scrollInput.setViewportView(FormInput);
        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);
        TabRawat.addTab("Input Penilaian Risiko Jatuh", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3");
        internalFrame3.setPreferredSize(new java.awt.Dimension(102, 480));
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll");
        Scroll.setPreferredSize(new java.awt.Dimension(102, 557));

        tbData.setToolTipText("Silahkan klik untuk memilih data");
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

        widget.Label lblT = new widget.Label();
        lblT.setText("Tanggal :");
        panelGlass9.add(lblT);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1");
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(100, 23));
        panelGlass9.add(DTPCari1);

        widget.Label lblSd = new widget.Label();
        lblSd.setText("s.d.");
        panelGlass9.add(lblSd);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2");
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(100, 23));
        panelGlass9.add(DTPCari2);

        widget.Label lblK = new widget.Label();
        lblK.setText("Key Word :");
        panelGlass9.add(lblK);

        TCari.setName("TCari");
        TCari.setPreferredSize(new java.awt.Dimension(160, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if(evt.getKeyCode()==java.awt.event.KeyEvent.VK_ENTER){ tampil(); }
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setBorder(null);
        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setName("BtnCari");
        BtnCari.addActionListener(evt -> tampil());
        panelGlass9.add(BtnCari);

        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);
        TabRawat.addTab("Data Penilaian Risiko Jatuh", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        setSize(1200, 674);
        pack();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, -1);
        DTPCari1.setDate(cal.getTime());
        DTPCari2.setDate(new Date());
    }

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {
        if(TNoRw.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"No.Rawat"); return;
        }
        if(NmPetugas.getText().trim().equals("")){
            Valid.textKosong(NmPetugas,"Petugas"); return;
        }
        String tanggal = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Sequel.queryu2tf("delete from penilaian_risiko_jatuh_cathlab where no_rawat=?",1,new String[]{TNoRw.getText()});
        if(Sequel.menyimpantf2("penilaian_risiko_jatuh_cathlab",
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?",20,
                new String[]{
                    TNoRw.getText(), tanggal, NmPetugas.getText(),
                    v(PPenglihatan), v(PKeseimbangan), v(PMobilitas), v(PAlatBantu),
                    v(QUmur), v(QHipertensi), v(QInfark), v(QPenglihatan), v(QDm),
                    v(ONarkotik), v(OAntihipertensi), v(ODiuretik), v(OJantung),
                    v(OPengencer), v(OTetesMata), v(OAntidiabetes),
                    v(TingkatRisiko)
                })==true){
            java.util.Calendar cal2 = java.util.Calendar.getInstance();
            cal2.add(java.util.Calendar.YEAR, -1);
            DTPCari1.setDate(cal2.getTime());
            DTPCari2.setDate(new Date());
            tampil(); emptyTeks();
            JOptionPane.showMessageDialog(null,"Data penilaian risiko jatuh berhasil disimpan.");
        }else{
            JOptionPane.showMessageDialog(null,"Data gagal disimpan, cek koneksi/kolom.");
        }
    }

    private String v(widget.ComboBox c){ return c.getSelectedItem().toString(); }
    private void setCb(widget.ComboBox c, String val){
        if(val==null) return;
        for(int k=0;k<c.getItemCount();k++){
            if(c.getItemAt(k).toString().equalsIgnoreCase(val)){ c.setSelectedIndex(k); return; }
        }
        c.setSelectedIndex(0);
    }

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {
        if(TNoRw.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"No.Rawat"); return;
        }
        if(JOptionPane.showConfirmDialog(null,"Yakin data mau dihapus?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            if(Sequel.queryu2tf("delete from penilaian_risiko_jatuh_cathlab where no_rawat=?",1,new String[]{TNoRw.getText()})==true){
                tampil(); emptyTeks();
            }
        }
    }

    private void BtnPetugasActionPerformed(java.awt.event.ActionEvent evt) {
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
        petugas.isCek();
        if (petugas.isVisible()) { petugas.toFront(); return; }
        petugas.setVisible(true);
    }

    public void isCek(){
        BtnSimpan.setEnabled(akses.gethasil_pemeriksaan_echo());
        BtnHapus.setEnabled(akses.gethasil_pemeriksaan_echo());
        BtnPrint.setEnabled(akses.gethasil_pemeriksaan_echo());
    }

    public void setNoRm(String norawat, Date tgl2) {
        TNoRw.setText(norawat);
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
                "select l.*, pasien.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir "
              + "from penilaian_risiko_jatuh_cathlab l "
              + "inner join reg_periksa on l.no_rawat=reg_periksa.no_rawat "
              + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
              + "where l.no_rawat=?");
            psd.setString(1, norawat);
            rsd = psd.executeQuery();
            if(rsd.next()){
                TNoRw.setText(rsd.getString("no_rawat"));
                TNoRM.setText(rsd.getString("no_rkm_medis"));
                TPasien.setText(rsd.getString("nm_pasien"));
                TglLahir.setText(rsd.getString("tgl_lahir"));
                NmPetugas.setText(rsd.getString("petugas"));
                setCb(PPenglihatan, rsd.getString("p_penglihatan"));
                setCb(PKeseimbangan, rsd.getString("p_keseimbangan"));
                setCb(PMobilitas, rsd.getString("p_mobilitas"));
                setCb(PAlatBantu, rsd.getString("p_alat_bantu"));
                setCb(QUmur, rsd.getString("q_umur"));
                setCb(QHipertensi, rsd.getString("q_hipertensi"));
                setCb(QInfark, rsd.getString("q_infark"));
                setCb(QPenglihatan, rsd.getString("q_penglihatan"));
                setCb(QDm, rsd.getString("q_dm"));
                setCb(ONarkotik, rsd.getString("o_narkotik"));
                setCb(OAntihipertensi, rsd.getString("o_antihipertensi"));
                setCb(ODiuretik, rsd.getString("o_diuretik"));
                setCb(OJantung, rsd.getString("o_jantung"));
                setCb(OPengencer, rsd.getString("o_pengencer"));
                setCb(OTetesMata, rsd.getString("o_tetes_mata"));
                setCb(OAntidiabetes, rsd.getString("o_antidiabetes"));
                setCb(TingkatRisiko, rsd.getString("tingkat_risiko"));
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Notifikasi : "+e);
        }finally{
            try { if(rsd!=null) rsd.close(); } catch(Exception e){}
            try { if(psd!=null) psd.close(); } catch(Exception e){}
        }
    }

    private void emptyTeks() {
        PPenglihatan.setSelectedIndex(0); PKeseimbangan.setSelectedIndex(0);
        PMobilitas.setSelectedIndex(0); PAlatBantu.setSelectedIndex(0);
        QUmur.setSelectedIndex(0); QHipertensi.setSelectedIndex(0);
        QInfark.setSelectedIndex(0); QPenglihatan.setSelectedIndex(0); QDm.setSelectedIndex(0);
        ONarkotik.setSelectedIndex(0); OAntihipertensi.setSelectedIndex(0);
        ODiuretik.setSelectedIndex(0); OJantung.setSelectedIndex(0);
        OPengencer.setSelectedIndex(0); OTetesMata.setSelectedIndex(0);
        OAntidiabetes.setSelectedIndex(0);
        TingkatRisiko.setSelectedIndex(0);
        norawatPilih="";
    }

    private void tampil(){
        try{
            for(i=tabMode.getRowCount()-1;i>=0;i--){ tabMode.removeRow(i); }
            String awal = Valid.SetTgl(DTPCari1.getSelectedItem()+"");
            String akhir = Valid.SetTgl(DTPCari2.getSelectedItem()+"");
            String cari = "%"+TCari.getText().trim()+"%";
            ps = koneksi.prepareStatement(
                "select l.no_rawat, pasien.no_rkm_medis, pasien.nm_pasien, l.tanggal, l.petugas, l.tingkat_risiko "
              + "from penilaian_risiko_jatuh_cathlab l "
              + "inner join reg_periksa on l.no_rawat=reg_periksa.no_rawat "
              + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
              + "where date(l.tanggal) between ? and ? "
              + "and (l.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or l.petugas like ?) "
              + "order by l.tanggal desc");
            ps.setString(1, awal); ps.setString(2, akhir);
            ps.setString(3, cari); ps.setString(4, cari); ps.setString(5, cari); ps.setString(6, cari);
            rs = ps.executeQuery();
            while(rs.next()){
                tabMode.addRow(new Object[]{
                    rs.getString("no_rawat"), rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"), rs.getString("tanggal"),
                    rs.getString("petugas"), rs.getString("tingkat_risiko")
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
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
            Valid.MyReportqrypdf("rptPenilaianRisikoJatuhCathLab.jasper","report","::[ Penilaian Risiko Jatuh Cath Lab ]::",
                "select l.no_rawat, date_format(l.tanggal,'%d-%m-%Y %H:%i') as tanggal, l.petugas, "
              + "l.p_penglihatan, l.p_keseimbangan, l.p_mobilitas, l.p_alat_bantu, "
              + "l.q_umur, l.q_hipertensi, l.q_infark, l.q_penglihatan, l.q_dm, "
              + "l.o_narkotik, l.o_antihipertensi, l.o_diuretik, l.o_jantung, "
              + "l.o_pengencer, l.o_tetes_mata, l.o_antidiabetes, l.tingkat_risiko, "
              + "pasien.no_rkm_medis, pasien.nm_pasien, date_format(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir, pasien.jk "
              + "from penilaian_risiko_jatuh_cathlab l "
              + "inner join reg_periksa on l.no_rawat=reg_periksa.no_rawat "
              + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
              + "where l.no_rawat='"+norawat+"'", param);
            setCursor(curs);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Notifikasi : "+e);
        }
    }

    private widget.InternalFrame internalFrame1, internalFrame2, internalFrame3;
    private widget.panelisi panelGlass8, panelGlass9;
    private javax.swing.JTabbedPane TabRawat;
    private widget.ScrollPane scrollInput, Scroll;
    private widget.PanelBiasa FormInput;
    private widget.Table tbData;
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnPrint, BtnAll, BtnKeluar, BtnPetugas, BtnCari;
    private widget.TextBox TNoRw, TNoRM, TPasien, TglLahir, KdPetugas, NmPetugas, TCari;
    private widget.Tanggal Tanggal, DTPCari1, DTPCari2;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JMenuItem MnCetakFormulir;
    private widget.ComboBox PPenglihatan, PKeseimbangan, PMobilitas, PAlatBantu,
            QUmur, QHipertensi, QInfark, QPenglihatan, QDm,
            ONarkotik, OAntihipertensi, ODiuretik, OJantung, OPengencer, OTetesMata, OAntidiabetes,
            TingkatRisiko;
}
