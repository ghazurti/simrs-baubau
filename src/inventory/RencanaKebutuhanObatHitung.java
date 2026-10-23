package inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Perhitungan Rencana Kebutuhan Obat & BMHP Gudang Farmasi
 * (struktur kolom sama dengan Excel konfirmasi obat & BMHP Instalasi Farmasi).
 *
 * G = CEIL(pemakaian / jml bulan), H = G x bulan stok kerja, I = H x %buffer,
 * J = lead time hari x G/30, K = H+I+J-F, L = CEILING(K, kelipatan), N = L x M,
 * O/P/Q = analisis ABC per kelompok berdasar N terbesar.
 */
public class RencanaKebutuhanObatHitung {
    public static final String SUMBER_TRANSAKSI="Transaksi";
    public static final String SUMBER_RIWAYAT="Riwayat Barang";
    public static final String HARGA_MASTER="Master";
    public static final String HARGA_PENERIMAAN="Penerimaan Terakhir";
    /** urutan section pada tabel & cetakan */
    public static final List<String> KELOMPOK=Arrays.asList(
        "OBAT","BMHP","BMHP BEDAH SYARAF","BMHP MATA","UROLOGI","HEMODIALISA","CATHLAB","ORTHOPEDI","BMHP GIGI");

    /** satu baris item */
    public static class Baris {
        public String kode,generik,nama,satuan,kelompok,ven="-",ket="",pareto="";
        public double pemakaian,f,g,h,i,j,k,l,m,n,o,p,kelipatan=1;
    }

    // parameter
    public LocalDate tglAwal,tglAkhir;
    public String kdBangsalStok;          // null = semua depo
    public double bulanStokKerja=4,persenBuffer=20;
    public int leadTimeHari=60;
    public String sumber=SUMBER_TRANSAKSI,harga=HARGA_MASTER;
    public boolean ikutPengeluaranBhp=false,tampilSemua=false;

    private final Connection koneksi;

    public RencanaKebutuhanObatHitung(Connection koneksi){
        this.koneksi=koneksi;
    }

    /** jumlah bulan inklusif, Jan 2025 - Sep 2026 = 21 */
    public static int jumlahBulan(LocalDate awal,LocalDate akhir){
        return (akhir.getYear()*12+akhir.getMonthValue())-(awal.getYear()*12+awal.getMonthValue())+1;
    }

    public int jumlahBulan(){
        return Math.max(1,jumlahBulan(tglAwal,tglAkhir));
    }

    /** kelompok otomatis dari golongan / kategori / jenis barang */
    public static String kelompokOtomatis(String golongan,String kategori,String jenis){
        String g=golongan==null?"":golongan.toUpperCase(Locale.ROOT);
        if(g.contains("SYARAF")||g.contains("SARAF")) return "BMHP BEDAH SYARAF";
        if(g.contains("MATA")) return "BMHP MATA";
        if(g.contains("UROLOGI")) return "UROLOGI";
        if(g.contains("HEMODIALISA")) return "HEMODIALISA";
        if(g.contains("CATHLAB")) return "CATHLAB";
        if(g.contains("ORTHOPEDI")||g.contains("ORTOPEDI")) return "ORTHOPEDI";
        if(g.contains("GIGI")) return "BMHP GIGI";
        String semua=(g+" "+kategori+" "+jenis).toUpperCase(Locale.ROOT);
        return semua.contains("BMHP")?"BMHP":"OBAT";
    }

    /** CEILING(K, kelipatan); K<=0 -> 0 */
    public static double pengadaan(double k,double kelipatan){
        if(k<=0) return 0;
        double kel=kelipatan<=0?1:kelipatan;
        // pembulatan 6 desimal agar 400.0000000001 tidak naik satu kelipatan
        double q=Math.round(k/kel*1000000d)/1000000d;
        return Math.ceil(q)*kel;
    }

    /** hitung ulang H..N dari F, G, kelipatan, harga */
    public void hitungBaris(Baris b,boolean pertahankanL){
        b.h=b.g*bulanStokKerja;
        b.i=b.h*persenBuffer/100d;
        b.j=leadTimeHari*(b.g/30d);
        b.k=b.h+b.i+b.j-b.f;
        if(!pertahankanL){
            b.l=pengadaan(b.k,b.kelipatan);
        }
        b.n=b.l*b.m;
    }

    /** analisis ABC per kelompok: urut N terbesar, kumulatif <=70 A, <=90 B, sisanya C */
    public static void hitungPareto(List<Baris> kelompok){
        double total=0;
        for(Baris b:kelompok){
            total+=b.n;
        }
        List<Baris> urut=new ArrayList<>(kelompok);
        urut.sort(Comparator.comparingDouble((Baris b)->b.n).reversed().thenComparing(b->b.generik));
        double kum=0;
        for(Baris b:urut){
            b.o=total==0?0:b.n/total;
            kum+=b.o;
            b.p=kum;
            if(total<=0){
                b.pareto="";
            }else{
                double pp=Math.round(b.p*1000000d)/1000000d;
                b.pareto=pp<=0.70?"A":(pp<=0.90?"B":"C");
            }
        }
    }

    /** hasil per kelompok sesuai urutan KELOMPOK, kelompok di luar daftar ditaruh di belakang */
    public Map<String,List<Baris>> hitung(String filterKelompok,String cari) throws Exception {
        Map<String,Double> pakai=pemakaian();
        Map<String,Double> stok=stok();
        Map<String,Double> hrgTerima=harga.equals(HARGA_PENERIMAAN)?hargaPenerimaanTerakhir():new HashMap<>();
        int bulan=jumlahBulan();

        Map<String,List<Baris>> hasil=new LinkedHashMap<>();
        for(String k:KELOMPOK){
            hasil.put(k,new ArrayList<>());
        }
        String kata=cari==null?"":cari.trim().toLowerCase(Locale.ROOT);
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select databarang.kode_brng,databarang.nama_brng,kodesatuan.satuan,databarang.h_beli,"+
                "ifnull(golongan_barang.nama,'') as golongan,ifnull(kategori_barang.nama,'') as kategori,ifnull(jenis.nama,'') as jenis,"+
                "s.nama_generik,s.kelompok,s.kelipatan,ifnull(s.ven,'-') as ven,ifnull(s.keterangan,'') as keterangan,ifnull(s.aktif,'Ya') as aktif,"+
                "ks.kelipatan as kelipatan_satuan "+
                "from databarang inner join kodesatuan on databarang.kode_sat=kodesatuan.kode_sat "+
                "left join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                "left join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                "left join jenis on databarang.kdjns=jenis.kdjns "+
                "left join rencana_kebutuhan_obat_setting s on databarang.kode_brng=s.kode_brng "+
                "left join rencana_kebutuhan_obat_kelipatan_satuan ks on databarang.kode_sat=ks.kode_sat "+
                "where databarang.status='1'");
             ResultSet rs=ps.executeQuery()) {
            while(rs.next()){
                if(rs.getString("aktif").equals("Tidak")){
                    continue;
                }
                Baris b=new Baris();
                b.kode=rs.getString("kode_brng");
                b.nama=rs.getString("nama_brng").trim();
                String gen=rs.getString("nama_generik");
                b.generik=(gen==null||gen.trim().equals(""))?b.nama:gen.trim();
                b.satuan=rs.getString("satuan");
                String kel=rs.getString("kelompok");
                b.kelompok=(kel==null||kel.trim().equals(""))?kelompokOtomatis(rs.getString("golongan"),rs.getString("kategori"),rs.getString("jenis")):kel.trim().toUpperCase(Locale.ROOT);
                if(filterKelompok!=null&&!filterKelompok.equals("")&&!filterKelompok.equals("Semua")&&!b.kelompok.equals(filterKelompok)){
                    continue;
                }
                if(!kata.equals("")&&!(b.kode.toLowerCase(Locale.ROOT).contains(kata)||b.nama.toLowerCase(Locale.ROOT).contains(kata)||b.generik.toLowerCase(Locale.ROOT).contains(kata))){
                    continue;
                }
                b.pemakaian=pakai.getOrDefault(b.kode,0d);
                b.f=stok.getOrDefault(b.kode,0d);
                if(!tampilSemua&&b.pemakaian<=0&&b.f<=0){
                    continue;
                }
                double kelItem=rs.getDouble("kelipatan");
                boolean kelItemAda=!rs.wasNull()&&kelItem>0;
                double kelSat=rs.getDouble("kelipatan_satuan");
                boolean kelSatAda=!rs.wasNull()&&kelSat>0;
                b.kelipatan=kelItemAda?kelItem:(kelSatAda?kelSat:1);
                b.ven=rs.getString("ven");
                b.ket=rs.getString("keterangan");
                b.m=hrgTerima.getOrDefault(b.kode,rs.getDouble("h_beli"));
                b.g=Math.ceil(Math.round(Math.max(0,b.pemakaian)/bulan*1000000d)/1000000d);
                hitungBaris(b,false);
                hasil.computeIfAbsent(b.kelompok,x->new ArrayList<>()).add(b);
            }
        }
        for(List<Baris> daftar:hasil.values()){
            daftar.sort(Comparator.comparing((Baris b)->b.generik.toUpperCase(Locale.ROOT)).thenComparing(b->b.kode));
            hitungPareto(daftar);
        }
        return hasil;
    }

    /** pemakaian per item: 1 query GROUP BY kode_brng */
    public Map<String,Double> pemakaian() throws Exception {
        String sql;
        int jmlParam;
        if(sumber.equals(SUMBER_RIWAYAT)){
            String konsumsi="'Pemberian Obat','Penjualan','Resep Pulang','Stok Pasien Ranap','Piutang'"+(ikutPengeluaranBhp?",'Stok Keluar'":"");
            sql="select kode_brng,sum(case when posisi in ("+konsumsi+") then keluar-masuk "+
                "when posisi in ('Retur Jual','Retur Pasien','Retur Piutang') then keluar-masuk else 0 end) as jml "+
                "from riwayat_barang_medis where tanggal between ? and ? group by kode_brng";
            jmlParam=1;
        }else{
            sql="select kode_brng,sum(jml) as jml from ("+
                "select kode_brng,sum(jml) as jml from detail_pemberian_obat where tgl_perawatan between ? and ? group by kode_brng "+
                "union all select detailjual.kode_brng,sum(detailjual.jumlah) from detailjual inner join penjualan on detailjual.nota_jual=penjualan.nota_jual "+
                "  where penjualan.status='Sudah Dibayar' and penjualan.tgl_jual between ? and ? group by detailjual.kode_brng "+
                "union all select detreturjual.kode_brng,-sum(detreturjual.jml_retur) from detreturjual inner join returjual on detreturjual.no_retur_jual=returjual.no_retur_jual "+
                "  where returjual.tgl_retur between ? and ? group by detreturjual.kode_brng "+
                "union all select kode_brng,sum(jml_barang) from resep_pulang where tanggal between ? and ? group by kode_brng "+
                "union all select kode_brng,-sum(jml) from returpasien where tanggal between ? and ? group by kode_brng "+
                (ikutPengeluaranBhp?"union all select detail_pengeluaran_obat_bhp.kode_brng,sum(detail_pengeluaran_obat_bhp.jumlah) from detail_pengeluaran_obat_bhp "+
                "  inner join pengeluaran_obat_bhp on detail_pengeluaran_obat_bhp.no_keluar=pengeluaran_obat_bhp.no_keluar "+
                "  where pengeluaran_obat_bhp.tanggal between ? and ? group by detail_pengeluaran_obat_bhp.kode_brng ":"")+
                ") x group by kode_brng";
            jmlParam=ikutPengeluaranBhp?6:5;
        }
        Map<String,Double> hasil=new HashMap<>();
        try (PreparedStatement ps=koneksi.prepareStatement(sql)) {
            for(int x=0;x<jmlParam;x++){
                ps.setString(x*2+1,tglAwal.toString());
                ps.setString(x*2+2,tglAkhir.toString());
            }
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()){
                    hasil.put(rs.getString(1),rs.getDouble(2));
                }
            }
        }
        return hasil;
    }

    /** rincian pemakaian per sumber untuk satu item (bahan pengecekan selisih) */
    public Map<String,Double> rincianPemakaian(String kode) throws Exception {
        Map<String,Double> hasil=new LinkedHashMap<>();
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select posisi,sum(keluar-masuk) from riwayat_barang_medis where kode_brng=? and tanggal between ? and ? group by posisi order by posisi")) {
            ps.setString(1,kode);
            ps.setString(2,tglAwal.toString());
            ps.setString(3,tglAkhir.toString());
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()){
                    hasil.put(rs.getString(1),rs.getDouble(2));
                }
            }
        }
        return hasil;
    }

    /** sisa stok saat ini per item */
    public Map<String,Double> stok() throws Exception {
        Map<String,Double> hasil=new HashMap<>();
        boolean semua=kdBangsalStok==null||kdBangsalStok.trim().equals("");
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select kode_brng,sum(stok) from gudangbarang "+(semua?"":"where kd_bangsal=? ")+"group by kode_brng")) {
            if(!semua){
                ps.setString(1,kdBangsalStok);
            }
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()){
                    hasil.put(rs.getString(1),rs.getDouble(2));
                }
            }
        }
        return hasil;
    }

    /** harga pemesanan/penerimaan terakhir (h_pesan pada faktur terbaru) */
    public Map<String,Double> hargaPenerimaanTerakhir() throws Exception {
        Map<String,Double> hasil=new HashMap<>();
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select detailpesan.kode_brng,detailpesan.h_pesan from detailpesan inner join pemesanan on detailpesan.no_faktur=pemesanan.no_faktur "+
                "inner join (select detailpesan.kode_brng,max(pemesanan.tgl_pesan) as tgl from detailpesan inner join pemesanan on detailpesan.no_faktur=pemesanan.no_faktur "+
                "  where detailpesan.h_pesan>0 group by detailpesan.kode_brng) t on t.kode_brng=detailpesan.kode_brng and t.tgl=pemesanan.tgl_pesan "+
                "where detailpesan.h_pesan>0");
             ResultSet rs=ps.executeQuery()) {
            while(rs.next()){
                hasil.put(rs.getString(1),rs.getDouble(2));
            }
        }
        return hasil;
    }
}
