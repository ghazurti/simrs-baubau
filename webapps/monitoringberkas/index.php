<?php
 session_start();
 require_once('conf/command.php');
 require_once('../conf/conf.php');
 header("Expires: Mon, 26 Jul 1997 05:00:00 GMT"); 
 header("Last-Modified: ".gmdate("D, d M Y H:i:s")." GMT"); 
 header("Cache-Control: no-store, no-cache, must-revalidate"); 
 header("Cache-Control: post-check=0, pre-check=0", false);
 header("Pragma: no-cache"); // HTTP/1.0
?>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title><?php title();?></title>
    <link href="css/default.css" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="library/tiny_mce/tiny_mce.js"></script>
    <script type="text/javascript" src="conf/validator.js"></script>
    <script type="text/javascript" src="js/jquery/jquery.js"></script>
    <script type="text/javascript" src="js/jquery/jquery.dropdown.js"></script>
    <script>
        function PopupCenter(pageURL, title,w,h) {
            var left = (screen.width/2)-(w/2);
            var top = (screen.height/2)-(h/2);
            var targetWin = window.open (pageURL, title, 'toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no, width='+w+', height='+h+', top='+top+', left='+left);
            
        }

        // Pilihan alasan saat berkas ditandai "Tidak Ada". Jendela HTML sendiri (bukan confirm/prompt)
        // karena halaman ini dibuka di WebView aplikasi Khanza yang tidak menampilkan dialog browser.
        var ALASAN_TIDAK_ADA = [
            "Berkas dipinjam unit lain",
            "Berkas masih di ruang rawat inap",
            "Berkas belum kembali dari poli",
            "Berkas tidak ditemukan di rak",
            "Pasien baru, berkas belum dibuat",
            "Lainnya"
        ];
        function pilihAlasan(a){
            var lama=document.getElementById('dlgAlasan'); if(lama) lama.parentNode.removeChild(lama);
            var d=document.createElement('div'); d.id='dlgAlasan';
            d.style.cssText='position:fixed;left:0;top:0;width:100%;height:100%;background:rgba(0,0,0,.35);z-index:9999';
            var h='<div style="background:#fff;width:380px;max-width:92%;margin:8% auto;padding:14px 16px;border-radius:6px;font-family:Tahoma,sans-serif;font-size:13px;box-shadow:0 4px 18px rgba(0,0,0,.3)">'+
                  '<div style="font-weight:bold;font-size:14px;margin-bottom:10px;color:#a00">Berkas Tidak Ada - pilih alasan</div>';
            for(var i=0;i<ALASAN_TIDAK_ADA.length;i++){
                h+='<label style="display:block;padding:4px 0;cursor:pointer"><input type="radio" name="alasanTA" value="'+ALASAN_TIDAK_ADA[i]+'"'+(i==0?' checked':'')+'> '+ALASAN_TIDAK_ADA[i]+'</label>';
            }
            h+='<input type="text" id="alasanLain" maxlength="100" placeholder="Tulis alasan lain / keterangan" style="width:100%;box-sizing:border-box;margin:6px 0 12px;padding:4px">'+
               '<div style="text-align:right"><button type="button" id="btnBatalTA" style="margin-right:8px">Batal</button>'+
               '<button type="button" id="btnSimpanTA" style="font-weight:bold">Simpan</button></div></div>';
            d.innerHTML=h; document.body.appendChild(d);
            document.getElementById('btnBatalTA').onclick=function(){ d.parentNode.removeChild(d); };
            document.getElementById('btnSimpanTA').onclick=function(){
                var r=d.getElementsByTagName('input'), v='';
                for(var i=0;i<r.length;i++){ if(r[i].type=='radio'&&r[i].checked) v=r[i].value; }
                var lain=document.getElementById('alasanLain').value.replace(/^\s+|\s+$/g,'');
                if(v=='Lainnya'){
                    if(lain==''){ document.getElementById('alasanLain').focus(); document.getElementById('alasanLain').style.borderColor='red'; return; }
                    v=lain;
                }else if(lain!=''){ v=v+' - '+lain; }
                window.location.href=a.href+'&alasan='+encodeURIComponent(v);
            };
            return false;
        }
    </script>
</head>
<body>
    <div id="mainContent">
        <?php actionPages();?>
    </div>    
</body>
</html>

