-- ============================================================
-- Bridging Khanza -> alat_rs (RIS + DICOM + Orthanc + SATUSEHAT)
-- ============================================================
-- Jalankan satu kali di database SIMRS Khanza sebelum pakai
-- tombol "Kirim Permintaan ke RIS (alat_rs)" di Data Permintaan Radiologi.
--
-- CATATAN: TIDAK ada tabel mapping pemeriksaan. Pasien, dokter, dan
-- pemeriksaan di-resolve/di-register otomatis ke RIS. Khanza hanya
-- perlu tabel log di bawah untuk tracking accession_number & status.
-- ============================================================

-- Log/tracking pengiriman permintaan radiologi ke alat_rs.
-- 1 baris per noorder. Tanpa foreign key supaya aman di semua
-- varian collation database Khanza (menghindari errno 150).
CREATE TABLE IF NOT EXISTS bridging_alatrs_log (
  noorder              VARCHAR(15)  NOT NULL,
  accession_number     VARCHAR(50)  DEFAULT '',
  worklist_id_alatrs   VARCHAR(50)  DEFAULT '',
  patient_id_alatrs    VARCHAR(50)  DEFAULT '',
  satu_sehat_status    VARCHAR(20)  DEFAULT 'PENDING',
  satu_sehat_stage     VARCHAR(50)  DEFAULT '',
  response_terakhir    TEXT,
  tgl_kirim            DATETIME     DEFAULT CURRENT_TIMESTAMP,
  tgl_update           DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (noorder)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Kalau sebelumnya sudah terlanjur membuat tabel mapping manual, boleh dihapus:
-- DROP TABLE IF EXISTS bridging_alatrs_pemeriksaan;

-- ------------------------------------------------------------
-- 1 order bisa berisi >1 pemeriksaan (mis. Foto Toraks + CT Sinus),
-- masing-masing punya worklist & accession sendiri di RIS. Supaya
-- semuanya tersimpan (dipisah koma), lebarkan 2 kolom ini.
-- Aman dijalankan berkali-kali; data lama tetap utuh.
-- ------------------------------------------------------------
ALTER TABLE bridging_alatrs_log MODIFY accession_number   VARCHAR(255) DEFAULT '';
ALTER TABLE bridging_alatrs_log MODIFY worklist_id_alatrs  VARCHAR(255) DEFAULT '';
