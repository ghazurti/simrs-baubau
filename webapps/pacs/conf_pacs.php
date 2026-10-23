<?php
    // Setting embed viewer PACS Serova (lihat Panduan Integrasi Embed Viewer PACS).
    // File ini berisi user/password PACS: jangan dibuka langsung dari browser.
    if(basename($_SERVER['SCRIPT_FILENAME'])==basename(__FILE__)){
        http_response_code(403);
        exit;
    }

    // alamat aplikasi viewer (origin PACS), tanpa / di akhir
    // lokal RSUD (whitelist frame-ancestors sudah memuat http://172.17.10.5);
    // cloud: https://rsudkotabaubau.risserova.cloud
    define('PACS_VIEWER', 'http://172.17.10.3:3000');
    // base URL API PACS yang dikirim ke viewer sebagai backendHost
    // lokal: API alat_rs 9005 (viewer memanggil <backend>/worklist/details_by_id/<ID worklist>);
    // 9005 tidak punya path /api-mantap. Cloud: https://rsudkotabaubau.risserova.cloud/api-mantap/api
    define('PACS_BACKEND', 'http://172.17.10.3:9005/api');
    // endpoint login untuk mendapatkan token PACS (dari tim Serova)
    define('PACS_TOKEN_URL', 'http://172.17.10.3:9005/api/user/login');
    // akun service PACS; isi sama dengan USERALATRS / PASSALATRS di Khanza kalau akunnya sama
    define('PACS_USER', '');
    define('PACS_PASS', '');
    // kalau Serova memberi API key, isi di sini (dikirim sebagai header x-api-key)
    define('PACS_API_KEY', '');
    // lama sesi viewer setelah tiket ditukar (detik)
    define('PACS_UMUR_SESI', 900);
?>
