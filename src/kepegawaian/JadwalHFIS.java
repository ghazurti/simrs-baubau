package kepegawaian;

import bridging.ApiMobileJKN;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Ambil jadwal praktek dokter dari HFIS BPJS (WS Antrean: jadwaldokter/kodepoli/{poli}/tanggal/{tgl})
 * lalu sinkron ke tabel jadwal.
 *
 * Aturan: tiap pasangan dokter+poli yang ada di HFIS, jadwal lamanya di Khanza diganti jadwal HFIS.
 * Jadwal dokter/poli yang tidak ada di HFIS tidak disentuh.
 * Poli diambil dari maping_poli_bpjs, dokter dari maping_dokter_dpjpvclaim.
 */
public class JadwalHFIS {
    public static final String[] HARI={"","SENIN","SELASA","RABU","KAMIS","JUMAT","SABTU","AKHAD"};

    public static final String ST_BARU="Baru",ST_UBAH="Ubah",ST_SAMA="Sama",ST_HAPUS="Dihapus",
            ST_DOKTER="Dokter belum dimapping",ST_POLI="Poli belum dimapping",ST_BENTROK="Bentrok",ST_FORMAT="Format jam salah";

    public static class Baris {
        public String status="",kdDokter="",nmDokter="",kdDokterBpjs="",nmDokterBpjs="",
                kdPoli="",nmPoli="",kdPoliBpjs="",nmPoliBpjs="",hari="",jamMulai="",jamSelesai="",ket="";
        public int kuota;
        public boolean libur;
        public final List<LocalDate> tglLibur=new ArrayList<>();
        public boolean bisaSimpan(){
            return status.equals(ST_BARU)||status.equals(ST_UBAH)||status.equals(ST_SAMA);
        }
    }

    private final Connection koneksi;
    private final ApiMobileJKN api=new ApiMobileJKN();
    private final ObjectMapper mapper=new ObjectMapper();
    public final List<String> galat=new ArrayList<>();

    public JadwalHFIS(Connection koneksi){
        this.koneksi=koneksi;
    }

    /** daftar kode poli BPJS yang sudah dimapping */
    public List<String> poliBpjs() throws Exception {
        List<String> l=new ArrayList<>();
        try (PreparedStatement ps=koneksi.prepareStatement("select distinct kd_poli_bpjs from maping_poli_bpjs where kd_poli_bpjs<>'' order by kd_poli_bpjs");
             ResultSet rs=ps.executeQuery()) {
            while(rs.next()) l.add(rs.getString(1));
        }
        return l;
    }

    /** panggil HFIS untuk 1 poli & 1 tanggal */
    public JsonNode ambil(String kdPoliBpjs,LocalDate tgl) throws Exception {
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-cons-id",koneksiDB.CONSIDAPIMOBILEJKN());
        String utc=String.valueOf(api.GetUTCdatetimeAsString());
        headers.add("x-timestamp",utc);
        headers.add("x-signature",api.getHmac(utc));
        headers.add("user_key",koneksiDB.USERKEYAPIMOBILEJKN());
        String url=koneksiDB.URLAPIMOBILEJKN()+"/jadwaldokter/kodepoli/"+kdPoliBpjs+"/tanggal/"+tgl;
        JsonNode root=mapper.readTree(api.getRest().exchange(url,HttpMethod.GET,new HttpEntity(headers),String.class).getBody());
        JsonNode meta=root.path("metadata");
        if(meta.path("code").asText().equals("200")){
            return mapper.readTree(api.Decrypt(root.path("response").asText(),utc));
        }
        // 201 = data tidak ada untuk poli/tanggal tsb -> bukan galat
        if(!meta.path("code").asText().equals("201")){
            galat.add(kdPoliBpjs+" "+tgl+" : "+meta.path("message").asText());
        }
        return null;
    }

    public interface Progres { void lapor(String teks); }

    /** ambil 7 hari ke depan untuk semua poli BPJS lalu bandingkan dengan tabel jadwal */
    public List<Baris> siapkan(Progres p) throws Exception {
        galat.clear();
        Map<String,String[]> dokter=new HashMap<>();     // kd_dokter_bpjs -> {kd_dokter, nm_dokter}
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select m.kd_dokter_bpjs,m.kd_dokter,d.nm_dokter from maping_dokter_dpjpvclaim m inner join dokter d on m.kd_dokter=d.kd_dokter where m.kd_dokter_bpjs<>''");
             ResultSet rs=ps.executeQuery()) {
            while(rs.next()) dokter.put(rs.getString(1).trim(),new String[]{rs.getString(2),rs.getString(3)});
        }
        Map<String,String[]> poli=new HashMap<>();       // kd_poli_bpjs -> {kd_poli, nm_poli}
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select m.kd_poli_bpjs,m.kd_poli_rs,p.nm_poli from maping_poli_bpjs m inner join poliklinik p on m.kd_poli_rs=p.kd_poli");
             ResultSet rs=ps.executeQuery()) {
            while(rs.next()) poli.put(rs.getString(1).trim().toUpperCase(),new String[]{rs.getString(2),rs.getString(3)});
        }

        // hasil HFIS, kunci dokterBpjs|subspesialis|hari|jam agar tidak dobel antar poli/tanggal
        Map<String,Baris> hfis=new LinkedHashMap<>();
        List<String> daftarPoli=poliBpjs();
        LocalDate awal=LocalDate.now();
        int n=0;
        for(String kp:daftarPoli){
            for(int d=0;d<7;d++){
                LocalDate tgl=awal.plusDays(d);
                n++;
                if(p!=null) p.lapor("Mengambil HFIS poli "+kp+" ("+n+"/"+(daftarPoli.size()*7)+")");
                JsonNode res;
                try {
                    res=ambil(kp,tgl);
                } catch (Exception e) {
                    galat.add(kp+" "+tgl+" : "+e.getMessage());
                    continue;
                }
                if(res==null||!res.isArray()) continue;
                for(JsonNode j:res){
                    Baris b=new Baris();
                    b.kdDokterBpjs=j.path("kodedokter").asText().trim();
                    b.nmDokterBpjs=j.path("namadokter").asText().trim();
                    String sub=j.path("kodesubspesialis").asText().trim().toUpperCase();
                    String pol=j.path("kodepoli").asText().trim().toUpperCase();
                    b.kdPoliBpjs=sub.equals("")?pol:sub;
                    b.nmPoliBpjs=j.path("namasubspesialis").asText();
                    if(b.nmPoliBpjs.equals("")) b.nmPoliBpjs=j.path("namapoli").asText();
                    int h=j.path("hari").asInt(0);
                    b.hari=(h>=1&&h<=7)?HARI[h]:j.path("namahari").asText().toUpperCase().replace("MINGGU","AKHAD");
                    b.libur=j.path("libur").asText().equals("1");
                    b.kuota=j.path("kapasitaspasien").asInt(0);
                    String jam=j.path("jadwal").asText().replace(" ","");
                    String[] jj=jam.split("-");
                    String kunci=b.kdDokterBpjs+"|"+b.kdPoliBpjs+"|"+b.hari+"|"+jam;
                    if(hfis.containsKey(kunci)){
                        Baris ada=hfis.get(kunci);
                        if(!b.libur) ada.libur=false;
                        else if(!ada.tglLibur.contains(tgl)) ada.tglLibur.add(tgl);
                        continue;
                    }
                    if(b.libur) b.tglLibur.add(tgl);
                    if(jj.length==2&&jj[0].matches("\\d{1,2}:\\d{2}")&&jj[1].matches("\\d{1,2}:\\d{2}")){
                        b.jamMulai=jam(jj[0]);
                        b.jamSelesai=jam(jj[1]);
                    }else{
                        b.status=ST_FORMAT;
                        b.ket="jadwal HFIS: "+jam;
                    }
                    String[] dk=dokter.get(b.kdDokterBpjs);
                    if(dk!=null){ b.kdDokter=dk[0]; b.nmDokter=dk[1]; }
                    String[] pl=poli.get(b.kdPoliBpjs);
                    if(pl==null) pl=poli.get(pol);
                    if(pl!=null){ b.kdPoli=pl[0]; b.nmPoli=pl[1]; }
                    if(b.status.equals("")){
                        if(dk==null) b.status=ST_DOKTER;
                        else if(pl==null) b.status=ST_POLI;
                    }
                    hfis.put(kunci,b);
                }
            }
        }
        java.time.format.DateTimeFormatter fmt=java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for(Baris b:hfis.values()){
            if(b.tglLibur.isEmpty()) continue;
            b.tglLibur.sort(null);
            StringBuilder t=new StringBuilder();
            for(LocalDate d:b.tglLibur) t.append(t.length()==0?"":", ").append(d.format(fmt));
            b.ket=b.ket.equals("")?"LIBUR "+t:b.ket+"; LIBUR "+t;
        }

        // bandingkan dengan jadwal Khanza
        List<Baris> hasil=new ArrayList<>(hfis.values());
        Set<String> pasangan=new LinkedHashSet<>();                 // kd_dokter|kd_poli yang dikelola HFIS
        Set<String> kunciHfis=new LinkedHashSet<>();                // kd_dokter|hari|jam_mulai
        for(Baris b:hasil){
            if(!b.status.equals("")) continue;
            String pk=b.kdDokter+"|"+b.hari+"|"+b.jamMulai;
            if(!kunciHfis.add(pk)){
                b.status=ST_BENTROK;
                b.ket="dokter sudah punya jadwal HFIS lain di hari & jam mulai yang sama";
                continue;
            }
            pasangan.add(b.kdDokter+"|"+b.kdPoli);
        }
        Map<String,String[]> lama=new HashMap<>();   // kd_dokter|hari|jam_mulai -> {kd_poli, jam_selesai, kuota}
        List<Baris> dihapus=new ArrayList<>();
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select j.kd_dokter,d.nm_dokter,j.hari_kerja,time_format(j.jam_mulai,'%H:%i:%s'),time_format(j.jam_selesai,'%H:%i:%s'),"+
                "ifnull(j.kd_poli,''),ifnull(p.nm_poli,''),ifnull(j.kuota,0) from jadwal j "+
                "left join dokter d on j.kd_dokter=d.kd_dokter left join poliklinik p on j.kd_poli=p.kd_poli");
             ResultSet rs=ps.executeQuery()) {
            while(rs.next()){
                String pk=rs.getString(1)+"|"+rs.getString(3)+"|"+rs.getString(4);
                lama.put(pk,new String[]{rs.getString(6),rs.getString(5),rs.getString(8)});
                if(pasangan.contains(rs.getString(1)+"|"+rs.getString(6))&&!kunciHfis.contains(pk)){
                    Baris b=new Baris();
                    b.status=ST_HAPUS;
                    b.kdDokter=rs.getString(1);b.nmDokter=rs.getString(2);
                    b.hari=rs.getString(3);b.jamMulai=rs.getString(4);b.jamSelesai=rs.getString(5);
                    b.kdPoli=rs.getString(6);b.nmPoli=rs.getString(7);b.kuota=rs.getInt(8);
                    b.ket="ada di Khanza, tidak ada di HFIS";
                    dihapus.add(b);
                }
            }
        }
        for(Baris b:hasil){
            if(!b.status.equals("")) continue;
            String[] l=lama.get(b.kdDokter+"|"+b.hari+"|"+b.jamMulai);
            if(l==null){
                b.status=ST_BARU;
            }else if(!l[0].equals(b.kdPoli)&&!pasangan.contains(b.kdDokter+"|"+l[0])){
                // slot dipakai poli lain yang tidak dikelola HFIS
                b.status=ST_BENTROK;
                b.ket="hari & jam mulai sudah dipakai di poli "+l[0]+" (bukan dari HFIS)";
            }else if(l[0].equals(b.kdPoli)&&l[1].equals(b.jamSelesai)&&Integer.parseInt(l[2])==b.kuota){
                b.status=ST_SAMA;
            }else{
                b.status=ST_UBAH;
                String seb="sebelumnya "+l[0]+" s.d. "+l[1]+" kuota "+l[2];
                b.ket=b.ket.equals("")?seb:seb+"; "+b.ket;
            }
        }
        hasil.addAll(dihapus);
        hasil.sort((a,b)->{
            int c=urutStatus(a.status)-urutStatus(b.status);
            if(c!=0) return c;
            c=(a.nmDokter.equals("")?a.nmDokterBpjs:a.nmDokter).compareToIgnoreCase(b.nmDokter.equals("")?b.nmDokterBpjs:b.nmDokter);
            if(c!=0) return c;
            return urutHari(a.hari)-urutHari(b.hari);
        });
        return hasil;
    }

    /** simpan: hanya baris Baru/Ubah/Dihapus yang disentuh, baris Sama dibiarkan (1 transaksi) */
    public int simpan(List<Baris> data) throws Exception {
        boolean auto=koneksi.getAutoCommit();
        int jml=0;
        try {
            koneksi.setAutoCommit(false);
            try (PreparedStatement ps=koneksi.prepareStatement("delete from jadwal where kd_dokter=? and hari_kerja=? and jam_mulai=? and kd_poli=?")) {
                for(Baris b:data){
                    if(!b.status.equals(ST_HAPUS)) continue;
                    ps.setString(1,b.kdDokter);ps.setString(2,b.hari);ps.setString(3,b.jamMulai);ps.setString(4,b.kdPoli);
                    jml+=ps.executeUpdate();
                }
            }
            try (PreparedStatement ps=koneksi.prepareStatement(
                    "insert into jadwal(kd_dokter,hari_kerja,jam_mulai,jam_selesai,kd_poli,kuota) values(?,?,?,?,?,?) "+
                    "on duplicate key update jam_selesai=values(jam_selesai),kd_poli=values(kd_poli),kuota=values(kuota)")) {
                for(Baris b:data){
                    if(!(b.status.equals(ST_BARU)||b.status.equals(ST_UBAH))) continue;
                    ps.setString(1,b.kdDokter);ps.setString(2,b.hari);ps.setString(3,b.jamMulai);
                    ps.setString(4,b.jamSelesai);ps.setString(5,b.kdPoli);ps.setInt(6,b.kuota);
                    ps.executeUpdate();
                    jml++;
                }
            }
            koneksi.commit();
        } catch (Exception e) {
            koneksi.rollback();
            throw e;
        } finally {
            koneksi.setAutoCommit(auto);
        }
        return jml;
    }

    private static String jam(String hhmm){
        String[] s=hhmm.split(":");
        return String.format("%02d:%s:00",Integer.parseInt(s[0]),s[1]);
    }

    private static int urutStatus(String s){
        switch(s){
            case ST_BARU: return 0;
            case ST_UBAH: return 1;
            case ST_HAPUS: return 2;
            case ST_BENTROK: return 3;
            case ST_DOKTER: return 4;
            case ST_POLI: return 5;
            case ST_FORMAT: return 6;
            default: return 7;
        }
    }

    private static int urutHari(String h){
        for(int i=1;i<HARI.length;i++) if(HARI[i].equals(h)) return i;
        return 9;
    }
}
