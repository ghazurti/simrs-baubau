-- Surat keterangan format RSUD Kota Baubau
-- Isian istirahat pada Surat Keterangan Opname/Perawatan (tabel pendamping surat_keterangan_rawat_inap)
CREATE TABLE IF NOT EXISTS surat_keterangan_rawat_inap_istirahat (
  no_surat varchar(17) NOT NULL,
  lama varchar(5) NOT NULL,
  tgl_awal date NOT NULL,
  tgl_akhir date NOT NULL,
  PRIMARY KEY (no_surat),
  CONSTRAINT surat_keterangan_rawat_inap_istirahat_ibfk_1 FOREIGN KEY (no_surat)
    REFERENCES surat_keterangan_rawat_inap (no_surat) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
