<?php
/*
 * Audit Kepatuhan ERM (Electronic Medical Record) — versi simple
 * Cek kelengkapan pengisian form dokter & perawat per pasien.
 * Integrasi DB Khanza (sik).
 */

require_once('../conf/conf.php');
$koneksi = bukakoneksi();
mysqli_set_charset($koneksi, 'utf8');

// Nama instansi
$q = mysqli_query($koneksi, "SELECT nama_instansi FROM setting LIMIT 1");
$instansi = mysqli_fetch_assoc($q);
$nama_rs = $instansi['nama_instansi'] ?? 'Rumah Sakit';

/*
 * Daftar form ERM diaudit.
 * Flag 'esensial' = ikut paket default (dicentang otomatis).
 */
$erm_map = [
    // IGD
    'Triase IGD'                    => ['tabel' => 'data_triase_igd',                              'grup' => 'IGD',                  'tipe' => 'All',   'esensial' => true],
    'Asesmen Medis IGD'             => ['tabel' => 'penilaian_medis_igd',                          'grup' => 'IGD',                  'tipe' => 'All',   'esensial' => true],
    'Asesmen Keperawatan IGD'       => ['tabel' => 'penilaian_awal_keperawatan_igd',               'grup' => 'IGD',                  'tipe' => 'All',   'esensial' => true],
    'Observasi IGD'                 => ['tabel' => 'catatan_observasi_igd',                        'grup' => 'IGD',                  'tipe' => 'All',   'esensial' => false],
    // Asesmen Medis Ralan
    'Asesmen Medis Ralan'           => ['tabel' => 'penilaian_medis_ralan',                        'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => true],
    'Asesmen Medis Anak'            => ['tabel' => 'penilaian_medis_ralan_anak',                   'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Medis Kandungan'       => ['tabel' => 'penilaian_medis_ralan_kandungan',              'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Medis Penyakit Dalam'  => ['tabel' => 'penilaian_medis_ralan_penyakit_dalam',         'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Medis Mata'            => ['tabel' => 'penilaian_medis_ralan_mata',                   'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Medis THT'             => ['tabel' => 'penilaian_medis_ralan_tht',                    'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Medis Bedah'           => ['tabel' => 'penilaian_medis_ralan_bedah',                  'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Medis Orthopedi'       => ['tabel' => 'penilaian_medis_ralan_orthopedi',              'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Medis Saraf'           => ['tabel' => 'penilaian_medis_ralan_neurologi',              'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Medis Jiwa'            => ['tabel' => 'penilaian_medis_ralan_psikiatrik',             'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Medis Kulit'           => ['tabel' => 'penilaian_medis_ralan_kulitdankelamin',        'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Medis Geriatri'        => ['tabel' => 'penilaian_medis_ralan_geriatri',               'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Medis Rehab Medik'     => ['tabel' => 'penilaian_medis_ralan_rehab_medik',            'grup' => 'Asesmen Medis Ralan',  'tipe' => 'Ralan', 'esensial' => false],
    // Asesmen Kep Ralan
    'Asesmen Kep Ralan'             => ['tabel' => 'penilaian_awal_keperawatan_ralan',             'grup' => 'Asesmen Kep Ralan',    'tipe' => 'Ralan', 'esensial' => true],
    'Asesmen Kep Bayi/Anak'         => ['tabel' => 'penilaian_awal_keperawatan_ralan_bayi',        'grup' => 'Asesmen Kep Ralan',    'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Kep Gigi'              => ['tabel' => 'penilaian_awal_keperawatan_gigi',              'grup' => 'Asesmen Kep Ralan',    'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Kep Kebidanan'         => ['tabel' => 'penilaian_awal_keperawatan_kebidanan',         'grup' => 'Asesmen Kep Ralan',    'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Kep Mata'              => ['tabel' => 'penilaian_awal_keperawatan_mata',              'grup' => 'Asesmen Kep Ralan',    'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Kep Psikiatri'         => ['tabel' => 'penilaian_awal_keperawatan_ralan_psikiatri',   'grup' => 'Asesmen Kep Ralan',    'tipe' => 'Ralan', 'esensial' => false],
    'Asesmen Kep Geriatri'          => ['tabel' => 'penilaian_awal_keperawatan_ralan_geriatri',    'grup' => 'Asesmen Kep Ralan',    'tipe' => 'Ralan', 'esensial' => false],
    // Asesmen Ranap
    'Asesmen Medis Ranap'           => ['tabel' => 'penilaian_medis_ranap',                        'grup' => 'Asesmen Ranap',        'tipe' => 'Ranap', 'esensial' => true],
    'Asesmen Medis Kandungan Ranap' => ['tabel' => 'penilaian_medis_ranap_kandungan',              'grup' => 'Asesmen Ranap',        'tipe' => 'Ranap', 'esensial' => false],
    'Asesmen Medis Neonatus'        => ['tabel' => 'penilaian_medis_ranap_neonatus',               'grup' => 'Asesmen Ranap',        'tipe' => 'Ranap', 'esensial' => false],
    'Asesmen Kep Ranap'             => ['tabel' => 'penilaian_awal_keperawatan_ranap',             'grup' => 'Asesmen Ranap',        'tipe' => 'Ranap', 'esensial' => true],
    'Asesmen Kep Kebidanan Ranap'   => ['tabel' => 'penilaian_awal_keperawatan_kebidanan_ranap',   'grup' => 'Asesmen Ranap',        'tipe' => 'Ranap', 'esensial' => false],
    'Asesmen Kep Neonatus'          => ['tabel' => 'penilaian_awal_keperawatan_ranap_neonatus',    'grup' => 'Asesmen Ranap',        'tipe' => 'Ranap', 'esensial' => false],
    'Transfer Antar Ruang'          => ['tabel' => 'transfer_pasien_antar_ruang',                  'grup' => 'Asesmen Ranap',        'tipe' => 'Ranap', 'esensial' => false],
    // CPPT & SOAP
    'CPPT Ralan'                    => ['tabel' => 'pemeriksaan_ralan',                            'grup' => 'CPPT & SOAP',          'tipe' => 'Ralan', 'esensial' => true],
    'CPPT Ranap'                    => ['tabel' => 'pemeriksaan_ranap',                            'grup' => 'CPPT & SOAP',          'tipe' => 'Ranap', 'esensial' => true],
    'Catatan Keperawatan'           => ['tabel' => 'catatan_keperawatan_ranap',                    'grup' => 'CPPT & SOAP',          'tipe' => 'Ranap', 'esensial' => false],
    'Grafik Observasi'              => ['tabel' => 'catatan_observasi_ranap',                      'grup' => 'CPPT & SOAP',          'tipe' => 'Ranap', 'esensial' => false],
    // Penunjang
    'Resep Dokter'                  => ['tabel' => 'resep_obat',                                   'grup' => 'Penunjang',            'tipe' => 'All',   'esensial' => true],
    'Permintaan Lab'                => ['tabel' => 'permintaan_lab',                               'grup' => 'Penunjang',            'tipe' => 'All',   'esensial' => false],
    'Permintaan Radiologi'          => ['tabel' => 'permintaan_radiologi',                         'grup' => 'Penunjang',            'tipe' => 'All',   'esensial' => false],
    'Diagnosa ICD10'                => ['tabel' => 'diagnosa_pasien',                              'grup' => 'Penunjang',            'tipe' => 'All',   'esensial' => true],
    'Prosedur ICD9'                 => ['tabel' => 'prosedur_pasien',                              'grup' => 'Penunjang',            'tipe' => 'All',   'esensial' => false],
    // Operasi
    'Penilaian Pre-Operasi'         => ['tabel' => 'penilaian_pre_operasi',                        'grup' => 'Operasi',              'tipe' => 'All',   'esensial' => false],
    'Penilaian Pre-Anestesi'        => ['tabel' => 'penilaian_pre_anestesi',                       'grup' => 'Operasi',              'tipe' => 'All',   'esensial' => false],
    'Sign In Anestesi'              => ['tabel' => 'signin_sebelum_anestesi',                      'grup' => 'Operasi',              'tipe' => 'All',   'esensial' => false],
    'Time Out Insisi'               => ['tabel' => 'timeout_sebelum_insisi',                       'grup' => 'Operasi',              'tipe' => 'All',   'esensial' => false],
    'Sign Out Operasi'              => ['tabel' => 'signout_sebelum_menutup_luka',                 'grup' => 'Operasi',              'tipe' => 'All',   'esensial' => false],
    'Laporan Operasi'               => ['tabel' => 'laporan_operasi',                              'grup' => 'Operasi',              'tipe' => 'All',   'esensial' => false],
    // Monitoring & Risiko
    'Penilaian Ulang Nyeri'         => ['tabel' => 'penilaian_ulang_nyeri',                        'grup' => 'Monitoring & Risiko',  'tipe' => 'All',   'esensial' => false],
    'Risiko Dekubitus'              => ['tabel' => 'penilaian_risiko_dekubitus',                   'grup' => 'Monitoring & Risiko',  'tipe' => 'Ranap', 'esensial' => false],
    'Risiko Jatuh Dewasa'           => ['tabel' => 'penilaian_lanjutan_resiko_jatuh_dewasa',       'grup' => 'Monitoring & Risiko',  'tipe' => 'Ranap', 'esensial' => false],
    'Risiko Jatuh Anak'             => ['tabel' => 'penilaian_lanjutan_resiko_jatuh_anak',         'grup' => 'Monitoring & Risiko',  'tipe' => 'Ranap', 'esensial' => false],
    'Risiko Jatuh Lansia'           => ['tabel' => 'penilaian_lanjutan_resiko_jatuh_lansia',       'grup' => 'Monitoring & Risiko',  'tipe' => 'Ranap', 'esensial' => false],
    'EWS Neonatus'                  => ['tabel' => 'pemantauan_ews_neonatus',                      'grup' => 'Monitoring & Risiko',  'tipe' => 'Ranap', 'esensial' => false],
    'MEOWS Obstetri'                => ['tabel' => 'pemantauan_meows_obstetri',                    'grup' => 'Monitoring & Risiko',  'tipe' => 'Ranap', 'esensial' => false],
    'NEWS Dewasa'                   => ['tabel' => 'pemantauan_pews_dewasa',                       'grup' => 'Monitoring & Risiko',  'tipe' => 'Ranap', 'esensial' => false],
    // Gizi
    'Skrining Gizi'                 => ['tabel' => 'skrining_gizi',                                'grup' => 'Gizi',                 'tipe' => 'All',   'esensial' => false],
    'Asuhan Gizi (ADIME)'           => ['tabel' => 'catatan_adime_gizi',                           'grup' => 'Gizi',                 'tipe' => 'Ranap', 'esensial' => false],
    // Resume & Pulang
    'Resume Ralan'                  => ['tabel' => 'resume_pasien',                                'grup' => 'Resume & Pulang',      'tipe' => 'Ralan', 'esensial' => true],
    'Resume Ranap'                  => ['tabel' => 'resume_pasien_ranap',                          'grup' => 'Resume & Pulang',      'tipe' => 'Ranap', 'esensial' => true],
    'Perencanaan Pemulangan'        => ['tabel' => 'perencanaan_pemulangan',                       'grup' => 'Resume & Pulang',      'tipe' => 'Ranap', 'esensial' => false],
];

// Dropdown filter
$list_poli = [];
$res_poli = mysqli_query($koneksi, "SELECT kd_poli, nm_poli FROM poliklinik ORDER BY nm_poli");
while ($r = mysqli_fetch_assoc($res_poli)) { $list_poli[] = $r; }

$list_bangsal = [];
$res_bangsal = mysqli_query($koneksi, "SELECT kd_bangsal, nm_bangsal FROM bangsal ORDER BY nm_bangsal");
while ($r = mysqli_fetch_assoc($res_bangsal)) { $list_bangsal[] = $r; }

// Default: hari ini, paket esensial, jalankan otomatis
$tgl_awal      = $_GET['tanggal_awal']  ?? date('Y-m-d');
$tgl_akhir     = $_GET['tanggal_akhir'] ?? date('Y-m-d');
$status_lanjut = $_GET['status_lanjut'] ?? 'Semua';
$kd_poli       = $_GET['kd_poli']       ?? '';
$kd_bangsal    = $_GET['kd_bangsal']    ?? '';
$paket         = $_GET['paket']         ?? 'esensial'; // esensial | semua | custom

// Tentukan kolom yang akan diaudit berdasarkan paket
if ($paket === 'semua') {
    $selected_cols = array_keys($erm_map);
} elseif ($paket === 'custom' && isset($_GET['cols']) && is_array($_GET['cols'])) {
    $selected_cols = array_filter($_GET['cols'], fn($c) => isset($erm_map[$c]));
} else {
    $paket = 'esensial';
    $selected_cols = array_keys(array_filter($erm_map, fn($c) => !empty($c['esensial'])));
}

// Sanitasi & whitelist
$tgl_awal   = mysqli_real_escape_string($koneksi, $tgl_awal);
$tgl_akhir  = mysqli_real_escape_string($koneksi, $tgl_akhir);
$kd_poli    = mysqli_real_escape_string($koneksi, $kd_poli);
$kd_bangsal = mysqli_real_escape_string($koneksi, $kd_bangsal);
$allowed_status = ['Semua', 'Ralan', 'Ranap'];
if (!in_array($status_lanjut, $allowed_status, true)) $status_lanjut = 'Semua';

$where_status  = ($status_lanjut !== 'Semua') ? "AND rp.status_lanjut = '$status_lanjut'" : "";
$where_poli    = ($kd_poli !== '')    ? "AND rp.kd_poli = '$kd_poli'"        : "";
$where_bangsal = ($kd_bangsal !== '') ? "AND b.kd_bangsal = '$kd_bangsal'"   : "";

$sql = "SELECT
            rp.no_rawat, rp.tgl_registrasi, rp.no_rkm_medis, rp.status_lanjut,
            p.nm_pasien, d.nm_dokter, pj.png_jawab, poli.nm_poli, b.nm_bangsal,
            IF(rp.status_lanjut='Ralan', poli.nm_poli, b.nm_bangsal) AS unit
        FROM reg_periksa rp
        JOIN pasien p    ON rp.no_rkm_medis = p.no_rkm_medis
        JOIN dokter d    ON rp.kd_dokter     = d.kd_dokter
        JOIN penjab pj   ON rp.kd_pj         = pj.kd_pj
        LEFT JOIN poliklinik poli ON rp.kd_poli = poli.kd_poli
        LEFT JOIN kamar_inap ki   ON rp.no_rawat = ki.no_rawat
        LEFT JOIN kamar k         ON ki.kd_kamar = k.kd_kamar
        LEFT JOIN bangsal b       ON k.kd_bangsal = b.kd_bangsal
        WHERE rp.tgl_registrasi BETWEEN '$tgl_awal' AND '$tgl_akhir'
          AND rp.stts <> 'Batal'
          $where_status $where_poli $where_bangsal
        GROUP BY rp.no_rawat
        ORDER BY rp.tgl_registrasi ASC, rp.jam_reg ASC";

$data_rows = [];
$res = mysqli_query($koneksi, $sql);
while ($row = mysqli_fetch_assoc($res)) { $data_rows[] = $row; }

// Bulk lookup: ada / tidak
$ada_map = [];
if (!empty($data_rows)) {
    $semua_no_rawat = array_map(
        fn($r) => "'" . mysqli_real_escape_string($koneksi, $r['no_rawat']) . "'",
        $data_rows
    );
    $in_clause = implode(',', $semua_no_rawat);

    foreach ($selected_cols as $col_label) {
        $tabel = mysqli_real_escape_string($koneksi, $erm_map[$col_label]['tabel']);
        $ada_map[$col_label] = [];
        $cek = @mysqli_query($koneksi, "SELECT DISTINCT no_rawat FROM `$tabel` WHERE no_rawat IN ($in_clause)");
        if ($cek) {
            while ($r = mysqli_fetch_assoc($cek)) {
                $ada_map[$col_label][$r['no_rawat']] = true;
            }
        }
    }

    // Tempelkan + hitung per pasien
    foreach ($data_rows as &$row) {
        $audit = []; $isi = 0; $relevan = 0;
        foreach ($selected_cols as $col_label) {
            $tipe = $erm_map[$col_label]['tipe'];
            // Filter relevansi: form "Ralan" tidak dihitung kalau pasien Ranap, vice versa
            if ($tipe !== 'All' && $tipe !== $row['status_lanjut']) {
                $audit[$col_label] = 'na'; // not applicable
                continue;
            }
            $ok = isset($ada_map[$col_label][$row['no_rawat']]);
            $audit[$col_label] = $ok ? 'ada' : 'kosong';
            $relevan++;
            if ($ok) $isi++;
        }
        $row['audit']    = $audit;
        $row['isi']      = $isi;
        $row['relevan']  = $relevan;
        $row['pct']      = $relevan > 0 ? round(($isi / $relevan) * 100, 1) : 0;
    }
    unset($row);
}

// Ringkasan keseluruhan + per grup
$total_pasien = count($data_rows);
$total_isi = 0; $total_relevan = 0;
$per_grup = []; // [grup => [isi, relevan]]
foreach ($data_rows as $row) {
    $total_isi     += $row['isi'];
    $total_relevan += $row['relevan'];
    foreach ($row['audit'] as $col => $status) {
        if ($status === 'na') continue;
        $g = $erm_map[$col]['grup'];
        if (!isset($per_grup[$g])) $per_grup[$g] = ['isi' => 0, 'relevan' => 0];
        $per_grup[$g]['relevan']++;
        if ($status === 'ada') $per_grup[$g]['isi']++;
    }
}
$pct = $total_relevan > 0 ? round(($total_isi / $total_relevan) * 100, 1) : 0;

// Untuk modal pilih kolom custom
$grouped = [];
foreach ($erm_map as $label => $cfg) { $grouped[$cfg['grup']][] = $label; }

// Helper format URL preserve filter
function q(array $extra = []): string {
    $base = $_GET; foreach ($extra as $k => $v) $base[$k] = $v;
    return '?' . http_build_query($base);
}
?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Audit RME — <?= htmlspecialchars($nama_rs) ?></title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
<style>
  :root { --ok:#16a34a; --warn:#f59e0b; --bad:#dc2626; --ink:#0f172a; --mute:#64748b; --line:#e2e8f0; }
  body { background:#f8fafc; color:var(--ink); font-family:'Inter',ui-sans-serif,system-ui,sans-serif; font-size:14px; }
  .topbar { background:#fff; border-bottom:1px solid var(--line); padding:14px 24px; }
  .brand { font-weight:700; font-size:16px; color:var(--ink); }
  .brand small { color:var(--mute); font-weight:400; }
  .wrap { padding:20px 24px 60px; }
  .card-flat { background:#fff; border:1px solid var(--line); border-radius:10px; }
  .stat { padding:16px 18px; }
  .stat .lbl { color:var(--mute); font-size:12px; text-transform:uppercase; letter-spacing:.5px; }
  .stat .val { font-size:24px; font-weight:700; margin-top:2px; }
  .pill { display:inline-block; padding:2px 8px; border-radius:999px; font-size:11px; font-weight:600; }
  .pill-ok  { background:#dcfce7; color:#166534; }
  .pill-mid { background:#fef3c7; color:#92400e; }
  .pill-bad { background:#fee2e2; color:#991b1b; }
  .pill-ralan { background:#dbeafe; color:#1e40af; }
  .pill-ranap { background:#fce7f3; color:#9d174d; }
  .ring { width:46px; height:46px; }
  .dot-row { display:flex; flex-wrap:wrap; gap:3px; max-width:240px; }
  .dot { width:10px; height:10px; border-radius:2px; display:inline-block; }
  .dot-ok { background:var(--ok); }
  .dot-bad { background:var(--bad); opacity:.85; }
  .dot-na { background:var(--line); }
  .preset-btn { border:1px solid var(--line); background:#fff; color:var(--ink); padding:8px 14px; border-radius:8px; font-size:13px; font-weight:500; }
  .preset-btn.active { background:var(--ink); color:#fff; border-color:var(--ink); }
  .preset-btn:hover:not(.active) { background:#f1f5f9; }
  .row-click { cursor:pointer; }
  .row-click:hover { background:#f8fafc !important; }
  .grup-card { background:#fff; border:1px solid var(--line); border-radius:8px; padding:12px 14px; margin-bottom:8px; }
  .grup-card .nm { font-weight:600; font-size:13px; }
  .grup-card .pc { font-size:18px; font-weight:700; }
  .bar { height:6px; background:var(--line); border-radius:3px; overflow:hidden; }
  .bar > div { height:100%; }
  .bar-ok  { background:var(--ok); }
  .bar-mid { background:var(--warn); }
  .bar-bad { background:var(--bad); }
  table.audit { font-size:13px; }
  table.audit thead th { background:#f1f5f9; color:var(--mute); font-weight:600; font-size:11.5px; text-transform:uppercase; letter-spacing:.5px; border-bottom:1px solid var(--line); padding:10px 12px; }
  table.audit tbody td { padding:12px; border-bottom:1px solid var(--line); vertical-align:middle; }
  .checklist-item { display:flex; align-items:center; gap:10px; padding:6px 0; font-size:13px; }
  .checklist-item .ic { width:18px; }
  .checklist-grp { font-weight:700; font-size:11px; color:var(--mute); text-transform:uppercase; letter-spacing:.5px; margin:14px 0 4px; }
  .checklist-item.ok { color:var(--ok); }
  .checklist-item.bad { color:var(--bad); }
  .checklist-item.na { color:var(--mute); opacity:.55; }
  .empty { padding:50px 20px; text-align:center; color:var(--mute); }
  .empty i { font-size:38px; opacity:.4; display:block; margin-bottom:10px; }
  .filter-row label { font-size:12px; color:var(--mute); font-weight:600; margin-bottom:4px; display:block; }
  .filter-row .form-control, .filter-row .form-select { font-size:13px; }
  @media print {
    .no-print { display:none !important; }
    body { background:#fff; }
  }
</style>
</head>
<body>

<div class="topbar d-flex justify-content-between align-items-center">
  <div class="brand"><i class="fas fa-clipboard-check me-2" style="color:#dc2626"></i>Audit RME <small> · <?= htmlspecialchars($nama_rs) ?></small></div>
  <div class="text-muted small"><?= date('d M Y · H:i') ?></div>
</div>

<div class="wrap">

  <!-- FILTER -->
  <form method="GET" class="card-flat p-3 mb-3 no-print" id="filterForm">
    <div class="row g-2 filter-row">
      <div class="col-md-2">
        <label>Tanggal awal</label>
        <input type="date" name="tanggal_awal" class="form-control form-control-sm" value="<?= htmlspecialchars($tgl_awal) ?>" required>
      </div>
      <div class="col-md-2">
        <label>Tanggal akhir</label>
        <input type="date" name="tanggal_akhir" class="form-control form-control-sm" value="<?= htmlspecialchars($tgl_akhir) ?>" required>
      </div>
      <div class="col-md-2">
        <label>Jenis layanan</label>
        <select name="status_lanjut" id="selStatus" class="form-select form-select-sm" onchange="toggleUnit()">
          <option value="Semua" <?= $status_lanjut === 'Semua' ? 'selected' : '' ?>>Semua</option>
          <option value="Ralan" <?= $status_lanjut === 'Ralan' ? 'selected' : '' ?>>Rawat Jalan</option>
          <option value="Ranap" <?= $status_lanjut === 'Ranap' ? 'selected' : '' ?>>Rawat Inap</option>
        </select>
      </div>
      <div class="col-md-2" id="filterPoli" style="display:<?= $status_lanjut === 'Ralan' ? 'block' : 'none' ?>">
        <label>Poliklinik</label>
        <select name="kd_poli" class="form-select form-select-sm">
          <option value="">— semua —</option>
          <?php foreach ($list_poli as $p): ?>
          <option value="<?= htmlspecialchars($p['kd_poli']) ?>" <?= $kd_poli === $p['kd_poli'] ? 'selected' : '' ?>><?= htmlspecialchars($p['nm_poli']) ?></option>
          <?php endforeach ?>
        </select>
      </div>
      <div class="col-md-2" id="filterBangsal" style="display:<?= $status_lanjut === 'Ranap' ? 'block' : 'none' ?>">
        <label>Bangsal</label>
        <select name="kd_bangsal" class="form-select form-select-sm">
          <option value="">— semua —</option>
          <?php foreach ($list_bangsal as $b): ?>
          <option value="<?= htmlspecialchars($b['kd_bangsal']) ?>" <?= $kd_bangsal === $b['kd_bangsal'] ? 'selected' : '' ?>><?= htmlspecialchars($b['nm_bangsal']) ?></option>
          <?php endforeach ?>
        </select>
      </div>
      <div class="col-md-2 d-flex align-items-end">
        <button type="submit" class="btn btn-dark btn-sm w-100"><i class="fas fa-rotate me-1"></i> Terapkan</button>
      </div>
    </div>

    <hr class="my-3">

    <div class="d-flex align-items-center gap-2 flex-wrap">
      <span class="text-muted small me-2">Paket form:</span>
      <button type="submit" name="paket" value="esensial" class="preset-btn <?= $paket === 'esensial' ? 'active' : '' ?>">
        <i class="fas fa-star me-1"></i> Esensial
        <span class="ms-1 small opacity-75">(<?= count(array_filter($erm_map, fn($c) => !empty($c['esensial']))) ?>)</span>
      </button>
      <button type="submit" name="paket" value="semua" class="preset-btn <?= $paket === 'semua' ? 'active' : '' ?>">
        <i class="fas fa-layer-group me-1"></i> Semua
        <span class="ms-1 small opacity-75">(<?= count($erm_map) ?>)</span>
      </button>
      <button type="button" class="preset-btn <?= $paket === 'custom' ? 'active' : '' ?>" data-bs-toggle="modal" data-bs-target="#mdlPilih">
        <i class="fas fa-sliders me-1"></i> Pilih sendiri
        <?php if ($paket === 'custom'): ?><span class="ms-1 small opacity-75">(<?= count($selected_cols) ?>)</span><?php endif ?>
      </button>
      <input type="hidden" name="tanggal_awal" value="<?= htmlspecialchars($tgl_awal) ?>">
      <input type="hidden" name="tanggal_akhir" value="<?= htmlspecialchars($tgl_akhir) ?>">
    </div>
  </form>

  <?php if ($total_pasien === 0): ?>
    <div class="card-flat empty">
      <i class="fas fa-folder-open"></i>
      <div>Tidak ada data pasien pada periode ini.</div>
      <small>Ubah tanggal atau filter, lalu klik <strong>Terapkan</strong>.</small>
    </div>
  <?php else: ?>

  <!-- RINGKASAN -->
  <div class="row g-2 mb-3">
    <div class="col-md-3">
      <div class="card-flat stat">
        <div class="lbl">Pasien</div>
        <div class="val"><?= $total_pasien ?></div>
        <div class="small text-muted"><?= htmlspecialchars($tgl_awal) ?> — <?= htmlspecialchars($tgl_akhir) ?></div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="card-flat stat">
        <div class="lbl">Form terisi</div>
        <div class="val text-success"><?= $total_isi ?></div>
        <div class="small text-muted">dari <?= $total_relevan ?> yg relevan</div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="card-flat stat">
        <div class="lbl">Form kosong</div>
        <div class="val text-danger"><?= $total_relevan - $total_isi ?></div>
        <div class="small text-muted">perlu dilengkapi</div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="card-flat stat d-flex justify-content-between align-items-center">
        <div>
          <div class="lbl">Kepatuhan</div>
          <div class="val"><?= $pct ?>%</div>
        </div>
        <svg class="ring" viewBox="0 0 36 36">
          <?php
            $c = 100; $off = $c - $pct;
            $col = $pct >= 80 ? '#16a34a' : ($pct >= 50 ? '#f59e0b' : '#dc2626');
          ?>
          <circle cx="18" cy="18" r="15.9" fill="none" stroke="#e2e8f0" stroke-width="3"/>
          <circle cx="18" cy="18" r="15.9" fill="none" stroke="<?= $col ?>" stroke-width="3" stroke-dasharray="<?= $pct ?> 100" transform="rotate(-90 18 18)" stroke-linecap="round"/>
        </svg>
      </div>
    </div>
  </div>

  <!-- KEPATUHAN PER GRUP -->
  <div class="card-flat p-3 mb-3">
    <div class="d-flex justify-content-between align-items-center mb-2">
      <div class="fw-bold">Kepatuhan per Grup</div>
      <div class="no-print">
        <button class="btn btn-sm btn-light border" onclick="exportCSV()"><i class="fas fa-file-csv me-1"></i> CSV</button>
        <button class="btn btn-sm btn-light border" onclick="window.print()"><i class="fas fa-print me-1"></i> Print</button>
      </div>
    </div>
    <div class="row g-2">
      <?php foreach ($per_grup as $g => $stats):
        $gp = $stats['relevan'] > 0 ? round(($stats['isi']/$stats['relevan'])*100, 1) : 0;
        $bc = $gp >= 80 ? 'bar-ok' : ($gp >= 50 ? 'bar-mid' : 'bar-bad');
      ?>
      <div class="col-md-4 col-sm-6">
        <div class="grup-card">
          <div class="d-flex justify-content-between align-items-center">
            <span class="nm"><?= htmlspecialchars($g) ?></span>
            <span class="pc"><?= $gp ?>%</span>
          </div>
          <div class="text-muted small mb-2"><?= $stats['isi'] ?> / <?= $stats['relevan'] ?> form</div>
          <div class="bar"><div class="<?= $bc ?>" style="width: <?= $gp ?>%"></div></div>
        </div>
      </div>
      <?php endforeach ?>
    </div>
  </div>

  <!-- TABEL PASIEN -->
  <div class="card-flat">
    <div class="d-flex justify-content-between align-items-center p-3 border-bottom">
      <div class="fw-bold">Hasil per Pasien <span class="text-muted small">· klik baris untuk detail</span></div>
      <input type="search" id="cariRow" class="form-control form-control-sm no-print" style="max-width:240px" placeholder="Cari nama / no.RM / dokter…">
    </div>
    <div class="table-responsive">
      <table class="audit table mb-0" id="tblAudit">
        <thead>
          <tr>
            <th>#</th>
            <th>No.Rawat / RM</th>
            <th>Pasien</th>
            <th>Tipe</th>
            <th>Unit</th>
            <th>Dokter</th>
            <th>Penjamin</th>
            <th>Kelengkapan</th>
            <th class="text-end">%</th>
          </tr>
        </thead>
        <tbody>
          <?php $no = 1; foreach ($data_rows as $row):
            $rp = $row['pct'];
            $pill = $rp >= 80 ? 'pill-ok' : ($rp >= 50 ? 'pill-mid' : 'pill-bad');
            $rowJson = htmlspecialchars(json_encode([
              'no_rawat' => $row['no_rawat'],
              'pasien'   => $row['nm_pasien'],
              'audit'    => $row['audit'],
            ]), ENT_QUOTES);
          ?>
          <tr class="row-click" data-row='<?= $rowJson ?>'>
            <td class="text-muted"><?= $no++ ?></td>
            <td>
              <div class="fw-semibold"><?= htmlspecialchars($row['no_rawat']) ?></div>
              <div class="text-muted small"><?= htmlspecialchars($row['no_rkm_medis']) ?> · <?= htmlspecialchars($row['tgl_registrasi']) ?></div>
            </td>
            <td><?= htmlspecialchars($row['nm_pasien']) ?></td>
            <td><span class="pill <?= $row['status_lanjut'] === 'Ralan' ? 'pill-ralan' : 'pill-ranap' ?>"><?= htmlspecialchars($row['status_lanjut']) ?></span></td>
            <td class="small"><?= htmlspecialchars($row['unit']) ?></td>
            <td class="small"><?= htmlspecialchars($row['nm_dokter']) ?></td>
            <td class="small"><?= htmlspecialchars($row['png_jawab']) ?></td>
            <td>
              <div class="dot-row mb-1">
                <?php foreach ($row['audit'] as $st):
                  $cls = $st === 'ada' ? 'dot-ok' : ($st === 'na' ? 'dot-na' : 'dot-bad');
                ?>
                <span class="dot <?= $cls ?>"></span>
                <?php endforeach ?>
              </div>
              <div class="text-muted small"><?= $row['isi'] ?> / <?= $row['relevan'] ?> terisi</div>
            </td>
            <td class="text-end"><span class="pill <?= $pill ?>"><?= $rp ?>%</span></td>
          </tr>
          <?php endforeach ?>
        </tbody>
      </table>
    </div>
  </div>

  <?php endif ?>

</div><!-- /wrap -->

<!-- MODAL DETAIL PASIEN -->
<div class="modal fade" id="mdlDetail" tabindex="-1">
  <div class="modal-dialog modal-dialog-scrollable">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="mdlDetailTitle">Detail kelengkapan form</h5>
        <button class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body" id="mdlDetailBody"></div>
    </div>
  </div>
</div>

<!-- MODAL PILIH KOLOM -->
<div class="modal fade" id="mdlPilih" tabindex="-1">
  <div class="modal-dialog modal-xl modal-dialog-scrollable">
    <form method="GET">
      <input type="hidden" name="paket" value="custom">
      <input type="hidden" name="tanggal_awal"  value="<?= htmlspecialchars($tgl_awal) ?>">
      <input type="hidden" name="tanggal_akhir" value="<?= htmlspecialchars($tgl_akhir) ?>">
      <input type="hidden" name="status_lanjut" value="<?= htmlspecialchars($status_lanjut) ?>">
      <input type="hidden" name="kd_poli"       value="<?= htmlspecialchars($kd_poli) ?>">
      <input type="hidden" name="kd_bangsal"    value="<?= htmlspecialchars($kd_bangsal) ?>">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title"><i class="fas fa-sliders me-2"></i>Pilih form yang diaudit</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <div class="d-flex gap-2 mb-3 sticky-top bg-white pt-1 pb-2">
            <input type="text" id="cariKolom" class="form-control form-control-sm" placeholder="Cari nama form…">
            <button type="button" class="btn btn-sm btn-outline-success" onclick="pilihSemua(true)">Pilih semua</button>
            <button type="button" class="btn btn-sm btn-outline-danger"  onclick="pilihSemua(false)">Kosongkan</button>
          </div>
          <?php foreach ($grouped as $grup => $items): ?>
            <div class="fw-bold small text-muted text-uppercase mt-3 mb-1 grup-label"><?= htmlspecialchars($grup) ?></div>
            <div class="row">
              <?php foreach ($items as $label):
                $checked = in_array($label, $selected_cols, true) ? 'checked' : '';
                $id_chk  = 'chk_' . md5($label);
              ?>
              <div class="col-md-3 col-sm-6 check-item mb-1">
                <div class="form-check">
                  <input class="form-check-input col-chk" type="checkbox" name="cols[]"
                         value="<?= htmlspecialchars($label) ?>" id="<?= $id_chk ?>" <?= $checked ?>>
                  <label class="form-check-label small" for="<?= $id_chk ?>"><?= htmlspecialchars($label) ?></label>
                </div>
              </div>
              <?php endforeach ?>
            </div>
          <?php endforeach ?>
        </div>
        <div class="modal-footer">
          <button type="submit" class="btn btn-dark">Terapkan</button>
        </div>
      </div>
    </form>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
  // Toggle filter poli/bangsal
  function toggleUnit() {
    var v = document.getElementById('selStatus').value;
    document.getElementById('filterPoli').style.display    = (v === 'Ralan') ? 'block' : 'none';
    document.getElementById('filterBangsal').style.display = (v === 'Ranap') ? 'block' : 'none';
  }

  // Cari di tabel pasien
  document.getElementById('cariRow')?.addEventListener('input', function () {
    var q = this.value.toLowerCase();
    document.querySelectorAll('#tblAudit tbody tr').forEach(function (tr) {
      tr.style.display = tr.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
  });

  // Detail klik baris
  const ermMeta = <?= json_encode(array_map(fn($cfg) => ['grup' => $cfg['grup']], $erm_map), JSON_UNESCAPED_UNICODE) ?>;
  const mdlEl   = document.getElementById('mdlDetail');
  const mdl     = mdlEl ? new bootstrap.Modal(mdlEl) : null;

  document.querySelectorAll('.row-click').forEach(function (tr) {
    tr.addEventListener('click', function () {
      var data = JSON.parse(tr.getAttribute('data-row'));
      document.getElementById('mdlDetailTitle').textContent = data.pasien + ' — ' + data.no_rawat;
      var grupMap = {};
      Object.keys(data.audit).forEach(function (col) {
        var g = (ermMeta[col] && ermMeta[col].grup) || 'Lainnya';
        if (!grupMap[g]) grupMap[g] = [];
        grupMap[g].push({ nama: col, st: data.audit[col] });
      });
      var html = '';
      Object.keys(grupMap).forEach(function (g) {
        html += '<div class="checklist-grp">' + g + '</div>';
        grupMap[g].forEach(function (it) {
          var cls = it.st === 'ada' ? 'ok' : (it.st === 'na' ? 'na' : 'bad');
          var ic  = it.st === 'ada' ? 'fa-circle-check' : (it.st === 'na' ? 'fa-minus' : 'fa-circle-xmark');
          var tag = it.st === 'na'  ? ' <small>(tidak relevan)</small>' : '';
          html += '<div class="checklist-item '+cls+'"><span class="ic"><i class="fas '+ic+'"></i></span>'+it.nama+tag+'</div>';
        });
      });
      document.getElementById('mdlDetailBody').innerHTML = html;
      mdl && mdl.show();
    });
  });

  // Cari di modal pilih kolom
  document.getElementById('cariKolom')?.addEventListener('input', function () {
    var q = this.value.toLowerCase();
    document.querySelectorAll('#mdlPilih .check-item').forEach(function (el) {
      el.style.display = el.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
  });
  function pilihSemua(s) {
    document.querySelectorAll('#mdlPilih .col-chk').forEach(function (c) { c.checked = s; });
  }

  // Export CSV (dari DOM, tanpa server-side)
  function exportCSV() {
    var rows = [['No.Rawat','No.RM','Tgl','Pasien','Tipe','Unit','Dokter','Penjamin','Terisi','Relevan','Persen']];
    document.querySelectorAll('#tblAudit tbody tr').forEach(function (tr) {
      if (tr.style.display === 'none') return;
      var c = tr.children;
      var no_rawat = c[1].querySelector('.fw-semibold').textContent.trim();
      var meta     = c[1].querySelector('.text-muted').textContent.trim().split(' · ');
      var counts   = c[7].querySelector('.text-muted').textContent.replace(/[^0-9 \/]/g,'').trim().split('/');
      rows.push([
        no_rawat, meta[0] || '', meta[1] || '',
        c[2].textContent.trim(), c[3].textContent.trim(),
        c[4].textContent.trim(), c[5].textContent.trim(), c[6].textContent.trim(),
        (counts[0]||'').trim(), (counts[1]||'').trim(),
        c[8].textContent.trim()
      ]);
    });
    var csv = rows.map(r => r.map(v => '"' + String(v).replace(/"/g,'""') + '"').join(',')).join('\n');
    var blob = new Blob([new Uint8Array([0xEF,0xBB,0xBF]), csv], { type: 'text/csv;charset=utf-8' });
    var a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'audit_rme_<?= $tgl_awal ?>_<?= $tgl_akhir ?>.csv';
    a.click();
  }
</script>
</body>
</html>
