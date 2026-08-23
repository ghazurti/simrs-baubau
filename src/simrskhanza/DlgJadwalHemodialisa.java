package simrskhanza;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import kepegawaian.DlgCariDokter;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Form Jadwal Hemodialisa.
 * Tabel target: {@code jadwal_hemodialisa}.
 * Digunakan petugas HD untuk entry jadwal harian. Data ini dibaca oleh
 * DlgDaftarPermintaanResep saat validasi resep untuk memunculkan marker
 * [HD] di kolom Ruang/Kamar — supaya apotek tahu resep pasien tsb
 * terkait sesi HD walaupun statusnya ranap.
 */
public final class DlgJadwalHemodialisa extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;

    private widget.InternalFrame internalFrame1;
    private widget.panelisi panelTop, panelToolbar;
    private widget.PanelBiasa panelInput;
    private widget.ScrollPane Scroll;
    private widget.Table tbData;
    private widget.Tanggal tglCari1, tglCari2, tglJadwal;
    private widget.TextBox TCari, TNoRw, TPasien, TKdDokter, TNmDokter, TKeterangan;
    private widget.ComboBox cmbJam, cmbMenit, cmbStatus;
    private widget.Button BtnSimpan, BtnEdit, BtnHapus, BtnBaru, BtnKeluar,
                          BtnCari, BtnAll, BtnCariRawat, BtnCariDokter;

    public DlgJadwalHemodialisa(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initUI();
        setSize(1100, 620);
        setLocationRelativeTo(parent);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat","Tanggal","Jam Mulai","Jam Selesai","No.RM","Nama Pasien",
            "Kd Dokter","Nama Dokter","Status","Keterangan"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbData.setModel(tabMode);
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {120,90,80,80,80,180,80,180,90,180};
        for (int i = 0; i < widths.length; i++) {
            TableColumn c = tbData.getColumnModel().getColumn(i);
            c.setPreferredWidth(widths[i]);
        }
        tbData.setDefaultRenderer(Object.class, new WarnaTable());
        tbData.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (tbData.getSelectedRow() > -1) loadDataDariTabel();
            }
        });

        String[] jams = new String[24];
        for (int i=0;i<24;i++) jams[i] = String.format("%02d", i);
        String[] menits = new String[60];
        for (int i=0;i<60;i++) menits[i] = String.format("%02d", i);
        cmbJam.setModel(new javax.swing.DefaultComboBoxModel<>(jams));
        cmbMenit.setModel(new javax.swing.DefaultComboBoxModel<>(menits));
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Terjadwal","Selesai","Batal"}));

        tglJadwal.setDate(new Date());
        tglCari1.setDate(new Date());
        tglCari2.setDate(new Date());
        cmbJam.setSelectedItem("08");

        emptTeks();
        tampil();
    }

    private void initUI() {
        internalFrame1 = new widget.InternalFrame();
        panelTop      = new widget.panelisi();
        panelInput    = new widget.PanelBiasa();
        panelToolbar  = new widget.panelisi();
        Scroll        = new widget.ScrollPane();
        tbData        = new widget.Table();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setTitle("Jadwal Hemodialisa");

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240,245,235)),
            "::[ Jadwal Hemodialisa ]::",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new java.awt.Font("Tahoma",0,11), new java.awt.Color(50,50,50)));
        internalFrame1.setLayout(new java.awt.BorderLayout(1,1));

        // ---- TOP CARI ----
        panelTop.setPreferredSize(new Dimension(100, 40));
        panelTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 8));
        widget.Label lblCari = new widget.Label(); lblCari.setText("Tgl. :");
        lblCari.setPreferredSize(new Dimension(35,23));
        lblCari.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tglCari1 = new widget.Tanggal(); tglCari1.setPreferredSize(new Dimension(100,23)); tglCari1.setDisplayFormat("dd-MM-yyyy");
        widget.Label lblSd = new widget.Label(); lblSd.setText("s/d");
        lblSd.setPreferredSize(new Dimension(25,23)); lblSd.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tglCari2 = new widget.Tanggal(); tglCari2.setPreferredSize(new Dimension(100,23)); tglCari2.setDisplayFormat("dd-MM-yyyy");
        widget.Label lblKey = new widget.Label(); lblKey.setText("Kata Kunci :");
        lblKey.setPreferredSize(new Dimension(75,23));
        lblKey.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        TCari = new widget.TextBox(); TCari.setPreferredSize(new Dimension(200,23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER) tampil();
            }
        });
        BtnCari = new widget.Button();
        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setPreferredSize(new Dimension(28,23));
        BtnCari.addActionListener(e -> tampil());
        BtnAll = new widget.Button();
        BtnAll.setText("Semua");
        BtnAll.setPreferredSize(new Dimension(75,23));
        BtnAll.addActionListener(e -> { TCari.setText(""); tampil(); });
        panelTop.add(lblCari); panelTop.add(tglCari1); panelTop.add(lblSd); panelTop.add(tglCari2);
        panelTop.add(lblKey); panelTop.add(TCari); panelTop.add(BtnCari); panelTop.add(BtnAll);
        internalFrame1.add(panelTop, java.awt.BorderLayout.PAGE_START);

        // ---- CENTER split ----
        Scroll.setViewportView(tbData);
        panelInput.setLayout(null);
        panelInput.setPreferredSize(new Dimension(100, 130));
        buildFormFields(panelInput);
        javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT, Scroll, panelInput);
        split.setResizeWeight(0.6);
        split.setDividerLocation(280);
        internalFrame1.add(split, java.awt.BorderLayout.CENTER);

        // ---- TOOLBAR ----
        panelToolbar.setPreferredSize(new Dimension(100, 40));
        panelToolbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 8));

        BtnSimpan = new widget.Button();
        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setText("Simpan"); BtnSimpan.setMnemonic('S');
        BtnSimpan.setPreferredSize(new Dimension(100,25));
        BtnSimpan.addActionListener(e -> simpan());

        BtnBaru = new widget.Button();
        BtnBaru.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png")));
        BtnBaru.setText("Baru"); BtnBaru.setMnemonic('B');
        BtnBaru.setPreferredSize(new Dimension(90,25));
        BtnBaru.addActionListener(e -> emptTeks());

        BtnEdit = new widget.Button();
        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/edit.png")));
        BtnEdit.setText("Ganti"); BtnEdit.setMnemonic('G');
        BtnEdit.setPreferredSize(new Dimension(90,25));
        BtnEdit.addActionListener(e -> edit());

        BtnHapus = new widget.Button();
        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setText("Hapus"); BtnHapus.setMnemonic('H');
        BtnHapus.setPreferredSize(new Dimension(95,25));
        BtnHapus.addActionListener(e -> hapus());

        BtnKeluar = new widget.Button();
        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setText("Keluar"); BtnKeluar.setMnemonic('K');
        BtnKeluar.setPreferredSize(new Dimension(95,25));
        BtnKeluar.addActionListener(e -> dispose());

        panelToolbar.add(BtnSimpan); panelToolbar.add(BtnBaru);
        panelToolbar.add(BtnEdit); panelToolbar.add(BtnHapus);
        panelToolbar.add(BtnKeluar);
        internalFrame1.add(panelToolbar, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        pack();
    }

    private void buildFormFields(widget.PanelBiasa p) {
        int y = 8, h = 22, dy = 26;

        addLabel(p, "No.Rawat :", 0, y, 100);
        TNoRw = new widget.TextBox(); TNoRw.setBounds(110, y, 160, h); p.add(TNoRw);
        BtnCariRawat = new widget.Button();
        BtnCariRawat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnCariRawat.setBounds(272, y, 24, h);
        BtnCariRawat.addActionListener(e -> pilihNoRawat());
        p.add(BtnCariRawat);
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER) loadPasien();
            }
        });

        addLabel(p, "Nama Pasien :", 300, y, 100);
        TPasien = new widget.TextBox(); TPasien.setBounds(405, y, 260, h);
        TPasien.setEditable(false); p.add(TPasien);

        addLabel(p, "Tanggal :", 680, y, 80);
        tglJadwal = new widget.Tanggal(); tglJadwal.setBounds(765, y, 130, h);
        tglJadwal.setDisplayFormat("dd-MM-yyyy"); p.add(tglJadwal);

        y += dy;
        addLabel(p, "Jam Mulai :", 0, y, 100);
        cmbJam = new widget.ComboBox(); cmbJam.setBounds(110, y, 60, h); p.add(cmbJam);
        widget.Label lblJP = new widget.Label(); lblJP.setText(":");
        lblJP.setBounds(172, y, 10, h); p.add(lblJP);
        cmbMenit = new widget.ComboBox(); cmbMenit.setBounds(185, y, 60, h); p.add(cmbMenit);

        addLabel(p, "Status :", 300, y, 100);
        cmbStatus = new widget.ComboBox(); cmbStatus.setBounds(405, y, 160, h); p.add(cmbStatus);

        addLabel(p, "Dokter :", 680, y, 80);
        TKdDokter = new widget.TextBox(); TKdDokter.setBounds(765, y, 60, h); p.add(TKdDokter);
        TNmDokter = new widget.TextBox(); TNmDokter.setBounds(827, y, 130, h);
        TNmDokter.setEditable(false); p.add(TNmDokter);
        BtnCariDokter = new widget.Button();
        BtnCariDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnCariDokter.setBounds(959, y, 24, h);
        BtnCariDokter.addActionListener(e -> pilihDokter());
        p.add(BtnCariDokter);

        y += dy;
        addLabel(p, "Keterangan :", 0, y, 100);
        TKeterangan = new widget.TextBox(); TKeterangan.setBounds(110, y, 873, h); p.add(TKeterangan);

        p.setPreferredSize(new Dimension(1000, y + 40));
    }

    private void addLabel(widget.PanelBiasa p, String txt, int x, int y, int w) {
        widget.Label l = new widget.Label();
        l.setText(txt);
        l.setBounds(x, y, w, 22);
        l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        p.add(l);
    }

    private void pilihNoRawat() {
        final DlgCariReg dlg = new DlgCariReg(null, false);
        dlg.addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosing(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e) {
                JTable tb = dlg.getTable();
                int r = tb.getSelectedRow();
                if (r != -1) {
                    TNoRw.setText(tb.getValueAt(r, 1).toString());   // No.Rawat
                    TPasien.setText(tb.getValueAt(r, 7).toString()); // Pasien
                }
            }
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });
        dlg.setSize(800, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void loadPasien() {
        String nm = Sequel.cariIsi(
            "select pasien.nm_pasien from reg_periksa "+
            "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
            "where reg_periksa.no_rawat=?", TNoRw.getText().trim());
        TPasien.setText(nm == null ? "" : nm);
    }

    private void pilihDokter() {
        final DlgCariDokter dlg = new DlgCariDokter(null, false);
        dlg.addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosing(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e) {
                JTable tb = dlg.getTable();
                int r = tb.getSelectedRow();
                if (r != -1) {
                    TKdDokter.setText(tb.getValueAt(r, 0).toString());
                    TNmDokter.setText(tb.getValueAt(r, 1).toString());
                }
            }
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });
        dlg.setSize(700, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void simpan() {
        if (TNoRw.getText().trim().isEmpty()) { warn("No.Rawat wajib diisi."); return; }
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(
                "insert into jadwal_hemodialisa (no_rawat,tanggal,jam_mulai,jam_selesai,kd_dokter,keterangan,status) "+
                "values (?,?,?,?,?,?,?)");
            bindCommon(ps, 1);
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Jadwal HD tersimpan.");
                tampil(); emptTeks();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal simpan : " + e.getMessage());
        } finally { closeRs(); }
    }

    private void edit() {
        int row = tbData.getSelectedRow();
        if (row < 0) { warn("Pilih baris yang mau diedit."); return; }
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(
                "update jadwal_hemodialisa set no_rawat=?,tanggal=?,jam_mulai=?,jam_selesai=?,kd_dokter=?,keterangan=?,status=? "+
                "where no_rawat=? and tanggal=? and jam_mulai=?");
            int i = bindCommon(ps, 1);
            ps.setString(i++, tbData.getValueAt(row,0).toString());
            ps.setString(i++, tbData.getValueAt(row,1).toString());
            ps.setString(i++, tbData.getValueAt(row,2).toString());
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Jadwal diperbarui.");
                tampil(); emptTeks();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal edit : " + e.getMessage());
        } finally { closeRs(); }
    }

    private void hapus() {
        int row = tbData.getSelectedRow();
        if (row < 0) { warn("Pilih baris yang mau dihapus."); return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus jadwal HD ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(
                "delete from jadwal_hemodialisa where no_rawat=? and tanggal=? and jam_mulai=?");
            ps.setString(1, tbData.getValueAt(row,0).toString());
            ps.setString(2, tbData.getValueAt(row,1).toString());
            ps.setString(3, tbData.getValueAt(row,2).toString());
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Jadwal dihapus.");
                tampil(); emptTeks();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus : " + e.getMessage());
        } finally { closeRs(); }
    }

    private int bindCommon(PreparedStatement ps, int i) throws Exception {
        String jam = (String)cmbJam.getSelectedItem() + ":" + (String)cmbMenit.getSelectedItem() + ":00";
        ps.setString(i++, TNoRw.getText().trim());
        ps.setString(i++, Valid.SetTgl(tglJadwal.getSelectedItem()+""));
        ps.setString(i++, jam);
        ps.setString(i++, null);
        ps.setString(i++, TKdDokter.getText().trim().isEmpty() ? null : TKdDokter.getText().trim());
        ps.setString(i++, TKeterangan.getText());
        ps.setString(i++, cmbStatus.getSelectedItem()+"");
        return i;
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        String kata = "%" + TCari.getText().trim() + "%";
        String sql = "select jh.no_rawat, jh.tanggal, jh.jam_mulai, ifnull(jh.jam_selesai,'') as jam_selesai, "+
                     "pasien.no_rkm_medis, pasien.nm_pasien, "+
                     "ifnull(jh.kd_dokter,'') as kd_dokter, ifnull(d.nm_dokter,'') as nm_dokter, "+
                     "jh.status, ifnull(jh.keterangan,'') as keterangan "+
                     "from jadwal_hemodialisa jh "+
                     "inner join reg_periksa r on jh.no_rawat=r.no_rawat "+
                     "inner join pasien on r.no_rkm_medis=pasien.no_rkm_medis "+
                     "left join dokter d on jh.kd_dokter=d.kd_dokter "+
                     "where jh.tanggal between ? and ? and "+
                     "(jh.no_rawat like ? or pasien.nm_pasien like ? or pasien.no_rkm_medis like ? or ifnull(d.nm_dokter,'') like ?) "+
                     "order by jh.tanggal desc, jh.jam_mulai desc";
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, Valid.SetTgl(tglCari1.getSelectedItem()+""));
            ps.setString(2, Valid.SetTgl(tglCari2.getSelectedItem()+""));
            ps.setString(3, kata); ps.setString(4, kata); ps.setString(5, kata); ps.setString(6, kata);
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    rs.getString("no_rawat"), rs.getString("tanggal"), rs.getString("jam_mulai"),
                    rs.getString("jam_selesai"), rs.getString("no_rkm_medis"), rs.getString("nm_pasien"),
                    rs.getString("kd_dokter"), rs.getString("nm_dokter"),
                    rs.getString("status"), rs.getString("keterangan")
                });
            }
        } catch (Exception e) {
            System.out.println("Notif tampil : " + e);
        } finally { closeRs(); }
    }

    private void loadDataDariTabel() {
        int r = tbData.getSelectedRow();
        if (r < 0) return;
        TNoRw.setText(tbData.getValueAt(r,0).toString());
        TPasien.setText(tbData.getValueAt(r,5).toString());
        Valid.SetTgl(tglJadwal, tbData.getValueAt(r,1).toString());
        String jam = tbData.getValueAt(r,2).toString();
        if (jam.length() >= 5) {
            cmbJam.setSelectedItem(jam.substring(0,2));
            cmbMenit.setSelectedItem(jam.substring(3,5));
        }
        TKdDokter.setText(tbData.getValueAt(r,6).toString());
        TNmDokter.setText(tbData.getValueAt(r,7).toString());
        cmbStatus.setSelectedItem(tbData.getValueAt(r,8).toString());
        TKeterangan.setText(tbData.getValueAt(r,9).toString());
    }

    public void emptTeks() {
        TNoRw.setText(""); TPasien.setText("");
        TKdDokter.setText(""); TNmDokter.setText("");
        TKeterangan.setText("");
        tglJadwal.setDate(new Date());
        cmbJam.setSelectedItem("08"); cmbMenit.setSelectedItem("00");
        cmbStatus.setSelectedIndex(0);
    }

    public void isCek() {
        BtnSimpan.setEnabled(true);
        BtnEdit.setEnabled(true);
        BtnHapus.setEnabled(true);
    }

    /**
     * Pre-populate No.Rawat + Nama Pasien + Dokter DPJP saat dialog
     * dibuka dari popup menu di DlgKamarInap (Permintaan → Jadwal
     * Hemodialisa). Filter tabel juga ke no_rawat itu supaya list
     * hanya tampil jadwal pasien tsb.
     */
    public void setNoRawat(String noRawat, String namaPasien) {
        if (noRawat == null) return;
        TNoRw.setText(noRawat.trim());
        if (namaPasien != null) TPasien.setText(namaPasien);
        // Auto-fetch DPJP: prioritas SEP ranap (kddpjp), fallback ke reg_periksa.kd_dokter
        String kdDokter = "";
        String nmDokter = "";
        try {
            koneksi = koneksiDB.condb();
            // Priority #1: DPJP dari bridging_sep ranap (jnspelayanan='2')
            ps = koneksi.prepareStatement(
                "select sep.kddpjp, ifnull(d.nm_dokter,'') as nm_dokter "+
                "from bridging_sep sep left join dokter d on sep.kddpjp=d.kd_dokter "+
                "where sep.no_rawat=? and sep.jnspelayanan='2' and sep.kddpjp<>'' "+
                "order by sep.tglsep desc limit 1");
            ps.setString(1, noRawat.trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                kdDokter = rs.getString("kddpjp");
                nmDokter = rs.getString("nm_dokter");
            }
            closeRs();
            // Priority #2: fallback ke reg_periksa.kd_dokter kalau SEP belum ada
            if (kdDokter == null || kdDokter.trim().isEmpty()) {
                ps = koneksi.prepareStatement(
                    "select r.kd_dokter, ifnull(d.nm_dokter,'') as nm_dokter "+
                    "from reg_periksa r left join dokter d on r.kd_dokter=d.kd_dokter "+
                    "where r.no_rawat=?");
                ps.setString(1, noRawat.trim());
                rs = ps.executeQuery();
                if (rs.next()) {
                    kdDokter = rs.getString("kd_dokter");
                    nmDokter = rs.getString("nm_dokter");
                }
            }
        } catch (Exception e) {
            System.out.println("Notif setNoRawat dokter : " + e);
        } finally { closeRs(); }
        TKdDokter.setText(kdDokter == null ? "" : kdDokter);
        TNmDokter.setText(nmDokter == null ? "" : nmDokter);
        TCari.setText(noRawat.trim());
        tampil();
    }

    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void closeRs() {
        try { if (rs != null) rs.close(); } catch (Exception ignore) {}
        try { if (ps != null) ps.close(); } catch (Exception ignore) {}
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgJadwalHemodialisa d = new DlgJadwalHemodialisa(new javax.swing.JFrame(), true);
            d.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
            });
            d.setVisible(true);
        });
    }
}
