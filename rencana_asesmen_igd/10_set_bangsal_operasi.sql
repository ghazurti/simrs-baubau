-- ============================================================
-- Setting depo obat per ruang operasi
--
-- Mapping kd_ruang_ok (dari master `ruang_ok`) → kd_depo (dari
-- master `bangsal`, karena depo obat disimpan sebagai bangsal).
-- Multi-row: tiap OK bisa punya depo sendiri (mis. Cathlab pakai
-- depo Cathlab, IBS I pakai depo IBS, dst).
--
-- Dipakai saat validasi resep yang dibuat dari Jadwal Operasi:
-- sistem cari kd_ruang_ok dari booking_operasi untuk hari itu,
-- lalu resolve depo-nya dari tabel ini.
--
-- Sifat: aditif — tidak mengubah tabel existing.
-- Rollback: DROP TABLE set_depo_ruang_ok;
-- ============================================================

CREATE TABLE IF NOT EXISTS `set_depo_ruang_ok` (
  `kd_ruang_ok` char(2) NOT NULL,
  `kd_depo`     char(5) NOT NULL,
  PRIMARY KEY (`kd_ruang_ok`),
  KEY `kd_depo` (`kd_depo`),
  CONSTRAINT `set_depo_ruang_ok_ibfk_1` FOREIGN KEY (`kd_ruang_ok`)
    REFERENCES `ruang_ok` (`kd_ruang_ok`) ON UPDATE CASCADE,
  CONSTRAINT `set_depo_ruang_ok_ibfk_2` FOREIGN KEY (`kd_depo`)
    REFERENCES `bangsal` (`kd_bangsal`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Kalau tabel sudah terlanjur di-create dengan varchar(4), jalankan ALTER:
-- ALTER TABLE set_depo_ruang_ok MODIFY COLUMN kd_depo char(5) NOT NULL;

-- Contoh: petakan Cathlab (kode 05) ke depo bangsal 'BOP1':
--   INSERT INTO set_depo_ruang_ok VALUES ('05','BOP1');
--   INSERT INTO set_depo_ruang_ok VALUES ('O1','BOP2');

-- Cek isi setelah setup:
--   SELECT r.kd_ruang_ok, r.nm_ruang_ok, s.kd_depo, b.nm_bangsal AS nm_depo
--   FROM ruang_ok r
--   LEFT JOIN set_depo_ruang_ok s ON r.kd_ruang_ok=s.kd_ruang_ok
--   LEFT JOIN bangsal b ON s.kd_depo=b.kd_bangsal;
