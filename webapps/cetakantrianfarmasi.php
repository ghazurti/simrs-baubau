<?php
 session_start();
 require_once('conf/conf.php');
 header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
 header("Cache-Control: no-store, no-cache, must-revalidate");
 header("Pragma: no-cache");
 date_default_timezone_set("Asia/Bangkok");
?>
<!doctype html>
<html lang="id">
<head>
    <title>Cetak No. Antrian Farmasi</title>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <style>
        *{box-sizing:border-box;}
        body{font-family:Arial,Helvetica,sans-serif;margin:0;background:#f4f4f4;color:#222;}
        header{background:#e64a19;color:#fff;padding:12px 16px;}
        header h3{margin:0;font-size:20px;}
        .wrap{padding:14px 16px;}
        .cari{width:100%;max-width:420px;padding:10px 12px;font-size:16px;border:1px solid #bbb;border-radius:6px;margin-bottom:12px;}
        table{width:100%;border-collapse:collapse;background:#fff;}
        th,td{padding:8px 10px;border-bottom:1px solid #eee;text-align:left;font-size:15px;}
        th{background:#fafafa;}
        .noantri{font-weight:bold;font-size:16px;padding:2px 8px;border-radius:4px;color:#fff;display:inline-block;min-width:60px;text-align:center;}
        .lane-R{background:#e64a19;}   /* racikan */
        .lane-N{background:#00695c;}   /* non racikan */
        .btn-cetak{background:#1565c0;color:#fff;border:none;padding:7px 14px;border-radius:5px;font-size:14px;cursor:pointer;}
        .btn-cetak:hover{background:#0d47a1;}
        .kosong{padding:20px;color:#777;}
    </style>
</head>
<body>
    <header><h3>🖨 Cetak Nomor Antrian Farmasi &mdash; <?php echo date("d-m-Y"); ?></h3></header>
    <div class="wrap">
        <input type="text" id="cari" class="cari" placeholder="Cari nama pasien / no. rawat / nomor antrian..." onkeyup="filter()">
        <table id="tbl">
            <thead>
                <tr><th>No. Antrian</th><th>Nama Pasien</th><th>Poli</th><th>Jam</th><th>Jenis</th><th></th></tr>
            </thead>
            <tbody>
            <?php
              // Ambil semua resep rawat jalan (non-IGD) hari ini, urut jam peresepan -> untuk penomoran per jalur.
              $sql="select ro.no_resep, ro.no_rawat, ro.jam_peresepan, p.nm_pasien, ifnull(pk.nm_poli,'-') as nm_poli,
                    (ro.no_resep in(select distinct no_resep from resep_dokter_racikan)) as racikan
                    from resep_obat ro
                    inner join reg_periksa rp on ro.no_rawat=rp.no_rawat
                    inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis
                    left join poliklinik pk on rp.kd_poli=pk.kd_poli
                    where ro.status='ralan' and rp.kd_poli<>'IGDK' and ro.jam_peresepan<>'00:00:00'
                    and ro.tgl_peresepan=CURDATE()
                    order by ro.jam_peresepan asc, ro.no_resep asc";
              $hasil=bukaquery($sql);
              $rows=array(); $nR=0; $nN=0;
              while($d=mysqli_fetch_array($hasil)){
                  if($d['racikan']){ $nR++; $j='R'; $n=$nR; } else { $nN++; $j='N'; $n=$nN; }
                  $d['j']=$j; $d['n']=$n; $d['noantri']=sprintf("%s-%03d",$j,$n);
                  $rows[]=$d;
              }
              // Tampilkan terbaru di atas.
              $rows=array_reverse($rows);
              if(count($rows)==0){
                  echo "<tr><td colspan='6' class='kosong'>Belum ada resep rawat jalan hari ini.</td></tr>";
              }
              foreach($rows as $d){
                  echo "<tr>
                        <td><span class='noantri lane-".$d['j']."'>".$d['noantri']."</span></td>
                        <td>".$d['nm_pasien']."</td>
                        <td>".$d['nm_poli']."</td>
                        <td>".substr($d['jam_peresepan'],0,5)."</td>
                        <td>".($d['racikan']?"Racikan":"Non-Racikan")."</td>
                        <td><button class='btn-cetak' onclick=\"cetak('".$d['j']."',".$d['n'].")\">🖨 Cetak</button></td>
                      </tr>";
              }
            ?>
            </tbody>
        </table>
    </div>
    <script>
        function cetak(j,n){
            window.open('strukantrianfarmasi.php?j='+j+'&n='+n,'cetak','width=360,height=520');
        }
        function filter(){
            var q=document.getElementById('cari').value.toLowerCase();
            var tr=document.querySelectorAll('#tbl tbody tr');
            tr.forEach(function(r){
                r.style.display = r.innerText.toLowerCase().indexOf(q)>-1 ? '' : 'none';
            });
        }
    </script>
</body>
</html>
