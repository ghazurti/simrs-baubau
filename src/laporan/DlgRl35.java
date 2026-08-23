/*
 * Formulir RL 3.5 Rekapitulasi Kunjungan (Juknis SIRS 6.3)
 * Kunjungan Instalasi Rawat Jalan, IGD, Medical Check Up, dan Day Care
 * dipilah menurut domisili pasien (dalam/luar kab/kota RS) dan jenis kelamin.
 */

package laporan;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author perpustakaan
 */
public final class DlgRl35 extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private final List<String> kodeKab = new ArrayList<>();
    private int i=0,dalamL=0,dalamP=0,luarL=0,luarP=0,total=0,
            ttldalamL=0,ttldalamP=0,ttlluarL=0,ttlluarP=0,ttltotal=0;

    /** Creates new form DlgRl35
     * @param parent
     * @param modal */
    public DlgRl35(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        Object[] rowKunjungan={"No.","Jenis Kegiatan","Dalam Kab/Kota (L)","Dalam Kab/Kota (P)","Luar Kab/Kota (L)","Luar Kab/Kota (P)","Total Kunjungan"};
        tabMode=new DefaultTableModel(null,rowKunjungan){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbBangsal.setModel(tabMode);
        tbBangsal.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 7; i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(30);
            }else if(i==1){
                column.setPreferredWidth(230);
            }else{
                column.setPreferredWidth(110);
            }
        }
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable());

        isiKabupaten();
    }

    private void isiKabupaten(){
        try {
            cmbKab.removeAllItems();
            kodeKab.clear();
            koneksi=koneksiDB.condb();
            PreparedStatement psk=koneksi.prepareStatement("select kd_kab, nm_kab from kabupaten order by nm_kab");
            ResultSet rsk=psk.executeQuery();
            while(rsk.next()){
                kodeKab.add(rsk.getString("kd_kab"));
                cmbKab.addItem(rsk.getString("nm_kab"));
            }
            rsk.close();
            psk.close();

            // pilih otomatis kabupaten/kota RS dari setting
            String kabrs=bersihNamaKab(akses.getkabupatenrs());
            if(!kabrs.equals("")){
                for(int k=0;k<cmbKab.getItemCount();k++){
                    String item=bersihNamaKab(cmbKab.getItemAt(k).toString());
                    if(item.contains(kabrs)||kabrs.contains(item)){
                        cmbKab.setSelectedIndex(k);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi isi kabupaten : "+e);
        }
    }

    private String bersihNamaKab(String nama){
        return nama==null?"":nama.toUpperCase()
                .replaceAll("KABUPATEN","").replaceAll("KAB\\.","").replaceAll("KOTA","")
                .replaceAll("[^A-Z]","").trim();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
        panelGlass5 = new widget.panelisi();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        labelKab = new widget.Label();
        cmbKab = new javax.swing.JComboBox<>();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ RL 3.5 Rekapitulasi Kunjungan ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50,50,50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbBangsal.setName("tbBangsal"); // NOI18N
        Scroll.setViewportView(tbBangsal);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass5.setName("panelGlass5"); // NOI18N
        panelGlass5.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(55, 23));
        panelGlass5.add(label11);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl1);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(25, 23));
        panelGlass5.add(label18);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl2);

        labelKab.setText("Kab/Kota RS :");
        labelKab.setName("labelKab"); // NOI18N
        labelKab.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass5.add(labelKab);

        cmbKab.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        cmbKab.setName("cmbKab"); // NOI18N
        cmbKab.setPreferredSize(new java.awt.Dimension(180, 23));
        panelGlass5.add(cmbKab);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        BtnCari.setToolTipText("Alt+2");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        panelGlass5.add(BtnCari);

        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(30, 23));
        panelGlass5.add(jLabel7);

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
        panelGlass5.add(BtnPrint);

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
        panelGlass5.add(BtnKeluar);

        internalFrame1.add(panelGlass5, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
        }else{
            Map<String, Object> param = new HashMap<>();
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());
            param.put("periode",Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem());
            param.put("tanggal",Tgl2.getDate());
            param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
            Sequel.queryu("delete from temporary where temp37='"+akses.getalamatip()+"'");
            for(int r=0;r<tabMode.getRowCount();r++){
                Sequel.menyimpan("temporary","'"+r+"','"+
                                tabMode.getValueAt(r,0).toString()+"','"+
                                tabMode.getValueAt(r,1).toString().replaceAll("'","`")+"','"+
                                tabMode.getValueAt(r,2).toString()+"','"+
                                tabMode.getValueAt(r,3).toString()+"','"+
                                tabMode.getValueAt(r,4).toString()+"','"+
                                tabMode.getValueAt(r,5).toString()+"','"+
                                tabMode.getValueAt(r,6).toString()+"','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','"+akses.getalamatip()+"'","Rekap Nota Pembayaran");
            }

            Valid.MyReportqry("rptRl35.jasper","report","::[ Formulir RL 3.5 ]::","select * from temporary where temporary.temp37='"+akses.getalamatip()+"' order by temporary.no",param);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }
    }

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }
    }

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {
        runBackground(() ->tampil());
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {
        runBackground(() ->tampil());
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgRl35 dialog = new DlgRl35(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCari;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.ScrollPane Scroll;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private javax.swing.JComboBox<String> cmbKab;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel7;
    private widget.Label label11;
    private widget.Label label18;
    private widget.Label labelKab;
    private widget.panelisi panelGlass5;
    private widget.Table tbBangsal;
    // End of variables declaration

    // Kelompokkan tiap poliklinik ke jenis kegiatan RL 3.5 berdasar nama poli.
    // Urutan pengecekan penting: yang spesifik (mis. BEDAH SARAF) harus dicek
    // sebelum yang umum (BEDAH) agar poli tidak salah kelompok.
    private Map<String,List<String>> kelompokkanPoli(){
        String[][] aturan = {
            {"Rawat Darurat","DARURAT","IGD","UGD"},
            {"Bedah Saraf","BEDAH SARAF","BEDAH SYARAF"},
            {"Bedah Orthopedi","ORTHOPEDI","ORTOPEDI"},
            {"Gigi & Mulut","GIGI","MULUT"},
            {"Bedah","BEDAH"},
            {"Penyakit Dalam","DALAM","INTERNA"},
            {"Kesehatan Anak","ANAK","PEDIATRI"},
            {"Obstetri & Ginekologi","OBST","KANDUNGAN","OBGYN","KEBIDANAN","GINEKOLOGI"},
            {"Keluarga Berencana","KELUARGA BERENCANA"},
            {"Napza","NAPZA"},
            {"Jiwa","JIWA","PSIKIATRI"},
            {"Psikologi","PSIKOLOGI"},
            {"THT","THT"},
            {"Mata","MATA"},
            {"Kulit dan Kelamin","KULIT","KELAMIN"},
            {"Geriatri","GERIATRI"},
            {"Kardiologi","KARDIO","JANTUNG"},
            {"Radiologi","RADIOLOGI","RONTGEN"},
            {"Paru - Paru","PARU"},
            {"Kanker","KANKER","ONKOLOGI"},
            {"Saraf","SARAF","SYARAF","NEUROLOGI","NEURO"},
            {"Uronefrologi","UROLOGI","URONEFRO","NEFRO","GINJAL","HEMODIALISA"},
            {"Kusta","KUSTA"},
            {"Rehabilitasi Medik","REHAB","FISIOTERAPI"},
            {"Akupungtur Medik","AKUPUN"},
            {"Konsultasi Gizi","GIZI"},
            {"Day Care","DAY CARE","DAYCARE"},
            {"Medical Check Up","MEDICAL CHECK","MCU","CHECK UP"},
            {"Umum","UMUM"},
        };
        Map<String,List<String>> kelompok = new LinkedHashMap<>();
        for(String[] a : aturan){
            kelompok.put(a[0], new ArrayList<>());
        }
        kelompok.put("Lain - Lain", new ArrayList<>());
        try {
            koneksi=koneksiDB.condb();
            PreparedStatement psp=koneksi.prepareStatement("select kd_poli, nm_poli from poliklinik where kd_poli<>'-'");
            ResultSet rsp=psp.executeQuery();
            while(rsp.next()){
                String kd=rsp.getString("kd_poli");
                String nm=rsp.getString("nm_poli").toUpperCase();
                String tujuan="Lain - Lain";
                if(kd.equals("IGDK")){
                    tujuan="Rawat Darurat";
                }else{
                    cari:
                    for(String[] a : aturan){
                        for(int k=1;k<a.length;k++){
                            if(nm.contains(a[k])){
                                tujuan=a[0];
                                break cari;
                            }
                        }
                    }
                }
                kelompok.get(tujuan).add(kd);
            }
            rsp.close();
            psp.close();
        } catch (Exception e) {
            System.out.println("Notifikasi kelompok poli : "+e);
        }
        return kelompok;
    }

    private String inPoli(List<String> daftar){
        StringBuilder sb=new StringBuilder();
        for(String kd : daftar){
            if(sb.length()>0){
                sb.append(",");
            }
            sb.append("'").append(kd.replaceAll("'","")).append("'");
        }
        return sb.toString();
    }

    public void tampil(){
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(tabMode);
            String tgl1 = Valid.SetTgl(Tgl1.getSelectedItem()+"");
            String tgl2 = Valid.SetTgl(Tgl2.getSelectedItem()+"");
            String kdKabRS = cmbKab.getSelectedIndex()>=0?kodeKab.get(cmbKab.getSelectedIndex()):"";
            // pasien dihitung dalam kab/kota jika kode kabupatennya cocok
            // atau alamatnya mengandung nama kab/kota RS (untuk data lama
            // yang kd_kab-nya tidak terisi)
            String kataKab = cmbKab.getSelectedIndex()>=0?cmbKab.getSelectedItem().toString():"";
            kataKab = kataKab.toUpperCase().replaceAll("KABUPATEN","").replaceAll("KAB\\.","")
                    .replaceAll("^KOTA","").replaceAll("'","").trim();
            String syaratDalam;
            if(kataKab.equals("")){
                syaratDalam = "pasien.kd_kab='"+kdKabRS+"'";
            }else{
                syaratDalam = "(pasien.kd_kab='"+kdKabRS+"' or upper(ifnull(kabupaten.nm_kab,'')) like '%"+kataKab+"%' or upper(pasien.alamat) like '%"+kataKab+"%')";
            }
            String dalamKab = " and "+syaratDalam;
            String luarKab  = " and not "+syaratDalam;

            Map<String,List<String>> kelompok = kelompokkanPoli();

            String diagHamil = " and reg_periksa.no_rawat in (select no_rawat from diagnosa_pasien where kd_penyakit like 'O%' or kd_penyakit like 'Z34%' or kd_penyakit like 'Z35%')";
            // Aturan rekam medis: stroke = seluruh I60-I69, dipisah menurut
            // poli tempat berkunjung (baris 30 = poli bedah saraf,
            // baris 32 = poli saraf). Baris 31 Bedah Saraf (Lainnya) diambil
            // dari diagnosa kasus bedah saraf non-stroke (S06 cedera kepala,
            // C70-C72 tumor SSP, G91 hidrosefalus, M50-M51 HNP) di poli
            // saraf/bedah saraf, dan baris 33 dikurangi kasus tersebut
            // agar tidak ada yang dihitung dobel.
            String subStrokeSemua = "(select no_rawat from diagnosa_pasien where kd_penyakit like 'I6%')";
            String subBedahSarafLain = "(select no_rawat from diagnosa_pasien where kd_penyakit like 'S06%' or kd_penyakit like 'C70%' or kd_penyakit like 'C71%' or kd_penyakit like 'C72%' or kd_penyakit like 'G91%' or kd_penyakit like 'M50%' or kd_penyakit like 'M51%')";
            String dxStroke = " and reg_periksa.no_rawat in "+subStrokeSemua;
            String dxBedahSarafLain = " and reg_periksa.no_rawat not in "+subStrokeSemua+" and reg_periksa.no_rawat in "+subBedahSarafLain;
            String dxSarafLain = " and reg_periksa.no_rawat not in "+subStrokeSemua+" and reg_periksa.no_rawat not in "+subBedahSarafLain;

            List<String> poliSarafGabung = new ArrayList<>();
            poliSarafGabung.addAll(kelompok.get("Bedah Saraf"));
            poliSarafGabung.addAll(kelompok.get("Saraf"));
            kelompok.put("Bedah Saraf + Saraf", poliSarafGabung);

            // [no, nama baris, kunci kelompok poli, kondisi tambahan]
            String[][] baris = {
                {"1","Penyakit Dalam","Penyakit Dalam",""},
                {"2","Bedah","Bedah",""},
                {"3","Kesehatan Anak (Neonatal)","Kesehatan Anak"," and reg_periksa.sttsumur='Hr'"},
                {"4","Kesehatan Anak (Lainnya)","Kesehatan Anak"," and reg_periksa.sttsumur<>'Hr'"},
                {"5","Obstetri & Ginekologi (Ibu Hamil)","Obstetri & Ginekologi",diagHamil},
                {"6","Obstetri & Ginekologi (Lainnya)","Obstetri & Ginekologi",diagHamil.replace(" in (select"," not in (select")},
                {"7","Keluarga Berencana","Keluarga Berencana",""},
                {"8","Jiwa","Jiwa",""},
                {"9","Napza","Napza",""},
                {"10","Psikologi","Psikologi",""},
                {"11","THT","THT",""},
                {"12","Mata","Mata",""},
                {"13","Kulit dan Kelamin","Kulit dan Kelamin",""},
                {"14","Gigi & Mulut","Gigi & Mulut",""},
                {"15","Geriatri","Geriatri",""},
                {"16","Kardiologi","Kardiologi",""},
                {"17","Radiologi","Radiologi",""},
                {"18","Bedah Orthopedi","Bedah Orthopedi",""},
                {"19","Paru - Paru","Paru - Paru",""},
                {"20","Kanker","Kanker",""},
                {"21","Uronefrologi","Uronefrologi",""},
                {"22","Kusta","Kusta",""},
                {"23","Umum","Umum",""},
                {"24","Rawat Darurat","Rawat Darurat",""},
                {"25","Rehabilitasi Medik","Rehabilitasi Medik",""},
                {"26","Akupungtur Medik","Akupungtur Medik",""},
                {"27","Konsultasi Gizi","Konsultasi Gizi",""},
                {"28","Day Care","Day Care",""},
                {"29","Medical Check Up","Medical Check Up",""},
                {"30","Bedah Saraf (Stroke)","Bedah Saraf",dxStroke},
                {"31","Bedah Saraf (Lainnya)","Bedah Saraf + Saraf",dxBedahSarafLain},
                {"32","Saraf (Stroke)","Saraf",dxStroke},
                {"33","Saraf (Lainnya)","Saraf",dxSarafLain},
                {"34","Lain - Lain","Lain - Lain",""},
            };

            // kunjungan dihitung per registrasi, mengikuti laporan
            // Data Kunjungan Rawat Jalan
            String hitung = "select count(distinct reg_periksa.no_rawat) from reg_periksa "+
                    "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                    "left join kabupaten on pasien.kd_kab=kabupaten.kd_kab ";
            String periode = " where reg_periksa.tgl_registrasi between '"+tgl1+"' and '"+tgl2+"' and reg_periksa.stts<>'Batal' ";

            ttldalamL=0;ttldalamP=0;ttlluarL=0;ttlluarP=0;ttltotal=0;

            for(String[] b : baris){
                List<String> daftarPoli = kelompok.get(b[2]);
                if(daftarPoli==null||daftarPoli.isEmpty()){
                    tabMode.addRow(new Object[]{b[0],b[1],0,0,0,0,0});
                    continue;
                }
                String filter = periode+" and reg_periksa.kd_poli in ("+inPoli(daftarPoli)+") "+b[3];
                dalamL = Sequel.cariInteger(hitung+filter+" and pasien.jk='L'"+dalamKab);
                dalamP = Sequel.cariInteger(hitung+filter+" and pasien.jk='P'"+dalamKab);
                luarL  = Sequel.cariInteger(hitung+filter+" and pasien.jk='L'"+luarKab);
                luarP  = Sequel.cariInteger(hitung+filter+" and pasien.jk='P'"+luarKab);
                total  = dalamL+dalamP+luarL+luarP;
                ttldalamL+=dalamL;ttldalamP+=dalamP;ttlluarL+=luarL;ttlluarP+=luarP;
                tabMode.addRow(new Object[]{b[0],b[1],dalamL,dalamP,luarL,luarP,total});
            }

            // baris 99: total kunjungan, pasien yang mengunjungi beberapa unit
            // di hari yang sama dihitung 1 kali (tanpa filter poli)
            int totDalamL = Sequel.cariInteger(hitung+periode+" and pasien.jk='L'"+dalamKab);
            int totDalamP = Sequel.cariInteger(hitung+periode+" and pasien.jk='P'"+dalamKab);
            int totLuarL  = Sequel.cariInteger(hitung+periode+" and pasien.jk='L'"+luarKab);
            int totLuarP  = Sequel.cariInteger(hitung+periode+" and pasien.jk='P'"+luarKab);
            ttltotal = totDalamL+totDalamP+totLuarL+totLuarP;
            tabMode.addRow(new Object[]{"99","TOTAL",totDalamL,totDalamP,totLuarL,totLuarP,ttltotal});

            // baris 66: rata-rata hari poliklinik buka = rata-rata jumlah hari
            // ada kunjungan per poliklinik dalam periode
            double rataHari=0;
            koneksi=koneksiDB.condb();
            ps=koneksi.prepareStatement("select ifnull(avg(hari),0) from (select count(distinct reg_periksa.tgl_registrasi) as hari from reg_periksa "+
                    "where reg_periksa.tgl_registrasi between ? and ? and reg_periksa.stts<>'Batal' group by reg_periksa.kd_poli) hariburka");
            try {
                ps.setString(1,tgl1);
                ps.setString(2,tgl2);
                rs=ps.executeQuery();
                if(rs.next()){
                    rataHari=rs.getDouble(1);
                }
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
            String rataHariStr=String.format(java.util.Locale.US,"%.1f",rataHari);
            tabMode.addRow(new Object[]{"66","Rata-Rata Hari Poliklinik Buka",rataHariStr,rataHariStr,rataHariStr,rataHariStr,rataHariStr});

            // baris 77: rata-rata kunjungan per hari = total kunjungan / rata-rata hari buka
            String rataKunjungan=rataHari>0?String.format(java.util.Locale.US,"%.1f",ttltotal/rataHari):"0.0";
            tabMode.addRow(new Object[]{"77","Rata-Rata Kunjungan per Hari",
                rataHari>0?String.format(java.util.Locale.US,"%.1f",totDalamL/rataHari):"0.0",
                rataHari>0?String.format(java.util.Locale.US,"%.1f",totDalamP/rataHari):"0.0",
                rataHari>0?String.format(java.util.Locale.US,"%.1f",totLuarL/rataHari):"0.0",
                rataHari>0?String.format(java.util.Locale.US,"%.1f",totLuarP/rataHari):"0.0",
                rataKunjungan});

            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
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
}
