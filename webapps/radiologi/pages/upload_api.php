<?php
    require_once('../../conf/conf.php');

    header('Content-Type: application/json');

    $usere = isset($_POST['usere']) ? trim($_POST['usere']) : '';
    $passwordte = isset($_POST['passwordte']) ? trim($_POST['passwordte']) : '';

    if (empty($usere) || empty($passwordte)) {
        echo json_encode(array('status' => 'error', 'pesan' => 'Kredensial kosong'));
        exit;
    }

    // Validasi sama seperti radiologi/login.php: cocokkan dengan konstanta conf
    if (!defined('USERHYBRIDWEB') || !defined('PASHYBRIDWEB')
            || USERHYBRIDWEB != $usere || PASHYBRIDWEB != $passwordte) {
        echo json_encode(array('status' => 'error', 'pesan' => 'Kredensial tidak valid'));
        exit;
    }

    $no_rawat = validTeks(isset($_POST['no_rawat']) ? $_POST['no_rawat'] : '');
    $tanggal  = validTeks(isset($_POST['tanggal']) ? $_POST['tanggal'] : '');
    $jam      = validTeks3(isset($_POST['jam']) ? $_POST['jam'] : '');

    if (empty($no_rawat) || empty($tanggal) || empty($jam)) {
        echo json_encode(array('status' => 'error', 'pesan' => 'no_rawat, tanggal, jam wajib diisi'));
        exit;
    }

    if (!isset($_FILES['gambar']) || $_FILES['gambar']['error'] !== UPLOAD_ERR_OK) {
        echo json_encode(array('status' => 'error', 'pesan' => 'File gambar tidak ditemukan'));
        exit;
    }

    $ext = strtolower(pathinfo($_FILES['gambar']['name'], PATHINFO_EXTENSION));
    if (!in_array($ext, array('jpg', 'jpeg', 'png'))) {
        echo json_encode(array('status' => 'error', 'pesan' => 'Format harus JPG/JPEG/PNG'));
        exit;
    }

    $mime = @mime_content_type($_FILES['gambar']['tmp_name']);
    if (!in_array($mime, array('image/jpeg', 'image/jpg', 'image/png'))) {
        echo json_encode(array('status' => 'error', 'pesan' => 'MIME type tidak valid: ' . $mime));
        exit;
    }

    $safeFileName = str_replace(' ', '_', $_FILES['gambar']['name']);
    $safeFileName = preg_replace('/[^a-zA-Z0-9_\.\-]/', '', $safeFileName);
    $gambar = 'pages/upload/' . $safeFileName;
    // Path fisik relatif dari lokasi file ini (pages/) ke pages/upload/
    $tujuanFisik = __DIR__ . '/upload/' . $safeFileName;

    if (move_uploaded_file($_FILES['gambar']['tmp_name'], $tujuanFisik)) {
        if (Tambah3(' gambar_radiologi ', " '$no_rawat','$tanggal','$jam','$gambar'")) {
            echo json_encode(array('status' => 'ok', 'pesan' => 'Berhasil', 'lokasi_gambar' => $gambar));
        } else {
            echo json_encode(array('status' => 'error', 'pesan' => 'Gagal insert ke database, file sudah tersimpan'));
        }
    } else {
        echo json_encode(array('status' => 'error', 'pesan' => 'Gagal simpan file'));
    }
?>
