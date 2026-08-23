-- ============================================================
-- Migrasi tabel penilaian_medis_igd agar sesuai form kertas
-- RSUD Kota Baubau (RM EMG-02.01 s.d. 02.04)
--
-- Sifat: aditif — hanya ADD COLUMN / CREATE TABLE, tidak
-- mengubah/menghapus kolom lama. Data lama tetap valid.
--
-- Ukuran total: 36 kolom lama tetap + 91 kolom baru + 1 tabel
-- pendukung. Setelah migrasi tabel `penilaian_medis_igd`
-- memiliki 127 kolom.
--
-- CATATAN: jalankan di database staging/backup dulu.
--          jangan lupa `mysqldump penilaian_medis_igd` sebelum
--          eksekusi di produksi.
-- ============================================================

-- -----------------------------------------------------------
-- BLOK 1. HEADER PASIEN (RM EMG-02.01 bagian atas)
-- -----------------------------------------------------------
ALTER TABLE `penilaian_medis_igd`
  ADD COLUMN `dibawa_oleh` enum('Datang Sendiri','Keluarga','Polisi','Lainnya') DEFAULT NULL,
  ADD COLUMN `dibawa_oleh_ket` varchar(100) DEFAULT NULL,
  ADD COLUMN `rujukan` enum('Tidak','Ya') DEFAULT 'Tidak',
  ADD COLUMN `rujukan_dari` enum('Puskesmas','Bidan/Perawat','Dokter','Rumah Sakit','Lainnya') DEFAULT NULL,
  ADD COLUMN `rujukan_ket` varchar(200) DEFAULT NULL,
  ADD COLUMN `kondisi_tiba` enum('Resusitasi','Emergent','Urgent','Less Urgent','Non Urgent','Dead') DEFAULT NULL,
  ADD COLUMN `trauma` enum('Trauma','Non Trauma') DEFAULT 'Non Trauma',
  ADD COLUMN `penyebab_trauma` set('Kec. Lalu Lintas','Kec. Kerja','KDRT','Kekerasan Anak','Kekerasan Lainnya','Huru-Hara','Gigitan','Intoksikasi','Bencana Alam') DEFAULT NULL,
  ADD COLUMN `hasil_ctas` varchar(50) DEFAULT NULL,
  ADD COLUMN `hamil` enum('Tidak','Ya') DEFAULT 'Tidak';

-- Catatan: pembiayaan pasien sudah tercatat di reg_periksa.png_jawab
-- dan sep_bpjs — tidak perlu duplikasi kolom baru.

-- -----------------------------------------------------------
-- BLOK 2. AIRWAY
-- -----------------------------------------------------------
ALTER TABLE `penilaian_medis_igd`
  ADD COLUMN `airway` enum('Bebas','Tidak Bebas') DEFAULT NULL,
  ADD COLUMN `airway_obstruksi` set('Palatum mole jatuh','Sputum','Benda Asing','Darah','Spasme') DEFAULT NULL,
  ADD COLUMN `airway_suara_nafas` enum('Normal','Stridor','Tidak ada suara nafas') DEFAULT NULL,
  ADD COLUMN `airway_lain` varchar(200) DEFAULT NULL;

-- -----------------------------------------------------------
-- BLOK 3. BREATHING
-- -----------------------------------------------------------
ALTER TABLE `penilaian_medis_igd`
  ADD COLUMN `breathing_pola` set('Apnoe','Bradipnoe','Orthopnoe','Dyspnoe','Takipnoe') DEFAULT NULL,
  ADD COLUMN `breathing_sao2` varchar(5) DEFAULT NULL,
  ADD COLUMN `breathing_bunyi` set('Vesikuler','Wheezing','Stridor','Ronchi') DEFAULT NULL,
  ADD COLUMN `breathing_irama` enum('Teratur','Tidak teratur') DEFAULT NULL,
  ADD COLUMN `breathing_otot_bantu` set('Penggunaan otot bantu','Cuping hidung','Retraksi dada/intercostal') DEFAULT NULL,
  ADD COLUMN `breathing_jalan` enum('Pernafasan dada','Pernafasan perut') DEFAULT NULL;

-- Catatan: kolom `rr` (frekuensi nafas) sudah ada — dipakai ulang.

-- -----------------------------------------------------------
-- BLOK 4. CIRCULATION
-- -----------------------------------------------------------
ALTER TABLE `penilaian_medis_igd`
  ADD COLUMN `circ_akral` enum('Hangat','Dingin') DEFAULT NULL,
  ADD COLUMN `circ_pucat` enum('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN `circ_cianosis` enum('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN `circ_kapiler` enum('<2 detik','>2 detik') DEFAULT NULL,
  ADD COLUMN `circ_nadi_teraba` enum('Tidak Teraba','Teraba') DEFAULT NULL,
  ADD COLUMN `circ_irama` enum('Reguler','Irreguler') DEFAULT NULL,
  ADD COLUMN `circ_kekuatan` enum('Kuat','Lemah') DEFAULT NULL,
  ADD COLUMN `circ_kehilangan_cairan` set('Diare','Muntah','Luka bakar','Perdarahan') DEFAULT NULL,
  ADD COLUMN `circ_perdarahan` enum('Tidak','Ya') DEFAULT NULL,
  ADD COLUMN `circ_perdarahan_lokasi` varchar(200) DEFAULT NULL,
  ADD COLUMN `circ_kelembaban` enum('Lembab','Kering') DEFAULT NULL,
  ADD COLUMN `circ_turgor` enum('Normal','Kurang') DEFAULT NULL,
  ADD COLUMN `circ_edema` enum('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN `circ_output_urine` varchar(10) DEFAULT NULL,
  ADD COLUMN `circ_luka_bakar_luas` varchar(10) DEFAULT NULL,
  ADD COLUMN `circ_luka_bakar_grade` enum('I','II','III','IV') DEFAULT NULL;

-- Catatan: `td` (tekanan darah) dan `nadi` (frekuensi) reuse yang lama.

-- -----------------------------------------------------------
-- BLOK 5. DISABILITY
-- -----------------------------------------------------------
ALTER TABLE `penilaian_medis_igd`
  ADD COLUMN `gcs_kategori` enum('Dewasa','Anak') DEFAULT 'Dewasa',
  ADD COLUMN `gcs_e` tinyint(1) DEFAULT NULL,
  ADD COLUMN `gcs_v` tinyint(1) DEFAULT NULL,
  ADD COLUMN `gcs_m` tinyint(1) DEFAULT NULL,
  ADD COLUMN `pupil_bentuk` enum('Normal','Tidak') DEFAULT NULL,
  ADD COLUMN `pupil_respon_cahaya` enum('+','-','+/-','-/+') DEFAULT NULL,
  ADD COLUMN `pupil_ukuran` enum('Isokor','Anisokor') DEFAULT NULL,
  ADD COLUMN `pupil_diameter_ka` enum('1 mm','2 mm','3 mm','4 mm') DEFAULT NULL,
  ADD COLUMN `pupil_diameter_ki` enum('1 mm','2 mm','3 mm','4 mm') DEFAULT NULL,
  ADD COLUMN `ekstremitas_sensorik` enum('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN `ekstremitas_motorik` enum('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN `kekuatan_otot` varchar(50) DEFAULT NULL;

-- Catatan: `gcs` (total sudah ada) tetap ada — bisa auto-fill dari e+v+m.
-- `kesadaran` sudah ada — reuse.

-- -----------------------------------------------------------
-- BLOK 6. EXPOSURE
-- -----------------------------------------------------------
ALTER TABLE `penilaian_medis_igd`
  ADD COLUMN `expo_deformitas` enum('Tidak','Ya') DEFAULT NULL,
  ADD COLUMN `expo_deformitas_daerah` varchar(200) DEFAULT NULL,
  ADD COLUMN `expo_contusion` enum('Tidak','Ya') DEFAULT NULL,
  ADD COLUMN `expo_contusion_daerah` varchar(200) DEFAULT NULL,
  ADD COLUMN `expo_abrasi` enum('Tidak','Ya') DEFAULT NULL,
  ADD COLUMN `expo_abrasi_daerah` varchar(200) DEFAULT NULL,
  ADD COLUMN `expo_penetrasi` enum('Tidak','Ya') DEFAULT NULL,
  ADD COLUMN `expo_penetrasi_daerah` varchar(200) DEFAULT NULL,
  ADD COLUMN `expo_laserasi` enum('Tidak','Ya') DEFAULT NULL,
  ADD COLUMN `expo_laserasi_daerah` varchar(200) DEFAULT NULL,
  ADD COLUMN `expo_edema` enum('Tidak','Ya') DEFAULT NULL,
  ADD COLUMN `expo_edema_daerah` varchar(200) DEFAULT NULL,
  ADD COLUMN `expo_ukuran_luka` varchar(50) DEFAULT NULL,
  ADD COLUMN `expo_kedalaman_luka` varchar(50) DEFAULT NULL,
  ADD COLUMN `nyeri` enum('Tidak','Ya') DEFAULT NULL,
  ADD COLUMN `nyeri_skala` tinyint(2) DEFAULT NULL,
  ADD COLUMN `nyeri_p` varchar(200) DEFAULT NULL,
  ADD COLUMN `nyeri_q` varchar(200) DEFAULT NULL,
  ADD COLUMN `nyeri_r` varchar(200) DEFAULT NULL,
  ADD COLUMN `nyeri_s` varchar(200) DEFAULT NULL,
  ADD COLUMN `nyeri_t` varchar(200) DEFAULT NULL;

-- -----------------------------------------------------------
-- BLOK 7. FAHRENHEID (paparan suhu)
-- -----------------------------------------------------------
ALTER TABLE `penilaian_medis_igd`
  ADD COLUMN `fahr_suhu` varchar(5) DEFAULT NULL,
  ADD COLUMN `fahr_lama_terpapar` varchar(10) DEFAULT NULL,
  ADD COLUMN `fahr_terpapar_jenis` enum('Panas','Dingin') DEFAULT NULL,
  ADD COLUMN `fahr_riwayat_penyakit` set('Metabolik','Kehilangan cairan','Penyakit SSP') DEFAULT NULL,
  ADD COLUMN `fahr_riwayat` set('Cedera kepala','Dampak tindakan medis (iatrogenic)','Pembagian cairan infus yang terlalu dingin','Pemberian transfusi darah yang masih dingin','Hipoglikemia') DEFAULT NULL;

-- Catatan: kolom `suhu` reuse (dinilai ganda dengan fahr_suhu — di
-- form input akan disamakan; dua-duanya diisi supaya laporan lain
-- yang sudah pakai kolom `suhu` tidak rusak).

-- -----------------------------------------------------------
-- BLOK 8. DIAGNOSA + STATUS LOKALIS gambar
-- -----------------------------------------------------------
ALTER TABLE `penilaian_medis_igd`
  ADD COLUMN `diagnosa_kerja` varchar(500) DEFAULT NULL,
  ADD COLUMN `diagnosa_kerja_icd10` varchar(20) DEFAULT NULL,
  ADD COLUMN `diagnosa_banding` varchar(500) DEFAULT NULL,
  ADD COLUMN `diagnosa_banding_icd10` varchar(20) DEFAULT NULL,
  ADD COLUMN `lokalis_gambar` mediumblob DEFAULT NULL;

-- Catatan: `diagnosis` lama tetap ada — nanti diisi ringkas
-- (kerja + banding digabung) supaya cetakan lain yang sudah
-- pakai kolom `diagnosis` tidak berubah.

-- -----------------------------------------------------------
-- BLOK 9. RENCANA TINDAK LANJUT
-- -----------------------------------------------------------
ALTER TABLE `penilaian_medis_igd`
  ADD COLUMN `pengantar_pasien` enum('Ada','Tidak ada') DEFAULT NULL,
  ADD COLUMN `rtl_rawat_inap` enum('Ya','Tidak') DEFAULT 'Tidak',
  ADD COLUMN `rtl_ranap_ruang` varchar(100) DEFAULT NULL,
  ADD COLUMN `rtl_ranap_kelas` varchar(50) DEFAULT NULL,
  ADD COLUMN `rtl_ranap_indikasi` varchar(500) DEFAULT NULL,
  ADD COLUMN `rtl_dpjp` varchar(20) DEFAULT NULL,
  ADD COLUMN `rtl_tindak_lanjut_di` set('Kamar Operasi','Kamar Bersalin') DEFAULT NULL,
  ADD COLUMN `rtl_rujuk` enum('Ya','Tidak') DEFAULT 'Tidak',
  ADD COLUMN `rtl_rujuk_ke` set('Rumah Sakit','Puskesmas','Dokter','Dokter Keluarga','Home Care') DEFAULT NULL,
  ADD COLUMN `rtl_rujuk_tujuan_nama` varchar(200) DEFAULT NULL,
  ADD COLUMN `rtl_rujuk_alasan` set('Klinikal','Non Klinikal','Permintaan pasien / keluarga','Keterbatasan fasilitas') DEFAULT NULL,
  ADD COLUMN `rtl_dipulangkan` enum('Ya','Tidak') DEFAULT 'Tidak',
  ADD COLUMN `rtl_kontrol_poli` varchar(100) DEFAULT NULL,
  ADD COLUMN `rtl_kontrol_tanggal` date DEFAULT NULL,
  ADD COLUMN `rtl_meninggal` enum('Ya','Tidak') DEFAULT 'Tidak',
  ADD COLUMN `rtl_meninggal_tanggal` datetime DEFAULT NULL,
  ADD COLUMN `rtl_meninggal_jenis` enum('Permintaan sendiri','DOA') DEFAULT NULL,
  ADD COLUMN `rtl_pulang_aps` enum('Ya','Tidak') DEFAULT 'Tidak',
  ADD COLUMN `rtl_melarikan_diri` enum('Ya','Tidak') DEFAULT 'Tidak',
  ADD COLUMN `rtl_transportasi` enum('Ambulans','Kendaraan pribadi','Kendaraan Jenazah') DEFAULT NULL;

-- -----------------------------------------------------------
-- BLOK 10. EDUKASI PASIEN
-- -----------------------------------------------------------
ALTER TABLE `penilaian_medis_igd`
  ADD COLUMN `edu_penerima` enum('Pasien','Keluarga Pasien') DEFAULT NULL,
  ADD COLUMN `edu_nama` varchar(100) DEFAULT NULL,
  ADD COLUMN `edu_hubungan` varchar(50) DEFAULT NULL,
  ADD COLUMN `edu_alamat` varchar(255) DEFAULT NULL,
  ADD COLUMN `edu_kel` varchar(50) DEFAULT NULL,
  ADD COLUMN `edu_kec` varchar(50) DEFAULT NULL,
  ADD COLUMN `edu_kab_kota` varchar(50) DEFAULT NULL,
  ADD COLUMN `edu_no_telp` varchar(20) DEFAULT NULL,
  ADD COLUMN `edu_tidak_bisa` enum('Ya','Tidak') DEFAULT 'Tidak',
  ADD COLUMN `edu_hambatan` set(
      'Gangguan penglihatan','Gangguan pendengaran','Gangguan emosional',
      'Hambatan bahasa','Perbedaan Budaya','Tingkat pendidikan',
      'Motivasi belajar','Gangguan proses pikir','Batasan jasmani dan kognitif',
      'Kepercayaan pasien dan keluarga','Lain-lain') DEFAULT NULL,
  ADD COLUMN `edu_hambatan_lain` varchar(200) DEFAULT NULL,
  ADD COLUMN `edu_pulang` set('Makan / minum obat teratur','Jaga kebersihan luka','Diet','Lain-lain') DEFAULT NULL,
  ADD COLUMN `edu_pulang_lain` varchar(200) DEFAULT NULL;


-- ============================================================
-- TABEL PENDUKUNG (opsional, kalau mau lebih rapi)
-- ============================================================

-- Diagnosa kerja & pembanding — 1 pasien bisa punya beberapa
-- diagnosis. Kalau tidak dipakai, cukup pakai kolom di atas.
CREATE TABLE IF NOT EXISTS `penilaian_medis_igd_diagnosa` (
  `no_rawat` varchar(17) NOT NULL,
  `urut` int(11) NOT NULL AUTO_INCREMENT,
  `jenis` enum('Kerja','Pembanding') NOT NULL,
  `kd_penyakit` varchar(10) DEFAULT NULL,
  `deskripsi` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`urut`),
  KEY `no_rawat` (`no_rawat`),
  CONSTRAINT `penilaian_medis_igd_diagnosa_ibfk_1`
    FOREIGN KEY (`no_rawat`) REFERENCES `penilaian_medis_igd` (`no_rawat`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ============================================================
-- SANITY CHECK
-- ============================================================
-- Setelah migrasi, harusnya:
--   SHOW COLUMNS FROM penilaian_medis_igd;      -- ±127 baris
--   SELECT COUNT(*) FROM penilaian_medis_igd;   -- jumlah data lama sama
--   SELECT * FROM penilaian_medis_igd LIMIT 1;  -- data lama utuh
-- ============================================================
