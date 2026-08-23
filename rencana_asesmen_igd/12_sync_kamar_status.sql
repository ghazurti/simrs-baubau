-- =============================================================================
-- Sync kamar.status dengan kondisi kamar_inap yang sebenarnya
-- =============================================================================
-- Tujuan  : bereskan drift antara kamar.status dan kamar_inap.stts_pulang
-- Sumber
-- kebenaran: kamar_inap dengan stts_pulang='-' (pasien aktif)
--
-- Kapan dijalankan:
--   1. Sekali saat pertama kali guard admisi baru dipasang (WAJIB)
--   2. Berkala (mingguan/bulanan) sebagai maintenance, hanya jika perlu
--
-- CATATAN PENTING:
--   - Backup DB dulu sebelum jalankan.
--   - Jalankan step 1 dulu untuk melihat daftar drift-nya.
--   - Step 2 hanya jalankan setelah lihat hasil step 1 dan yakin.
-- =============================================================================


-- -----------------------------------------------------------------------------
-- STEP 1 : LIHAT dulu kamar mana saja yang drift (jangan diubah apa-apa)
-- -----------------------------------------------------------------------------
SELECT
    k.kd_kamar,
    k.status                                                    AS status_kamar_sekarang,
    (SELECT COUNT(*) FROM kamar_inap ki
       WHERE ki.kd_kamar = k.kd_kamar AND ki.stts_pulang='-')   AS pasien_aktif,
    CASE
        WHEN (SELECT COUNT(*) FROM kamar_inap ki
                WHERE ki.kd_kamar = k.kd_kamar AND ki.stts_pulang='-') > 0
             AND k.status = 'KOSONG'
             THEN 'DRIFT: seharusnya ISI'
        WHEN (SELECT COUNT(*) FROM kamar_inap ki
                WHERE ki.kd_kamar = k.kd_kamar AND ki.stts_pulang='-') = 0
             AND k.status = 'ISI'
             THEN 'DRIFT: seharusnya KOSONG'
        ELSE 'OK'
    END AS diagnosa
FROM kamar k
WHERE k.statusdata='1'
  AND (
        ( (SELECT COUNT(*) FROM kamar_inap ki
             WHERE ki.kd_kamar = k.kd_kamar AND ki.stts_pulang='-') > 0
          AND k.status='KOSONG' )
     OR ( (SELECT COUNT(*) FROM kamar_inap ki
             WHERE ki.kd_kamar = k.kd_kamar AND ki.stts_pulang='-') = 0
          AND k.status='ISI' )
      )
ORDER BY k.kd_kamar;


-- -----------------------------------------------------------------------------
-- STEP 1b : Cek kamar yang punya lebih dari 1 pasien aktif (double-booking).
--           Kalau ada hasil, harus dibereskan MANUAL dulu (pindahkan salah
--           satu pasien ke kamar lain) sebelum jalankan step 2.
-- -----------------------------------------------------------------------------
SELECT
    ki.kd_kamar,
    COUNT(*)                             AS jml_pasien_aktif,
    GROUP_CONCAT(ki.no_rawat SEPARATOR ', ') AS daftar_norawat
FROM kamar_inap ki
WHERE ki.stts_pulang = '-'
GROUP BY ki.kd_kamar
HAVING COUNT(*) > 1
ORDER BY ki.kd_kamar;


-- -----------------------------------------------------------------------------
-- STEP 2 : Perbaiki drift-nya.
--          HANYA jalankan setelah STEP 1b tidak ada hasil (tidak ada
--          double-booking yang tersisa).
--
-- Perhatikan: status 'DIBERSIHKAN' dan 'DIBOOKING' tidak diubah — hanya
-- ISI <-> KOSONG yang dibereskan.
-- -----------------------------------------------------------------------------
UPDATE kamar k
SET k.status = CASE
    WHEN (SELECT COUNT(*) FROM kamar_inap ki
            WHERE ki.kd_kamar = k.kd_kamar AND ki.stts_pulang='-') > 0
        THEN 'ISI'
    ELSE 'KOSONG'
END
WHERE k.statusdata='1'
  AND k.status IN ('ISI','KOSONG');


-- -----------------------------------------------------------------------------
-- STEP 3 : Verifikasi setelah step 2 — harusnya kosong (tidak ada baris).
-- -----------------------------------------------------------------------------
SELECT
    k.kd_kamar, k.status,
    (SELECT COUNT(*) FROM kamar_inap ki
       WHERE ki.kd_kamar = k.kd_kamar AND ki.stts_pulang='-') AS pasien_aktif
FROM kamar k
WHERE k.statusdata='1'
  AND (
        ( (SELECT COUNT(*) FROM kamar_inap ki
             WHERE ki.kd_kamar = k.kd_kamar AND ki.stts_pulang='-') > 0
          AND k.status='KOSONG' )
     OR ( (SELECT COUNT(*) FROM kamar_inap ki
             WHERE ki.kd_kamar = k.kd_kamar AND ki.stts_pulang='-') = 0
          AND k.status='ISI' )
      );
