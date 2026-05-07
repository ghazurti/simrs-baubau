-- Tabel penyimpanan IHS Patient ID dari Satu Sehat
-- Jalankan di database: sik
-- Prasyarat: kolom no_ktp di tabel pasien sudah ada index (sudah ada di sik.sql default)

CREATE TABLE IF NOT EXISTS `satu_sehat_ihs_patient` (
  `nikpasien`  varchar(20)  NOT NULL,
  `ihspasien`  varchar(100) DEFAULT NULL,
  PRIMARY KEY (`nikpasien`) USING BTREE,
  CONSTRAINT `satu_sehat_ihs_pasien_ibfk_1`
    FOREIGN KEY (`nikpasien`)
    REFERENCES `pasien` (`no_ktp`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=latin1
  COLLATE=latin1_swedish_ci
  ROW_FORMAT=COMPACT;
