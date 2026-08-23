# Rencana Pengerjaan: Rombak Form Asesmen Medis IGD

Tujuan: menyamakan form asesmen medis IGD Khanza dengan formulir kertas RSUD Kota Baubau (RM EMG-02.01 s.d. 02.04).

## Prinsip yang saya pegang

1. **Aditif, tidak destruktif.** Kolom lama tidak dihapus, tidak diganti nama. Cetakan lain yang memakai kolom lama (`diagnosis`, `suhu`, `tata`, `keluhan_utama`, dll) tetap jalan.
2. **Backward compatible.** Data pasien lama tetap bisa dibuka di form baru — field kertas yang belum pernah diisi otomatis kosong, bukan error.
3. **Bertahap per blok.** Setiap fase menghasilkan aplikasi yang bisa dipakai. Kalau salah satu blok belum selesai, dokter jaga tetap bisa isi blok yang sudah jadi.
4. **Setiap fase punya rollback SQL.** Kalau di produksi bermasalah, kolom baru bisa di-drop tanpa menyentuh data lama.

## Fase pengerjaan

Urutan berdasarkan **kepentingan klinis** dan **kemudahan implementasi**.

### Fase 0 — Persiapan (½ hari)
- Backup database produksi (`mysqldump penilaian_medis_igd`)
- Eksekusi `01_migrasi_database.sql` di server staging
- Verifikasi: `SHOW COLUMNS FROM penilaian_medis_igd` → 127 baris, data lama utuh
- **Deliverable**: database staging siap; tidak ada perubahan UI dulu

### Fase 1 — Header pasien (1 hari)
Blok: dibawa oleh, rujukan, kondisi tiba, trauma + penyebab, CTAS, hamil.
- Tambah panel di atas Anamnesis di [RMPenilaianAwalMedisIGD.java](src/rekammedis/RMPenilaianAwalMedisIGD.java)
- ±15 komponen JCheckBox/JRadioButton/JComboBox
- Update SQL insert & update: kolom bertambah dari 36 → ±47
- **Deliverable**: dokter bisa cetak status masuk pasien (triase, trauma, rujukan) — sebelumnya hanya di kertas

### Fase 2 — ABCDE (3–4 hari, blok terbesar)
Blok: Airway, Breathing, Circulation, Disability, Exposure.
- Tambah 5 tab / accordion — supaya form tidak terlalu tinggi
- ±70 komponen input
- Tabel skoring GCS interaktif (pilih baris → skor otomatis) untuk field baru E/V/M
- Update SQL: kolom bertambah dari ±47 → ±117
- Perhitungan otomatis: total GCS = e+v+m; skala nyeri → validasi 0-10
- **Deliverable**: form Khanza sudah setara isi ABCDE kertas. Cetakan tetap format lama sampai fase 5.

### Fase 3 — Fahrenheid + Diagnosa + Lokalis (1 hari)
- Panel Fahrenheid (7 field)
- Field diagnosa kerja/pembanding + kolom ICD-10 (pakai search dialog `DlgCariPenyakit` yang sudah ada di project)
- Status Lokalis: awalnya field teks saja (`ket_lokalis` lama). Gambar tubuh optional (bisa ditunda ke fase terpisah)
- **Deliverable**: diagnosa lebih terstruktur, bisa filter/ekspor per ICD-10

### Fase 4 — Rencana Tindak Lanjut + Edukasi (2 hari)
- Panel besar berisi ±35 komponen
- Perlu integrasi ke fitur ranap: kalau `rtl_rawat_inap='Ya'` → tombol "Buat kamar inap" yang membuka [DlgKamarInap](src/simrskhanza/DlgKamarInap.java)
- Kalau `rtl_rujuk='Ya'` → integrasi ke modul rujukan BPJS bila ada
- **Deliverable**: keputusan akhir dokter (rawat/rujuk/pulang/meninggal) tercatat lengkap

### Fase 5 — Cetakan JRXML (2 hari)
- Bikin `rptPenilaianMedisIgd.jrxml` baru — layout 4 halaman mirip form kertas Baubau
- Simpan di folder [report/](report)
- Compile ke `.jasper` (ikuti trik di memori `khanza-build-tricks`)
- Tombol Cetak di form ganti ke laporan baru; laporan lama tetap disimpan sebagai `rptPenilaianMedisIgd_lama.jasper` untuk kompatibilitas
- **Deliverable**: cetakan dari sistem persis form kertas — tidak perlu isi ulang di kertas

### Fase 6 — Gambar tubuh interaktif (opsional, 2 hari)
- Custom JPanel dengan gambar tubuh sebagai background
- Klik menandai titik → simpan sebagai list koordinat + tipe luka
- Render ke PNG saat simpan → `lokalis_gambar` BLOB
- **Deliverable**: gambar tubuh di cetakan berisi tanda dari dokter, bukan kosong

## Estimasi total
| Fase | Waktu |
|---|---|
| 0. Persiapan | 0,5 hari |
| 1. Header pasien | 1 hari |
| 2. ABCDE | 3–4 hari |
| 3. Fahrenheid+Diagnosa | 1 hari |
| 4. Rencana Tindak Lanjut + Edukasi | 2 hari |
| 5. Cetakan JRXML | 2 hari |
| 6. Gambar tubuh (opsional) | 2 hari |
| **Total** | **9,5–12,5 hari kerja** |

Kerugian: form akan besar (perkiraan 5.500 baris). Untuk mengurangi risiko, tiap fase saya commit terpisah.

## Risiko & mitigasi

| Risiko | Mitigasi |
|---|---|
| Form terlalu panjang, dokter jaga malas mengisi | Pakai default value & auto-fill; field wajib hanya yang critical (ABCDE + Diagnosa + RTL); sisanya opsional |
| SQL migrasi gagal di produksi | Skrip aditif — bisa di-rollback dengan `DROP COLUMN` per kolom; backup wajib sebelum eksekusi |
| Cetakan lama tetap dipakai unit lain | Kolom lama (`diagnosis`, `suhu`, `tata`) tidak dihapus; disinkron ganda saat simpan |
| Data pasien lama pecah kalau enum diperluas | Migrasi ini tidak mengubah enum lama, hanya menambah kolom baru |
| Fase 2 (ABCDE) makan waktu paling lama | Bisa dipecah lagi: Fase 2a Airway+Breathing, 2b Circulation, 2c Disability+Exposure |

## Hal yang belum diputuskan (butuh masukan Anda)

1. **Blok pertama yang mau saya kerjakan** — mengikuti urutan Fase 1→6, atau prioritas berbeda?
2. **Diagnosa multi baris** — pakai tabel `penilaian_medis_igd_diagnosa` atau cukup 1 kolom teks saja per jenis?
3. **Gambar tubuh (Fase 6)** — dikerjakan atau di-skip? Kalau di-skip, status lokalis cukup field teks bebas.
4. **Kolom akses (`akses.getpenilaian_awal_medis_igd()`)** — mau tambah akses baru untuk edukasi/RTL, atau pakai akses yang sama?
5. **Data pasien lama** — apakah perlu backfill (misalnya kolom `trauma` default 'Non Trauma') atau biarkan NULL?

## File dalam folder ini

- `01_migrasi_database.sql` — skrip DDL siap eksekusi (aditif, backward compatible)
- `02_peta_field.md` — mapping tiap field kertas → kolom database
- `03_rencana_pengerjaan.md` — dokumen ini
