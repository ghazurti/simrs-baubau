import re

with open("src/rekammedis/RMAsesmenInvasifNonBedah.java", "r") as f:
    content = f.read()

var_decls = """
    private widget.TextBox JamTiba;
    private widget.TextBox LokasiPungsi;
    private widget.TextBox JumlahPungsi;
    private widget.TextBox LokasiSheath;
    private widget.TextBox PulsasiRadialisKanan;
    private widget.TextBox PulsasiRadialisKiri;
    private widget.TextBox PulsasiDorsalisKanan;
    private widget.TextBox PulsasiDorsalisKiri;
    private widget.TextBox AlatTerpasang;
    private widget.TextBox JenisAnestesi;
    private widget.TextBox Sedasi;
    private widget.TextBox DosisPre;
    private widget.TextBox DosisIntra;
    private widget.TextBox DosisPost;
    private widget.TextBox Komplikasi;
    private widget.TextBox Intake;
    private widget.TextBox Output;
    private widget.TextBox InstruksiPaska;
    private widget.TextBox MasalahKeperawatan;
    private widget.TextBox RencanaKeperawatan;
    private widget.ComboBox TerpasangSheath;
    private widget.ComboBox KeputusanRawat;
"""

var_inits = """
        JamTiba = new widget.TextBox();
        LokasiPungsi = new widget.TextBox();
        JumlahPungsi = new widget.TextBox();
        LokasiSheath = new widget.TextBox();
        PulsasiRadialisKanan = new widget.TextBox();
        PulsasiRadialisKiri = new widget.TextBox();
        PulsasiDorsalisKanan = new widget.TextBox();
        PulsasiDorsalisKiri = new widget.TextBox();
        AlatTerpasang = new widget.TextBox();
        JenisAnestesi = new widget.TextBox();
        Sedasi = new widget.TextBox();
        DosisPre = new widget.TextBox();
        DosisIntra = new widget.TextBox();
        DosisPost = new widget.TextBox();
        Komplikasi = new widget.TextBox();
        Intake = new widget.TextBox();
        Output = new widget.TextBox();
        InstruksiPaska = new widget.TextBox();
        MasalahKeperawatan = new widget.TextBox();
        RencanaKeperawatan = new widget.TextBox();
        TerpasangSheath = new widget.ComboBox();
        TerpasangSheath.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        KeputusanRawat = new widget.ComboBox();
        KeputusanRawat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Rawat Inap", "Pulang", "Rujuk", "Meninggal" }));

        // Tambahkan ke FormInput agar dirender (opsional secara visual, tapi wajib ada instansinya)
        FormInput.add(JamTiba); FormInput.add(LokasiPungsi); FormInput.add(JumlahPungsi); 
        FormInput.add(LokasiSheath); FormInput.add(PulsasiRadialisKanan); FormInput.add(PulsasiRadialisKiri);
        FormInput.add(PulsasiDorsalisKanan); FormInput.add(PulsasiDorsalisKiri); FormInput.add(AlatTerpasang);
        FormInput.add(JenisAnestesi); FormInput.add(Sedasi); FormInput.add(DosisPre); FormInput.add(DosisIntra);
        FormInput.add(DosisPost); FormInput.add(Komplikasi); FormInput.add(Intake); FormInput.add(Output);
        FormInput.add(InstruksiPaska); FormInput.add(MasalahKeperawatan); FormInput.add(RencanaKeperawatan);
        FormInput.add(TerpasangSheath); FormInput.add(KeputusanRawat);
"""

# Insert declarations at the end of class before the last brace
content = re.sub(r'\}\s*$', var_decls + '\n}', content)

# Insert initializations in constructor
# Find constructor: public RMAsesmenInvasifNonBedah(java.awt.Frame parent, boolean modal) { ... initComponents();
content = re.sub(r'(initComponents\(\);)', r'\1' + var_inits, content)

with open("src/rekammedis/RMAsesmenInvasifNonBedah.java", "w") as f:
    f.write(content)
