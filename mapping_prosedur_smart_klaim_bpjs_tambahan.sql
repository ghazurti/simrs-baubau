-- ============================================================
-- Data Mapping Prosedur SNOMED CT -> ICD-9 CM (TAMBAHAN)
-- Tabel: mapping_prosedur_smart_klaim_bpjs
-- Kategori: IGD, ICU, Hemodialisis, Kemoterapi,
--           Bedah lanjutan, Diagnostik lanjutan,
--           Kebidanan lanjutan
-- ============================================================

SET NAMES utf8mb4;

INSERT IGNORE INTO `mapping_prosedur_smart_klaim_bpjs`
    (`kode_snomed`, `display`, `system`, `icd9`)
VALUES

-- ============================================================
-- TINDAKAN IGD / GAWAT DARURAT
-- ============================================================
('89666000',  'Resusitasi jantung paru (RJP/CPR)',              'http://snomed.info/sct', '99.60'),
('308546005', 'Defibrilasi jantung',                            'http://snomed.info/sct', '99.62'),
('112798008', 'Intubasi endotrakeal',                           'http://snomed.info/sct', '96.04'),
('182700009', 'Reposisi / reduksi fraktur tertutup',            'http://snomed.info/sct', '79.00'),
('264286008', 'Pemasangan gips',                                'http://snomed.info/sct', '93.53'),
('182354008', 'Pemasangan bidai / splint',                      'http://snomed.info/sct', '93.54'),
('68810005',  'Resusitasi cairan intravena',                    'http://snomed.info/sct', '99.18'),
('226234005', 'Bilas lambung',                                  'http://snomed.info/sct', '96.33'),
('431182000', 'Krikotiroidotomi darurat',                       'http://snomed.info/sct', '31.1'),
('232727009', 'Penjahitan laserasi kepala',                     'http://snomed.info/sct', '86.59'),

-- ============================================================
-- TINDAKAN ICU / CRITICAL CARE
-- ============================================================
('40617009',  'Ventilasi mekanik (bantuan napas mesin)',        'http://snomed.info/sct', '96.70'),
('392230005', 'Pemasangan kateter vena sentral (CVC/CVP)',      'http://snomed.info/sct', '38.93'),
('45201005',  'Pemasangan arterial line',                       'http://snomed.info/sct', '38.91'),
('239541005', 'Pemasangan WSD (water seal drainage)',           'http://snomed.info/sct', '34.04'),
('91602002',  'Torakosentesis / punksi pleura',                 'http://snomed.info/sct', '34.91'),
('86088003',  'Parasentesis / punksi ascites',                  'http://snomed.info/sct', '54.91'),
('405326008', 'Pemasangan PICC (peripherally inserted CVC)',    'http://snomed.info/sct', '38.93'),
('57024004',  'Monitoring tekanan vena sentral (CVP)',          'http://snomed.info/sct', '89.62'),
('43825004',  'Pemasangan pacemaker sementara',                 'http://snomed.info/sct', '37.78'),

-- ============================================================
-- HEMODIALISIS & DIALISIS
-- ============================================================
('302497006', 'Hemodialisis',                                   'http://snomed.info/sct', '39.95'),
('71192002',  'Peritoneal dialisis',                            'http://snomed.info/sct', '54.98'),
('234556001', 'Pemasangan akses vaskular untuk hemodialisis',   'http://snomed.info/sct', '39.27'),
('174800002', 'Pembuatan AV shunt / fistula untuk HD',          'http://snomed.info/sct', '39.27'),

-- ============================================================
-- KEMOTERAPI & RADIOTERAPI
-- ============================================================
('367336001', 'Kemoterapi antineoplastik (infus)',              'http://snomed.info/sct', '99.25'),
('108290001', 'Radioterapi eksterna',                           'http://snomed.info/sct', '92.29'),
('394902000', 'Kemoterapi oral',                                'http://snomed.info/sct', '99.25'),
('31438003',  'Terapi radiasi intrakavitas',                    'http://snomed.info/sct', '92.27'),

-- ============================================================
-- TINDAKAN BEDAH LANJUTAN
-- ============================================================
('172043006', 'Mastektomi sederhana',                           'http://snomed.info/sct', '85.41'),
('30956003',  'Tiroidektomi total',                             'http://snomed.info/sct', '06.4'),
('116140006', 'Histerektomi total abdominal',                   'http://snomed.info/sct', '68.49'),
('116143008', 'Histerektomi laparoskopik',                      'http://snomed.info/sct', '68.41'),
('67097003',  'Splenektomi',                                    'http://snomed.info/sct', '41.5'),
('48387007',  'Trakeostomi',                                    'http://snomed.info/sct', '31.1'),
('4558008',   'Hemorrhoidektomi',                               'http://snomed.info/sct', '49.46'),
('22523008',  'Vasektomi',                                      'http://snomed.info/sct', '63.73'),
('74596007',  'Laparoskopi diagnostik',                         'http://snomed.info/sct', '54.21'),
('81060000',  'Fistulektomi / fistulotomi anal',                'http://snomed.info/sct', '49.11'),
('80872001',  'Repair tendon',                                  'http://snomed.info/sct', '83.42'),
('73056005',  'ORIF (open reduction internal fixation)',        'http://snomed.info/sct', '79.39'),
('14361000',  'Skin graft / cangkok kulit',                     'http://snomed.info/sct', '86.60'),
('84275009',  'Bedah sinus paranasal (FESS)',                   'http://snomed.info/sct', '22.60'),
('54535009',  'Prostatektomi terbuka',                          'http://snomed.info/sct', '60.5'),
('173418001', 'Adenotonsilektomi',                              'http://snomed.info/sct', '28.3'),
('36253005',  'Herniorafi umbilikal',                           'http://snomed.info/sct', '53.42'),
('55208006',  'Repair ventral hernia / hernia insisional',      'http://snomed.info/sct', '53.59'),
('32771006',  'Operasi katarak dengan fakoemulsifikasi',        'http://snomed.info/sct', '13.41'),

-- ============================================================
-- TINDAKAN DIAGNOSTIK LANJUTAN
-- ============================================================
('77477000',  'CT scan thorax',                                 'http://snomed.info/sct', '87.41'),
('169070004', 'CT scan abdomen',                                'http://snomed.info/sct', '88.01'),
('241615005', 'MRI vertebra / tulang belakang',                 'http://snomed.info/sct', '88.93'),
('45321001',  'USG tiroid',                                     'http://snomed.info/sct', '88.71'),
('47079000',  'USG payudara / mamografi',                       'http://snomed.info/sct', '88.73'),
('174589003', 'ERCP (endoskopi retrograd kolangiopankreatografi)','http://snomed.info/sct','51.10'),
('33367009',  'Angiografi koroner',                             'http://snomed.info/sct', '88.57'),
('44578006',  'Biopsi jarum halus (FNAB)',                      'http://snomed.info/sct', '40.11'),
('122459003', 'Biopsi hati perkutan',                           'http://snomed.info/sct', '50.11'),
('177882009', 'Elektrofisiologi jantung',                       'http://snomed.info/sct', '37.26'),
('6953000',   'Treadmill test / uji latih jantung',             'http://snomed.info/sct', '89.41'),
('241531003', 'Holter monitoring EKG 24 jam',                   'http://snomed.info/sct', '89.50'),
('16830007',  'Rontgen tulang (bone survey)',                    'http://snomed.info/sct', '88.31'),
('73201008',  'USG muskuloskeletal',                            'http://snomed.info/sct', '88.79'),

-- ============================================================
-- TINDAKAN KEBIDANAN LANJUTAN
-- ============================================================
('236958009', 'Induksi persalinan',                             'http://snomed.info/sct', '73.01'),
('70473003',  'Versi luar (external cephalic version)',         'http://snomed.info/sct', '73.91'),
('274025005', 'Sterilisasi wanita (MOW)',                       'http://snomed.info/sct', '66.29'),
('176804006', 'Biopsi endometrium',                             'http://snomed.info/sct', '68.16'),
('168741007', 'Histerosalpingografi (HSG)',                     'http://snomed.info/sct', '87.83'),
('24892003',  'Pemasangan implant KB',                          'http://snomed.info/sct', '99.29'),
('169553002', 'Injeksi kontrasepsi (KB suntik)',                'http://snomed.info/sct', '99.29'),
('386638009', 'Observasi dalam persalinan (kala I-IV)',         'http://snomed.info/sct', '73.59'),

-- ============================================================
-- TINDAKAN RAWAT JALAN TAMBAHAN
-- ============================================================
('171149006', 'Papanicolaou smear / pap smear',                 'http://snomed.info/sct', '91.46'),
('229070002', 'Latihan fisik terapeutik',                       'http://snomed.info/sct', '93.19'),
('410618006', 'Konseling medis',                                'http://snomed.info/sct', '94.09'),
('229070003', 'Terapi okupasi',                                 'http://snomed.info/sct', '93.83'),
('229558007', 'Terapi wicara',                                  'http://snomed.info/sct', '93.75'),
('428191000124101', 'Pemasangan bidai jari',                    'http://snomed.info/sct', '93.56');

-- ============================================================
-- Verifikasi:
-- SELECT COUNT(*) FROM mapping_prosedur_smart_klaim_bpjs;
-- SELECT m.display, m.icd9, i.deskripsi_panjang
-- FROM mapping_prosedur_smart_klaim_bpjs m
-- INNER JOIN icd9 i ON m.icd9 = i.kode
-- ORDER BY m.kode_snomed;
-- ============================================================
