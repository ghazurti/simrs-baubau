/*
 * Formulir RL 3.10 Rekapitulasi Kegiatan Pelayanan Rujukan (Juknis SIRS 6.3)
 * Rujukan masuk (diterima dari & dikembalikan ke Puskesmas/RS lain/faskes lain)
 * dan dirujuk keluar (pasien rujukan/datang sendiri/diterima kembali)
 * per jenis spesialisasi.
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
public final class DlgRl310 extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private int i=0;

    /** Creates new form DlgRl310
     * @param parent
     * @param modal */
    public DlgRl310(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        Object[] rowRujukan={"No.","Jenis Spesialisasi",
            "RM Dari Puskesmas","RM Dari RS Lain","RM Dari Faskes Lain","Total Rujukan Masuk",
            "Kembali Ke Puskesmas","Kembali Ke RS Asal","Kembali Ke Faskes Lain","Total Dikembalikan",
            "DK Pasien Rujukan","DK Datang Sendiri","Total Dirujuk Keluar","Diterima Kembali"};
        tabMode=new DefaultTableModel(null,rowRujukan){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbBangsal.setModel(tabMode);
        tbBangsal.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 14; i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(30);
            }else if(i==1){
                column.setPreferredWidth(160);
            }else{
                column.setPreferredWidth(115);
            }
        }
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable());
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ RL 3.10 Rekapitulasi Kegiatan Pelayanan Rujukan ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50,50,50))); // NOI18N
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
                                tabMode.getValueAt(r,6).toString()+"','"+
                                tabMode.getValueAt(r,7).toString()+"','"+
                                tabMode.getValueAt(r,8).toString()+"','"+
                                tabMode.getValueAt(r,9).toString()+"','"+
                                tabMode.getValueAt(r,10).toString()+"','"+
                                tabMode.getValueAt(r,11).toString()+"','"+
                                tabMode.getValueAt(r,12).toString()+"','"+
                                tabMode.getValueAt(r,13).toString()+"','','','','','','','','','','','','','','','','','','','','','','','"+akses.getalamatip()+"'","Rekap Nota Pembayaran");
            }

            Valid.MyReportqry("rptRl310.jasper","report","::[ Formulir RL 3.10 ]::","select * from temporary where temporary.temp37='"+akses.getalamatip()+"' order by temporary.no",param);
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
            DlgRl310 dialog = new DlgRl310(new javax.swing.JFrame(), true);
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
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel7;
    private widget.Label label11;
    private widget.Label label18;
    private widget.panelisi panelGlass5;
    private widget.Table tbBangsal;
    // End of variables declaration

    // Kelompokkan tiap poliklinik ke jenis spesialisasi RL 3.10 berdasar nama
    // poli. Urutan pengecekan penting: yang spesifik harus dicek lebih dulu.
    private Map<String,List<String>> kelompokkanPoli(){
        String[][] aturan = {
            {"Kesehatan Remaja","REMAJA","PKPR"},
            {"Saraf","BEDAH SARAF","BEDAH SYARAF","SARAF","SYARAF","NEUROLOGI"},
            {"Gigi dan Mulut","GIGI","MULUT"},
            {"Bedah","BEDAH","ORTHOPEDI","ORTOPEDI"},
            {"Penyakit Dalam","DALAM","INTERNA"},
            {"Kesehatan Anak","ANAK","PEDIATRI"},
            {"Obstetri & Ginekologi","OBST","KANDUNGAN","OBGYN","KEBIDANAN","GINEKOLOGI"},
            {"Keluarga Berencana","KELUARGA BERENCANA"},
            {"Jiwa","JIWA","PSIKIATRI","NAPZA"},
            {"THT","THT"},
            {"Mata","MATA"},
            {"Kulit dan Kelamin","KULIT","KELAMIN"},
            {"Radiologi","RADIOLOGI","RONTGEN"},
            {"Paru","PARU"},
            {"Kardiologi","KARDIO","JANTUNG"},
            {"Kanker","KANKER","ONKOLOGI"},
            {"Uronefrologi","URO","NEFRO","GINJAL","HEMODIALISA"},
        };
        Map<String,List<String>> kelompok = new LinkedHashMap<>();
        for(String[] a : aturan){
            kelompok.put(a[0], new ArrayList<>());
        }
        kelompok.put("Spesialisasi Lain", new ArrayList<>());
        try {
            koneksi=koneksiDB.condb();
            PreparedStatement psp=koneksi.prepareStatement("select kd_poli, nm_poli from poliklinik where kd_poli<>'-'");
            ResultSet rsp=psp.executeQuery();
            while(rsp.next()){
                String kd=rsp.getString("kd_poli");
                String nm=rsp.getString("nm_poli").toUpperCase();
                String tujuan="Spesialisasi Lain";
                cari:
                for(String[] a : aturan){
                    for(int k=1;k<a.length;k++){
                        if(nm.contains(a[k])){
                            tujuan=a[0];
                            break cari;
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

            Map<String,List<String>> kelompok = kelompokkanPoli();

            String diagHamil = " and reg_periksa.no_rawat in (select no_rawat from diagnosa_pasien where kd_penyakit like 'O%' or kd_penyakit like 'Z34%' or kd_penyakit like 'Z35%')";
            String diagStroke = " and reg_periksa.no_rawat in (select no_rawat from diagnosa_pasien where kd_penyakit like 'I6%')";

            // [no, nama baris, kunci kelompok poli, kondisi tambahan]
            String[][] baris = {
                {"1","Penyakit Dalam","Penyakit Dalam",""},
                {"2","Bedah","Bedah",""},
                {"3","Kesehatan Anak","Kesehatan Anak",""},
                {"4","Kesehatan Remaja","Kesehatan Remaja",""},
                {"5","Obstetri","Obstetri & Ginekologi",diagHamil},
                {"6","Ginekologi","Obstetri & Ginekologi",diagHamil.replace(" in (select"," not in (select")},
                {"7","Keluarga Berencana","Keluarga Berencana",""},
                {"8","Saraf (Non Stroke)","Saraf",diagStroke.replace(" in (select"," not in (select")},
                {"9","Jiwa","Jiwa",""},
                {"10","THT","THT",""},
                {"11","Mata","Mata",""},
                {"12","Kulit dan Kelamin","Kulit dan Kelamin",""},
                {"13","Gigi dan Mulut","Gigi dan Mulut",""},
                {"14","Radiologi","Radiologi",""},
                {"15","Paru","Paru",""},
                {"16","Kardiologi","Kardiologi",""},
                {"17","Kanker","Kanker",""},
                {"18","Uronefrologi","Uronefrologi",""},
                {"19","Saraf (Stroke)","Saraf",diagStroke},
                {"20","Spesialisasi Lain","Spesialisasi Lain",""},
            };

            // jenis faskes perujuk : utamakan kode PPK rujukan pada SEP BPJS
            // (karakter ke-5 'R' = rumah sakit, kode numerik murni = puskesmas, huruf lain = faskes lainnya),
            // pasien tanpa SEP jatuh ke deteksi teks nama perujuk pada rujukan masuk
            String kodeSep = "(select s.kdppkrujukan from bridging_sep s where s.no_rawat=reg_periksa.no_rawat and ifnull(s.kdppkrujukan,'')<>'' limit 1)";
            String namaPKM = "(upper(ifnull(rujuk_masuk.perujuk,'')) like '%PUSKESMAS%' or upper(ifnull(rujuk_masuk.perujuk,'')) like '%PKM%' or upper(ifnull(rujuk_masuk.perujuk,'')) like '%PUSTU%')";
            String namaRS  = "((upper(ifnull(rujuk_masuk.perujuk,'')) like '%RUMAH SAKIT%' or upper(ifnull(rujuk_masuk.perujuk,'')) like '%RS%') and not "+namaPKM+")";
            String faskesPKM = " and if("+kodeSep+" is not null,"+kodeSep+" regexp '^[0-9]+$',"+namaPKM+")";
            String faskesRS  = " and if("+kodeSep+" is not null,substring("+kodeSep+",5,1)='R',"+namaRS+")";
            String faskesLain= " and if("+kodeSep+" is not null,("+kodeSep+" regexp '[A-Za-z]' and substring("+kodeSep+",5,1)<>'R'),(not "+namaPKM+" and not "+namaRS+"))";
            String dibalas   = " and ifnull(rujuk_masuk.no_balasan,'')<>''";

            String rmBase = "select count(distinct reg_periksa.no_rawat) from reg_periksa "+
                    "inner join rujuk_masuk on rujuk_masuk.no_rawat=reg_periksa.no_rawat "+
                    "where reg_periksa.tgl_registrasi between '"+tgl1+"' and '"+tgl2+"' and reg_periksa.stts<>'Batal' ";

            String dkBase = "select count(distinct rujuk.no_rawat) from rujuk "+
                    "inner join reg_periksa on reg_periksa.no_rawat=rujuk.no_rawat "+
                    "where rujuk.tgl_rujuk between '"+tgl1+"' and '"+tgl2+"' ";
            String dkRujukan = " and rujuk.no_rawat in (select no_rawat from rujuk_masuk)";
            String dkSendiri = " and rujuk.no_rawat not in (select no_rawat from rujuk_masuk)";
            String dkKembali = " and exists(select 1 from reg_periksa r2 where r2.no_rkm_medis=reg_periksa.no_rkm_medis and r2.tgl_registrasi>rujuk.tgl_rujuk)";

            int[] ttl = new int[12];
            for(String[] b : baris){
                List<String> daftarPoli = kelompok.get(b[2]);
                if(daftarPoli==null||daftarPoli.isEmpty()){
                    tabMode.addRow(new Object[]{b[0],b[1],0,0,0,0,0,0,0,0,0,0,0,0});
                    continue;
                }
                String poli = " and reg_periksa.kd_poli in ("+inPoli(daftarPoli)+") "+b[3];

                int[] n = new int[12];
                n[0] = Sequel.cariInteger(rmBase+poli+faskesPKM);
                n[1] = Sequel.cariInteger(rmBase+poli+faskesRS);
                n[2] = Sequel.cariInteger(rmBase+poli+faskesLain);
                n[3] = Sequel.cariInteger(rmBase+poli);
                n[4] = Sequel.cariInteger(rmBase+poli+dibalas+faskesPKM);
                n[5] = Sequel.cariInteger(rmBase+poli+dibalas+faskesRS);
                n[6] = Sequel.cariInteger(rmBase+poli+dibalas+faskesLain);
                n[7] = Sequel.cariInteger(rmBase+poli+dibalas);
                n[8] = Sequel.cariInteger(dkBase+poli+dkRujukan);
                n[9] = Sequel.cariInteger(dkBase+poli+dkSendiri);
                n[10]= Sequel.cariInteger(dkBase+poli);
                n[11]= Sequel.cariInteger(dkBase+poli+dkKembali);

                for(int k=0;k<12;k++){
                    ttl[k]+=n[k];
                }
                tabMode.addRow(new Object[]{b[0],b[1],n[0],n[1],n[2],n[3],n[4],n[5],n[6],n[7],n[8],n[9],n[10],n[11]});
            }
            tabMode.addRow(new Object[]{"99","TOTAL",ttl[0],ttl[1],ttl[2],ttl[3],ttl[4],ttl[5],ttl[6],ttl[7],ttl[8],ttl[9],ttl[10],ttl[11]});

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
