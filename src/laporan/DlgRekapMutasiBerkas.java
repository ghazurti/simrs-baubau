/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgLhtBiaya.java
 *
 * Created on 12 Jul 10, 16:21:34
 */

package laporan;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
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
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author perpustakaan
 */
public final class DlgRekapMutasiBerkas extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private int i=0;
    /** Creates new form DlgLhtBiaya
     * @param parent
     * @param modal */
    public DlgRekapMutasiBerkas(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat","No.RM","Nama Pasien","Status RM Terakhir","Jam Registrasi","Jam Dikirim ke Poli","Jam Diterima Poli","Jam Kembali RM","Jam RM Hilang","Alasan RM Tidak Ada","RM Ke Ranap","Status Pasien"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbBangsal.setModel(tabMode);
        //tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal.getBackground()));
        tbBangsal.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 12; i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(160);
            }else if(i==3){
                column.setPreferredWidth(100);
            }else if(i==4){
                column.setPreferredWidth(120);
            }else if(i==5){
                column.setPreferredWidth(120);
            }else if(i==6){
                column.setPreferredWidth(120);
            }else if(i==7){
                column.setPreferredWidth(120);
            }else if(i==8){
                column.setPreferredWidth(120);
            }else if(i==9){
                column.setPreferredWidth(220);
            }else if(i==10){
                column.setPreferredWidth(120);
            }else if(i==11){
                column.setPreferredWidth(75);
            }
        }
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable());

        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        siapkanTabelAlasan();
        tambahFilter();
    }

    private widget.ComboBox cmbStatus,cmbAlasan,cmbRawat;
    private static final String[] ALASAN_BAKU={
        "Berkas dipinjam unit lain","Berkas masih di ruang rawat inap","Berkas belum kembali dari poli",
        "Berkas tidak ditemukan di rak","Pasien baru, berkas belum dibuat"
    };

    /** baris filter di atas tombol: Status Berkas, Alasan (Tidak Ada), Jenis Rawat; berlaku untuk tabel & cetak */
    private void tambahFilter(){
        widget.panelisi panelFilter=new widget.panelisi();
        panelFilter.setPreferredSize(new Dimension(55,40));
        panelFilter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,5,7));
        cmbStatus=comboFilter(panelFilter,"Status Berkas :",95,new String[]{"Semua","Sudah Dikirim","Sudah Diterima","Sudah Kembali","Tidak Ada","Masuk Ranap"},120);
        String[] alasan=new String[ALASAN_BAKU.length+3];
        alasan[0]="Semua";
        System.arraycopy(ALASAN_BAKU,0,alasan,1,ALASAN_BAKU.length);
        alasan[ALASAN_BAKU.length+1]="Lainnya";
        alasan[ALASAN_BAKU.length+2]="Tanpa Alasan";
        cmbAlasan=comboFilter(panelFilter,"Alasan :",55,alasan,230);
        cmbRawat=comboFilter(panelFilter,"Jenis Rawat :",80,new String[]{"Semua","Ralan","Ranap"},100);
        // memilih alasan berarti melihat berkas Tidak Ada; ganti filter langsung memperbarui tabel (cetak = isi tabel)
        cmbAlasan.addActionListener(e->{
            if(cmbAlasan.getSelectedIndex()>0&&!"Tidak Ada".equals(cmbStatus.getSelectedItem())) cmbStatus.setSelectedItem("Tidak Ada");
            else tampil();
        });
        cmbStatus.addActionListener(e->tampil());
        cmbRawat.addActionListener(e->tampil());
        java.awt.Container induk=panelGlass5.getParent();
        induk.remove(panelGlass5);
        javax.swing.JPanel bawah=new javax.swing.JPanel(new java.awt.BorderLayout());
        bawah.setOpaque(false);
        bawah.add(panelFilter,java.awt.BorderLayout.NORTH);
        bawah.add(panelGlass5,java.awt.BorderLayout.CENTER);
        induk.add(bawah,java.awt.BorderLayout.PAGE_END);
    }

    private widget.ComboBox comboFilter(java.awt.Container panel,String judul,int lebarJudul,String[] isi,int lebar){
        widget.Label l=new widget.Label();
        l.setText(judul);
        l.setPreferredSize(new Dimension(lebarJudul,23));
        panel.add(l);
        widget.ComboBox c=new widget.ComboBox();
        c.setModel(new javax.swing.DefaultComboBoxModel<>(isi));
        c.setPreferredSize(new Dimension(lebar,23));
        panel.add(c);
        return c;
    }

    private String teks(String s){ return s.replace("\\","").replace("'","''"); }

    /** query rekap (tabel & cetak sama persis) */
    private String sqlRekap(){
        String cari="%"+teks(TCari.getText().trim())+"%";
        StringBuilder w=new StringBuilder();
        w.append("reg_periksa.tgl_registrasi between '").append(Valid.SetTgl(Tgl1.getSelectedItem()+"")).append("' and '").append(Valid.SetTgl(Tgl2.getSelectedItem()+"")).append("' ");
        w.append("and (mutasi_berkas.no_rawat like '").append(cari).append("' or reg_periksa.no_rkm_medis like '").append(cari)
         .append("' or pasien.nm_pasien like '").append(cari).append("' or mutasi_berkas.status like '").append(cari)
         .append("' or reg_periksa.status_lanjut like '").append(cari).append("') ");
        if(cmbStatus!=null&&cmbStatus.getSelectedIndex()>0){
            w.append("and mutasi_berkas.status='").append(teks(cmbStatus.getSelectedItem().toString())).append("' ");
        }
        if(cmbRawat!=null&&cmbRawat.getSelectedIndex()>0){
            w.append("and reg_periksa.status_lanjut='").append(teks(cmbRawat.getSelectedItem().toString())).append("' ");
        }
        if(cmbAlasan!=null&&cmbAlasan.getSelectedIndex()>0){
            String a=cmbAlasan.getSelectedItem().toString();
            String kolom="ifnull((select mutasi_berkas_tidakada.alasan from mutasi_berkas_tidakada where mutasi_berkas_tidakada.no_rawat=mutasi_berkas.no_rawat),'')";
            if(a.equals("Tanpa Alasan")){
                w.append("and ").append(kolom).append("='' ");
            }else if(a.equals("Lainnya")){
                w.append("and ").append(kolom).append("<>'' ");
                for(String b:ALASAN_BAKU) w.append("and ").append(kolom).append(" not like '").append(teks(b)).append("%' ");
            }else{
                w.append("and ").append(kolom).append(" like '").append(teks(a)).append("%' ");
            }
        }
        return "select mutasi_berkas.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,mutasi_berkas.status,reg_periksa.tgl_registrasi,reg_periksa.jam_reg, "+
            "mutasi_berkas.dikirim,mutasi_berkas.diterima,mutasi_berkas.kembali,mutasi_berkas.tidakada,mutasi_berkas.ranap,reg_periksa.status_lanjut, "+ALASAN+
            "from mutasi_berkas inner join reg_periksa inner join pasien on mutasi_berkas.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
            "where "+w+"order by reg_periksa.tgl_registrasi desc ";
    }

    /** keterangan filter yang dicetak di bawah judul laporan */
    private String keteranganFilter(){
        String k="Periode "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem();
        if(cmbStatus.getSelectedIndex()>0) k+="  |  Status : "+cmbStatus.getSelectedItem();
        if(cmbAlasan.getSelectedIndex()>0) k+="  |  Alasan : "+cmbAlasan.getSelectedItem();
        if(cmbRawat.getSelectedIndex()>0) k+="  |  "+cmbRawat.getSelectedItem();
        if(!TCari.getText().trim().isEmpty()) k+="  |  Kata kunci : "+TCari.getText().trim();
        return k;
    }

    /** alasan berkas "Tidak Ada" diisi di halaman Mutasi Berkas (webapps/monitoringberkas) */
    private static final String ALASAN="ifnull((select mutasi_berkas_tidakada.alasan from mutasi_berkas_tidakada "+
            "where mutasi_berkas_tidakada.no_rawat=mutasi_berkas.no_rawat),'') as alasan ";

    private void siapkanTabelAlasan(){
        try (java.sql.Statement st=koneksiDB.condb().createStatement()) {
            st.executeUpdate("create table if not exists mutasi_berkas_tidakada (no_rawat varchar(17) not null,alasan varchar(150) not null,"+
                    "waktu datetime not null,primary key (no_rawat)) engine=InnoDB default charset=latin1");
        } catch (Exception e) {
            System.out.println("Notif tabel alasan mutasi berkas : "+e);
        }
    }    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
        panelGlass5 = new widget.panelisi();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Rekap Mutasi Berkas ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50,50,50))); // NOI18N
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
        label11.setPreferredSize(new java.awt.Dimension(50, 23));
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

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass5.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(155, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass5.add(TCari);

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
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnAll);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass5.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass5.add(LCount);

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
    }// </editor-fold>//GEN-END:initComponents

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            //TCari.requestFocus();
        }else if(tabMode.getRowCount()!=0){
             Map<String, Object> param = new HashMap<>();     
             param.put("namars",akses.getnamars());
             param.put("alamatrs",akses.getalamatrs());
             param.put("kotars",akses.getkabupatenrs());
             param.put("propinsirs",akses.getpropinsirs());
             param.put("kontakrs",akses.getkontakrs());
             param.put("emailrs",akses.getemailrs());   
             param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
             param.put("filter",keteranganFilter());
             Valid.MyReportqry("rptRekapMutasiBerkas.jasper","report","::[ Laporan Rekap Mutasi Berkas ]::",sqlRekap(),param);
        }
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            //Valid.pindah(evt, BtnHapus, BtnAll);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnKeluar,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
       runBackground(() ->tampil());
}//GEN-LAST:event_BtnCariActionPerformed

private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            runBackground(() ->tampil());
            this.setCursor(Cursor.getDefaultCursor());
        }else{
            Valid.pindah(evt, TCari, BtnPrint);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
            });
        }
    }//GEN-LAST:event_formWindowOpened

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
           TCari.setText("");
           runBackground(() ->tampil());
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        runBackground(() ->tampil());

    }//GEN-LAST:event_formWindowActivated

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgRekapMutasiBerkas dialog = new DlgRekapMutasiBerkas(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnAll;
    private widget.Button BtnCari;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Label LCount;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label label11;
    private widget.Label label18;
    private widget.panelisi panelGlass5;
    private widget.Table tbBangsal;
    // End of variables declaration//GEN-END:variables

    private void tampil(){        
        try{   
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            Valid.tabelKosong(tabMode);   
            koneksi=koneksiDB.condb();
            ps=koneksi.prepareStatement(sqlRekap());
            try {
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("status"),
                        rs.getString("tgl_registrasi")+" "+rs.getString("jam_reg"),
                        rs.getString("dikirim"),rs.getString("diterima"),rs.getString("kembali"),rs.getString("tidakada"),
                        rs.getString("alasan"),rs.getString("ranap"),rs.getString("status_lanjut")
                    });
                }        
                LCount.setText(""+tabMode.getRowCount());
            } catch (Exception e) {
                System.out.println("Notif : "+e);
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
