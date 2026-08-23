<?php
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
<!doctype html>
<html lang="en">
<head>
    <title>Antrian Farmasi (Racikan &amp; Non Racikan)</title>
    <link rel="icon" href="assets/img/rs.png" type="image/x-icon">
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" />
    <link type="text/css" rel="stylesheet" href="assets/css/materialize.min.css"  media="screen,projection"/>
    <link type="text/css" rel="stylesheet" href="assets/css/jquery-ui.css"  media="screen,projection"/>
    <link rel="stylesheet" href="assets/css/marquee.css" />
    <link rel="stylesheet" href="assets/css/example.css" />
    <link rel="stylesheet" href="assets/css/ok.css" />
    <style type="text/css">
        .bg::before {
            content: '';
            background-image: url('./assets/img/operasi.jpg');
            background-size: cover;
            background-repeat: no-repeat;
            background-attachment: scroll;
            position: fixed;
            z-index: -1;
            top: 0; bottom: 0; left: 0; right: 0;
            opacity: 0.10;
            filter:alpha(opacity=10);
        }
        /* dua kolom untuk 1 TV */
        .kolom-antri { padding: 0 8px; display:flex; flex-direction:column; height:calc(100vh - 66px); }
        .scrollbox   { flex:1; overflow:hidden; }   /* area daftar yang di-scroll otomatis */
        .judul-jalur {
            text-align:center; font-weight:bold; color:#fff;
            padding:6px 0; margin-bottom:4px; border-radius:4px;
        }
        .jalur-racikan  { background:#e64a19; }   /* deep-orange */
        .jalur-nonracik { background:#00695c; }   /* teal */
        table.default td { padding: 4px 6px; }
    </style>
</head>
<body class="bg">
    <header>
        <nav class="deep-orange accent-3">
            <div class="nav-wrapper">
                <ul class="center hide-on-med-and-down" id="nv">
                    <li>
                        <a href="./" class="ams hide-on-med-and-down"><i class="material-icons md-36">local_hospital</i> Antrian Apotek/Farmasi</a>
                    </li>
                    <li class="right" style="margin-right: 10px;">
                        <i class="material-icons">perm_contact_calendar</i>
                        <a href="" class="white-text">
                            <?php
                              $a_hari   = array(1=>"Senin","Selasa","Rabu","Kamis","Jumat", "Sabtu","Minggu");
                              $hari     = $a_hari[date("N")];
                              $tanggal  = date ("j");
                              $a_bulan  = array(1=>"Januari","Februari","Maret", "April", "Mei", "Juni","Juli","Agustus","September","Oktober", "November","Desember");
                              $bulan    = $a_bulan[date("n")];
                              $tahun    = date("Y");
                              echo $hari . ", " . $tanggal ." ". $bulan ." ". $tahun;
                            ?>
                        </a>
                        <i class="material-icons md-12">query_builder</i>
                        <a href="" class="white-text" id="jam"></a>
                  </li>
                </ul>
            </div>
        </nav>
    </header>

    <main>
        <div class="row" style="margin:0;">
            <!-- KIRI: RACIKAN -->
            <div class="col s6 kolom-antri">
                <div class="judul-jalur jalur-racikan">RACIKAN</div>
                <div id="judulR"></div>
                <div id="dataR" class="scrollbox"></div>
            </div>
            <!-- KANAN: NON RACIKAN -->
            <div class="col s6 kolom-antri">
                <div class="judul-jalur jalur-nonracik">NON RACIKAN</div>
                <div id="judulN"></div>
                <div id="dataN" class="scrollbox"></div>
            </div>
        </div>
    </main>

    <script type="text/javascript" src="assets/js/jquery-2.1.1.min.js"></script>
    <script type="text/javascript" src="assets/js/materialize.min.js"></script>
    <script type="text/javascript" src="assets/js/jquery-ui.min.js"></script>
    <script type="text/javascript" src="assets/js/bootstrap.min.js"></script>
    <script data-pace-options='{ "ajax": false }' src='assets/js/pace.min.js'></script>
    <script type="text/javascript" src="assets/js/marquee.js"></script>
    <script type="text/javascript">
       window.onload = function() { jam(); }
       function jam() {
        var e = document.getElementById('jam'), d = new Date(), h, m, s;
        h = d.getHours(); m = set(d.getMinutes()); s = set(d.getSeconds());
        e.innerHTML = h +':'+ m +':'+ s;
        setTimeout('jam()', 1000);
       }
       function set(e){ e = e < 10 ? '0'+ e : e; return e; }
    </script>

    <script type="text/javascript" src="assets/js/jquery.js"></script>
    <script type="text/javascript">
        // Muat daftar tapi PERTAHANKAN posisi scroll (biar auto-scroll tak lompat ke atas tiap refresh).
        function muatDaftar(id,url){
            var el=document.getElementById(id);
            var posisi = el ? el.scrollTop : 0;
            $('#'+id).load(url, function(){ if(el){ el.scrollTop = posisi; } });
        }
        function muatSemua(){
            muatDaftar('dataR','data_antrianfarmasiracikan.php');
            muatDaftar('dataN','data_antrianfarmasinonracikan.php');
            $('#judulR').load('data_antrianfarmasijudulracikan.php');
            $('#judulN').load('data_antrianfarmasijudulnonracikan.php');
        }
        muatSemua();                       // langsung tampil, tidak nunggu 9 detik
        setInterval(muatSemua, 9000);      // segarkan tiap 9 detik

        // Auto-scroll vertikal: naik pelan bila daftar lebih panjang dari layar, lalu balik ke atas.
        function autoScroll(id){
            var el=document.getElementById(id);
            if(!el) return;
            if(el.scrollHeight - el.clientHeight <= 2){ el.scrollTop=0; el._jeda=0; return; } // muat, tak perlu scroll
            if(el.scrollTop + el.clientHeight >= el.scrollHeight - 1){
                el._jeda=(el._jeda||0)+1;
                if(el._jeda > 50){ el.scrollTop=0; el._jeda=0; }   // jeda ~2 dtk di bawah lalu balik atas
            } else {
                el.scrollTop += 1;
            }
        }
        setInterval(function(){ autoScroll('dataR'); autoScroll('dataN'); }, 40); // ~25 px/detik
    </script>

    <!-- Kontrol ukuran TV (tersimpan per-TV) + suara panggilan -->
    <div id="ctrlukuran" style="position:fixed;right:10px;bottom:10px;z-index:99999;opacity:0.85;">
        <button type="button" onclick="ubahUkuran(-0.1)" style="font-size:16px;padding:4px 12px;cursor:pointer;">A&minus;</button>
        <button type="button" onclick="ubahUkuran(0.1)" style="font-size:16px;padding:4px 12px;cursor:pointer;">A+</button>
        <button type="button" onclick="resetUkuran()" style="font-size:13px;padding:4px 10px;cursor:pointer;">Reset</button>
        <button type="button" onclick="aktifkanSuara()" style="font-size:13px;padding:4px 10px;cursor:pointer;">&#128266; Suara</button>
    </div>
    <script type="text/javascript">
        var KEY_UKURAN='ukuranTVfarmasiGabung';
        function terapkanUkuran(z){ var m=document.querySelector('main'); if(m){ m.style.zoom=z; } }
        function ubahUkuran(d){ var z=parseFloat(localStorage.getItem(KEY_UKURAN)||'1')+d; if(z<0.5)z=0.5; if(z>3)z=3; z=Math.round(z*10)/10; localStorage.setItem(KEY_UKURAN,z); terapkanUkuran(z); }
        function resetUkuran(){ localStorage.setItem(KEY_UKURAN,'1'); terapkanUkuran(1); }
        terapkanUkuran(parseFloat(localStorage.getItem(KEY_UKURAN)||'1'));

        // Suara panggilan (TTS). Dipanggil dari fragmen judul via jQuery .load.
        function bilang(teks){ try{ if(!('speechSynthesis' in window))return; var u=new SpeechSynthesisUtterance(teks); u.lang='id-ID'; u.rate=0.9; window.speechSynthesis.speak(u);}catch(e){} }
        function aktifkanSuara(){ try{ if('speechSynthesis' in window){ var u=new SpeechSynthesisUtterance('Suara panggilan diaktifkan'); u.lang='id-ID'; window.speechSynthesis.speak(u);} }catch(e){} }
    </script>
</body>
</html>
