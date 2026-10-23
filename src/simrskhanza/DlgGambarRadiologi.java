package simrskhanza;

import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Daftar gambar radiologi (CT, rontgen, USG, dll.) satu pasien yang sudah dikirim
 * ke RIS/PACS alat_rs, dari semua kunjungan. Dokter cukup pilih study lalu
 * "Buka Gambar" -> viewer PACS di browser (pola URL sama dengan DlgCariPeriksaRadiologi).
 */
public class DlgGambarRadiologi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private final widget.Table tbGambar=new widget.Table();
    private final javax.swing.JLabel lblPasien=new javax.swing.JLabel();

    public DlgGambarRadiologi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Gambar Radiologi Pasien (PACS) ]::");
        setUndecorated(false);

        widget.InternalFrame frame=new widget.InternalFrame();
        frame.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240,245,235)),
                "::[ Gambar Radiologi Pasien (PACS) ]::",javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,new java.awt.Font("Tahoma",0,11),new java.awt.Color(50,50,50)));
        frame.setLayout(new BorderLayout(1,1));

        lblPasien.setFont(new java.awt.Font("Tahoma",1,12));
        lblPasien.setBorder(javax.swing.BorderFactory.createEmptyBorder(6,8,6,8));
        frame.add(lblPasien,BorderLayout.NORTH);

        tabMode=new DefaultTableModel(null,new Object[]{"Tanggal","Jam","No.Rawat","Pemeriksaan","Accession","Hasil Bacaan","Dokter Perujuk"}){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        tbGambar.setModel(tabMode);
        tbGambar.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar={80,60,130,320,150,90,200};
        for(int i=0;i<lebar.length;i++){
            tbGambar.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        tbGambar.setDefaultRenderer(Object.class,new fungsi.WarnaTable());
        tbGambar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e){
                if(e.getClickCount()==2) bukaGambar();
            }
        });
        widget.ScrollPane scroll=new widget.ScrollPane();
        scroll.setViewportView(tbGambar);
        frame.add(scroll,BorderLayout.CENTER);

        widget.panelisi panel=new widget.panelisi();
        panel.setPreferredSize(new Dimension(100,50));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT,5,9));
        widget.Button btnBuka=new widget.Button();
        btnBuka.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png")));
        btnBuka.setText("Buka Gambar");
        btnBuka.setMnemonic('B');
        btnBuka.setPreferredSize(new Dimension(140,30));
        btnBuka.addActionListener(e->bukaGambar());
        panel.add(btnBuka);
        widget.Button btnTutup=new widget.Button();
        btnTutup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        btnTutup.setText("Tutup");
        btnTutup.setMnemonic('T');
        btnTutup.setPreferredSize(new Dimension(100,30));
        btnTutup.addActionListener(e->dispose());
        panel.add(btnTutup);
        javax.swing.JLabel info=new javax.swing.JLabel("Klik 2x baris atau tombol Buka Gambar untuk membuka viewer PACS di browser");
        info.setFont(new java.awt.Font("Tahoma",2,11));
        panel.add(info);
        frame.add(panel,BorderLayout.SOUTH);

        getContentPane().add(frame);
        setSize(1000,420);
    }

    /** isi daftar study pasien; satu order bisa berisi beberapa accession (dipisah koma) */
    public void setPasien(String noRkmMedis,String namaPasien){
        lblPasien.setText("Pasien : "+noRkmMedis+" "+namaPasien);
        while(tabMode.getRowCount()>0) tabMode.removeRow(0);
        // 1 baris per study (accession), dari tabel bridging_alatrs_worklist / log lama
        java.util.List<String> orders=bridging.AlatRSWorklist.orderPasien(noRkmMedis);
        try (PreparedStatement ps=koneksiDB.condb().prepareStatement(
                "select p.tgl_permintaan,p.jam_permintaan,p.no_rawat,ifnull(d.nm_dokter,'') as dokter,"+
                "exists(select 1 from hasil_radiologi h where h.no_rawat=p.no_rawat and h.tgl_periksa=p.tgl_hasil and h.jam=p.jam_hasil) as ada_hasil "+
                "from permintaan_radiologi p left join dokter d on d.kd_dokter=p.dokter_perujuk where p.noorder=?")) {
            for(String no:orders){
                java.util.List<bridging.AlatRSWorklist.Item> items=bridging.AlatRSWorklist.untukOrder(java.util.Collections.singletonList(no));
                if(items.isEmpty()) continue;
                ps.setString(1,no);
                try (ResultSet rs=ps.executeQuery()) {
                    if(!rs.next()) continue;
                    for(bridging.AlatRSWorklist.Item it:items){
                        if(it.accession.isEmpty()) continue;
                        tabMode.addRow(new Object[]{
                            rs.getString("tgl_permintaan"),rs.getString("jam_permintaan"),rs.getString("no_rawat"),
                            it.namaPemeriksaan,it.accession,rs.getBoolean("ada_hasil")?"Sudah":"Belum",rs.getString("dokter")
                        });
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif DlgGambarRadiologi : "+e);
        }
        if(tabMode.getRowCount()>0){
            tbGambar.setRowSelectionInterval(0,0);
        }
    }

    public int jumlahGambar(){
        return tabMode.getRowCount();
    }

    private void bukaGambar(){
        int r=tbGambar.getSelectedRow();
        if(r<0){
            JOptionPane.showMessageDialog(this,"Silahkan pilih gambar yang mau dibuka...!!!");
            return;
        }
        try{
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            String url=koneksiDB.URLALATRS()+":"+koneksiDB.PORTALATRS()+"/viewer?accession="+
                    java.net.URLEncoder.encode(tbGambar.getValueAt(r,4).toString(),"UTF-8");
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Gagal buka browser: "+e.getMessage());
        }finally{
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
