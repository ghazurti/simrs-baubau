-- ============================================================
-- Data Mapping Prosedur SNOMED CT -> ICD-9 CM (KOMPREHENSIF)
-- Tabel: mapping_prosedur_smart_klaim_bpjs
-- Mencakup semua bab ICD-9 prosedur (00-99)
-- ============================================================

SET NAMES utf8mb4;

INSERT IGNORE INTO `mapping_prosedur_smart_klaim_bpjs`
    (`kode_snomed`, `display`, `system`, `icd9`)
VALUES

-- ============================================================
-- BAB 01-05: OPERASI SISTEM SARAF
-- ============================================================
('3217005',   'Insisi dan drainase abses intrakranial',         'http://snomed.info/sct', '01.21'),
('8399008',   'Pembukaan ulang situs kraniotomi',               'http://snomed.info/sct', '01.23'),
('7433005',   'Kranioektomi',                                   'http://snomed.info/sct', '01.25'),
('52734007',  'Biopsi jaringan otak (biopsi meninges)',         'http://snomed.info/sct', '01.11'),
('173169000', 'Biopsi terbuka meninges serebral',               'http://snomed.info/sct', '01.14'),
('55815001',  'Lobotomi dan traktotomi otak',                   'http://snomed.info/sct', '01.32'),
('66186001',  'Hemisferektomi',                                 'http://snomed.info/sct', '01.52'),
('173182009', 'Lobektomi otak',                                 'http://snomed.info/sct', '01.53'),
('47020004',  'Insersi shunt ventrikel intrakranial',           'http://snomed.info/sct', '02.21'),
('48284003',  'Revisi shunt ventrikel intrakranial',            'http://snomed.info/sct', '02.22'),
('85558006',  'Shunt ventrikel ke rongga perut (VP shunt)',     'http://snomed.info/sct', '02.31'),
('7540006',   'Penggantian shunt ventrikel',                    'http://snomed.info/sct', '02.42'),
('40607007',  'Pengangkatan shunt ventrikel',                   'http://snomed.info/sct', '02.43'),
('32764009',  'Eksplorasi dan dekompresi kanalis spinalis',     'http://snomed.info/sct', '03.09'),
('56320003',  'Laminektomi',                                    'http://snomed.info/sct', '03.09'),
('51209006',  'Biopsi korda spinalis / meninges spinal',        'http://snomed.info/sct', '03.32'),
('2780002',   'Pungsi lumbal/spinal tap lainnya',               'http://snomed.info/sct', '03.39'),
('42455003',  'Diskektomi / eksisi diskus intervertebralis',    'http://snomed.info/sct', '80.51'),
('57251007',  'Fusi tulang belakang / spinal fusion',           'http://snomed.info/sct', '81.00'),
('44946008',  'Dekompresi saraf tepi',                          'http://snomed.info/sct', '04.41'),
('176946001', 'Pelepasan carpal tunnel (CTR)',                  'http://snomed.info/sct', '04.43'),
('74286000',  'Blok saraf tepi',                                'http://snomed.info/sct', '04.81'),

-- ============================================================
-- BAB 06-07: OPERASI SISTEM ENDOKRIN
-- ============================================================
('13619001',  'Tiroidektomi hemilateral / lobektomi tiroid',   'http://snomed.info/sct', '06.2'),
('22602002',  'Reseksi tiroid subtotal',                       'http://snomed.info/sct', '06.39'),
('173171007', 'Tiroidektomi parsial lainnya',                  'http://snomed.info/sct', '06.39'),
('26212005',  'Paratiroidektomi',                              'http://snomed.info/sct', '06.81'),
('29303009',  'Operasi gondok / strumektomi',                  'http://snomed.info/sct', '06.4'),
('173285001', 'Adrenalektomi unilateral',                      'http://snomed.info/sct', '07.22'),
('174432000', 'Adrenalektomi bilateral',                       'http://snomed.info/sct', '07.3'),
('45619003',  'Biopsi kelenjar tiroid',                        'http://snomed.info/sct', '06.11'),
('29050002',  'Biopsi kelenjar adrenal',                       'http://snomed.info/sct', '07.11'),
('173178001', 'Reseksi kelenjar timus (timektomi)',             'http://snomed.info/sct', '07.80'),

-- ============================================================
-- BAB 08-16: OPERASI MATA
-- ============================================================
('231760002', 'Blefaroplasti / koreksi ptosis kelopak mata',   'http://snomed.info/sct', '08.31'),
('309765007', 'Eksisi kalazion / chalazion',                   'http://snomed.info/sct', '08.21'),
('271440004', 'Repair kelopak mata bawah (ektropion/entropion)','http://snomed.info/sct', '08.42'),
('174823009', 'Enukleasi bola mata',                           'http://snomed.info/sct', '16.49'),
('19279006',  'Eviserasi bola mata',                           'http://snomed.info/sct', '16.39'),
('410493008', 'Trabekulektomi (operasi glaukoma)',              'http://snomed.info/sct', '12.64'),
('56576004',  'Iridektomi',                                    'http://snomed.info/sct', '12.14'),
('172524005', 'Vitrektomi',                                    'http://snomed.info/sct', '14.74'),
('46362003',  'Fotokoagulasi retina dengan laser',             'http://snomed.info/sct', '14.25'),
('6832004',   'Repair ablasio retina (cerclage)',               'http://snomed.info/sct', '14.49'),
('172524005', 'Pengangkatan membran vitreous',                 'http://snomed.info/sct', '14.72'),
('31177006',  'Koreksi strabismus (operasi juling)',            'http://snomed.info/sct', '15.11'),
('229231008', 'Strabismus - reseksi otot ekstraokular',        'http://snomed.info/sct', '15.21'),
('90831004',  'Dacryocystorhinostomy (DCR)',                   'http://snomed.info/sct', '09.81'),
('174817009', 'Operasi pterigium',                             'http://snomed.info/sct', '11.39'),
('1290000',   'Keratoplasti / transplantasi kornea',           'http://snomed.info/sct', '11.60'),
('46332009',  'Keratotomi / keratektomi refraktif',            'http://snomed.info/sct', '11.79'),

-- ============================================================
-- BAB 17-20: OPERASI TELINGA
-- ============================================================
('172861008', 'Miringotomi / insisi timpani',                  'http://snomed.info/sct', '20.01'),
('172862001', 'Pemasangan tube ventilasi telinga (grommet)',   'http://snomed.info/sct', '20.01'),
('48387007',  'Timpanoplasti',                                 'http://snomed.info/sct', '19.4'),
('173193006', 'Mastoidektomi sederhana',                       'http://snomed.info/sct', '20.41'),
('49218005',  'Mastoidektomi radikal',                         'http://snomed.info/sct', '20.42'),
('173201001', 'Mastoidektomi dimodifikasi',                    'http://snomed.info/sct', '20.49'),
('81839001',  'Miringoplasti / repair membran timpani',        'http://snomed.info/sct', '19.4'),
('3298001',   'Stapedektomi (operasi otosklerosis)',           'http://snomed.info/sct', '19.19'),
('173226003', 'Pemasangan implan koklea',                      'http://snomed.info/sct', '20.96'),
('172852004', 'Insisi dan drainase abses telinga',             'http://snomed.info/sct', '18.01'),
('53742009',  'Eksisi tumor telinga luar',                     'http://snomed.info/sct', '18.31'),

-- ============================================================
-- BAB 21-29: OPERASI HIDUNG, MULUT, FARING
-- ============================================================
('173422009', 'Polipektomi hidung',                            'http://snomed.info/sct', '21.31'),
('172406009', 'Kauterisasi / ablasi mukosa hidung',            'http://snomed.info/sct', '21.61'),
('16473004',  'Septoplasti / repair septum hidung',            'http://snomed.info/sct', '21.88'),
('24875000',  'Rinoplasti',                                    'http://snomed.info/sct', '21.87'),
('37157002',  'Antrostomi / drainase sinus maksilaris',        'http://snomed.info/sct', '22.2'),
('14355007',  'Eksisi kista atau tumor sinus',                 'http://snomed.info/sct', '22.39'),
('36985005',  'Nasofaringoskopi / pemeriksaan nasofarings',    'http://snomed.info/sct', '29.19'),
('173466006', 'Adenoidektomi',                                 'http://snomed.info/sct', '28.6'),
('174483001', 'Eksisi kista brankial',                         'http://snomed.info/sct', '29.2'),
('61697003',  'Laringoskopi langsung / direct laryngoscopy',   'http://snomed.info/sct', '31.42'),
('29408003',  'Tonsil abses/peritonsilar drainase',            'http://snomed.info/sct', '28.0'),
('80359001',  'Insisi dan drainase abses submandibula',        'http://snomed.info/sct', '27.0'),
('84270004',  'Ekstraksi gigi molar impaksi',                  'http://snomed.info/sct', '23.19'),
('173392000', 'Penutupan fisura palatum (palatoplasti)',        'http://snomed.info/sct', '27.62'),
('40766002',  'Repair bibir sumbing (labioplasti)',             'http://snomed.info/sct', '27.54'),
('81706006',  'Eksisi kelenjar ludah (sialoadenektomi)',        'http://snomed.info/sct', '26.30'),
('31160004',  'Insisi dan drainase kelenjar ludah',            'http://snomed.info/sct', '26.0'),

-- ============================================================
-- BAB 30-34: OPERASI SISTEM PERNAPASAN
-- ============================================================
('173422009', 'Laringektomi total',                            'http://snomed.info/sct', '30.3'),
('67744001',  'Laringektomi parsial',                          'http://snomed.info/sct', '30.29'),
('64978005',  'Reseksi paru wedge / segmental',                'http://snomed.info/sct', '32.29'),
('392021009', 'Lobektomi paru',                                'http://snomed.info/sct', '32.4'),
('173171007', 'Pneumonektomi total',                           'http://snomed.info/sct', '32.5'),
('44275001',  'Dekortikasi pleura / insisi mediastinum',         'http://snomed.info/sct', '34.1'),
('68159004',  'Torakotomi eksplorasi',                         'http://snomed.info/sct', '34.09'),
('173420001', 'Pleurodesis',                                   'http://snomed.info/sct', '34.92'),
('68565006',  'Reseksi massa mediastinum',                     'http://snomed.info/sct', '34.3'),
('74254000',  'Mediastinoskopi',                               'http://snomed.info/sct', '34.22'),
('16830007',  'Biopsi paru terbuka',                           'http://snomed.info/sct', '33.28'),
('232675003', 'Bronkoskopi kaku / rigid bronchoscopy',         'http://snomed.info/sct', '33.21'),
('78823007',  'Lavase bronkoalveolar (BAL)',                   'http://snomed.info/sct', '33.24'),

-- ============================================================
-- BAB 35-39: OPERASI SISTEM KARDIOVASKULAR
-- ============================================================
('173996001', 'Penggantian katup jantung',                     'http://snomed.info/sct', '35.20'),
('174036003', 'Penggantian katup aorta',                       'http://snomed.info/sct', '35.21'),
('174041002', 'Penggantian katup mitral',                      'http://snomed.info/sct', '35.23'),
('232717009', 'Repair katup mitral (komisurotomi)',             'http://snomed.info/sct', '35.12'),
('232723002', 'Repair katup aorta',                            'http://snomed.info/sct', '35.11'),
('70536003',  'Operasi bypass koroner (CABG)',                 'http://snomed.info/sct', '36.10'),
('6557003',   'Intervensi koroner perkutan (PCI/PTCA)',        'http://snomed.info/sct', '36.09'),
('11101003',  'Pemasangan stent koroner',                      'http://snomed.info/sct', '36.06'),
('175039005', 'Pemasangan pacemaker permanen',                 'http://snomed.info/sct', '37.80'),
('233174007', 'Implantasi ICD (kardioverter defibrilator)',     'http://snomed.info/sct', '37.94'),
('174802008', 'Perikardiotomi / insisi perikardium',           'http://snomed.info/sct', '37.12'),
('44946007',  'Perikardiosentesis',                            'http://snomed.info/sct', '37.0'),
('61459000',  'Reseksi aneurisma aorta',                       'http://snomed.info/sct', '38.44'),
('71493009',  'Embolektomi arteri',                            'http://snomed.info/sct', '38.04'),
('31509003',  'Trombektomi vena',                              'http://snomed.info/sct', '38.09'),
('82403009',  'Ligasi dan stripping vena varikosum',           'http://snomed.info/sct', '38.59'),
('176780005', 'Bypass arteri perifer',                         'http://snomed.info/sct', '39.29'),
('33344001',  'Endarterektomi (CEA)',                          'http://snomed.info/sct', '38.12'),
('174943007', 'Pemasangan filter vena kava',                   'http://snomed.info/sct', '38.7'),
('274025005', 'Amputasi diatas lutut (above knee amputation)', 'http://snomed.info/sct', '84.17'),
('175898006', 'Amputasi bawah lutut (below knee amputation)',  'http://snomed.info/sct', '84.15'),

-- ============================================================
-- BAB 40-41: OPERASI SISTEM LIMFATIK DAN HEMATOLOGI
-- ============================================================
('173420001', 'Diseksi kelenjar getah bening leher',          'http://snomed.info/sct', '40.40'),
('173424008', 'Diseksi kelenjar getah bening aksila',          'http://snomed.info/sct', '40.50'),
('174930001', 'Diseksi kelenjar getah bening inguinal',        'http://snomed.info/sct', '40.54'),
('236987005', 'Biopsi kelenjar getah bening sentinel',         'http://snomed.info/sct', '40.11'),
('58390007',  'Biopsi sumsum tulang (bone marrow biopsy)',     'http://snomed.info/sct', '41.31'),
('61901006',  'Aspirasi sumsum tulang',                        'http://snomed.info/sct', '41.91'),
('58776007',  'Transplantasi sumsum tulang / stem cell',       'http://snomed.info/sct', '41.00'),
('71897006',  'Splenorafi / repair limpa',                     'http://snomed.info/sct', '41.95'),

-- ============================================================
-- BAB 42-54: OPERASI SISTEM PENCERNAAN
-- ============================================================
('40683001',  'Esofagoskopi dengan biopsi',                   'http://snomed.info/sct', '42.24'),
('174443008', 'Esofagektomi parsial',                         'http://snomed.info/sct', '42.41'),
('173387006', 'Esofagektomi total',                           'http://snomed.info/sct', '42.42'),
('72957005',  'Dilatasi esofagus',                            'http://snomed.info/sct', '42.92'),
('173389004', 'Esofagomiotomi (Heller myotomy)',               'http://snomed.info/sct', '42.7'),
('45595009',  'Gastrostomi / pemasangan selang lambung',       'http://snomed.info/sct', '43.19'),
('54551009',  'Gastrektomi total',                            'http://snomed.info/sct', '43.99'),
('43116000',  'Piloroplasti',                                  'http://snomed.info/sct', '44.29'),
('427524001', 'Gastroskopi terapeutik (varises esofagus)',     'http://snomed.info/sct', '44.91'),
('18181002',  'Vagotomi trunkus',                             'http://snomed.info/sct', '44.01'),
('174244000', 'Reseksi usus halus (small bowel resection)',    'http://snomed.info/sct', '45.62'),
('73651003',  'Ileostomi',                                     'http://snomed.info/sct', '46.20'),
('73390009',  'Reseksi kolon kiri / hemikolektomi kiri',      'http://snomed.info/sct', '45.75'),
('307280005', 'Reseksi kolon kanan / hemikolektomi kanan',    'http://snomed.info/sct', '45.73'),
('26390003',  'Reseksi anterior rendah rektum (LAR)',          'http://snomed.info/sct', '48.62'),
('46936009',  'Amputasi abdominoperineal rektum (APR)',        'http://snomed.info/sct', '48.5'),
('173498007', 'Pembentukan kantong ileal (ileal pouch)',       'http://snomed.info/sct', '45.95'),
('52260008',  'Sigmoidoskopi',                                 'http://snomed.info/sct', '45.24'),
('235160006', 'Kolonoskopi terapeutik (polipektomi)',          'http://snomed.info/sct', '45.42'),
('281302004', 'Endoskopi kapsul (kamera pil)',                 'http://snomed.info/sct', '44.19'),
('55697006',  'Transeksi hemoroid / PPH (stapler)',            'http://snomed.info/sct', '49.49'),
('82004009',  'Injeksi skleroterapi hemoroid',                 'http://snomed.info/sct', '49.42'),
('79917009',  'Insisi dan drainase abses perianal',            'http://snomed.info/sct', '49.01'),
('60490007',  'Sfingterotomi anal lateral',                    'http://snomed.info/sct', '49.59'),
('175137000', 'Operasi pilonidal sinus / kista',               'http://snomed.info/sct', '86.21'),
('57087004',  'Hepatektomi parsial',                           'http://snomed.info/sct', '50.22'),
('87601000',  'Drainase abses hati perkutan',                  'http://snomed.info/sct', '50.91'),
('231699004', 'ERCP dengan sfingterotomi',                     'http://snomed.info/sct', '51.85'),
('174422005', 'Kolesistostomi',                                'http://snomed.info/sct', '51.04'),
('236975003', 'Eksplorasi duktus biliaris',                    'http://snomed.info/sct', '51.59'),
('8281000',   'Pankreatektomi parsial',                        'http://snomed.info/sct', '52.59'),
('396440002', 'Operasi Whipple (pankreatoduodenektomi)',        'http://snomed.info/sct', '52.7'),
('83621004',  'Pankreatoskopi / ERCP pankreas',                'http://snomed.info/sct', '52.13'),
('68875008',  'Repair hernia hiatal / fundoplikasi Nissen',    'http://snomed.info/sct', '53.7'),
('174428009', 'Laparotomi second-look',                        'http://snomed.info/sct', '54.12'),

-- ============================================================
-- BAB 55-59: OPERASI SISTEM URINARI
-- ============================================================
('46052005',  'Nefrolitotomi / pengangkatan batu ginjal',      'http://snomed.info/sct', '55.01'),
('175820009', 'Pielolitotomi',                                 'http://snomed.info/sct', '55.11'),
('67305006',  'PCNL (percutaneous nephrolithotomy)',           'http://snomed.info/sct', '55.04'),
('22557002',  'ESWL (litotripsi gelombang kejut)',              'http://snomed.info/sct', '98.51'),
('70571002',  'Nefroureterektomi',                             'http://snomed.info/sct', '55.54'),
('175905003', 'Pieloplasti / repair pelvis ginjal',            'http://snomed.info/sct', '55.87'),
('175902000', 'Transplantasi ginjal',                          'http://snomed.info/sct', '55.69'),
('173434006', 'Ureterolitotomi / pengangkatan batu ureter',    'http://snomed.info/sct', '56.2'),
('174384003', 'Repair ureter (anastomosis/reimplantasi)',       'http://snomed.info/sct', '56.79'),
('236424005', 'Ureteroskopi diagnostik',                       'http://snomed.info/sct', '56.31'),
('40415009',  'Sistoskopi diagnostik',                         'http://snomed.info/sct', '57.32'),
('65823005',  'Sistolitotomi / pengangkatan batu kandung kemih','http://snomed.info/sct', '57.19'),
('174422005', 'Sistektomi parsial',                            'http://snomed.info/sct', '57.6'),
('14861000',  'Sistektomi total',                              'http://snomed.info/sct', '57.71'),
('107010003', 'Uretroplasti / repair uretra',                  'http://snomed.info/sct', '58.39'),
('231821001', 'TURBT (reseksi tumor kandung kemih transuretral)','http://snomed.info/sct', '57.49'),
('310582007', 'Operasi antiinkontinensia (TVT/TOT)',           'http://snomed.info/sct', '59.79'),
('264228006', 'Pemasangan nefrostomi perkutan',                'http://snomed.info/sct', '55.03'),

-- ============================================================
-- BAB 60-64: OPERASI GENITALIA PRIA
-- ============================================================
('50543003',  'Prostatektomi radikal',                         'http://snomed.info/sct', '60.5'),
('80238000',  'Krioterapi prostat',                            'http://snomed.info/sct', '60.62'),
('265764009', 'Biopsi prostat transrektal (TRUS biopsy)',      'http://snomed.info/sct', '60.11'),
('45990005',  'Orkhiektomi unilateral',                        'http://snomed.info/sct', '62.3'),
('236884008', 'Orkhidopeksi (operasi testis undescended)',     'http://snomed.info/sct', '62.5'),
('174431007', 'Operasi varikokel',                             'http://snomed.info/sct', '63.1'),
('10455003',  'Epididimektomi',                                'http://snomed.info/sct', '63.4'),
('236979009', 'Operasi hipospadia',                            'http://snomed.info/sct', '58.45'),
('55048006',  'Amputasi penis',                                'http://snomed.info/sct', '64.3'),
('176609005', 'Implant prostetis penis',                       'http://snomed.info/sct', '64.97'),
('236980007', 'Operasi phimosis (sirkumsisi)',                 'http://snomed.info/sct', '64.0'),

-- ============================================================
-- BAB 65-71: OPERASI GENITALIA WANITA (TAMBAHAN)
-- ============================================================
('116144002', 'Salpingektomi bilateral',                       'http://snomed.info/sct', '66.51'),
('309887000', 'Salpingektomi unilateral',                      'http://snomed.info/sct', '66.4'),
('78148006',  'Ooforektomi / kistektomi ovarium',              'http://snomed.info/sct', '65.29'),
('116992007', 'Laparoskopi ginekologi diagnostik',             'http://snomed.info/sct', '68.14'),
('386637004', 'Miomektomi (eksisi mioma uteri)',               'http://snomed.info/sct', '68.29'),
('265764009', 'Biopsi serviks uteri',                          'http://snomed.info/sct', '67.12'),
('176886006', 'LEEP / konisasi serviks',                       'http://snomed.info/sct', '67.32'),
('386639001', 'Histeroskopi diagnostik',                       'http://snomed.info/sct', '68.19'),
('24195005',  'Vagino-plasty / repair vagina',                 'http://snomed.info/sct', '70.79'),
('44669005',  'Kolporafi anterior (repair prolaps anterior)',  'http://snomed.info/sct', '70.51'),
('60580009',  'Kolporafi posterior (repair prolaps posterior)','http://snomed.info/sct', '70.52'),
('388898005', 'Histeropeksi / operasi prolaps uteri',          'http://snomed.info/sct', '69.22'),
('6750004',   'Pemasangan pessarium',                          'http://snomed.info/sct', '96.18'),

-- ============================================================
-- BAB 72-75: PROSEDUR KEBIDANAN (TAMBAHAN)
-- ============================================================
('177128007', 'Seksio Sesarea klasik',                         'http://snomed.info/sct', '74.0'),
('287970008', 'Pemasangan cerclage serviks (McDonald)',        'http://snomed.info/sct', '67.59'),
('177141003', 'Partus prematurus / persalinan preterm',        'http://snomed.info/sct', '73.59'),
('236973008', 'Ligasi arteri uterina',                         'http://snomed.info/sct', '75.94'),
('82558006',  'Balon tamponade uterus (Bakri balloon)',        'http://snomed.info/sct', '75.99'),

-- ============================================================
-- BAB 76-84: OPERASI MUSKULOSKELETAL
-- ============================================================
('405406007', 'Reduksi terbuka fraktur klavikula',             'http://snomed.info/sct', '79.31'),
('426993003', 'Fiksasi internal fraktur humerus',              'http://snomed.info/sct', '79.31'),
('264220008', 'ORIF fraktur radius/ulna',                      'http://snomed.info/sct', '79.32'),
('44478001',  'ORIF fraktur panggul / asetabulum',             'http://snomed.info/sct', '79.35'),
('179344005', 'ORIF fraktur femur',                            'http://snomed.info/sct', '79.35'),
('426993003', 'ORIF fraktur tibia/fibula',                     'http://snomed.info/sct', '79.36'),
('56306000',  'ORIF fraktur tulang kaki',                      'http://snomed.info/sct', '79.37'),
('179345006', 'Pemasangan pin intramedular (IM nail)',          'http://snomed.info/sct', '79.35'),
('52101004',  'Eksisi eksostosis / benjolan tulang',           'http://snomed.info/sct', '77.60'),
('28304003',  'Bunionektomi / operasi Hallux Valgus',          'http://snomed.info/sct', '77.59'),
('307831009', 'Amputasi jari kaki / jari tangan',              'http://snomed.info/sct', '84.01'),
('18003008',  'Amputasi jari tangan',                          'http://snomed.info/sct', '84.01'),
('36955009',  'Amputasi lengan atas',                          'http://snomed.info/sct', '84.07'),
('179357008', 'Penggantian sendi lutut total (TKR)',            'http://snomed.info/sct', '81.54'),
('52734007',  'Penggantian sendi panggul total (THR)',          'http://snomed.info/sct', '81.51'),
('52734007',  'Penggantian sendi panggul parsial (hemiarthroplasty)','http://snomed.info/sct', '81.52'),
('440596007', 'Artroplasti bahu',                              'http://snomed.info/sct', '81.80'),
('179368008', 'Artroskopi lutut diagnostik',                   'http://snomed.info/sct', '80.26'),
('443487008', 'Rekonstruksi ligamen anterior (ACL repair)',     'http://snomed.info/sct', '81.45'),
('234186004', 'Menisektomi / repair meniskus',                 'http://snomed.info/sct', '80.6'),
('61686008',  'Eksisi tumor tulang (kuretase)',                 'http://snomed.info/sct', '77.80'),
('61399006',  'Repair ruptur tendon Achilles',                 'http://snomed.info/sct', '83.64'),
('33218003',  'Operasi koreksi skoliosis dengan instrumentasi', 'http://snomed.info/sct', '81.05'),
('230690007', 'Dekompresi operatif fraktur vertebra',          'http://snomed.info/sct', '03.09'),
('179358003', 'Fusi tulang belakang lumbosakral',              'http://snomed.info/sct', '81.08'),
('11466000',  'Osteotomi koreksi deformitas tulang',           'http://snomed.info/sct', '77.30'),
('73202003',  'Biopsi tulang perkutan',                        'http://snomed.info/sct', '77.40'),
('265093000', 'Pengangkatan implan/hardware tulang',           'http://snomed.info/sct', '78.69'),
('387715001', 'Artroskopi sendi bahu',                         'http://snomed.info/sct', '80.21'),

-- ============================================================
-- BAB 85-86: OPERASI INTEGUMEN (KULIT)
-- ============================================================
('173418001', 'Mastektomi radikal',                            'http://snomed.info/sct', '85.43'),
('172043006', 'Mastektomi dengan rekonstruksi',                'http://snomed.info/sct', '85.70'),
('392021009', 'Rekonstruksi payudara dengan implan',           'http://snomed.info/sct', '85.54'),
('394553001', 'Biopsi payudara (core biopsy)',                 'http://snomed.info/sct', '85.12'),
('173396002', 'Eksisi lesi kulit',                             'http://snomed.info/sct', '86.3'),
('68134008',  'Eksisi lipoma / kista subkutan',                'http://snomed.info/sct', '86.3'),
('52752003',  'Eksisi skar / keloid',                          'http://snomed.info/sct', '86.3'),
('56136009',  'Eksisi tumor kulit luas (wide excision)',       'http://snomed.info/sct', '86.4'),
('22559005',  'Split thickness skin graft (STSG)',             'http://snomed.info/sct', '86.69'),
('52752003',  'Full thickness skin graft (FTSG)',              'http://snomed.info/sct', '86.63'),
('175114002', 'Flap pedikel / rotasi flap kulit',              'http://snomed.info/sct', '86.73'),
('265194006', 'Debridemen luka bakar',                         'http://snomed.info/sct', '86.28'),
('29680005',  'Nekrosektomi / debridemen jaringan nekrotik',   'http://snomed.info/sct', '86.22'),
('59102007',  'Eskarotomi / fasiotomi',                        'http://snomed.info/sct', '86.09'),
('173395003', 'Amputasi jari (ray amputation)',                'http://snomed.info/sct', '84.11'),
('66222009',  'Operasi hiperhidrosis',                         'http://snomed.info/sct', '86.09'),
('174000007', 'Reduksi mammaplasti / pengecilan payudara',     'http://snomed.info/sct', '85.32'),

-- ============================================================
-- BAB 87-88: PROSEDUR DIAGNOSTIK LANJUTAN
-- ============================================================
('241617002', 'CT scan pelvis',                                'http://snomed.info/sct', '88.01'),
('418690000', 'CT scan ekstremitas',                           'http://snomed.info/sct', '88.38'),
('241615005', 'MRI kepala/otak dengan kontras',                'http://snomed.info/sct', '88.91'),
('241586001', 'MRI sendi lutut',                               'http://snomed.info/sct', '88.93'),
('241593002', 'MRI sendi bahu',                                'http://snomed.info/sct', '88.93'),
('408732007', 'PET scan onkologi',                             'http://snomed.info/sct', '92.18'),
('48551003',  'Skintigrafi tulang (bone scan)',                 'http://snomed.info/sct', '92.14'),
('241489004', 'Mamografi (rontgen payudara)',                   'http://snomed.info/sct', '87.37'),
('77477000',  'CT scan thorax dengan kontras',                 'http://snomed.info/sct', '87.41'),
('168731009', 'Foto polos abdomen (BNO)',                      'http://snomed.info/sct', '87.59'),
('287022003', 'Foto polos servikal',                           'http://snomed.info/sct', '87.22'),
('182462004', 'IVP (intravenous pyelography)',                  'http://snomed.info/sct', '87.73'),
('386807006', 'USG Doppler vaskular',                          'http://snomed.info/sct', '88.77'),
('37561007',  'Fluoroskopi dengan kontras',                    'http://snomed.info/sct', '87.65'),
('413815006', 'Artrografi sendi',                              'http://snomed.info/sct', '88.32'),
('312445008', 'Biopsi CT-guided perkutan',                     'http://snomed.info/sct', '88.47'),
('413815006', 'Arteriografi / DSA (digital subtraction)',      'http://snomed.info/sct', '88.48'),
('241531003', 'Pemantauan EKG ambulatoir',                     'http://snomed.info/sct', '89.50'),
('37800007',  'Ekokardiografi transesofageal (TEE)',            'http://snomed.info/sct', '88.72'),
('43897006',  'Ekokardiografi stress / dobutamin',             'http://snomed.info/sct', '88.72'),

-- ============================================================
-- BAB 89-99: PROSEDUR DIAGNOSTIK/TERAPEUTIK LAIN
-- ============================================================
('55555000',  'Pemeriksaan bawah anestesi (EUA)',               'http://snomed.info/sct', '89.08'),
('314849005', 'Pemasangan akses vena sentral (port-a-cath)',   'http://snomed.info/sct', '38.93'),
('23857005',  'Kateterisasi arteri untuk monitoring',          'http://snomed.info/sct', '38.91'),
('233577002', 'Kardiofersi (DC cardioversion)',                 'http://snomed.info/sct', '99.62'),
('183028005', 'Terapi fibrinolisis / trombolitik IV',          'http://snomed.info/sct', '99.10'),
('415068006', 'Pemberian kemoterapi intratekal',               'http://snomed.info/sct', '99.25'),
('229553006', 'Plasmapheresis / pertukaran plasma',            'http://snomed.info/sct', '99.71'),
('182992009', 'Irigasi kandung kemih',                         'http://snomed.info/sct', '96.45'),
('182992009', 'Irigasi kolon / enema terapeutik',              'http://snomed.info/sct', '96.38'),
('228564003', 'Nutrisi parenteral total (TPN)',                 'http://snomed.info/sct', '99.15'),
('229228005', 'Terapi okupasi pasca stroke',                   'http://snomed.info/sct', '93.83'),
('229070002', 'Terapi latihan aktif / pasif',                  'http://snomed.info/sct', '93.12'),
('229461006', 'Traksi kulit / tulang (skeletal traction)',     'http://snomed.info/sct', '93.44'),
('229591000', 'Ultrasonografi terapeutik',                     'http://snomed.info/sct', '00.09'),
('13687003',  'Akupunktur medis',                              'http://snomed.info/sct', '99.92'),
('182796001', 'Pemasangan kateter suprapubik',                 'http://snomed.info/sct', '57.17'),
('230581002', 'Ablasi tumor dengan radiofrequency (RFA)',       'http://snomed.info/sct', '50.26'),
('441879006', 'Embolisasi transarterial (TACE/TAE)',           'http://snomed.info/sct', '39.79'),
('307291009', 'Drainase abses dengan perkutan / USG guided',   'http://snomed.info/sct', '54.91'),
('13231001',  'Injeksi kortikosteroid intraartikular',          'http://snomed.info/sct', '81.92'),
('18946005',  'Aspirasi cairan sendi',                         'http://snomed.info/sct', '81.91'),
('174943007', 'Pemasangan IVC filter perkutan',                'http://snomed.info/sct', '38.7'),
('265214000', 'Aspirasi / biopsi sumsum tulang iliak',         'http://snomed.info/sct', '41.31'),
('229552001', 'Terapi oksigen hiperbarik (HBO)',                'http://snomed.info/sct', '93.95');

-- ============================================================
-- Verifikasi setelah import:
-- SELECT COUNT(*) FROM mapping_prosedur_smart_klaim_bpjs;
-- SELECT m.kode_snomed, m.display, m.icd9, i.deskripsi_panjang
-- FROM mapping_prosedur_smart_klaim_bpjs m
-- LEFT JOIN icd9 i ON m.icd9 = i.kode
-- WHERE i.kode IS NULL;  -- cek kode icd9 yang tidak valid
-- ============================================================
