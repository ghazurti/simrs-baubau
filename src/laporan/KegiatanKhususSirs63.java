/*
 * Daftar jenis kegiatan Formulir RL 3.14 Rekapitulasi Kegiatan Pelayanan Khusus
 * sesuai Juknis SIRS Revisi 6.3 (2025), BAB IV huruf N.
 *
 * Kegiatan dipetakan dari nama tindakan (jns_perawatan.nm_perawatan) yang tercatat di
 * tabel pemakaian tindakan (rawat_jl_dr/pr/drpr, rawat_inap_dr/pr/drpr).
 *
 * Hanya tindakan yang teridentifikasi sebagai pelayanan khusus yang dihitung; tindakan
 * umum (konsultasi, rawat luka, dll) TIDAK dihitung. Baris "Lain-Lain" (18) dan baris
 * yang tidak ada tindakannya di Khanza dibiarkan 0 dan dilengkapi manual di SIRS Online v3.
 */
package laporan;

public class KegiatanKhususSirs63 {

    public static class Baris {
        public final String no;
        public final String nama;
        public final String jumlahDari; // "@"=TOTAL semua; prefix="16."; null=baris tindakan biasa
        public final boolean judul;     // baris kelompok tanpa angka sendiri (mis. "16")
        Baris(String no,String nama,String jumlahDari,boolean judul){
            this.no=no; this.nama=nama; this.jumlahDari=jumlahDari; this.judul=judul;
        }
    }
    private static Baris row(String no,String nama){ return new Baris(no,nama,null,false); }

    public static final Baris[] BARIS = {
        row("1","Elektro Kardiographi (EKG)"),
        row("2","Elektro Myographi (EMG)"),
        row("3","Echo Cardiographi (ECG)"),
        row("4","Endoskopi (semua bentuk)"),
        row("5","Hemodialisa"),
        row("6","Densometri Tulang"),
        row("7","Pungsi"),
        row("8","Spirometri"),
        row("9","Tes Kulit/Alergi/Histamin"),
        row("10","Topometri"),
        row("11","Akupunktur Medik"),
        row("12","Akupunktur Tradisional"),
        row("13","Akupressur"),
        row("14","Herbal/Jamu"),
        row("15","Pijat Baduta"),
        new Baris("16","Kunjungan Rumah (Homecare)",null,true),
        row("16.1","Kunjungan Rumah (Homecare) Non Lansia"),
        row("16.2","Rehabilitasi Medis Lansia"),
        row("16.3","Pemeriksaan Medis Umum dan Spesialis Bagi Lansia"),
        row("16.4","Asuhan dan/atau Tindakan Keperawatan Bagi Lansia"),
        row("16.5","Kunjungan Rumah (Homecare) Lansia Lainnya"),
        row("17","Tindak lanjut lesi pra Kanker Leher Rahim"),
        row("18","Lain-Lain"),
        new Baris("99","TOTAL","@",false),
    };

    private static boolean ada(String s,String... kata){
        for(String x:kata){
            if(s.contains(x)){
                return true;
            }
        }
        return false;
    }

    /**
     * Memetakan nama tindakan ke nomor baris RL 3.14, atau null bila bukan pelayanan khusus.
     * @param nm nama tindakan (jns_perawatan.nm_perawatan)
     */
    public static String kategori(String nm){
        String s=nm==null?"":nm.toUpperCase().trim();

        // Echo cardiography diperiksa sebelum EKG/ECG karena juknis menuliskan
        // Echo Cardiographi dengan singkatan "ECG" (baris 3), sedangkan EKG = baris 1.
        if(ada(s,"ECHOCARDIO","ECHO CARDIO","ECHOKARDIO","ECHO JANTUNG")){ return "3"; }
        if(ada(s,"EMG","MYOGRAF","MIOGRAF","ELEKTROMYO")){ return "2"; }
        if(ada(s,"EKG","ELEKTROKARDIO","ELECTROCARDIO")||s.equals("ECG")||ada(s,"ECG)","(ECG")){ return "1"; }

        if(ada(s,"ENDOSKOP","ENDOSCOP","CYSTOSCOP","SISTOSKOP","BRONKOSKOP","KOLONOSKOP","GASTROSKOP","LARINGOSKOP")){ return "4"; }
        if(ada(s,"DIALIS","HEMODIALIS","HD ","CAPD")){ return "5"; }
        if(ada(s,"DENSITO","DENSOMETRI","BMD","BONE DENSIT")){ return "6"; }
        if(ada(s,"PUNGSI","PUNKSI","LUMBAL PUNCTIE","PUNCTIE")){ return "7"; }
        if(ada(s,"SPIROMETR")){ return "8"; }
        if(ada(s,"TES KULIT","SKIN TEST","TES ALERGI","HISTAMIN","PATCH TEST","PRICK TEST")){ return "9"; }
        if(ada(s,"TOPOMETRI")){ return "10"; }
        if(ada(s,"AKUPUNKTUR MEDIK","AKUPUNTUR MEDIK")){ return "11"; }
        if(ada(s,"AKUPUNKTUR TRADISIONAL","AKUPUNTUR TRAD")){ return "12"; }
        if(ada(s,"AKUPUNK","AKUPUNT")){ return "11"; } // akupunktur tanpa keterangan -> medik
        if(ada(s,"AKUPRES","AKUPRESUR")){ return "13"; }
        if(ada(s,"HERBAL","JAMU")){ return "14"; }
        if(ada(s,"PIJAT BADUTA","PIJAT BAYI")){ return "15"; }

        if(ada(s,"HOMECARE","HOME CARE","KUNJUNGAN RUMAH")){
            if(ada(s,"LANSIA")){
                if(ada(s,"REHAB")){ return "16.2"; }
                if(ada(s,"SPESIALIS")||ada(s,"MEDIS UMUM")){ return "16.3"; }
                if(ada(s,"KEPERAWATAN")||ada(s,"ASUHAN")){ return "16.4"; }
                return "16.5";
            }
            return "16.1";
        }
        if(ada(s,"IVA","KRIOTERAPI","LESI PRA KANKER","PRAKANKER","THERMAL ABLASI","THERMOKOAGULASI")){ return "17"; }

        return null;   // bukan pelayanan khusus
    }
}
