/*
 * Daftar jenis kegiatan Formulir RL 3.7 Rekapitulasi Kegiatan Pelayanan Neonatal, Bayi,
 * dan Balita. Struktur mengikuti TEMPLATE RESMI RS (file "RL 3.7 BAYI.xls", sheet
 * "data RL 37") : 17 kelompok kegiatan.
 *
 * KETERBATASAN DATA KHANZA :
 *  - Usia gestasi (prematur/tidak) TIDAK dicatat. Pemilahan Prematur (<37mgg) /
 *    Non Prematur (>=37-41mgg) / >41mgg tidak bisa pasti. Sebagai pendekatan, bayi
 *    lahir hidup dikelompokkan menurut BERAT LAHIR (dari pasien_bayi) dengan heuristik
 *    klinis : berat <2500 gram (BBLR/BBLSR/BBLER) hampir selalu prematur -> dimasukkan
 *    ke kelompok Prematur; berat >=2500 gram -> Non Prematur. Kelompok >41 minggu dan
 *    pemilahan gestasi lebih rinci dibiarkan 0 untuk penyesuaian manual (ditandai *).
 *  - Sebagian besar baris (imunisasi, skrining, gizi buruk, HIV/Sifilis/Hepatitis, dll.)
 *    tidak ada sumber datanya di Khanza -> selalu 0, ditandai (*), diisi manual di
 *    SIRS Online versi 3.
 *
 * PENGISIAN OTOMATIS ATAS PERMINTAAN RS (mengikuti nomor pada template resmi) :
 *  - No. 6  Bayi baru lahir yang dilakukan IMD -> diambil dari kategori No. 1.2 + 1.3
 *    (bayi lahir hidup berat >=2500 gram) yang proses lahirnya NORMAL/spontan.
 *  - No. 7  Skrining Hipertiroid Kongenital  -> disalin dari No. 1 Bayi Lahir Hidup.
 *  - No. 15.2 Pemberian Hb 0 (bayi dari Ibu Hepatitis+) -> disalin dari No. 1.
 *  - No. 16.6 Pemberian KIE (anak balita)               -> disalin dari No. 1.
 *  - No. 4.1 - 4.7 Komplikasi Neonatal -> dihitung dari diagnosa ICD-10 pasien neonatal
 *    (usia hari / 0 bulan) dalam periode, dipisah Non Rujukan Hidup / Mati (mati bila
 *    reg_periksa.stts='Meninggal' atau kamar_inap.stts_pulang='Meninggal') :
 *      4.1 Asfiksia          = P21
 *      4.2 Trauma Kelahiran  = P10-P15
 *      4.3 BBLR              = P07
 *      4.4 Tetanus Neonatorum= A33
 *      4.5 Kelainan Bawaan   = Q (Q00-Q99)
 *      4.6 Covid-19          = U07.1 / U07.2 / B34.2
 *      4.7 Infeksi / Sepsis  = P36-P39
 *    (4.8 Komplikasi lainnya dibiarkan manual agar tidak dobel hitung.)
 */
package laporan;

public class KegiatanNeonatalSirs63 {

    /** penanda sumber query khusus : bayi lahir hidup kategori 1.2 + 1.3 (berat >=2500 gram)
     * yang proses lahirnya normal/spontan. Dipakai baris No. 6 (IMD). */
    public static final String SUMBER_NORMAL_12_13 = "NORMAL_12_13";

    public static class Baris {
        public final String no;
        public final String nama;
        public final boolean judul;      // baris section (tanpa angka) - tidak dipakai template ini
        public final String jumlahDari;  // prefix sub-baris yang dijumlahkan (mis. "1.1.")
        public final String beratBand;   // penanda pengisian otomatis dari berat lahir
        public final String salinDari;   // ambil angka apa adanya dari baris lain (mis. "1")
        public final String sumber;      // penanda sumber query khusus
        public final String[] icd;       // prefix ICD-10 diagnosa pasien neonatal (mis. "P21")
        Baris(String no,String nama,boolean judul,String jumlahDari,String beratBand){
            this(no,nama,judul,jumlahDari,beratBand,null,null,null);
        }
        Baris(String no,String nama,boolean judul,String jumlahDari,String beratBand,String salinDari,String sumber){
            this(no,nama,judul,jumlahDari,beratBand,salinDari,sumber,null);
        }
        Baris(String no,String nama,boolean judul,String jumlahDari,String beratBand,String salinDari,String sumber,String[] icd){
            this.no=no; this.nama=nama; this.judul=judul; this.jumlahDari=jumlahDari; this.beratBand=beratBand;
            this.salinDari=salinDari; this.sumber=sumber; this.icd=icd;
        }
        public boolean tanpaSumberData(){
            return jumlahDari==null && beratBand==null && salinDari==null && sumber==null && icd==null && !judul;
        }
    }
    private static Baris sec(String no,String nama){ return new Baris(no,nama,true,null,null); }
    private static Baris grp(String no,String nama,String pf){ return new Baris(no,nama,false,pf,null); }
    private static Baris row(String no,String nama){ return new Baris(no,nama,false,null,null); }
    private static Baris berat(String no,String nama,String band){ return new Baris(no,nama,false,null,band); }
    private static Baris salin(String no,String nama,String dari){ return new Baris(no,nama,false,null,null,dari,null); }
    private static Baris sumberKhusus(String no,String nama,String sumber){ return new Baris(no,nama,false,null,null,null,sumber); }
    private static Baris komplikasi(String no,String nama,String... icd){ return new Baris(no,nama,false,null,null,null,null,icd); }

    // kode berat : BBLER=<1000, BBLSR=1000-<1500, BBLR=1500-<2500, BBLN=2500-<4000, BBLL=>=4000
    public static final Baris[] BARIS = {
        grp("1","Bayi Lahir Hidup","1."),
        grp("1.1","Lahir Prematur (< 37 minggu)","1.1."),
        berat("1.1.1","1500 - <2500 gram (BBLR)","BBLR"),
        berat("1.1.2","1000 - <1500 gram (BBLSR)","BBLSR"),
        berat("1.1.3","<1000 gram (BBLER)","BBLER"),
        grp("1.2","Lahir Non Prematur (>= 37 - 41 minggu)","1.2."),
        row("1.2.1","1500 - <2500 gram (BBLR)"),
        berat("1.2.2","2500 - <4000 gram (BBLN)","BBLN"),
        berat("1.2.3",">=4000 gram (BBLL)","BBLL"),
        grp("1.3","Lahir Lebih dari 41 minggu","1.3."),
        row("1.3.1","1500 - <2500 gram (BBLR)"),
        row("1.3.2","2500 - <4000 gram (BBLN)"),
        row("1.3.3",">=4000 gram (BBLL)"),
        grp("2","Lahir Mati","2."),
        row("2.1","Lahir Mati Antepartum"),
        row("2.2","Lahir Mati Intrapartum"),
        grp("3","Kematian Neonatal dan Perinatal","3."),
        row("3.1","Kematian Neonatal Dini (0 - 7 hari)"),
        row("3.2","Kematian Neonatal Lanjut Perinatal (8 - 28 hari)"),
        grp("4","Komplikasi Neonatal:","4."),
        komplikasi("4.1","Asfiksia","P21"),
        komplikasi("4.2","Trauma Kelahiran","P10","P11","P12","P13","P14","P15"),
        komplikasi("4.3","BBLR","P07"),
        komplikasi("4.4","Tetanus Neonatorum","A33"),
        komplikasi("4.5","Kelainan Bawaan","Q"),
        komplikasi("4.6","Covid-19","U07.1","U07.2","B34.2"),
        komplikasi("4.7","Infeksi / Sepsis","P36","P37","P38","P39"),
        row("4.8","Komplikasi lainnya"),
        row("5","Bayi BBLR yang dilakukan perawatan metode kanguru"),
        sumberKhusus("6","Bayi baru lahir yang dilakukan IMD",SUMBER_NORMAL_12_13),
        salin("7","Bayi baru lahir yang dilakukan Skrining Hipertiroid Kongenital","1"),
        grp("8","Bayi dan Anak Balita","8."),
        row("8.1","Bayi Baru Lahir (0 - 28 hari)"),
        row("8.2","Bayi (29 hari - 11 bulan)"),
        row("8.3","Anak Balita (12 - 59 bulan)"),
        grp("9","Balita Gizi Buruk","9."),
        row("9.1","Balita Gizi Buruk usia 0-5 bulan"),
        row("9.2","Balita Gizi Buruk usia 6-59 bulan"),
        row("10","Balita menggunakan Buku KIA"),
        grp("11","Balita dilakukan skrining pertumbuhan dan perkembangan","11."),
        row("11.1","Skrining Pertumbuhan sesuai umur"),
        row("11.2","Skrining perkembangan sesuai umur"),
        row("11.3","Skrining keterlambatan bicara dan bahasa"),
        row("11.4","Assessment kelainan motorik"),
        row("11.5","Skrining Kelainan Perilaku"),
        row("11.6","Skrining Gangguan Pendengaran"),
        row("11.7","Skrining Gangguan Penglihatan"),
        grp("12","Bayi mendapatkan imunisasi, Vitamin, dan Pengobatan Profilaksis:","12."),
        row("12.1","Hb 0"),
        row("12.2","BCG"),
        row("12.3","Polio 1,2,3"),
        row("12.4","DPT-HB-HiB 1,2,3,4"),
        row("12.5","IPV"),
        row("12.6","Campak-Rubella"),
        row("12.7","Vitamin A 100.000 SI (1 kali dalam setahun)"),
        row("12.8","Pemberian Komunikasi, Informasi dan Edukasi (KIE)"),
        grp("13","Bayi yang lahir dari Ibu HIV +","13."),
        row("13.1","Pemeriksaan Early Infant Diagnosis (EID)"),
        row("13.2","Pengobatan ARV bagi balita HIV+"),
        row("13.3","Pengobatan profilaksis kotrimoksazol"),
        grp("14","Bayi yang lahir dari Ibu Sifilis +","14."),
        row("14.1","Pemeriksaan Titer RPR"),
        row("14.2","Pengobatan dosis tunggal Benzatin Penicilin G"),
        grp("15","Bayi yang lahir dari Ibu Hepatitis +","15."),
        row("15.1","Pemeriksaan serologis HBs Ag"),
        salin("15.2","Pemberian Hb 0","1"),
        row("15.3","Pemberian Hb Ig"),
        grp("16","Anak Balita (12 - 59 bulan) mendapatkan Imunisasi, Vitamin, dan Pengobatan profilaksis:","16."),
        row("16.1","Campak-Rubela"),
        row("16.2","Vitamin A 200.000 SI (2 kali dalam setahun)"),
        row("16.3","Anak balita mendapat obat pencegahan kecacingan 1 kali setahun"),
        row("16.4","Balita (0-59 bulan) terduga TBC/kontak erat mendapat TPT (Terapi Pencegahan TBC)"),
        row("16.5","Balita (0-59 bulan) TBC mendapatkan OAT"),
        salin("16.6","Pemberian Komunikasi, Informasi dan Edukasi (KIE)","1"),
        grp("17","Balita Gizi Buruk mendapat perawatan","17."),
        row("17.1","Balita Gizi Buruk usia 0-5 bulan yang mendapat rawat inap"),
        row("17.2","Balita Gizi Buruk usia 6-59 bulan yang mendapat rawat inap"),
        row("17.3","Balita Gizi Buruk usia 6-59 bulan yang mendapat rawat jalan"),
    };

    /** kode band berat lahir, atau null bila berat tidak diketahui */
    public static String bandBerat(double gram){
        if(gram<=0){ return null; }
        if(gram<1000){ return "BBLER"; }
        if(gram<1500){ return "BBLSR"; }
        if(gram<2500){ return "BBLR"; }
        if(gram<4000){ return "BBLN"; }
        return "BBLL";
    }
}
