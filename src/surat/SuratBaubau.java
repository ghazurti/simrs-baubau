package surat;

import fungsi.koneksiDB;
import fungsi.validasi;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Fungsi bantu cetakan surat keterangan format RSUD Kota Baubau
 * (kop Pemkot Baubau, tanggal Indonesia, terbilang, nomor 445/../ROMAWI/TAHUN).
 */
public final class SuratBaubau {
    private static final String[] BULAN={"Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"};
    private static final String[] HARI={"Senin","Selasa","Rabu","Kamis","Jumat","Sabtu","Minggu"};
    private static final String[] ROMAWI={"I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII"};
    private static final String[] SATUAN={"","Satu","Dua","Tiga","Empat","Lima","Enam","Tujuh","Delapan","Sembilan","Sepuluh","Sebelas"};

    /** Penandatangan "Mengetahui" pada Surat Keterangan Kelahiran */
    public static final String KABID_PELAYANAN="dr. Wa Ode Nurul Husna";
    public static final String KABID_PELAYANAN_NIP="19771013 201001 2 003";

    private SuratBaubau(){}

    private static LocalDate tgl(String yyyyMMdd){
        try {
            return LocalDate.parse(yyyyMMdd.trim().substring(0,10));
        } catch (Exception e) {
            return null;
        }
    }

    /** 2026-10-19 -> 19 Oktober 2026 */
    public static String tglIndo(String yyyyMMdd){
        LocalDate t=tgl(yyyyMMdd);
        return t==null?"":String.format("%02d",t.getDayOfMonth())+" "+BULAN[t.getMonthValue()-1]+" "+t.getYear();
    }

    /** 2026-10-19 -> Senin */
    public static String hariIndo(String yyyyMMdd){
        LocalDate t=tgl(yyyyMMdd);
        return t==null?"":HARI[t.getDayOfWeek().getValue()-1];
    }

    public static String hariIni(){
        return LocalDate.now().toString();
    }

    public static String romawi(int angka){
        return (angka>=1&&angka<=12)?ROMAWI[angka-1]:String.valueOf(angka);
    }

    /** 3 -> Tiga, 25 -> Dua Puluh Lima */
    public static String terbilang(long angka){
        if(angka<12){
            return angka<=0?"Nol":SATUAN[(int)angka];
        }else if(angka<20){
            return SATUAN[(int)angka-10]+" Belas";
        }else if(angka<100){
            return (SATUAN[(int)angka/10]+" Puluh "+(angka%10==0?"":SATUAN[(int)angka%10])).trim();
        }else if(angka<200){
            return ("Seratus "+(angka%100==0?"":terbilang(angka%100))).trim();
        }else if(angka<1000){
            return (SATUAN[(int)angka/100]+" Ratus "+(angka%100==0?"":terbilang(angka%100))).trim();
        }
        return String.valueOf(angka);
    }

    /** "3" / "3 hari" -> "3 ( Tiga )", teks tanpa angka di depan dikembalikan apa adanya */
    public static String angkaTerbilang(String angka){
        java.util.regex.Matcher m=java.util.regex.Pattern.compile("^\\s*(\\d+)").matcher(angka==null?"":angka);
        if(m.find()){
            long n=Long.parseLong(m.group(1));
            return n+" ( "+terbilang(n)+" )";
        }
        return angka==null?"":angka;
    }

    /** jumlah hari inklusif antara dua tanggal yyyy-MM-dd */
    public static long lamaHari(String awal,String akhir){
        LocalDate a=tgl(awal),b=tgl(akhir);
        return (a==null||b==null)?0:java.time.temporal.ChronoUnit.DAYS.between(a,b)+1;
    }

    /**
     * Nomor surat: bila sudah berformat lengkap (mengandung "/") dipakai apa adanya,
     * bila nomor otomatis Khanza dicetak "445 / ...... / ROMAWI / TAHUN" untuk diisi tangan.
     */
    public static String nomor(String nomor,String yyyyMMdd){
        if(nomor!=null&&nomor.contains("/")){
            return nomor;
        }
        LocalDate t=tgl(yyyyMMdd);
        if(t==null){
            t=LocalDate.now();
        }
        return "445 /            / "+romawi(t.getMonthValue())+" / "+t.getYear();
    }

    /** Kode pegawai berformat NIP 18 digit -> "19790607 200312 2 009", selain itu kosong */
    public static String nip(String kode){
        if(kode!=null&&kode.matches("[0-9]{18}")){
            return kode.substring(0,8)+" "+kode.substring(8,14)+" "+kode.substring(14,15)+" "+kode.substring(15);
        }
        return "";
    }

    public static String jk(String jk){
        if(jk==null){
            return "";
        }
        return jk.equals("L")?"Laki-Laki":(jk.equals("P")?"Perempuan":jk);
    }

    /** umurdaftar + sttsumur (Th/Bl/Hr) -> "45 Tahun" */
    public static String umur(String umur,String stts){
        String s=stts==null?"":stts.trim();
        switch (s) {
            case "Th": s="Tahun"; break;
            case "Bl": s="Bulan"; break;
            case "Hr": s="Hari"; break;
        }
        return ((umur==null?"":umur.trim())+" "+s).trim();
    }

    /** Nilai parameter untuk text field markup="styled" */
    public static String esc(String s){
        return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    public static void put(Map<String,Object> param,String kunci,String nilai){
        param.put(kunci,esc(nilai));
    }

    /**
     * Identitas pasien & dokter registrasi untuk no_rawat:
     * nama, umur, norm, jk, pekerjaan, alamat, agama, bpjs, dokter, nip.
     */
    public static Map<String,Object> identitas(String norawat){
        Map<String,Object> param=new HashMap<>();
        try (PreparedStatement ps=koneksiDB.condb().prepareStatement(
                "select pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,pasien.no_rkm_medis,pasien.jk,pasien.pekerjaan,"+
                "pasien.agama,pasien.no_peserta,dokter.kd_dokter,dokter.nm_dokter,"+
                "concat_ws(', ',nullif(trim(pasien.alamat),'-'),nullif(kelurahan.nm_kel,'-'),nullif(kecamatan.nm_kec,'-'),nullif(kabupaten.nm_kab,'-')) as alamat "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "+
                "left join kelurahan on pasien.kd_kel=kelurahan.kd_kel left join kecamatan on pasien.kd_kec=kecamatan.kd_kec "+
                "left join kabupaten on pasien.kd_kab=kabupaten.kd_kab where reg_periksa.no_rawat=?")) {
            ps.setString(1,norawat);
            try (ResultSet rs=ps.executeQuery()) {
                if(rs.next()){
                    put(param,"nama",rs.getString("nm_pasien"));
                    put(param,"umur",umur(rs.getString("umurdaftar"),rs.getString("sttsumur")));
                    put(param,"norm",rs.getString("no_rkm_medis"));
                    put(param,"jk",jk(rs.getString("jk")));
                    put(param,"pekerjaan",rs.getString("pekerjaan"));
                    put(param,"alamat",rs.getString("alamat"));
                    put(param,"agama",rs.getString("agama"));
                    put(param,"bpjs",rs.getString("no_peserta"));
                    put(param,"dokter",rs.getString("nm_dokter"));
                    put(param,"nip",nip(rs.getString("kd_dokter")));
                }
            }
        } catch (Exception e) {
            System.out.println("surat.SuratBaubau.identitas() : "+e);
        }
        return param;
    }

    /** Item popup menu bergaya Khanza */
    public static javax.swing.JMenuItem menu(String teks,Runnable aksi){
        javax.swing.JMenuItem mn=new javax.swing.JMenuItem(teks);
        mn.setBackground(new java.awt.Color(255,255,254));
        mn.setFont(new java.awt.Font("Tahoma",0,11));
        mn.setForeground(new java.awt.Color(50,50,50));
        mn.setIcon(new javax.swing.ImageIcon(SuratBaubau.class.getResource("/picture/category.png")));
        mn.setPreferredSize(new java.awt.Dimension(280,26));
        mn.addActionListener(e -> aksi.run());
        return mn;
    }

    public static void cetak(String jasper,String judul,Map<String,Object> param){
        new validasi().MyReportqry(jasper,"report","::[ "+judul+" ]::","select 1",param);
    }

    /**
     * Daftar pilihan dengan coretan pada pilihan yang tidak berlaku,
     * untuk text field Jasper dengan markup="styled".
     */
    public static String coret(String[] opsi,Collection<String> berlaku){
        return coret(opsi,berlaku," / ");
    }

    public static String coret(String[] opsi,Collection<String> berlaku,String pemisah){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<opsi.length;i++){
            if(i>0){
                sb.append(pemisah);
            }
            if(berlaku.contains(opsi[i])){
                sb.append(opsi[i]);
            }else{
                sb.append("<style isStrikeThrough=\"true\">").append(opsi[i]).append("</style>");
            }
        }
        return sb.toString();
    }
}
