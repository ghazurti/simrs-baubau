<?php
    // Token PACS untuk halaman viewer.php (hanya sesi yang dibuka dari tiket Khanza).
    require_once('conf_pacs.php');

    session_set_cookie_params(0, dirname($_SERVER['SCRIPT_NAME']).'/', '', false, true);
    session_name('PACSVIEWER');
    session_start();
    header('Content-Type: application/json');
    header('Cache-Control: no-store');

    function balas($kode,$data){
        http_response_code($kode);
        echo json_encode($data,JSON_UNESCAPED_SLASHES);
        exit;
    }

    if($_SERVER['REQUEST_METHOD']!=='POST') balas(405,array('error'=>'POST only'));
    if(empty($_SESSION['pacs'])||$_SESSION['pacs']['exp']<time()) balas(401,array('error'=>'Sesi viewer sudah habis'));
    if(PACS_TOKEN_URL==''||PACS_USER=='') balas(500,array('error'=>'Setting PACS di webapps/pacs/conf_pacs.php belum diisi'));
    session_write_close();

    $header=array('Content-Type: application/json');
    if(PACS_API_KEY!='') $header[]='x-api-key: '.PACS_API_KEY;
    $ch=curl_init(PACS_TOKEN_URL);
    curl_setopt_array($ch,array(
        CURLOPT_POST=>true,
        CURLOPT_POSTFIELDS=>json_encode(array('username'=>PACS_USER,'password'=>PACS_PASS)),
        CURLOPT_HTTPHEADER=>$header,
        CURLOPT_RETURNTRANSFER=>true,
        CURLOPT_CONNECTTIMEOUT=>10,
        CURLOPT_TIMEOUT=>20,
    ));
    $isi=curl_exec($ch);
    $kode=curl_getinfo($ch,CURLINFO_HTTP_CODE);
    $err=curl_error($ch);
    curl_close($ch);
    if($isi===false) balas(502,array('error'=>'Login PACS gagal: '.$err));
    if($kode>=400) balas(502,array('error'=>'Login PACS gagal: HTTP '.$kode));

    $r=json_decode($isi,true);
    if(!is_array($r)) balas(502,array('error'=>'Balasan login PACS bukan JSON'));
    // token bisa di data[0], data, atau root
    $d=$r;
    if(isset($r['data'][0])&&is_array($r['data'][0])) $d=$r['data'][0];
    elseif(isset($r['data'])&&is_array($r['data'])) $d=$r['data'];

    function ambil($n,$kunci){
        foreach($kunci as $k){
            if(isset($n[$k])&&!is_array($n[$k])&&$n[$k]!=='') return (string)$n[$k];
            if(isset($n[$k]['name'])) return (string)$n[$k]['name'];
        }
        return '';
    }
    $token=ambil($d,array('token','access_token','accessToken','jwt'));
    if($token=='') balas(502,array('error'=>'Balasan login PACS tanpa token'));
    $u=(isset($d['user'])&&is_array($d['user']))?$d['user']:$d;
    $username=ambil($u,array('username','user_name','nama_user'));

    balas(200,array(
        'token'=>$token,
        'user'=>array(
            'id'=>ambil($u,array('id','user_id','id_user')),
            'username'=>$username==''?PACS_USER:$username,
            'role'=>ambil($u,array('role','role_name','level')),
        ),
        'backendHost'=>PACS_BACKEND,
    ));
?>
