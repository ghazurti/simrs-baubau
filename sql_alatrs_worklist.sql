-- =====================================================================
-- Bridging RIS/PACS alat_rs: 1 baris per pemeriksaan (accession)
-- RSUD Kota Baubau
--
-- Accession dibuat oleh RIS/PACS (mis. DX-20261022-0021). Tabel ini mencatat
-- accession itu milik permintaan (noorder) + pemeriksaan (kd_jenis_prw) mana,
-- supaya pasien dengan >1 permintaan bisa ditarik hasil/gambarnya tanpa menebak.
-- bridging_alatrs_log TETAP dipakai (ringkasan per permintaan), tidak diubah.
--
-- Aman dijalankan ulang (CREATE IF NOT EXISTS + INSERT IGNORE).
-- =====================================================================

CREATE TABLE IF NOT EXISTS bridging_alatrs_worklist (
  noorder            VARCHAR(15) NOT NULL,
  kd_jenis_prw       VARCHAR(15) NULL,                  -- NULL hanya utk data lama yang tak bisa dipastikan
  accession_number   VARCHAR(50) NOT NULL,              -- dari RIS/PACS
  worklist_id_alatrs VARCHAR(50) NOT NULL DEFAULT '',   -- id worklist di RIS
  ae_title           VARCHAR(30) NOT NULL DEFAULT '',   -- alat tujuan
  status_hasil       ENUM('Belum','Sudah') NOT NULL DEFAULT 'Belum',
  hasil_status_ris   VARCHAR(20) NOT NULL DEFAULT '',   -- final / preliminary / amended
  hasil_issued_at    VARCHAR(30) NOT NULL DEFAULT '',   -- waktu hasil dikeluarkan radiolog
  tgl_kirim          DATETIME DEFAULT CURRENT_TIMESTAMP,
  tgl_tarik_hasil    DATETIME NULL,                     -- terakhir dicek ke RIS
  PRIMARY KEY (accession_number),
  KEY idx_alatrs_wl_noorder (noorder),
  KEY idx_alatrs_wl_worklist (worklist_id_alatrs),
  CONSTRAINT fk_alatrs_wl_order FOREIGN KEY (noorder)
    REFERENCES permintaan_radiologi (noorder) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_alatrs_wl_prw FOREIGN KEY (kd_jenis_prw)
    REFERENCES jns_perawatan_radiologi (kd_jenis_prw) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ---------------------------------------------------------------------
-- Pindahkan data lama: accession & worklist di bridging_alatrs_log berupa
-- teks dipisah koma -> dipecah 1 baris per accession (maks 10 per permintaan).
-- Pemeriksaan dipasangkan per urutan kd_jenis_prw (urutan saat dikirim), HANYA
-- kalau jumlah accession = jumlah pemeriksaan, atau permintaan cuma 1 pemeriksaan.
-- Selain itu kd_jenis_prw dibiarkan NULL (tidak ditebak).
-- ---------------------------------------------------------------------
INSERT IGNORE INTO bridging_alatrs_worklist
  (noorder, kd_jenis_prw, accession_number, worklist_id_alatrs, status_hasil, tgl_kirim)
SELECT x.noorder,
       CASE
         WHEN x.jml_periksa = 1 THEN
           (SELECT pp.kd_jenis_prw FROM permintaan_pemeriksaan_radiologi pp WHERE pp.noorder = x.noorder LIMIT 1)
         WHEN x.jml_acc = x.jml_periksa THEN
           (SELECT pp.kd_jenis_prw FROM permintaan_pemeriksaan_radiologi pp
             WHERE pp.noorder = x.noorder
               AND (SELECT COUNT(*) FROM permintaan_pemeriksaan_radiologi p2
                     WHERE p2.noorder = pp.noorder AND p2.kd_jenis_prw <= pp.kd_jenis_prw) = x.n
             LIMIT 1)
         ELSE NULL
       END,
       TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(x.accession_number, ',', x.n), ',', -1)),
       IF(x.jml_wl >= x.n AND x.worklist_id_alatrs <> '',
          TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(x.worklist_id_alatrs, ',', x.n), ',', -1)), ''),
       IF(EXISTS(SELECT 1 FROM permintaan_radiologi p
                  INNER JOIN hasil_radiologi h ON h.no_rawat = p.no_rawat
                         AND h.tgl_periksa = p.tgl_hasil AND h.jam = p.jam_hasil
                  WHERE p.noorder = x.noorder), 'Sudah', 'Belum'),
       x.tgl_kirim
FROM (
  SELECT l.noorder, l.accession_number, IFNULL(l.worklist_id_alatrs, '') AS worklist_id_alatrs, l.tgl_kirim, n.n,
         1 + LENGTH(l.accession_number) - LENGTH(REPLACE(l.accession_number, ',', '')) AS jml_acc,
         IF(IFNULL(l.worklist_id_alatrs, '') = '', 0,
            1 + LENGTH(l.worklist_id_alatrs) - LENGTH(REPLACE(l.worklist_id_alatrs, ',', ''))) AS jml_wl,
         (SELECT COUNT(*) FROM permintaan_pemeriksaan_radiologi pp WHERE pp.noorder = l.noorder) AS jml_periksa
  FROM bridging_alatrs_log l
  INNER JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
              UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) n
          ON n.n <= 1 + LENGTH(l.accession_number) - LENGTH(REPLACE(l.accession_number, ',', ''))
  WHERE IFNULL(l.accession_number, '') <> ''
) x
WHERE TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(x.accession_number, ',', x.n), ',', -1)) <> '';

-- Cek hasil pemindahan
SELECT COUNT(*) AS jml_baris,
       COUNT(DISTINCT noorder) AS jml_permintaan,
       SUM(kd_jenis_prw IS NULL) AS pemeriksaan_tak_pasti,
       SUM(status_hasil = 'Sudah') AS sudah_ada_hasil
FROM bridging_alatrs_worklist;
