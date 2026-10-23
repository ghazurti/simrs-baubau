-- Tiket sekali pakai untuk membuka viewer PACS di webapps (webapps/pacs/viewer.php).
-- Khanza membuat tiket saat dokter klik Buka Gambar; viewer.php menukarnya dengan sesi lalu menghapusnya.
-- Khanza juga membuat tabel ini otomatis kalau belum ada.
CREATE TABLE IF NOT EXISTS pacs_embed_tiket (
  tiket char(48) NOT NULL,
  accession varchar(50) NOT NULL,
  judul varchar(200) NOT NULL DEFAULT '',
  username varchar(60) NOT NULL DEFAULT '',
  dibuat datetime NOT NULL,
  kedaluwarsa datetime NOT NULL,
  PRIMARY KEY (tiket),
  KEY kedaluwarsa (kedaluwarsa)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
