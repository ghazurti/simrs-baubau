-- Satu Sehat: Kirim Composition RME (Triase IGD & Penilaian Awal Keperawatan IGD)
-- Diambil dari sik.sql SIMRS Khanza upstream. Hanya MENAMBAH kolom penyimpan ID kiriman Satu Sehat,
-- tidak mengubah/menghapus data. Kolom ditambahkan di akhir tabel (urutan sama dengan upstream).
-- PENTING: jalankan file ini BERSAMAAN dengan update aplikasi (RMTriaseIGD & RMPenilaianAwalKeperawatanIGD
-- sudah disesuaikan). Aplikasi lama yang belum diupdate akan GAGAL menyimpan Triase/Asesmen IGD setelah SQL ini dijalankan.
-- MySQL 5.7: hapus 'IF NOT EXISTS' dan jalankan sekali saja.

ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `id_observation_cara_masuk` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `id_observation_alat_transportasi` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `id_observation_alasan_kedatangan` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `id_observation_macam_kasus` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `id_observation_tekanan_darah` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `id_observation_nadi` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `id_observation_pernapasan` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `id_observation_suhu` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `id_observation_saturasi_o2` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `id_observation_nyeri` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `id_composition` varchar(40) DEFAULT NULL;

ALTER TABLE `data_triase_igdprimer` ADD COLUMN IF NOT EXISTS `id_observation_keluhan_utama` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igdprimer` ADD COLUMN IF NOT EXISTS `id_observation_kebutuhan_khusus` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igdprimer` ADD COLUMN IF NOT EXISTS `id_observation_catatan` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igdprimer` ADD COLUMN IF NOT EXISTS `id_careplan_keputusan` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igdprimer` ADD COLUMN IF NOT EXISTS `id_observation_skala` varchar(40) NOT NULL;

ALTER TABLE `data_triase_igdsekunder` ADD COLUMN IF NOT EXISTS `id_clinicalimpression_anamnesa` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igdsekunder` ADD COLUMN IF NOT EXISTS `id_observation_catatan` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igdsekunder` ADD COLUMN IF NOT EXISTS `id_careplan_keputusan` varchar(40) DEFAULT NULL;
ALTER TABLE `data_triase_igdsekunder` ADD COLUMN IF NOT EXISTS `id_observation_skala` varchar(40) NOT NULL;

ALTER TABLE `data_triase_igddetail_skala1` ADD COLUMN IF NOT EXISTS `id_observation_skala1` varchar(40) DEFAULT NULL;

ALTER TABLE `data_triase_igddetail_skala2` ADD COLUMN IF NOT EXISTS `id_observation_skala2` varchar(40) DEFAULT NULL;

ALTER TABLE `data_triase_igddetail_skala3` ADD COLUMN IF NOT EXISTS `id_observation_skala3` varchar(40) DEFAULT NULL;

ALTER TABLE `data_triase_igddetail_skala4` ADD COLUMN IF NOT EXISTS `id_observation_skala4` varchar(40) DEFAULT NULL;

ALTER TABLE `data_triase_igddetail_skala5` ADD COLUMN IF NOT EXISTS `id_observation_skala5` varchar(40) DEFAULT NULL;

ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_informasi` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_keluhan_utama` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_rpd` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_rpo` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_status_kehamilan` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_gravida` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_para` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_abortus` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_hpht` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_tekanan` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_pupil` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_neurosensorik` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_integumen` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_turgor` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_edema` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_mukosa` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_perdarahan` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_intoksikasi` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_eliminasi` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_psikologis` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_jiwa` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_perilaku` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_hubungan` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_tinggal_dengan` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_budaya` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_pendidikan_pj` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_edukasi` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_kemampuan` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_aktifitas` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_alat_bantu` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_nyeri` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_observation_resiko_jatuh` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_careplan_rencana` varchar(40) DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `id_composition` varchar(40) DEFAULT '';

-- Hak akses menu
ALTER TABLE user ADD COLUMN IF NOT EXISTS satu_sehat_kirim_composition ENUM('true','false') NOT NULL DEFAULT 'false';
