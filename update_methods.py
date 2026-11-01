import re
import sys

with open("src/rekammedis/RMAsesmenInvasifNonBedah.java", "r") as f:
    content = f.read()

# Replace simpan() body
simpan_code = """
    private void simpan() {
        if(Sequel.menyimpantf("asesmen_keperawatan_invasif_non_bedah","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","Data",25,new String[]{
            TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem(),
            JamTiba.getText(),LokasiPungsi.getText(),JumlahPungsi.getText(),TerpasangSheath.getSelectedItem().toString(),LokasiSheath.getText(),
            PulsasiRadialisKanan.getText(),PulsasiRadialisKiri.getText(),PulsasiDorsalisKanan.getText(),PulsasiDorsalisKiri.getText(),
            AlatTerpasang.getText(),JenisAnestesi.getText(),Sedasi.getText(),DosisPre.getText(),DosisIntra.getText(),DosisPost.getText(),
            Komplikasi.getText(),Intake.getText(),Output.getText(),InstruksiPaska.getText(),MasalahKeperawatan.getText(),
            RencanaKeperawatan.getText(),KeputusanRawat.getSelectedItem().toString(),KdPetugas.getText()
        })==true){
            tampil();
            emptTeks();
        }
    }
"""
content = re.sub(r'private void simpan\(\) \{.*?\n    \}', simpan_code, content, flags=re.DOTALL)

# Replace ganti() body
ganti_code = """
    private void ganti() {
        if(Sequel.mengubahtitip("asesmen_keperawatan_invasif_non_bedah","no_rawat=? and tanggal=?","jam_tiba_rr=?,lokasi_pungsi=?,jumlah_pungsi=?,terpasang_sheath=?,lokasi_sheath=?,pulsasi_radialis_kanan=?,pulsasi_radialis_kiri=?,pulsasi_dorsalis_kanan=?,pulsasi_dorsalis_kiri=?,alat_terpasang=?,jenis_anestesi=?,sedasi=?,antikoagulan_dosis_pre=?,antikoagulan_dosis_intra=?,antikoagulan_dosis_post=?,komplikasi=?,balance_cairan_intake=?,balance_cairan_output=?,instruksi_paska_tindakan=?,masalah_keperawatan=?,rencana_keperawatan=?,keputusan_rawat=?,nip=?",25,new String[]{
            JamTiba.getText(),LokasiPungsi.getText(),JumlahPungsi.getText(),TerpasangSheath.getSelectedItem().toString(),LokasiSheath.getText(),
            PulsasiRadialisKanan.getText(),PulsasiRadialisKiri.getText(),PulsasiDorsalisKanan.getText(),PulsasiDorsalisKiri.getText(),
            AlatTerpasang.getText(),JenisAnestesi.getText(),Sedasi.getText(),DosisPre.getText(),DosisIntra.getText(),DosisPost.getText(),
            Komplikasi.getText(),Intake.getText(),Output.getText(),InstruksiPaska.getText(),MasalahKeperawatan.getText(),
            RencanaKeperawatan.getText(),KeputusanRawat.getSelectedItem().toString(),KdPetugas.getText(),
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),7).toString()
        })){
            tampil();
            emptTeks();
        }
    }
"""
content = re.sub(r'private void ganti\(\) \{.*?\n    \}', ganti_code, content, flags=re.DOTALL)

with open("src/rekammedis/RMAsesmenInvasifNonBedah.java", "w") as f:
    f.write(content)
print("Updated Java methods.")
