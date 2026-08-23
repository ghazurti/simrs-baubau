package simrskhanza;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Dialog cari + list tagihan operasi cathlab.
 * <p>
 * Alur mirip menu Operasi/VK standar (DlgCariTagihanOperasi):
 * <ol>
 *   <li>User buka menu → muncul dialog ini (list & search)</li>
 *   <li>Panel atas: filter tanggal + kata kunci</li>
 *   <li>Tabel utama: 1 baris = 1 record operasi_cathlab</li>
 *   <li>Klik baris → panel rincian bawah tampilkan tim &amp; biaya</li>
 *   <li>Toolbar bawah: Tambah / Edit / Hapus / Cetak Nota / Keluar</li>
 *   <li>Tambah/Edit → buka {@link DlgTagihanOperasiCathlab} sebagai form</li>
 *   <li>Setelah form ditutup, list otomatis di-refresh</li>
 * </ol>
 */
public class DlgCariTagihanOperasiCathlab extends javax.swing.JDialog {

    private final DefaultTableModel tabMode, tabRincian;
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private final NumberFormat fmt = NumberFormat.getInstance(new Locale("id","ID"));

    // Header komponen
    private widget.InternalFrame internalFrame1;
    private widget.panelisi panelTop, panelToolbar;
    private widget.PanelBiasa panelRincian;
    private widget.ScrollPane scrData, scrRincian;
    private widget.Table tbData, tbRincian;
    private widget.Tanggal tglAwal, tglAkhir;
    private widget.TextBox TCari;
    private widget.Button BtnCari, BtnAll, BtnTambah, BtnEdit, BtnHapus, BtnCetak, BtnKeluar;
    private widget.Label LblTotal, LblRincianJudul;

    public DlgCariTagihanOperasiCathlab(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setSize(1250, 720);
        setLocationRelativeTo(parent);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat","Tgl Operasi","Kd Paket","Nama Paket","Jenis Prosedur","Kategori",
            "No.RM","Nama Pasien","Operator","Radiographer","Dosis (mGy)",
            "Waktu Fluoro (mnt)","Kontras (ml)","Status","Total Biaya"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbData.setModel(tabMode);
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbData.setPreferredScrollableViewportSize(new Dimension(800, 300));
        int[] widths = {110,140,80,180,180,80,80,200,160,160,80,110,80,60,110};
        for (int i = 0; i < widths.length; i++) {
            TableColumn c = tbData.getColumnModel().getColumn(i);
            c.setPreferredWidth(widths[i]);
        }
        tbData.setDefaultRenderer(Object.class, new WarnaTable());
        tbData.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (tbData.getSelectedRow() > -1) tampilRincian();
                if (e.getClickCount() == 2) editData();
            }
        });

        tabRincian = new DefaultTableModel(null, new Object[]{
            "Peran","Kode","Nama","Jasa (Rp)"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbRincian.setModel(tabRincian);
        tbRincian.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbRincian.setDefaultRenderer(Object.class, new WarnaTable());
        int[] wR = {150, 90, 250, 130};
        for (int i = 0; i < wR.length; i++) {
            TableColumn c = tbRincian.getColumnModel().getColumn(i);
            c.setPreferredWidth(wR[i]);
        }

        tglAwal.setDate(new Date());
        tglAkhir.setDate(new Date());
        tampil();
    }

    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        panelTop      = new widget.panelisi();
        panelToolbar  = new widget.panelisi();
        panelRincian  = new widget.PanelBiasa();
        scrData       = new widget.ScrollPane();
        scrRincian    = new widget.ScrollPane();
        tbData        = new widget.Table();
        tbRincian     = new widget.Table();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setTitle("Cari Tagihan Operasi Cathlab");

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240,245,235)),
            "::[ Tagihan Operasi Cathlab ]::",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new java.awt.Font("Tahoma",0,11), new java.awt.Color(50,50,50)));
        internalFrame1.setLayout(new java.awt.BorderLayout(1,1));

        // ============ TOP: panel search ============
        panelTop.setPreferredSize(new Dimension(100, 45));
        panelTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 10));

        widget.Label lblTgl = new widget.Label(); lblTgl.setText("Tgl. :");
        lblTgl.setPreferredSize(new Dimension(45,23));
        lblTgl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        tglAwal = new widget.Tanggal();
        tglAwal.setPreferredSize(new Dimension(105,23));
        tglAwal.setDisplayFormat("dd-MM-yyyy");

        widget.Label lblSd = new widget.Label(); lblSd.setText("s/d");
        lblSd.setPreferredSize(new Dimension(25,23));
        lblSd.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        tglAkhir = new widget.Tanggal();
        tglAkhir.setPreferredSize(new Dimension(105,23));
        tglAkhir.setDisplayFormat("dd-MM-yyyy");

        widget.Label lblKey = new widget.Label(); lblKey.setText("Kata Kunci :");
        lblKey.setPreferredSize(new Dimension(80,23));
        lblKey.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        TCari = new widget.TextBox();
        TCari.setPreferredSize(new Dimension(260,23));
        TCari.setToolTipText("no_rawat / nama pasien / paket / jenis prosedur");
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) tampil();
            }
        });

        BtnCari = new widget.Button();
        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setMnemonic('C'); BtnCari.setToolTipText("Alt+C");
        BtnCari.setPreferredSize(new Dimension(28,23));
        BtnCari.addActionListener(e -> tampil());

        BtnAll = new widget.Button();
        BtnAll.setText("Semua"); BtnAll.setMnemonic('U'); BtnAll.setToolTipText("Alt+U");
        BtnAll.setPreferredSize(new Dimension(75,23));
        BtnAll.addActionListener(e -> { TCari.setText(""); tampil(); });

        panelTop.add(lblTgl); panelTop.add(tglAwal); panelTop.add(lblSd); panelTop.add(tglAkhir);
        panelTop.add(lblKey); panelTop.add(TCari);   panelTop.add(BtnCari); panelTop.add(BtnAll);
        internalFrame1.add(panelTop, java.awt.BorderLayout.PAGE_START);

        // ============ CENTER: tabel data + rincian di bawah (JSplitPane) ============
        scrData.setViewportView(tbData);

        // Panel rincian bawah
        panelRincian.setLayout(new java.awt.BorderLayout(2,2));

        LblRincianJudul = new widget.Label();
        LblRincianJudul.setText("  Rincian tim & biaya (pilih baris di atas untuk melihat)");
        LblRincianJudul.setFont(new java.awt.Font("Tahoma", 1, 11));
        LblRincianJudul.setForeground(new java.awt.Color(255,255,255));
        LblRincianJudul.setBackground(new java.awt.Color(178,91,32));
        LblRincianJudul.setOpaque(true);
        LblRincianJudul.setPreferredSize(new Dimension(100, 22));
        panelRincian.add(LblRincianJudul, java.awt.BorderLayout.PAGE_START);

        scrRincian.setViewportView(tbRincian);
        panelRincian.add(scrRincian, java.awt.BorderLayout.CENTER);

        javax.swing.JSplitPane split = new javax.swing.JSplitPane(
            javax.swing.JSplitPane.VERTICAL_SPLIT, scrData, panelRincian);
        split.setResizeWeight(0.55);
        split.setDividerLocation(360);
        internalFrame1.add(split, java.awt.BorderLayout.CENTER);

        // ============ BOTTOM: toolbar aksi ============
        panelToolbar.setPreferredSize(new Dimension(100, 45));
        panelToolbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10));

        BtnTambah = new widget.Button();
        BtnTambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png")));
        BtnTambah.setText("Tambah"); BtnTambah.setMnemonic('T'); BtnTambah.setToolTipText("Alt+T (Insert)");
        BtnTambah.setPreferredSize(new Dimension(105,26));
        BtnTambah.addActionListener(e -> tambahData());

        BtnEdit = new widget.Button();
        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/edit.png")));
        BtnEdit.setText("Edit"); BtnEdit.setMnemonic('E'); BtnEdit.setToolTipText("Alt+E (Double-click)");
        BtnEdit.setPreferredSize(new Dimension(90,26));
        BtnEdit.addActionListener(e -> editData());

        BtnHapus = new widget.Button();
        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setText("Hapus"); BtnHapus.setMnemonic('H'); BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setPreferredSize(new Dimension(95,26));
        BtnHapus.addActionListener(e -> hapusData());

        BtnCetak = new widget.Button();
        BtnCetak.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnCetak.setText("Cetak Nota"); BtnCetak.setMnemonic('N'); BtnCetak.setToolTipText("Alt+N");
        BtnCetak.setPreferredSize(new Dimension(120,26));
        BtnCetak.addActionListener(e -> cetakNota());

        BtnKeluar = new widget.Button();
        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setText("Keluar"); BtnKeluar.setMnemonic('K'); BtnKeluar.setToolTipText("Alt+K (Esc)");
        BtnKeluar.setPreferredSize(new Dimension(95,26));
        BtnKeluar.addActionListener(e -> dispose());

        LblTotal = new widget.Label();
        LblTotal.setText("Total Semua Tagihan : 0");
        LblTotal.setPreferredSize(new Dimension(320,26));
        LblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        LblTotal.setFont(new java.awt.Font("Tahoma",1,13));
        LblTotal.setForeground(new java.awt.Color(180,60,0));

        panelToolbar.add(BtnTambah); panelToolbar.add(BtnEdit);
        panelToolbar.add(BtnHapus);  panelToolbar.add(BtnCetak);
        panelToolbar.add(LblTotal);  panelToolbar.add(BtnKeluar);
        internalFrame1.add(panelToolbar, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        // Shortcut Esc = keluar, Insert = tambah
        javax.swing.JRootPane rp = getRootPane();
        rp.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
          .put(javax.swing.KeyStroke.getKeyStroke("ESCAPE"), "esc");
        rp.getActionMap().put("esc", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { dispose(); }
        });
        rp.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
          .put(javax.swing.KeyStroke.getKeyStroke("INSERT"), "ins");
        rp.getActionMap().put("ins", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { tambahData(); }
        });

        pack();
    }

    // =====================================================================
    // TAMPIL: ambil data operasi_cathlab
    // =====================================================================
    private void tampil() {
        Valid.tabelKosong(tabMode);
        tabRincian.setRowCount(0);
        String kata = "%" + TCari.getText().trim() + "%";
        String sql =
            "select oc.no_rawat, oc.tgl_operasi, oc.kode_paket, oc.jenis_prosedur, oc.kategori, "+
            "  ifnull(pasien.no_rkm_medis,'') as no_rkm_medis, "+
            "  ifnull(pasien.nm_pasien,'') as nm_pasien, "+
            "  ifnull(paket_operasi.nm_perawatan,'') as nm_paket, "+
            "  ifnull(op1.nm_dokter,'') as nm_op1, "+
            "  ifnull(rad1.nama,'')     as nm_rad1, "+
            "  ifnull(oc.dosis_radiasi_mgy,0)    as dosis, "+
            "  ifnull(oc.waktu_fluoroskopi_min,0) as fluoro, "+
            "  ifnull(oc.kontras_ml,0)           as kontras, "+
            "  oc.status, "+
            "  (ifnull(oc.biaya_operator1,0)+ifnull(oc.biaya_operator2,0)+ifnull(oc.biaya_operator3,0)+"+
            "   ifnull(oc.biaya_asisten_operator1,0)+ifnull(oc.biaya_asisten_operator2,0)+ifnull(oc.biaya_asisten_operator3,0)+"+
            "   ifnull(oc.biaya_dokter_anestesi,0)+"+
            "   ifnull(oc.biaya_scrub_nurse1,0)+ifnull(oc.biaya_scrub_nurse2,0)+"+
            "   ifnull(oc.biaya_circulate_nurse1,0)+ifnull(oc.biaya_circulate_nurse2,0)+"+
            "   ifnull(oc.biaya_radiographer1,0)+"+
            "   ifnull(oc.biaya_monitoring1,0)+ifnull(oc.biaya_monitoring2,0)+ifnull(oc.biaya_monitoring3,0)+"+
            "   ifnull(oc.biaya_nurse1,0)+ifnull(oc.biaya_nurse2,0)+ifnull(oc.biaya_nurse3,0)+"+
            "   ifnull(oc.biayaalat,0)+ifnull(oc.biayasewaok,0)+"+
            "   ifnull(oc.akomodasi,0)+ifnull(oc.bagian_rs,0)+ifnull(oc.biayasarpras,0)) as total "+
            "from operasi_cathlab oc "+
            "left join reg_periksa on reg_periksa.no_rawat = oc.no_rawat "+
            "left join pasien       on pasien.no_rkm_medis = reg_periksa.no_rkm_medis "+
            "left join paket_operasi on paket_operasi.kode_paket = oc.kode_paket "+
            "left join dokter  op1  on op1.kd_dokter = oc.operator1 "+
            "left join petugas rad1 on rad1.nip     = oc.radiographer1 "+
            "where oc.tgl_operasi between ? and ? and (" +
            "   oc.no_rawat like ? or ifnull(pasien.no_rkm_medis,'') like ? or "+
            "   ifnull(pasien.nm_pasien,'') like ? or "+
            "   ifnull(paket_operasi.nm_perawatan,'') like ? or "+
            "   oc.jenis_prosedur like ? or oc.kategori like ?"+
            ") order by oc.tgl_operasi desc";
        double totalGlobal = 0;
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, Valid.SetTgl(tglAwal.getSelectedItem()+"")+" 00:00:00");
            ps.setString(2, Valid.SetTgl(tglAkhir.getSelectedItem()+"")+" 23:59:59");
            for (int i = 3; i <= 8; i++) ps.setString(i, kata);
            rs = ps.executeQuery();
            while (rs.next()) {
                double total = rs.getDouble("total");
                totalGlobal += total;
                tabMode.addRow(new Object[]{
                    rs.getString("no_rawat"),
                    rs.getString("tgl_operasi"),
                    rs.getString("kode_paket"),
                    rs.getString("nm_paket"),
                    rs.getString("jenis_prosedur"),
                    rs.getString("kategori"),
                    rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"),
                    rs.getString("nm_op1"),
                    rs.getString("nm_rad1"),
                    fmt.format(rs.getDouble("dosis")),
                    fmt.format(rs.getDouble("fluoro")),
                    fmt.format(rs.getDouble("kontras")),
                    rs.getString("status"),
                    fmt.format(total)
                });
            }
            LblTotal.setText("Total Semua Tagihan : Rp " + fmt.format(totalGlobal) + "     ");
        } catch (Exception e) {
            System.out.println("Notif tampil DlgCariTagihanOperasiCathlab : " + e);
        } finally { closeRs(); }
    }

    /** Isi tabel rincian bawah untuk baris yang sedang dipilih. */
    private void tampilRincian() {
        int r = tbData.getSelectedRow();
        if (r < 0) return;
        tabRincian.setRowCount(0);
        String no  = str(tbData.getValueAt(r, 0));
        String tgl = str(tbData.getValueAt(r, 1));
        String kd  = str(tbData.getValueAt(r, 2));
        LblRincianJudul.setText("  Rincian: " + no + "  |  " + tgl + "  |  " +
            str(tbData.getValueAt(r, 3)) + "  (" + str(tbData.getValueAt(r, 7)) + ")");
        String sql =
            "select oc.*, "+
            "  ifnull(op1.nm_dokter,'') as nm_op1, ifnull(op2.nm_dokter,'') as nm_op2, "+
            "  ifnull(op3.nm_dokter,'') as nm_op3, "+
            "  ifnull(asop1.nama,'') as nm_asop1, ifnull(asop2.nama,'') as nm_asop2, ifnull(asop3.nama,'') as nm_asop3, "+
            "  ifnull(dan.nm_dokter,'') as nm_anes, "+
            "  ifnull(sc1.nama,'') as nm_sc1, ifnull(sc2.nama,'') as nm_sc2, "+
            "  ifnull(cir1.nama,'') as nm_cir1, ifnull(cir2.nama,'') as nm_cir2, "+
            "  ifnull(rad1.nama,'') as nm_rad1, "+
            "  ifnull(mon1.nama,'') as nm_mon1, ifnull(mon2.nama,'') as nm_mon2, ifnull(mon3.nama,'') as nm_mon3, "+
            "  ifnull(nur1.nama,'') as nm_nur1, ifnull(nur2.nama,'') as nm_nur2, ifnull(nur3.nama,'') as nm_nur3 "+
            "from operasi_cathlab oc "+
            "left join dokter  op1  on op1.kd_dokter = oc.operator1 "+
            "left join dokter  op2  on op2.kd_dokter = oc.operator2 "+
            "left join dokter  op3  on op3.kd_dokter = oc.operator3 "+
            "left join petugas asop1 on asop1.nip = oc.asisten_operator1 "+
            "left join petugas asop2 on asop2.nip = oc.asisten_operator2 "+
            "left join petugas asop3 on asop3.nip = oc.asisten_operator3 "+
            "left join dokter  dan  on dan.kd_dokter = oc.dokter_anestesi "+
            "left join petugas sc1  on sc1.nip = oc.scrub_nurse1 "+
            "left join petugas sc2  on sc2.nip = oc.scrub_nurse2 "+
            "left join petugas cir1 on cir1.nip = oc.circulate_nurse1 "+
            "left join petugas cir2 on cir2.nip = oc.circulate_nurse2 "+
            "left join petugas rad1 on rad1.nip = oc.radiographer1 "+
            "left join petugas mon1 on mon1.nip = oc.monitoring1 "+
            "left join petugas mon2 on mon2.nip = oc.monitoring2 "+
            "left join petugas mon3 on mon3.nip = oc.monitoring3 "+
            "left join petugas nur1 on nur1.nip = oc.nurse1 "+
            "left join petugas nur2 on nur2.nip = oc.nurse2 "+
            "left join petugas nur3 on nur3.nip = oc.nurse3 "+
            "where oc.no_rawat=? and oc.tgl_operasi=? and oc.kode_paket=?";
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, no);
            ps.setString(2, tgl);
            ps.setString(3, kd);
            rs = ps.executeQuery();
            if (rs.next()) {
                addRincian("Operator 1",       rs.getString("operator1"),        rs.getString("nm_op1"),  rs.getDouble("biaya_operator1"));
                addRincian("Operator 2",       rs.getString("operator2"),        rs.getString("nm_op2"),  rs.getDouble("biaya_operator2"));
                addRincian("Operator 3",       rs.getString("operator3"),        rs.getString("nm_op3"),  rs.getDouble("biaya_operator3"));
                addRincian("Asisten Operator 1", rs.getString("asisten_operator1"), rs.getString("nm_asop1"), rs.getDouble("biaya_asisten_operator1"));
                addRincian("Asisten Operator 2", rs.getString("asisten_operator2"), rs.getString("nm_asop2"), rs.getDouble("biaya_asisten_operator2"));
                addRincian("Asisten Operator 3", rs.getString("asisten_operator3"), rs.getString("nm_asop3"), rs.getDouble("biaya_asisten_operator3"));
                addRincian("dr. Anestesi",     rs.getString("dokter_anestesi"),  rs.getString("nm_anes"), rs.getDouble("biaya_dokter_anestesi"));
                addRincian("Scrub Nurse 1",    rs.getString("scrub_nurse1"),     rs.getString("nm_sc1"),  rs.getDouble("biaya_scrub_nurse1"));
                addRincian("Scrub Nurse 2",    rs.getString("scrub_nurse2"),     rs.getString("nm_sc2"),  rs.getDouble("biaya_scrub_nurse2"));
                addRincian("Circulate Nurse 1",rs.getString("circulate_nurse1"), rs.getString("nm_cir1"), rs.getDouble("biaya_circulate_nurse1"));
                addRincian("Circulate Nurse 2",rs.getString("circulate_nurse2"), rs.getString("nm_cir2"), rs.getDouble("biaya_circulate_nurse2"));
                addRincian("Radiographer",     rs.getString("radiographer1"),    rs.getString("nm_rad1"), rs.getDouble("biaya_radiographer1"));
                addRincian("Monitoring 1",     rs.getString("monitoring1"),      rs.getString("nm_mon1"), rs.getDouble("biaya_monitoring1"));
                addRincian("Monitoring 2",     rs.getString("monitoring2"),      rs.getString("nm_mon2"), rs.getDouble("biaya_monitoring2"));
                addRincian("Monitoring 3",     rs.getString("monitoring3"),      rs.getString("nm_mon3"), rs.getDouble("biaya_monitoring3"));
                addRincian("Nurse 1",          rs.getString("nurse1"),           rs.getString("nm_nur1"), rs.getDouble("biaya_nurse1"));
                addRincian("Nurse 2",          rs.getString("nurse2"),           rs.getString("nm_nur2"), rs.getDouble("biaya_nurse2"));
                addRincian("Nurse 3",          rs.getString("nurse3"),           rs.getString("nm_nur3"), rs.getDouble("biaya_nurse3"));

                // Info klinis (info-only, tidak masuk jasa)
                tabRincian.addRow(new Object[]{"— Safety Radiasi —","","",""});
                addInfoRow("Dosis Radiasi",     rs.getDouble("dosis_radiasi_mgy"), "mGy");
                addInfoRow("Waktu Fluoroskopi", rs.getDouble("waktu_fluoroskopi_min"), "menit");
                addInfoRow("Kontras Digunakan", rs.getDouble("kontras_ml"), "ml");

                // Jenis prosedur & kateter
                tabRincian.addRow(new Object[]{"— Info Prosedur —","","",""});
                tabRincian.addRow(new Object[]{"Jenis Prosedur", "", nvl(rs.getString("jenis_prosedur")), ""});
                tabRincian.addRow(new Object[]{"No. Kateter",    "", nvl(rs.getString("nomor_kateter")), ""});
                tabRincian.addRow(new Object[]{"Alat Khusus",    "", nvl(rs.getString("alat_khusus")), ""});
            }
        } catch (Exception e) {
            System.out.println("Notif tampilRincian : " + e);
        } finally { closeRs(); }
    }

    private void addRincian(String peran, String kode, String nama, double biaya) {
        if ((kode == null || kode.isEmpty()) && biaya == 0) return; // slot kosong → skip
        tabRincian.addRow(new Object[]{
            peran, nvl(kode), nvl(nama), fmt.format(biaya)
        });
    }
    private void addInfoRow(String label, double v, String satuan) {
        tabRincian.addRow(new Object[]{label, "", fmt.format(v) + " " + satuan, ""});
    }
    private String nvl(String s) { return s == null ? "" : s; }
    private String str(Object o) { return o == null ? "" : o.toString(); }

    // =====================================================================
    // AKSI: Tambah / Edit / Hapus / Cetak Nota
    // =====================================================================
    private void tambahData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgTagihanOperasiCathlab form = new DlgTagihanOperasiCathlab(null, true);
        form.isCek();
        form.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { tampil(); }
        });
        form.setVisible(true);
        setCursor(Cursor.getDefaultCursor());
    }

    private void editData() {
        int r = tbData.getSelectedRow();
        if (r < 0) { warn("Pilih baris data yang mau diedit dulu."); return; }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgTagihanOperasiCathlab form = new DlgTagihanOperasiCathlab(null, true);
        form.isCek();
        form.loadEditData(str(tbData.getValueAt(r,0)),
                          str(tbData.getValueAt(r,1)),
                          str(tbData.getValueAt(r,2)));
        form.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { tampil(); }
        });
        form.setVisible(true);
        setCursor(Cursor.getDefaultCursor());
    }

    private void hapusData() {
        int r = tbData.getSelectedRow();
        if (r < 0) { warn("Pilih baris data yang mau dihapus."); return; }
        if (!akses.getoperasi()) { warn("Anda tidak memiliki hak akses menghapus operasi."); return; }
        String no  = str(tbData.getValueAt(r,0));
        String tgl = str(tbData.getValueAt(r,1));
        String kd  = str(tbData.getValueAt(r,2));
        String nm  = str(tbData.getValueAt(r,7));
        if (JOptionPane.showConfirmDialog(this,
                "Hapus data operasi cathlab berikut?\n\n"+
                "Pasien : " + nm + "\n"+
                "No.Rw  : " + no + "\n"+
                "Tgl    : " + tgl,
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement("delete from operasi_cathlab where no_rawat=? and tgl_operasi=? and kode_paket=?");
            ps.setString(1, no); ps.setString(2, tgl); ps.setString(3, kd);
            int n = ps.executeUpdate();
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "Data dihapus.");
                tampil();
            } else {
                JOptionPane.showMessageDialog(this, "Tidak ada data yang dihapus.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus : " + e.getMessage());
        } finally { closeRs(); }
    }

    private void cetakNota() {
        int r = tbData.getSelectedRow();
        if (r < 0) { warn("Pilih baris data yang mau dicetak."); return; }
        String no  = str(tbData.getValueAt(r,0));
        String tgl = str(tbData.getValueAt(r,1));
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("namars",     akses.getnamars());
            param.put("alamatrs",   akses.getalamatrs());
            param.put("kotars",     akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs",   akses.getkontakrs());
            param.put("emailrs",    akses.getemailrs());
            param.put("logo",       Sequel.cariGambar("select setting.logo from setting"));
            param.put("norawat",       no);
            param.put("tanggaloperasi",tgl);
            param.put("tindakan", "");
            param.put("finger",   "");
            String kosong = "";
            param.put("tgl_perawatan", null);
            param.put("jam_rawat",     kosong);
            param.put("alergi",        kosong);
            param.put("keluhan",       kosong);
            param.put("pemeriksaan",   kosong);
            param.put("penilaian",     kosong);
            param.put("rtl",           kosong);
            param.put("ruang",         kosong);
            param.put("suhu_tubuh",    kosong);
            param.put("tensi",         kosong);
            param.put("tinggi",        kosong);
            param.put("berat",         kosong);
            param.put("nadi",          kosong);
            param.put("respirasi",     kosong);
            param.put("gcs",           kosong);
            try {
                koneksi = koneksiDB.condb();
                ps = koneksi.prepareStatement(
                    "select tgl_perawatan,jam_rawat,suhu_tubuh,tensi,nadi,respirasi,tinggi,berat,gcs,"+
                    "keluhan,pemeriksaan,alergi,rtl,penilaian "+
                    "from pemeriksaan_ranap where no_rawat=? and concat(tgl_perawatan,' ',jam_rawat) <= ? "+
                    "order by tgl_perawatan desc, jam_rawat desc limit 1");
                ps.setString(1, no);
                ps.setString(2, tgl);
                rs = ps.executeQuery();
                if (rs.next()) {
                    param.put("tgl_perawatan", rs.getDate("tgl_perawatan"));
                    param.put("jam_rawat",     rs.getString("jam_rawat"));
                    param.put("alergi",        rs.getString("alergi"));
                    param.put("keluhan",       rs.getString("keluhan"));
                    param.put("pemeriksaan",   rs.getString("pemeriksaan"));
                    param.put("penilaian",     rs.getString("penilaian"));
                    param.put("rtl",           rs.getString("rtl"));
                    param.put("suhu_tubuh",    rs.getString("suhu_tubuh"));
                    param.put("tensi",         rs.getString("tensi"));
                    param.put("tinggi",        rs.getString("tinggi"));
                    param.put("berat",         rs.getString("berat"));
                    param.put("nadi",          rs.getString("nadi"));
                    param.put("respirasi",     rs.getString("respirasi"));
                    param.put("gcs",           rs.getString("gcs"));
                    param.put("ruang", Sequel.cariIsi(
                        "select nm_bangsal from bangsal inner join kamar inner join kamar_inap "+
                        "on bangsal.kd_bangsal=kamar.kd_bangsal and kamar_inap.kd_kamar=kamar.kd_kamar "+
                        "where no_rawat=? order by tgl_masuk desc limit 1", no));
                }
            } catch (Exception ignore) {} finally { closeRs(); }

            Valid.MyReport("rptTagihanOperasiCathlab.jasper", "report",
                "::[ Laporan Operasi Cathlab ]::", param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal cetak : " + e.getMessage());
        }
    }

    // =====================================================================
    // UTIL
    // =====================================================================
    public void isCek() {
        BtnTambah.setEnabled(akses.getoperasi());
        BtnEdit.setEnabled(akses.getoperasi());
        BtnHapus.setEnabled(akses.getoperasi());
        BtnCetak.setEnabled(akses.getoperasi());
    }
    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void closeRs() {
        try { if (rs != null) rs.close(); } catch (Exception ignore) {}
        try { if (ps != null) ps.close(); } catch (Exception ignore) {}
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgCariTagihanOperasiCathlab d = new DlgCariTagihanOperasiCathlab(new javax.swing.JFrame(), true);
            d.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
            });
            d.setVisible(true);
        });
    }
}
