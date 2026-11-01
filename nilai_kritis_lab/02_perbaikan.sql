-- =====================================================================
-- PERBAIKAN set_nilai_kritis_lab
-- Jalankan di server yang SUDAH terlanjur menjalankan 01_tabel_dan_seed.sql
-- versi lama (yang polanya terlalu longgar). Menghapus mapping SALAH +
-- menambah Hematokrit yang ejaannya beda ("Hematrokit").
--
-- Aman diulang (idempotent): DELETE hanya kena baris salah, INSERT IGNORE.
-- =====================================================================

-- 1) HAPUS mapping yang salah konteks / salah satuan:
--    - Creatinin Clearance  : nilai ~100 ml/mnt, kalau atas=5 semua kena HH palsu
--    - Saturasi Iron        : kena aturan SpO2 (bawah=75) -> LL palsu
--    - Leukosit/Glukosa/Trombosit sedimen urine & GDT kualitatif (diawali '-', /LPB, PMN, sedimen)
DELETE k FROM set_nilai_kritis_lab k
JOIN template_laboratorium t ON t.id_template = k.id_template
WHERE t.Pemeriksaan LIKE '%clearance%'
   OR t.Pemeriksaan LIKE '%klirens%'
   OR t.Pemeriksaan LIKE '%saturasi iron%'
   OR t.Pemeriksaan LIKE '%saturasi besi%'
   OR t.Pemeriksaan LIKE '%saturasi transferin%'
   OR t.Pemeriksaan LIKE '- Leukosit%'
   OR t.Pemeriksaan LIKE '- Glukosa%'
   OR t.Pemeriksaan LIKE '- TROMBOSIT%'
   OR t.Pemeriksaan LIKE '%sedimen leukosit%'
   OR t.Pemeriksaan LIKE 'Leukosit : PMN%'
   OR t.satuan LIKE '%lpb%';

-- 2) TAMBAH Hematokrit dewasa utk template yg ejaannya beda (mis. "Hematrokit"),
--    pola '%hemat%kit%' menangkap Hematokrit / Hematrokit / Hematocrit.
--    (Kalau ada template neonatus/bayi, sesuaikan manual ke 33/70.)
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,20,60,'Hematokrit dewasa (<=20 / >=60)' FROM template_laboratorium
WHERE (Pemeriksaan LIKE '%hemat%kit%' OR Pemeriksaan LIKE '%hematocrit%' OR Pemeriksaan LIKE '%hct%')
  AND id_template NOT IN (SELECT id_template FROM set_nilai_kritis_lab);

-- 3) VERIFIKASI (cek hasil akhir + satuan). Perhatikan kolom satuan:
--    kalau leukosit/trombosit dikirim dalam /uL penuh (bukan 10^3), sesuaikan batasnya.
-- SELECT k.id_template, t.Pemeriksaan, t.satuan, k.batas_bawah, k.batas_atas, k.teks_kritis
-- FROM set_nilai_kritis_lab k JOIN template_laboratorium t ON t.id_template=k.id_template
-- ORDER BY t.Pemeriksaan;
