/*
 * Daftar jenis pemeriksaan Formulir RL 3.8 Rekapitulasi Kegiatan Pelayanan Laboratorium
 * sesuai Juknis SIRS Revisi 6.3 (2025), BAB IV huruf H.
 *
 * Khanza tidak menyimpan kategori laboratorium versi SIRS, sehingga pemeriksaan
 * dipetakan dari nama tes (template_laboratorium.Pemeriksaan).
 *
 * Catatan penting : formulir RL 3.8 TIDAK menyediakan baris "lain-lain". Tes yang tidak
 * tercantum pada daftar juknis (mis. MCV, MCH, MCHC, RDW, MPV, Troponin, Ferritin,
 * Golongan Darah) memang tidak dilaporkan pada formulir ini.
 */
package laporan;

public class KegiatanLabSirs63 {

    public static class Baris {
        public final String no;
        public final String nama;
        public final boolean judul;     // baris kelompok/section, tidak diisi angka sendiri
        public final String jumlahDari; // bila diisi, baris = penjumlahan sub-baris berprefix ini
        Baris(String no,String nama,boolean judul,String jumlahDari){
            this.no=no; this.nama=nama; this.judul=judul; this.jumlahDari=jumlahDari;
        }
    }

    private static Baris sec(String no,String nama){ return new Baris(no,nama,true,null); }
    private static Baris grp(String no,String nama){ return new Baris(no,nama,false,no+"."); }
    private static Baris row(String no,String nama){ return new Baris(no,nama,false,null); }

    public static final Baris[] BARIS = {
        sec("A","PATOLOGI KLINIK"),
        grp("1","Hematologi"),
        row("1.1","Kadar Hemoglobin"),
        row("1.2","Nilai Hematokrit"),
        row("1.3","Hitung Lekosit"),
        row("1.4","Hitung Eritrosit"),
        row("1.5","Hitung Eosinophil"),
        row("1.6","Hitung Jenis Lekosit (%/absolut)"),
        row("1.7","Laju Endap Darah"),
        row("1.8","Hitung Retikulosit"),
        row("1.9","Hitung Trombosit"),
        grp("2","Kimia Klinik"),
        row("2.1","Protein Total"),
        row("2.2","Albumin"),
        row("2.3","Globulin"),
        row("2.4","Bilirubin Total/Direk/Indirek"),
        row("2.5","SGOT/AST"),
        row("2.6","SGPT/ALT"),
        row("2.7","Ureum/BUN"),
        row("2.8","Kreatinin (eGFR)"),
        row("2.9","Asam Urat"),
        row("2.10","Trigliserida"),
        row("2.11","Kolesterol Total"),
        row("2.12","Kolesterol HDL"),
        row("2.13","Kolesterol LDL (direk)"),
        row("2.14","Glukosa Sewaktu/Puasa / 2jam PP"),
        row("2.15","HbA1c"),
        row("2.16","Fosfatase alkali"),
        row("2.17","Gamma GT"),
        row("2.18","LDH"),
        row("2.19","G 6 PD"),
        row("2.20","Amilase"),
        row("2.21","Lipase"),
        row("2.22","Cholinesterase"),
        row("2.23","CK Total -CK MB"),
        row("2.24","SI/TIBC"),
        row("2.25","Elektrolit Darah (Na, K, Cl, Ca, Mg, P)"),
        row("2.26","Analisa Gas Darah"),
        grp("3","Imunologi Klinik"),
        row("3.1","Widal"),
        row("3.2","Antibodi anti SARS-CoV-2"),
        row("3.3","Antigen SARS-CoV-2"),
        row("3.4","Dengue IgG-IgM"),
        row("3.5","HBs Ag"),
        row("3.6","Anti HBs"),
        row("3.7","Anti HBc"),
        row("3.8","Anti HBe"),
        row("3.9","Hbe Ag"),
        row("3.10","Anti HCV"),
        row("3.11","IgM Anti HAV"),
        row("3.12","Anti HIV"),
        row("3.13","NS1 (non structure antigen) Dengue"),
        row("3.14","Tes Antigen Malaria"),
        row("3.15","T3/T4 total"),
        row("3.16","FT3/FT4"),
        row("3.17","TSH"),
        grp("4","Urinalisis dan analisis cairan"),
        row("4.1","Protein/albumin"),
        row("4.2","Urobilinogen"),
        row("4.3","Bilirubin"),
        row("4.4","Sedimen Urine"),
        row("4.5","NAPZA Skrining"),
        grp("5","Hemostasis"),
        row("5.1","Masa perdarahan"),
        row("5.2","Masa pembekuan"),
        row("5.3","Masa prothrombin plasma"),
        row("5.4","Masa tromboplastin partial teraktivasi"),
        row("5.5","Masa thrombin"),
        row("5.6","Fibrinogen"),
        row("5.7","D-dimer"),
        row("5.8","Lupus anticoagulant"),

        sec("B","MIKROBIOLOGI KLINIK"),
        grp("6","Pemeriksaan dahak mikroskopis TBC Bakteri Tahan Asam (Mycobakterium tuberkulosis)"),
        row("6.1","Negatif"),
        row("6.2","1-9"),
        row("6.3","1+"),
        row("6.4","2+"),
        row("6.5","3+"),
        row("6.6","Tidak Dilakukan"),
        row("7","Biakan dan identifikasi bakteri aerob, serta uji kepekaan terhadap antibiotik"),
        row("8","Biakan virus dan uji kepekaan terhadap antivirus"),
        row("9","Biakan dan identifikasi M. tuberculosis dan uji kepekaan terhadap OAT"),
        grp("10","Pemeriksaan berbasis molekuler untuk deteksi virus DNA dan RNA (Influenza, SARS-CoV 1 dan 2, HIV)"),
        row("10.1","PCR"),
        row("10.2","Real time PCR"),
        row("10.3","Tes Cepat Molekuler"),
        row("10.4","Hibridisasi"),
        row("10.5","Sekuensing"),
        row("10.6","Metode lainnya"),
        grp("11","Pemeriksaan Tes Cepat Molekuler (TCM) untuk TBC dan TBC Resistan Obat (RO)"),
        row("11.1","Negatif"),
        row("11.2","Rif Sen"),
        row("11.3","Rif Res"),
        row("11.4","Rif Indet"),
        row("11.5","Invalid"),
        row("11.6","Error"),
        row("11.7","No Result"),
        row("11.8","Tidak Dilakukan"),
        grp("12","Pemeriksaan berbasis molekuler untuk deteksi bakteri aerob, anaerob dan fastidious"),
        row("12.1","PCR"),
        row("12.2","Real time PCR"),
        row("12.3","Tes Cepat Molekuler"),
        row("12.4","Hibridisasi"),
        row("12.5","Sekuensing"),
        row("12.6","Metode lainnya"),
        grp("13","Pemeriksaan berbasis molekuler untuk deteksi gen pengkode resistensi antimikroba"),
        row("13.1","PCR"),
        row("13.2","Real time PCR"),
        row("13.3","Tes Cepat Molekuler"),
        row("13.4","Hibridisasi"),
        row("13.5","Sekuensing"),
        row("13.6","Metode lainnya"),
        grp("14","Pemeriksaan berbasis molekuler untuk deteksi jamur"),
        row("14.1","PCR"),
        row("14.2","Real time PCR"),
        row("14.3","Tes Cepat Molekuler"),
        row("14.4","Hibridisasi"),
        row("14.5","Sekuensing"),
        row("14.6","Metode lainnya"),

        sec("C","PARASITOLOGI KLINIK"),
        grp("15","Pemeriksaan Mikroskopis"),
        row("15.1","Identifikasi cacing, larva/proglottid"),
        row("15.2","Identifikasi arthropoda (tuma, tungau, pinjal, kutu, arachnida, crustacea)"),
        row("15.3","Identifikasi nyamuk, larva nyamuk"),
        row("15.4","Identifikasi lalat dan larva lalat"),
        grp("16","Pemeriksaan Jamur"),
        row("16.1","Pemeriksaan langsung KOH"),
        row("16.2","Pemeriksaan langsung LPCB/tinta India"),
        row("16.3","Pemeriksaan jamur dengan pulasan khusus"),
        row("16.4","Kultur dan identifikasi jamur dari spesimen"),
        row("16.5","Identifikasi jamur dari biakan"),
        row("16.6","Uji kepekaan jamur ragi (manual/semiotomatis)"),
        row("16.7","Uji kepekaan jamur kapang (manual)"),

        sec("D","PATOLOGI ANATOMI"),
        row("17","Pemeriksaan tindakan biopsi aspirasi jarum halus dan/atau tindakan kedokteran lainnya"),
        grp("18","Pemeriksaan Sitopatologi"),
        row("18.1","Pemeriksaan Pap's Smear"),
        row("18.2","Pemeriksaan sitologi apus non ginekologi"),
        row("18.3","Pemeriksaan sitologi cairan"),
        grp("19","Pemeriksaan Histopatologi"),
        row("19.1","Pemeriksaan jaringan kecil"),
        row("19.2","Pemeriksaan jaringan sedang"),
        row("19.3","Pemeriksaan jaringan besar"),
        grp("20","Pemeriksaan Imunopatologi"),
        row("20.1","Pemeriksaan imunohistokimia Payudara"),
        row("20.2","Pemeriksaan imunohistokimia Limfoma"),
        row("20.3","Pemeriksaan imunohistokimia lanjutan"),
        row("20.4","Pemeriksaan imunositokimia"),
        row("20.5","Pemeriksaan imunofluoresensi"),
        grp("21","Pemeriksaan Patologi Molekuler"),
        row("21.1","Deteksi mutasi EGFR"),
        row("21.2","Deteksi mutasi all-RAS"),
        row("21.3","Deteksi mutasi BRAF"),
        row("21.4","Deteksi HPV Genotyping"),
        row("21.5","ISH"),
        row("21.6","CISH"),
        row("21.7","FISH"),
        row("22","Pemeriksaan Potong Beku"),
        row("23","Pemeriksaan Otopsi Klinik"),
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
     * Memetakan nama tes laboratorium ke nomor baris formulir RL 3.8.
     * @return nomor baris (mis. "1.1"), atau null bila tes tidak punya baris di juknis
     */
    public static String kategori(String pemeriksaan){
        String s=pemeriksaan==null?"":pemeriksaan.toUpperCase().trim();
        // buang penanda daftar & baris judul template
        while(s.startsWith("-")||s.startsWith(" ")){
            s=s.substring(1).trim();
        }
        if(s.isEmpty()||s.endsWith(":")){
            return null;   // baris judul template (mis. "DARAH RUTIN :")
        }

        // ---- Hematologi ----
        if(ada(s,"HEMOGLOBIN")||s.equals("HB")){ return "1.1"; }
        if(ada(s,"HEMATOKRIT","HEMATROKIT","HCT")){ return "1.2"; }
        if(ada(s,"LEKOSIT","LEUKOSIT")&&!ada(s,"SEDIMEN")){
            // "Hitung Jenis Lekosit" ditangani di bawah
            return "1.3";
        }
        if(ada(s,"ERITROSIT")&&!ada(s,"SEDIMEN")){ return "1.4"; }
        if(ada(s,"EOSINOFIL","EOSINOPHIL")){ return "1.5"; }
        if(ada(s,"LIMFOSIT","NEUTROFIL","MONOSIT","BASOFIL","HITUNG JENIS","I/T RATIO")){ return "1.6"; }
        if(ada(s,"LAJU ENDAP","LED")){ return "1.7"; }
        if(ada(s,"RETIKULOSIT")){ return "1.8"; }
        if(ada(s,"TROMBOSIT")){ return "1.9"; }

        // ---- Kimia Klinik ----
        if(ada(s,"PROTEIN TOTAL","TOTAL PROTEIN")){ return "2.1"; }
        if(ada(s,"ALBUMIN")&&!ada(s,"URIN")){ return "2.2"; }
        if(ada(s,"GLOBULIN")){ return "2.3"; }
        if(ada(s,"BILIRUBIN")){ return "2.4"; }
        if(ada(s,"SGOT","AST")){ return "2.5"; }
        if(ada(s,"SGPT","ALT")){ return "2.6"; }
        if(ada(s,"UREUM","BUN")){ return "2.7"; }
        if(ada(s,"CREATININ","KREATININ","EGFR")){ return "2.8"; }
        if(ada(s,"ASAM URAT","URIC ACID")){ return "2.9"; }
        if(ada(s,"TRIGLISERIDA","TRIGLYCERID")){ return "2.10"; }
        if(ada(s,"HDL")){ return "2.12"; }
        if(ada(s,"LDL")){ return "2.13"; }
        if(ada(s,"KOLESTEROL","CHOLESTEROL")){ return "2.11"; }
        if(ada(s,"GLUKOSA","GLUCOSA","GULA DARAH","GDS","GDP")){ return "2.14"; }
        if(ada(s,"HBA1C")){ return "2.15"; }
        if(ada(s,"FOSFATASE ALKALI","ALKALI PHOSPHAT","ALP")){ return "2.16"; }
        if(ada(s,"GAMMA GT","GGT")){ return "2.17"; }
        if(ada(s,"LDH")){ return "2.18"; }
        if(ada(s,"G 6 PD","G6PD")){ return "2.19"; }
        if(ada(s,"AMILASE","AMYLASE")){ return "2.20"; }
        if(ada(s,"LIPASE")){ return "2.21"; }
        if(ada(s,"CHOLINESTERASE")){ return "2.22"; }
        if(ada(s,"CK-MB","CKMB","CK TOTAL","CK MB")){ return "2.23"; }
        if(ada(s,"TIBC")||s.equals("SI")){ return "2.24"; }
        if(ada(s,"NATRIUM","KALIUM","CHLORIDA","KLORIDA","KALSIUM","MAGNESIUM","ELEKTROLIT")
           ||s.equals("NA")||s.equals("K")||s.equals("CL")||s.equals("CA")||s.equals("MG")){ return "2.25"; }
        if(ada(s,"GAS DARAH","BGA","ASTRUP")){ return "2.26"; }

        // ---- Imunologi Klinik ----
        if(ada(s,"WIDAL")){ return "3.1"; }
        if(ada(s,"SARS-COV","COVID")){
            return ada(s,"ANTIGEN","SWAB","RAPID ANTIGEN")?"3.3":"3.2";
        }
        if(ada(s,"NS1")){ return "3.13"; }
        if(ada(s,"DENGUE","DHF IGG","IGG-IGM")){ return "3.4"; }
        if(ada(s,"HBSAG","HBS AG")){ return "3.5"; }
        if(ada(s,"ANTI HBS","ANTI-HBS")){ return "3.6"; }
        if(ada(s,"ANTI HBC","ANTI-HBC")){ return "3.7"; }
        if(ada(s,"ANTI HBE")){ return "3.8"; }
        if(ada(s,"HBEAG","HBE AG")){ return "3.9"; }
        if(ada(s,"ANTI HCV","HCV")){ return "3.10"; }
        if(ada(s,"ANTI HAV","IGM HAV","HAV")){ return "3.11"; }
        if(ada(s,"HIV")){ return "3.12"; }
        if(ada(s,"MALARIA")){ return "3.14"; }
        if(ada(s,"FT3","FT4")){ return "3.16"; }
        if(ada(s,"TSH")){ return "3.17"; }
        if(ada(s,"T3","T4")){ return "3.15"; }

        // ---- Urinalisis ----
        if(ada(s,"SEDIMEN")){ return "4.4"; }
        if(ada(s,"UROBILINOGEN")){ return "4.2"; }
        if(ada(s,"NAPZA","AMPHETAMIN","METHAMPHETAMIN","BENZODIAZEPIN","NARKOBA")){ return "4.5"; }
        if(s.equals("PROTEIN")){ return "4.1"; }

        // ---- Hemostasis ----
        if(ada(s,"WAKTU PERDARAHAN","MASA PERDARAHAN","BLEEDING TIME")||s.equals("BT")){ return "5.1"; }
        if(ada(s,"WAKTU PEMBEKUAN","MASA PEMBEKUAN","CLOTTING TIME")||s.equals("CT")){ return "5.2"; }
        if(ada(s,"PROTHROMBIN","PROTOMBIN")||s.equals("PT")){ return "5.3"; }
        if(ada(s,"APTT","TROMBOPLASTIN")){ return "5.4"; }
        if(ada(s,"MASA THROMBIN")||s.equals("TT")){ return "5.5"; }
        if(ada(s,"FIBRINOGEN")){ return "5.6"; }
        if(ada(s,"D-DIMER","D DIMER")){ return "5.7"; }
        if(ada(s,"LUPUS")){ return "5.8"; }

        // ---- Mikrobiologi ----
        if(ada(s,"BTA","TAHAN ASAM")){ return "6"; }
        if(ada(s,"TCM","GENEXPERT","XPERT")){ return "11"; }
        if(ada(s,"KULTUR","BIAKAN","GRAM")){ return "7"; }

        return null;   // tidak ada barisnya di formulir RL 3.8
    }
}
