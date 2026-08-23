-- ============================================================
-- Filter obat Formularium Nasional untuk pasien BPJS
--
-- Pendekatan: tabel terpisah `obat_fornas` (bukan ALTER databarang)
-- karena `menyimpantf` di DlgBarang pakai `insert into databarang
-- values(...)` tanpa nama kolom. Menambah kolom di databarang akan
-- memutus fungsi tambah obat baru.
--
-- Sifat: aditif — tabel baru, tidak menyentuh databarang.
-- Rollback aman: DROP TABLE obat_fornas.
-- ============================================================

CREATE TABLE IF NOT EXISTS `obat_fornas` (
  `kode_brng`  VARCHAR(15) NOT NULL PRIMARY KEY,
  `restriksi`  TEXT DEFAULT NULL,      -- catatan restriksi Fornas (mis. "hanya untuk indikasi X")
  `keterangan` TEXT DEFAULT NULL,      -- catatan admin
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT `obat_fornas_ibfk_1` FOREIGN KEY (`kode_brng`)
    REFERENCES `databarang` (`kode_brng`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Cara pakai:
--   * INSERT ke obat_fornas   → obat menjadi Fornas
--   * DELETE dari obat_fornas → obat kembali non-Fornas
--   * Filter query lookup obat pasien BPJS:
--       INNER JOIN obat_fornas ON obat_fornas.kode_brng = databarang.kode_brng
--     atau:
--       WHERE EXISTS (SELECT 1 FROM obat_fornas WHERE kode_brng = databarang.kode_brng)
--
-- Rollback: DROP TABLE obat_fornas;
