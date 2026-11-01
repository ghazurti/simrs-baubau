-- Alasan berkas rekam medis "Tidak Ada" di menu Mutasi Berkas (webapps/monitoringberkas).
-- Halaman membuat tabel ini otomatis kalau belum ada; file ini untuk dijalankan manual bila perlu.
CREATE TABLE IF NOT EXISTS mutasi_berkas_tidakada (
  no_rawat varchar(17) NOT NULL,
  alasan varchar(150) NOT NULL,
  waktu datetime NOT NULL,
  PRIMARY KEY (no_rawat)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
