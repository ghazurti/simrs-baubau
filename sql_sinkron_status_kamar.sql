-- Sinkron status kamar dengan pasien aktif (kamar_inap.stts_pulang='-')
-- Jalankan manual di server SETELAH dicek; hanya mengubah kolom kamar.status.

-- 1. CEK DULU: kamar KOSONG padahal ada pasien aktif
select k.kd_kamar, b.nm_bangsal, ki.no_rawat, p.nm_pasien, ki.tgl_masuk
from kamar k
join bangsal b on k.kd_bangsal=b.kd_bangsal
join kamar_inap ki on ki.kd_kamar=k.kd_kamar and ki.stts_pulang='-'
join reg_periksa rp on ki.no_rawat=rp.no_rawat
join pasien p on rp.no_rkm_medis=p.no_rkm_medis
where k.status='KOSONG' and k.statusdata='1'
order by k.kd_kamar;

-- 2. PERBAIKAN: kamar yang ada pasien aktif -> ISI
update kamar k set k.status='ISI'
where k.status='KOSONG'
and exists(select 1 from kamar_inap ki where ki.kd_kamar=k.kd_kamar and ki.stts_pulang='-');

-- 3. CEK: kamar ISI tanpa pasien aktif (perbaiki manual setelah dicek ruangan,
--    bisa jadi kamar memang sedang dibersihkan/dibooking)
select k.kd_kamar, b.nm_bangsal from kamar k join bangsal b on k.kd_bangsal=b.kd_bangsal
where k.status='ISI' and k.statusdata='1'
and not exists(select 1 from kamar_inap ki where ki.kd_kamar=k.kd_kamar and ki.stts_pulang='-');

-- 4. CEK: satu kode kamar dipakai >1 pasien aktif / pasien aktif sangat lama
--    (ruangan perlu memulangkan atau memindahkan manual)
select ki.kd_kamar, ki.no_rawat, p.nm_pasien, ki.tgl_masuk, datediff(curdate(),ki.tgl_masuk) hari
from kamar_inap ki join reg_periksa rp on ki.no_rawat=rp.no_rawat join pasien p on rp.no_rkm_medis=p.no_rkm_medis
where ki.stts_pulang='-'
and (ki.kd_kamar in (select kd_kamar from kamar_inap where stts_pulang='-' group by kd_kamar having count(*)>1)
     or ki.tgl_masuk < curdate() - interval 30 day)
order by ki.kd_kamar, ki.tgl_masuk;
