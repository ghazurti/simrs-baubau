-- =====================================================================
-- Perbaikan LOINC radiologi yang membuat modality salah / kode SATUSEHAT salah
-- Sumber kode: master LOINC Radiologi SATUSEHAT (satusehat.kemkes.go.id,
-- terminology/loinc/radiologi). RSUD Kota Baubau, 2026-10-22.
--
-- Setelah dijalankan, master ms_pemeriksaan di RIS alat_rs untuk id (kd_jenis_prw)
-- yang sama JUGA harus di-update (LOINC + kode_modality), karena RIS tidak
-- memperbarui pemeriksaan yang sudah terdaftar.
-- =====================================================================

-- backup baris yang diubah (sekali saja)
CREATE TABLE IF NOT EXISTS satu_sehat_mapping_radiologi_bak_20261022 AS
SELECT * FROM satu_sehat_mapping_radiologi
WHERE kd_jenis_prw IN ('J000016','J000149','J000039','J000040','J000049','J000050','J000053','J000054',
                       'J000057','J000058','J000265','J000269','J000026','J000069','J000074');

-- kalau dijalankan ulang setelah daftar bertambah, simpan juga baris yang belum ada di backup
INSERT INTO satu_sehat_mapping_radiologi_bak_20261022
SELECT m.* FROM satu_sehat_mapping_radiologi m
WHERE m.kd_jenis_prw IN ('J000016','J000149','J000039','J000040','J000049','J000050','J000053','J000054',
                       'J000057','J000058','J000265','J000269','J000026','J000069','J000074')
  AND m.kd_jenis_prw NOT IN (SELECT kd_jenis_prw FROM satu_sehat_mapping_radiologi_bak_20261022);

-- Rontgen yang tercatat CT (terkirim ke alat sebagai CT)
UPDATE satu_sehat_mapping_radiologi SET code='24695-9', display='XR Facial bones Views'
 WHERE kd_jenis_prw IN ('J000016','J000149');                  -- Foto Maxilarias, Maxilla (REVIEW: kalau maksudnya sinus maxillaris pakai 24916-9 XR Sinuses Views)

-- CT yang memakai kode rontgen (XR)
UPDATE satu_sehat_mapping_radiologi SET code='46298-6', display='CT Mastoid - bilateral'          WHERE kd_jenis_prw='J000039'; -- CT Mastoid tanpa kontras
UPDATE satu_sehat_mapping_radiologi SET code='87900-7', display='CT Mastoid W contrast IV'        WHERE kd_jenis_prw='J000040'; -- CT Mastoid dengan kontras
UPDATE satu_sehat_mapping_radiologi SET code='46331-5', display='CT Orbit WO contrast'            WHERE kd_jenis_prw='J000049'; -- CT Orbita tanpa kontras
UPDATE satu_sehat_mapping_radiologi SET code='48449-3', display='CT Orbit W contrast IV'          WHERE kd_jenis_prw='J000050'; -- CT Orbita dengan kontras
UPDATE satu_sehat_mapping_radiologi SET code='36049-5', display='CT Maxilla and Mandible'
 WHERE kd_jenis_prw IN ('J000053','J000054');                  -- CT Mandibula (master tidak punya CT mandibula saja)
UPDATE satu_sehat_mapping_radiologi SET code='82697-4', display='CT Spine Lumbar and Sacrum WO contrast'   WHERE kd_jenis_prw IN ('J000057','J000265'); -- CT Vertebra tanpa kontras (REVIEW: cervical/thoracal punya kode sendiri)
UPDATE satu_sehat_mapping_radiologi SET code='82696-6', display='CT Spine Lumbar and Sacrum W contrast IV' WHERE kd_jenis_prw IN ('J000058','J000269'); -- CT Vertebra dengan kontras

-- USG yang memakai kode mammografi / rontgen
UPDATE satu_sehat_mapping_radiologi SET code='24601-7', display='US Breast', sampel_code='76752008', sampel_display='Breast structure'
 WHERE kd_jenis_prw IN ('J000026','J000069');                  -- USG Mamma/Mamae Bilateral (sebelumnya MG Breast + Thorax structure)
UPDATE satu_sehat_mapping_radiologi SET code='24630-6', display='US Chest'                        WHERE kd_jenis_prw='J000074'; -- USG Thorax

-- Cek hasil
SELECT m.kd_jenis_prw, j.nm_perawatan, b.code AS code_lama, b.display AS display_lama, m.code, m.display
FROM satu_sehat_mapping_radiologi m
INNER JOIN jns_perawatan_radiologi j ON j.kd_jenis_prw=m.kd_jenis_prw
INNER JOIN satu_sehat_mapping_radiologi_bak_20261022 b ON b.kd_jenis_prw=m.kd_jenis_prw
ORDER BY m.kd_jenis_prw;
