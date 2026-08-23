-- ============================================================
-- Tambahan petugas cathlab: Asisten Operator 1-3, Monitoring 1-3,
-- Nurse 1-3. Semua kolom bersifat opsional (nullable, default 0).
--
-- Sifat: aditif — hanya ADD COLUMN pada tabel `operasi_cathlab`
-- yang memang milik menu ini. `paket_operasi` TIDAK diubah untuk
-- monitoring/nurse (tariff default dientri manual per record).
-- Untuk asisten operator: reuse kolom `asisten_operator1/2/3` yang
-- SUDAH ADA di `paket_operasi` sejak awal — cocok semantik.
--
-- SAFE: INSERT/UPDATE di form pakai kolom eksplisit (bukan
--       positional `values(?...)` tanpa nama), jadi ALTER TABLE
--       tidak akan memutus statement existing.
-- ============================================================

ALTER TABLE `operasi_cathlab`
  ADD COLUMN IF NOT EXISTS `asisten_operator1`      varchar(20) DEFAULT NULL AFTER `biaya_operator3`,
  ADD COLUMN IF NOT EXISTS `asisten_operator2`      varchar(20) DEFAULT NULL AFTER `asisten_operator1`,
  ADD COLUMN IF NOT EXISTS `asisten_operator3`      varchar(20) DEFAULT NULL AFTER `asisten_operator2`,
  ADD COLUMN IF NOT EXISTS `biaya_asisten_operator1` double DEFAULT 0 AFTER `asisten_operator3`,
  ADD COLUMN IF NOT EXISTS `biaya_asisten_operator2` double DEFAULT 0 AFTER `biaya_asisten_operator1`,
  ADD COLUMN IF NOT EXISTS `biaya_asisten_operator3` double DEFAULT 0 AFTER `biaya_asisten_operator2`,

  ADD COLUMN IF NOT EXISTS `monitoring1` varchar(20) DEFAULT NULL AFTER `biaya_radiographer1`,
  ADD COLUMN IF NOT EXISTS `monitoring2` varchar(20) DEFAULT NULL AFTER `monitoring1`,
  ADD COLUMN IF NOT EXISTS `monitoring3` varchar(20) DEFAULT NULL AFTER `monitoring2`,
  ADD COLUMN IF NOT EXISTS `biaya_monitoring1` double DEFAULT 0 AFTER `monitoring3`,
  ADD COLUMN IF NOT EXISTS `biaya_monitoring2` double DEFAULT 0 AFTER `biaya_monitoring1`,
  ADD COLUMN IF NOT EXISTS `biaya_monitoring3` double DEFAULT 0 AFTER `biaya_monitoring2`,

  ADD COLUMN IF NOT EXISTS `nurse1` varchar(20) DEFAULT NULL AFTER `biaya_monitoring3`,
  ADD COLUMN IF NOT EXISTS `nurse2` varchar(20) DEFAULT NULL AFTER `nurse1`,
  ADD COLUMN IF NOT EXISTS `nurse3` varchar(20) DEFAULT NULL AFTER `nurse2`,
  ADD COLUMN IF NOT EXISTS `biaya_nurse1` double DEFAULT 0 AFTER `nurse3`,
  ADD COLUMN IF NOT EXISTS `biaya_nurse2` double DEFAULT 0 AFTER `biaya_nurse1`,
  ADD COLUMN IF NOT EXISTS `biaya_nurse3` double DEFAULT 0 AFTER `biaya_nurse2`;

-- Foreign keys (opsional — kalau MariaDB kompatibel)
ALTER TABLE `operasi_cathlab`
  ADD KEY IF NOT EXISTS `asisten_operator1` (`asisten_operator1`),
  ADD KEY IF NOT EXISTS `asisten_operator2` (`asisten_operator2`),
  ADD KEY IF NOT EXISTS `asisten_operator3` (`asisten_operator3`),
  ADD KEY IF NOT EXISTS `monitoring1`       (`monitoring1`),
  ADD KEY IF NOT EXISTS `monitoring2`       (`monitoring2`),
  ADD KEY IF NOT EXISTS `monitoring3`       (`monitoring3`),
  ADD KEY IF NOT EXISTS `nurse1`            (`nurse1`),
  ADD KEY IF NOT EXISTS `nurse2`            (`nurse2`),
  ADD KEY IF NOT EXISTS `nurse3`            (`nurse3`);

-- ============================================================
-- SANITY CHECK
-- ============================================================
-- Setelah eksekusi:
--   DESC operasi_cathlab;
--   -- Cek ada 18 kolom baru: asisten_operator1/2/3, monitoring1/2/3, nurse1/2/3
--   -- dan biaya masing-masing
--
-- Rollback bila perlu:
--   ALTER TABLE operasi_cathlab
--     DROP COLUMN asisten_operator1, DROP COLUMN asisten_operator2, DROP COLUMN asisten_operator3,
--     DROP COLUMN biaya_asisten_operator1, DROP COLUMN biaya_asisten_operator2, DROP COLUMN biaya_asisten_operator3,
--     DROP COLUMN monitoring1, DROP COLUMN monitoring2, DROP COLUMN monitoring3,
--     DROP COLUMN biaya_monitoring1, DROP COLUMN biaya_monitoring2, DROP COLUMN biaya_monitoring3,
--     DROP COLUMN nurse1, DROP COLUMN nurse2, DROP COLUMN nurse3,
--     DROP COLUMN biaya_nurse1, DROP COLUMN biaya_nurse2, DROP COLUMN biaya_nurse3;
