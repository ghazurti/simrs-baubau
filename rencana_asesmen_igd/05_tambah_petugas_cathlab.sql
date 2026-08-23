-- ============================================================
-- Menu KHUSUS Operasi Cathlab (terpisah dari tabel `operasi`)
--
-- Pendekatan: bikin tabel `operasi_cathlab` mandiri supaya:
--   - Form Tagihan Operasi existing (DlgTagihanOperasi) TIDAK
--     tersentuh sama sekali → zero risk memutus fungsi save operasi
--   - Field khusus cathlab (jenis prosedur, kateter, dosis
--     radiasi, waktu fluoroskopi, scrub/circulate/radiographer)
--     jadi first-class fields di tabel baru
--
-- Sifat: aditif — hanya CREATE TABLE. Data lama tetap.
-- Rollback aman: DROP TABLE operasi_cathlab kalau perlu.
-- ============================================================

CREATE TABLE IF NOT EXISTS `operasi_cathlab` (
    -- Identitas record
    `no_rawat`         varchar(17) NOT NULL,
    `tgl_operasi`      datetime    NOT NULL,
    `kode_paket`       varchar(15) NOT NULL,

    -- Standar operasi
    `jenis_anasthesi`  varchar(20) DEFAULT NULL,
    `kategori`         enum('-','Khusus','Besar','Sedang','Kecil','Elektive','Emergency') DEFAULT '-',
    `jam_selesai`      datetime    DEFAULT NULL,
    `diagnosis_pre`    varchar(255) DEFAULT NULL,
    `diagnosis_post`   varchar(255) DEFAULT NULL,
    `laporan_operasi`  text        DEFAULT NULL,

    -- Khusus cathlab: jenis prosedur
    `jenis_prosedur`   enum('Angiografi Diagnostik','PCI (Percutaneous Coronary Intervention)',
                            'TAVI (Transcatheter Aortic Valve Implantation)',
                            'Ablasi Aritmia','Pacemaker/ICD','Ballooning',
                            'IABP (Intra-Aortic Balloon Pump)','Peripheral Angiography',
                            'Lainnya') DEFAULT 'Angiografi Diagnostik',
    `nomor_kateter`    varchar(100) DEFAULT NULL,
    `alat_khusus`      text         DEFAULT NULL,

    -- Safety report radiasi (untuk akreditasi RS)
    `dosis_radiasi_mgy`     double DEFAULT 0,
    `waktu_fluoroskopi_min` double DEFAULT 0,
    `kontras_ml`            double DEFAULT 0,

    -- Dokter operator (utama + 2 asisten)
    `operator1`            varchar(20) DEFAULT NULL,
    `operator2`            varchar(20) DEFAULT NULL,
    `operator3`            varchar(20) DEFAULT NULL,
    `biaya_operator1`      double      DEFAULT 0,
    `biaya_operator2`      double      DEFAULT 0,
    `biaya_operator3`      double      DEFAULT 0,

    -- Dokter anestesi (opsional pada cathlab tapi sering ada)
    `dokter_anestesi`      varchar(20) DEFAULT NULL,
    `biaya_dokter_anestesi` double     DEFAULT 0,

    -- Petugas cathlab: 2 Scrub + 2 Circulate + 1 Radiographer
    `scrub_nurse1`         varchar(20) DEFAULT NULL,
    `scrub_nurse2`         varchar(20) DEFAULT NULL,
    `circulate_nurse1`     varchar(20) DEFAULT NULL,
    `circulate_nurse2`     varchar(20) DEFAULT NULL,
    `radiographer1`        varchar(20) DEFAULT NULL,
    `biaya_scrub_nurse1`     double DEFAULT 0,
    `biaya_scrub_nurse2`     double DEFAULT 0,
    `biaya_circulate_nurse1` double DEFAULT 0,
    `biaya_circulate_nurse2` double DEFAULT 0,
    `biaya_radiographer1`    double DEFAULT 0,

    -- Biaya lain (mengikuti struktur `operasi`)
    `biayaalat`      double DEFAULT 0,
    `biayasewaok`    double DEFAULT 0,
    `akomodasi`      double DEFAULT 0,
    `bagian_rs`      double DEFAULT 0,
    `biayasarpras`   double DEFAULT 0,

    -- Status pasien
    `status`         enum('Ranap','Ralan') DEFAULT NULL,

    PRIMARY KEY (`no_rawat`,`tgl_operasi`,`kode_paket`),
    KEY `no_rawat`         (`no_rawat`),
    KEY `operator1`        (`operator1`),
    KEY `operator2`        (`operator2`),
    KEY `operator3`        (`operator3`),
    KEY `dokter_anestesi`  (`dokter_anestesi`),
    KEY `scrub_nurse1`     (`scrub_nurse1`),
    KEY `scrub_nurse2`     (`scrub_nurse2`),
    KEY `circulate_nurse1` (`circulate_nurse1`),
    KEY `circulate_nurse2` (`circulate_nurse2`),
    KEY `radiographer1`    (`radiographer1`),
    KEY `kode_paket`       (`kode_paket`),
    KEY `jenis_prosedur`   (`jenis_prosedur`),

    CONSTRAINT `operasi_cathlab_ibfk_1` FOREIGN KEY (`no_rawat`)         REFERENCES `reg_periksa` (`no_rawat`)   ON UPDATE CASCADE,
    CONSTRAINT `operasi_cathlab_ibfk_2` FOREIGN KEY (`operator1`)        REFERENCES `dokter`      (`kd_dokter`)  ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `operasi_cathlab_ibfk_3` FOREIGN KEY (`operator2`)        REFERENCES `dokter`      (`kd_dokter`)  ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `operasi_cathlab_ibfk_4` FOREIGN KEY (`operator3`)        REFERENCES `dokter`      (`kd_dokter`)  ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `operasi_cathlab_ibfk_5` FOREIGN KEY (`dokter_anestesi`)  REFERENCES `dokter`      (`kd_dokter`)  ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `operasi_cathlab_ibfk_6` FOREIGN KEY (`scrub_nurse1`)     REFERENCES `petugas`     (`nip`)        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `operasi_cathlab_ibfk_7` FOREIGN KEY (`scrub_nurse2`)     REFERENCES `petugas`     (`nip`)        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `operasi_cathlab_ibfk_8` FOREIGN KEY (`circulate_nurse1`) REFERENCES `petugas`     (`nip`)        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `operasi_cathlab_ibfk_9` FOREIGN KEY (`circulate_nurse2`) REFERENCES `petugas`     (`nip`)        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `operasi_cathlab_ibfk_10` FOREIGN KEY (`radiographer1`)   REFERENCES `petugas`     (`nip`)        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `operasi_cathlab_ibfk_11` FOREIGN KEY (`kode_paket`)      REFERENCES `paket_operasi` (`kode_paket`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ============================================================
-- Tambah kolom biaya default petugas cathlab di paket_operasi
-- supaya admin bisa set tarif per paket lewat Master Paket Operasi
-- ============================================================
ALTER TABLE `paket_operasi`
  ADD COLUMN IF NOT EXISTS `scrub_nurse1`     double DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `scrub_nurse2`     double DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `circulate_nurse1` double DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `circulate_nurse2` double DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `radiographer1`    double DEFAULT 0;

-- ============================================================
-- SANITY CHECK
-- ============================================================
-- Setelah eksekusi:
--   SHOW CREATE TABLE operasi_cathlab;
--   DESC paket_operasi;   -- ada 5 kolom baru di akhir
--   SELECT COUNT(*) FROM operasi;  -- angka lama tetap, tabel operasi TIDAK diubah
--
-- Rollback bila perlu:
--   DROP TABLE operasi_cathlab;
--   ALTER TABLE paket_operasi
--     DROP COLUMN scrub_nurse1, DROP COLUMN scrub_nurse2,
--     DROP COLUMN circulate_nurse1, DROP COLUMN circulate_nurse2,
--     DROP COLUMN radiographer1;
