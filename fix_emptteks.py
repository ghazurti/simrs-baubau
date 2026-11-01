import re

with open("src/rekammedis/RMAsesmenInvasifNonBedah.java", "r") as f:
    content = f.read()

emptTeks_code = """
    private void emptTeks() {
        JamTiba.setText("");
        LokasiPungsi.setText("");
        JumlahPungsi.setText("");
        TerpasangSheath.setSelectedIndex(0);
        LokasiSheath.setText("");
        PulsasiRadialisKanan.setText("");
        PulsasiRadialisKiri.setText("");
        PulsasiDorsalisKanan.setText("");
        PulsasiDorsalisKiri.setText("");
        AlatTerpasang.setText("");
        JenisAnestesi.setText("");
        Sedasi.setText("");
        DosisPre.setText("");
        DosisIntra.setText("");
        DosisPost.setText("");
        Komplikasi.setText("");
        Intake.setText("");
        Output.setText("");
        InstruksiPaska.setText("");
        MasalahKeperawatan.setText("");
        RencanaKeperawatan.setText("");
        KeputusanRawat.setSelectedIndex(0);
        
        Tanggal.setDate(new java.util.Date());
    }
"""
content = re.sub(r'private void emptTeks\(\) \{.*?\n    \}', emptTeks_code, content, flags=re.DOTALL)

with open("src/rekammedis/RMAsesmenInvasifNonBedah.java", "w") as f:
    f.write(content)

