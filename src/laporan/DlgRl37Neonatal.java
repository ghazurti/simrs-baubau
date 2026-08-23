/*
 * Formulir RL 3.7 Rekapitulasi Kegiatan Pelayanan Neonatal, Bayi, dan Balita
 * (Juknis SIRS Revisi 6.3, 2025).
 *
 * Catatan penomoran : pada SIRS 2011 nomor RL 3.7 dipakai untuk Kegiatan Radiologi
 * (lihat DlgRl37). Pada SIRS 6.3 RL 3.7 menjadi Kegiatan Pelayanan Neonatal, Bayi,
 * dan Balita, sehingga form ini dibuat terpisah.
 *
 * Bayi lahir hidup dikelompokkan menurut berat lahir dari pasien_bayi (menu Data
 * Kelahiran Bayi). Karena usia gestasi tidak dicatat, digunakan heuristik : berat
 * <2500 gram dimasukkan ke kelompok Prematur, >=2500 gram ke Non Prematur (lihat
 * KegiatanNeonatalSirs63). Baris imunisasi/skrining/HIV/Sifilis/Hepatitis tidak ada
 * sumber datanya di Khanza -> tampil 0 dan ditandai (*), diisi manual di SIRS Online v3.
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
import laporan.KegiatanNeonatalSirs63.Baris;

public final class DlgRl37Neonatal extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private int i=0;

    public DlgRl37Neonatal(java.awt.Frame parent, boolean modal) {
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
                column.setPreferredWidth(40);
            }else if(i==1){
                column.setPreferredWidth(330);
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ RL 3.7 Rekapitulasi Kegiatan Pelayanan Neonatal, Bayi, dan Balita ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50,50,50)));
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

            // Bayi lahir hidup per pita berat lahir, dari pasien_bayi (menu Data Kelahiran Bayi).
            // Bayi lahir di RS ini dianggap Non Rujukan - Hidup.
            Map<String,Integer> perBand=new LinkedHashMap<>();
            ps=koneksi.prepareStatement(
                "select case when pb.berat_badan+0<1000 then 'BBLER' when pb.berat_badan+0<1500 then 'BBLSR' "+
                "when pb.berat_badan+0<2500 then 'BBLR' when pb.berat_badan+0<4000 then 'BBLN' else 'BBLL' end band, "+
                "count(*) n from pasien_bayi pb "+
                "inner join reg_periksa rp on rp.no_rkm_medis=pb.no_rkm_medis "+
                "where rp.tgl_registrasi between ? and ? and pb.berat_badan is not null "+
                "and pb.berat_badan<>'' and pb.berat_badan+0>0 group by band");
            try {
                ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                rs=ps.executeQuery();
                while(rs.next()){
                    perBand.put(rs.getString("band"),rs.getInt("n"));
                }
            } finally {
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }

            // No. 6 Bayi mendapatkan Skrining : bayi lahir hidup kategori 1.2 + 1.3
            // (berat >=2500 gram) yang proses lahirnya NORMAL/spontan.
            int normal1213=0;
            ps=koneksi.prepareStatement(
                "select count(*) n from pasien_bayi pb "+
                "inner join reg_periksa rp on rp.no_rkm_medis=pb.no_rkm_medis "+
                "where rp.tgl_registrasi between ? and ? and pb.berat_badan is not null "+
                "and pb.berat_badan<>'' and pb.berat_badan+0>=2500 "+
                "and (lower(pb.proses_lahir) like '%normal%' or lower(pb.proses_lahir) like '%spontan%') "+
                "and lower(pb.proses_lahir) not like '%tidak normal%' "+
                "and lower(pb.proses_lahir) not like '%abnormal%'");
            try {
                ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                rs=ps.executeQuery();
                if(rs.next()){
                    normal1213=rs.getInt("n");
                }
            } finally {
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }

            // isi angka per baris : indeks kolom 0..13 (lihat header)
            Map<String,int[]> hasil=new LinkedHashMap<>();
            for(Baris b : KegiatanNeonatalSirs63.BARIS){
                int[] n=new int[14];
                if(b.beratBand!=null){
                    Integer jml=perBand.get(b.beratBand);
                    if(jml!=null){
                        n[11]+=jml;   // Non Rujukan - Jumlah Hidup
                        n[13]+=jml;   // Total Non Rujukan
                    }
                }
                if(KegiatanNeonatalSirs63.SUMBER_NORMAL_12_13.equals(b.sumber)){
                    n[11]+=normal1213;
                    n[13]+=normal1213;
                }
                if(b.icd!=null && b.icd.length>0){
                    int[] hm=hitungKomplikasi(b.icd);
                    n[11]+=hm[0];          // Non Rujukan - Jumlah Hidup
                    n[12]+=hm[1];          // Non Rujukan - Jumlah Mati
                    n[13]+=hm[0]+hm[1];    // Total Non Rujukan
                }
                hasil.put(b.no,n);
            }
            Baris[] br=KegiatanNeonatalSirs63.BARIS;
            // Lintasan-1 : jumlahkan baris kelompok dari anak langsungnya (mis. No.1 = 1.1+1.2+1.3)
            // supaya No.1 sudah terisi sebelum baris salin (No.7/15.2/16.6 <- No.1) dijalankan.
            jumlahkanKelompok(br,hasil);
            // Baris yang menyalin angka baris lain (mis. No.7 <- No.1).
            for(Baris b : br){
                if(b.salinDari==null){
                    continue;
                }
                int[] src=hasil.get(b.salinDari);
                if(src!=null){
                    int[] n=hasil.get(b.no);
                    for(int c=0;c<14;c++){
                        n[c]=src[c];
                    }
                }
            }
            // Lintasan-2 : hitung ulang kelompok supaya induk (mis. No.15/No.16) ikut memuat
            // anak yang baru diisi lewat salin (15.2, 16.6). Baris kelompok dinolkan dulu.
            for(Baris b : br){
                if(b.jumlahDari!=null){
                    int[] n=hasil.get(b.no);
                    for(int c=0;c<14;c++){
                        n[c]=0;
                    }
                }
            }
            jumlahkanKelompok(br,hasil);

            for(Baris b : KegiatanNeonatalSirs63.BARIS){
                int[] n=hasil.get(b.no);
                Object[] row=new Object[16];
                row[0]=b.no;
                row[1]=b.nama+(b.tanpaSumberData()?"   (*)":"");
                if(b.judul && b.jumlahDari==null){
                    for(int c=0;c<14;c++){
                        row[c+2]="";
                    }
                }else{
                    for(int c=0;c<14;c++){
                        row[c+2]=n[c];
                    }
                }
                tabMode.addRow(row);
            }
            this.setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            System.out.println("laporan.DlgRl37Neonatal.tampil() : "+e);
        }
    }

    /** Hitung jumlah pasien NEONATAL (usia hari, atau 0 bulan) dalam periode yang punya
     * diagnosa dengan salah satu prefix ICD-10 tsb, dipisah HIDUP vs MATI. Bayi dianggap
     * mati bila reg_periksa.stts='Meninggal' atau salah satu kamar_inap.stts_pulang='Meninggal'.
     * Hasil : int[]{hidup, mati}. Dipakai baris Komplikasi Neonatal (4.x). */
    private int[] hitungKomplikasi(String[] icd){
        StringBuilder cond=new StringBuilder();
        for(int k=0;k<icd.length;k++){
            cond.append(k==0?"":" or ").append("dp.kd_penyakit like ?");
        }
        int[] hm={0,0};
        try (PreparedStatement psk=koneksi.prepareStatement(
                "select sum(case when mati=1 then 0 else 1 end) hidup, sum(case when mati=1 then 1 else 0 end) mati from ("+
                "select rp.no_rawat, max(case when rp.stts='Meninggal' "+
                "or exists(select 1 from kamar_inap ki where ki.no_rawat=rp.no_rawat and ki.stts_pulang='Meninggal') "+
                "then 1 else 0 end) mati "+
                "from reg_periksa rp inner join diagnosa_pasien dp on dp.no_rawat=rp.no_rawat "+
                "where rp.tgl_registrasi between ? and ? "+
                "and (rp.sttsumur='Hr' or (rp.sttsumur='Bl' and rp.umurdaftar=0)) "+
                "and ("+cond+") group by rp.no_rawat) t")) {
            psk.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
            psk.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
            for(int k=0;k<icd.length;k++){
                psk.setString(3+k,icd[k]+"%");
            }
            try (ResultSet rsk=psk.executeQuery()) {
                if(rsk.next()){
                    hm[0]=rsk.getInt("hidup");
                    hm[1]=rsk.getInt("mati");
                }
            }
        } catch (Exception e) {
            System.out.println("laporan.DlgRl37Neonatal.hitungKomplikasi() : "+e);
        }
        return hm;
    }

    /** Jumlahkan tiap baris kelompok dari anak LANGSUNG-nya, diproses dari DALAM ke LUAR
     * supaya sub-kelompok (mis. 1.1/1.2/1.3) terisi lebih dulu sebelum induknya (No.1). */
    private void jumlahkanKelompok(Baris[] br, Map<String,int[]> hasil){
        for(int idx=br.length-1; idx>=0; idx--){
            Baris b=br[idx];
            if(b.jumlahDari==null){
                continue;
            }
            int[] n=hasil.get(b.no);
            for(Map.Entry<String,int[]> e : hasil.entrySet()){
                String key=e.getKey();
                if(key.equals(b.no)){
                    continue;
                }
                // hanya anak LANGSUNG (satu tingkat lebih dalam)
                if(key.startsWith(b.jumlahDari) && key.indexOf('.',b.jumlahDari.length())<0){
                    for(int c=0;c<14;c++){
                        n[c]+=e.getValue()[c];
                    }
                }
            }
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
            for(int c=16;c<36;c++){
                v.append(",''");
            }
            v.append(",'").append(akses.getalamatip()).append("'");
            Sequel.menyimpan("temporary",v.toString(),"");
        }
        Valid.MyReportqry("rptRl37Neonatal.jasper","report","::[ Formulir RL 3.7 Neonatal, Bayi, dan Balita ]::",
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
