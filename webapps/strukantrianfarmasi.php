<?php
 require_once('conf/conf.php');
 header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
 header("Cache-Control: no-store, no-cache, must-revalidate");
 header("Pragma: no-cache");
 date_default_timezone_set("Asia/Bangkok");

 // Acuan pakai nomor antrian R-/N- (bukan no_resep).
 $j = (isset($_GET['j']) && $_GET['j']=='R') ? 'R' : 'N';   // R=racikan, N=non-racikan
 $n = isset($_GET['n']) ? max(1,intval($_GET['n'])) : 1;
 $offset = $n-1;
 $filter = ($j=='R') ? "in" : "not in";

 // Cari resep ke-n pada jalurnya hari ini (urutan sama dgn TV: jam_peresepan, no_resep).
 $sql="select ro.no_rawat, ro.jam_peresepan, p.nm_pasien, ifnull(pk.nm_poli,'-') as poli
       from resep_obat ro
       inner join reg_periksa rp on ro.no_rawat=rp.no_rawat
       inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis
       left join poliklinik pk on rp.kd_poli=pk.kd_poli
       where ro.status='ralan' and rp.kd_poli<>'IGDK' and ro.jam_peresepan<>'00:00:00'
       and ro.tgl_peresepan=CURDATE()
       and ro.no_resep ".$filter." (select distinct no_resep from resep_dokter_racikan)
       order by ro.jam_peresepan asc, ro.no_resep asc
       limit 1 offset ".$offset;
 $hasil=bukaquery($sql);
 $data = $hasil ? mysqli_fetch_array($hasil) : null;

 $noantri = sprintf("%s-%03d",$j,$n);
 $namars  = getOne("select nama_instansi from setting");
 $almtrs  = getOne("select alamat_instansi from setting");
 $kontak  = getOne("select kontak from setting");
 $jenis   = ($j=='R') ? "RACIKAN" : "NON RACIKAN";
 $a_bulan = array(1=>"Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des");
 $tgl     = date("j")." ".$a_bulan[(int)date("n")]." ".date("Y");
 $jamcetak= date("H:i");
?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8" />
<title>Struk Antrian <?php echo $noantri; ?></title>
<style>
    @page { size: 58mm auto; margin: 0; }
    html,body{ margin:0; padding:0; }
    body{ width:58mm; font-family:"Courier New",monospace; color:#000; }
    .struk{ padding:4mm 3mm; text-align:center; }
    .rs{ font-size:12px; font-weight:bold; line-height:1.2; }
    .alamat{ font-size:9px; line-height:1.2; margin-bottom:3px; }
    hr{ border:none; border-top:1px dashed #000; margin:4px 0; }
    .label{ font-size:11px; }
    .jenis{ font-size:13px; font-weight:bold; margin-top:2px; }
    .nomor{ font-size:52px; font-weight:bold; line-height:1; margin:4px 0; }
    .info{ font-size:11px; text-align:left; margin:2px 0; }
    .info b{ display:inline-block; min-width:42px; }
    .tunggu{ font-size:11px; margin-top:6px; }
    .footer{ font-size:9px; margin-top:6px; }
</style>
</head>
<body onload="window.print();">
<div class="struk">
    <div class="rs"><?php echo htmlspecialchars($namars); ?></div>
    <div class="alamat"><?php echo htmlspecialchars($almtrs); ?><br><?php echo htmlspecialchars($kontak); ?></div>
    <hr>
    <div class="label">NOMOR ANTRIAN FARMASI</div>
    <div class="jenis"><?php echo $jenis; ?></div>
    <div class="nomor"><?php echo $noantri; ?></div>
    <hr>
    <?php if($data){ ?>
    <div class="info"><b>Nama</b>: <?php echo htmlspecialchars($data['nm_pasien']); ?></div>
    <div class="info"><b>Poli</b>: <?php echo htmlspecialchars($data['poli']); ?></div>
    <div class="info"><b>No.RW</b>: <?php echo htmlspecialchars($data['no_rawat']); ?></div>
    <div class="info"><b>Jam</b>: <?php echo substr($data['jam_peresepan'],0,5); ?></div>
    <?php } else { ?>
    <div class="info" style="text-align:center;color:#000;">(Data pasien tidak ditemukan)</div>
    <?php } ?>
    <hr>
    <div class="info"><b>Tgl</b>: <?php echo $tgl; ?> &nbsp; <b>Cetak</b>: <?php echo $jamcetak; ?></div>
    <div class="tunggu">Silakan menunggu nomor Anda<br>dipanggil di layar / loket farmasi.</div>
    <div class="footer">Terima kasih &mdash; semoga lekas sembuh</div>
</div>
<script>
    window.onafterprint = function(){ window.close(); };
</script>
</body>
</html>
