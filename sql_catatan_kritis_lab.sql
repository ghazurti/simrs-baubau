-- Catatan pelaporan nilai kritis lab dari eLIMS/PROLIMS
-- Sumber : sik_bridging_lab.detail_hasil_lab.catatan (diisi PROLIMS)
-- Disalin ke Khanza saat hasil disimpan dari "Ambil Hasil dari ELIMS".
-- Terhubung ke detail_periksa_lab: hasil dihapus -> catatan ikut terhapus.
-- Jalankan di database Khanza (sik-baru), BUKAN di sik_bridging_lab.

CREATE TABLE IF NOT EXISTS `catatan_kritis_lab` (
  `no_rawat` varchar(17) NOT NULL,
  `kd_jenis_prw` varchar(15) NOT NULL,
  `tgl_periksa` date NOT NULL,
  `jam` time NOT NULL,
  `id_template` int(11) NOT NULL,
  `catatan` varchar(800) NOT NULL,
  PRIMARY KEY (`no_rawat`,`kd_jenis_prw`,`tgl_periksa`,`jam`,`id_template`),
  CONSTRAINT `catatan_kritis_lab_ibfk_1` FOREIGN KEY (`no_rawat`,`kd_jenis_prw`,`tgl_periksa`,`jam`,`id_template`)
    REFERENCES `detail_periksa_lab` (`no_rawat`,`kd_jenis_prw`,`tgl_periksa`,`jam`,`id_template`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
