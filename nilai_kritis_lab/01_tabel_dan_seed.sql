-- =====================================================================
-- NILAI KRITIS LABORATORIUM - RSUD KOTA BAUBAU
-- Dipakai oleh DlgPeriksaLaboratorium untuk menandai hasil kritis
-- (sel merah + tanda H/L) di tampilan Khanza.
--
-- Cara kerja renderer:
--   * nilai (numerik) >= batas_atas   -> KRITIS TINGGI (tanda H)
--   * nilai (numerik) <= batas_bawah  -> KRITIS RENDAH (tanda L)
--   * kalau numerik null, dan teks_kritis diisi, dan hasil MENGANDUNG
--     teks_kritis (mis. 'Reaktif','Positif','+3') -> KRITIS (tanda !)
--
-- PENTING soal SATUAN:
--   batas numerik HARUS sesuai satuan yang dikirim eLIMS ke Khanza.
--   Contoh sudah dicocokkan dgn PROLIMS: Leukosit & Trombosit dalam
--   10^3/uL (WBC 2-40 ; Platelet 50-1000). Kalau alat/eLIMS kamu pakai
--   satuan /uL penuh, sesuaikan angkanya.
-- =====================================================================

CREATE TABLE IF NOT EXISTS set_nilai_kritis_lab (
  id_template  varchar(15) NOT NULL,
  batas_bawah  double DEFAULT NULL,
  batas_atas   double DEFAULT NULL,
  teks_kritis  varchar(60) DEFAULT NULL,
  keterangan   varchar(150) DEFAULT NULL,
  PRIMARY KEY (id_template)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ---------------------------------------------------------------------
-- SEED otomatis: cocokkan berdasarkan NAMA pemeriksaan yg sudah ada di
-- template_laboratorium. Yang lebih spesifik (bayi/neonatus) di-INSERT
-- lebih dulu; INSERT IGNORE mencegah baris yg sama tertimpa aturan umum.
-- ---------------------------------------------------------------------

-- ===== A. KIMIA DARAH =====
-- Bilirubin total (bayi) : > 15
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,NULL,15,'Bilirubin total bayi (>15)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%bilirubin%total%' AND (Pemeriksaan LIKE '%bayi%' OR Pemeriksaan LIKE '%neonat%');
-- Bilirubin total (dewasa) : > 12
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,NULL,12,'Bilirubin total dewasa (>12)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%bilirubin%total%';
-- Glukosa (bayi) : <=50 / >=325
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,50,325,'Glukosa bayi (<=50 / >=325)' FROM template_laboratorium
WHERE (Pemeriksaan LIKE '%glukosa%' OR Pemeriksaan LIKE '%gula darah%' OR Pemeriksaan LIKE '%gds%' OR Pemeriksaan LIKE '%gdp%')
  AND (Pemeriksaan LIKE '%bayi%' OR Pemeriksaan LIKE '%neonat%');
-- Glukosa (dewasa) : <=50 / >=500
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,50,500,'Glukosa dewasa (<=50 / >=500)' FROM template_laboratorium
WHERE (Pemeriksaan LIKE '%glukosa%' OR Pemeriksaan LIKE '%gula darah%' OR Pemeriksaan LIKE '%gds%' OR Pemeriksaan LIKE '%gdp%' OR Pemeriksaan LIKE '%gd2pp%' OR Pemeriksaan LIKE '%glukosa 2 jam%')
  AND Pemeriksaan NOT LIKE '- %';   -- kecualikan glukosa urine kualitatif
-- Ureum : <2 / >200
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,2,200,'Ureum (<2 / >200)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%ureum%' OR Pemeriksaan LIKE '%urea%' OR Pemeriksaan LIKE '%bun%';
-- Creatinin : >5 -- KECUALI Creatinin Clearance (nilai ~100 ml/mnt)
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,NULL,5,'Creatinin (>5)' FROM template_laboratorium
WHERE (Pemeriksaan LIKE '%creatin%' OR Pemeriksaan LIKE '%kreatin%')
  AND Pemeriksaan NOT LIKE '%clearance%' AND Pemeriksaan NOT LIKE '%klirens%';
-- Albumin : <=1.5
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,1.5,NULL,'Albumin (<=1.5)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%albumin%';

-- ===== B. HEMATOLOGI ===== (Hb g/dL ; HCT % ; WBC & PLT 10^3/uL)
-- Hemoglobin bayi baru lahir : <=9.5 / >=25
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,9.5,25,'Hemoglobin bayi baru lahir (<=9.5 / >=25)' FROM template_laboratorium
WHERE (Pemeriksaan LIKE '%hemoglobin%' OR Pemeriksaan LIKE '%haemoglobin%' OR Pemeriksaan LIKE 'hb%' OR Pemeriksaan LIKE '%(hb)%')
  AND (Pemeriksaan LIKE '%bayi%' OR Pemeriksaan LIKE '%baru lahir%' OR Pemeriksaan LIKE '%neonat%');
-- Hemoglobin dewasa : <=7 / >=20
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,7,20,'Hemoglobin dewasa (<=7 / >=20)' FROM template_laboratorium
WHERE (Pemeriksaan LIKE '%hemoglobin%' OR Pemeriksaan LIKE '%haemoglobin%' OR Pemeriksaan LIKE 'hb %' OR Pemeriksaan='HB' OR Pemeriksaan LIKE '%(hb)%');
-- Hematokrit neonatus : <=33 / >=70  ('%hemat%kit%' menangkap ejaan Hematokrit/Hematrokit)
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,33,70,'Hematokrit neonatus (<=33 / >=70)' FROM template_laboratorium
WHERE (Pemeriksaan LIKE '%hemat%kit%' OR Pemeriksaan LIKE '%hematocrit%' OR Pemeriksaan LIKE '%hct%')
  AND (Pemeriksaan LIKE '%neonat%' OR Pemeriksaan LIKE '%bayi%');
-- Hematokrit dewasa : <=20 / >=60
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,20,60,'Hematokrit dewasa (<=20 / >=60)' FROM template_laboratorium
WHERE (Pemeriksaan LIKE '%hemat%kit%' OR Pemeriksaan LIKE '%hematocrit%' OR Pemeriksaan LIKE '%hct%');
-- WBC / Leukosit : <=2 / >=40  (10^3/uL) -- KECUALI sedimen urine / PMN / diawali '-'
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,2,40,'Leukosit/WBC (<=2 / >=40 x10^3)' FROM template_laboratorium
WHERE (Pemeriksaan LIKE '%leukosit%' OR Pemeriksaan LIKE '%wbc%')
  AND Pemeriksaan NOT LIKE '%absolut%' AND Pemeriksaan NOT LIKE '%hitung jenis%'
  AND Pemeriksaan NOT LIKE '- %' AND Pemeriksaan NOT LIKE '%sedimen%'
  AND Pemeriksaan NOT LIKE '%pmn%' AND (satuan IS NULL OR satuan NOT LIKE '%lpb%');
-- Platelet / Trombosit : <=50 / >=1000  (10^3/uL) -- KECUALI item GDT kualitatif ('- TROMBOSIT')
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,50,1000,'Trombosit/Platelet (<=50 / >=1000 x10^3)' FROM template_laboratorium
WHERE (Pemeriksaan LIKE '%trombosit%' OR Pemeriksaan LIKE '%platelet%' OR Pemeriksaan LIKE '%plt%')
  AND Pemeriksaan NOT LIKE '- %';

-- ===== C. HEMOSTASIS =====
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,NULL,15,'BT (>=15 menit)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%bleeding time%' OR Pemeriksaan='BT' OR Pemeriksaan LIKE 'BT %' OR Pemeriksaan LIKE '%(bt)%';
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,NULL,44.7,'PT (>=44.7 detik)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%prothrombin%' OR Pemeriksaan='PT' OR Pemeriksaan LIKE 'PT %' OR Pemeriksaan LIKE '%(pt)%';
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,NULL,85,'APTT (>=85 detik)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%aptt%' OR Pemeriksaan LIKE '%appt%' OR Pemeriksaan LIKE '%thromboplastin%';
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,NULL,5,'INR (>=5)' FROM template_laboratorium
WHERE Pemeriksaan='INR' OR Pemeriksaan LIKE 'INR %' OR Pemeriksaan LIKE '%(inr)%';
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,50,700,'Fibrinogen (<50 / >700)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%fibrinogen%';

-- ===== D. URINE (teks) =====
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,teks_kritis,keterangan)
SELECT id_template,'+3','Protein urine (+3)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%protein%' AND (Pemeriksaan LIKE '%urin%' OR Pemeriksaan LIKE '%urine%');
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,teks_kritis,keterangan)
SELECT id_template,'positif','Keton urine (positif)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%keton%';

-- ===== E. IMUNOSEROLOGI (teks) =====
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,teks_kritis,keterangan)
SELECT id_template,'reaktif','Anti HIV (Reaktif)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%hiv%';

-- ===== F. MIKROBIOLOGI (teks) =====
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,teks_kritis,keterangan)
SELECT id_template,'positif','BTA (Positif)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%bta%';
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,teks_kritis,keterangan)
SELECT id_template,'rif.resistance','MDR (Rif.Resistance Detected)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%mdr%' OR Pemeriksaan LIKE '%tcm%' OR Pemeriksaan LIKE '%genexpert%' OR Pemeriksaan LIKE '%rif%';

-- ===== G. ANALISA GAS DARAH =====
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,7.25,7.55,'pH AGD (<=7.25 / >=7.55)' FROM template_laboratorium
WHERE (Pemeriksaan='PH' OR Pemeriksaan LIKE 'PH %' OR Pemeriksaan LIKE '%p h%') AND (Pemeriksaan LIKE '%gas%' OR Pemeriksaan LIKE '%agd%' OR Pemeriksaan LIKE '%bga%' OR Pemeriksaan LIKE '%analisa gas%');
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,20,60,'PCO2 (<=20 / >=60 mmHg)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%pco2%' OR Pemeriksaan LIKE '%pco 2%';
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,40,NULL,'PO2 (<=40 mmHg)' FROM template_laboratorium
WHERE (Pemeriksaan LIKE '%po2%' OR Pemeriksaan LIKE '%po 2%') AND Pemeriksaan NOT LIKE '%pco%';
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,10,40,'HCO3 (<=10 / >=40)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%hco3%' OR Pemeriksaan LIKE '%hco 3%' OR Pemeriksaan LIKE '%bikarbonat%';
INSERT IGNORE INTO set_nilai_kritis_lab (id_template,batas_bawah,batas_atas,keterangan)
SELECT id_template,75,NULL,'SpO2 (<=75%)' FROM template_laboratorium
WHERE Pemeriksaan LIKE '%spo2%' OR Pemeriksaan LIKE '%spo 2%'
   OR (Pemeriksaan LIKE '%saturasi%' AND Pemeriksaan NOT LIKE '%iron%' AND Pemeriksaan NOT LIKE '%transferin%' AND Pemeriksaan NOT LIKE '%besi%');

-- ---------------------------------------------------------------------
-- VERIFIKASI: jalankan ini utk cek hasil seed + cocokkan satuan.
-- Kalau ada satuan yg tidak sesuai (mis. leukosit dalam /uL penuh),
-- perbaiki dgn UPDATE set_nilai_kritis_lab SET batas_... WHERE id_template=...
-- ---------------------------------------------------------------------
-- SELECT k.id_template, t.Pemeriksaan, t.satuan, k.batas_bawah, k.batas_atas, k.teks_kritis, k.keterangan
-- FROM set_nilai_kritis_lab k JOIN template_laboratorium t ON t.id_template=k.id_template
-- ORDER BY t.Pemeriksaan;
