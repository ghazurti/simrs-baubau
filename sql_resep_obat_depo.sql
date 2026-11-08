-- ============================================================
-- Depo asal resep (jalankan di database sik)
-- Resep ranap tidak lagi jadi resep OK hanya karena ada jadwal operasi di tanggal itu.
--   - resep dari Jadwal Operasi -> Obat/BHP      : dicatat dengan kd_ruang_ok + depo ruang OK
--   - resep dari komputer ber-DEPOAKTIFOBAT lain  : dicatat dengan depo komputer itu
--   - resep dari ruangan                          : tidak dicatat (= depo ruangan pasien)
-- Tabel juga dibuat otomatis oleh aplikasi saat form resep dibuka.
-- ============================================================

create table if not exists resep_obat_depo (
  no_resep varchar(14) not null,
  kd_depo char(5) not null,
  kd_ruang_ok varchar(3) not null default '',
  primary key (no_resep)
) engine=InnoDB default charset=latin1;

-- ------------------------------------------------------------
-- OPSIONAL, sekali jalan saat jar baru dipasang:
-- resep ranap yang BELUM tervalidasi dan dibuat selama operasi berlangsung
-- (status Proses Operasi, atau jam resep di antara jam mulai & selesai)
-- ditandai sebagai resep OK supaya tetap dilayani dari depo OK.
-- ------------------------------------------------------------
insert ignore into resep_obat_depo (no_resep,kd_depo,kd_ruang_ok)
select r.no_resep, s.kd_depo, b.kd_ruang_ok
from resep_obat r
inner join booking_operasi b on b.no_rawat=r.no_rawat and b.tanggal=r.tgl_peresepan
inner join set_depo_ruang_ok s on s.kd_ruang_ok=b.kd_ruang_ok
where r.status='ranap' and r.tgl_perawatan='0000-00-00'
  and ((b.status='Proses Operasi' and r.jam_peresepan>=b.jam_mulai)
    or (b.status='Selesai' and r.jam_peresepan between b.jam_mulai and b.jam_selesai));

-- ------------------------------------------------------------
-- PERBAIKAN (bila jar versi awal sudah terpasang):
-- resep pasien ranap dari komputer IGD ikut tercatat Depo UGD dan "kembali ke IGD".
-- Catatan dari depo bersama (Depo UGD dan depo yang dipakai poli/ruangan) dihapus;
-- resep itu kembali ke ruangan & depo ruangan. Resep dari Jadwal Operasi tidak disentuh.
-- ------------------------------------------------------------
delete from resep_obat_depo
where kd_ruang_ok=''
  and (kd_depo in (select kd_bangsal from set_depo_ralan)
    or kd_depo in (select kd_depo from set_depo_ranap));
