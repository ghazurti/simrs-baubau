package inventory;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import inventory.RencanaKebutuhanObatHitung.Baris;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariPegawai;
import simrskhanza.DlgCariBangsal;

/**
 * Rencana Kebutuhan Obat & BMHP Gudang Farmasi (format Excel konfirmasi obat & BMHP).
 */
public class DlgRencanaKebutuhanObat extends javax.swing.JDialog {
    private final Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final validasi Valid=new validasi();
    private final RencanaKebutuhanObatHitung hitung=new RencanaKebutuhanObatHitung(koneksi);
    private Map<String,List<Baris>> data=new LinkedHashMap<>();
    private final List<Object> rowRef=new ArrayList<>();
    private boolean sedangIsi=false;
    private String noRencana="";
    private final DecimalFormat angka,persen;

    // kolom tabel rencana
    static final int C_KODE=0,C_NO=1,C_GENERIK=2,C_NAMA=3,C_SATUAN=4,C_F=5,C_G=6,C_H=7,C_I=8,C_J=9,C_K=10,C_L=11,C_M=12,C_N=13,C_O=14,C_P=15,C_Q=16,C_VEN=17,C_KET=18;
    static final String[] VEN={"-","V","E","N","K/E"};

    private DefaultTableModel tabMode,tabUsulan;
    private widget.Table tbRencana,tbUsulan;
    private widget.Tanggal TglAwal,TglAkhir,TglTtd;
    private widget.Label LBulan,LInfo,LTotal;
    private widget.TextBox Periode,KdBangsal,NmBangsal,StokKerja,Buffer,LeadTime,TCari,Kota;
    private widget.TextBox NmKepala,NipKepala,NmPembuat,NipPembuat,NmDirektur,NipDirektur,NmKabid,NipKabid;
    private widget.TextArea Catatan;
    private widget.CekBox ChkSemuaDepo,ChkTampilSemua,ChkPengeluaran;
    private widget.ComboBox CmbKelompok,CmbSumber,CmbHarga,CmbStatus;
    private widget.Button BtnHitung,BtnSimpan,BtnBuka,BtnCetak,BtnExcel,BtnSetting,BtnKeluar;

    public DlgRencanaKebutuhanObat(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        DecimalFormatSymbols id=new DecimalFormatSymbols(new Locale("id","ID"));
        angka=new DecimalFormat("#,##0.##",id);
        persen=new DecimalFormat("0.00",id);
        bangunForm();
        setSize(1200,680);
        setLocationRelativeTo(null);
        isiAwal();
    }

    // ------------------------------------------------------------------ UI
    private widget.Label label(String teks,int lebar){
        widget.Label l=new widget.Label();
        l.setText(teks);
        l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        l.setPreferredSize(new Dimension(lebar,23));
        return l;
    }

    private widget.TextBox teks(int lebar){
        widget.TextBox t=new widget.TextBox();
        t.setPreferredSize(new Dimension(lebar,23));
        return t;
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

    private widget.CekBox cek(String teks){
        widget.CekBox c=new widget.CekBox();
        c.setText(teks);
        c.setOpaque(false);
        return c;
    }

    private widget.Tanggal tanggal(){
        widget.Tanggal t=new widget.Tanggal();
        t.setDisplayFormat("dd-MM-yyyy");
        t.setPreferredSize(new Dimension(95,23));
        return t;
    }

    private void bangunForm(){
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        widget.InternalFrame frame=new widget.InternalFrame();
        frame.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(240,245,235)),
                "::[ Rencana Kebutuhan Obat & BMHP Gudang Farmasi ]::",javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,new Font("Tahoma",0,11),new Color(50,50,50)));
        frame.setLayout(new BorderLayout(1,1));

        // --- filter & parameter
        widget.panelisi atas=new widget.panelisi();
        atas.setLayout(new javax.swing.BoxLayout(atas,javax.swing.BoxLayout.Y_AXIS));
        atas.setPreferredSize(new Dimension(100,104));
        JPanel b1=baris(),b2=baris(),b3=baris();
        TglAwal=tanggal();TglAkhir=tanggal();
        LBulan=new widget.Label();LBulan.setPreferredSize(new Dimension(70,23));
        Periode=teks(190);
        KdBangsal=teks(55);KdBangsal.setEditable(false);
        NmBangsal=teks(150);NmBangsal.setEditable(false);
        widget.Button BtnBangsal=tombol("","/picture/190.png",28);BtnBangsal.setPreferredSize(new Dimension(28,23));
        ChkSemuaDepo=cek("Semua Depo");
        b1.add(label("Pemakaian :",75));b1.add(TglAwal);b1.add(label("s.d.",25));b1.add(TglAkhir);b1.add(LBulan);
        b1.add(label("Periode Rencana :",100));b1.add(Periode);
        b1.add(label("Stok di :",55));b1.add(KdBangsal);b1.add(NmBangsal);b1.add(BtnBangsal);b1.add(ChkSemuaDepo);

        CmbKelompok=new widget.ComboBox();
        CmbKelompok.addItem("Semua");
        for(String k:RencanaKebutuhanObatHitung.KELOMPOK){
            CmbKelompok.addItem(k);
        }
        CmbKelompok.setPreferredSize(new Dimension(150,23));
        CmbSumber=new widget.ComboBox();
        CmbSumber.addItem(RencanaKebutuhanObatHitung.SUMBER_TRANSAKSI);CmbSumber.addItem(RencanaKebutuhanObatHitung.SUMBER_RIWAYAT);
        CmbSumber.setPreferredSize(new Dimension(120,23));
        CmbHarga=new widget.ComboBox();
        CmbHarga.addItem(RencanaKebutuhanObatHitung.HARGA_MASTER);CmbHarga.addItem(RencanaKebutuhanObatHitung.HARGA_PENERIMAAN);
        CmbHarga.setPreferredSize(new Dimension(140,23));
        StokKerja=teks(35);Buffer=teks(35);LeadTime=teks(40);
        b2.add(label("Kelompok :",75));b2.add(CmbKelompok);b2.add(label("Sumber Pemakaian :",110));b2.add(CmbSumber);
        b2.add(label("Harga :",45));b2.add(CmbHarga);
        b2.add(label("Stok Kerja :",70));b2.add(StokKerja);b2.add(new JLabel("bln"));
        b2.add(label("Buffer :",50));b2.add(Buffer);b2.add(new JLabel("%"));
        b2.add(label("Lead Time :",65));b2.add(LeadTime);b2.add(new JLabel("hari"));

        ChkTampilSemua=cek("Tampilkan semua item");
        ChkPengeluaran=cek("Ikut Pengeluaran Obat & BHP");
        ChkPengeluaran.setToolTipText("Pengeluaran Obat & BHP (Stok Keluar) berisi juga ED, retur distributor, koreksi salah input");
        TCari=teks(200);
        CmbStatus=new widget.ComboBox();CmbStatus.addItem("Draft");CmbStatus.addItem("Final");CmbStatus.setPreferredSize(new Dimension(70,23));
        LInfo=new widget.Label();LInfo.setPreferredSize(new Dimension(330,23));
        b3.add(label("Cari :",75));b3.add(TCari);b3.add(ChkTampilSemua);b3.add(ChkPengeluaran);
        b3.add(label("Status :",50));b3.add(CmbStatus);b3.add(LInfo);
        atas.add(b1);atas.add(b2);atas.add(b3);
        frame.add(atas,BorderLayout.PAGE_START);

        // --- tab
        widget.TabPane tab=new widget.TabPane();
        tabMode=new DefaultTableModel(null,new String[]{"Kode Barang SIM RS","No","Nama Generik","Nama Obat Merk Dagang","Satuan",
                "Sisa Stok (F)","Rata-rata/Bln (G)","Stok Kerja (H)","Buffer (I)","Lead Time (J)","Rencana Kebutuhan (K)",
                "Rencana Pengadaan (L)","Harga Satuan (M)","Jumlah Total Harga (N)","% Item (O)","% Kumulatif (P)","Pareto (Q)","VEN","Ket."}){
            @Override public boolean isCellEditable(int r,int c){
                return r<rowRef.size()&&rowRef.get(r) instanceof Baris&&(c==C_L||c==C_VEN||c==C_KET);
            }
            @Override public Class<?> getColumnClass(int c){
                return (c>=C_F&&c<=C_P)?Double.class:Object.class;
            }
        };
        tbRencana=new widget.Table();
        tbRencana.setModel(tabMode);
        tbRencana.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar={95,35,220,220,65,70,75,75,65,70,90,90,85,110,60,70,50,45,160};
        for(int i=0;i<lebar.length;i++){
            tbRencana.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        Penanda penanda=new Penanda();
        tbRencana.setDefaultRenderer(Object.class,penanda);
        tbRencana.setDefaultRenderer(Double.class,penanda);
        tbRencana.getColumnModel().getColumn(C_VEN).setCellEditor(new DefaultCellEditor(new JComboBox<>(VEN)));
        tabMode.addTableModelListener(this::ubahSel);
        widget.ScrollPane sc=new widget.ScrollPane();
        sc.setViewportView(tbRencana);
        tab.addTab("Rencana Kebutuhan",sc);

        tabUsulan=new DefaultTableModel(null,new String[]{"No","Kelompok","Nama Obat/BMHP","Satuan","Rencana Pengadaan","Harga","Total","Keterangan (Dokter/Ruangan)"}){
            @Override public boolean isCellEditable(int r,int c){return c!=0&&c!=6;}
            @Override public Class<?> getColumnClass(int c){return (c==4||c==5||c==6)?Double.class:Object.class;}
        };
        tbUsulan=new widget.Table();
        tbUsulan.setModel(tabUsulan);
        tbUsulan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebarU={40,150,260,90,110,100,110,260};
        for(int i=0;i<lebarU.length;i++){
            tbUsulan.getColumnModel().getColumn(i).setPreferredWidth(lebarU[i]);
        }
        tbUsulan.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox<>(RencanaKebutuhanObatHitung.KELOMPOK.toArray(new String[0]))));
        tabUsulan.addTableModelListener(e->{
            if(sedangIsi||e.getType()!=TableModelEvent.UPDATE||e.getColumn()==6) return;
            int r=e.getFirstRow();
            if(r>=0&&r<tabUsulan.getRowCount()){
                tabUsulan.setValueAt(dbl(tabUsulan.getValueAt(r,4))*dbl(tabUsulan.getValueAt(r,5)),r,6);
            }
        });
        JPanel pUsulan=new JPanel(new BorderLayout());
        widget.ScrollPane scu=new widget.ScrollPane();
        scu.setViewportView(tbUsulan);
        pUsulan.add(scu,BorderLayout.CENTER);
        widget.panelisi pu=new widget.panelisi();
        pu.setLayout(new FlowLayout(FlowLayout.LEFT,5,4));
        widget.Button BtnTambah=tombol("Tambah Baris","/picture/plus_16.png",130);
        widget.Button BtnHapusBaris=tombol("Hapus Baris","/picture/stop_f2.png",120);
        pu.add(BtnTambah);pu.add(BtnHapusBaris);
        pUsulan.add(pu,BorderLayout.PAGE_END);
        tab.addTab("Usulan Obat/BMHP Baru",pUsulan);

        JPanel pTtd=new JPanel(new java.awt.GridLayout(0,1,0,2));
        pTtd.setBackground(Color.WHITE);
        Kota=teks(150);TglTtd=tanggal();
        JPanel t0=baris();t0.add(label("Kota & Tanggal :",210));t0.add(Kota);t0.add(TglTtd);pTtd.add(t0);
        NmKepala=teks(260);NipKepala=teks(170);pTtd.add(barisTtd("Kepala Instalasi Farmasi :",NmKepala,NipKepala));
        NmPembuat=teks(260);NipPembuat=teks(170);pTtd.add(barisTtd("Pembuat Laporan / PJ Gudang Farmasi :",NmPembuat,NipPembuat));
        NmDirektur=teks(260);NipDirektur=teks(170);pTtd.add(barisTtd("Direktur RSUD :",NmDirektur,NipDirektur));
        NmKabid=teks(260);NipKabid=teks(170);pTtd.add(barisTtd("Kepala Bidang Pelayanan Penunjang :",NmKabid,NipKabid));
        Catatan=new widget.TextArea();Catatan.setLineWrap(true);Catatan.setWrapStyleWord(true);
        widget.ScrollPane scc=new widget.ScrollPane();scc.setViewportView(Catatan);scc.setPreferredSize(new Dimension(600,46));
        JPanel t5=baris();t5.add(label("Catatan kaki :",210));t5.add(scc);pTtd.add(t5);
        JPanel pTtdLuar=new JPanel(new BorderLayout());pTtdLuar.setBackground(Color.WHITE);pTtdLuar.add(pTtd,BorderLayout.NORTH);
        tab.addTab("Tanda Tangan & Catatan",pTtdLuar);
        frame.add(tab,BorderLayout.CENTER);

        // --- tombol
        widget.panelisi bawah=new widget.panelisi();
        bawah.setLayout(new FlowLayout(FlowLayout.LEFT,5,9));
        bawah.setPreferredSize(new Dimension(55,55));
        BtnHitung=tombol("Hitung","/picture/accept.png",100);
        BtnSimpan=tombol("Simpan","/picture/save-16x16.png",100);
        BtnBuka=tombol("Buka","/picture/Search-16x16.png",95);
        BtnCetak=tombol("Cetak","/picture/b_print.png",95);
        BtnExcel=tombol("Excel","/picture/b_print.png",95);
        BtnSetting=tombol("Setting","/picture/category.png",100);
        BtnKeluar=tombol("Keluar","/picture/exit.png",100);
        LTotal=new widget.Label();LTotal.setPreferredSize(new Dimension(330,23));
        bawah.add(BtnHitung);bawah.add(BtnSimpan);bawah.add(BtnBuka);bawah.add(BtnCetak);bawah.add(BtnExcel);bawah.add(BtnSetting);bawah.add(BtnKeluar);bawah.add(LTotal);
        frame.add(bawah,BorderLayout.PAGE_END);
        getContentPane().add(frame,BorderLayout.CENTER);

        // --- aksi
        TglAwal.addPropertyChangeListener(e->tampilBulan());
        TglAkhir.addPropertyChangeListener(e->tampilBulan());
        TglAwal.addItemListener(e->tampilBulan());
        TglAkhir.addItemListener(e->tampilBulan());
        BtnBangsal.addActionListener(e->cariBangsal());
        ChkSemuaDepo.addActionListener(e->{KdBangsal.setEnabled(!ChkSemuaDepo.isSelected());NmBangsal.setEnabled(!ChkSemuaDepo.isSelected());});
        BtnHitung.addActionListener(e->jalankanHitung());
        TCari.addActionListener(e->jalankanHitung());
        BtnSimpan.addActionListener(e->simpan());
        BtnBuka.addActionListener(e->bukaTersimpan());
        BtnCetak.addActionListener(e->cetak());
        BtnExcel.addActionListener(e->exportExcel());
        BtnSetting.addActionListener(e->{
            DlgSettingRencanaKebutuhanObat s=new DlgSettingRencanaKebutuhanObat(null,true);
            s.setSize(getWidth()-40,getHeight()-40);
            s.setLocationRelativeTo(this);
            s.setVisible(true);
        });
        BtnKeluar.addActionListener(e->dispose());
        BtnTambah.addActionListener(e->{
            if(tbUsulan.isEditing()) tbUsulan.getCellEditor().stopCellEditing();
            tabUsulan.addRow(new Object[]{tabUsulan.getRowCount()+1,"OBAT","","",0d,0d,0d,""});
        });
        BtnHapusBaris.addActionListener(e->{
            int r=tbUsulan.getSelectedRow();
            if(r>=0){
                if(tbUsulan.isEditing()) tbUsulan.getCellEditor().cancelCellEditing();
                tabUsulan.removeRow(r);
                for(int i=0;i<tabUsulan.getRowCount();i++) tabUsulan.setValueAt(i+1,i,0);
            }
        });
    }

    private JPanel baris(){
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,4,2));
        p.setOpaque(false);
        return p;
    }

    private JPanel barisTtd(String judul,widget.TextBox nama,widget.TextBox nip){
        JPanel p=baris();
        p.add(label(judul,210));
        p.add(nama);
        p.add(label("NIP :",35));
        p.add(nip);
        widget.Button b=tombol("","/picture/190.png",28);
        b.setPreferredSize(new Dimension(28,23));
        b.addActionListener(e->cariPegawai(nama,nip));
        p.add(b);
        return p;
    }

    /** baris judul kelompok & total dicetak tebal */
    private class Penanda extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean fokus,int r,int c){
            Object tampil=v;
            if(v instanceof Double){
                tampil=(c==C_O||c==C_P)?persen.format((Double)v*100)+"%":angka.format(v);
            }
            Component k=super.getTableCellRendererComponent(t,tampil,sel,fokus,r,c);
            setHorizontalAlignment(v instanceof Double?RIGHT:LEFT);
            Object ref=r<rowRef.size()?rowRef.get(r):null;
            if(ref instanceof String){
                k.setFont(k.getFont().deriveFont(Font.BOLD));
                k.setBackground(((String)ref).startsWith("H:")?new Color(222,235,247):new Color(255,242,204));
                k.setForeground(new Color(50,50,50));
            }else{
                k.setBackground(sel?new Color(255,255,200):(c==C_L||c==C_VEN||c==C_KET?new Color(245,255,245):Color.WHITE));
                k.setForeground(new Color(50,50,50));
            }
            return k;
        }
    }

    // ------------------------------------------------------------ nilai awal
    private void isiAwal(){
        LocalDate kini=LocalDate.now();
        // default: pemakaian Jan tahun lalu s.d. akhir bulan lalu, rencana triwulan berikutnya
        LocalDate akhir=kini.withDayOfMonth(1).minusDays(1);
        TglAwal.setDate(java.sql.Date.valueOf(LocalDate.of(akhir.getYear()-1,1,1)));
        TglAkhir.setDate(java.sql.Date.valueOf(akhir));
        String[] bln={"JANUARI","FEBRUARI","MARET","APRIL","MEI","JUNI","JULI","AGUSTUS","SEPTEMBER","OKTOBER","NOVEMBER","DESEMBER"};
        LocalDate r1=kini.withDayOfMonth(1);
        LocalDate r3=r1.plusMonths(2);
        Periode.setText(bln[r1.getMonthValue()-1]+" - "+bln[r3.getMonthValue()-1]+" "+r3.getYear());
        StokKerja.setText("4");Buffer.setText("20");LeadTime.setText("60");
        Kota.setText("Baubau");
        TglTtd.setDate(new java.util.Date());
        String kd=Sequel.cariIsi("select kd_bangsal from bangsal where nm_bangsal like '%gudang farmasi%' order by kd_bangsal limit 1");
        KdBangsal.setText(kd);
        NmBangsal.setText(Sequel.cariIsi("select nm_bangsal from bangsal where kd_bangsal=?",kd));
        // penandatangan & catatan dari rencana terakhir
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select kepala_instalasi_nama,kepala_instalasi_nip,pembuat_nama,pembuat_nip,direktur_nama,direktur_nip,kabid_nama,kabid_nip,ttd_kota "+
                "from rencana_kebutuhan_obat order by tgl_rencana desc,no_rencana desc limit 1");
             ResultSet rs=ps.executeQuery()) {
            if(rs.next()){
                NmKepala.setText(nz(rs.getString(1)));NipKepala.setText(nz(rs.getString(2)));
                NmPembuat.setText(nz(rs.getString(3)));NipPembuat.setText(nz(rs.getString(4)));
                NmDirektur.setText(nz(rs.getString(5)));NipDirektur.setText(nz(rs.getString(6)));
                NmKabid.setText(nz(rs.getString(7)));NipKabid.setText(nz(rs.getString(8)));
                if(!nz(rs.getString(9)).equals("")) Kota.setText(rs.getString(9));
            }
        } catch (Exception e) {
            System.out.println("Notif Rencana Kebutuhan : "+e);
        }
        BtnSimpan.setEnabled(akses.getrencana_kebutuhan_obat());
        BtnSetting.setEnabled(akses.getsetting_rencana_kebutuhan_obat());
        tampilBulan();
    }

    private static String nz(String s){
        return s==null?"":s;
    }

    private static double dbl(Object o){
        if(o==null) return 0;
        if(o instanceof Number) return ((Number)o).doubleValue();
        try {
            return Double.parseDouble(o.toString().replace(",","."));
        } catch (Exception e) {
            return 0;
        }
    }

    private LocalDate tgl(widget.Tanggal t){
        return LocalDate.parse(Valid.SetTgl(t.getSelectedItem()+""));
    }

    private void tampilBulan(){
        try {
            LBulan.setText("("+RencanaKebutuhanObatHitung.jumlahBulan(tgl(TglAwal),tgl(TglAkhir))+" bulan)");
        } catch (Exception e) {
            LBulan.setText("");
        }
    }

    private void cariBangsal(){
        DlgCariBangsal bangsal=new DlgCariBangsal(null,false);
        bangsal.addWindowListener(new WindowAdapter(){
            @Override public void windowClosed(WindowEvent e){
                if(bangsal.getTable().getSelectedRow()!=-1){
                    KdBangsal.setText(bangsal.getTable().getValueAt(bangsal.getTable().getSelectedRow(),0).toString());
                    NmBangsal.setText(bangsal.getTable().getValueAt(bangsal.getTable().getSelectedRow(),1).toString());
                    ChkSemuaDepo.setSelected(false);
                }
            }
        });
        bangsal.isCek();
        bangsal.setSize(getWidth()-40,getHeight()-40);
        bangsal.setLocationRelativeTo(this);
        bangsal.setVisible(true);
    }

    private void cariPegawai(widget.TextBox nama,widget.TextBox nip){
        DlgCariPegawai pegawai=new DlgCariPegawai(null,false);
        pegawai.addWindowListener(new WindowAdapter(){
            @Override public void windowClosed(WindowEvent e){
                int r=pegawai.getTable().getSelectedRow();
                if(r!=-1){
                    String kode=pegawai.getTable().getValueAt(r,0).toString();
                    nama.setText(pegawai.getTable().getValueAt(r,1).toString());
                    nip.setText(kode.matches("[0-9]{18}")?kode.substring(0,8)+" "+kode.substring(8,14)+" "+kode.substring(14,15)+" "+kode.substring(15):"");
                }
            }
        });
        pegawai.emptTeks();
        pegawai.setSize(getWidth()-40,getHeight()-40);
        pegawai.setLocationRelativeTo(this);
        pegawai.setVisible(true);
    }

    // ---------------------------------------------------------------- hitung
    private boolean bacaParameter(){
        try {
            hitung.tglAwal=tgl(TglAwal);
            hitung.tglAkhir=tgl(TglAkhir);
            if(hitung.tglAkhir.isBefore(hitung.tglAwal)){
                JOptionPane.showMessageDialog(null,"Tanggal akhir pemakaian tidak boleh sebelum tanggal awal...!!!");
                return false;
            }
            hitung.bulanStokKerja=Double.parseDouble(StokKerja.getText().trim().replace(",","."));
            hitung.persenBuffer=Double.parseDouble(Buffer.getText().trim().replace(",","."));
            hitung.leadTimeHari=Integer.parseInt(LeadTime.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"Parameter stok kerja / buffer / lead time harus angka...!!!");
            return false;
        }
        hitung.kdBangsalStok=ChkSemuaDepo.isSelected()?null:KdBangsal.getText().trim();
        hitung.sumber=CmbSumber.getSelectedItem().toString();
        hitung.harga=CmbHarga.getSelectedItem().toString();
        hitung.ikutPengeluaranBhp=ChkPengeluaran.isSelected();
        hitung.tampilSemua=ChkTampilSemua.isSelected();
        return true;
    }

    private void jalankanHitung(){
        if(!bacaParameter()) return;
        final String kelompok=CmbKelompok.getSelectedItem().toString(),cari=TCari.getText();
        BtnHitung.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        LInfo.setText("Menghitung pemakaian "+hitung.jumlahBulan()+" bulan...");
        new SwingWorker<Map<String,List<Baris>>,Void>(){
            @Override protected Map<String,List<Baris>> doInBackground() throws Exception {
                return hitung.hitung(kelompok,cari);
            }
            @Override protected void done(){
                try {
                    data=get();
                    noRencana="";
                    isiTabel();
                    LInfo.setText("Hasil hitung baru (belum disimpan)");
                } catch (Exception e) {
                    System.out.println("Notif Rencana Kebutuhan : "+e);
                    JOptionPane.showMessageDialog(null,"Gagal menghitung : "+e.getMessage());
                    LInfo.setText("");
                }
                BtnHitung.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());
            }
        }.execute();
    }

    private void isiTabel(){
        if(tbRencana.isEditing()) tbRencana.getCellEditor().cancelCellEditing();
        sedangIsi=true;
        tabMode.setRowCount(0);
        rowRef.clear();
        for(Map.Entry<String,List<Baris>> e:data.entrySet()){
            if(e.getValue().isEmpty()) continue;
            rowRef.add("H:"+e.getKey());
            tabMode.addRow(new Object[]{"","",e.getKey(),"","",null,null,null,null,null,null,null,null,null,null,null,"","",""});
            int no=1;
            for(Baris b:e.getValue()){
                rowRef.add(b);
                tabMode.addRow(new Object[]{b.kode,no++,b.generik,b.nama,b.satuan,b.f,b.g,b.h,b.i,b.j,b.k,b.l,b.m,b.n,b.o,b.p,b.pareto,b.ven,b.ket});
            }
            rowRef.add("T:"+e.getKey());
            tabMode.addRow(new Object[]{"","","TOTAL "+e.getKey(),"","",null,null,null,null,null,null,null,null,total(e.getValue()),null,null,"","",""});
        }
        sedangIsi=false;
        tampilTotal();
    }

    private static double total(List<Baris> l){
        double t=0;
        for(Baris b:l) t+=b.n;
        return t;
    }

    private void tampilTotal(){
        double t=0;
        int n=0;
        for(List<Baris> l:data.values()){
            t+=total(l);
            n+=l.size();
        }
        LTotal.setText("Item : "+n+"   Total : Rp "+angka.format(t));
    }

    /** edit L / VEN / KET langsung menghitung ulang N, O, P, Q & TOTAL kelompok */
    private void ubahSel(TableModelEvent e){
        if(sedangIsi||e.getType()!=TableModelEvent.UPDATE) return;
        int r=e.getFirstRow(),c=e.getColumn();
        if(r<0||r>=rowRef.size()||!(rowRef.get(r) instanceof Baris)) return;
        Baris b=(Baris)rowRef.get(r);
        if(c==C_VEN){
            b.ven=nz((String)tabMode.getValueAt(r,c));
        }else if(c==C_KET){
            b.ket=nz((String)tabMode.getValueAt(r,c));
        }else if(c==C_L){
            b.l=Math.max(0,dbl(tabMode.getValueAt(r,c)));
            b.n=b.l*b.m;
            List<Baris> kel=data.get(b.kelompok);
            RencanaKebutuhanObatHitung.hitungPareto(kel);
            SwingUtilities.invokeLater(()->segarkanKelompok(b.kelompok));
        }
    }

    private void segarkanKelompok(String kelompok){
        sedangIsi=true;
        for(int r=0;r<rowRef.size();r++){
            Object ref=rowRef.get(r);
            if(ref instanceof Baris&&((Baris)ref).kelompok.equals(kelompok)){
                Baris b=(Baris)ref;
                tabMode.setValueAt(b.l,r,C_L);
                tabMode.setValueAt(b.n,r,C_N);
                tabMode.setValueAt(b.o,r,C_O);
                tabMode.setValueAt(b.p,r,C_P);
                tabMode.setValueAt(b.pareto,r,C_Q);
            }else if(("T:"+kelompok).equals(ref)){
                tabMode.setValueAt(total(data.get(kelompok)),r,C_N);
            }
        }
        sedangIsi=false;
        tampilTotal();
    }

    // ---------------------------------------------------------------- simpan
    private void hentikanEdit(){
        if(tbRencana.isEditing()) tbRencana.getCellEditor().stopCellEditing();
        if(tbUsulan.isEditing()) tbUsulan.getCellEditor().stopCellEditing();
    }

    private void simpan(){
        hentikanEdit();
        if(data.values().stream().allMatch(List::isEmpty)){
            JOptionPane.showMessageDialog(null,"Belum ada hasil hitung yang bisa disimpan...!!!");
            return;
        }
        if(!bacaParameter()) return;
        if(!noRencana.equals("")&&Sequel.cariIsi("select status from rencana_kebutuhan_obat where no_rencana=?",noRencana).equals("Final")&&
                JOptionPane.showConfirmDialog(null,"Rencana "+noRencana+" sudah Final. Tetap timpa?","Konfirmasi",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        boolean baru=noRencana.equals("");
        String no=noRencana;
        boolean autoLama=true;
        try {
            autoLama=koneksi.getAutoCommit();
            koneksi.setAutoCommit(false);
            if(baru){
                LocalDate kini=LocalDate.now();
                String awal="RKO/"+kini.getYear()+"/"+String.format("%02d",kini.getMonthValue())+"/";
                int urut=(int)Sequel.cariIsiAngka("select ifnull(max(convert(right(no_rencana,3),signed)),0) from rencana_kebutuhan_obat where no_rencana like ?",awal+"%");
                no=awal+String.format("%03d",urut+1);
            }
            try (PreparedStatement ps=koneksi.prepareStatement(
                    "replace into rencana_kebutuhan_obat (no_rencana,tgl_rencana,periode_rencana,tgl_awal_pemakaian,tgl_akhir_pemakaian,jml_bulan,"+
                    "bulan_stok_kerja,persen_buffer,lead_time_hari,kd_bangsal_stok,nip,catatan,harga_dari,sumber_pemakaian,ttd_kota,ttd_tanggal,"+
                    "kepala_instalasi_nama,kepala_instalasi_nip,pembuat_nama,pembuat_nip,direktur_nama,direktur_nip,kabid_nama,kabid_nip,status) "+
                    "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                int i=1;
                ps.setString(i++,no);
                ps.setString(i++,baru?LocalDate.now().toString():Sequel.cariIsi("select tgl_rencana from rencana_kebutuhan_obat where no_rencana=?",no));
                ps.setString(i++,Periode.getText());
                ps.setString(i++,hitung.tglAwal.toString());
                ps.setString(i++,hitung.tglAkhir.toString());
                ps.setInt(i++,hitung.jumlahBulan());
                ps.setDouble(i++,hitung.bulanStokKerja);
                ps.setDouble(i++,hitung.persenBuffer);
                ps.setInt(i++,hitung.leadTimeHari);
                ps.setString(i++,hitung.kdBangsalStok);
                ps.setString(i++,akses.getkode());
                ps.setString(i++,Catatan.getText());
                ps.setString(i++,hitung.harga);
                ps.setString(i++,hitung.sumber);
                ps.setString(i++,Kota.getText());
                ps.setString(i++,tgl(TglTtd).toString());
                for(widget.TextBox t:new widget.TextBox[]{NmKepala,NipKepala,NmPembuat,NipPembuat,NmDirektur,NipDirektur,NmKabid,NipKabid}){
                    ps.setString(i++,t.getText());
                }
                ps.setString(i++,CmbStatus.getSelectedItem().toString());
                ps.executeUpdate();
            }
            try (PreparedStatement ps=koneksi.prepareStatement("delete from rencana_kebutuhan_obat_detail where no_rencana=?")) {
                ps.setString(1,no);
                ps.executeUpdate();
            }
            try (PreparedStatement ps=koneksi.prepareStatement("delete from rencana_kebutuhan_obat_usulan_baru where no_rencana=?")) {
                ps.setString(1,no);
                ps.executeUpdate();
            }
            try (PreparedStatement ps=koneksi.prepareStatement(
                    "insert into rencana_kebutuhan_obat_detail values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                for(List<Baris> l:data.values()){
                    for(Baris b:l){
                        int i=1;
                        ps.setString(i++,no);ps.setString(i++,b.kode);ps.setString(i++,b.kelompok);
                        for(double v:new double[]{b.f,b.g,b.h,b.i,b.j,b.k,b.l,b.m,b.n,b.o,b.p}) ps.setDouble(i++,v);
                        ps.setString(i++,b.pareto);ps.setString(i++,b.ven);ps.setString(i++,b.ket);
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps=koneksi.prepareStatement(
                    "insert into rencana_kebutuhan_obat_usulan_baru values(?,?,?,?,?,?,?,?)")) {
                int urut=1;
                for(int r=0;r<tabUsulan.getRowCount();r++){
                    String nama=nz((String)tabUsulan.getValueAt(r,2)).trim();
                    if(nama.equals("")) continue;
                    ps.setString(1,no);ps.setInt(2,urut++);
                    ps.setString(3,nz((String)tabUsulan.getValueAt(r,1)));ps.setString(4,nama);
                    ps.setString(5,nz((String)tabUsulan.getValueAt(r,3)));
                    ps.setDouble(6,dbl(tabUsulan.getValueAt(r,4)));ps.setDouble(7,dbl(tabUsulan.getValueAt(r,5)));
                    ps.setString(8,nz((String)tabUsulan.getValueAt(r,7)));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            koneksi.commit();
            noRencana=no;
            LInfo.setText("Tersimpan : "+no+" ("+CmbStatus.getSelectedItem()+")");
            JOptionPane.showMessageDialog(null,"Rencana kebutuhan tersimpan dengan nomor "+no);
        } catch (Exception e) {
            try { koneksi.rollback(); } catch (Exception ex) { }
            System.out.println("Notif Simpan Rencana Kebutuhan : "+e);
            JOptionPane.showMessageDialog(null,"Gagal menyimpan : "+e.getMessage());
        } finally {
            try { koneksi.setAutoCommit(autoLama); } catch (Exception ex) { }
            setCursor(Cursor.getDefaultCursor());
        }
    }

    // ------------------------------------------------------------ buka lagi
    private void bukaTersimpan(){
        DefaultTableModel m=new DefaultTableModel(null,new String[]{"No.Rencana","Tgl","Periode Rencana","Pemakaian","Item","Status"}){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select r.no_rencana,r.tgl_rencana,r.periode_rencana,concat(date_format(r.tgl_awal_pemakaian,'%d-%m-%Y'),' s.d. ',date_format(r.tgl_akhir_pemakaian,'%d-%m-%Y')),"+
                "(select count(*) from rencana_kebutuhan_obat_detail d where d.no_rencana=r.no_rencana),r.status "+
                "from rencana_kebutuhan_obat r order by r.tgl_rencana desc,r.no_rencana desc");
             ResultSet rs=ps.executeQuery()) {
            while(rs.next()){
                m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getInt(5),rs.getString(6)});
            }
        } catch (Exception e) {
            System.out.println("Notif Rencana Kebutuhan : "+e);
        }
        widget.Table t=new widget.Table();
        t.setModel(m);
        widget.ScrollPane sp=new widget.ScrollPane();
        sp.setViewportView(t);
        sp.setPreferredSize(new Dimension(720,320));
        t.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2){
                    SwingUtilities.getWindowAncestor(t).dispose();
                }
            }
        });
        Object[] pilihan=akses.getrencana_kebutuhan_obat()?new Object[]{"Buka","Hapus","Batal"}:new Object[]{"Buka","Batal"};
        int jawab=JOptionPane.showOptionDialog(null,sp,"Rencana Kebutuhan Tersimpan",JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,pilihan,pilihan[0]);
        int r=t.getSelectedRow();
        if(r<0) return;
        String no=m.getValueAt(r,0).toString();
        if(jawab==JOptionPane.CLOSED_OPTION||jawab==0){
            muat(no);
        }else if(jawab==1&&pilihan.length==3){
            if(JOptionPane.showConfirmDialog(null,"Hapus rencana "+no+" ?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
                Sequel.queryu2("delete from rencana_kebutuhan_obat where no_rencana=?",1,new String[]{no});
                if(no.equals(noRencana)){
                    noRencana="";
                    LInfo.setText("");
                }
            }
        }
    }

    private void muat(String no){
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            try (PreparedStatement ps=koneksi.prepareStatement("select * from rencana_kebutuhan_obat where no_rencana=?")) {
                ps.setString(1,no);
                try (ResultSet rs=ps.executeQuery()) {
                    if(!rs.next()) return;
                    Periode.setText(nz(rs.getString("periode_rencana")));
                    TglAwal.setDate(rs.getDate("tgl_awal_pemakaian"));
                    TglAkhir.setDate(rs.getDate("tgl_akhir_pemakaian"));
                    StokKerja.setText(angka.format(rs.getDouble("bulan_stok_kerja")).replace(".",""));
                    Buffer.setText(angka.format(rs.getDouble("persen_buffer")).replace(".",""));
                    LeadTime.setText(String.valueOf(rs.getInt("lead_time_hari")));
                    String kd=rs.getString("kd_bangsal_stok");
                    ChkSemuaDepo.setSelected(kd==null||kd.equals(""));
                    if(kd!=null&&!kd.equals("")){
                        KdBangsal.setText(kd);
                        NmBangsal.setText(Sequel.cariIsi("select nm_bangsal from bangsal where kd_bangsal=?",kd));
                    }
                    CmbHarga.setSelectedItem(rs.getString("harga_dari"));
                    CmbSumber.setSelectedItem(rs.getString("sumber_pemakaian"));
                    CmbStatus.setSelectedItem(rs.getString("status"));
                    Catatan.setText(nz(rs.getString("catatan")));
                    Kota.setText(nz(rs.getString("ttd_kota")));
                    if(rs.getDate("ttd_tanggal")!=null) TglTtd.setDate(rs.getDate("ttd_tanggal"));
                    NmKepala.setText(nz(rs.getString("kepala_instalasi_nama")));NipKepala.setText(nz(rs.getString("kepala_instalasi_nip")));
                    NmPembuat.setText(nz(rs.getString("pembuat_nama")));NipPembuat.setText(nz(rs.getString("pembuat_nip")));
                    NmDirektur.setText(nz(rs.getString("direktur_nama")));NipDirektur.setText(nz(rs.getString("direktur_nip")));
                    NmKabid.setText(nz(rs.getString("kabid_nama")));NipKabid.setText(nz(rs.getString("kabid_nip")));
                }
            }
            bacaParameter();
            Map<String,List<Baris>> hasil=new LinkedHashMap<>();
            for(String k:RencanaKebutuhanObatHitung.KELOMPOK) hasil.put(k,new ArrayList<>());
            try (PreparedStatement ps=koneksi.prepareStatement(
                    "select d.*,databarang.nama_brng,kodesatuan.satuan,s.nama_generik from rencana_kebutuhan_obat_detail d "+
                    "inner join databarang on d.kode_brng=databarang.kode_brng inner join kodesatuan on databarang.kode_sat=kodesatuan.kode_sat "+
                    "left join rencana_kebutuhan_obat_setting s on d.kode_brng=s.kode_brng where d.no_rencana=?")) {
                ps.setString(1,no);
                try (ResultSet rs=ps.executeQuery()) {
                    while(rs.next()){
                        Baris b=new Baris();
                        b.kode=rs.getString("kode_brng");
                        b.nama=rs.getString("nama_brng").trim();
                        String gen=rs.getString("nama_generik");
                        b.generik=(gen==null||gen.trim().equals(""))?b.nama:gen.trim();
                        b.satuan=rs.getString("satuan");
                        b.kelompok=rs.getString("kelompok");
                        b.f=rs.getDouble("sisa_stok");b.g=rs.getDouble("rata_rata");b.h=rs.getDouble("stok_kerja");
                        b.i=rs.getDouble("buffer");b.j=rs.getDouble("lead_time");b.k=rs.getDouble("rencana_kebutuhan");
                        b.l=rs.getDouble("rencana_pengadaan");b.m=rs.getDouble("harga");b.n=rs.getDouble("total");
                        b.o=rs.getDouble("persen_item");b.p=rs.getDouble("persen_kumulatif");
                        b.pareto=nz(rs.getString("pareto"));b.ven=nz(rs.getString("ven"));b.ket=nz(rs.getString("keterangan"));
                        hasil.computeIfAbsent(b.kelompok,x->new ArrayList<>()).add(b);
                    }
                }
            }
            for(List<Baris> l:hasil.values()){
                l.sort((a,b)->a.generik.compareToIgnoreCase(b.generik));
            }
            data=hasil;
            isiTabel();
            sedangIsi=true;
            tabUsulan.setRowCount(0);
            try (PreparedStatement ps=koneksi.prepareStatement("select * from rencana_kebutuhan_obat_usulan_baru where no_rencana=? order by no_urut")) {
                ps.setString(1,no);
                try (ResultSet rs=ps.executeQuery()) {
                    while(rs.next()){
                        tabUsulan.addRow(new Object[]{rs.getInt("no_urut"),rs.getString("kelompok"),rs.getString("nama_barang"),rs.getString("satuan"),
                            rs.getDouble("jumlah"),rs.getDouble("harga"),rs.getDouble("jumlah")*rs.getDouble("harga"),rs.getString("keterangan")});
                    }
                }
            }
            sedangIsi=false;
            noRencana=no;
            LInfo.setText("Rencana tersimpan : "+no+" ("+CmbStatus.getSelectedItem()+")");
        } catch (Exception e) {
            sedangIsi=false;
            System.out.println("Notif Rencana Kebutuhan : "+e);
            JOptionPane.showMessageDialog(null,"Gagal membuka rencana : "+e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    // ------------------------------------------------------ cetak & excel
    private String bulanTahun(LocalDate t,boolean pendek){
        String[] bln={"JANUARI","FEBRUARI","MARET","APRIL","MEI","JUNI","JULI","AGUSTUS","SEPTEMBER","OKTOBER","NOVEMBER","DESEMBER"};
        String b=bln[t.getMonthValue()-1];
        return (pendek?b.substring(0,3):b)+" "+t.getYear();
    }

    /** teks dinamis judul kolom (sama dengan Excel) */
    private Map<String,Object> paramJudul(){
        Map<String,Object> p=new HashMap<>();
        p.put("judul_g","RATA-RATA PER BULAN ("+bulanTahun(hitung.tglAwal,true)+" - "+bulanTahun(hitung.tglAkhir,false)+")");
        p.put("judul_h","STOK KERJA (PEMAKAIAN RATA-RATA X "+angka.format(hitung.bulanStokKerja)+" BLN)");
        p.put("judul_i","BUFFER STOCK ("+angka.format(hitung.persenBuffer)+"% x STOK KERJA)");
        p.put("judul_j","LEAD TIME STOCK "+hitung.leadTimeHari+" HARI");
        return p;
    }

    /** baris datasource cetakan: data per kelompok lalu usulan baru */
    private List<Map<String,?>> barisCetak(){
        List<Map<String,?>> rows=new ArrayList<>();
        int urutKel=0;
        for(Map.Entry<String,List<Baris>> e:data.entrySet()){
            urutKel++;
            int no=1;
            for(Baris b:e.getValue()){
                Map<String,Object> m=new HashMap<>();
                m.put("urut_kelompok",urutKel);m.put("kelompok",e.getKey());m.put("jenis","DATA");
                m.put("kode",b.kode);m.put("no",no++);m.put("generik",b.generik);m.put("nama",b.nama);m.put("satuan",b.satuan);
                m.put("f",b.f);m.put("g",b.g);m.put("h",b.h);m.put("i",b.i);m.put("j",b.j);m.put("k",b.k);
                m.put("l",b.l);m.put("m",b.m);m.put("n",b.n);m.put("o",b.o);m.put("p",b.p);
                m.put("q",b.pareto);m.put("ven",b.ven);m.put("ket",b.ket);
                rows.add(m);
            }
        }
        int no=1;
        for(int r=0;r<tabUsulan.getRowCount();r++){
            String nama=nz((String)tabUsulan.getValueAt(r,2)).trim();
            if(nama.equals("")) continue;
            Map<String,Object> m=new HashMap<>();
            m.put("urut_kelompok",99);m.put("kelompok","USULAN OBAT/BMHP BARU");m.put("jenis","USULAN");
            m.put("kode","");m.put("no",no++);m.put("generik",nama);m.put("nama",nz((String)tabUsulan.getValueAt(r,1)));
            m.put("satuan",nz((String)tabUsulan.getValueAt(r,3)));
            double l=dbl(tabUsulan.getValueAt(r,4)),h=dbl(tabUsulan.getValueAt(r,5));
            m.put("l",l);m.put("m",h);m.put("n",l*h);m.put("q","");m.put("ven","");m.put("ket",nz((String)tabUsulan.getValueAt(r,7)));
            rows.add(m);
        }
        return rows;
    }

    private void cetak(){
        hentikanEdit();
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            return;
        }
        if(!bacaParameter()) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Map<String,Object> param=paramJudul();
            boolean obatSaja=true,bmhpSaja=true;
            for(Map.Entry<String,List<Baris>> e:data.entrySet()){
                if(e.getValue().isEmpty()) continue;
                if(e.getKey().equals("OBAT")) bmhpSaja=false; else obatSaja=false;
            }
            param.put("judul","RENCANA KEBUTUHAN "+(obatSaja?"OBAT":(bmhpSaja?"BMHP":"OBAT DAN BMHP"))+" GUDANG FARMASI");
            param.put("periode",Periode.getText().toUpperCase(Locale.ROOT));
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs());
            param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
            param.put("kota_tanggal",Kota.getText()+", "+surat.SuratBaubau.tglIndo(tgl(TglTtd).toString()));
            param.put("kepala_nama",NmKepala.getText());param.put("kepala_nip",NipKepala.getText());
            param.put("pembuat_nama",NmPembuat.getText());param.put("pembuat_nip",NipPembuat.getText());
            param.put("direktur_nama",NmDirektur.getText());param.put("direktur_nip",NipDirektur.getText());
            param.put("kabid_nama",NmKabid.getText());param.put("kabid_nip",NipKabid.getText());
            param.put("catatan",Catatan.getText());
            String namafile="./report/rptRencanaKebutuhanObat.jasper";
            if(!new File(namafile).exists()){
                net.sf.jasperreports.engine.JasperCompileManager.compileReportToFile(namafile.replace(".jasper",".jrxml"),namafile);
            }
            net.sf.jasperreports.engine.JasperPrint jp=net.sf.jasperreports.engine.JasperFillManager.fillReport(namafile,param,
                    new net.sf.jasperreports.engine.data.JRMapCollectionDataSource(barisCetak()));
            net.sf.jasperreports.view.JasperViewer v=new net.sf.jasperreports.view.JasperViewer(jp,false);
            v.setTitle("::[ Rencana Kebutuhan Obat & BMHP ]::");
            Dimension layar=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            v.setSize(layar.width-50,layar.height-50);
            v.setModalExclusionType(java.awt.Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
            v.setLocationRelativeTo(null);
            v.setVisible(true);
        } catch (Exception e) {
            System.out.println("Notif Cetak Rencana Kebutuhan : "+e);
            JOptionPane.showMessageDialog(null,"Gagal mencetak : "+e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void exportExcel(){
        hentikanEdit();
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Tidak ada data yang bisa diexport...!!!");
            return;
        }
        if(!bacaParameter()) return;
        JFileChooser fc=new JFileChooser();
        fc.setSelectedFile(new File("Rencana Kebutuhan Obat BMHP "+Periode.getText().replace("/","-")+".xls"));
        if(fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        File f=fc.getSelectedFile();
        if(!f.getName().toLowerCase(Locale.ROOT).endsWith(".xls")) f=new File(f.getPath()+".xls");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            RencanaKebutuhanObatExcel.tulis(f,"RENCANA KEBUTUHAN OBAT DAN BMHP GUDANG FARMASI",Periode.getText().toUpperCase(Locale.ROOT),
                    paramJudul(),data,barisCetak());
            JOptionPane.showMessageDialog(null,"Export selesai : "+f.getPath());
        } catch (Exception e) {
            System.out.println("Notif Excel Rencana Kebutuhan : "+e);
            JOptionPane.showMessageDialog(null,"Gagal export : "+e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgRencanaKebutuhanObat dialog=new DlgRencanaKebutuhanObat(new javax.swing.JFrame(),true);
            dialog.addWindowListener(new WindowAdapter(){
                @Override public void windowClosing(WindowEvent e){System.exit(0);}
            });
            dialog.setVisible(true);
        });
    }
}
