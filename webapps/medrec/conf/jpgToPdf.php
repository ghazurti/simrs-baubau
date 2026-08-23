<?php
/**
 * Gabungkan beberapa foto JPEG jadi satu file PDF (1 foto = 1 halaman).
 * Ditulis manual (embed JPEG lewat filter DCTDecode) supaya tidak perlu
 * library pihak ketiga / dependency GD-Imagick di server.
 */
function kompresFotoUntukPdf($path, $info) {
    $maxSisi = 2000;
    $kualitas = 75;

    if (!function_exists('imagecreatefromjpeg')) {
        return null;
    }
    $img = @imagecreatefromjpeg($path);
    if ($img === false) {
        return null;
    }

    $w = imagesx($img);
    $h = imagesy($img);
    $sisiTerpanjang = max($w, $h);
    if ($sisiTerpanjang > $maxSisi) {
        $skala = $maxSisi / $sisiTerpanjang;
        $wBaru = (int) round($w * $skala);
        $hBaru = (int) round($h * $skala);
        $resized = imagecreatetruecolor($wBaru, $hBaru);
        imagecopyresampled($resized, $img, 0, 0, 0, 0, $wBaru, $hBaru, $w, $h);
        imagedestroy($img);
        $img = $resized;
        $w = $wBaru;
        $h = $hBaru;
    }

    ob_start();
    imagejpeg($img, null, $kualitas);
    $imgData = ob_get_clean();
    imagedestroy($img);

    if ($imgData === false || strlen($imgData) === 0) {
        return null;
    }

    return array('data' => $imgData, 'width' => $w, 'height' => $h);
}

function buatPdfDariFoto($daftarFotoTmp, $outputPath) {
    if (empty($daftarFotoTmp)) {
        return false;
    }

    $pageWidth  = 595;
    $pageHeight = 842;
    $margin     = 20;

    $nextId  = 3; // 1 = Catalog, 2 = Pages
    $bodies  = array();
    $order   = array();
    $pageIds = array();

    foreach ($daftarFotoTmp as $idx => $path) {
        if (!is_uploaded_file($path)) {
            return false;
        }
        $info = @getimagesize($path);
        if ($info === false || $info[2] !== IMAGETYPE_JPEG) {
            return false;
        }

        $kompres = kompresFotoUntukPdf($path, $info);
        if ($kompres !== null) {
            $imgData    = $kompres['data'];
            $w          = $kompres['width'];
            $h          = $kompres['height'];
            $colorSpace = '/DeviceRGB';
        } else {
            $imgData = @file_get_contents($path);
            if ($imgData === false || strlen($imgData) === 0) {
                return false;
            }
            $w = $info[0];
            $h = $info[1];
            $channels = isset($info['channels']) ? $info['channels'] : 3;
            $colorSpace = '/DeviceRGB';
            if ($channels == 1) {
                $colorSpace = '/DeviceGray';
            } else if ($channels == 4) {
                $colorSpace = '/DeviceCMYK';
            }
        }

        $scale = min(($pageWidth - 2 * $margin) / $w, ($pageHeight - 2 * $margin) / $h);
        $drawW = $w * $scale;
        $drawH = $h * $scale;
        $x = ($pageWidth - $drawW) / 2;
        $y = ($pageHeight - $drawH) / 2;

        $pageId    = $nextId++;
        $contentId = $nextId++;
        $imageId   = $nextId++;
        $imgName   = "Im" . $idx;

        $contentStream = sprintf("q\n%.2F 0 0 %.2F %.2F %.2F cm\n/%s Do\nQ\n", $drawW, $drawH, $x, $y, $imgName);

        $bodies[$pageId] = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $pageWidth $pageHeight] " .
                           "/Resources << /XObject << /$imgName $imageId 0 R >> >> " .
                           "/Contents $contentId 0 R >>";

        $bodies[$contentId] = array('stream' => $contentStream);

        $bodies[$imageId] = array(
            'dict'   => "<< /Type /XObject /Subtype /Image /Width $w /Height $h /ColorSpace $colorSpace /BitsPerComponent 8 /Filter /DCTDecode /Length " . strlen($imgData) . " >>",
            'stream' => $imgData
        );

        $pageIds[] = $pageId;
        $order[] = $pageId;
        $order[] = $contentId;
        $order[] = $imageId;
    }

    if (empty($pageIds)) {
        return false;
    }

    $kids = implode(' ', array_map(function ($id) {
        return "$id 0 R";
    }, $pageIds));
    $bodies[1] = "<< /Type /Catalog /Pages 2 0 R >>";
    $bodies[2] = "<< /Type /Pages /Kids [ $kids ] /Count " . count($pageIds) . " >>";

    $pdf = "%PDF-1.4\n%\xE2\xE3\xCF\xD3\n";
    $offsets = array();
    $allIds = array_merge(array(1, 2), $order);

    foreach ($allIds as $id) {
        $offsets[$id] = strlen($pdf);
        $body = $bodies[$id];
        if (is_array($body)) {
            $pdf .= "$id 0 obj\n";
            if (isset($body['dict'])) {
                $pdf .= $body['dict'] . "\nstream\n" . $body['stream'] . "\nendstream\nendobj\n";
            } else {
                $pdf .= "<< /Length " . strlen($body['stream']) . " >>\nstream\n" . $body['stream'] . "\nendstream\nendobj\n";
            }
        } else {
            $pdf .= "$id 0 obj\n$body\nendobj\n";
        }
    }

    $xrefStart = strlen($pdf);
    $maxId = max($allIds);
    $pdf .= "xref\n0 " . ($maxId + 1) . "\n";
    $pdf .= "0000000000 65535 f \n";
    for ($i = 1; $i <= $maxId; $i++) {
        if (isset($offsets[$i])) {
            $pdf .= sprintf("%010d 00000 n \n", $offsets[$i]);
        } else {
            $pdf .= "0000000000 00000 f \n";
        }
    }
    $pdf .= "trailer\n<< /Size " . ($maxId + 1) . " /Root 1 0 R >>\nstartxref\n$xrefStart\n%%EOF";

    return @file_put_contents($outputPath, $pdf) !== false;
}
