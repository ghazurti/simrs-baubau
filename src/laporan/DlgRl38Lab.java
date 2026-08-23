/*
 * Formulir RL 3.8 Rekapitulasi Kegiatan Pelayanan Laboratorium (Juknis SIRS Revisi 6.3, 2025).
 *
 * Penomoran RL 3.8 tidak berubah dari SIRS 2011, tetapi bentuk formulirnya berubah :
 * pemeriksaan dikelompokkan ke daftar baku (Patologi Klinik, Mikrobiologi, Parasitologi,
 * Patologi Anatomi) dengan kolom Jumlah Pemeriksaan (L/P) dan Nilai Rata-Rata (L/P).
 *
 * Nilai rata-rata dihitung hanya dari hasil yang berupa angka (juknis butir 4).
 * Pemetaan nama tes ke baris formulir lihat KegiatanLabSirs63.
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import laporan.KegiatanLabSirs63.Baris;

public final class DlgRl38Lab extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private int i=0;

    /** akumulator angka per baris : jumlah & (sumNumerik, cntNumerik) per jenis kelamin */
    private static class Akum {
        int jmlL, jmlP;
        double sumL, sumP;
        int cntL, cntP;
    }

    public DlgRl38Lab(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        tabMode=new DefaultTableModel(null,new Object[]{
            "No.","Pemeriksaan","Jml L","Jml P","Rata-Rata L","Rata-Rata P"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbKeg.setModel(tabMode);
        tbKeg.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbKeg.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 6; i++) {
            TableColumn column = tbKeg.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(45);
            }else if(i==1){
                column.setPreferredWidth(340);
            }else{
                column.setPreferredWidth(90);
            }
        }
        tbKeg.setDefaultRenderer(Object.class, new WarnaTable());
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbKeg = new widget.Table();
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ RL 3.8 Rekapitulasi Kegiatan Pelayanan Laboratorium ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50,50,50)));
        internalFrame1.setName("internalFrame1");
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll");
        Scroll.setOpaque(true);
        tbKeg.setName("tbKeg");
        Scroll.setViewportView(tbKeg);
        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass5.setName("panelGlass5");
        panelGlass5.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tanggal :");
        label11.setName("label11");
        label11.setPreferredSize(new java.awt.Dimension(55, 23));
        panelGlass5.add(label11);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1");
        Tgl1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl1);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18");
        label18.setPreferredSize(new java.awt.Dimension(25, 23));
        panelGlass5.add(label18);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2");
        Tgl2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl2);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setMnemonic('2');
        BtnCari.setToolTipText("Alt+2");
        BtnCari.setName("BtnCari");
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(evt -> runBackground(() ->tampil()));
        panelGlass5.add(BtnCari);

        jLabel7.setName("jLabel7");
        jLabel7.setPreferredSize(new java.awt.Dimension(30, 23));
        panelGlass5.add(jLabel7);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint");
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(evt -> BtnPrintActionPerformed(evt));
        panelGlass5.add(BtnPrint);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar");
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(evt -> dispose());
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if(evt.getKeyCode()==KeyEvent.VK_ESCAPE){
                    dispose();
                }
            }
        });
        panelGlass5.add(BtnKeluar);

        internalFrame1.add(panelGlass5, java.awt.BorderLayout.PAGE_END);
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        pack();
    }

    public void tampil() {
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(tabMode);
            koneksi=koneksiDB.condb();

            Map<String,Akum> akum=new LinkedHashMap<>();
            // agregasi per (nama tes, jenis kelamin) : jumlah item, serta jumlah & cacah
            // nilai yang berupa angka untuk menghitung rata-rata (juknis butir 4).
            ps=koneksi.prepareStatement(
                "select t.Pemeriksaan as nm, pasien.jk as jk, count(*) as n, "+
                "sum(case when d.nilai regexp '^-?[0-9]+([.][0-9]+)?$' then cast(d.nilai as decimal(20,4)) else 0 end) as sumnum, "+
                "sum(d.nilai regexp '^-?[0-9]+([.][0-9]+)?$') as cntnum "+
                "from detail_periksa_lab d "+
                "inner join template_laboratorium t on t.kd_jenis_prw=d.kd_jenis_prw and t.id_template=d.id_template "+
                "inner join reg_periksa on reg_periksa.no_rawat=d.no_rawat "+
                "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                "where d.tgl_periksa between ? and ? "+
                "group by t.Pemeriksaan, pasien.jk");
            try {
                ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                rs=ps.executeQuery();
                while(rs.next()){
                    String no=KegiatanLabSirs63.kategori(rs.getString("nm"));
                    if(no==null){
                        continue;
                    }
                    Akum a=akum.get(no);
                    if(a==null){
                        a=new Akum();
                        akum.put(no,a);
                    }
                    boolean lk="L".equals(rs.getString("jk"));
                    int n=rs.getInt("n");
                    double sumnum=rs.getDouble("sumnum");
                    int cntnum=rs.getInt("cntnum");
                    if(lk){
                        a.jmlL+=n; a.sumL+=sumnum; a.cntL+=cntnum;
                    }else{
                        a.jmlP+=n; a.sumP+=sumnum; a.cntP+=cntnum;
                    }
                }
            } finally {
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }

            // baris kelompok = penjumlahan jumlah sub-baris (rata-rata tidak dijumlahkan)
            for(Baris b : KegiatanLabSirs63.BARIS){
                if(b.jumlahDari==null){
                    continue;
                }
                Akum g=new Akum();
                for(Map.Entry<String,Akum> e : akum.entrySet()){
                    if(e.getKey().startsWith(b.jumlahDari) && e.getKey().indexOf('.')>0){
                        g.jmlL+=e.getValue().jmlL;
                        g.jmlP+=e.getValue().jmlP;
                    }
                }
                akum.put(b.no,g);
            }

            for(Baris b : KegiatanLabSirs63.BARIS){
                if(b.judul){
                    tabMode.addRow(new Object[]{b.no,b.nama,"","","",""});
                    continue;
                }
                Akum a=akum.get(b.no);
                if(a==null){
                    tabMode.addRow(new Object[]{b.no,b.nama,0,0,"",""});
                    continue;
                }
                tabMode.addRow(new Object[]{
                    b.no,b.nama,a.jmlL,a.jmlP,
                    (b.jumlahDari==null?rata(a.sumL,a.cntL):""),
                    (b.jumlahDari==null?rata(a.sumP,a.cntP):"")
                });
            }
            this.setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            System.out.println("laporan.DlgRl38Lab.tampil() : "+e);
        }
    }

    private String rata(double sum, int cnt){
        if(cnt<=0){
            return "";
        }
        return new java.text.DecimalFormat("#,##0.##").format(sum/cnt);
    }

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, belum ada data yang bisa dicetak...!!!!");
            return;
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
            StringBuilder v=new StringBuilder("'"+r+"'");
            for(int c=0;c<6;c++){
                v.append(",'").append(tabMode.getValueAt(r,c).toString().replaceAll("'","`")).append("'");
            }
            for(int c=6;c<36;c++){
                v.append(",''");
            }
            v.append(",'").append(akses.getalamatip()).append("'");
            Sequel.menyimpan("temporary",v.toString(),"");
        }
        Valid.MyReportqry("rptRl38Lab.jasper","report","::[ Formulir RL 3.8 Kegiatan Pelayanan Laboratorium ]::",
            "select * from temporary where temporary.temp37='"+akses.getalamatip()+"' order by temporary.no",param);
        this.setCursor(Cursor.getDefaultCursor());
    }

    public void isCek(){
        BtnPrint.setEnabled(akses.getrl38());
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
    private widget.Table tbKeg;
}
