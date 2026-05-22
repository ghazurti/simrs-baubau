-- Tabel odontogram: per gigi per kunjungan
CREATE TABLE IF NOT EXISTS `odontogram` (
  `id`                INT           NOT NULL AUTO_INCREMENT,
  `no_rawat`          VARCHAR(17)   NOT NULL,
  `no_permintaan`     VARCHAR(25)   DEFAULT '',
  `no_gigi`           VARCHAR(3)    NOT NULL,
  `hasil_pemeriksaan` VARCHAR(50)   DEFAULT '',
  `rahang`            VARCHAR(20)   DEFAULT '',
  `diagnosa_gigi`     VARCHAR(255)  DEFAULT '',
  `kd_icd`            VARCHAR(10)   DEFAULT '',
  `catatan`           VARCHAR(255)  DEFAULT '',
  `status_gigi`       VARCHAR(20)   DEFAULT '',
  `tanggal`           DATE          DEFAULT NULL,
  `kd_dokter`         VARCHAR(20)   DEFAULT NULL,
  `nm_dokter`         VARCHAR(100)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_no_rawat` (`no_rawat`),
  KEY `idx_no_gigi`  (`no_gigi`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Hak akses odontogram
ALTER TABLE `hak_akses`
  ADD COLUMN IF NOT EXISTS `odontogram` TINYINT(1) DEFAULT 0;
