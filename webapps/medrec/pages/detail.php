<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){
        if(!strpos($_SERVER['REQUEST_URI'],"pages/upload/")){
            exit(header("Location:../index.php"));
        }
    }
    require_once 'conf/jpgToPdf.php';
?>
<div id="post">
    <div class="entry">        
        <form name="frm_aturadmin" onsubmit="return validasiIsi();" method="post" action="" enctype=multipart/form-data>
            <?php
                echo "";
                $action  = isset($_GET['action'])?$_GET['action']:NULL;
                $id      = validTeks4((isset($_GET['id'])?$_GET['id']:NULL),15);
                echo "<input type=hidden name=id  value=$id><input type=hidden name=action value=$action>";
                echo "<div align='center' class='link'>
                          <a href=?act=List>| List Retensi |</a>
                      </div>";
            ?>
            <div style="width: 100%; height: 40%; overflow: auto;">
            <table width="100%" align="center">
                <tr class="head">
                    <td width="31%" >No.RM</td><td width="">:</td>
                    <td width="67%"><?php echo $id;?></td>
                </tr>
		        <tr class="head">
                    <td width="31%">Nama Pasien</td><td width="">:</td>
                    <td width="67%"><?php echo getOne("select pasien.nm_pasien from pasien where pasien.no_rkm_medis='$id'");?></td>
                </tr>
                <tr class="head">
                    <td width="31%">Jenis Kelamin</td><td width="">:</td>
                    <td width="67%"><?php echo getOne("select if(pasien.jk='L','Laki-Laki','Perempuan') from pasien where pasien.no_rkm_medis='$id'");?></td>
                </tr>
                <tr class="head">
                    <td width="31%">Tanggal Lahir</td><td width="">:</td>
                    <td width="67%"><?php echo getOne("select pasien.tgl_lahir from pasien where pasien.no_rkm_medis='$id'");?></td>
                </tr>
                <tr class="head">
                    <td width="31%">Nama Ibu</td><td width="">:</td>
                    <td width="67%"><?php echo getOne("select pasien.nm_ibu from pasien where pasien.no_rkm_medis='$id'");?></td>
                </tr>
                <tr class="head">
                    <td width="31%" >Terakhir Daftar</td><td width="">:</td>
                    <td width="67%">
                        <select name="TglTerakhir" class="text" onkeydown="setDefault(this, document.getElementById('MsgIsi1'));" id="TxtIsi1">
                             <?php
                                if($action == "UBAH"){
                                    echo "<option id='TxtIsi1' value=$TglTerakhir>$TglTerakhir</option>";
                                }
                                loadTgl2();
                             ?>
                        </select>
			<select name="BlnTerakhir" class="text" onkeydown="setDefault(this, document.getElementById('MsgIsi1'));" id="TxtIsi1">
                             <?php
                                if($action == "UBAH"){
                                    echo "<option id='TxtIsi1' value=$BlnTerakhir>$BlnTerakhir</option>";
                                }
                                loadBln2();
                             ?>
                        </select>
			<select name="ThnTerakhir" class="text" onkeydown="setDefault(this, document.getElementById('MsgIsi1'));" id="TxtIsi1">
                             <?php
                                if($action == "UBAH"){
                                    echo "<option id='TxtIsi1' value=$ThnTerakhir>$ThnTerakhir</option>";
                                }
                                loadThn4();
                             ?>
                        </select>
                        <span id="MsgIsi1" style="color:#CC0000; font-size:10px;"></span>
                    </td>
                </tr>
                <tr class="head">
                    <td width="31%" >Tgl.Retensi</td><td width="">:</td>
                    <td width="67%">
                        <select name="TglRetensi" class="text" onkeydown="setDefault(this, document.getElementById('MsgIsi2'));" id="TxtIsi2">
                             <?php
                                if($action == "UBAH"){
                                    echo "<option id='TxtIsi2' value=$TglRetensi>$TglRetensi</option>";
                                }
                                loadTglnow();
                             ?>
                        </select>
			<select name="BlnRetensi" class="text" onkeydown="setDefault(this, document.getElementById('MsgIsi2'));" id="TxtIsi2">
                             <?php
                                if($action == "UBAH"){
                                    echo "<option id='TxtIsi2' value=$BlnRetensi>$BlnRetensi</option>";
                                }
                                loadBlnnow();
                             ?>
                        </select>
			<select name="ThnRetensi" class="text" onkeydown="setDefault(this, document.getElementById('MsgIsi2'));" id="TxtIsi2">
                             <?php
                                if($action == "UBAH"){
                                    echo "<option id='TxtIsi2' value=$ThnRetensi>$ThnRetensi</option>";
                                }
                                loadThnnow();
                             ?>
                        </select>
                        <span id="MsgIsi2" style="color:#CC0000; font-size:10px;"></span>
                    </td>
                </tr>                
                <tr class="head">
                    <td width="31%" >File Retensi</td><td width="">:</td>
                    <td width="67%">
                        <input type="button" class="button" id="btnAmbilFoto" value="&nbsp;&nbsp;Ambil Foto&nbsp;&nbsp;">
                        <span id="jmlFoto">0 foto</span>
                        <div id="previewFoto" style="margin-top:6px;"></div>
                        <input type="file" accept="image/*" capture="environment" id="fotoCapture" style="display:none">
                        <input type="file" name="dokumen[]" multiple id="TxtIsi3" style="display:none" onkeydown="setDefault(this, document.getElementById('MsgIsi3'));">
                        <span id="MsgIsi3" style="color:#CC0000; font-size:10px;"></span>
                    </td>
                </tr>
            </table>
            </div>
            <script>
                var daftarFoto = [];

                document.getElementById('btnAmbilFoto').addEventListener('click', function () {
                    document.getElementById('fotoCapture').click();
                });

                document.getElementById('fotoCapture').addEventListener('change', function () {
                    if (this.files && this.files[0]) {
                        daftarFoto.push(this.files[0]);
                        renderPreviewFoto();
                        sinkronInputFoto();
                    }
                    this.value = "";
                });

                function renderPreviewFoto() {
                    var wrap = document.getElementById('previewFoto');
                    wrap.innerHTML = "";
                    daftarFoto.forEach(function (file, idx) {
                        var url = URL.createObjectURL(file);
                        var box = document.createElement('span');
                        box.style.position = "relative";
                        box.style.display = "inline-block";
                        box.style.marginRight = "6px";
                        box.innerHTML = '<img src="' + url + '" style="width:60px;height:60px;object-fit:cover;border:1px solid #999;">' +
                            '<a href="javascript:void(0)" onclick="hapusFoto(' + idx + ')" style="position:absolute;top:-6px;right:-6px;background:#CC0000;color:#fff;border-radius:50%;width:16px;height:16px;font-size:10px;line-height:16px;text-align:center;text-decoration:none;">x</a>';
                        wrap.appendChild(box);
                    });
                    document.getElementById('jmlFoto').innerText = daftarFoto.length + " foto";
                }

                function hapusFoto(idx) {
                    daftarFoto.splice(idx, 1);
                    renderPreviewFoto();
                    sinkronInputFoto();
                }

                function sinkronInputFoto() {
                    var dt = new DataTransfer();
                    daftarFoto.forEach(function (file) {
                        dt.items.add(file);
                    });
                    document.getElementById('TxtIsi3').files = dt.files;
                }
            </script>
            <div align="center"><input name=BtnSimpan type=submit class="button" value="&nbsp;&nbsp;Simpan&nbsp;&nbsp;">&nbsp<input name=BtnKosong type=reset class="button" value="&nbsp;&nbsp;Kosong&nbsp;&nbsp;"></div><br>
            <?php
                $BtnSimpan=isset($_POST['BtnSimpan'])?$_POST['BtnSimpan']:NULL;
                if (isset($BtnSimpan)) {
                    $id                 = validTeks4(trim($_POST['id']),15);
                    $terakhir_daftar    = validTeks4(trim($_POST['ThnTerakhir'])."-".trim($_POST['BlnTerakhir'])."-".trim($_POST['TglTerakhir']),20);
                    $tgl_retensi        = validTeks4(trim($_POST['ThnRetensi'])."-".trim($_POST['BlnRetensi'])."-".trim($_POST['TglRetensi']),20);

                    $fotoTmp = array();
                    if (!empty($_FILES['dokumen']['name'][0])) {
                        foreach ($_FILES['dokumen']['name'] as $i => $namaFoto) {
                            $ext  = strtolower(substr($namaFoto,-4));
                            $ext5 = strtolower(substr($namaFoto,-5));
                            $tipe = $_FILES['dokumen']['type'][$i];
                            $tmp  = $_FILES['dokumen']['tmp_name'][$i];
                            $mime = @mime_content_type($tmp);
                            if((($ext==".jpg")||($ext5==".jpeg"))
                                && (($tipe=='image/jpeg')||($tipe=='image/jpg'))
                                && (($mime=='image/jpeg')||($mime=='image/jpg'))){
                                $fotoTmp[] = $tmp;
                            } else {
                                $fotoTmp = array();
                                break;
                            }
                        }
                    }

                    if (empty($fotoTmp)) {
                        echo "Berkas harus berupa foto JPG/JPEG";
                    } else if (empty($id)) {
                        echo 'Semua field harus isi..!!!';
                    } else {
                        switch($action) {
                            case "TAMBAH":
                                $namaPdf = "pages/upload/retensi_".$id."_".date("YmdHis").".pdf";
                                if (buatPdfDariFoto($fotoTmp, $namaPdf)) {
                                    if(Tambah(" retensi_pasien "," '$id','$terakhir_daftar','$tgl_retensi','$namaPdf'", " Riwayat Retensi " )){
                                        echo"<meta http-equiv='refresh' content='1;URL=?act=Detail&action=TAMBAH&id=$id'>";
                                    }
                                } else {
                                    echo "Gagal membuat PDF dari foto, silakan coba lagi.";
                                }
                                break;
                        }
                    }
                }
            ?>
            <div style="width: 100%; height: 42%; overflow: auto;">
            <?php
                $_sql = "SELECT * from retensi_pasien where retensi_pasien.no_rkm_medis='$id' ORDER BY retensi_pasien.tgl_retensi ASC ";
                $hasil=bukaquery($_sql);
                $jumlah=mysqli_num_rows($hasil);
                $ttllembur=0;
                $ttlhr=0;

                if(mysqli_num_rows($hasil)!=0) {
                    echo "<table width='99.6%' border='0' align='center' cellpadding='0' cellspacing='0' class='tbl_form'>
                            <tr class='head'>
                                <td width='5%'><div align='center'>Proses</div></td>
                                <td width='10%'><div align='center'>Terakhir Daftar</div></td>
                                <td width='10%'><div align='center'>Tgl.Retensi</div></td>
                                <td width='25%'><div align='center'>File Retensi</div></td>
                            </tr>";
                    while($baris = mysqli_fetch_array($hasil)) {                        
                      echo "<tr class='isi'>
                                <td width='70'>
                                    <center>"; ?>
                                    <a href="?act=Detail&action=HAPUS&tgl_retensi=<?php print $baris[2] ?>&id=<?php echo $id ?>&lokasi_pdf=<?php print $baris[3] ?>" >[hapus]</a>
                            <?php
                            echo "</center>
                                </td>
                                <td>$baris[1]</td>
                                <td>$baris[2]</td>
                                <td><a target=_blank href=../medrec/pages/upload/$baris[3]>".str_replace("pages/upload/","",$baris[3])."</a></td>
                           </tr>";
                    }
                echo "</table>";

            } else {echo "<table width='99.6%' border='0' align='center' cellpadding='0' cellspacing='0' class='tbl_form'>
                            <tr class='head'>
                                <td width='5%'><div align='center'>Proses</div></td>
                                <td width='10%'><div align='center'>Terakhir Daftar</div></td>
                                <td width='10%'><div align='center'>Tgl.Retensi</div></td>
                                <td width='25%'><div align='center'>File Retensi</div></td>
                            </tr>
                         </table>";}
        ?>
        </div>
        </form>
        <?php
            if ($action=="HAPUS") {
                $lokasi_pdf_hapus = "pages/upload/".basename($_GET['lokasi_pdf']);
                if (is_file($lokasi_pdf_hapus)) {
                    unlink($lokasi_pdf_hapus);
                }
                Hapus(" retensi_pasien "," no_rkm_medis ='".validTeks($_GET['id'])."' and tgl_retensi ='".validTeks($_GET['tgl_retensi'])."' ","?act=Detail&action=TAMBAH&id=$id");
            }

        
            echo("<table width='99.6%' border='0' align='center' cellpadding='0' cellspacing='0' class='tbl_form'>
                    <tr class='head'>
                        <td><div align='left'>Data : $jumlah</div></td>                        
                    </tr>     
                 </table>");
        
        ?>
    </div>

</div>
