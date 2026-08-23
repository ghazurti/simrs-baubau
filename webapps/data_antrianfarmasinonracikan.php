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
?>
 <div class="col s12 row">
    <div class="col s12">
        <table class="default">
            <thead>
               <tr class='head4'>
                    <td><b>No. Antrian</b></td>
                    <td><b>No.Resep</b></td>
                    <td><b>No.Rawat</b></td>
                    <td><b>Nama Pasien</b></td>
                    <td><b>Poli</b></td>
                    <td><b>Dokter Peresep</b></td>
                    <td><b>Peresepan</b></td>
                    <td><b>Validasi</b></td>
                    <td><b>Penyerahan</b></td>
               </tr>
            </thead>
            <tbody>
            <?php  
              $_sql="select resep_obat.no_resep,resep_obat.no_rawat,pasien.nm_pasien,resep_obat.jam_peresepan,
                    if(resep_obat.jam='00:00:00','',resep_obat.jam) as jam_validasi,
                    if(resep_obat.jam_penyerahan='00:00:00','',resep_obat.jam_penyerahan) as jam_penyerahan,dokter.nm_dokter,
                    ifnull(poliklinik.nm_poli,'-') as nm_poli
                    from resep_obat inner join reg_periksa on resep_obat.no_rawat=reg_periksa.no_rawat
                    inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis
                    inner join dokter on resep_obat.kd_dokter=dokter.kd_dokter
                    left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli
                    where resep_obat.no_resep not in(select distinct resep_dokter_racikan.no_resep from resep_dokter_racikan)
                    and resep_obat.jam_peresepan<>'00:00:00' and resep_obat.status='ralan'
                    and reg_periksa.kd_poli<>'IGDK'
                    and resep_obat.tgl_peresepan='".date("Y-m-d", $tanggal)."' order by resep_obat.jam_peresepan asc, resep_obat.no_resep asc" ;
              $hasil=bukaquery($_sql);
              $no=0;
              while ($data = mysqli_fetch_array ($hasil)){
                $no++;
                $noantri=sprintf("N-%03d",$no);
                echo "<tr class='isi7' >
                          <td><b>".$noantri."</b></td>
                          <td>".$data['no_resep']."</td>
                          <td>".$data['no_rawat']."</td>
                          <td>".samarkan($data['nm_pasien'])."</td>
                          <td>".$data['nm_poli']."</td>
                          <td>".$data['nm_dokter']."</td>
                          <td>".$data['jam_peresepan']."</td>
                          <td>".$data['jam_validasi']."</td>
                          <td>".$data['jam_penyerahan']."</td>
                      </tr> ";
                }
            ?>
            </tbody>
        </table>
    </div>
</div>