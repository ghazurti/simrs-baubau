-- Pelengkapan Surgical Safety Checklist sesuai formulir RM 22.OK-03 RSUD Kota Baubau
-- Kolom tambahan untuk Sign-In / Time-Out / Sign-Out

ALTER TABLE signin_sebelum_anestesi
  ADD COLUMN persetujuan_penandaan enum('Ya','Tidak') NOT NULL DEFAULT 'Ya',
  ADD COLUMN persetujuan_bedah enum('Ya','Tidak') NOT NULL DEFAULT 'Ya',
  ADD COLUMN persetujuan_anestesi enum('Ya','Tidak') NOT NULL DEFAULT 'Ya',
  ADD COLUMN hasil_radiologi enum('Ya','Tidak Diperlukan') NOT NULL DEFAULT 'Ya';

ALTER TABLE timeout_sebelum_insisi
  ADD COLUMN konfirmasi_tim enum('Ya','Tidak') NOT NULL DEFAULT 'Ya',
  ADD COLUMN posisi_pasien varchar(100) NOT NULL DEFAULT '',
  ADD COLUMN alat_khusus varchar(100) NOT NULL DEFAULT '',
  ADD COLUMN perhatian_anestesi varchar(100) NOT NULL DEFAULT '',
  ADD COLUMN berdoa enum('Ya','Tidak') NOT NULL DEFAULT 'Ya',
  ADD COLUMN jam_dimulai varchar(10) NOT NULL DEFAULT '';

ALTER TABLE signout_sebelum_menutup_luka
  ADD COLUMN masalah_peralatan varchar(100) NOT NULL DEFAULT '',
  ADD COLUMN komplikasi enum('Tidak','Ada') NOT NULL DEFAULT 'Tidak',
  ADD COLUMN komplikasi_ket varchar(100) NOT NULL DEFAULT '',
  ADD COLUMN perhatian_pemulihan_anestesi varchar(100) NOT NULL DEFAULT '',
  ADD COLUMN disposisi enum('Ruang Recovery','ICU') NOT NULL DEFAULT 'Ruang Recovery',
  ADD COLUMN nip_perawat_instrumen varchar(20) NOT NULL DEFAULT '';
