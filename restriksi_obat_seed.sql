-- =====================================================
-- SEED DATA RESTRIKSI OBAT FORNAS — SIMRS Khanza
-- Aman: hanya insert untuk obat yang ADA di databarang RS Anda
-- Gunakan: import via phpMyAdmin atau jalankan langsung di MySQL
--
-- Kolom max_jml = batas jumlah obat PER RESEP (hardblock kalau dilanggar)
-- Disesuaikan dengan praktik klinis & Fornas
-- =====================================================

-- ------- ADENOSIN -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 3, 'Y', 'Fornas — Adenosin',
       'Hanya untuk Supraventrikular Takikardia (SVT) yang dibuktikan dengan EKG, atau untuk uji pembebanan jantung (cardiac stress test). Max 3 amp/resep.',
       3
FROM databarang WHERE nama_brng LIKE '%adenosin%' OR nama_brng LIKE '%adenosine%'
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- AMIODARON -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 5, 'Y', 'Fornas — Amiodaron Injeksi',
       'Hanya untuk aritmia ventrikel mengancam jiwa (VT/VF) atau AF respon ventrikel cepat refrakter. Max 5 amp/resep (injeksi) atau 60 tab/resep (oral).',
       2
FROM databarang WHERE nama_brng LIKE '%amiodaron%' OR nama_brng LIKE '%amiodarone%'
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- ALBUMIN -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 5, 'Y', 'Fornas — Albumin 20%',
       'Hanya untuk hipoalbuminemia berat (<2,5 g/dL): sirosis hati dengan asites refrakter, sindrom nefrotik, luka bakar luas, pasca operasi besar. Wajib lampirkan hasil lab albumin. Max 5 vial/resep.',
       2
FROM databarang WHERE nama_brng LIKE '%albumin%' AND nama_brng NOT LIKE '%albumin powder%'
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- ALTEPLASE -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 1, 'Y', 'Fornas — Alteplase',
       'Hanya untuk stroke iskemik akut dalam golden period (<4,5 jam onset) atau emboli paru masif. Wajib CT scan & evaluasi kontraindikasi perdarahan. Max 1 vial/resep (single dose).',
       3
FROM databarang WHERE nama_brng LIKE '%alteplase%' OR nama_brng LIKE '%actilyse%'
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- CEFTRIAXONE -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 14, 'Y', 'Fornas — Ceftriaxone',
       'Hanya untuk infeksi bakteri sensitif: meningitis, pneumonia berat, sepsis, ISK komplikata, gonore. Bukan untuk ISPA ringan. Max 14 vial/resep (course 7 hari x 2 vial/hari).',
       2
FROM databarang WHERE nama_brng LIKE '%ceftriaxone%' OR nama_brng LIKE '%ceftriakson%'
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- MEROPENEM -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 21, 'Y', 'Fornas — Meropenem',
       'Hanya untuk infeksi berat oleh bakteri MDR/ESBL+, sesuai hasil kultur & sensitivitas. Wajib persetujuan tim PPRA/komite antibiotik. Max 21 vial/resep (7 hari x 3 vial).',
       3
FROM databarang WHERE nama_brng LIKE '%meropenem%' OR nama_brng LIKE '%meronem%'
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- CLOPIDOGREL -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 30, 'Y', 'Fornas — Clopidogrel',
       'Untuk ACS/STEMI/NSTEMI, pasca PCI dengan stent, atau stroke iskemik. Lama DAPT max 12 bulan. Max 30 tab/resep (1 bulan).',
       2
FROM databarang WHERE nama_brng LIKE '%clopidogrel%' OR nama_brng LIKE '%plavix%'
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- ATORVASTATIN -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 30, 'Y', 'Fornas — Atorvastatin',
       'Untuk dislipidemia LDL >190 mg/dL, atau pasien risiko tinggi kardiovaskuler (DM, post-MI, stroke). Wajib hasil lab profil lipid. Max 30 tab/resep (1 bulan).',
       1
FROM databarang WHERE nama_brng LIKE '%atorvastatin%'
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- INSULIN GLARGINE -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 5, 'Y', 'Fornas — Insulin Glargine',
       'Hanya untuk DM tipe 1, DM tipe 2 gagal terapi insulin NPH, atau DM dengan hipoglikemia nokturnal berulang. Wajib HbA1c & GDS. Max 5 pen/resep (1 bulan).',
       2
FROM databarang WHERE nama_brng LIKE '%glargine%' OR nama_brng LIKE '%lantus%'
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- OMEPRAZOLE INJEKSI -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 14, 'Y', 'Fornas — Omeprazole Injeksi',
       'Hanya untuk perdarahan saluran cerna atas akut, GERD berat yang tidak bisa per-oral, atau profilaksis stress ulcer pada pasien ICU. Bukan untuk dispepsia rutin. Max 14 vial/resep.',
       2
FROM databarang WHERE (nama_brng LIKE '%omeprazol%' OR nama_brng LIKE '%omeprazole%') AND (nama_brng LIKE '%inj%' OR nama_brng LIKE '%vial%')
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- PARACETAMOL INFUS -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 20, 'Y', 'Fornas — Paracetamol Infus',
       'Hanya untuk pasien yang tidak bisa per-oral/rektal (puasa, post-op, gangguan kesadaran). Bukan untuk demam ringan yang bisa dengan tablet. Max 20 fls/resep.',
       2
FROM databarang WHERE nama_brng LIKE '%paracetamol%infus%' OR nama_brng LIKE '%paracetamol%inf%' OR nama_brng LIKE '%paracetamol%fls%' OR nama_brng LIKE '%pamol%inf%'
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- ASAM TRANEKSAMAT -------
INSERT INTO restriksi_obat (kode_brng, kdjenis, kd_pj, max_jml, aktif, keterangan, restriksi_text, level_faskes)
SELECT kode_brng, 'ALL', 'ALL', 30, 'Y', 'Fornas — Asam Traneksamat',
       'Untuk perdarahan akut: post-partum, post-trauma, post-operasi besar, atau menorrhagia berat. Bukan untuk memar/luka ringan. Max 30 amp atau 30 tab/resep.',
       1
FROM databarang WHERE nama_brng LIKE '%tranexamic%' OR nama_brng LIKE '%traneksamat%' OR nama_brng LIKE '%kalnex%'
ON DUPLICATE KEY UPDATE max_jml=VALUES(max_jml), restriksi_text=VALUES(restriksi_text), level_faskes=VALUES(level_faskes), aktif='Y';

-- ------- CEK HASIL -------
SELECT ro.kode_brng, db.nama_brng, ro.max_jml, ro.restriksi_text, ro.level_faskes
FROM restriksi_obat ro
INNER JOIN databarang db ON db.kode_brng = ro.kode_brng
WHERE ro.restriksi_text IS NOT NULL AND ro.restriksi_text <> ''
ORDER BY db.nama_brng;
