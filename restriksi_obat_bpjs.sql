-- =====================================================
-- RESTRIKSI OBAT BPJS — SIMRS Khanza
-- Schema + Trigger gate untuk iterasi & resep obat
-- Aman: tidak mengubah tabel existing, hanya menambah.
-- =====================================================

-- =========================================
-- 0) PERMISSION MENU KHANZA
-- =========================================
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND column_name = 'restriksi_obat_bpjs'
);

SET @sql := IF(@col_exists = 0,
  "ALTER TABLE `user` ADD `restriksi_obat_bpjs` enum('true','false') DEFAULT 'false'",
  "SELECT 'kolom restriksi_obat_bpjs sudah ada'"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================================
-- 1) MASTER RESTRIKSI OBAT
-- =========================================
DROP TABLE IF EXISTS `restriksi_obat`;
CREATE TABLE `restriksi_obat` (
  `kode_brng`      VARCHAR(15)  NOT NULL,
  `kdjenis`        VARCHAR(40)  NOT NULL DEFAULT 'ALL',
                                   -- 'ALL' | '1. Obat PRB' | '2. Obat Kronis Belum Stabil' | '3. Obat Kemoterapi'
  `kd_pj`          CHAR(3)      NOT NULL DEFAULT 'ALL',
                                   -- 'ALL' atau kd_pj dari tabel penjab (BPJS, dll)
  `max_jml`        DECIMAL(10,2) DEFAULT NULL, -- max jumlah per resep
  `max_iterasi`    TINYINT       DEFAULT 0,    -- 0=tidak boleh iterasi, 1=1x, 2=2x
  `butuh_approval` ENUM('Y','N') NOT NULL DEFAULT 'N',
  `aktif`          ENUM('Y','N') NOT NULL DEFAULT 'Y',
  `keterangan`     TEXT,
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`kode_brng`,`kdjenis`,`kd_pj`),
  KEY `idx_restriksi_brng`  (`kode_brng`),
  KEY `idx_restriksi_aktif` (`aktif`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- =========================================
-- 2) LOG PELANGGARAN / KEPUTUSAN RESTRIKSI
-- =========================================
DROP TABLE IF EXISTS `log_restriksi_obat`;
CREATE TABLE `log_restriksi_obat` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `no_resep`      VARCHAR(14)  DEFAULT NULL,
  `no_resep_awal` VARCHAR(14)  DEFAULT NULL, -- untuk iterasi
  `kode_brng`     VARCHAR(15)  DEFAULT NULL,
  `jml_diminta`   DECIMAL(10,2) DEFAULT NULL,
  `jml_diizinkan` DECIMAL(10,2) DEFAULT NULL,
  `kdjenis`       VARCHAR(40)  DEFAULT NULL,
  `kd_pj`         CHAR(3)      DEFAULT NULL,
  `status_iter`   VARCHAR(20)  DEFAULT NULL,
  `hasil`         ENUM('LOLOS','DITOLAK','OVERRIDE') NOT NULL DEFAULT 'LOLOS',
  `alasan`        VARCHAR(255) DEFAULT NULL,
  `user`          VARCHAR(50)  DEFAULT NULL,
  `waktu`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_log_resep`  (`no_resep`),
  KEY `idx_log_brng`   (`kode_brng`),
  KEY `idx_log_waktu`  (`waktu`),
  KEY `idx_log_hasil`  (`hasil`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- =====================================================
-- TRIGGER GATE
--
-- Strategi: tidak block INSERT detail obat (resep_dokter)
-- karena dokter bisa bingung. Block di titik IT ERASI BPJS:
--   sebelum INSERT ke permintaan_resep_iterasi_bpjs.
--
-- Cara cek: untuk setiap obat di resep_obat yang akan
-- diiterasi, cek apakah ada baris restriksi_obat aktif
-- yang melarang. Aturan dipilih dari spesifik -> umum:
--   (kdjenis, kd_pj) -> (kdjenis, ALL) -> (ALL, kd_pj) -> (ALL, ALL)
--
-- Tabel obat per resep (asumsi Khanza):
--   resep_dokter (no_resep, kode_brng, jml)
--   resep_dokter_racikan (no_resep, kode_brng, jml)
-- Jika nama tabel berbeda di instalasi, sesuaikan di blok
-- LOOP_OBAT cursor.
-- =====================================================

DROP TRIGGER IF EXISTS `trg_restriksi_iterasi_bpjs`;

DELIMITER $$

CREATE TRIGGER `trg_restriksi_iterasi_bpjs`
BEFORE INSERT ON `permintaan_resep_iterasi_bpjs`
FOR EACH ROW
BEGIN
    DECLARE v_kdjenis      VARCHAR(40);
    DECLARE v_kd_pj        CHAR(3);
    DECLARE v_no_rawat     VARCHAR(17);
    DECLARE v_iter_idx     TINYINT;

    DECLARE v_kode_brng    VARCHAR(15);
    DECLARE v_jml          DECIMAL(10,2);

    DECLARE v_max_jml      DECIMAL(10,2);
    DECLARE v_max_iter     TINYINT;
    DECLARE v_aktif        CHAR(1);
    DECLARE v_done         INT DEFAULT 0;

    DECLARE cur_obat CURSOR FOR
        SELECT kode_brng, jml FROM resep_dokter                WHERE no_resep = NEW.no_resep_awal
        UNION ALL
        SELECT kode_brng, jml FROM resep_dokter_racikan_detail WHERE no_resep = NEW.no_resep_awal;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    -- Ambil konteks resep awal (jenis & penjab)
    SELECT b.kdjenis, p.kd_pj
      INTO v_kdjenis, v_kd_pj
      FROM bridging_resep_apotek_bpjs b
      JOIN resep_obat ro  ON ro.no_resep = b.no_resep
      JOIN reg_periksa p  ON p.no_rawat  = ro.no_rawat
     WHERE b.no_resep = NEW.no_resep_awal
     LIMIT 1;

    IF v_kdjenis IS NULL THEN SET v_kdjenis = 'ALL'; END IF;
    IF v_kd_pj  IS NULL THEN SET v_kd_pj   = 'ALL'; END IF;

    -- Index iterasi yang sedang dibuat
    SET v_iter_idx = CASE NEW.status_iter
                       WHEN 'Iterasi Ke 1' THEN 1
                       WHEN 'Iterasi Ke 2' THEN 2
                       ELSE 0
                     END;

    OPEN cur_obat;
    loop_obat: LOOP
        FETCH cur_obat INTO v_kode_brng, v_jml;
        IF v_done = 1 THEN LEAVE loop_obat; END IF;

        -- Cari restriksi paling spesifik yang aktif
        SET v_max_jml  = NULL;
        SET v_max_iter = NULL;
        SET v_aktif    = 'N';

        SELECT max_jml, max_iterasi, aktif
          INTO v_max_jml, v_max_iter, v_aktif
          FROM restriksi_obat
         WHERE kode_brng = v_kode_brng
           AND aktif = 'Y'
           AND (
                 (kdjenis = v_kdjenis AND kd_pj = v_kd_pj) OR
                 (kdjenis = v_kdjenis AND kd_pj = 'ALL')   OR
                 (kdjenis = 'ALL'     AND kd_pj = v_kd_pj) OR
                 (kdjenis = 'ALL'     AND kd_pj = 'ALL')
               )
         ORDER BY
            CASE WHEN kdjenis = v_kdjenis AND kd_pj = v_kd_pj THEN 1
                 WHEN kdjenis = v_kdjenis                     THEN 2
                 WHEN kd_pj   = v_kd_pj                       THEN 3
                 ELSE 4 END
         LIMIT 1;

        -- Tidak ada restriksi: lanjut
        IF v_aktif = 'Y' THEN

            -- 1) Batas iterasi
            IF v_max_iter IS NOT NULL AND v_iter_idx > v_max_iter THEN
                INSERT INTO log_restriksi_obat
                  (no_resep,no_resep_awal,kode_brng,jml_diminta,jml_diizinkan,
                   kdjenis,kd_pj,status_iter,hasil,alasan,user)
                VALUES
                  (NEW.no_resep,NEW.no_resep_awal,v_kode_brng,v_jml,v_max_jml,
                   v_kdjenis,v_kd_pj,NEW.status_iter,'DITOLAK',
                   CONCAT('Iterasi melebihi batas (max ',v_max_iter,')'),CURRENT_USER());

                SIGNAL SQLSTATE '45000'
                  SET MESSAGE_TEXT = 'RESTRIKSI: obat melebihi batas iterasi yang diizinkan.';
            END IF;

            -- 2) Batas jumlah per resep
            IF v_max_jml IS NOT NULL AND v_jml > v_max_jml THEN
                INSERT INTO log_restriksi_obat
                  (no_resep,no_resep_awal,kode_brng,jml_diminta,jml_diizinkan,
                   kdjenis,kd_pj,status_iter,hasil,alasan,user)
                VALUES
                  (NEW.no_resep,NEW.no_resep_awal,v_kode_brng,v_jml,v_max_jml,
                   v_kdjenis,v_kd_pj,NEW.status_iter,'DITOLAK',
                   CONCAT('Jumlah obat melebihi batas (max ',v_max_jml,')'),CURRENT_USER());

                SIGNAL SQLSTATE '45000'
                  SET MESSAGE_TEXT = 'RESTRIKSI: jumlah obat melebihi batas yang diizinkan.';
            END IF;

            -- 3) Lolos: tetap dicatat sebagai LOLOS untuk audit
            INSERT INTO log_restriksi_obat
              (no_resep,no_resep_awal,kode_brng,jml_diminta,jml_diizinkan,
               kdjenis,kd_pj,status_iter,hasil,alasan,user)
            VALUES
              (NEW.no_resep,NEW.no_resep_awal,v_kode_brng,v_jml,v_max_jml,
               v_kdjenis,v_kd_pj,NEW.status_iter,'LOLOS',
               'Sesuai restriksi',CURRENT_USER());
        END IF;
    END LOOP;
    CLOSE cur_obat;
END $$

DELIMITER ;


-- =====================================================
-- CONTOH ISI MASTER RESTRIKSI
-- (hapus / ubah sesuai kebijakan formularium RS)
-- =====================================================
-- Obat PRB: max 1x iterasi, max 30 tablet
-- INSERT INTO restriksi_obat (kode_brng,kdjenis,kd_pj,max_jml,max_iterasi,aktif,keterangan)
-- VALUES ('OBT0001','1. Obat PRB','BPJ',30,1,'Y','Batas PRB sesuai formularium');

-- Obat kemoterapi: tidak boleh iterasi sama sekali
-- INSERT INTO restriksi_obat (kode_brng,kdjenis,kd_pj,max_iterasi,aktif,keterangan)
-- VALUES ('OBT0099','3. Obat Kemoterapi','BPJ',0,'Y','Kemoterapi butuh resep baru tiap siklus');

-- Obat umum semua jenis & penjamin: max 60 tablet, max 2x iterasi
-- INSERT INTO restriksi_obat (kode_brng,max_jml,max_iterasi,aktif)
-- VALUES ('OBT0123',60,2,'Y');
