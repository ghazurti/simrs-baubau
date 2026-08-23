/*
 * Helper cetak Formulir Surgical Safety Checklist (RM 22.OK-03) gabungan 1 lembar
 * (Sign In + Time Out + Sign Out) untuk satu no_rawat.
 */
package rekammedis;

import fungsi.akses;
import fungsi.sekuel;
import fungsi.validasi;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

public class CetakSurgicalSafetyChecklist {

    public static void cetak(String norawat){
        if(norawat==null || norawat.trim().isEmpty()){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih dulu pasien yang mau dicetak...!!");
            return;
        }
        sekuel Sequel=new sekuel();
        validasi Valid=new validasi();
        Map<String,Object> param=new HashMap<>();
        param.put("namars",akses.getnamars());
        param.put("alamatrs",akses.getalamatrs());
        param.put("kotars",akses.getkabupatenrs());
        param.put("propinsirs",akses.getpropinsirs());
        param.put("logo",Sequel.cariGambar("select setting.logo from setting"));
        Valid.MyReportqry("rptSurgicalSafetyChecklist.jasper","report","::[ Surgical Safety Checklist (RM 22.OK-03) ]::",
            "select "+
            "reg_periksa.no_rawat as no_rawat, "+
            "pasien.no_rkm_medis as no_rkm_medis, "+
            "pasien.nm_pasien as nm_pasien, "+
            "date_format(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir, "+
            "pasien.jk as jk, "+
            "coalesce(so.tindakan,ti.tindakan,si.tindakan) as tindakan, "+
            "date_format(coalesce(so.tanggal,ti.tanggal,si.tanggal),'%d-%m-%Y %H:%i') as tanggal, "+
            "(select nm_dokter from dokter where kd_dokter=coalesce(si.kd_dokter_bedah,ti.kd_dokter_bedah,so.kd_dokter_bedah)) as dokterbedah, "+
            "(select nm_dokter from dokter where kd_dokter=coalesce(si.kd_dokter_anestesi,ti.kd_dokter_anestesi,so.kd_dokter_anestesi)) as dokteranestesi, "+
            "si.identitas as si_identitas, "+
            "si.persetujuan_penandaan as si_p_penandaan, "+
            "si.persetujuan_bedah as si_p_bedah, "+
            "si.persetujuan_anestesi as si_p_anestesi, "+
            "si.penandaan_area_operasi as si_area, "+
            "si.kesiapan_alat_obat_anestesi as si_alat, "+
            "si.alergi as si_alergi, "+
            "si.resiko_aspirasi as si_aspirasi, "+
            "si.resiko_kehilangan_darah as si_darah, "+
            "si.resiko_kehilangan_darah_line as si_line, "+
            "si.hasil_radiologi as si_radiologi, "+
            "date_format(si.tanggal,'%H:%i') as si_jam, "+
            "(select nama from petugas where nip=si.nip_perawat_ok) as si_perawat, "+
            "ti.konfirmasi_tim as to_konfirmasi, "+
            "ti.verbal_identitas as to_identitas, "+
            "ti.verbal_tindakan as to_tindakan, "+
            "ti.posisi_pasien as to_posisi, "+
            "ti.lama_operasi as to_lama, "+
            "ti.antisipasi_kehilangan_darah as to_darah, "+
            "ti.hal_khusus as to_khusus, "+
            "ti.hal_khusus_diperhatikan as to_khusus_ket, "+
            "ti.alat_khusus as to_alat, "+
            "ti.perhatian_anestesi as to_anestesi, "+
            "ti.petujuk_sterilisasi as to_steril, "+
            "ti.antibiotik_profilaks as to_antibiotik, "+
            "ti.nama_antibiotik as to_antibiotik_nm, "+
            "ti.berdoa as to_berdoa, "+
            "ti.jam_dimulai as to_jam, "+
            "so.verbal_tindakan as so_tindakan, "+
            "so.verbal_kelengkapan_kasa as so_kasa, "+
            "so.verbal_instrumen as so_instrumen, "+
            "so.verbal_alat_tajam as so_tajam, "+
            "so.kelengkapan_specimen_label as so_label, "+
            "so.kelengkapan_specimen_formulir as so_formulir, "+
            "so.masalah_peralatan as so_masalah, "+
            "so.komplikasi as so_komplikasi, "+
            "so.komplikasi_ket as so_komplikasi_ket, "+
            "so.perhatian_utama_fase_pemulihan as so_pemulihan_op, "+
            "so.perhatian_pemulihan_anestesi as so_pemulihan_an, "+
            "so.disposisi as so_disposisi, "+
            "(select nama from petugas where nip=so.nip_perawat_ok) as so_perawat, "+
            "(select nama from petugas where nip=so.nip_perawat_instrumen) as so_instrumen_nm "+
            "from reg_periksa inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
            "left join signin_sebelum_anestesi si on si.no_rawat=reg_periksa.no_rawat "+
            "left join timeout_sebelum_insisi ti on ti.no_rawat=reg_periksa.no_rawat "+
            "left join signout_sebelum_menutup_luka so on so.no_rawat=reg_periksa.no_rawat "+
            "where reg_periksa.no_rawat='"+norawat+"'",param);
    }
}
