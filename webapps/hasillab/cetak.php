<?php
/*
 * Cetak Hasil Pemeriksaan Laboratorium (PK) versi web - setara report Model 1 (rptPeriksaLab / rptPeriksaLabPermintaan).
 * Dipanggil dari DlgCariPeriksaLab: hasillab/cetak.php?norawat=..&tanggal=yyyy-mm-dd&jam=hh:mm:ss&usere=..&passwordte=..
 * Data dibaca langsung dari periksa_lab/detail_periksa_lab (tidak memakai temporary_lab yang dipakai bersama).
 */
include '../conf/conf.php';
include '../phpqrcode/qrlib.php';

$usere      = isset($_GET['usere'])?trim($_GET['usere']):'';
$passwordte = isset($_GET['passwordte'])?trim($_GET['passwordte']):'';
if((USERHYBRIDWEB!=$usere)||(PASHYBRIDWEB!=$passwordte)){
    exit("<h3>Akses ditolak</h3>");
}
$norawat = isset($_GET['norawat'])?trim($_GET['norawat']):'';
$tanggal = isset($_GET['tanggal'])?trim($_GET['tanggal']):'';
$jam     = isset($_GET['jam'])?trim($_GET['jam']):'';
if(!preg_match('/^[0-9\/]{1,17}$/',$norawat)||!preg_match('/^\d{4}-\d{2}-\d{2}$/',$tanggal)||!preg_match('/^\d{2}:\d{2}:\d{2}$/',$jam)){
    exit("<h3>Parameter tidak valid</h3>");
}

mysqli_report(MYSQLI_REPORT_OFF);
$db = bukakoneksi();

function ambil($db,$sql,$tipe='',$args=array()){
    $st = mysqli_prepare($db,$sql);
    if(!$st){ return array(); }
    if($tipe!==''){ mysqli_stmt_bind_param($st,$tipe,...$args); }
    mysqli_stmt_execute($st);
    $rs = mysqli_stmt_get_result($st);
    $data = array();
    while($rs && ($r = mysqli_fetch_assoc($rs))){ $data[] = $r; }
    mysqli_stmt_close($st);
    return $data;
}
function satu($db,$sql,$tipe='',$args=array()){
    $d = ambil($db,$sql,$tipe,$args);
    return count($d)>0?reset($d[0]):'';
}
function h($s){ return htmlspecialchars((string)$s,ENT_QUOTES,'UTF-8'); }
function tglIndo($t){ return $t==''?'':date('d-m-Y',strtotime($t)); }
function kunci($s){ return preg_replace('/\s+/',' ',strtolower(trim((string)$s))); }

function qrBase64($teks){
    $f = tempnam(sys_get_temp_dir(),'qrlab');
    QRcode::png($teks,$f,'H',3,2);
    $b = base64_encode(file_get_contents($f));
    @unlink($f);
    return $b;
}

/* ===== Flag H/L/HH/LL - sama dengan DlgCariPeriksaLab.hitungFlag ===== */
$kritis = array(); $kritisTeks = array();
foreach(ambil($db,"select t.Pemeriksaan,k.batas_bawah,k.batas_atas,k.teks_kritis from set_nilai_kritis_lab k inner join template_laboratorium t on t.id_template=k.id_template") as $r){
    $k = kunci($r['Pemeriksaan']);
    $kritis[$k] = array($r['batas_bawah']===null?null:(float)$r['batas_bawah'],$r['batas_atas']===null?null:(float)$r['batas_atas']);
    if(trim((string)$r['teks_kritis'])!==''){ $kritisTeks[$k]=trim($r['teks_kritis']); }
}

function teksKritis($hasil,$teks){
    if($hasil===null||$teks===null){ return false; }
    $h = strtolower($hasil); $tk = strtolower(trim($teks));
    if($tk===''){ return false; }
    $i = strpos($h,$tk);
    while($i!==false){
        $depan = preg_replace('/[\s\-_.:]+$/','',substr($h,0,$i));
        if(!preg_match('/(?:^|[^\p{L}])(?:non|tidak|tdk|not|no)$/u',$depan)){ return true; }
        $i = strpos($h,$tk,$i+strlen($tk));
    }
    return false;
}
function rentang($s){
    $bawah=null; $atas=null;
    $t = str_replace(',','.',trim((string)$s));
    preg_match_all('/\d+(?:\.\d+)?/',$t,$m);
    $n = count($m[0]);
    if($n>=2){ $a=(float)$m[0][0]; $b=(float)$m[0][1]; $bawah=min($a,$b); $atas=max($a,$b); }
    else if($n==1){
        $a=(float)$m[0][0];
        $hi = strpos($t,'≤')!==false||strpos($t,'<')!==false;
        $lo = strpos($t,'≥')!==false||strpos($t,'>')!==false;
        if($hi&&!$lo){ $atas=$a; } else if($lo&&!$hi){ $bawah=$a; }
    }
    return array($bawah,$atas);
}
function hitungFlag($pemeriksaan,$nilai,$rujukan){
    global $kritis,$kritisTeks;
    if($nilai===null){ return ''; }
    $k = kunci($pemeriksaan);
    $num = str_replace(',','.',trim($nilai));
    $v = preg_match('/^-?\d+(?:\.\d+)?$/',$num)?(float)$num:null;
    if($v!==null && isset($kritis[$k])){
        if($kritis[$k][1]!==null && $v>=$kritis[$k][1]){ return 'HH'; }
        if($kritis[$k][0]!==null && $v<=$kritis[$k][0]){ return 'LL'; }
    }
    if(isset($kritisTeks[$k]) && teksKritis($nilai,$kritisTeks[$k])){ return 'HH'; }
    if($v!==null){
        list($b,$a) = rentang($rujukan);
        if($a!==null && $v>$a){ return 'H'; }
        if($b!==null && $v<$b){ return 'L'; }
    }
    return '';
}
function ketWithFlag($pemeriksaan,$nilai,$rujukan,$ket){
    $ket = (string)$ket;
    $flag = hitungFlag($pemeriksaan,$nilai,$rujukan);
    $kl = strtolower(trim($ket));
    if($flag===''){
        if(preg_match('/^[hlt]/',$kl) && !preg_match('/^[hlt]( |$)/',$kl)){ return ' '.$ket; }
        return $ket;
    }
    $fl = strtolower($flag);
    if(strpos($kl,$fl.' ')===0 || $kl===$fl){ return $ket; }
    return $flag.' '.$ket;
}
/* kelas warna mengikuti printWhen di rptPeriksaLab: HH/LL merah, H/T oranye, L biru */
function kelasFlag($ket){
    $k = strtolower($ket);
    if(strpos($k,'hh')===0||strpos($k,'ll')===0){ return 'kritis'; }
    if($k!==''&&($k[0]=='h'||$k[0]=='t')){ return 'tinggi'; }
    if($k!==''&&$k[0]=='l'){ return 'rendah'; }
    return '';
}

/* ===== Data kepala ===== */
$kepala = ambil($db,
    "select periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.umur,petugas.nama,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.nip,".
    "periksa_lab.dokter_perujuk,periksa_lab.kd_dokter,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat,dokter.nm_dokter ".
    "from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis ".
    "inner join petugas on periksa_lab.nip=petugas.nip inner join dokter on periksa_lab.kd_dokter=dokter.kd_dokter ".
    "inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab ".
    "where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? and periksa_lab.jam=? limit 1",
    'sss',array($norawat,$tanggal,$jam));
if(count($kepala)==0){ exit("<h3>Data hasil laboratorium tidak ditemukan</h3>"); }
$p = $kepala[0];

$setting = ambil($db,"select nama_instansi,alamat_instansi,kabupaten,propinsi,kontak,email,logo from setting limit 1");
$setting = count($setting)>0?$setting[0]:array();

$pengirim = satu($db,"select nm_dokter from dokter where kd_dokter=?",'s',array($p['dokter_perujuk']));
$kdkamar  = satu($db,"select ifnull(kd_kamar,'') from kamar_inap where no_rawat=? order by tgl_masuk desc,jam_masuk desc limit 1",'s',array($norawat));
if($kdkamar!=''){
    $kamar = 'Kamar';
    $namakamar = $kdkamar.', '.satu($db,"select bangsal.nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal where kamar.kd_kamar=?",'s',array($kdkamar));
}else{
    $kamar = 'Poli';
    $namakamar = satu($db,"select poliklinik.nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli where reg_periksa.no_rawat=?",'s',array($norawat));
}
$permintaan = ambil($db,"select noorder,tgl_permintaan,jam_permintaan from permintaan_lab where no_rawat=? and tgl_hasil=? and jam_hasil=? limit 1",'sss',array($norawat,$tanggal,$jam));
$permintaan = count($permintaan)>0?$permintaan[0]:null;

function tandaTangan($db,$setting,$nama,$id,$tgl){
    $sidik = satu($db,"select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",'s',array($id));
    return qrBase64("Dikeluarkan di ".$setting['nama_instansi'].", Kabupaten/Kota ".$setting['kabupaten']."\nDitandatangani secara elektronik oleh ".$nama."\nID ".($sidik==''?$id:$sidik)."\n".tglIndo($tgl));
}
$qrPenjab  = tandaTangan($db,$setting,$p['nm_dokter'],$p['kd_dokter'],$tanggal);
$qrPetugas = tandaTangan($db,$setting,$p['nama'],$p['nip'],$tanggal);

/* ===== Item pemeriksaan ===== */
$catatan = array();
foreach(ambil($db,"select kd_jenis_prw,id_template,catatan from catatan_kritis_lab where no_rawat=? and tgl_periksa=? and jam=?",'sss',array($norawat,$tanggal,$jam)) as $r){
    $catatan[$r['kd_jenis_prw'].'|'.$r['id_template']] = $r['catatan'];
}
$jenis = ambil($db,"select jns_perawatan_lab.kd_jenis_prw,jns_perawatan_lab.nm_perawatan from periksa_lab inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw ".
    "where periksa_lab.kategori='PK' and periksa_lab.no_rawat=? and periksa_lab.tgl_periksa=? and periksa_lab.jam=?",'sss',array($norawat,$tanggal,$jam));
?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Hasil Lab <?php echo h($p['nm_pasien']); ?> - <?php echo h(tglIndo($tanggal)); ?></title>
<style>
    @page { size: A4; margin: 12mm 12mm 14mm 12mm; }
    * { box-sizing: border-box; }
    body { margin: 0; background: #e9ecef; font-family: Tahoma, Verdana, Arial, sans-serif; font-size: 12px; color: #000; }
    .alat { position: sticky; top: 0; z-index: 9; background: #1f3b57; color: #fff; padding: 8px 16px; display: flex; gap: 10px; align-items: center; }
    .alat b { flex: 1; font-weight: normal; }
    .alat button { font: inherit; padding: 6px 16px; border: 0; border-radius: 4px; cursor: pointer; background: #fff; color: #1f3b57; font-weight: bold; }
    .kertas { width: 210mm; min-height: 297mm; margin: 16px auto; background: #fff; padding: 12mm; box-shadow: 0 2px 10px rgba(0,0,0,.25); }
    .kop { display: flex; align-items: center; gap: 12px; border-bottom: 2px solid #000; padding-bottom: 6px; }
    .kop img { width: 62px; height: 62px; object-fit: contain; }
    .kop .tengah { flex: 1; text-align: center; }
    .kop .rs { font-size: 16px; font-weight: bold; }
    .kop .alamat { font-size: 11px; }
    h1 { font-size: 14px; text-align: center; margin: 10px 0 8px; letter-spacing: .5px; }
    table.info { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
    table.info td { padding: 1px 2px; vertical-align: top; }
    table.info td.l { width: 105px; white-space: nowrap; }
    table.info td.c { width: 8px; }
    table.hasil { width: 100%; border-collapse: collapse; }
    table.hasil th { border-top: 1px solid #000; border-bottom: 1px solid #000; padding: 4px 3px; text-align: left; }
    table.hasil td { padding: 2px 3px; vertical-align: top; }
    table.hasil tr.jenis td { font-weight: bold; padding-top: 6px; }
    table.hasil td.item { padding-left: 14px; }
    table.hasil tr.kritis td { background: #ffd5d5; }
    .kritis .nilai, .kritis .ket { color: #f00; font-weight: bold; }
    .tinggi .nilai, .tinggi .ket { color: #ba7517; }
    .rendah .nilai, .rendah .ket { color: #185fa5; }
    tr.lapor td { color: #006b3c; font-style: italic; padding-left: 14px; }
    table.hasil tbody tr:last-child td { border-bottom: 1px solid #000; }
    .kaki { display: flex; justify-content: space-between; margin-top: 10px; page-break-inside: avoid; }
    .ttd { width: 210px; text-align: center; }
    .ttd img { width: 90px; height: 90px; display: block; margin: 4px auto; }
    .catatan { font-size: 10px; margin-top: 8px; }
    @media print {
        body { background: #fff; }
        .alat { display: none; }
        .kertas { width: auto; min-height: 0; margin: 0; padding: 0; box-shadow: none; }
        tr { page-break-inside: avoid; }
        * { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    }
    @media (max-width: 820px) { .kertas { width: auto; min-height: 0; margin: 8px; padding: 16px; } }
</style>
</head>
<body>
<div class="alat"><b>Hasil Laboratorium &mdash; <?php echo h($p['nm_pasien']); ?></b><button onclick="window.print()">Cetak / Simpan PDF</button></div>
<div class="kertas">
    <div class="kop">
        <?php if(!empty($setting['logo'])){ ?><img src="data:image/jpeg;base64,<?php echo base64_encode($setting['logo']); ?>" alt=""><?php } ?>
        <div class="tengah">
            <div class="rs"><?php echo h($setting['nama_instansi']); ?></div>
            <div class="alamat"><?php echo h($setting['alamat_instansi'].', '.$setting['kabupaten'].', '.$setting['propinsi']); ?><br><?php echo h($setting['kontak']); ?>, E-mail : <?php echo h($setting['email']); ?></div>
        </div>
    </div>
    <h1>HASIL PEMERIKSAAN LABORATORIUM</h1>
    <table class="info">
        <tr><td class="l">No.RM</td><td class="c">:</td><td><?php echo h($p['no_rkm_medis']); ?></td>
            <td class="l">No.Periksa</td><td class="c">:</td><td><?php echo h($p['no_rawat']); ?></td></tr>
        <tr><td class="l">Nama Pasien</td><td class="c">:</td><td><?php echo h($p['nm_pasien']); ?></td>
            <td class="l">Penanggung Jawab</td><td class="c">:</td><td><?php echo h($p['nm_dokter']); ?></td></tr>
        <tr><td class="l">JK/Umur</td><td class="c">:</td><td><?php echo h($p['jk'].' / '.$p['umur']); ?></td>
            <td class="l"><?php echo $permintaan?'Tgl. Keluar Hasil':'Tgl.Pemeriksaan'; ?></td><td class="c">:</td><td><?php echo h(tglIndo($tanggal)); ?></td></tr>
        <tr><td class="l">Alamat</td><td class="c">:</td><td><?php echo h($p['alamat']); ?></td>
            <td class="l"><?php echo $permintaan?'Jam Keluar Hasil':'Jam Pemeriksaan'; ?></td><td class="c">:</td><td><?php echo h($jam); ?></td></tr>
        <tr><td class="l">Dokter Pengirim</td><td class="c">:</td><td><?php echo h($pengirim); ?></td>
            <td class="l"><?php echo h($kamar); ?></td><td class="c">:</td><td><?php echo h($namakamar); ?></td></tr>
        <?php if($permintaan){ ?>
        <tr><td class="l">No.Permintaan Lab</td><td class="c">:</td><td><?php echo h($permintaan['noorder']); ?></td>
            <td class="l">Tgl/Jam Permintaan</td><td class="c">:</td><td><?php echo h(tglIndo($permintaan['tgl_permintaan']).' '.$permintaan['jam_permintaan']); ?></td></tr>
        <?php } ?>
    </table>
    <table class="hasil">
        <thead><tr><th style="width:34%">Pemeriksaan</th><th style="width:16%">Hasil</th><th style="width:14%">Satuan</th><th style="width:22%">Nilai Rujukan</th><th>Keterangan</th></tr></thead>
        <tbody>
        <?php foreach($jenis as $j){ ?>
            <tr class="jenis"><td colspan="5"><?php echo h($j['nm_perawatan']); ?></td></tr>
            <?php
            $item = ambil($db,"select template_laboratorium.id_template,template_laboratorium.Pemeriksaan,detail_periksa_lab.nilai,template_laboratorium.satuan,detail_periksa_lab.nilai_rujukan,detail_periksa_lab.keterangan ".
                "from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template ".
                "where detail_periksa_lab.no_rawat=? and detail_periksa_lab.kd_jenis_prw=? and detail_periksa_lab.tgl_periksa=? and detail_periksa_lab.jam=? order by template_laboratorium.urut",
                'ssss',array($norawat,$j['kd_jenis_prw'],$tanggal,$jam));
            foreach($item as $d){
                $ket = ketWithFlag($d['Pemeriksaan'],$d['nilai'],$d['nilai_rujukan'],$d['keterangan']);
                $kelas = kelasFlag(trim($ket));
            ?>
            <tr class="<?php echo $kelas; ?>">
                <td class="item"><?php echo h($d['Pemeriksaan']); ?></td>
                <td class="nilai"><?php echo h($d['nilai']).($kelas=='kritis'?' &nbsp;KRITIS':''); ?></td>
                <td><?php echo h($d['satuan']); ?></td>
                <td><?php echo h($d['nilai_rujukan']); ?></td>
                <td class="ket"><?php echo h($ket); ?></td>
            </tr>
            <?php $kc = $j['kd_jenis_prw'].'|'.$d['id_template']; if(!empty($catatan[$kc])){ ?>
            <tr class="lapor"><td colspan="5">&raquo; <?php echo h($catatan[$kc]); ?></td></tr>
            <?php } ?>
        <?php } } ?>
        </tbody>
    </table>
    <div class="kaki">
        <div class="ttd">Penanggung Jawab<img src="data:image/png;base64,<?php echo $qrPenjab; ?>" alt="QR"><?php echo h($p['nm_dokter']); ?></div>
        <div class="ttd">Tgl.Cetak : <?php echo h(satu($db,"select date_format(now(),'%d/%m/%Y %H:%i')")); ?><br>Petugas Laboratorium<img src="data:image/png;base64,<?php echo $qrPetugas; ?>" alt="QR"><?php echo h($p['nama']); ?></div>
    </div>
    <div class="catatan">Catatan :<br>Jika ada keragu-raguan pemeriksaan, diharapkan segera menghubungi laboratorium.</div>
</div>
</body>
</html>
<?php mysqli_close($db); ?>
