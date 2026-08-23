# Rencana: Menu Khusus Tagihan Operasi Cathlab

Permintaan Cathlab: form khusus tagihan operasi cathlab yang punya field
scrub nurse (2 slot), circulate nurse (2 slot), radiographer (1 slot),
plus field spesifik cathlab (jenis prosedur, nomor kateter, dosis radiasi).

## Keputusan yang sudah diambil

- **Pendekatan**: menu + tabel terpisah `operasi_cathlab` (bukan modif `operasi`)
- **Rasional**: form `DlgTagihanOperasi` existing tidak tersentuh → zero risk memutus fungsi save operasi
- **Petugas cathlab**: 2 Scrub Nurse + 2 Circulate Nurse + 1 Radiographer
- **Field khusus cathlab**:
  - `jenis_prosedur` enum (Angiografi, PCI, TAVI, Ablasi, Pacemaker, Ballooning, IABP, Peripheral, Lainnya)
  - `nomor_kateter` varchar
  - `alat_khusus` text
  - Safety radiasi: `dosis_radiasi_mgy`, `waktu_fluoroskopi_min`, `kontras_ml`

## Deliverables

### 1. Database
Skrip: [05_tambah_petugas_cathlab.sql](05_tambah_petugas_cathlab.sql)
- `CREATE TABLE operasi_cathlab` (baru, ±40 kolom)
- `ALTER TABLE paket_operasi ADD 5 kolom biaya cathlab`
- **Tabel `operasi` tidak disentuh sama sekali**

### 2. Form Java baru
File: `src/simrskhanza/DlgTagihanOperasiCathlab.java` (baru)
- Struktur mirip `DlgTagihanOperasi.java` tapi disederhanakan untuk cathlab
- Section field:
  1. **Identitas**: No.Rawat + Nama Pasien (auto-fill) + Kategori + Jenis Anestesi + Tgl mulai + Tgl selesai
  2. **Prosedur Cathlab**: Jenis Prosedur (combo enum), Nomor Kateter, Alat Khusus, Diagnosa Pre/Post, Laporan Operasi
  3. **Safety Radiasi**: Dosis (mGy), Waktu Fluoro (menit), Kontras (ml)
  4. **Tim Dokter**: Operator 1-3 (dokter), Dokter Anestesi
  5. **Tim Cathlab**: Scrub Nurse 1-2, Circulate Nurse 1-2, Radiographer
  6. **Tabel Tindakan**: pilih dari paket_operasi (auto-fill tarif dari master)
  7. **Total Biaya + tombol Simpan/Edit/Hapus/Cetak**

### 3. Menu di frmUtama
Tambah tombol "Tagihan Operasi Cathlab" di frmUtama.java
- Import class baru
- Deklarasi field tombol
- Handler action
- Inisialisasi di initKhanza2() (ingat batas 64KB initKhanza)
- 3 titik conditional visibility (kategori-menu, semua-menu, pencarian)

Permission: reuse `akses.getoperasi()` atau tambah `akses.getoperasi_cathlab()` (opsional).

### 4. Cetakan jrxml (opsional)
`report/rptTagihanOperasiCathlab.jrxml` — mirror struktur `rptTagihanOperasi.jasper` tapi dengan
field cathlab (jenis prosedur, dosis radiasi, dsb). Bisa dikerjakan di fase terpisah.

### 5. Master Paket Operasi (opsional)
Kalau mau bisa set tarif Scrub/Circulate/Radiographer per paket via UI, form master paket operasi
perlu diupdate. Kalau tidak, admin set tarif via SQL manual atau default 0.

## Fase pengerjaan (bertahap, aman)

| Fase | Deliverable | Estimasi | Risiko |
|---|---|---|---|
| **0** Persiapan | Backup DB, jalankan SQL di staging | 15 mnt | Rendah — hanya CREATE TABLE aditif |
| **1** Form Java baru | `DlgTagihanOperasiCathlab.java` skeleton + save/edit ke `operasi_cathlab` | 2 jam | Sedang — file baru, tidak ganggu existing |
| **2** Integrasi menu | Patch frmUtama.java (import, tombol, handler, 3 titik menu) | 30 mnt | Rendah — ikuti pola LaporanTahunanIRNA |
| **3** Field khusus cathlab | Jenis prosedur combo, dosis radiasi, kateter, alat khusus di form | 1 jam | Rendah |
| **4** Tabel tindakan + auto-fill tarif dari paket_operasi | Tabel di form yang load tarif petugas cathlab | 1,5 jam | Sedang |
| **5** Cetakan jrxml (opsional) | Layout tagihan cathlab | 1,5 jam | Rendah |
| **6** Master Paket update (opsional) | Field tarif cathlab di form master paket | 1 jam | Rendah |
| **Total minimum** (fase 0–4, tanpa opsional) | | **±5 jam** | |

## Yang perlu jawaban Anda

1. **Fase yang mau saya kerjakan sekarang?**
   - Fase 0 saja (SQL) — biar dulu di-review sebelum eksekusi Java, atau
   - Fase 0–4 sekaligus (form fungsional siap pakai) — 5 jam kerja, atau
   - Fase 0–6 penuh — 8 jam kerja
2. **Permission**: pakai `akses.getoperasi()` yang sudah ada, atau tambah `akses.getoperasi_cathlab()` baru?
3. **Perhitungan jasa cathlab**: total biaya cathlab (jasa petugas + alat + kontras) masuk ke:
   - Kasir Ranap/Ralan seperti operasi biasa (via billing_pasien atau serupa), atau
   - Tabel biaya terpisah?

## File terkait yang sudah dibuat

- [05_tambah_petugas_cathlab.sql](05_tambah_petugas_cathlab.sql) — DDL siap eksekusi
- [06_rencana_petugas_cathlab.md](06_rencana_petugas_cathlab.md) — dokumen ini
