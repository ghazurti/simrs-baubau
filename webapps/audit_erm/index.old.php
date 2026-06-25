<?php
/*
 * Audit Kepatuhan ERM (Electronic Medical Record)
 * Cek kelengkapan pengisian form dokter & perawat per pasien
 * Terintegrasi dengan database Khanza (sik)
 */

require_once('../conf/conf.php');
$koneksi = bukakoneksi();

// Ambil nama instansi
$q = mysqli_query($koneksi, "SELECT nama_instansi FROM setting LIMIT 1");
$instansi = mysqli_fetch_assoc($q);
$nama_rs = $instansi['nama_instansi'] ?? 'Rumah Sakit';

// Daftar semua form ERM yang diaudit (label => nama_tabel_di_DB)
$erm_map = [
    // IGD
    'Triase IGD'                    => ['tabel' => 'data_triase_igd',                              'grup' => 'IGD',                      'tipe' => 'All'],
    'Asesmen Medis IGD'             => ['tabel' => 'penilaian_medis_igd',                          'grup' => 'IGD',                      'tipe' => 'All'],
    'Asesmen Keperawatan IGD'       => ['tabel' => 'penilaian_awal_keperawatan_igd',               'grup' => 'IGD',                      'tipe' => 'All'],
    'Observasi IGD'                 => ['tabel' => 'catatan_observasi_igd',                        'grup' => 'IGD',                      'tipe' => 'All'],
    // Asesmen Medis Ralan
    'Asesmen Medis Ralan'           => ['tabel' => 'penilaian_medis_ralan',                        'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis Anak'            => ['tabel' => 'penilaian_medis_ralan_anak',                   'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis Kandungan'       => ['tabel' => 'penilaian_medis_ralan_kandungan',              'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis Penyakit Dalam'  => ['tabel' => 'penilaian_medis_ralan_penyakit_dalam',        'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis Mata'            => ['tabel' => 'penilaian_medis_ralan_mata',                   'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis THT'             => ['tabel' => 'penilaian_medis_ralan_tht',                    'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis Bedah'           => ['tabel' => 'penilaian_medis_ralan_bedah',                  'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis Orthopedi'       => ['tabel' => 'penilaian_medis_ralan_orthopedi',              'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis Saraf'           => ['tabel' => 'penilaian_medis_ralan_neurologi',              'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis Jiwa'            => ['tabel' => 'penilaian_medis_ralan_psikiatrik',             'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis Kulit'           => ['tabel' => 'penilaian_medis_ralan_kulitdankelamin',        'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis Geriatri'        => ['tabel' => 'penilaian_medis_ralan_geriatri',               'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    'Asesmen Medis Rehab Medik'     => ['tabel' => 'penilaian_medis_ralan_rehab_medik',            'grup' => 'Asesmen Medis Ralan',      'tipe' => 'Ralan'],
    // Asesmen Keperawatan Ralan
    'Asesmen Kep Ralan'             => ['tabel' => 'penilaian_awal_keperawatan_ralan',             'grup' => 'Asesmen Kep Ralan',        'tipe' => 'Ralan'],
    'Asesmen Kep Bayi/Anak'         => ['tabel' => 'penilaian_awal_keperawatan_ralan_bayi',        'grup' => 'Asesmen Kep Ralan',        'tipe' => 'Ralan'],
    'Asesmen Kep Gigi'              => ['tabel' => 'penilaian_awal_keperawatan_gigi',              'grup' => 'Asesmen Kep Ralan',        'tipe' => 'Ralan'],
    'Asesmen Kep Kebidanan'         => ['tabel' => 'penilaian_awal_keperawatan_kebidanan',         'grup' => 'Asesmen Kep Ralan',        'tipe' => 'Ralan'],
    'Asesmen Kep Mata'              => ['tabel' => 'penilaian_awal_keperawatan_mata',              'grup' => 'Asesmen Kep Ralan',        'tipe' => 'Ralan'],
    'Asesmen Kep Psikiatri'         => ['tabel' => 'penilaian_awal_keperawatan_ralan_psikiatri',   'grup' => 'Asesmen Kep Ralan',        'tipe' => 'Ralan'],
    'Asesmen Kep Geriatri'          => ['tabel' => 'penilaian_awal_keperawatan_ralan_geriatri',    'grup' => 'Asesmen Kep Ralan',        'tipe' => 'Ralan'],
    // Asesmen Ranap
    'Asesmen Medis Ranap'           => ['tabel' => 'penilaian_medis_ranap',                        'grup' => 'Asesmen Ranap',            'tipe' => 'Ranap'],
    'Asesmen Medis Kandungan Ranap' => ['tabel' => 'penilaian_medis_ranap_kandungan',              'grup' => 'Asesmen Ranap',            'tipe' => 'Ranap'],
    'Asesmen Medis Neonatus'        => ['tabel' => 'penilaian_medis_ranap_neonatus',               'grup' => 'Asesmen Ranap',            'tipe' => 'Ranap'],
    'Asesmen Kep Ranap'             => ['tabel' => 'penilaian_awal_keperawatan_ranap',             'grup' => 'Asesmen Ranap',            'tipe' => 'Ranap'],
    'Asesmen Kep Kebidanan Ranap'   => ['tabel' => 'penilaian_awal_keperawatan_kebidanan_ranap',   'grup' => 'Asesmen Ranap',            'tipe' => 'Ranap'],
    'Asesmen Kep Neonatus'          => ['tabel' => 'penilaian_awal_keperawatan_ranap_neonatus',    'grup' => 'Asesmen Ranap',            'tipe' => 'Ranap'],
    'Transfer Antar Ruang'          => ['tabel' => 'transfer_pasien_antar_ruang',                  'grup' => 'Asesmen Ranap',            'tipe' => 'Ranap'],
    // CPPT & SOAP
    'CPPT Ralan'                    => ['tabel' => 'pemeriksaan_ralan',                            'grup' => 'CPPT & SOAP',             'tipe' => 'Ralan'],
    'CPPT Ranap'                    => ['tabel' => 'pemeriksaan_ranap',                            'grup' => 'CPPT & SOAP',             'tipe' => 'Ranap'],
    'Catatan Keperawatan'           => ['tabel' => 'catatan_keperawatan_ranap',                    'grup' => 'CPPT & SOAP',             'tipe' => 'Ranap'],
    'Grafik Observasi'              => ['tabel' => 'catatan_observasi_ranap',                      'grup' => 'CPPT & SOAP',             'tipe' => 'Ranap'],
    // Penunjang
    'Resep Dokter'                  => ['tabel' => 'resep_obat',                                   'grup' => 'Penunjang',                'tipe' => 'All'],
    'Permintaan Lab'                => ['tabel' => 'permintaan_lab',                               'grup' => 'Penunjang',                'tipe' => 'All'],
    'Permintaan Radiologi'          => ['tabel' => 'permintaan_radiologi',                         'grup' => 'Penunjang',                'tipe' => 'All'],
    'Diagnosa ICD10'                => ['tabel' => 'diagnosa_pasien',                              'grup' => 'Penunjang',                'tipe' => 'All'],
    'Prosedur ICD9'                 => ['tabel' => 'prosedur_pasien',                              'grup' => 'Penunjang',                'tipe' => 'All'],
    // Operasi
    'Penilaian Pre-Operasi'         => ['tabel' => 'penilaian_pre_operasi',                        'grup' => 'Operasi',                  'tipe' => 'All'],
    'Penilaian Pre-Anestesi'        => ['tabel' => 'penilaian_pre_anestesi',                       'grup' => 'Operasi',                  'tipe' => 'All'],
    'Sign In Anestesi'              => ['tabel' => 'signin_sebelum_anestesi',                      'grup' => 'Operasi',                  'tipe' => 'All'],
    'Time Out Insisi'               => ['tabel' => 'timeout_sebelum_insisi',                       'grup' => 'Operasi',                  'tipe' => 'All'],
    'Sign Out Operasi'              => ['tabel' => 'signout_sebelum_menutup_luka',                 'grup' => 'Operasi',                  'tipe' => 'All'],
    'Laporan Operasi'               => ['tabel' => 'laporan_operasi',                              'grup' => 'Operasi',                  'tipe' => 'All'],
    // Monitoring & Risiko
    'Penilaian Ulang Nyeri'         => ['tabel' => 'penilaian_ulang_nyeri',                        'grup' => 'Monitoring & Risiko',     'tipe' => 'All'],
    'Risiko Dekubitus'              => ['tabel' => 'penilaian_risiko_dekubitus',                   'grup' => 'Monitoring & Risiko',     'tipe' => 'Ranap'],
    'Risiko Jatuh Dewasa'           => ['tabel' => 'penilaian_lanjutan_resiko_jatuh_dewasa',       'grup' => 'Monitoring & Risiko',     'tipe' => 'Ranap'],
    'Risiko Jatuh Anak'             => ['tabel' => 'penilaian_lanjutan_resiko_jatuh_anak',         'grup' => 'Monitoring & Risiko',     'tipe' => 'Ranap'],
    'Risiko Jatuh Lansia'           => ['tabel' => 'penilaian_lanjutan_resiko_jatuh_lansia',       'grup' => 'Monitoring & Risiko',     'tipe' => 'Ranap'],
    'EWS Neonatus'                  => ['tabel' => 'pemantauan_ews_neonatus',                      'grup' => 'Monitoring & Risiko',     'tipe' => 'Ranap'],
    'MEOWS Obstetri'                => ['tabel' => 'pemantauan_meows_obstetri',                    'grup' => 'Monitoring & Risiko',     'tipe' => 'Ranap'],
    'NEWS Dewasa'                   => ['tabel' => 'pemantauan_pews_dewasa',                       'grup' => 'Monitoring & Risiko',     'tipe' => 'Ranap'],
    // Gizi
    'Skrining Gizi'                 => ['tabel' => 'skrining_gizi',                                'grup' => 'Gizi',                     'tipe' => 'All'],
    'Asuhan Gizi (ADIME)'           => ['tabel' => 'catatan_adime_gizi',                           'grup' => 'Gizi',                     'tipe' => 'Ranap'],
    // Resume & Pulang
    'Resume Ralan'                  => ['tabel' => 'resume_pasien',                                'grup' => 'Resume & Pulang',         'tipe' => 'Ralan'],
    'Resume Ranap'                  => ['tabel' => 'resume_pasien_ranap',                          'grup' => 'Resume & Pulang',         'tipe' => 'Ranap'],
    'Perencanaan Pemulangan'        => ['tabel' => 'perencanaan_pemulangan',                       'grup' => 'Resume & Pulang',         'tipe' => 'Ranap'],
];

// Ambil daftar poli dan bangsal untuk dropdown filter
$list_poli = [];
$res_poli = mysqli_query($koneksi, "SELECT kd_poli, nm_poli FROM poliklinik ORDER BY nm_poli");
while ($r = mysqli_fetch_assoc($res_poli)) { $list_poli[] = $r; }

$list_bangsal = [];
$res_bangsal = mysqli_query($koneksi, "SELECT kd_bangsal, nm_bangsal FROM bangsal ORDER BY nm_bangsal");
while ($r = mysqli_fetch_assoc($res_bangsal)) { $list_bangsal[] = $r; }

// Handler form submit
$tgl_awal      = date('Y-m-d');
$tgl_akhir     = date('Y-m-d');
$status_lanjut = 'Semua';
$kd_poli       = '';
$kd_bangsal    = '';
$selected_cols = array_keys($erm_map);
$data_rows     = [];
$sudah_cari    = false;

if (isset($_POST['cari'])) {
    $sudah_cari    = true;
    $tgl_awal      = mysqli_real_escape_string($koneksi, $_POST['tanggal_awal']);
    $tgl_akhir     = mysqli_real_escape_string($koneksi, $_POST['tanggal_akhir']);
    $kd_poli       = mysqli_real_escape_string($koneksi, $_POST['kd_poli'] ?? '');
    $kd_bangsal    = mysqli_real_escape_string($koneksi, $_POST['kd_bangsal'] ?? '');

    // Whitelist status_lanjut untuk cegah SQL injection
    $allowed_status = ['Semua', 'Ralan', 'Ranap'];
    $status_lanjut  = in_array($_POST['status_lanjut'], $allowed_status) ? $_POST['status_lanjut'] : 'Semua';

    if (isset($_POST['cols']) && is_array($_POST['cols'])) {
        $selected_cols = array_filter($_POST['cols'], fn($c) => isset($erm_map[$c]));
    }

    $where_status  = ($status_lanjut !== 'Semua') ? "AND rp.status_lanjut = '$status_lanjut'" : "";
    $where_poli    = ($kd_poli !== '')    ? "AND rp.kd_poli = '$kd_poli'"         : "";
    $where_bangsal = ($kd_bangsal !== '') ? "AND b.kd_bangsal = '$kd_bangsal'"    : "";

    $sql = "SELECT
                rp.no_rawat, rp.tgl_registrasi, rp.no_rkm_medis, rp.status_lanjut,
                p.nm_pasien,
                d.nm_dokter,
                pj.png_jawab,
                poli.nm_poli,
                b.nm_bangsal,
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
            $where_status
            $where_poli
            $where_bangsal
            GROUP BY rp.no_rawat
            ORDER BY rp.tgl_registrasi ASC, rp.jam_reg ASC";

    // Ambil semua data pasien dulu
    $res = mysqli_query($koneksi, $sql);
    while ($row = mysqli_fetch_assoc($res)) {
        $data_rows[] = $row;
    }

    if (!empty($data_rows)) {
        // Kumpulkan semua no_rawat sekaligus untuk IN clause
        $semua_no_rawat = array_map(
            fn($r) => "'" . mysqli_real_escape_string($koneksi, $r['no_rawat']) . "'",
            $data_rows
        );
        $in_clause = implode(',', $semua_no_rawat);

        // Buat lookup: $ada_map[nama_tabel][no_rawat] = true
        // Hanya 1 query per tabel, bukan 1 query per pasien per tabel
        $ada_map = [];
        foreach ($selected_cols as $col_label) {
            $tabel = mysqli_real_escape_string($koneksi, $erm_map[$col_label]['tabel']);
            $ada_map[$col_label] = [];
            $cek = mysqli_query($koneksi, "SELECT DISTINCT no_rawat FROM `$tabel` WHERE no_rawat IN ($in_clause)");
            if ($cek) {
                while ($r = mysqli_fetch_assoc($cek)) {
                    $ada_map[$col_label][$r['no_rawat']] = true;
                }
            }
        }

        // Tempelkan hasil audit ke tiap baris pasien dari lookup (tanpa query lagi)
        foreach ($data_rows as &$row) {
            $audit_status = [];
            foreach ($selected_cols as $col_label) {
                $audit_status[$col_label] = isset($ada_map[$col_label][$row['no_rawat']]) ? 'ada' : 'kosong';
            }
            $row['audit'] = $audit_status;
        }
        unset($row);
    }
}

// Kelompokkan $erm_map per grup untuk modal pilih kolom
$grouped = [];
foreach ($erm_map as $label => $cfg) {
    $grouped[$cfg['grup']][] = $label;
}
?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Audit ERM - <?php echo htmlspecialchars($nama_rs); ?></title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.datatables.net/1.13.6/css/dataTables.bootstrap5.min.css" rel="stylesheet">
<link href="https://cdn.datatables.net/buttons/2.4.2/css/buttons.bootstrap5.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
<style>
    body { background-color: #f4f6f9; font-size: .875rem; }
    th { white-space: nowrap; font-size: 0.75rem; }
    td { font-size: 0.8rem; }
    .grup-header { background: #e9ecef; font-weight: 700; padding: 6px 10px; border-radius: 4px; margin: 8px 0 4px; font-size: 0.78rem; text-transform: uppercase; color: #495057; }
    .check-item label { font-size: 0.83rem; cursor: pointer; }
    .badge-ralan { background-color: #0dcaf0; color: #000; }
    .badge-ranap { background-color: #ffc107; color: #000; }
    .icon-ada { color: #198754; font-size: 1rem; }
    .icon-kosong { font-size: 0.65rem; }
    .ringkasan-bar { background: #fff; border-radius: 8px; padding: 14px 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); margin-bottom: 20px; }
</style>
</head>
<body>

<!-- HEADER -->
<nav class="navbar navbar-dark bg-danger px-3 mb-4">
    <span class="navbar-brand fw-bold">
        <i class="fas fa-clipboard-check me-2"></i>Audit Kepatuhan ERM
    </span>
    <span class="text-white small"><?php echo htmlspecialchars($nama_rs); ?> &mdash; <?php echo date('d F Y'); ?></span>
</nav>

<div class="container-fluid px-4">

<!-- FORM FILTER -->
<form method="POST" action="">
    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <div class="row g-2 align-items-end">
                <div class="col-md-2">
                    <label class="form-label fw-bold mb-1">Tanggal Awal</label>
                    <input type="date" name="tanggal_awal" class="form-control form-control-sm" value="<?php echo htmlspecialchars($tgl_awal); ?>" required>
                </div>
                <div class="col-md-2">
                    <label class="form-label fw-bold mb-1">Tanggal Akhir</label>
                    <input type="date" name="tanggal_akhir" class="form-control form-control-sm" value="<?php echo htmlspecialchars($tgl_akhir); ?>" required>
                </div>
                <div class="col-md-2">
                    <label class="form-label fw-bold mb-1">Jenis Pelayanan</label>
                    <select name="status_lanjut" id="selStatusLanjut" class="form-select form-select-sm" onchange="toggleUnitFilter()">
                        <option value="Semua"  <?php echo ($status_lanjut == 'Semua') ? 'selected' : ''; ?>>Semua</option>
                        <option value="Ralan"  <?php echo ($status_lanjut == 'Ralan') ? 'selected' : ''; ?>>Rawat Jalan</option>
                        <option value="Ranap"  <?php echo ($status_lanjut == 'Ranap') ? 'selected' : ''; ?>>Rawat Inap</option>
                    </select>
                </div>

                <!-- Filter Poli (muncul saat Ralan dipilih) -->
                <div class="col-md-2" id="filterPoli" style="display:<?php echo ($status_lanjut === 'Ralan') ? 'block' : 'none'; ?>">
                    <label class="form-label fw-bold mb-1">Poliklinik</label>
                    <select name="kd_poli" class="form-select form-select-sm">
                        <option value="">-- Semua Poli --</option>
                        <?php foreach ($list_poli as $p): ?>
                        <option value="<?php echo htmlspecialchars($p['kd_poli']); ?>"
                            <?php echo ($kd_poli === $p['kd_poli']) ? 'selected' : ''; ?>>
                            <?php echo htmlspecialchars($p['nm_poli']); ?>
                        </option>
                        <?php endforeach; ?>
                    </select>
                </div>

                <!-- Filter Bangsal (muncul saat Ranap dipilih) -->
                <div class="col-md-2" id="filterBangsal" style="display:<?php echo ($status_lanjut === 'Ranap') ? 'block' : 'none'; ?>">
                    <label class="form-label fw-bold mb-1">Bangsal / Kamar</label>
                    <select name="kd_bangsal" class="form-select form-select-sm">
                        <option value="">-- Semua Bangsal --</option>
                        <?php foreach ($list_bangsal as $b): ?>
                        <option value="<?php echo htmlspecialchars($b['kd_bangsal']); ?>"
                            <?php echo ($kd_bangsal === $b['kd_bangsal']) ? 'selected' : ''; ?>>
                            <?php echo htmlspecialchars($b['nm_bangsal']); ?>
                        </option>
                        <?php endforeach; ?>
                    </select>
                </div>

                <div class="col-md-2">
                    <label class="form-label fw-bold mb-1">Kolom Audit</label>
                    <button type="button" class="btn btn-outline-secondary btn-sm w-100" data-bs-toggle="modal" data-bs-target="#modalPilihKolom">
                        <i class="fas fa-list-check me-1"></i>Pilih Form
                        <span class="badge bg-primary ms-1"><?php echo count($selected_cols); ?></span>
                    </button>
                </div>
                <div class="col-md-2">
                    <label class="form-label fw-bold mb-1">&nbsp;</label>
                    <button type="submit" name="cari" class="btn btn-danger btn-sm w-100">
                        <i class="fas fa-search me-1"></i>Tampilkan
                    </button>
                </div>
                <?php if ($sudah_cari): ?>
                <div class="col-md-2">
                    <label class="form-label fw-bold mb-1">&nbsp;</label>
                    <a href="index.php" class="btn btn-outline-secondary btn-sm w-100">
                        <i class="fas fa-redo me-1"></i>Reset
                    </a>
                </div>
                <?php endif; ?>
            </div>
        </div>
    </div>

    <!-- MODAL PILIH KOLOM -->
    <div class="modal fade" id="modalPilihKolom" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-xl modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><i class="fas fa-tasks me-2"></i>Pilih Form yang Diaudit</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="row mb-3 sticky-top bg-white pt-2 pb-2 border-bottom">
                        <div class="col-md-6">
                            <input type="text" id="cariKolom" class="form-control form-control-sm" placeholder="Cari nama form...">
                        </div>
                        <div class="col-md-6 text-end">
                            <button type="button" class="btn btn-sm btn-success me-1" onclick="pilihSemua(true)">Pilih Semua</button>
                            <button type="button" class="btn btn-sm btn-danger" onclick="pilihSemua(false)">Hapus Semua</button>
                        </div>
                    </div>
                    <div class="row" id="daftarKolom">
                        <?php foreach ($grouped as $grup => $items): ?>
                            <div class="col-12 grup-header grup-label"><?php echo htmlspecialchars($grup); ?></div>
                            <?php foreach ($items as $label):
                                $checked = in_array($label, $selected_cols) ? 'checked' : '';
                                $id_chk  = 'chk_' . md5($label);
                            ?>
                            <div class="col-md-3 col-sm-6 mb-1 check-item">
                                <div class="form-check">
                                    <input class="form-check-input col-chk" type="checkbox"
                                           name="cols[]" value="<?php echo htmlspecialchars($label); ?>"
                                           id="<?php echo $id_chk; ?>" <?php echo $checked; ?>>
                                    <label class="form-check-label" for="<?php echo $id_chk; ?>">
                                        <?php echo htmlspecialchars($label); ?>
                                    </label>
                                </div>
                            </div>
                            <?php endforeach; ?>
                        <?php endforeach; ?>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" data-bs-dismiss="modal">Simpan Pilihan</button>
                </div>
            </div>
        </div>
    </div>
</form>

<!-- RINGKASAN -->
<?php if ($sudah_cari): ?>
<?php
    $total_pasien = count($data_rows);
    $total_form   = count($selected_cols);
    $total_sel    = $total_pasien * $total_form;
    $total_isi    = 0;
    foreach ($data_rows as $row) {
        foreach ($row['audit'] as $status) {
            if ($status === 'ada') $total_isi++;
        }
    }
    $pct = ($total_sel > 0) ? round(($total_isi / $total_sel) * 100, 1) : 0;
    $warna_bar = $pct >= 80 ? 'bg-success' : ($pct >= 50 ? 'bg-warning' : 'bg-danger');
?>
<div class="ringkasan-bar d-flex flex-wrap gap-4 align-items-center">
    <div>
        <div class="text-muted small">Total Pasien</div>
        <div class="fw-bold fs-5"><?php echo $total_pasien; ?></div>
    </div>
    <div>
        <div class="text-muted small">Form Diaudit</div>
        <div class="fw-bold fs-5"><?php echo $total_form; ?></div>
    </div>
    <div>
        <div class="text-muted small">Terisi</div>
        <div class="fw-bold fs-5 text-success"><?php echo $total_isi; ?></div>
    </div>
    <div>
        <div class="text-muted small">Kosong</div>
        <div class="fw-bold fs-5 text-danger"><?php echo $total_sel - $total_isi; ?></div>
    </div>
    <div class="flex-grow-1">
        <div class="d-flex justify-content-between small mb-1">
            <span class="text-muted">Tingkat Kepatuhan</span>
            <span class="fw-bold"><?php echo $pct; ?>%</span>
        </div>
        <div class="progress" style="height: 10px;">
            <div class="progress-bar <?php echo $warna_bar; ?>" style="width: <?php echo $pct; ?>%"></div>
        </div>
    </div>
</div>
<?php endif; ?>

<!-- TABEL AUDIT -->
<div class="card shadow-sm mb-5">
    <div class="card-header d-flex justify-content-between align-items-center py-2">
        <span class="fw-bold text-danger"><i class="fas fa-table me-2"></i>Hasil Audit Kelengkapan Form</span>
        <?php if ($sudah_cari): ?>
        <small class="text-muted"><?php echo $tgl_awal; ?> s/d <?php echo $tgl_akhir; ?> &mdash; <?php echo htmlspecialchars($status_lanjut); ?></small>
        <?php endif; ?>
    </div>
    <div class="card-body p-2">
        <div class="table-responsive">
            <table id="tabelAudit" class="table table-sm table-bordered table-striped table-hover w-100">
                <thead class="table-dark">
                    <tr>
                        <th>No.</th>
                        <th>No. Rawat</th>
                        <th>Tgl Reg</th>
                        <th>No. RM</th>
                        <th>Pasien</th>
                        <th>Penjamin</th>
                        <th>Dokter</th>
                        <th>Poli/Bangsal</th>
                        <th>Status</th>
                        <?php foreach ($selected_cols as $col): ?>
                        <th class="text-center"><?php echo htmlspecialchars($col); ?></th>
                        <?php endforeach; ?>
                    </tr>
                </thead>
                <tbody>
                <?php if ($sudah_cari): ?>
                    <?php $no = 1; foreach ($data_rows as $row): ?>
                    <tr>
                        <td class="text-center"><?php echo $no++; ?></td>
                        <td class="fw-bold"><?php echo htmlspecialchars($row['no_rawat']); ?></td>
                        <td><?php echo htmlspecialchars($row['tgl_registrasi']); ?></td>
                        <td><?php echo htmlspecialchars($row['no_rkm_medis']); ?></td>
                        <td><?php echo htmlspecialchars($row['nm_pasien']); ?></td>
                        <td><?php echo htmlspecialchars($row['png_jawab']); ?></td>
                        <td><?php echo htmlspecialchars($row['nm_dokter']); ?></td>
                        <td><?php echo htmlspecialchars($row['unit']); ?></td>
                        <td class="text-center">
                            <?php if ($row['status_lanjut'] === 'Ralan'): ?>
                                <span class="badge badge-ralan">Ralan</span>
                            <?php else: ?>
                                <span class="badge badge-ranap">Ranap</span>
                            <?php endif; ?>
                        </td>
                        <?php foreach ($row['audit'] as $status): ?>
                        <td class="text-center">
                            <?php if ($status === 'ada'): ?>
                                <i class="fas fa-check-circle icon-ada"></i>
                            <?php else: ?>
                                <span class="badge bg-danger icon-kosong">KOSONG</span>
                            <?php endif; ?>
                        </td>
                        <?php endforeach; ?>
                    </tr>
                    <?php endforeach; ?>
                <?php endif; ?>
                </tbody>
            </table>
        </div>
        <?php if (!$sudah_cari): ?>
        <div class="text-center text-muted py-5">
            <i class="fas fa-search fa-3x mb-3 d-block text-secondary"></i>
            Atur filter tanggal dan klik <strong>Tampilkan</strong> untuk memulai audit.
        </div>
        <?php endif; ?>
    </div>
</div>

</div><!-- /container-fluid -->

<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.datatables.net/1.13.6/js/jquery.dataTables.min.js"></script>
<script src="https://cdn.datatables.net/1.13.6/js/dataTables.bootstrap5.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jszip/3.10.1/jszip.min.js"></script>
<script src="https://cdn.datatables.net/buttons/2.4.2/js/dataTables.buttons.min.js"></script>
<script src="https://cdn.datatables.net/buttons/2.4.2/js/buttons.bootstrap5.min.js"></script>
<script src="https://cdn.datatables.net/buttons/2.4.2/js/buttons.html5.min.js"></script>
<script src="https://cdn.datatables.net/buttons/2.4.2/js/buttons.print.min.js"></script>
<script>
$(document).ready(function() {
    <?php if ($sudah_cari && count($data_rows) > 0): ?>
    $('#tabelAudit').DataTable({
        scrollX: true,
        scrollY: '58vh',
        scrollCollapse: true,
        paging: false,
        dom: 'Bfrtip',
        buttons: [
            {
                extend: 'excelHtml5',
                className: 'btn btn-success btn-sm',
                text: '<i class="fas fa-file-excel me-1"></i>Export Excel',
                title: 'Audit ERM - <?php echo addslashes($nama_rs); ?>',
                exportOptions: {
                    columns: ':not(:last-child)',
                    format: {
                        body: function(data) {
                            return $('<div>').html(data).text().trim();
                        }
                    }
                }
            },
            {
                extend: 'print',
                className: 'btn btn-secondary btn-sm',
                text: '<i class="fas fa-print me-1"></i>Print',
                exportOptions: { columns: ':visible' }
            }
        ],
        language: {
            search: 'Cari:',
            lengthMenu: 'Tampilkan _MENU_ baris',
            info: 'Menampilkan _START_ - _END_ dari _TOTAL_ data',
            zeroRecords: 'Tidak ada data yang cocok'
        }
    });
    <?php endif; ?>

    // Toggle filter poli/bangsal saat halaman load (jaga state setelah submit)
    toggleUnitFilter();

    // Cari di modal kolom
    $('#cariKolom').on('keyup', function() {
        var val = $(this).val().toLowerCase();
        $('.check-item').each(function() {
            $(this).toggle($(this).text().toLowerCase().includes(val));
        });
        $('.grup-label').each(function() {
            var ada = $(this).nextUntil('.grup-label', '.check-item:visible').length > 0;
            $(this).toggle(ada);
        });
    });
});

function pilihSemua(status) {
    $('.col-chk').prop('checked', status);
}

function toggleUnitFilter() {
    var val = document.getElementById('selStatusLanjut').value;
    document.getElementById('filterPoli').style.display    = (val === 'Ralan') ? 'block' : 'none';
    document.getElementById('filterBangsal').style.display = (val === 'Ranap') ? 'block' : 'none';
    // Reset nilai dropdown yang tersembunyi agar tidak ikut terfilter
    if (val !== 'Ralan')  document.querySelector('[name="kd_poli"]').value    = '';
    if (val !== 'Ranap')  document.querySelector('[name="kd_bangsal"]').value = '';
}
</script>
</body>
</html>
