/*
 * Pengelompokan umur pasien sesuai SIRS Revisi 6.3 (Juknis 2025, BAB V & VI).
 * Dipakai bersama oleh RL 4.1 (morbiditas rawat inap) dan RL 5.1 (morbiditas rawat jalan).
 *
 * 25 kelompok umur:
 *   <1 jam, 1-23 jam, 1-7 hr, 8-28 hr, 29 hr-<3 bln, 3-<6 bln, 6-11 bln,
 *   1-4 th, 5-9 th, 10-14 th, 15-19 th, 20-24 th, 25-29 th, 30-34 th, 35-39 th,
 *   40-44 th, 45-49 th, 50-54 th, 55-59 th, 60-64 th, 65-69 th, 70-74 th,
 *   75-79 th, 80-84 th, >=85 th
 *
 * Catatan: pasien.tgl_lahir bertipe DATE (tanpa jam). Jam lahir diambil dari
 * pasien_bayi.jam_lahir (menu Data Kelahiran Bayi) bila ada, sehingga kelompok
 * "<1 jam" dan "1-23 jam" bisa dihitung tepat untuk bayi baru lahir.
 * Bila jam lahir tidak diketahui, dianggap 00:00.
 */
package laporan;

public class UmurSirs63 {

    /** Label 25 kelompok umur, urut sesuai indeks 0..24 */
    public static final String[] LABEL = {
        "<1 jam","1-23 jam","1-7 hr","8-28 hr","29hr-<3bln","3-<6bln","6-11bln",
        "1-4 th","5-9 th","10-14 th","15-19 th","20-24 th","25-29 th","30-34 th",
        "35-39 th","40-44 th","45-49 th","50-54 th","55-59 th","60-64 th",
        "65-69 th","70-74 th","75-79 th","80-84 th",">=85 th"
    };

    public static final int JML = 25;

    /**
     * Sama dengan {@link #caseUmur(String,String,String)} dengan jam lahir dianggap 00:00.
     *
     * @param lahir ekspresi tanggal lahir, mis. "pasien.tgl_lahir"
     * @param acuan ekspresi datetime acuan penghitungan umur
     */
    public static String caseUmur(String lahir, String acuan){
        return caseUmur(lahir,"'00:00:00'",acuan);
    }

    /**
     * Menghasilkan ekspresi SQL CASE yang mengembalikan indeks kelompok umur 0..24,
     * atau -1 bila tanggal lahir tidak diketahui.
     *
     * @param lahir    ekspresi tanggal lahir, mis. "pasien.tgl_lahir"
     * @param jamLahir ekspresi jam lahir, mis. "ifnull(pasien_bayi.jam_lahir,'00:00:00')"
     * @param acuan    ekspresi datetime acuan penghitungan umur,
     *                 mis. "timestamp(kamar_inap.tgl_keluar,kamar_inap.jam_keluar)"
     */
    public static String caseUmur(String lahir, String jamLahir, String acuan){
        String jam = "timestampdiff(hour,timestamp("+lahir+","+jamLahir+"),"+acuan+")";
        String hr  = "timestampdiff(day,"+lahir+",date("+acuan+"))";
        String bln = "timestampdiff(month,"+lahir+",date("+acuan+"))";
        String th  = "timestampdiff(year,"+lahir+",date("+acuan+"))";
        StringBuilder s = new StringBuilder();
        s.append("case when ").append(lahir).append(" is null then -1 ");
        s.append("when ").append(jam).append("<1 then 0 ");
        s.append("when ").append(jam).append("<24 then 1 ");
        s.append("when ").append(hr).append("<=7 then 2 ");
        s.append("when ").append(hr).append("<=28 then 3 ");
        s.append("when ").append(bln).append("<3 then 4 ");
        s.append("when ").append(bln).append("<6 then 5 ");
        s.append("when ").append(bln).append("<12 then 6 ");
        // 1-4 th s.d. 80-84 th : pita 5 tahunan
        int idx = 7;
        for(int batas=5; batas<=85; batas+=5){
            s.append("when ").append(th).append("<").append(batas).append(" then ").append(idx).append(" ");
            idx++;
        }
        s.append("else 24 end");
        return s.toString();
    }
}
