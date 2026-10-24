-- Satu Sehat: Mapping KPTL (diambil dari sik.sql SIMRS Khanza upstream)
-- Aman dijalankan ulang: hanya membuat tabel baru & menambah kolom hak akses.

CREATE TABLE IF NOT EXISTS `satu_sehat_mapping_tarifkamar_kptl` (
  `kd_kamar` varchar(15) NOT NULL,
  `code` varchar(30) DEFAULT NULL,
  `system` varchar(100) NOT NULL,
  `display` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`kd_kamar`),
  CONSTRAINT `satu_sehat_mapping_tarifkamar_kptl_ibfk_1` FOREIGN KEY (`kd_kamar`) REFERENCES `kamar` (`kd_kamar`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `satu_sehat_mapping_tindakanlab_kptl` (
  `kd_jenis_prw` varchar(15) NOT NULL,
  `code` varchar(30) DEFAULT NULL,
  `system` varchar(100) NOT NULL,
  `display` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`kd_jenis_prw`),
  CONSTRAINT `satu_sehat_mapping_tindakanlab_kptl_ibfk_1` FOREIGN KEY (`kd_jenis_prw`) REFERENCES `jns_perawatan_lab` (`kd_jenis_prw`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `satu_sehat_mapping_tindakanoperasi_kptl` (
  `kode_paket` varchar(15) NOT NULL,
  `code` varchar(30) DEFAULT NULL,
  `system` varchar(100) NOT NULL,
  `display` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`kode_paket`),
  CONSTRAINT `satu_sehat_mapping_tindakanoperasi_kptl_ibfk_1` FOREIGN KEY (`kode_paket`) REFERENCES `paket_operasi` (`kode_paket`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `satu_sehat_mapping_tindakanradiologi_kptl` (
  `kd_jenis_prw` varchar(15) NOT NULL,
  `code` varchar(30) DEFAULT NULL,
  `system` varchar(100) NOT NULL,
  `display` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`kd_jenis_prw`),
  CONSTRAINT `satu_sehat_mapping_tindakanradiologi_kptl_ibfk_1` FOREIGN KEY (`kd_jenis_prw`) REFERENCES `jns_perawatan_radiologi` (`kd_jenis_prw`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `satu_sehat_mapping_tindakanralan_kptl` (
  `kd_jenis_prw` varchar(15) NOT NULL,
  `code` varchar(30) DEFAULT NULL,
  `system` varchar(100) NOT NULL,
  `display` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`kd_jenis_prw`),
  CONSTRAINT `satu_sehat_mapping_tindakanralan_kptl_ibfk_1` FOREIGN KEY (`kd_jenis_prw`) REFERENCES `jns_perawatan` (`kd_jenis_prw`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `satu_sehat_mapping_tindakanranap_kptl` (
  `kd_jenis_prw` varchar(15) NOT NULL,
  `code` varchar(30) DEFAULT NULL,
  `system` varchar(100) NOT NULL,
  `display` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`kd_jenis_prw`),
  CONSTRAINT `satu_sehat_mapping_tindakanranap_kptl_ibfk_1` FOREIGN KEY (`kd_jenis_prw`) REFERENCES `jns_perawatan_inap` (`kd_jenis_prw`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Hak akses menu (MySQL 8 / MariaDB 10.0.2+ mendukung IF NOT EXISTS pada ADD COLUMN;
-- untuk MySQL 5.7 hapus "IF NOT EXISTS" dan jalankan sekali saja)
ALTER TABLE user ADD COLUMN IF NOT EXISTS satu_sehat_mapping_kptl_tindakan_ralan ENUM('true','false') NOT NULL DEFAULT 'false';
ALTER TABLE user ADD COLUMN IF NOT EXISTS satu_sehat_mapping_kptl_tindakan_ranap ENUM('true','false') NOT NULL DEFAULT 'false';
ALTER TABLE user ADD COLUMN IF NOT EXISTS satu_sehat_mapping_kptl_tindakan_radiologi ENUM('true','false') NOT NULL DEFAULT 'false';
ALTER TABLE user ADD COLUMN IF NOT EXISTS satu_sehat_mapping_kptl_tindakan_laborat ENUM('true','false') NOT NULL DEFAULT 'false';
ALTER TABLE user ADD COLUMN IF NOT EXISTS satu_sehat_mapping_kptl_tindakan_operasi ENUM('true','false') NOT NULL DEFAULT 'false';
ALTER TABLE user ADD COLUMN IF NOT EXISTS satu_sehat_mapping_kptl_tarif_kamar ENUM('true','false') NOT NULL DEFAULT 'false';
