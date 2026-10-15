<?php
/**
 * Endpoint penerima PDF retensi dari backend mobile (mutasiberkas-mobile).
 * Dipanggil server-to-server (bukan dari browser), diautentikasi lewat
 * header X-Retensi-Secret yang harus cocok dengan $retensi_secret di conf.php.
 */

require_once __DIR__ . '/../conf/conf.php';

header('Content-Type: application/json');

$secretGiven = isset($_SERVER['HTTP_X_RETENSI_SECRET']) ? $_SERVER['HTTP_X_RETENSI_SECRET'] : '';
if (!isset($retensi_secret) || $retensi_secret === '' || !hash_equals($retensi_secret, $secretGiven)) {
    http_response_code(401);
    echo json_encode(['status' => 'error', 'message' => 'Unauthorized']);
    exit;
}

if (!isset($_FILES['file']) || $_FILES['file']['error'] !== UPLOAD_ERR_OK) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'File tidak ditemukan']);
    exit;
}

$filename = isset($_POST['filename']) ? basename($_POST['filename']) : '';
if ($filename === '' || !preg_match('/^retensi_[A-Za-z0-9_-]+\.pdf$/', $filename)) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Nama file tidak valid']);
    exit;
}

$uploadDir = __DIR__ . '/../pages/upload/';
if (!is_dir($uploadDir)) {
    mkdir($uploadDir, 0755, true);
}

$destination = $uploadDir . $filename;
if (!move_uploaded_file($_FILES['file']['tmp_name'], $destination)) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => 'Gagal menyimpan file']);
    exit;
}

echo json_encode(['status' => 'ok']);
