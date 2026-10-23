<?php
    // Halaman induk viewer PACS: iframe viewer-v3 + handshake postMessage.
    // Dibuka Khanza dengan ?t=<tiket sekali pakai> dari tabel pacs_embed_tiket.
    require_once('../conf/conf.php');
    require_once('conf_pacs.php');

    session_set_cookie_params(0, dirname($_SERVER['SCRIPT_NAME']).'/', '', false, true);
    session_name('PACSVIEWER');
    session_start();
    header('Cache-Control: no-store');
    header('Referrer-Policy: no-referrer');

    function pacs_gagal($pesan){
        http_response_code(410);
        echo "<!doctype html><meta charset=utf-8><title>Viewer Radiologi</title>".
             "<body style='font-family:Tahoma,sans-serif;padding:24px'>".htmlspecialchars($pesan).
             "<br><br>Silahkan buka lagi dari SIMRS.</body>";
        exit;
    }

    if(isset($_GET['t'])){
        $tiket=preg_replace('/[^a-f0-9]/','',$_GET['t']);
        $kon=bukakoneksi();
        $st=mysqli_prepare($kon,"select accession,judul,username from pacs_embed_tiket where tiket=? and kedaluwarsa>now()");
        mysqli_stmt_bind_param($st,"s",$tiket);
        mysqli_stmt_execute($st);
        mysqli_stmt_bind_result($st,$acc,$judul,$user);
        $ada=mysqli_stmt_fetch($st);
        mysqli_stmt_close($st);
        // tiket sekali pakai: hapus, sekalian bersihkan yang kedaluwarsa
        $st=mysqli_prepare($kon,"delete from pacs_embed_tiket where tiket=? or kedaluwarsa<now()");
        mysqli_stmt_bind_param($st,"s",$tiket);
        mysqli_stmt_execute($st);
        mysqli_stmt_close($st);
        mysqli_close($kon);
        if(!$ada) pacs_gagal("Tautan viewer sudah kedaluwarsa atau sudah dipakai.");
        session_regenerate_id(true);
        $_SESSION['pacs']=array('acc'=>$acc,'judul'=>$judul,'user'=>$user,'exp'=>time()+PACS_UMUR_SESI);
        // buang tiket dari URL
        header('Location: '.strtok($_SERVER['REQUEST_URI'],'?'));
        exit;
    }

    if(empty($_SESSION['pacs'])||$_SESSION['pacs']['exp']<time()) pacs_gagal("Sesi viewer sudah habis.");
    $s=$_SESSION['pacs'];
    $viewer=PACS_VIEWER.'/viewer-v3/'.rawurlencode($s['acc']);
    $judul=htmlspecialchars($s['judul']==''?$s['acc']:$s['judul']);
    $cfg=json_encode(array('viewer'=>$viewer),JSON_HEX_TAG|JSON_HEX_AMP|JSON_HEX_APOS|JSON_HEX_QUOT|JSON_UNESCAPED_SLASHES);
?><!doctype html>
<html>
<head>
<meta charset="utf-8">
<meta name="referrer" content="no-referrer">
<title>Viewer Radiologi - <?php echo $judul; ?></title>
<style>
html,body{margin:0;height:100%;background:#111;color:#eee;font-family:Tahoma,sans-serif}
#bar{height:32px;line-height:32px;padding:0 12px;font-size:13px;background:#1f3b57;display:flex;justify-content:space-between}
#st{color:#ffd27f}
iframe{width:100%;height:calc(100% - 32px);border:0;display:block}
</style>
</head>
<body>
<div id="bar"><span>Viewer Radiologi - <?php echo $judul; ?></span><span id="st">Menghubungkan ke PACS...</span></div>
<iframe id="pacs" allow="fullscreen" allowfullscreen referrerpolicy="no-referrer"></iframe>
<script>
(function(){
    var C=<?php echo $cfg; ?>;
    var ORIGIN=new URL(C.viewer).origin, f=document.getElementById('pacs'), st=document.getElementById('st'), retry=0;
    function auth(){
        fetch('token.php',{method:'POST',credentials:'same-origin'})
            .then(function(r){return r.json();})
            .then(function(d){
                if(!d.token){st.textContent=d.error||'Gagal ambil token';return;}
                f.contentWindow.postMessage({type:'ris:embed-auth',token:d.token,user:d.user,backendHost:d.backendHost},ORIGIN);
                st.textContent='';
            })
            .catch(function(e){st.textContent='Gagal ambil token: '+e;});
    }
    window.addEventListener('message',function(e){
        if(e.origin!==ORIGIN||e.source!==f.contentWindow) return;
        var ty=e.data&&e.data.type;
        if(ty==='ris:embed-ready'){retry=0;auth();}
        else if(ty==='ris:embed-unauthorized'){if(retry++<2) auth(); else st.textContent='Token ditolak PACS';}
    });
    window.addEventListener('pagehide',function(){f.src='about:blank';});
    // listener dipasang dulu, baru iframe dimuat
    f.src=C.viewer;
})();
</script>
</body>
</html>
