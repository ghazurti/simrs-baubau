package bridging;

import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Pasangan permintaan radiologi Khanza <-> worklist RIS alat_rs, satu baris per
 * pemeriksaan (tabel bridging_alatrs_worklist). Accession dibuat oleh RIS/PACS
 * (mis. DX-20261022-0021); Khanza mencatat accession itu milik noorder + kd_jenis_prw mana,
 * sehingga pasien dengan >1 permintaan bisa ditarik hasil/gambarnya tanpa menebak.
 *
 * Kalau tabel baru belum dibuat / data lama belum dipindah, semua pembacaan jatuh ke
 * kolom CSV di bridging_alatrs_log seperti sebelumnya.
 */
public final class AlatRSWorklist {
    private AlatRSWorklist(){}

    public static class Item {
        public String noorder="",kdJenisPrw="",namaPemeriksaan="",accession="",worklistId="",statusHasil="";
    }

    /** catat 1 pemeriksaan yang berhasil dikirim; kirim ulang item yang sama menggantikan accession lamanya */
    public static void simpan(String noorder,String kdJenisPrw,String accession,String worklistId,String aeTitle){
        if(noorder==null||noorder.isEmpty()||accession==null||accession.trim().isEmpty()) return;
        try {
            Connection kon=koneksiDB.condb();
            try (PreparedStatement ps=kon.prepareStatement(
                    "delete from bridging_alatrs_worklist where noorder=? and kd_jenis_prw=? and accession_number<>?")) {
                ps.setString(1,noorder);
                ps.setString(2,kdJenisPrw);
                ps.setString(3,accession.trim());
                ps.executeUpdate();
            }
            try (PreparedStatement ps=kon.prepareStatement(
                    "insert into bridging_alatrs_worklist (noorder,kd_jenis_prw,accession_number,worklist_id_alatrs,ae_title) values (?,?,?,?,?) "+
                    "on duplicate key update noorder=values(noorder),kd_jenis_prw=values(kd_jenis_prw),"+
                    "worklist_id_alatrs=values(worklist_id_alatrs),ae_title=values(ae_title)")) {
                ps.setString(1,noorder);
                ps.setString(2,kdJenisPrw);
                ps.setString(3,accession.trim());
                ps.setString(4,worklistId==null?"":worklistId.trim());
                ps.setString(5,aeTitle==null?"":aeTitle.trim());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("Gagal simpan bridging_alatrs_worklist : "+e);
        }
    }

    /**
     * Permintaan milik 1 baris pemeriksaan (periksa_radiologi no_rawat+tgl+jam):
     *  1. permintaan yang hasilnya dicatat di baris ini (tgl_hasil/jam_hasil = baris), ditambah
     *  2. permintaan No.Rawat yang sama yang tidak punya baris pemeriksaan sendiri;
     *  3. kalau tidak ada yang cocok sama sekali, semua permintaan No.Rawat itu.
     */
    public static List<String> orderUntukPeriksa(String noRawat,String tglPeriksa,String jam){
        LinkedHashSet<String> cocok=new LinkedHashSet<>(),lepas=new LinkedHashSet<>(),semua=new LinkedHashSet<>();
        try (PreparedStatement ps=koneksiDB.condb().prepareStatement(
                "select p.noorder,p.tgl_hasil,p.jam_hasil,"+
                "exists(select 1 from periksa_radiologi pr where pr.no_rawat=p.no_rawat and pr.tgl_periksa=p.tgl_hasil and pr.jam=p.jam_hasil) as punya_baris "+
                "from permintaan_radiologi p where p.no_rawat=? order by p.tgl_permintaan,p.jam_permintaan")) {
            ps.setString(1,noRawat);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()){
                    String no=rs.getString("noorder");
                    semua.add(no);
                    if(String.valueOf(rs.getString("tgl_hasil")).equals(tglPeriksa)&&String.valueOf(rs.getString("jam_hasil")).equals(jam)){
                        cocok.add(no);
                    }else if(!rs.getBoolean("punya_baris")){
                        lepas.add(no);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Cari permintaan radiologi gagal: "+e);
        }
        LinkedHashSet<String> pakai=new LinkedHashSet<>(cocok);
        pakai.addAll(lepas);
        return new ArrayList<>(pakai.isEmpty()?semua:pakai);
    }

    /** semua pemeriksaan terkirim untuk daftar permintaan, urut sesuai daftar */
    public static List<Item> untukOrder(Collection<String> noorders){
        List<Item> hasil=new ArrayList<>();
        for(String no:noorders){
            List<Item> baru=dariTabelBaru(no);
            hasil.addAll(baru.isEmpty()?dariLogLama(no):baru);
        }
        return hasil;
    }

    /** semua pemeriksaan terkirim milik 1 pasien (semua kunjungan), terbaru dulu */
    public static List<String> orderPasien(String noRkmMedis){
        List<String> hasil=new ArrayList<>();
        try (PreparedStatement ps=koneksiDB.condb().prepareStatement(
                "select p.noorder from permintaan_radiologi p inner join reg_periksa r on r.no_rawat=p.no_rawat "+
                "where r.no_rkm_medis=? order by p.tgl_permintaan desc,p.jam_permintaan desc")) {
            ps.setString(1,noRkmMedis);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()) hasil.add(rs.getString(1));
            }
        } catch (Exception e) {
            System.out.println("Cari permintaan pasien gagal: "+e);
        }
        return hasil;
    }

    private static List<Item> dariTabelBaru(String noorder){
        List<Item> hasil=new ArrayList<>();
        try (PreparedStatement ps=koneksiDB.condb().prepareStatement(
                "select w.noorder,ifnull(w.kd_jenis_prw,'') as kd_jenis_prw,ifnull(j.nm_perawatan,'') as nama,w.accession_number,"+
                "w.worklist_id_alatrs,w.status_hasil from bridging_alatrs_worklist w "+
                "left join jns_perawatan_radiologi j on j.kd_jenis_prw=w.kd_jenis_prw "+
                "where w.noorder=? order by w.tgl_kirim,w.accession_number")) {
            ps.setString(1,noorder);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()){
                    Item it=new Item();
                    it.noorder=rs.getString("noorder");
                    it.kdJenisPrw=rs.getString("kd_jenis_prw");
                    it.namaPemeriksaan=rs.getString("nama").trim();
                    it.accession=rs.getString("accession_number");
                    it.worklistId=rs.getString("worklist_id_alatrs");
                    it.statusHasil=rs.getString("status_hasil");
                    hasil.add(it);
                }
            }
        } catch (Exception e) {
            // tabel belum dibuat -> pakai log lama
        }
        return hasil;
    }

    /** cadangan: CSV accession/worklist di bridging_alatrs_log, dipasangkan per urutan */
    private static List<Item> dariLogLama(String noorder){
        List<Item> hasil=new ArrayList<>();
        try {
            Connection kon=koneksiDB.condb();
            String acc="",wid="";
            try (PreparedStatement ps=kon.prepareStatement(
                    "select ifnull(accession_number,''),ifnull(worklist_id_alatrs,'') from bridging_alatrs_log where noorder=?")) {
                ps.setString(1,noorder);
                try (ResultSet rs=ps.executeQuery()) {
                    if(rs.next()){ acc=rs.getString(1); wid=rs.getString(2); }
                }
            }
            if(acc.trim().isEmpty()&&wid.trim().isEmpty()) return hasil;
            List<String[]> periksa=new ArrayList<>();
            try (PreparedStatement ps=kon.prepareStatement(
                    "select pp.kd_jenis_prw,j.nm_perawatan from permintaan_pemeriksaan_radiologi pp "+
                    "inner join jns_perawatan_radiologi j on j.kd_jenis_prw=pp.kd_jenis_prw where pp.noorder=? order by pp.kd_jenis_prw")) {
                ps.setString(1,noorder);
                try (ResultSet rs=ps.executeQuery()) {
                    while(rs.next()) periksa.add(new String[]{rs.getString(1),rs.getString(2).trim()});
                }
            }
            String[] a=acc.split(","),w=wid.split(",");
            int n=Math.max(a.length,w.length);
            for(int i=0;i<n;i++){
                Item it=new Item();
                it.noorder=noorder;
                it.accession=i<a.length?a[i].trim():"";
                it.worklistId=i<w.length?w[i].trim():"";
                if(it.accession.isEmpty()&&it.worklistId.isEmpty()) continue;
                if(i<periksa.size()&&n==periksa.size()){
                    it.kdJenisPrw=periksa.get(i)[0];
                    it.namaPemeriksaan=periksa.get(i)[1];
                }else if(periksa.size()==1){
                    it.kdJenisPrw=periksa.get(0)[0];
                    it.namaPemeriksaan=periksa.get(0)[1];
                }
                hasil.add(it);
            }
        } catch (Exception e) {
            System.out.println("Baca bridging_alatrs_log gagal: "+e);
        }
        return hasil;
    }

    /** worklist id dipisah koma (untuk ApiAlatRS) */
    public static String worklistCsv(List<Item> items){
        LinkedHashSet<String> s=new LinkedHashSet<>();
        for(Item it:items) if(!it.worklistId.isEmpty()) s.add(it.worklistId);
        return String.join(",",s);
    }

    /** worklist id -> judul pemeriksaan versi Khanza (lebih tepat dari nama di RIS) */
    public static LinkedHashMap<String,String> judulPerWorklist(List<Item> items){
        LinkedHashMap<String,String> m=new LinkedHashMap<>();
        for(Item it:items) if(!it.worklistId.isEmpty()) m.put(it.worklistId,it.namaPemeriksaan);
        return m;
    }

    /** catat status hasil per worklist setelah ditarik dari RIS */
    public static void tandaiHasil(List<ApiAlatRS.HasilBacaan> rincian){
        if(rincian==null) return;
        Map<String,ApiAlatRS.HasilBacaan> perId=new HashMap<>();
        for(ApiAlatRS.HasilBacaan hb:rincian) if(hb.worklistId!=null&&!hb.worklistId.isEmpty()) perId.put(hb.worklistId,hb);
        for(Map.Entry<String,ApiAlatRS.HasilBacaan> e:perId.entrySet()){
            ApiAlatRS.HasilBacaan hb=e.getValue();
            if(!hb.ok) continue;
            try (PreparedStatement ps=koneksiDB.condb().prepareStatement(
                    "update bridging_alatrs_worklist set status_hasil=?,hasil_status_ris=?,hasil_issued_at=?,tgl_tarik_hasil=now() where worklist_id_alatrs=?")) {
                ps.setString(1,hb.ada?"Sudah":"Belum");
                ps.setString(2,hb.status==null?"":hb.status);
                ps.setString(3,hb.issuedAt==null?"":hb.issuedAt);
                ps.setString(4,e.getKey());
                ps.executeUpdate();
            } catch (Exception ex) {
                // tabel belum dibuat
            }
        }
    }
}
