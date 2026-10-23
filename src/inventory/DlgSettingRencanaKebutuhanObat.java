package inventory;

import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

/**
 * Master setting Rencana Kebutuhan Obat: kelompok, nama generik, kelipatan, VEN per item
 * dan kelipatan default per satuan.
 */
public class DlgSettingRencanaKebutuhanObat extends javax.swing.JDialog {
    private final Connection koneksi=koneksiDB.condb();
    private DefaultTableModel tabItem,tabSatuan;
    private widget.Table tbItem,tbSatuan;
    private widget.TextBox TCari;
    private widget.Label LCount;
    private final Set<Integer> barisUbah=new HashSet<>();
    private boolean sedangIsi=false;

    // kolom tab item
    static final int I_KODE=0,I_NAMA=1,I_SATUAN=2,I_GOLONGAN=3,I_OTOMATIS=4,I_KELOMPOK=5,I_GENERIK=6,I_KELIPATAN=7,I_VEN=8,I_KET=9,I_AKTIF=10;

    public DlgSettingRencanaKebutuhanObat(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        bangunForm();
        setSize(1100,620);
        setLocationRelativeTo(null);
        tampilItem();
        tampilSatuan();
    }

    private widget.Button tombol(String teks,String ikon,int lebar){
        widget.Button b=new widget.Button();
        b.setText(teks);
        if(ikon!=null){
            b.setIcon(new javax.swing.ImageIcon(getClass().getResource(ikon)));
        }
        b.setPreferredSize(new Dimension(lebar,30));
        return b;
    }

    private void bangunForm(){
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        widget.InternalFrame frame=new widget.InternalFrame();
        frame.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(240,245,235)),
                "::[ Setting Rencana Kebutuhan Obat & BMHP ]::",javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,new Font("Tahoma",0,11),new Color(50,50,50)));
        frame.setLayout(new BorderLayout(1,1));
        widget.TabPane tab=new widget.TabPane();

        // --- per item
        tabItem=new DefaultTableModel(null,new String[]{"Kode Barang","Nama Barang","Satuan","Golongan","Kelompok Otomatis",
                "Kelompok (Setting)","Nama Generik","Kelipatan","VEN","Keterangan","Aktif"}){
            @Override public boolean isCellEditable(int r,int c){return c>=I_KELOMPOK;}
        };
        tbItem=new widget.Table();
        tbItem.setModel(tabItem);
        tbItem.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar={95,230,70,130,130,150,200,70,50,180,55};
        for(int i=0;i<lebar.length;i++) tbItem.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        List<String> kel=new ArrayList<>();
        kel.add("");
        kel.addAll(RencanaKebutuhanObatHitung.KELOMPOK);
        JComboBox<String> cbKel=new JComboBox<>(kel.toArray(new String[0]));
        cbKel.setEditable(true);
        tbItem.getColumnModel().getColumn(I_KELOMPOK).setCellEditor(new DefaultCellEditor(cbKel));
        tbItem.getColumnModel().getColumn(I_VEN).setCellEditor(new DefaultCellEditor(new JComboBox<>(DlgRencanaKebutuhanObat.VEN)));
        tbItem.getColumnModel().getColumn(I_AKTIF).setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"Ya","Tidak"})));
        tabItem.addTableModelListener(e->{
            if(!sedangIsi&&e.getType()==TableModelEvent.UPDATE&&e.getFirstRow()>=0) barisUbah.add(e.getFirstRow());
        });
        widget.ScrollPane sc=new widget.ScrollPane();
        sc.setViewportView(tbItem);
        JPanel pItem=new JPanel(new BorderLayout());
        pItem.add(sc,BorderLayout.CENTER);
        widget.panelisi cari=new widget.panelisi();
        cari.setLayout(new FlowLayout(FlowLayout.LEFT,5,4));
        widget.Label lc=new widget.Label();lc.setText("Cari :");lc.setPreferredSize(new Dimension(40,23));
        TCari=new widget.TextBox();TCari.setPreferredSize(new Dimension(250,23));
        widget.Button BtnCari=tombol("","/picture/accept.png",28);BtnCari.setPreferredSize(new Dimension(28,23));
        widget.Label lk=new widget.Label();
        lk.setText("Kelompok kosong = otomatis dari golongan/kategori; kelipatan kosong = default satuan");
        lk.setPreferredSize(new Dimension(480,23));
        LCount=new widget.Label();LCount.setPreferredSize(new Dimension(120,23));
        cari.add(lc);cari.add(TCari);cari.add(BtnCari);cari.add(lk);cari.add(LCount);
        pItem.add(cari,BorderLayout.PAGE_START);
        tab.addTab("Setting Per Item",pItem);

        // --- per satuan
        tabSatuan=new DefaultTableModel(null,new String[]{"Kode Satuan","Satuan","Kelipatan Default"}){
            @Override public boolean isCellEditable(int r,int c){return c==2;}
        };
        tbSatuan=new widget.Table();
        tbSatuan.setModel(tabSatuan);
        widget.ScrollPane ss=new widget.ScrollPane();
        ss.setViewportView(tbSatuan);
        tab.addTab("Kelipatan Default Per Satuan",ss);
        frame.add(tab,BorderLayout.CENTER);

        widget.panelisi bawah=new widget.panelisi();
        bawah.setLayout(new FlowLayout(FlowLayout.LEFT,5,9));
        bawah.setPreferredSize(new Dimension(55,55));
        widget.Button BtnSimpan=tombol("Simpan","/picture/save-16x16.png",100);
        widget.Button BtnImport=tombol("Import CSV","/picture/Search-16x16.png",120);
        widget.Button BtnKeluar=tombol("Keluar","/picture/exit.png",100);
        bawah.add(BtnSimpan);bawah.add(BtnImport);bawah.add(BtnKeluar);
        frame.add(bawah,BorderLayout.PAGE_END);
        getContentPane().add(frame,BorderLayout.CENTER);

        BtnCari.addActionListener(e->tampilItem());
        TCari.addActionListener(e->tampilItem());
        BtnSimpan.addActionListener(e->{
            if(tab.getSelectedIndex()==0) simpanItem(); else simpanSatuan();
        });
        BtnImport.addActionListener(e->importCsv());
        BtnKeluar.addActionListener(e->dispose());
        BtnSimpan.setEnabled(akses.getsetting_rencana_kebutuhan_obat());
        BtnImport.setEnabled(akses.getsetting_rencana_kebutuhan_obat());
    }

    private static String nz(Object s){
        return s==null?"":s.toString();
    }

    private void tampilItem(){
        if(!barisUbah.isEmpty()&&JOptionPane.showConfirmDialog(null,"Ada perubahan yang belum disimpan, abaikan?","Konfirmasi",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
            return;
        }
        if(tbItem.isEditing()) tbItem.getCellEditor().cancelCellEditing();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        sedangIsi=true;
        tabItem.setRowCount(0);
        barisUbah.clear();
        String kata="%"+TCari.getText().trim()+"%";
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select databarang.kode_brng,databarang.nama_brng,kodesatuan.satuan,ifnull(golongan_barang.nama,'') as golongan,"+
                "ifnull(kategori_barang.nama,'') as kategori,ifnull(jenis.nama,'') as jenis,s.kelompok,s.nama_generik,s.kelipatan,s.ven,s.keterangan,s.aktif "+
                "from databarang inner join kodesatuan on databarang.kode_sat=kodesatuan.kode_sat "+
                "left join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                "left join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                "left join jenis on databarang.kdjns=jenis.kdjns "+
                "left join rencana_kebutuhan_obat_setting s on databarang.kode_brng=s.kode_brng "+
                "where databarang.status='1' and (databarang.kode_brng like ? or databarang.nama_brng like ? or ifnull(s.nama_generik,'') like ? "+
                "or ifnull(golongan_barang.nama,'') like ? or ifnull(s.kelompok,'') like ?) order by databarang.nama_brng")) {
            for(int i=1;i<=5;i++) ps.setString(i,kata);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()){
                    double kel=rs.getDouble("kelipatan");
                    boolean adaKel=!rs.wasNull();
                    tabItem.addRow(new Object[]{rs.getString("kode_brng"),rs.getString("nama_brng"),rs.getString("satuan"),rs.getString("golongan"),
                        RencanaKebutuhanObatHitung.kelompokOtomatis(rs.getString("golongan"),rs.getString("kategori"),rs.getString("jenis")),
                        nz(rs.getString("kelompok")),nz(rs.getString("nama_generik")),adaKel?trimAngka(kel):"",
                        rs.getString("ven")==null?"-":rs.getString("ven"),nz(rs.getString("keterangan")),rs.getString("aktif")==null?"Ya":rs.getString("aktif")});
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Setting Rencana Kebutuhan : "+e);
        }
        sedangIsi=false;
        LCount.setText("Item : "+tabItem.getRowCount());
        setCursor(Cursor.getDefaultCursor());
    }

    private static String trimAngka(double v){
        return v==Math.rint(v)?String.valueOf((long)v):String.valueOf(v);
    }

    private void tampilSatuan(){
        tabSatuan.setRowCount(0);
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select kodesatuan.kode_sat,kodesatuan.satuan,k.kelipatan from kodesatuan left join rencana_kebutuhan_obat_kelipatan_satuan k "+
                "on kodesatuan.kode_sat=k.kode_sat order by kodesatuan.satuan");
             ResultSet rs=ps.executeQuery()) {
            while(rs.next()){
                double kel=rs.getDouble(3);
                tabSatuan.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.wasNull()?"":trimAngka(kel)});
            }
        } catch (Exception e) {
            System.out.println("Notif Setting Rencana Kebutuhan : "+e);
        }
    }

    /** simpan / hapus setting satu item; semua isian default -> baris setting dihapus */
    private void simpanSetting(PreparedStatement psSimpan,PreparedStatement psHapus,String kode,String kelompok,String generik,String kelipatan,String ven,String ket,String aktif) throws Exception {
        kelompok=kelompok.trim().toUpperCase(Locale.ROOT);
        Double kel=null;
        if(!kelipatan.trim().equals("")){
            kel=Double.parseDouble(kelipatan.trim().replace(",","."));
            if(kel<=0) kel=null;
        }
        if(ven.equals("")) ven="-";
        if(aktif.equals("")) aktif="Ya";
        if(kelompok.equals("")&&generik.trim().equals("")&&kel==null&&ven.equals("-")&&ket.trim().equals("")&&aktif.equals("Ya")){
            psHapus.setString(1,kode);
            psHapus.executeUpdate();
            return;
        }
        psSimpan.setString(1,kode);
        psSimpan.setString(2,generik.trim().equals("")?null:generik.trim());
        psSimpan.setString(3,kelompok.equals("")?null:kelompok);
        if(kel==null) psSimpan.setNull(4,java.sql.Types.DOUBLE); else psSimpan.setDouble(4,kel);
        psSimpan.setString(5,ven);
        psSimpan.setString(6,ket.trim().equals("")?null:ket.trim());
        psSimpan.setString(7,aktif);
        psSimpan.executeUpdate();
    }

    private static final String SQL_SIMPAN="replace into rencana_kebutuhan_obat_setting (kode_brng,nama_generik,kelompok,kelipatan,ven,keterangan,aktif) values(?,?,?,?,?,?,?)";
    private static final String SQL_HAPUS="delete from rencana_kebutuhan_obat_setting where kode_brng=?";

    private void simpanItem(){
        if(tbItem.isEditing()) tbItem.getCellEditor().stopCellEditing();
        if(barisUbah.isEmpty()){
            JOptionPane.showMessageDialog(null,"Tidak ada perubahan...");
            return;
        }
        int ok=0;
        List<String> gagal=new ArrayList<>();
        try (PreparedStatement ps=koneksi.prepareStatement(SQL_SIMPAN);PreparedStatement ph=koneksi.prepareStatement(SQL_HAPUS)) {
            for(int r:barisUbah){
                try {
                    simpanSetting(ps,ph,nz(tabItem.getValueAt(r,I_KODE)),nz(tabItem.getValueAt(r,I_KELOMPOK)),nz(tabItem.getValueAt(r,I_GENERIK)),
                            nz(tabItem.getValueAt(r,I_KELIPATAN)),nz(tabItem.getValueAt(r,I_VEN)),nz(tabItem.getValueAt(r,I_KET)),nz(tabItem.getValueAt(r,I_AKTIF)));
                    ok++;
                } catch (Exception e) {
                    gagal.add(nz(tabItem.getValueAt(r,I_KODE))+" ("+e.getMessage()+")");
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Setting Rencana Kebutuhan : "+e);
        }
        barisUbah.clear();
        JOptionPane.showMessageDialog(null,ok+" item tersimpan"+(gagal.isEmpty()?"":"\nGagal : "+String.join(", ",gagal)));
    }

    private void simpanSatuan(){
        if(tbSatuan.isEditing()) tbSatuan.getCellEditor().stopCellEditing();
        int ok=0;
        try (PreparedStatement ps=koneksi.prepareStatement("replace into rencana_kebutuhan_obat_kelipatan_satuan values(?,?)");
             PreparedStatement ph=koneksi.prepareStatement("delete from rencana_kebutuhan_obat_kelipatan_satuan where kode_sat=?")) {
            for(int r=0;r<tabSatuan.getRowCount();r++){
                String v=nz(tabSatuan.getValueAt(r,2)).trim().replace(",",".");
                String kode=nz(tabSatuan.getValueAt(r,0));
                if(v.equals("")){
                    ph.setString(1,kode);
                    ph.executeUpdate();
                }else{
                    ps.setString(1,kode);
                    ps.setDouble(2,Double.parseDouble(v));
                    ps.executeUpdate();
                    ok++;
                }
            }
            JOptionPane.showMessageDialog(null,"Kelipatan default tersimpan ("+ok+" satuan)");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"Gagal menyimpan : "+e.getMessage());
        }
        tampilSatuan();
    }

    /**
     * Import CSV (pemisah , atau ;). Baris pertama judul kolom; kolom yang dikenali:
     * kode_brng (wajib), kelompok, kelipatan, ven, nama_generik, keterangan, aktif.
     * Kolom yang tidak ada di file tidak mengubah setting lama.
     */
    private void importCsv(){
        JFileChooser fc=new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV (*.csv)","csv","txt"));
        if(fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        File f=fc.getSelectedFile();
        int ok=0,lewat=0;
        List<String> gagal=new ArrayList<>();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try (BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(f),StandardCharsets.UTF_8));
             PreparedStatement ps=koneksi.prepareStatement(SQL_SIMPAN);PreparedStatement ph=koneksi.prepareStatement(SQL_HAPUS);
             PreparedStatement lama=koneksi.prepareStatement("select kelompok,nama_generik,kelipatan,ven,keterangan,aktif from rencana_kebutuhan_obat_setting where kode_brng=?");
             PreparedStatement ada=koneksi.prepareStatement("select count(*) from databarang where kode_brng=?")) {
            String judul=br.readLine();
            if(judul==null) return;
            judul=judul.replace("﻿","");
            String sep=judul.contains(";")?";":",";
            List<String> kol=new ArrayList<>();
            for(String k:judul.split(sep,-1)) kol.add(k.trim().toLowerCase(Locale.ROOT).replace("\"",""));
            int iKode=kol.indexOf("kode_brng");
            if(iKode<0){
                JOptionPane.showMessageDialog(null,"Kolom kode_brng tidak ditemukan pada baris judul.\nContoh judul : kode_brng;kelompok;kelipatan;ven");
                return;
            }
            String baris;
            while((baris=br.readLine())!=null){
                if(baris.trim().equals("")) continue;
                String[] v=baris.split(sep,-1);
                for(int i=0;i<v.length;i++) v[i]=v[i].trim().replaceAll("^\"|\"$","");
                String kode=iKode<v.length?v[iKode]:"";
                ada.setString(1,kode);
                try (ResultSet ra=ada.executeQuery()) {
                    ra.next();
                    if(ra.getInt(1)==0){
                        lewat++;
                        continue;
                    }
                }
                String[] isi={"","","","-","","Ya"};   // kelompok,generik,kelipatan,ven,ket,aktif
                lama.setString(1,kode);
                try (ResultSet rl=lama.executeQuery()) {
                    if(rl.next()){
                        isi[0]=nz(rl.getString(1));isi[1]=nz(rl.getString(2));
                        double k=rl.getDouble(3);isi[2]=rl.wasNull()?"":trimAngka(k);
                        isi[3]=nz(rl.getString(4));isi[4]=nz(rl.getString(5));isi[5]=nz(rl.getString(6));
                    }
                }
                String[] nama={"kelompok","nama_generik","kelipatan","ven","keterangan","aktif"};
                for(int n=0;n<nama.length;n++){
                    int idx=kol.indexOf(nama[n]);
                    if(idx>=0&&idx<v.length) isi[n]=v[idx];
                }
                isi[3]=isi[3].toUpperCase(Locale.ROOT);
                if(!Arrays.asList(DlgRencanaKebutuhanObat.VEN).contains(isi[3])) isi[3]="-";
                try {
                    simpanSetting(ps,ph,kode,isi[0],isi[1],isi[2],isi[3],isi[4],isi[5].equalsIgnoreCase("tidak")?"Tidak":"Ya");
                    ok++;
                } catch (Exception e) {
                    gagal.add(kode);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"Gagal import : "+e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
        JOptionPane.showMessageDialog(null,"Import selesai : "+ok+" item"+(lewat>0?"\n"+lewat+" kode tidak ada di master barang (dilewati)":"")+
                (gagal.isEmpty()?"":"\nGagal : "+String.join(", ",gagal)));
        barisUbah.clear();
        tampilItem();
    }
}
