/*
 * Daftar jenis kegiatan Formulir RL 3.9 Rekapitulasi Kegiatan Pelayanan Radiologi
 * sesuai Juknis SIRS Revisi 6.3 (2025), BAB IV huruf I.
 *
 * Kegiatan dikelompokkan dari nama pemeriksaan (jns_perawatan_radiologi.nm_perawatan)
 * karena Khanza tidak menyimpan kategori radiologi versi SIRS. Pemeriksaan yang tidak
 * dikenali masuk ke baris "Lain-Lain" pada kelompoknya (juknis butir 3).
 */
package laporan;

public class KegiatanRadiologiSirs63 {

    public static class Baris {
        public final String no;
        public final String nama;
        public final String jumlahDari; // bila diisi, baris = penjumlahan sub-baris berprefix ini
        Baris(String no,String nama,String jumlahDari){ this.no=no; this.nama=nama; this.jumlahDari=jumlahDari; }
    }

    /** 24 baris sesuai formulir juknis (termasuk baris kelompok dan TOTAL) */
    public static final Baris[] BARIS = {
        new Baris("1","Radiodiagnostik","1."),
        new Baris("1.1","Foto tanpa bahan kontras",null),
        new Baris("1.2","Foto dengan bahan kontras",null),
        new Baris("1.3","Foto dengan rol film",null),
        new Baris("1.4","Flouroskopi",null),
        new Baris("1.5","Foto Gigi",null),
        new Baris("1.6","C.T. Scan",null),
        new Baris("1.7","Lymphografi",null),
        new Baris("1.8","Angiograpi",null),
        new Baris("1.9","Lain-Lain",null),

        new Baris("2","Radioterapi","2."),
        new Baris("2.1","Radioterapi dengan Linac",null),
        new Baris("2.2","Radioterapi dengan Cobalt",null),
        new Baris("2.3","Radioterapi dengan Brakhiterapi",null),
        new Baris("2.4","Lain-Lain",null),

        new Baris("3","Kedokteran Nuklir","3."),
        new Baris("3.1","Diagnostik",null),
        new Baris("3.2","Therapi",null),
        new Baris("3.3","Lain-Lain",null),

        new Baris("4","Imaging/Pencitraan","4."),
        new Baris("4.1","USG",null),
        new Baris("4.2","MRI",null),
        new Baris("4.3","Lain-lain",null),

        new Baris("99","TOTAL","@"),   // @ = jumlah seluruh baris kelompok
    };

    private static boolean ada(String s, String... kata){
        for(String x : kata){
            if(s.contains(x)){
                return true;
            }
        }
        return false;
    }

    /**
     * Memetakan nama pemeriksaan radiologi ke nomor baris formulir RL 3.9.
     *
     * Urutan pengecekan penting : modalitas diperiksa lebih dulu (USG/MRI/CT/angiografi),
     * baru jenis foto. Contoh "CT KEPALA - Dengan Kontras" harus masuk 1.6 C.T. Scan,
     * bukan 1.2 Foto dengan bahan kontras, karena C.T. Scan adalah baris tersendiri.
     *
     * @param nmPerawatan nama pemeriksaan apa adanya
     * @return nomor baris, mis. "1.1"
     */
    public static String kategori(String nmPerawatan){
        String s=nmPerawatan==null?"":nmPerawatan.toUpperCase().trim();

        // --- Imaging/Pencitraan ---
        if(ada(s,"USG","ULTRASONOGRAF","ULTRASONOGRAFI")){
            return "4.1";
        }
        if(ada(s,"MRI","MRA","MAGNETIC RESONANCE")){
            return "4.2";
        }

        // --- Kedokteran Nuklir ---
        if(ada(s,"SINTIGRAF","SCINTIGRAF","KEDOKTERAN NUKLIR","NUKLIR")){
            return ada(s,"TERAPI","THERAPI")?"3.2":"3.1";
        }

        // --- Radioterapi ---
        if(ada(s,"LINAC")){
            return "2.1";
        }
        if(ada(s,"COBALT","KOBALT")){
            return "2.2";
        }
        if(ada(s,"BRAKHI","BRACHY","BRAKI")){
            return "2.3";
        }
        if(ada(s,"RADIOTERAPI","RADIASI EKSTERNA")){
            return "2.4";
        }

        // --- Radiodiagnostik : modalitas khusus lebih dulu ---
        if(ada(s,"CT ","CT-","C.T","CT_","CTSCAN","CT SCAN","MSCT","MDCT")){
            return "1.6";
        }
        if(ada(s,"ANGIOGRAF","ANGIOGRAPI","ARTERIOGRAF","DSA")){
            return "1.8";
        }
        if(ada(s,"LYMPHOGRAF","LIMFOGRAF")){
            return "1.7";
        }
        if(ada(s,"FLUOROS","FLOUROS")){
            return "1.4";
        }
        if(ada(s,"GIGI","DENTAL","PANORAMIC","PANORAMIK","CEPHALOMETRI")){
            return "1.5";
        }
        if(ada(s,"ROL FILM","ROLL FILM")){
            return "1.3";
        }

        // pemeriksaan foto berbahan kontras :
        // "TANPA KONTRAS" harus dicek dulu supaya tidak salah masuk ke 1.2
        boolean tanpaKontras=ada(s,"TANPA KONTRAS","TANPA KONTAS","NON KONTRAS");
        if(!tanpaKontras && ada(s,"KONTRAS","IVP","HSG","COLON IN LOOP","COLON INLOOP","BARIUM",
                                  "OMD","URETROGRAF","CYSTOGRAF","SISTOGRAF","FISTULOGRAF","MYELOGRAF","APPENDICOGRAM")){
            return "1.2";
        }

        // sisanya dianggap foto polos bila memang pemeriksaan foto,
        // selain itu masuk lain-lain radiodiagnostik
        if(ada(s,"FOTO","THORAX","TORAKS","THORAK","BNO","ABDOMEN","CRANIUM","PELVIS","VERT.","VERTEBRA",
                 "JOINT","FEMUR","CRURIS","MANUS","PEDIS","ANTEBRACHI","HUMERUS","CLAVICULA","MANDIBULA",
                 "MAXILLA","LUMBOSACRAL","CERVICAL","THORACAL","SCOLIOSIS","GENU","ANKLE","WRIST","ELBOW",
                 "SHOULDER","HIP","DIGITI","EXTREMITAS","LEHER","CUBITI","SINUS","ATRESIA")){
            return "1.1";
        }
        return "1.9";
    }
}
