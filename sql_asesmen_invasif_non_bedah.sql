-- ============================================================
-- SQL: Tabel Asesmen Keperawatan Invasif Non Bedah
-- SIMRS Khanza - RSUD Kota Baubau
-- ============================================================

CREATE TABLE IF NOT EXISTS `asesmen_keperawatan_invasif_non_bedah` (
  `no_rawat`                  varchar(17)   NOT NULL,
  `tanggal`                   datetime      NOT NULL,
  `jam_tiba_rr`               varchar(10)   DEFAULT NULL COMMENT 'Jam pasien tiba di Recovery Room',
  `lokasi_pungsi`             varchar(100)  DEFAULT NULL COMMENT 'Lokasi pungsi / sayatan',
  `jumlah_pungsi`             varchar(10)   DEFAULT NULL COMMENT 'Jumlah pungsi',
  `terpasang_sheath`          varchar(10)   DEFAULT NULL COMMENT 'Ya / Tidak',
  `lokasi_sheath`             varchar(100)  DEFAULT NULL COMMENT 'Lokasi sheath terpasang',
  `pulsasi_radialis_kanan`    varchar(50)   DEFAULT NULL COMMENT 'Adekuat / Tidak Adekuat',
  `pulsasi_radialis_kiri`     varchar(50)   DEFAULT NULL COMMENT 'Adekuat / Tidak Adekuat',
  `pulsasi_dorsalis_kanan`    varchar(50)   DEFAULT NULL COMMENT 'Adekuat / Tidak Adekuat',
  `pulsasi_dorsalis_kiri`     varchar(50)   DEFAULT NULL COMMENT 'Adekuat / Tidak Adekuat',
  `alat_terpasang`            varchar(200)  DEFAULT NULL COMMENT 'IV Cath, TPM, PPM, Dowercath, IABP, dsb',
  `jenis_anestesi`            varchar(50)   DEFAULT NULL COMMENT 'Umum / Lokal',
  `sedasi`                    varchar(100)  DEFAULT NULL,
  `antikoagulan_dosis_pre`    varchar(100)  DEFAULT NULL COMMENT 'Dosis antikoagulan pre prosedur',
  `antikoagulan_dosis_intra`  varchar(100)  DEFAULT NULL COMMENT 'Dosis antikoagulan intra prosedur',
  `antikoagulan_dosis_post`   varchar(100)  DEFAULT NULL COMMENT 'Dosis antikoagulan post prosedur',
  `komplikasi`                varchar(255)  DEFAULT NULL COMMENT 'Hematoma, Laserasi, Reaksi Alergi, dll',
  `balance_cairan_intake`     varchar(100)  DEFAULT NULL COMMENT 'Intake cairan (ml)',
  `balance_cairan_output`     varchar(100)  DEFAULT NULL COMMENT 'Output cairan: Perdarahan, Urin (ml)',
  `instruksi_paska_tindakan`  text          DEFAULT NULL COMMENT 'Immobilisasi, EKG, Antibiotik, Hidrasi, dll',
  `masalah_keperawatan`       text          DEFAULT NULL COMMENT 'Penurunan curah jantung, Nyeri, Risiko perdarahan, dll',
  `rencana_keperawatan`       text          DEFAULT NULL COMMENT 'Manajemen nyeri, Monitoring TTV, dll',
  `keputusan_rawat`           varchar(50)   DEFAULT NULL COMMENT 'Rawat Inap / Pulang / Rujuk / Meninggal',
  `nip`                       varchar(20)   NOT NULL COMMENT 'NIP/Kode petugas yang melakukan observasi',
  PRIMARY KEY (`no_rawat`, `tanggal`),
  KEY `fk_invasif_reg_periksa` (`no_rawat`),
  KEY `fk_invasif_petugas` (`nip`),
  CONSTRAINT `fk_invasif_reg_periksa`
    FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_invasif_petugas`
    FOREIGN KEY (`nip`) REFERENCES `petugas` (`nip`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Asesmen Awal Keperawatan Tindakan Invasif Non Bedah - F.V.3.1/029/B/2015';
