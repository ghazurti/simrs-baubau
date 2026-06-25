<?php
 //fitur update kamar aplicare ini adalah penyempurnaan dari kontribusi Mas Tirta dari RSUK Ciracas Jakarta Timur
 session_start();
 require_once('conf/conf.php');
 header("Expires: Mon, 26 Jul 1997 05:00:00 GMT"); 
 header("Last-Modified: ".gmdate("D, d M Y H:i:s")." GMT"); 
 header("Cache-Control: no-store, no-cache, must-revalidate"); 
 header("Cache-Control: post-check=0, pre-check=0", false);
 header("Pragma: no-cache"); // HTTP/1.0
 date_default_timezone_set("Asia/Bangkok");
 $tanggal= mktime(date("m"),date("d"),date("Y"));
 $jam=date("H:i");
?>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link href="css/default.css" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="conf/validator.js"></script>
    <meta http-equiv="refresh" content="20"/>
    <title>Informasi Ketersediaan Kamar</title>
    <script src="Scripts/AC_RunActiveContent.js" type="text/javascript"></script>
    <script src="Scripts/AC_ActiveX.js" type="text/javascript"></script>
	<style type="text/css">
		body {
			margin: 0;
			font-family: Tahoma, Arial, sans-serif;
			background: linear-gradient(180deg, #f8f9ff 0%, #eef3ff 100%);
			color: #20304a;
		}
		.wrapper {
			max-width: 1600px;
			margin: 0 auto;
			padding: 18px 20px 24px;
		}
		.header-card {
			background: #ffffff;
			border: 1px solid #d8e1f2;
			border-radius: 18px;
			box-shadow: 0 10px 30px rgba(35, 56, 99, 0.08);
			padding: 16px 18px;
			margin-bottom: 16px;
		}
		.header-grid {
			display: table;
			width: 100%;
		}
		.header-logo, .header-title, .header-right {
			display: table-cell;
			vertical-align: middle;
		}
		.header-logo { width: 100px; }
		.header-right { width: 180px; text-align: right; }
		.title-main {
			font-size: 30px;
			font-weight: 800;
			color: #7a1fa2;
			line-height: 1.1;
		}
		.title-sub {
			font-size: 16px;
			color: #5b6b84;
			margin-top: 6px;
		}
		.title-meta {
			display: inline-block;
			margin-top: 8px;
			padding: 8px 12px;
			border-radius: 999px;
			background: #fff3c4;
			color: #8a6d00;
			font-size: 15px;
			font-weight: 800;
		}
		.table-card {
			background: #ffffff;
			border: 1px solid #d8e1f2;
			border-radius: 18px;
			box-shadow: 0 10px 30px rgba(35, 56, 99, 0.08);
			overflow: hidden;
		}
		.room-table {
			width: 100%;
			border-collapse: collapse;
		}
		.room-table thead th {
			background: linear-gradient(90deg, #7a1fa2 0%, #b23dbd 100%);
			color: #fff;
			padding: 14px 12px;
			font-size: 18px;
			letter-spacing: .4px;
		}
		.room-table tbody td {
			padding: 14px 12px;
			border-bottom: 1px solid #e8eef8;
			font-size: 18px;
		}
		.room-table tbody tr:nth-child(even) { background: #f7faff; }
		.room-table tbody tr:hover { background: #eef6ff; }
		.room-name { font-weight: 800; color: #5f1786; }
		.num-total { color: #d32f2f; font-size: 24px; font-weight: 800; }
		.num-filled { color: #c79200; font-size: 24px; font-weight: 800; }
		.num-empty { color: #1b8f4b; font-size: 24px; font-weight: 800; }
		.tarif-cell { padding: 10px 14px !important; }
		.tarif-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 10px;
			padding: 4px 0;
		}
		.tarif-row + .tarif-row { border-top: 1px dashed #e1e8f5; }
		.kelas-tag {
			display: inline-block;
			padding: 4px 10px;
			border-radius: 999px;
			background: #f1e8f8;
			color: #5f1786;
			font-size: 14px;
			font-weight: 700;
			white-space: nowrap;
		}
		.harga {
			color: #1b3a73;
			font-weight: 800;
			font-size: 16px;
			text-align: right;
		}
		.harga-na { color: #98a2b3; font-style: italic; }
		.footer-bar {
			margin-top: 0;
			border-radius: 14px;
			overflow: hidden;
			box-shadow: 0 8px 22px rgba(35, 56, 99, 0.08);
		}
		.table-card { margin-bottom: 0; }

			.title-main { font-size: 24px; }
			.title-sub { font-size: 14px; }
			.title-meta { font-size: 13px; }
			.room-table thead th { font-size: 16px; padding: 12px 10px; }
			.room-table tbody td { font-size: 16px; padding: 12px 10px; }
			.num-total, .num-filled, .num-empty { font-size: 16px; }
		}
	</style>
</head>
<body>

<div class="wrapper">
	<div align="left">
		<script type="text/javascript">
			AC_AX_RunContent( 'width','32','height','32' ); //end AC code
		</script>
		<noscript>
		   <object width="32" height="32">
		     <embed width="32" height="32"></embed>
		   </object>
		 </noscript>
	</div>
	<?php
		$setting=  mysqli_fetch_array(bukaquery("select setting.nama_instansi,setting.alamat_instansi,setting.kabupaten,setting.propinsi,setting.kontak,setting.email,setting.logo from setting"));
		echo "<div class='header-card'>
			<div class='header-grid'>
				<div class='header-logo' align='center'>
					<img width='96' height='96' style='border-radius:16px;object-fit:cover;' src='data:image/jpeg;base64,". base64_encode($setting['logo']). "'/>
				</div>
				<div class='header-title' align='center'>
					<div class='title-main'>".$setting["nama_instansi"]."</div>
					<div class='title-sub'>".$setting["alamat_instansi"].", ".$setting["kabupaten"].", ".$setting["propinsi"]."</div>
					<div class='title-meta'>".date("d-M-Y", $tanggal)." &nbsp; ". $jam."</div>
				</div>
				<div class='header-right'>
					<img width='180' height='130' style='border-radius:14px;object-fit:cover;' src='header-kanan.jpg'/>
				</div>
			</div>
		</div>";
	?>
	<div class="table-card">
	<table class='room-table'>
	     <thead>
	     <tr>
              <th width='34%'>NAMA KAMAR</th>
              <th width='14%'>JUMLAH BED</th>
              <th width='14%'>BED TERISI</th>
              <th width='14%'>BED KOSONG</th>
              <th width='24%'>TARIF / HARI</th>
         </tr>
	     </thead>
	     <tbody>

	<?php  
		$_sql="Select * From bangsal where status='1' and kd_bangsal in(select kd_bangsal from kamar)" ;  
		$hasil=bukaquery($_sql);

		while ($data = mysqli_fetch_array ($hasil)){
			$total = mysqli_fetch_array(bukaquery("select count(kd_bangsal) from kamar where kamar.statusdata='1' and kd_bangsal='".$data['kd_bangsal']."'"))[0];
			$isi = mysqli_fetch_array(bukaquery("select count(kd_bangsal) from kamar where kamar.statusdata='1' and kd_bangsal='".$data['kd_bangsal']."' and status='ISI'"))[0];
			$kosong = mysqli_fetch_array(bukaquery("select count(kd_bangsal) from kamar where kamar.statusdata='1' and kd_bangsal='".$data['kd_bangsal']."' and status='KOSONG'"))[0];

			$tarifRows = bukaquery("select kelas, min(trf_kamar) as tmin, max(trf_kamar) as tmax from kamar where statusdata='1' and trf_kamar is not null and trf_kamar>0 and kd_bangsal='".$data['kd_bangsal']."' group by kelas order by tmin asc");
			$tarifHtml = "";
			while($t = mysqli_fetch_array($tarifRows)){
				$kelasNm = $t['kelas'] ?: '-';
				if($t['tmin'] == $t['tmax']){
					$harga = "Rp ".number_format($t['tmin'],0,',','.');
				} else {
					$harga = "Rp ".number_format($t['tmin'],0,',','.')." - ".number_format($t['tmax'],0,',','.');
				}
				$tarifHtml .= "<div class='tarif-row'><span class='kelas-tag'>".$kelasNm."</span><span class='harga'>".$harga."</span></div>";
			}
			if($tarifHtml === ""){ $tarifHtml = "<span class='harga-na'>-</span>"; }

			echo "<tr>
					<td class='room-name'>".$data['nm_bangsal']."</td>
					<td align='center' class='num-total'>".$total."</td>
					<td align='center' class='num-filled'>".$isi."</td>
					<td align='center' class='num-empty'>".$kosong."</td>
					<td class='tarif-cell'>".$tarifHtml."</td>
				</tr> ";
		}
	?>
	     </tbody>
	</table>
	</div>
</div>
</body>
