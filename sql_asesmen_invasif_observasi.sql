-- ============================================================
-- SQL: Tabel Observasi Tanda Vital Intra & Paska Tindakan
-- Asesmen Invasif Non Bedah - SIMRS Khanza RSUD Baubau
-- ============================================================
CREATE TABLE IF NOT EXISTS `asesmen_invasif_observasi` (
  `id`              int(11)     NOT NULL AUTO_INCREMENT,
  `no_rawat`        varchar(17) NOT NULL,
  `tanggal`         date        NOT NULL,
  `jam_obs`         varchar(8)  NOT NULL COMMENT 'HH:mm format',
  `bp_sistolik`     varchar(10) DEFAULT NULL COMMENT 'mmHg',
  `bp_diastolik`    varchar(10) DEFAULT NULL COMMENT 'mmHg',
  `nadi`            varchar(10) DEFAULT NULL COMMENT 'x/menit',
  `pernapasan`      varchar(10) DEFAULT NULL COMMENT 'x/menit',
  `suhu`            varchar(10) DEFAULT NULL COMMENT 'derajat C',
  `saturasi_o2`     varchar(10) DEFAULT NULL COMMENT '%',
  `pulsasi_distal`  varchar(50) DEFAULT NULL COMMENT 'Adekuat / Tidak Adekuat',
  `reflek_menelan`  varchar(50) DEFAULT NULL COMMENT 'Ada / Tidak Ada',
  `nip`             varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_obs_rawat_tgl` (`no_rawat`,`tanggal`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1
  COMMENT='Observasi Tanda Vital per jam - Asesmen Invasif Non Bedah';
