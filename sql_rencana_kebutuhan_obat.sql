-- =====================================================================
-- Rencana Kebutuhan Obat & BMHP Gudang Farmasi - RSUD Kota Baubau
-- =====================================================================

-- setting parameter per item (kolom NULL = pakai nilai otomatis)
--   kelompok  NULL -> dari golongan/kategori barang
--   kelipatan NULL -> dari rencana_kebutuhan_obat_kelipatan_satuan, lalu 1
CREATE TABLE IF NOT EXISTS rencana_kebutuhan_obat_setting (
  kode_brng      VARCHAR(15) NOT NULL,
  nama_generik   VARCHAR(200) NULL,
  kelompok       VARCHAR(40)  NULL,
  kelipatan      DOUBLE NULL,
  ven            ENUM('V','E','N','K/E','-') NOT NULL DEFAULT '-',
  keterangan     VARCHAR(200) NULL,
  aktif          ENUM('Ya','Tidak') NOT NULL DEFAULT 'Ya',
  PRIMARY KEY (kode_brng),
  CONSTRAINT fk_rko_brng FOREIGN KEY (kode_brng) REFERENCES databarang (kode_brng) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- default kelipatan pengadaan per satuan
CREATE TABLE IF NOT EXISTS rencana_kebutuhan_obat_kelipatan_satuan (
  kode_sat  CHAR(4) NOT NULL,
  kelipatan DOUBLE NOT NULL DEFAULT 1,
  PRIMARY KEY (kode_sat),
  CONSTRAINT fk_rko_sat FOREIGN KEY (kode_sat) REFERENCES kodesatuan (kode_sat) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- seed kelipatan (pola Excel konfirmasi farmasi); satuan yang tidak ada di DB dilewati
INSERT IGNORE INTO rencana_kebutuhan_obat_kelipatan_satuan (kode_sat,kelipatan)
SELECT k.kode_sat, s.kelipatan FROM kodesatuan k INNER JOIN (
  SELECT 'TAB' kode_sat,100 kelipatan UNION ALL SELECT 'AMP',10 UNION ALL SELECT 'AMPL',10
  UNION ALL SELECT 'VIAL',10 UNION ALL SELECT 'S38',10 UNION ALL SELECT 'BTL',5
  UNION ALL SELECT 'TUB',12 UNION ALL SELECT 'SACH',12 UNION ALL SELECT 'LBR',100
  UNION ALL SELECT 'PSG',50 UNION ALL SELECT 'STRP',20 UNION ALL SELECT 'RL',12
  UNION ALL SELECT 'PEN',5 UNION ALL SELECT 'SUPP',10 UNION ALL SELECT 'CAPS',100
  UNION ALL SELECT 'PCS',1
) s ON s.kode_sat=k.kode_sat;

-- header snapshot rencana
CREATE TABLE IF NOT EXISTS rencana_kebutuhan_obat (
  no_rencana          VARCHAR(20) NOT NULL,          -- RKO/YYYY/MM/xxx
  tgl_rencana         DATE NOT NULL,
  periode_rencana     VARCHAR(50) NULL,              -- "OKTOBER - DESEMBER 2026"
  tgl_awal_pemakaian  DATE NOT NULL,
  tgl_akhir_pemakaian DATE NOT NULL,
  jml_bulan           INT NOT NULL,
  bulan_stok_kerja    DOUBLE NOT NULL DEFAULT 4,
  persen_buffer       DOUBLE NOT NULL DEFAULT 20,
  lead_time_hari      INT NOT NULL DEFAULT 60,
  kd_bangsal_stok     VARCHAR(5) NULL,               -- NULL = semua depo
  nip                 VARCHAR(20) NULL,              -- petugas penyusun
  catatan             VARCHAR(300) NULL,             -- footer note cetakan
  harga_dari          ENUM('Master','Penerimaan Terakhir') NOT NULL DEFAULT 'Master',
  sumber_pemakaian    ENUM('Transaksi','Riwayat Barang') NOT NULL DEFAULT 'Transaksi',
  ttd_kota            VARCHAR(50) NULL,
  ttd_tanggal         DATE NULL,
  kepala_instalasi_nama VARCHAR(100) NULL, kepala_instalasi_nip VARCHAR(30) NULL,
  pembuat_nama          VARCHAR(100) NULL, pembuat_nip          VARCHAR(30) NULL,
  direktur_nama         VARCHAR(100) NULL, direktur_nip         VARCHAR(30) NULL,
  kabid_nama            VARCHAR(100) NULL, kabid_nip            VARCHAR(30) NULL,
  status              ENUM('Draft','Final') NOT NULL DEFAULT 'Draft',
  PRIMARY KEY (no_rencana)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS rencana_kebutuhan_obat_detail (
  no_rencana        VARCHAR(20) NOT NULL,
  kode_brng         VARCHAR(15) NOT NULL,
  kelompok          VARCHAR(40) NOT NULL,
  sisa_stok         DOUBLE NOT NULL DEFAULT 0,
  rata_rata         DOUBLE NOT NULL DEFAULT 0,
  stok_kerja        DOUBLE NOT NULL DEFAULT 0,
  buffer            DOUBLE NOT NULL DEFAULT 0,
  lead_time         DOUBLE NOT NULL DEFAULT 0,
  rencana_kebutuhan DOUBLE NOT NULL DEFAULT 0,
  rencana_pengadaan DOUBLE NOT NULL DEFAULT 0,
  harga             DOUBLE NOT NULL DEFAULT 0,
  total             DOUBLE NOT NULL DEFAULT 0,
  persen_item       DOUBLE NOT NULL DEFAULT 0,
  persen_kumulatif  DOUBLE NOT NULL DEFAULT 0,
  pareto            CHAR(1) NULL,
  ven               VARCHAR(5) NULL,
  keterangan        VARCHAR(200) NULL,
  PRIMARY KEY (no_rencana,kode_brng),
  KEY kode_brng (kode_brng),
  CONSTRAINT fk_rkod_rencana FOREIGN KEY (no_rencana) REFERENCES rencana_kebutuhan_obat (no_rencana) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_rkod_brng FOREIGN KEY (kode_brng) REFERENCES databarang (kode_brng) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS rencana_kebutuhan_obat_usulan_baru (
  no_rencana  VARCHAR(20) NOT NULL,
  no_urut     INT NOT NULL,
  kelompok    VARCHAR(40) NOT NULL DEFAULT 'OBAT',
  nama_barang VARCHAR(200) NOT NULL,
  satuan      VARCHAR(30) NULL,
  jumlah      DOUBLE NOT NULL DEFAULT 0,
  harga       DOUBLE NOT NULL DEFAULT 0,
  keterangan  VARCHAR(200) NULL,                   -- dokter/ruangan peminta
  PRIMARY KEY (no_rencana,no_urut),
  CONSTRAINT fk_rkou_rencana FOREIGN KEY (no_rencana) REFERENCES rencana_kebutuhan_obat (no_rencana) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- hak akses (abaikan error "Duplicate column" bila sudah pernah dijalankan)
ALTER TABLE user ADD COLUMN rencana_kebutuhan_obat ENUM('true','false') NOT NULL DEFAULT 'false';
ALTER TABLE user ADD COLUMN setting_rencana_kebutuhan_obat ENUM('true','false') NOT NULL DEFAULT 'false';
