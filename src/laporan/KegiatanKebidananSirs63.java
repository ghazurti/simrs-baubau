/*
 * Daftar jenis kegiatan Formulir RL 3.6 Rekapitulasi Kegiatan Pelayanan Kebidanan
 * sesuai Juknis SIRS Revisi 6.3 (2025), BAB IV huruf F.
 *
 * Setiap baris dipetakan ke kode ICD-10 diagnosa pasien semampu data Khanza.
 * Sebagian baris memang TIDAK ada sumber datanya di Khanza (tidak ada field-nya),
 * sehingga selalu 0 dan harus dilengkapi manual saat entri di SIRS Online versi 3.
 *
 * Catatan juknis butir 2 : formulir ini mencatat banyaknya KEGIATAN, bukan jumlah
 * pasien, sehingga satu pasien memang bisa terhitung di beberapa baris sekaligus.
 * Butir 27 : data kesakitan bisa dilaporkan berkali-kali, kematian hanya 1 kali.
 */
package laporan;

public class KegiatanKebidananSirs63 {

    /** Satu baris kegiatan pada formulir RL 3.6 */
    public static class Baris {
        public final String no;          // nomor sesuai juknis, mis. "3.1"
        public final String nama;        // label jenis kegiatan
        public final String[] kode;      // prefix ICD-10 penanda baris; null = tidak ada sumber data di Khanza
        public final String[] kecuali;   // pasien dikecualikan bila punya salah satu prefix ini
        public final String jumlahDari;  // bila diisi, baris = penjumlahan sub-baris berprefix ini (mis. "3.")

        Baris(String no,String nama,String[] kode,String[] kecuali,String jumlahDari){
            this.no=no; this.nama=nama; this.kode=kode; this.kecuali=kecuali; this.jumlahDari=jumlahDari;
        }
        /** true bila baris ini tidak punya sumber data sama sekali di Khanza */
        public boolean tanpaSumberData(){
            return kode==null && jumlahDari==null;
        }
    }

    private static String[] k(String... x){ return x; }

    /** 30 baris kegiatan, urut sesuai formulir juknis. */
    public static final Baris[] BARIS = {
        new Baris("1","Pemberian Buku KIA pada Ibu Hamil", null, null, null),
        new Baris("2","Pelayanan Antenatal", k("Z34","Z35","Z36"), null, null),

        new Baris("3","Persalinan:", null, null, "3."),
        new Baris("3.1","Persalinan pervaginam tanpa penyulit (normal)", k("O80"), null, null),
        // penyulit persalinan yang berakhir pervaginam spontan :
        // ada komplikasi O60-O75, tetapi bukan persalinan berbantuan (O81) / sectio (O82)
        new Baris("3.2","Persalinan pervaginam spontan dengan penyulit",
                k("O60","O61","O62","O63","O64","O65","O66","O67","O68","O69","O70","O71","O73","O74","O75"),
                k("O81","O82"), null),
        new Baris("3.3","Persalinan pervaginam dengan bantuan", k("O81","O83"), null, null),
        new Baris("3.4","Persalinan Sectio caesaria", k("O82"), null, null),

        new Baris("4","Komplikasi obstetri pada ibu hamil, bersalin, dan nifas", null, null, "4."),
        new Baris("4.1","Perdarahan sebelum persalinan", k("O20","O44","O45","O46"), null, null),
        new Baris("4.2","Perdarahan setelah persalinan", k("O67","O72"), null, null),
        new Baris("4.3","Pre eklamsia", k("O13","O14"), null, null),
        new Baris("4.4","Eklamsia", k("O15"), null, null),
        new Baris("4.5","Infeksi", k("O85","O86","O91"), null, null),
        new Baris("4.6","Abortus", k("O03","O05","O06"), null, null),
        new Baris("4.7","Komplikasi lainnya", k("O21","O22","O23","O26","O47","O48","O87","O88","O89","O90"), null, null),

        new Baris("5","Aborsi", null, null, "5."),
        new Baris("5.1","Aborsi atas indikasi kedaruratan medis", k("O04"), null, null),
        new Baris("5.2","Aborsi atas indikasi kehamilan akibat perkosaan", null, null, null),

        new Baris("6","Skrining Status Imunisasi Tetanus", null, null, null),

        new Baris("7","Komplikasi non obstetri pada ibu hamil, bersalin, dan nifas:", null, null, "7."),
        new Baris("7.1","HIV", k("O98.7"), null, null),
        new Baris("7.2","Hepatitis B", k("O98.4"), null, null),
        new Baris("7.3","Sifilis", k("O98.1"), null, null),
        new Baris("7.4","Tuberkulosis", k("O98.0"), null, null),
        new Baris("7.5","Penyakit jantung", k("O99.4"), null, null),
        new Baris("7.6","Anemia", k("O99.0"), null, null),
        new Baris("7.7","Diabetes Melitus", k("O24"), null, null),
        new Baris("7.8","Terkonfirmasi COVID-19", k("O98.5","U07.1"), null, null),
        new Baris("7.9","Komplikasi lainnya",
                k("O98.2","O98.3","O98.6","O98.8","O98.9","O99.1","O99.2","O99.3","O99.5","O99.6","O99.7","O99.8"), null, null),

        new Baris("8","Ibu Hamil berisiko mempunyai bayi prematur", k("O60"), null, null),
        new Baris("8.1","Diberikan antenatal kortikosteroid", null, null, null),
        new Baris("8.2","Tidak diberikan antenatal kortikosteroid", null, null, null),

        new Baris("9","Pelayanan Nifas", k("Z39"), null, null),
        new Baris("10","Ibu Nifas mendapat vitamin A", null, null, null),
    };

    /** true bila salah satu kode diagnosa pasien berawalan salah satu prefix */
    public static boolean cocok(java.util.Set<String> dxPasien, String[] prefix){
        if(prefix==null){
            return false;
        }
        for(String dx : dxPasien){
            for(String p : prefix){
                if(dx.startsWith(p)){
                    return true;
                }
            }
        }
        return false;
    }

    /** true bila pasien memenuhi baris ini (kode cocok dan tidak kena pengecualian) */
    public static boolean cocokBaris(java.util.Set<String> dxPasien, Baris b){
        if(!cocok(dxPasien,b.kode)){
            return false;
        }
        return !cocok(dxPasien,b.kecuali);
    }

    // ===== klasifikasi asal pasien untuk kolom formulir =====
    public static final int RM_RS=0, RM_BIDAN=1, RM_PUSKESMAS=2, RM_FASKES_LAIN=3, RUJUKAN_NON_MEDIS=4, NON_RUJUKAN=5;

    /**
     * Ekspresi SQL yang menghasilkan kode asal pasien 0..5 (lihat konstanta di atas).
     *
     * Rujukan Medis = pasien MASUK melalui rujukan dari faskes LUAR (juknis butir 31).
     * Karena itu rujukan internal tidak boleh ikut terhitung. Pada SEP BPJS, rujukan
     * internal ditandai kdppkrujukan sama dengan kdppkpelayanan (kode RS ini sendiri) —
     * banyak sekali kasusnya (mis. rujukan dari poli sendiri / pasien datang langsung),
     * sehingga wajib dikecualikan, kalau tidak pasien akan salah terhitung sebagai
     * "rujukan dari Rumah Sakit".
     *
     * Jenis faskes perujuk luar dibaca dari kode PPK pada SEP (karakter ke-5 'R' =
     * rumah sakit, kode numerik murni = puskesmas, huruf lain = klinik/dokter praktik),
     * lalu jatuh ke deteksi teks nama perujuk pada rujukan masuk.
     *
     * Catatan : "KLINIK POLRES" adalah fasilitas medis sehingga masuk Rujukan Medis,
     * sedangkan "KEPOLISIAN/VISUM" dsb. adalah Rujukan Non Medis (juknis butir 34).
     */
    public static String caseAsal(){
        // hanya kode PPK rujukan dari faskes LUAR (bukan RS ini sendiri)
        String sepLuar="(select s.kdppkrujukan from bridging_sep s where s.no_rawat=reg_periksa.no_rawat "+
                       "and ifnull(s.kdppkrujukan,'')<>'' and s.kdppkrujukan<>s.kdppkpelayanan limit 1)";
        // ada SEP yang menyatakan rujukan internal (kode rujukan = kode RS ini)
        String sepInternal="exists(select 1 from bridging_sep s2 where s2.no_rawat=reg_periksa.no_rawat "+
                           "and ifnull(s2.kdppkrujukan,'')<>'' and s2.kdppkrujukan=s2.kdppkpelayanan)";
        String nm="upper(ifnull(rujuk_masuk.perujuk,''))";
        String nmKosong="(trim("+nm+") in ('','-','--'))";
        String nonMedis="("+nm+" rlike 'POLISI|KEPOLISIAN|POLRES|POLSEK' and "+nm+" not like '%KLIN%')";
        String nmBidan="("+nm+" like '%BIDAN%')";
        String nmPkm="("+nm+" like '%PUSKESMAS%' or "+nm+" like '%PKM%' or "+nm+" like '%PUSTU%')";
        String nmRS ="(("+nm+" like '%RUMAH SAKIT%' or "+nm+" like '%RS%') and not "+nmPkm+" and not "+nmBidan+")";
        return
            "case when rujuk_masuk.no_rawat is null then "+NON_RUJUKAN+" "+
            "when "+nonMedis+" then "+RUJUKAN_NON_MEDIS+" "+
            // bidan tidak punya kode PPK khusus di SEP, jadi selalu dikenali dari nama perujuk
            "when "+nmBidan+" then "+RM_BIDAN+" "+
            // rujukan dari faskes luar menurut SEP
            "when "+sepLuar+" is not null then ("+
            "  case when substring("+sepLuar+",5,1)='R' then "+RM_RS+" "+
            "       when "+sepLuar+" regexp '^[0-9]+$' then "+RM_PUSKESMAS+" "+
            "       else "+RM_FASKES_LAIN+" end) "+
            // SEP menyatakan rujukan internal -> bukan rujukan dari luar
            "when "+sepInternal+" then "+NON_RUJUKAN+" "+
            // tanpa SEP : andalkan nama perujuk
            "when "+nmKosong+" then "+NON_RUJUKAN+" "+
            "when "+nmRS+" then "+RM_RS+" "+
            "when "+nmPkm+" then "+RM_PUSKESMAS+" "+
            "else "+RM_FASKES_LAIN+" end";
    }
}
