/*
 * Cetak Surat Rujuk Balik (SRB / PRB BPJS) per pasien.
 * Cari pasien BPJS ber-SEP dalam periode, pilih, lalu cetak surat rujuk balik
 * (laporan report/rptSuratRujukBalik.jasper) dengan identitas + diagnosa + obat
 * terisi otomatis dari data kunjungan. Tanggal kontrol & centang penyakit penyerta
 * diisi tangan pada surat.
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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public final class DlgSuratRujukBalik extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final validasi Valid=new validasi();
    private PreparedStatement ps,ps2;
    private ResultSet rs,rs2;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private int i=0;

    public DlgSuratRujukBalik(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(980,620);

        Object[] kolom={"No.Rawat","Tgl SEP","No.SEP","No.Kartu","No.RM","Nama Pasien","Diagnosa","DPJP"};
        tabMode=new DefaultTableModel(null,kolom){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbData.setModel(tabMode);
        tbData.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar={95,70,150,110,60,180,230,140};
        for (i = 0; i < kolom.length; i++) {
            TableColumn column = tbData.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebar[i]);
        }
        tbData.setDefaultRenderer(Object.class, new WarnaTable());
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbData = new widget.Table();
        panelGlass5 = new widget.panelisi();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        labelCari = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Cetak Surat Rujuk Balik (PRB BPJS) ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50,50,50)));
        internalFrame1.setName("internalFrame1");
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll");
        Scroll.setOpaque(true);
        tbData.setName("tbData");
        tbData.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if(evt.getClickCount()==2){ cetak(); }
            }
        });
        Scroll.setViewportView(tbData);
        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass5.setName("panelGlass5");
        panelGlass5.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tgl. SEP :");
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

        labelCari.setText("Cari :");
        labelCari.setName("labelCari");
        labelCari.setPreferredSize(new java.awt.Dimension(35, 23));
        panelGlass5.add(labelCari);

        TCari.setName("TCari");
        TCari.setPreferredSize(new java.awt.Dimension(200, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if(evt.getKeyCode()==KeyEvent.VK_ENTER){ runBackground(() ->tampil()); }
            }
        });
        panelGlass5.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setMnemonic('2');
        BtnCari.setText("Cari");
        BtnCari.setToolTipText("Alt+2");
        BtnCari.setName("BtnCari");
        BtnCari.setPreferredSize(new java.awt.Dimension(85, 23));
        BtnCari.addActionListener(evt -> runBackground(() ->tampil()));
        panelGlass5.add(BtnCari);

        jLabel7.setName("jLabel7");
        jLabel7.setPreferredSize(new java.awt.Dimension(20, 23));
        panelGlass5.add(jLabel7);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak SRB");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint");
        BtnPrint.setPreferredSize(new java.awt.Dimension(110, 30));
        BtnPrint.addActionListener(evt -> cetak());
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
                if(evt.getKeyCode()==KeyEvent.VK_ESCAPE){ dispose(); }
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
            String cari=TCari.getText().trim();
            String sql="select sep.no_rawat,sep.tglsep,sep.no_sep,sep.no_kartu,sep.nomr,sep.nama_pasien,"+
                "trim(concat(ifnull(sep.diagawal,''),' ',ifnull(sep.nmdiagnosaawal,''))) diagnosa,dr.nm_dokter "+
                "from bridging_sep sep inner join reg_periksa rp on rp.no_rawat=sep.no_rawat "+
                "left join dokter dr on dr.kd_dokter=rp.kd_dokter "+
                "where sep.tglsep between ? and ? "+
                (cari.equals("")?"":"and (sep.nama_pasien like ? or sep.no_sep like ? or sep.no_kartu like ? or sep.no_rawat like ? or sep.nomr like ?) ")+
                "order by sep.tglsep desc limit 500";
            ps=koneksi.prepareStatement(sql);
            try {
                ps.setString(1,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                if(!cari.equals("")){
                    for(int p=3;p<=7;p++){ ps.setString(p,"%"+cari+"%"); }
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),rs.getString("tglsep"),rs.getString("no_sep"),
                        rs.getString("no_kartu"),rs.getString("nomr"),rs.getString("nama_pasien"),
                        rs.getString("diagnosa"),rs.getString("nm_dokter")});
                }
            } finally {
                if(rs!=null){ rs.close(); }
                if(ps!=null){ ps.close(); }
            }
            this.setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            System.out.println("laporan.DlgSuratRujukBalik.tampil() : "+e);
        }
    }

    private void cetak() {
        if(tbData.getSelectedRow()==-1){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih pasien yang mau dicetak Surat Rujuk Balik-nya...!!!");
            return;
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            String norawat=tabMode.getValueAt(tbData.getSelectedRow(),0).toString();
            String nosep=tabMode.getValueAt(tbData.getSelectedRow(),2).toString();

            // rakit obat dari resep pasien
            StringBuilder nm=new StringBuilder(),jm=new StringBuilder(),ds=new StringBuilder();
            koneksi=koneksiDB.condb();
            ps2=koneksi.prepareStatement(
                "select db.nama_brng,rd.jml,rd.aturan_pakai from resep_obat ro "+
                "inner join resep_dokter rd on rd.no_resep=ro.no_resep "+
                "inner join databarang db on db.kode_brng=rd.kode_brng where ro.no_rawat=?");
            try {
                ps2.setString(1,norawat);
                rs2=ps2.executeQuery();
                while(rs2.next()){
                    nm.append(rs2.getString(1)==null?"":rs2.getString(1).trim()).append("\n");
                    jm.append(Valid.SetAngka2(rs2.getDouble(2))).append("\n");
                    ds.append(rs2.getString(3)==null?"":rs2.getString(3).trim()).append("\n");
                }
            } finally {
                if(rs2!=null){ rs2.close(); }
                if(ps2!=null){ ps2.close(); }
            }

            Map<String, Object> param = new HashMap<>();
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
            param.put("nosurat","");
            param.put("obatNama",nm.toString());
            param.put("obatJml",jm.toString());
            param.put("obatDosis",ds.toString());
            Valid.MyReportqry("rptSuratRujukBalik.jasper","report","::[ Surat Rujuk Balik ]::",
                "select sep.no_rawat,sep.no_sep,sep.no_kartu,sep.nama_pasien,sep.diagawal,sep.nmdiagnosaawal,"+
                "ps.no_ktp,ps.alamat,ps.no_tlp,kel.nm_kel,kec.nm_kec,kab.nm_kab,dr.nm_dokter,rp.tgl_registrasi "+
                "from bridging_sep sep "+
                "inner join reg_periksa rp on rp.no_rawat=sep.no_rawat "+
                "inner join pasien ps on ps.no_rkm_medis=sep.nomr "+
                "left join kelurahan kel on kel.kd_kel=ps.kd_kel "+
                "left join kecamatan kec on kec.kd_kec=ps.kd_kec "+
                "left join kabupaten kab on kab.kd_kab=ps.kd_kab "+
                "left join dokter dr on dr.kd_dokter=rp.kd_dokter "+
                "where sep.no_sep='"+nosep+"'",param);
        } catch (Exception e) {
            System.out.println("laporan.DlgSuratRujukBalik.cetak() : "+e);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

    public void isCek(){
        BtnPrint.setEnabled(akses.getbpjs_program_prb());
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
    private widget.TextBox TCari;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel7;
    private widget.Label label11;
    private widget.Label label18;
    private widget.Label labelCari;
    private widget.panelisi panelGlass5;
    private widget.Table tbData;
}
