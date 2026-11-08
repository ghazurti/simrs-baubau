package inventory;

import fungsi.koneksiDB;
import fungsi.sekuel;

/**
 * Depo asal resep, dicatat saat resep disimpan (tabel resep_obat_depo).
 * Dasar ruangan & depo resep ranap di Daftar Permintaan Resep, bukan lagi jadwal operasi:
 *   - dibuat dari Jadwal Operasi -> Obat/BHP : kd_ruang_ok + depo ruang OK (set_depo_ruang_ok)
 *   - dibuat dari komputer ber-DEPOAKTIFOBAT lain dari depo ruangan pasien : depo komputer itu
 *   - selain itu tidak dicatat = depo ruangan pasien (set_depo_ranap)
 */
public final class ResepDepo {
    private static boolean siap=false;

    private ResepDepo() {}

    /** join untuk query daftar resep (alias rdp, rok, bdp) */
    public static final String JOIN=
            " left join resep_obat_depo rdp on rdp.no_resep=resep_obat.no_resep "+
            " left join ruang_ok rok on rok.kd_ruang_ok=rdp.kd_ruang_ok "+
            " left join bangsal bdp on bdp.kd_bangsal=rdp.kd_depo ";

    /** nama ruangan resep: ruang OK, atau "Ruangan (Depo X)", atau ruangan rawat */
    public static final String RUANGAN=
            "(case when rok.nm_ruang_ok is not null then rok.nm_ruang_ok "+
            " when bdp.nm_bangsal is not null then concat(bangsal.nm_bangsal,' (',bdp.nm_bangsal,')') "+
            " else bangsal.nm_bangsal end)";

    /** filter daftar resep untuk komputer ber-DEPOAKTIFOBAT (butuh join set_depo_ranap) */
    public static String filterDepo(String depo){
        return " (rdp.kd_depo='"+depo+"' or (rdp.no_resep is null and set_depo_ranap.kd_depo='"+depo+"')) ";
    }

    /** dipanggil di konstruktor form, JANGAN di dalam transaksi (create table = implicit commit) */
    public static void siapkanTabel(){
        if(siap) return;
        try (java.sql.Statement st=koneksiDB.condb().createStatement()) {
            st.executeUpdate("create table if not exists resep_obat_depo (no_resep varchar(14) not null,"+
                    "kd_depo char(5) not null,kd_ruang_ok varchar(3) not null default '',primary key (no_resep)) "+
                    "engine=InnoDB default charset=latin1");
            siap=true;
        } catch (Exception e) {
            System.out.println("Notif tabel resep_obat_depo : "+e);
        }
    }

    public static String depoRuangOK(String kdRuangOK){
        if(kdRuangOK==null||kdRuangOK.isEmpty()) return "";
        try{
            return new sekuel().cariIsi("select kd_depo from set_depo_ruang_ok where kd_ruang_ok=?",kdRuangOK);
        }catch(Exception e){
            return "";
        }
    }

    /**
     * Depo komputer yang khusus kamar operasi (mis. Depo OK): ada di set_depo_ruang_ok dan tidak
     * dipakai poli/ruangan. Depo bersama seperti Depo UGD (IGD + Cathlab) tidak dihitung, supaya
     * resep pasien ranap dari komputer IGD tidak pindah ke Depo UGD; Cathlab lewat Jadwal Operasi.
     */
    public static boolean depoKhususOK(String depo){
        if(depo==null||depo.isEmpty()) return false;
        try{
            return new sekuel().cariInteger("select count(*) from set_depo_ruang_ok where kd_depo=?",depo)>0
                &&new sekuel().cariInteger("select count(*) from set_depo_ralan where kd_bangsal=?",depo)==0
                &&new sekuel().cariInteger("select count(*) from set_depo_ranap where kd_depo=?",depo)==0;
        }catch(Exception e){
            return false;
        }
    }

    /** depo bawaan ruangan rawat pasien saat ini (set_depo_ranap) */
    public static String depoRuangan(String norawat){
        return new sekuel().cariIsi("select set_depo_ranap.kd_depo from kamar_inap inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "+
                "inner join set_depo_ranap on set_depo_ranap.kd_bangsal=kamar.kd_bangsal "+
                "where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc,kamar_inap.jam_masuk desc limit 1",norawat);
    }

    /** depo yang tercatat untuk resep ini, "" kalau tidak tercatat (= depo ruangan) */
    public static String depoResep(String noResep){
        try{
            return new sekuel().cariIsi("select kd_depo from resep_obat_depo where no_resep=?",noResep);
        }catch(Exception e){
            return "";
        }
    }

    public static void catat(String noResep,String kdDepo,String kdRuangOK){
        new sekuel().menyimpan2("resep_obat_depo","?,?,?",3,new String[]{noResep,kdDepo,kdRuangOK==null?"":kdRuangOK});
    }

    /** no_resep bisa dipakai ulang setelah resep dihapus: buang catatan lama sebelum resep baru dicatat */
    public static void hapus(String noResep){
        try (java.sql.PreparedStatement ps=koneksiDB.condb().prepareStatement("delete from resep_obat_depo where no_resep=?")) {
            ps.setString(1,noResep);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notif resep_obat_depo : "+e);
        }
    }
}
