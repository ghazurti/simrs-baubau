package inventory;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public final class MasterRestriksiObatBPJS extends javax.swing.JDialog {

    // --- tabel utama ---
    private final DefaultTableModel tabMode = new DefaultTableModel(
        new Object[]{"Kode Barang","Nama Barang","KdJenis","Kd PJ","Max Jml","Max Iter","Aktif","Keterangan","Restriksi","Level"}, 0
    ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private widget.Table tbData = new widget.Table();

    // --- tabel cari obat ---
    private final DefaultTableModel tabCari = new DefaultTableModel(
        new Object[]{"Kode","Nama Barang","Jenis"}, 0
    ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private widget.Table tbCari = new widget.Table();

    // --- form fields ---
    private widget.TextBox TCariObat   = new widget.TextBox();
    private widget.TextBox TKode       = new widget.TextBox();
    private widget.Label   LNama       = new widget.Label();
    private widget.TextBox TKdJenis    = new widget.TextBox();
    @SuppressWarnings("unchecked")
    private widget.ComboBox TKdPJ      = new widget.ComboBox();
    private widget.TextBox TMaxJml     = new widget.TextBox();
    private widget.TextBox TMaxIter    = new widget.TextBox();
    @SuppressWarnings("unchecked")
    private widget.ComboBox CbAktif    = new widget.ComboBox();
    private widget.TextBox TKet        = new widget.TextBox();
    private final JTextArea TRestriksi = new JTextArea(3, 60);
    @SuppressWarnings("unchecked")
    private widget.ComboBox CbLevel    = new widget.ComboBox();
    private widget.Label   LCount      = new widget.Label();

    // --- buttons ---
    private widget.Button BtnSimpan = new widget.Button();
    private widget.Button BtnBaru   = new widget.Button();
    private widget.Button BtnHapus  = new widget.Button();
    private widget.Button BtnKeluar = new widget.Button();
    private widget.Button BtnCariObat = new widget.Button();
    private widget.Button BtnCari   = new widget.Button();
    private widget.Button BtnAll    = new widget.Button();

    private Timer searchTimer;

    public MasterRestriksiObatBPJS(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(1150, 760);
        setLocationRelativeTo(parent);

        initWidgets();
        initComponents();
        loadData();
        TCariObat.requestFocusInWindow();
    }

    private void initWidgets() {
        CbAktif.setModel(new DefaultComboBoxModel<>(new String[]{"Y", "N"}));
        CbLevel.setModel(new DefaultComboBoxModel<>(new String[]{
            "0 - ALL", "1 - FKTP", "2 - FKRTL Tk.2", "3 - FKRTL Tk.3"
        }));
        TRestriksi.setLineWrap(true);
        TRestriksi.setWrapStyleWord(true);
        TRestriksi.setFont(new Font("Tahoma", Font.PLAIN, 11));

        TKode.setEditable(false);
        TKode.setBackground(new Color(230, 230, 230));

        LNama.setFont(new Font("Tahoma", Font.BOLD, 11));
        LNama.setForeground(new Color(0, 0, 150));
        LNama.setText("-");

        LCount.setText("0");

        TCariObat.setDocument(new batasInput((byte) 100).getKata(TCariObat));
        TKdJenis.setDocument(new batasInput((byte) 20).getKata(TKdJenis));
        loadPenjabModel();
        TMaxJml.setDocument(new batasInput((byte) 15).getKata(TMaxJml));
        TMaxIter.setDocument(new batasInput((byte) 5).getKata(TMaxIter));
        TKet.setDocument(new batasInput((byte) 200).getKata(TKet));

        TKdJenis.setText("ALL");
        setKdPj("ALL");
        TMaxIter.setText("0");

        // tabel utama
        tbData.setAutoCreateRowSorter(true);
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbData.setRowHeight(20);
        tbData.setDefaultRenderer(Object.class, new WarnaTable());
        tbData.setModel(tabMode);
        int[] w = {100, 200, 60, 60, 60, 55, 40, 180, 240, 80};
        for (int i = 0; i < w.length; i++) tbData.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        // tabel cari obat
        tbCari.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbCari.setRowHeight(20);
        tbCari.setDefaultRenderer(Object.class, new WarnaTable());
        tbCari.setModel(tabCari);
        tbCari.getColumnModel().getColumn(0).setPreferredWidth(100);
        tbCari.getColumnModel().getColumn(1).setPreferredWidth(330);
        tbCari.getColumnModel().getColumn(2).setPreferredWidth(100);

        // buttons
        BtnSimpan.setIcon(new ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setMnemonic('S'); BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S"); BtnSimpan.setPreferredSize(new Dimension(100, 30));

        BtnBaru.setIcon(new ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png")));
        BtnBaru.setMnemonic('B'); BtnBaru.setText("Baru");
        BtnBaru.setToolTipText("Alt+B"); BtnBaru.setPreferredSize(new Dimension(100, 30));

        BtnHapus.setIcon(new ImageIcon(getClass().getResource("/picture/delete-16x16.png")));
        BtnHapus.setMnemonic('H'); BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H"); BtnHapus.setPreferredSize(new Dimension(100, 30));

        BtnKeluar.setIcon(new ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K'); BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K"); BtnKeluar.setPreferredSize(new Dimension(100, 30));

        BtnCariObat.setIcon(new ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCariObat.setPreferredSize(new Dimension(28, 23));
        BtnCariObat.setToolTipText("Cari Obat");

        BtnCari.setIcon(new ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setMnemonic('1'); BtnCari.setToolTipText("Alt+1");
        BtnCari.setPreferredSize(new Dimension(28, 23));

        BtnAll.setIcon(new ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setMnemonic('2'); BtnAll.setToolTipText("Alt+2");
        BtnAll.setPreferredSize(new Dimension(28, 23));

        // events
        BtnSimpan.addActionListener(this::save);
        BtnBaru.addActionListener(e -> baru());
        BtnHapus.addActionListener(this::delete);
        BtnKeluar.addActionListener(e -> dispose());
        BtnCariObat.addActionListener(e -> runCariObat(TCariObat.getText()));
        BtnAll.addActionListener(e -> loadData());

        tbData.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { fillFromRow(); }
        });
        tbCari.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) pilihObat();
            }
        });
        tbCari.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) pilihObat();
            }
        });

        TCariObat.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    runCariObat(TCariObat.getText());
                else if (e.getKeyCode() == KeyEvent.VK_DOWN)
                    tbCari.requestFocus();
            }
        });

        searchTimer = new Timer(300, e -> runCariObat(TCariObat.getText()));
        searchTimer.setRepeats(false);
        TCariObat.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { if (TCariObat.getText().length() > 2) searchTimer.restart(); }
            public void removeUpdate(DocumentEvent e)  { if (TCariObat.getText().length() > 2) searchTimer.restart(); }
            public void changedUpdate(DocumentEvent e) {}
        });
    }

    private void initComponents() {
        widget.InternalFrame frame = new widget.InternalFrame();
        frame.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(240, 245, 235)),
            "::[ Master Restriksi Obat BPJS ]::",
            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
            new Font("Tahoma", Font.PLAIN, 11), new Color(50, 50, 50)
        ));
        frame.setLayout(new BorderLayout(1, 1));

        // === PAGE_START: cari obat + form input ===
        JPanel pTop = new JPanel(new BorderLayout(1, 1));
        pTop.setOpaque(false);

        // -- panel cari obat --
        widget.panelisi pCariObat = new widget.panelisi();
        pCariObat.setPreferredSize(new Dimension(0, 46));
        pCariObat.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        widget.Label lCari = new widget.Label(); lCari.setText("Cari Obat :");
        lCari.setPreferredSize(new Dimension(70, 23));
        TCariObat.setPreferredSize(new Dimension(320, 23));
        widget.Label lNamaLabel = new widget.Label(); lNamaLabel.setText("Terpilih :");
        lNamaLabel.setPreferredSize(new Dimension(60, 23));
        LNama.setPreferredSize(new Dimension(280, 23));
        pCariObat.add(lCari); pCariObat.add(TCariObat); pCariObat.add(BtnCariObat);
        widget.Label lSep = new widget.Label(); lSep.setPreferredSize(new Dimension(10, 23)); pCariObat.add(lSep);
        pCariObat.add(lNamaLabel); pCariObat.add(LNama);

        // -- tabel hasil cari obat --
        widget.ScrollPane spCari = new widget.ScrollPane();
        spCari.setViewportView(tbCari);
        spCari.setPreferredSize(new Dimension(0, 100));

        // -- panel form input --
        widget.panelisi pForm = new widget.panelisi();
        pForm.setPreferredSize(new Dimension(0, 46));
        pForm.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        widget.Label lKode = new widget.Label(); lKode.setText("Kode Barang :");
        lKode.setPreferredSize(new Dimension(85, 23));
        TKode.setPreferredSize(new Dimension(120, 23));

        widget.Label lKdJ = new widget.Label(); lKdJ.setText("KdJenis :");
        lKdJ.setPreferredSize(new Dimension(60, 23));
        TKdJenis.setPreferredSize(new Dimension(70, 23));

        widget.Label lKdPJ = new widget.Label(); lKdPJ.setText("Kd PJ :");
        lKdPJ.setPreferredSize(new Dimension(45, 23));
        TKdPJ.setPreferredSize(new Dimension(180, 23));

        widget.Label lMJml = new widget.Label(); lMJml.setText("Max Jml :");
        lMJml.setPreferredSize(new Dimension(60, 23));
        TMaxJml.setPreferredSize(new Dimension(70, 23));

        widget.Label lMIter = new widget.Label(); lMIter.setText("Max Iter :");
        lMIter.setPreferredSize(new Dimension(60, 23));
        TMaxIter.setPreferredSize(new Dimension(40, 23));

        widget.Label lAkt = new widget.Label(); lAkt.setText("Aktif :");
        lAkt.setPreferredSize(new Dimension(38, 23));
        CbAktif.setPreferredSize(new Dimension(55, 23));

        widget.Label lKet = new widget.Label(); lKet.setText("Keterangan :");
        lKet.setPreferredSize(new Dimension(78, 23));
        TKet.setPreferredSize(new Dimension(160, 23));

        pForm.add(lKode); pForm.add(TKode);
        pForm.add(lKdJ);  pForm.add(TKdJenis);
        pForm.add(lKdPJ); pForm.add(TKdPJ);
        pForm.add(lMJml); pForm.add(TMaxJml);
        pForm.add(lMIter);pForm.add(TMaxIter);
        pForm.add(lAkt);  pForm.add(CbAktif);
        pForm.add(lKet);  pForm.add(TKet);

        // -- panel restriksi text + level faskes --
        widget.panelisi pRestr = new widget.panelisi();
        pRestr.setPreferredSize(new Dimension(0, 76));
        pRestr.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 6));

        widget.Label lRestr = new widget.Label(); lRestr.setText("Restriksi :");
        lRestr.setPreferredSize(new Dimension(70, 23));
        JScrollPane spRestr = new JScrollPane(TRestriksi);
        spRestr.setPreferredSize(new Dimension(560, 60));

        widget.Label lLvl = new widget.Label(); lLvl.setText("Level Faskes :");
        lLvl.setPreferredSize(new Dimension(90, 23));
        CbLevel.setPreferredSize(new Dimension(130, 23));

        pRestr.add(lRestr); pRestr.add(spRestr);
        pRestr.add(lLvl); pRestr.add(CbLevel);

        JPanel pFormAll = new JPanel(new BorderLayout(1,1));
        pFormAll.setOpaque(false);
        pFormAll.add(pForm,  BorderLayout.PAGE_START);
        pFormAll.add(pRestr, BorderLayout.CENTER);

        pTop.add(pCariObat, BorderLayout.PAGE_START);
        pTop.add(spCari,    BorderLayout.CENTER);
        pTop.add(pFormAll,  BorderLayout.PAGE_END);

        frame.add(pTop, BorderLayout.PAGE_START);

        // === CENTER: tabel utama ===
        widget.ScrollPane spData = new widget.ScrollPane();
        spData.setViewportView(tbData);
        frame.add(spData, BorderLayout.CENTER);

        // === PAGE_END: search filter + action buttons ===
        JPanel pBottom = new JPanel(new BorderLayout(1, 1));
        pBottom.setOpaque(false);
        pBottom.setPreferredSize(new Dimension(0, 92));

        // -- search filter --
        widget.panelisi pSearch = new widget.panelisi();
        pSearch.setPreferredSize(new Dimension(0, 44));
        pSearch.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        widget.TextBox TCariFilter = new widget.TextBox();
        widget.Label lKw = new widget.Label(); lKw.setText("Key Word :");
        lKw.setPreferredSize(new Dimension(70, 23));
        TCariFilter.setPreferredSize(new Dimension(250, 23));
        TCariFilter.setDocument(new batasInput((byte) 100).getKata(TCariFilter));
        widget.Label lRec = new widget.Label(); lRec.setText("Record :");
        lRec.setPreferredSize(new Dimension(55, 23));
        LCount.setPreferredSize(new Dimension(50, 23));

        pSearch.add(lKw); pSearch.add(TCariFilter); pSearch.add(BtnCari); pSearch.add(BtnAll);
        pSearch.add(lRec); pSearch.add(LCount);

        TCariFilter.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    runSearch(TCariFilter.getText());
            }
        });
        BtnCari.addActionListener(e -> runSearch(TCariFilter.getText()));

        // -- action buttons --
        widget.panelisi pBtn = new widget.panelisi();
        pBtn.setPreferredSize(new Dimension(0, 44));
        pBtn.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        pBtn.add(BtnSimpan); pBtn.add(BtnBaru); pBtn.add(BtnHapus); pBtn.add(BtnKeluar);

        pBottom.add(pSearch, BorderLayout.PAGE_START);
        pBottom.add(pBtn,    BorderLayout.CENTER);

        frame.add(pBottom, BorderLayout.PAGE_END);

        getContentPane().add(frame, BorderLayout.CENTER);
    }

    // =========================================================
    // BUSINESS LOGIC
    // =========================================================
    private Connection conn() throws Exception { return koneksiDB.condb(); }

    @SuppressWarnings("unchecked")
    private void loadPenjabModel() {
        java.util.List<String> items = new java.util.ArrayList<>();
        items.add("ALL - Semua Penjamin");
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(
                 "select kd_pj, png_jawab from penjab where status='1' order by png_jawab");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(rs.getString(1).trim() + " - " + rs.getString(2));
            }
        } catch (Exception ex) {
            System.out.println("Gagal load penjab: " + ex);
        }
        TKdPJ.setModel(new DefaultComboBoxModel<>(items.toArray(new String[0])));
        TKdPJ.setSelectedIndex(0);
    }

    private String getKdPj() {
        Object sel = TKdPJ.getSelectedItem();
        if (sel == null) return "ALL";
        String s = sel.toString();
        int sep = s.indexOf(" - ");
        return (sep > 0 ? s.substring(0, sep) : s).trim();
    }

    private void setKdPj(String kd) {
        if (kd == null || kd.trim().isEmpty()) kd = "ALL";
        kd = kd.trim();
        for (int i = 0; i < TKdPJ.getItemCount(); i++) {
            String it = TKdPJ.getItemAt(i).toString();
            int sep = it.indexOf(" - ");
            String code = sep > 0 ? it.substring(0, sep).trim() : it.trim();
            if (code.equalsIgnoreCase(kd)) { TKdPJ.setSelectedIndex(i); return; }
        }
        TKdPJ.setSelectedIndex(0);
    }

    private void runCariObat(String kw) {
        tabCari.setRowCount(0);
        if (kw == null || kw.trim().length() < 2) return;
        String sql = "select kode_brng, nama_brng, kdjns from databarang " +
                     "where nama_brng like ? or kode_brng like ? order by nama_brng limit 100";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + kw.trim() + "%");
            ps.setString(2, "%" + kw.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    tabCari.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3)});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void pilihObat() {
        int r = tbCari.getSelectedRow();
        if (r < 0) return;
        TKode.setText(tabCari.getValueAt(r, 0).toString());
        LNama.setText(tabCari.getValueAt(r, 1).toString());
        Object kdjns = tabCari.getValueAt(r, 2);
        if (kdjns != null && !kdjns.toString().trim().isEmpty())
            TKdJenis.setText(kdjns.toString().trim());
        TKdJenis.requestFocus();
    }

    private void loadData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        tabMode.setRowCount(0);
        String sql = "select ro.kode_brng,db.nama_brng,ro.kdjenis,ro.kd_pj,ro.max_jml,ro.max_iterasi," +
                     "ro.aktif,ro.keterangan,ro.restriksi_text,ro.level_faskes " +
                     "from restriksi_obat ro left join databarang db on db.kode_brng=ro.kode_brng " +
                     "order by ro.kode_brng";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                tabMode.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
                                            rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),
                                            rs.getString(9),rs.getString(10)});
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        LCount.setText(String.valueOf(tabMode.getRowCount()));
        setCursor(Cursor.getDefaultCursor());
    }

    private void runSearch(String kw) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        tabMode.setRowCount(0);
        String sql = "select ro.kode_brng,db.nama_brng,ro.kdjenis,ro.kd_pj,ro.max_jml,ro.max_iterasi," +
                     "ro.aktif,ro.keterangan,ro.restriksi_text,ro.level_faskes " +
                     "from restriksi_obat ro left join databarang db on db.kode_brng=ro.kode_brng " +
                     "where ro.kode_brng like ? or db.nama_brng like ? order by ro.kode_brng";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + kw + "%");
            ps.setString(2, "%" + kw + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    tabMode.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
                                                rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),
                                                rs.getString(9),rs.getString(10)});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        LCount.setText(String.valueOf(tabMode.getRowCount()));
        setCursor(Cursor.getDefaultCursor());
    }

    private void save(ActionEvent e) {
        if (TKode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih obat terlebih dahulu dari panel pencarian.");
            TCariObat.requestFocus();
            return;
        }
        String sql = "insert into restriksi_obat(kode_brng,kdjenis,kd_pj,max_jml,max_iterasi,butuh_approval,aktif,keterangan,restriksi_text,level_faskes) " +
                     "values (?,?,?,?,?,?,?,?,?,?) " +
                     "on duplicate key update max_jml=values(max_jml),max_iterasi=values(max_iterasi)," +
                     "butuh_approval=values(butuh_approval),aktif=values(aktif),keterangan=values(keterangan)," +
                     "restriksi_text=values(restriksi_text),level_faskes=values(level_faskes)";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, TKode.getText().trim());
            ps.setString(2, TKdJenis.getText().trim().isEmpty() ? "ALL" : TKdJenis.getText().trim());
            ps.setString(3, getKdPj());
            ps.setObject(4, TMaxJml.getText().trim().isEmpty()  ? null  : Double.parseDouble(TMaxJml.getText().trim()));
            ps.setInt(5, Integer.parseInt(TMaxIter.getText().trim().isEmpty() ? "0" : TMaxIter.getText().trim()));
            ps.setString(6, "N");
            ps.setString(7, CbAktif.getSelectedItem().toString());
            ps.setString(8, TKet.getText());
            ps.setString(9, TRestriksi.getText());
            ps.setInt(10, parseLevel(CbLevel.getSelectedItem().toString()));
            ps.executeUpdate();
            loadData();
            baru();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void delete(ActionEvent e) {
        if (TKode.getText().trim().isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Hapus restriksi obat " + TKode.getText() + " ?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(
                 "delete from restriksi_obat where kode_brng=? and kdjenis=? and kd_pj=?")) {
            ps.setString(1, TKode.getText().trim());
            ps.setString(2, TKdJenis.getText().trim().isEmpty() ? "ALL" : TKdJenis.getText().trim());
            ps.setString(3, getKdPj());
            ps.executeUpdate();
            loadData();
            baru();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void baru() {
        TCariObat.setText(""); tabCari.setRowCount(0);
        TKode.setText(""); LNama.setText("-");
        TKdJenis.setText("ALL"); setKdPj("ALL");
        TMaxJml.setText(""); TMaxIter.setText("0");
        CbAktif.setSelectedItem("Y");
        TKet.setText("");
        TRestriksi.setText("");
        CbLevel.setSelectedIndex(0);
        TCariObat.requestFocus();
    }

    private int parseLevel(String s) {
        if (s == null || s.isEmpty()) return 0;
        char c = s.charAt(0);
        return (c >= '0' && c <= '9') ? (c - '0') : 0;
    }

    private void setLevelByValue(int lvl) {
        for (int i = 0; i < CbLevel.getItemCount(); i++) {
            if (parseLevel(CbLevel.getItemAt(i).toString()) == lvl) {
                CbLevel.setSelectedIndex(i); return;
            }
        }
        CbLevel.setSelectedIndex(0);
    }

    private void fillFromRow() {
        int r = tbData.getSelectedRow();
        if (r < 0) return;
        TKode.setText(str(tabMode.getValueAt(r, 0)));
        LNama.setText(str(tabMode.getValueAt(r, 1)));
        TKdJenis.setText(str(tabMode.getValueAt(r, 2)));
        setKdPj(str(tabMode.getValueAt(r, 3)));
        TMaxJml.setText(str(tabMode.getValueAt(r, 4)));
        TMaxIter.setText(str(tabMode.getValueAt(r, 5)));
        CbAktif.setSelectedItem(str(tabMode.getValueAt(r, 6)));
        TKet.setText(str(tabMode.getValueAt(r, 7)));
        TRestriksi.setText(str(tabMode.getValueAt(r, 8)));
        String lvl = str(tabMode.getValueAt(r, 9));
        try { setLevelByValue(lvl.isEmpty() ? 0 : Integer.parseInt(lvl)); }
        catch(Exception ex) { CbLevel.setSelectedIndex(0); }
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }
}
