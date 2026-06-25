<?php
    require_once('../../conf/conf.php');

    if ($_SERVER['REQUEST_METHOD'] !== 'POST'
        || empty($_POST['nosurat'])
        || empty($_POST['image'])) {
        http_response_code(400);
        exit("<center><h3>Halaman ini hanya bisa diakses melalui form pengambilan TTD.</h3>"
            ."<a href='../index.php'>Kembali</a></center>");
    }

    $nosurat           = validTeks4($_POST["nosurat"],20);
    $pengobatan_kepada = validTeks4($_POST["pengobatan_kepada"] ?? '',20);
    $nilai_kepercayaan = validTeks4($_POST["nilai_kepercayaan"] ?? '',50);

    $folderPath = __DIR__ . "/upload/";
    $oldFile    = $folderPath . $nosurat . ".jpeg";
    if (file_exists($oldFile)) {
        @unlink($oldFile);
    }

    $img         = $_POST["image"];
    $image_parts = explode(";base64,", (string)$img);
    if (count($image_parts) < 2) {
        http_response_code(400);
        exit("<center><h3>Data gambar tidak valid.</h3></center>");
    }
    $image_base64 = base64_decode($image_parts[1]);
    $fileName     = $nosurat . ".jpeg";
    $file         = $folderPath . $fileName;
    file_put_contents($file, $image_base64);

    Tambah3("surat_persetujuan_umum_pembuat_pernyataan","'".$nosurat."','pages/upload/$fileName'");
    Ubah2("surat_persetujuan_umum","pengobatan_kepada='$pengobatan_kepada',nilai_kepercayaan='$nilai_kepercayaan' where no_surat='$nosurat'");
?>
<head>
    <title>SIMKES Khanza</title>
    <link rel="stylesheet" href="../css/bootstrap.min.css" />
    <style type="text/css">
        #results { padding: 0px; background:#EEFFEE; width: 490; height: 390 }
    </style>
</head>
<html xmlns="http://www.w3.org/1999/xhtml">
    <body><center>Proses Pengambilan Persetujuan Umum Pasien/Keluarga Pasien Sudah Selesai ..!! <br><a href='../login.php?iyem=<?=encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e")?>' class='btn btn-secondary' >Kembali</a></center></body>
</html>

