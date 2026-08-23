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
    <title>Antrian Farmasi Non Racikan</title>
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
            top: 0;
            bottom: 0;
            left: 0;
            right: 0;
            opacity: 0.10;
            filter:alpha(opacity=10);
        }
    </style>
    <!-- Global style END -->

</head>
<body class="bg">
    <header>
        <nav class="deep-orange accent-3">
            <div class="nav-wrapper">
                <ul class="center hide-on-med-and-down" id="nv">
                    <li>
                        <a href="./" class="ams hide-on-med-and-down"><i class="material-icons md-36">local_hospital</i> Antrian Apotek/Farmasi Non Racikan</a>
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
        <div class="container-fluid" id="judul">
        </div>
        <div class="container-fluid" id="data">
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
        var e = document.getElementById('jam'),
        d = new Date(), h, m, s;
        h = d.getHours();
        m = set(d.getMinutes());
        s = set(d.getSeconds());

        e.innerHTML = h +':'+ m +':'+ s;

        setTimeout('jam()', 1000);
       }

       function set(e) {
        e = e < 10 ? '0'+ e : e;
        return e;
      }
    </script>

    <script type="text/javascript" src="assets/js/jquery.js"></script> 
    <script type="text/javascript"> 
        var auto_refresh = setInterval( 
            function() { 
                $('#data').load('data_antrianfarmasinonracikan.php').fadeIn("fast"); 
            }, 9000
        );
        var auto_refresh = setInterval( 
            function() { 
                $('#judul').load('data_antrianfarmasijudulnonracikan.php').fadeIn("fast"); 
            }, 9000
        );
    </script>

    <!-- Kontrol ukuran TV (tersimpan per-TV) + suara panggilan -->
    <div id="ctrlukuran" style="position:fixed;right:10px;bottom:10px;z-index:99999;opacity:0.85;">
        <button type="button" onclick="ubahUkuran(-0.1)" style="font-size:16px;padding:4px 12px;cursor:pointer;">A&minus;</button>
        <button type="button" onclick="ubahUkuran(0.1)" style="font-size:16px;padding:4px 12px;cursor:pointer;">A+</button>
        <button type="button" onclick="resetUkuran()" style="font-size:13px;padding:4px 10px;cursor:pointer;">Reset</button>
        <button type="button" onclick="aktifkanSuara()" style="font-size:13px;padding:4px 10px;cursor:pointer;">&#128266; Suara</button>
    </div>
    <script type="text/javascript">
        var KEY_UKURAN='ukuranTVfarmasiNonRacikan';
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
