# Peta Field: Form Kertas RSUD Baubau (RM EMG-02) → Kolom Database

Tabel target: `penilaian_medis_igd` (36 kolom lama + 91 kolom baru + 1 tabel opsional).
Legenda: **lama** = kolom yang sudah ada (dipakai ulang). **BARU** = kolom hasil migrasi (lihat `01_migrasi_database.sql`).

---

## Halaman 1 — RM EMG-02.01

### Identitas pasien (diambil otomatis dari `reg_periksa` / `pasien`)
| Field kertas | Sumber di DB |
|---|---|
| Nama, No.RM, Tanggal Masuk, Jam Masuk | `reg_periksa` + `pasien` |
| Lk/Pr, Alamat, Tempat/Tgl Lahir, Umur | `pasien` |
| Agama, Pekerjaan, Status Pernikahan | `pasien` |
| Keluarga yang dihubungi + alamat | `pasien.namakeluarga`, `pasien.alamatpj`, dst. |
| Pembiayaan | `reg_periksa.png_jawab`, `bridging_sep` |

*Tidak perlu kolom baru untuk blok ini.*

### Kondisi masuk
| Field kertas | Kolom DB |
|---|---|
| Masuk RS dibawa oleh | **BARU** `dibawa_oleh` + `dibawa_oleh_ket` |
| Rujukan (Tidak/Ya) + asal + keterangan | **BARU** `rujukan`, `rujukan_dari`, `rujukan_ket` |
| Kondisi pasien saat tiba di RS | **BARU** `kondisi_tiba` |
| Trauma / Non Trauma + penyebab (9 pilihan) | **BARU** `trauma`, `penyebab_trauma` |
| Hasil CTAS | **BARU** `hasil_ctas` |
| Hamil (Tidak/Ya) | **BARU** `hamil` |
| Riwayat Alergi Obat + Nama Obat | **lama** `alergi` |

### Anamnesis
| Field kertas | Kolom DB |
|---|---|
| Anamnesis (auto/allo) | **lama** `anamnesis`, `hubungan` |
| Keluhan Utama | **lama** `keluhan_utama` |
| Riwayat Penyakit Sekarang | **lama** `rps` |
| Riwayat Penyakit Dahulu | **lama** `rpd` |
| Riwayat Penyakit Keluarga | **lama** `rpk` |
| Obat-obat yang sedang dikonsumsi | **lama** `rpo` |

### AIRWAY
| Field kertas | Kolom DB |
|---|---|
| Bebas / Tidak Bebas | **BARU** `airway` |
| Palatum mole jatuh / Sputum / Benda Asing / Darah / Spasme | **BARU** `airway_obstruksi` (SET) |
| Suara Nafas (Normal / Stridor / Tidak ada) | **BARU** `airway_suara_nafas` |
| Lain-lain | **BARU** `airway_lain` |

---

## Halaman 2 — RM EMG-02.02

### BREATHING
| Field kertas | Kolom DB |
|---|---|
| Pola nafas | **BARU** `breathing_pola` (SET: Apnoe/Bradipnoe/Orthopnoe/Dyspnoe/Takipnoe) |
| Frekuensi nafas | **lama** `rr` |
| SaO2 | **lama** `spo` (atau **BARU** `breathing_sao2` — kalau mau dipisah) |
| Bunyi nafas | **BARU** `breathing_bunyi` (SET) |
| Irama | **BARU** `breathing_irama` |
| Penggunaan otot bantu | **BARU** `breathing_otot_bantu` (SET) |
| Jalan pernafasan | **BARU** `breathing_jalan` |

### CIRCULATION
| Field kertas | Kolom DB |
|---|---|
| Akral (Hangat/Dingin) | **BARU** `circ_akral` |
| Pucat (Ya/Tidak) | **BARU** `circ_pucat` |
| Cianosis (Ya/Tidak) | **BARU** `circ_cianosis` |
| Pengisian kapiler | **BARU** `circ_kapiler` |
| Nadi Teraba/Tidak + Frekuensi | **BARU** `circ_nadi_teraba` + **lama** `nadi` |
| Irama (Reguler/Irreguler) | **BARU** `circ_irama` |
| Kekuatan (Kuat/Lemah) | **BARU** `circ_kekuatan` |
| Tekanan darah | **lama** `td` |
| Riwayat kehilangan cairan | **BARU** `circ_kehilangan_cairan` (SET) |
| Perdarahan Ya/Tidak + lokasi | **BARU** `circ_perdarahan`, `circ_perdarahan_lokasi` |
| Kelembaban kulit | **BARU** `circ_kelembaban` |
| Turgor | **BARU** `circ_turgor` |
| Edema (Ya/Tidak) | **BARU** `circ_edema` |
| Output Urine ml/jam | **BARU** `circ_output_urine` |
| Luas & Grade luka bakar | **BARU** `circ_luka_bakar_luas`, `circ_luka_bakar_grade` |

### DISABILITY
| Field kertas | Kolom DB |
|---|---|
| GCS Dewasa/Anak | **BARU** `gcs_kategori` |
| E / V / M | **BARU** `gcs_e`, `gcs_v`, `gcs_m` (total auto → **lama** `gcs`) |
| Kesadaran | **lama** `kesadaran` (perlu tambah opsi ke enum: sudah lengkap) |
| Pupil (Normal/Tidak) | **BARU** `pupil_bentuk` |
| Respon Cahaya (+ / -) | **BARU** `pupil_respon_cahaya` |
| Ukuran Pupil (Isokor/Anisokor) | **BARU** `pupil_ukuran` |
| Diameter (1/2/3/4 mm) — di kertas satu kolom, di sini dipisah ka/ki | **BARU** `pupil_diameter_ka`, `pupil_diameter_ki` |
| Ekstremitas Sensorik/Motorik | **BARU** `ekstremitas_sensorik`, `ekstremitas_motorik` |
| Kekuatan Otot | **BARU** `kekuatan_otot` |

### EXPOSURE
| Field kertas | Kolom DB |
|---|---|
| Deformitas / Contusion / Abrasi / Penetrasi / Laserasi / Edema + Daerah | **BARU** `expo_*` (12 kolom, 6 pasang Ya/Tidak + daerah) |
| Ukuran Luka, Kedalaman Luka | **BARU** `expo_ukuran_luka`, `expo_kedalaman_luka` |
| Keluhan Nyeri (Ya/Tidak) | **BARU** `nyeri` |
| Skala Nyeri (Skor 0-10) | **BARU** `nyeri_skala` |
| Pengkajian Nyeri P/Q/R/S/T | **BARU** `nyeri_p`, `nyeri_q`, `nyeri_r`, `nyeri_s`, `nyeri_t` |

---

## Halaman 3 — RM EMG-02.03

### FAHRENHEID
| Field kertas | Kolom DB |
|---|---|
| Suhu | **lama** `suhu` (+ **BARU** `fahr_suhu` bila mau dipisah, tapi biasanya sama) |
| Lamanya terpapar suhu panas/dingin (jam) | **BARU** `fahr_lama_terpapar`, `fahr_terpapar_jenis` |
| Riwayat Penyakit (Metabolik / Kehilangan cairan / Penyakit SSP) | **BARU** `fahr_riwayat_penyakit` (SET) |
| Riwayat (Cedera kepala/Iatrogenic/Infus dingin/Transfusi dingin/Hipoglikemia) | **BARU** `fahr_riwayat` (SET) |

### Status Lokalis (gambar tubuh)
| Field kertas | Kolom DB |
|---|---|
| Coretan pada gambar tubuh depan/belakang/kanan/kiri | **BARU** `lokalis_gambar` (blob PNG hasil canvas Java) |
| Catatan tulis | **lama** `ket_lokalis` |

*Alternatif ringan: form Khanza menampilkan gambar template + JLabel `Klik untuk tandai`. Kalau tidak dipakai, biarkan `NULL` — pemeriksaan tetap ditulis di `ket_lokalis`.*

### Pemeriksaan Penunjang
| Field kertas | Kolom DB |
|---|---|
| Pemeriksaan Penunjang (satu blok teks) | **lama** `ekg` + **lama** `rad` + **lama** `lab` (3 field terpisah di Khanza; di cetakan digabung) |

### Diagnosa & Tatalaksana
| Field kertas | Kolom DB |
|---|---|
| Diagnosa kerja + ICD-10 | **BARU** `diagnosa_kerja`, `diagnosa_kerja_icd10` |
| Diagnosa pembanding + ICD-10 | **BARU** `diagnosa_banding`, `diagnosa_banding_icd10` |
| Penatalaksanaan | **lama** `tata` |

Kalau mau multi-diagnosa (kertas hanya sediakan 1 baris tapi realitanya sering >1): pakai tabel opsional `penilaian_medis_igd_diagnosa`.

---

## Halaman 4 — RM EMG-02.04

### Rencana Tindak Lanjut
| Field kertas | Kolom DB |
|---|---|
| Pengantar Pasien (Ada/Tidak ada) | **BARU** `pengantar_pasien` |
| Rawat Inap + Ruang + Kelas + Indikasi + DPJP | **BARU** `rtl_rawat_inap`, `rtl_ranap_ruang`, `rtl_ranap_kelas`, `rtl_ranap_indikasi`, `rtl_dpjp` |
| Tindak lanjut di Kamar Operasi/Bersalin | **BARU** `rtl_tindak_lanjut_di` (SET) |
| Rujuk Ke (RS/Puskesmas/Dokter/Dokter Keluarga/Home Care) + tujuan | **BARU** `rtl_rujuk`, `rtl_rujuk_ke`, `rtl_rujuk_tujuan_nama` |
| Alasan dirujuk (Klinikal/Non/Permintaan/Fasilitas) | **BARU** `rtl_rujuk_alasan` (SET) |
| Dipulangkan + Kontrol Poliklinik + Tanggal | **BARU** `rtl_dipulangkan`, `rtl_kontrol_poli`, `rtl_kontrol_tanggal` |
| Meninggal Dunia + Tanggal + Pukul + (Permintaan sendiri / DOA) | **BARU** `rtl_meninggal`, `rtl_meninggal_tanggal`, `rtl_meninggal_jenis` |
| Pulang atas permintaan sendiri/menolak perawatan | **BARU** `rtl_pulang_aps` |
| Melarikan diri | **BARU** `rtl_melarikan_diri` |
| Transportasi pulang (Ambulans/Pribadi/Jenazah) | **BARU** `rtl_transportasi` |

### Edukasi Pasien
| Field kertas | Kolom DB |
|---|---|
| Pasien / Keluarga Pasien | **BARU** `edu_penerima` |
| Nama + Hubungan + Alamat + Kel + Kec + Kab/Kota + No.Telp | **BARU** `edu_nama`, `edu_hubungan`, `edu_alamat`, `edu_kel`, `edu_kec`, `edu_kab_kota`, `edu_no_telp` |
| "Tidak dapat memberi edukasi karena..." + 10 hambatan + Lain-lain | **BARU** `edu_tidak_bisa`, `edu_hambatan` (SET), `edu_hambatan_lain` |
| Pendidikan kesehatan pasien pulang (4 pilihan + Lain-lain) | **BARU** `edu_pulang` (SET), `edu_pulang_lain` |

### Tanda tangan
| Field kertas | Kolom DB |
|---|---|
| Tanggal & Jam pemeriksaan | **lama** `tanggal` |
| Nama & TTD Dokter Jaga | **lama** `kd_dokter` (join ke `dokter`) — TTD dari `dokter.tanda_tangan` |

---

## Ringkasan angka

- Kolom lama dipakai ulang: **21**
- Kolom lama tidak berperan langsung (tapi dibiarkan agar cetakan lama tetap jalan): **15**
- Kolom baru: **91**
- Tabel baru opsional: **1** (`penilaian_medis_igd_diagnosa`)
- **Total kolom setelah migrasi: 127**
