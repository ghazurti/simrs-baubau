-- ============================================================
-- Jadwal Hemodialisa
--
-- Tabel penjadwalan HD (mirip booking_operasi tapi lebih ringkas).
-- Petugas HD entry jadwal harian pagi hari. Digunakan apotek untuk
-- deteksi konteks HD saat validasi resep — tampil marker [HD] di
-- kolom Ruang/Kamar.
--
-- PK: no_rawat + tanggal + jam_mulai (pasien bisa HD >1x sehari
-- kalau perlu, walaupun jarang).
--
-- Sifat: aditif — tidak mengubah tabel existing.
-- Rollback: DROP TABLE jadwal_hemodialisa;
-- ============================================================

CREATE TABLE IF NOT EXISTS `jadwal_hemodialisa` (
  `no_rawat`    varchar(17) NOT NULL,
  `tanggal`     date        NOT NULL,
  `jam_mulai`   time        NOT NULL DEFAULT '00:00:00',
  `jam_selesai` time        DEFAULT NULL,
  `kd_dokter`   varchar(20) DEFAULT NULL,
  `keterangan`  varchar(200) DEFAULT NULL,
  `status`      enum('Terjadwal','Selesai','Batal') DEFAULT 'Terjadwal',
  PRIMARY KEY (`no_rawat`,`tanggal`,`jam_mulai`),
  KEY `tanggal`   (`tanggal`),
  KEY `kd_dokter` (`kd_dokter`),
  CONSTRAINT `jadwal_hemodialisa_ibfk_1` FOREIGN KEY (`no_rawat`)
    REFERENCES `reg_periksa` (`no_rawat`) ON UPDATE CASCADE,
  CONSTRAINT `jadwal_hemodialisa_ibfk_2` FOREIGN KEY (`kd_dokter`)
    REFERENCES `dokter` (`kd_dokter`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Cek isi:
--   SELECT jh.no_rawat, jh.tanggal, jh.jam_mulai, jh.status,
--          p.nm_pasien, d.nm_dokter
--   FROM jadwal_hemodialisa jh
--   INNER JOIN reg_periksa r ON jh.no_rawat=r.no_rawat
--   INNER JOIN pasien p ON r.no_rkm_medis=p.no_rkm_medis
--   LEFT JOIN dokter d ON jh.kd_dokter=d.kd_dokter
--   ORDER BY jh.tanggal DESC, jh.jam_mulai;
