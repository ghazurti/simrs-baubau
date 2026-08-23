 <?php
 require_once('conf/conf.php');
 header("Expires: Mon, 26 Jul 1997 05:00:00 GMT"); 
 header("Last-Modified: ".gmdate("D, d M Y H:i:s")." GMT"); 
 header("Cache-Control: no-store, no-cache, must-revalidate"); 
 header("Cache-Control: post-check=0, pre-check=0", false);
 header("Pragma: no-cache"); // HTTP/1.0
 date_default_timezone_set("Asia/Bangkok");
 $tanggal= mktime(date("m"),date("d"),date("Y"));
 $jam=date("H:i");

 // Hitung nomor antrian ramah (R-00N) dari no_resep yang dipanggil:
 // urutan resep sejalur (racikan) hari ini berdasarkan jam_peresepan, no_resep.
 function rankFarmasi($noresep,$racikan){
    $filter = $racikan ? "in" : "not in";
    $r=getOne("select count(*) from resep_obat ro
        inner join reg_periksa rp on ro.no_rawat=rp.no_rawat
        where ro.status='ralan' and rp.kd_poli<>'IGDK' and ro.tgl_peresepan=CURDATE() and ro.jam_peresepan<>'00:00:00'
        and ro.no_resep ".$filter." (select distinct no_resep from resep_dokter_racikan)
        and ( ro.jam_peresepan < (select jam_peresepan from resep_obat where no_resep='".$noresep."')
           or (ro.jam_peresepan = (select jam_peresepan from resep_obat where no_resep='".$noresep."') and ro.no_resep <= '".$noresep."') )");
    return intval($r);
 }
 // Samarkan nama: sisakan 3 huruf pertama tiap kata, sisanya jadi '*'.
 function samarkan($nama){
    $kata=explode(' ', trim($nama));
    foreach($kata as $i=>$w){
        $len=mb_strlen($w);
        if($len<=3){ continue; }
        $kata[$i]=mb_substr($w,0,3).str_repeat('*',$len-3);
    }
    return implode(' ', $kata);
 }
 function infoPasienPoli($norawat){
    $x=getOne("select concat(pasien.nm_pasien,'|',ifnull(poliklinik.nm_poli,'-')) from reg_periksa
        inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis
        left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli
        where reg_periksa.no_rawat='".$norawat."'");
    return explode('|',$x);
 }
?>
 
<div class="row">
    <div class="col s12" id="header-instansi">
        <div class="card deep-orange accent-3 white-text">
            <div class="card-content">
                <h5>
                <table border='0' witdh='100%'>
                    <tr border='0'>
                        <td>Panggilan Validasi Resep</td><td>:</td>
                        <td>
                        <?php 
                            $_sql="select a.* from antriapotek2 a
                                    inner join resep_obat ro on a.no_resep=ro.no_resep
                                    inner join reg_periksa rp on a.no_rawat=rp.no_rawat
                                    where a.no_resep in(select distinct resep_dokter_racikan.no_resep from resep_dokter_racikan)
                                    and rp.kd_poli<>'IGDK' and ro.tgl_peresepan=CURDATE()" ;
                            $hasil=bukaquery($_sql);
                            while ($data = mysqli_fetch_array ($hasil)){
                                $rank=rankFarmasi($data['no_resep'],true);
                                $noantri=$rank>0?sprintf("R-%03d",$rank):$data['no_resep'];
                                list($nmpsn,$nmpoli)=infoPasienPoli($data['no_rawat']);
                                echo "<b style='font-size:1.4em'>".$noantri."</b> &mdash; ".samarkan($nmpsn)." (".$nmpoli.")";
                                if($data['status']=="1"){
                                    // Validasi: hanya tampil di layar, TIDAK berbunyi. Suara hanya saat penyerahan.
                                    bukaquery2("update antriapotek2 set antriapotek2.status='0'");
                                }
                            }
                        ?>
                        </td>
                    </tr>
                    <tr border='0'>
                        <td>Panggilan Penyerahan Resep</td><td>:</td>
                        <td>
                        <?php 
                            $_sql="select a.* from antriapotek3 a
                                    inner join resep_obat ro on a.no_resep=ro.no_resep
                                    inner join reg_periksa rp on a.no_rawat=rp.no_rawat
                                    where a.no_resep in(select distinct resep_dokter_racikan.no_resep from resep_dokter_racikan)
                                    and rp.kd_poli<>'IGDK' and ro.tgl_peresepan=CURDATE()" ;
                            $hasil=bukaquery($_sql);
                            while ($data = mysqli_fetch_array ($hasil)){
                                $rank=rankFarmasi($data['no_resep'],true);
                                $noantri=$rank>0?sprintf("R-%03d",$rank):$data['no_resep'];
                                list($nmpsn,$nmpoli)=infoPasienPoli($data['no_rawat']);
                                echo "<b style='font-size:1.4em'>".$noantri."</b> &mdash; ".samarkan($nmpsn)." (".$nmpoli.")";
                                if($data['status']=="1"){
                                    echo "<audio autoplay='true' src='bell2.wav'>";
                                    $ucap="Nomor antrian racikan nomor ".$rank.", ".$nmpsn.", silakan mengambil obat di loket penyerahan";
                                    echo "<script>if(window.bilang)bilang('".addslashes($ucap)."');</script>";
                                    bukaquery2("update antriapotek3 set antriapotek3.status='0'");
                                }
                            }
                        ?>
                        </td>
                    </tr>
                </table>    
                </h5>
            </div>
        </div>
    </div>
</div>