-- Tabel penyimpanan IHS Practitioner ID dari Satu Sehat
-- Jalankan di database: sik
-- Dipakai service SatuSehat agar ID praktisi tidak dicari ulang ke API di tiap kiriman

CREATE TABLE IF NOT EXISTS `satu_sehat_ihs_practitioner` (
  `nikpraktisi`  varchar(20)  NOT NULL,
  `ihspraktisi`  varchar(100) DEFAULT NULL,
  PRIMARY KEY (`nikpraktisi`) USING BTREE
) ENGINE=InnoDB
  DEFAULT CHARSET=latin1
  COLLATE=latin1_swedish_ci
  ROW_FORMAT=COMPACT;
