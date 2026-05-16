-- ============================================================
-- Data Mapping Prosedur SNOMED CT -> ICD-9 CM
-- Untuk tabel: mapping_prosedur_smart_klaim_bpjs
-- Smart Klaim BPJS - SIMRS Khanza
-- ============================================================
-- Struktur kolom: kode_snomed, display, system, icd9
-- system = http://snomed.info/sct (SNOMED CT standard)
-- icd9   = harus ada di tabel icd9 (kode)
-- ============================================================
-- CARA IMPORT:
-- MySQL: source /path/ke/file/mapping_prosedur_smart_klaim_bpjs.sql
-- Atau copy-paste ke HeidiSQL / phpMyAdmin / DBeaver
-- ============================================================

SET NAMES utf8mb4;

INSERT IGNORE INTO `mapping_prosedur_smart_klaim_bpjs`
    (`kode_snomed`, `display`, `system`, `icd9`)
VALUES

-- ============================================================
-- TINDAKAN RAWAT JALAN
-- ============================================================
('28114007',  'Injeksi intravena',                                'http://snomed.info/sct', '99.18'),
('76962009',  'Injeksi intramuskular',                           'http://snomed.info/sct', '99.29'),
('47537003',  'Pemasangan infus intravena',                      'http://snomed.info/sct', '99.18'),
('182531007', 'Perawatan dan ganti balut luka',                  'http://snomed.info/sct', '93.57'),
('26449006',  'Terapi nebulisasi',                               'http://snomed.info/sct', '93.94'),
('57485005',  'Pemberian oksigen',                               'http://snomed.info/sct', '93.96'),
('44946007',  'Pemasangan kateter urin menetap',                 'http://snomed.info/sct', '57.94'),
('16740008',  'Pemasangan selang nasogastrik (NGT)',             'http://snomed.info/sct', '96.07'),
('29303009',  'Elektrokardiogram (EKG/ECG)',                     'http://snomed.info/sct', '89.52'),
('116859006', 'Transfusi darah',                                 'http://snomed.info/sct', '99.04'),
('305180004', 'Jahit luka (penjahitan)',                         'http://snomed.info/sct', '86.59'),
('128928008', 'Insisi dan drainase',                             'http://snomed.info/sct', '86.04'),
('229070002', 'Fisioterapi / rehabilitasi medis',                'http://snomed.info/sct', '93.39'),
('310249008', 'Pelepasan kateter urin',                          'http://snomed.info/sct', '97.64'),
('182992009', 'Ekstraksi benda asing',                           'http://snomed.info/sct', '98.29'),

-- ============================================================
-- TINDAKAN BEDAH / OPERASI
-- ============================================================
('80146002',  'Apendektomi (terbuka)',                           'http://snomed.info/sct', '47.09'),
('44950006',  'Apendektomi laparoskopik',                        'http://snomed.info/sct', '47.01'),
('11466000',  'Seksio Sesarea segmen bawah rahim',               'http://snomed.info/sct', '74.1'),
('173171007', 'Laparotomi eksplorasi',                           'http://snomed.info/sct', '54.11'),
('44558001',  'Herniorafi / perbaikan hernia inguinal',          'http://snomed.info/sct', '53.00'),
('38102005',  'Kolesistektomi (terbuka)',                         'http://snomed.info/sct', '51.22'),
('45595009',  'Kolesistektomi laparoskopik',                     'http://snomed.info/sct', '51.23'),
('173422009', 'Tonsilektomi',                                    'http://snomed.info/sct', '28.2'),
('36777000',  'Debridemen luka eksisi',                          'http://snomed.info/sct', '86.22'),
('72310004',  'Sirkumsisi',                                      'http://snomed.info/sct', '64.0'),
('54885007',  'Ekstraksi katarak',                               'http://snomed.info/sct', '13.59'),
('25353009',  'Kraniotomi',                                      'http://snomed.info/sct', '01.24'),
('174384003', 'Amputasi ekstremitas bawah',                      'http://snomed.info/sct', '84.15'),
('31638003',  'Kolostomi',                                       'http://snomed.info/sct', '46.10'),
('53611008',  'Gastrektomi parsial',                             'http://snomed.info/sct', '43.6'),
('56306000',  'Nefrektomi',                                      'http://snomed.info/sct', '55.51'),
('176697007', 'Prostatektomi transuretra (TURP)',                'http://snomed.info/sct', '60.29'),
('173886002', 'Operasi katarak dengan IOL',                      'http://snomed.info/sct', '13.71'),

-- ============================================================
-- TINDAKAN DIAGNOSTIK / PEMERIKSAAN PENUNJANG
-- ============================================================
('386807006', 'Esofagogastroduodenoskopi (EGD)',                 'http://snomed.info/sct', '44.13'),
('73761001',  'Kolonoskopi',                                     'http://snomed.info/sct', '45.23'),
('41976001',  'Kateterisasi jantung kiri',                       'http://snomed.info/sct', '37.22'),
('29893006',  'Kateterisasi jantung kanan',                      'http://snomed.info/sct', '37.21'),
('45036003',  'USG abdomen diagnostik',                          'http://snomed.info/sct', '88.76'),
('304700009', 'USG kandungan / uterus gravid',                   'http://snomed.info/sct', '88.78'),
('40701008',  'Ekokardiografi',                                  'http://snomed.info/sct', '88.72'),
('303653007', 'CT Scan kepala',                                  'http://snomed.info/sct', '87.03'),
('433073001', 'MRI otak',                                        'http://snomed.info/sct', '88.91'),
('10847001',  'Bronkoskopi serat optik',                         'http://snomed.info/sct', '33.22'),
('86273004',  'Biopsi jaringan kulit / subkutan',                'http://snomed.info/sct', '86.11'),
('277762005', 'Pungsi lumbal / spinal tap',                      'http://snomed.info/sct', '03.31'),
('54550000',  'Elektroensefalogram (EEG)',                       'http://snomed.info/sct', '89.14'),
('127783003', 'Spirometri / uji fungsi paru',                    'http://snomed.info/sct', '89.37'),
('168731009', 'Rontgen thorax / dada',                           'http://snomed.info/sct', '87.44'),

-- ============================================================
-- TINDAKAN KEBIDANAN
-- ============================================================
('177141003', 'Persalinan pervaginam spontan',                   'http://snomed.info/sct', '73.59'),
('85548006',  'Episiotomi',                                      'http://snomed.info/sct', '73.6'),
('236974004', 'Manual plasenta',                                 'http://snomed.info/sct', '75.4'),
('61586001',  'Vakum ekstraksi',                                 'http://snomed.info/sct', '72.71'),
('302383004', 'Persalinan dengan forsep',                        'http://snomed.info/sct', '72.2'),
('34536000',  'Amniosentesis',                                   'http://snomed.info/sct', '75.1'),
('65200003',  'Pemasangan IUD / AKDR',                           'http://snomed.info/sct', '69.7'),
('18209004',  'Ligasi tuba bilateral (KB operasi)',              'http://snomed.info/sct', '66.32'),
('65801008',  'Dilatasi dan kuretase (D&C)',                     'http://snomed.info/sct', '69.09'),
('288189009', 'Perbaikan robekan perineum',                      'http://snomed.info/sct', '75.69');

-- ============================================================
-- Verifikasi setelah import:
-- SELECT COUNT(*) FROM mapping_prosedur_smart_klaim_bpjs;
-- SELECT m.*, i.deskripsi_panjang FROM mapping_prosedur_smart_klaim_bpjs m
-- INNER JOIN icd9 i ON m.icd9 = i.kode ORDER BY m.kode_snomed;
-- ============================================================
