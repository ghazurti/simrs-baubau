/*
 * Formulir RL 3.6 Rekapitulasi Kegiatan Pelayanan Kebidanan (Juknis SIRS Revisi 6.3, 2025).
 *
 * Catatan penomoran : pada SIRS 2011 nomor RL 3.6 dipakai untuk Kegiatan Pembedahan
 * (lihat DlgRl36). Pada SIRS 6.3 penomoran bergeser, RL 3.6 menjadi Kegiatan Pelayanan
 * Kebidanan sedangkan pembedahan pindah ke RL 3.12. Karena itu form ini dibuat terpisah
 * dan form pembedahan yang lama tetap dipertahankan.
 *
 * Dilaporkan bulanan. Jenis kegiatan diturunkan dari kode ICD-10 diagnosa pasien
 * (lihat KegiatanKebidananSirs63). Beberapa baris tidak punya sumber data di Khanza
 * sehingga selalu 0 dan wajib dilengkapi manual saat entri di SIRS Online versi 3.
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import laporan.KegiatanKebidananSirs63.Baris;

public final class DlgRl36Kebidanan extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private int i=0;

    /** ringkasan satu pasien kebidanan pada periode laporan */
    private static class Pasien {
        final Set<String> dx=new HashSet<>();
        int asal=KegiatanKebidananSirs63.NON_RUJUKAN;
        boolean mati=false;
        boolean dirujuk=false;
    }

    public DlgRl36Kebidanan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        Object[] kolom={"No.","Jenis Kegiatan","Dirujuk",
            "RM:Rumah Sakit","RM:Bidan","RM:Puskesmas","RM:Faskes Lainnya",
            "RM:Jml Hidup","RM:Jml Mati","Total Rujukan Medis",
            "RNM:Jml Hidup","RNM:Jml Mati","Total Rujukan Non Medis",
            "NR:Jml Hidup","NR:Jml Mati","Total Non Rujukan"};
        tabMode=new DefaultTableModel(null,kolom){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbKeg.setModel(tabMode);
        tbKeg.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbKeg.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < kolom.length; i++) {
            TableColumn column = tbKeg.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(35);
            }else if(i==1){
                column.setPreferredWidth(300);
            }else{
                column.setPreferredWidth(110);
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ RL 3.6 Rekapitulasi Kegiatan Pelayanan Kebidanan ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50,50,50)));
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

            // Kumpulkan pasien kebidanan pada periode beserta seluruh diagnosanya.
            // Hanya kode yang relevan kebidanan yang ditarik supaya ringan.
            ps=koneksi.prepareStatement(
                "select reg_periksa.no_rawat as no_rawat, diagnosa_pasien.kd_penyakit as kd, "+
                KegiatanKebidananSirs63.caseAsal()+" as asal, "+
                "exists(select 1 from kamar_inap ki where ki.no_rawat=reg_periksa.no_rawat and ki.stts_pulang='Meninggal') as mati, "+
                "exists(select 1 from rujuk rj where rj.no_rawat=reg_periksa.no_rawat) as dirujuk "+
                "from reg_periksa "+
                "inner join diagnosa_pasien on diagnosa_pasien.no_rawat=reg_periksa.no_rawat "+
                "left join rujuk_masuk on rujuk_masuk.no_rawat=reg_periksa.no_rawat "+
                "where reg_periksa.tgl_registrasi between ? and ? and reg_periksa.stts<>'Batal' "+
                "and (left(diagnosa_pasien.kd_penyakit,1)='O' "+
                "     or diagnosa_pasien.kd_penyakit like 'Z34%' or diagnosa_pasien.kd_penyakit like 'Z35%' "+
                "     or diagnosa_pasien.kd_penyakit like 'Z36%' or diagnosa_pasien.kd_penyakit like 'Z39%' "+
                "     or diagnosa_pasien.kd_penyakit like 'U07.1%')");

            Map<String,Pasien> pasien=new LinkedHashMap<>();
            try {
                ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                rs=ps.executeQuery();
                while(rs.next()){
                    String nr=rs.getString("no_rawat");
                    Pasien p=pasien.get(nr);
                    if(p==null){
                        p=new Pasien();
                        p.asal=rs.getInt("asal");
                        p.mati=rs.getInt("mati")==1;
                        p.dirujuk=rs.getInt("dirujuk")==1;
                        pasien.put(nr,p);
                    }
                    p.dx.add(rs.getString("kd"));
                }
            } finally {
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }

            // hitung per baris kegiatan : no -> 14 kolom angka
            Map<String,int[]> hasil=new LinkedHashMap<>();
            for(Baris b : KegiatanKebidananSirs63.BARIS){
                int[] n=new int[14];
                if(b.kode!=null){
                    for(Pasien p : pasien.values()){
                        if(!KegiatanKebidananSirs63.cocokBaris(p.dx,b)){
                            continue;
                        }
                        isiKolom(n,p);
                    }
                }
                hasil.put(b.no,n);
            }
            // baris kelompok = penjumlahan sub-barisnya
            for(Baris b : KegiatanKebidananSirs63.BARIS){
                if(b.jumlahDari==null){
                    continue;
                }
                int[] n=hasil.get(b.no);
                for(Map.Entry<String,int[]> e : hasil.entrySet()){
                    if(e.getKey().startsWith(b.jumlahDari) && !e.getKey().equals(b.no)){
                        for(int c=0;c<14;c++){
                            n[c]+=e.getValue()[c];
                        }
                    }
                }
            }

            for(Baris b : KegiatanKebidananSirs63.BARIS){
                int[] n=hasil.get(b.no);
                Object[] row=new Object[16];
                row[0]=b.no;
                row[1]=b.nama+(b.tanpaSumberData()?"   (*)":"");
                for(int c=0;c<14;c++){
                    row[c+2]=n[c];
                }
                tabMode.addRow(row);
            }
            this.setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            System.out.println("laporan.DlgRl36Kebidanan.tampil() : "+e);
        }
    }

    /**
     * Menambahkan satu pasien ke kolom yang sesuai.
     * Urutan kolom : 0=Dirujuk, 1..4=RM RS/Bidan/Puskesmas/FaskesLain, 5=RM Hidup, 6=RM Mati,
     * 7=Total RM, 8=RNM Hidup, 9=RNM Mati, 10=Total RNM, 11=NR Hidup, 12=NR Mati, 13=Total NR
     */
    private void isiKolom(int[] n, Pasien p){
        if(p.dirujuk){
            n[0]++;
        }
        switch(p.asal){
            case KegiatanKebidananSirs63.RM_RS:
            case KegiatanKebidananSirs63.RM_BIDAN:
            case KegiatanKebidananSirs63.RM_PUSKESMAS:
            case KegiatanKebidananSirs63.RM_FASKES_LAIN:
                n[1+p.asal]++;              // asal faskes perujuk
                if(p.mati){ n[6]++; }else{ n[5]++; }
                n[7]++;
                break;
            case KegiatanKebidananSirs63.RUJUKAN_NON_MEDIS:
                if(p.mati){ n[9]++; }else{ n[8]++; }
                n[10]++;
                break;
            default:                        // NON_RUJUKAN
                if(p.mati){ n[12]++; }else{ n[11]++; }
                n[13]++;
                break;
        }
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
            for(int c=0;c<16;c++){
                v.append(",'").append(tabMode.getValueAt(r,c).toString().replaceAll("'","`")).append("'");
            }
            // sisa kolom temporary dikosongkan sampai temp36, temp37 dipakai penanda IP
            for(int c=16;c<36;c++){
                v.append(",''");
            }
            v.append(",'").append(akses.getalamatip()).append("'");
            Sequel.menyimpan("temporary",v.toString(),"");
        }
        Valid.MyReportqry("rptRl36Kebidanan.jasper","report","::[ Formulir RL 3.6 Kegiatan Pelayanan Kebidanan ]::",
            "select * from temporary where temporary.temp37='"+akses.getalamatip()+"' order by temporary.no",param);
        this.setCursor(Cursor.getDefaultCursor());
    }

    public void isCek(){
        BtnPrint.setEnabled(akses.getrl36());
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
