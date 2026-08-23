package inventory;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Master data Obat Formularium Nasional (Fornas).
 * Admin mencentang obat mana yang termasuk Fornas — data disimpan ke
 * tabel {@code obat_fornas} (kolom terpisah, TIDAK mengubah struktur
 * {@code databarang} untuk keamanan menyimpantf).
 *
 * Fitur:
 * - Filter search by kode / nama / jenis
 * - Filter status: Semua / Hanya Fornas / Bukan Fornas
 * - Klik cell "Fornas" → toggle status langsung tersimpan
 * - Field restriksi & keterangan bisa diedit inline
 * - Bulk: tombol "Tandai Semua Selected sebagai Fornas" dan sebaliknya
 */
public class DlgObatFornas extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;

    public DlgObatFornas(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setSize(1150, 640);
        setLocationRelativeTo(parent);

        tabMode = new DefaultTableModel(null, new Object[]{
            "Fornas","Kode","Nama Obat","Jenis","Satuan","Restriksi","Keterangan"
        }) {
            @Override public boolean isCellEditable(int r, int c) {
                return c == 0 || c == 5 || c == 6; // Fornas + restriksi + keterangan bisa diubah
            }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : String.class;
            }
        };
        tbData.setModel(tabMode);
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbData.setPreferredScrollableViewportSize(new Dimension(900, 450));
        int[] widths = {60, 110, 320, 120, 60, 220, 200};
        for (int i = 0; i < widths.length; i++) {
            TableColumn c = tbData.getColumnModel().getColumn(i);
            c.setPreferredWidth(widths[i]);
        }
        tbData.setDefaultRenderer(String.class, new WarnaTable());

        // Listener perubahan cell → auto-save ke DB
        tabMode.addTableModelListener(e -> {
            if (e.getType() != javax.swing.event.TableModelEvent.UPDATE) return;
            int r = e.getFirstRow();
            int c = e.getColumn();
            if (r < 0 || c < 0 || r >= tabMode.getRowCount()) return;
            saveRow(r);
        });

        cmbFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "Semua Obat", "Hanya Fornas", "Bukan Fornas"
        }));

        loadData();
    }

    // =====================================================================
    // UI
    // =====================================================================
    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        panelTop      = new widget.panelisi();
        panelToolbar  = new widget.panelisi();
        Scroll        = new widget.ScrollPane();
        tbData        = new widget.Table();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setTitle("Master Obat Fornas");

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240,245,235)),
            "::[ Master Obat Formularium Nasional (Fornas) — Filter Otomatis untuk Pasien BPJS ]::",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new java.awt.Font("Tahoma",0,11), new java.awt.Color(50,50,50)));
        internalFrame1.setLayout(new java.awt.BorderLayout(1,1));

        panelTop.setPreferredSize(new Dimension(100, 45));
        panelTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10));

        widget.Label lblCari = new widget.Label(); lblCari.setText("Kata Kunci :");
        lblCari.setPreferredSize(new Dimension(75, 23));
        TCari = new widget.TextBox(); TCari.setPreferredSize(new Dimension(260, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) loadData();
            }
        });
        widget.Label lblF = new widget.Label(); lblF.setText("Tampilkan :");
        lblF.setPreferredSize(new Dimension(70, 23));
        cmbFilter = new widget.ComboBox(); cmbFilter.setPreferredSize(new Dimension(150, 23));

        BtnCari = new widget.Button(); BtnCari.setText("Cari"); BtnCari.setMnemonic('C');
        BtnCari.setPreferredSize(new Dimension(80, 23));
        BtnCari.addActionListener(e -> loadData());

        panelTop.add(lblCari); panelTop.add(TCari);
        panelTop.add(lblF); panelTop.add(cmbFilter);
        panelTop.add(BtnCari);

        internalFrame1.add(panelTop, java.awt.BorderLayout.PAGE_START);

        Scroll.setViewportView(tbData);
        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelToolbar.setPreferredSize(new Dimension(100, 45));
        panelToolbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10));

        BtnPilihSemua = new widget.Button(); BtnPilihSemua.setText("☑ Ceklis Semua Fornas");
        BtnPilihSemua.setPreferredSize(new Dimension(180, 25));
        BtnPilihSemua.addActionListener(e -> ceklisSemuaFornas(true));

        BtnUncheckSemua = new widget.Button(); BtnUncheckSemua.setText("☐ Hapus Semua Ceklis");
        BtnUncheckSemua.setPreferredSize(new Dimension(180, 25));
        BtnUncheckSemua.addActionListener(e -> ceklisSemuaFornas(false));

        BtnBulkFornas = new widget.Button(); BtnBulkFornas.setText("✓ Tandai Fornas (selected)");
        BtnBulkFornas.setPreferredSize(new Dimension(200, 25));
        BtnBulkFornas.addActionListener(e -> bulkSet(true));

        BtnBulkNon = new widget.Button(); BtnBulkNon.setText("✕ Hapus Fornas (selected)");
        BtnBulkNon.setPreferredSize(new Dimension(200, 25));
        BtnBulkNon.addActionListener(e -> bulkSet(false));

        LblStatus = new widget.Label();
        LblStatus.setPreferredSize(new Dimension(430, 25));
        LblStatus.setFont(new java.awt.Font("Tahoma", 1, 12));

        BtnKeluar = new widget.Button(); BtnKeluar.setText("Keluar"); BtnKeluar.setMnemonic('K');
        BtnKeluar.setPreferredSize(new Dimension(100, 25));
        BtnKeluar.addActionListener(e -> dispose());

        panelToolbar.add(BtnPilihSemua);
        panelToolbar.add(BtnUncheckSemua);
        panelToolbar.add(BtnBulkFornas);
        panelToolbar.add(BtnBulkNon);
        panelToolbar.add(LblStatus);
        panelToolbar.add(BtnKeluar);
        internalFrame1.add(panelToolbar, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        pack();
    }

    // =====================================================================
    // DATA
    // =====================================================================
    private volatile boolean loading = false;

    private void loadData() {
        loading = true; // supaya tableModelListener tidak trigger save saat reload
        Valid.tabelKosong(tabMode);
        String kata = "%" + TCari.getText().trim() + "%";
        int mode = cmbFilter.getSelectedIndex();
        String filterFornas;
        switch (mode) {
            case 1: filterFornas = "and of.kode_brng is not null"; break;
            case 2: filterFornas = "and of.kode_brng is null";     break;
            default: filterFornas = "";                             break;
        }
        String sql =
            "select db.kode_brng, db.nama_brng, "+
            "  ifnull(jenis.nama,'') as nm_jenis, "+
            "  ifnull(kodesatuan.satuan,'') as satuan, "+
            "  of.kode_brng as is_fornas, "+
            "  ifnull(of.restriksi,'')  as restriksi, "+
            "  ifnull(of.keterangan,'') as keterangan "+
            "from databarang db "+
            "left join jenis      on jenis.kdjns=db.kdjns "+
            "left join kodesatuan on kodesatuan.kode_sat=db.kode_sat "+
            "left join obat_fornas of on of.kode_brng=db.kode_brng "+
            "where db.status='1' " + filterFornas + " "+
            "and (db.kode_brng like ? or db.nama_brng like ? or ifnull(jenis.nama,'') like ?) "+
            "order by db.nama_brng limit 5000";
        int cntFornas = 0, cntTotal = 0;
        Connection cx = null;
        PreparedStatement lp = null;
        ResultSet lr = null;
        try {
            cx = koneksiDB.condb();
            try { cx.setAutoCommit(true); } catch (Exception ignore) {}
            lp = cx.prepareStatement(sql);
            lp.setString(1, kata); lp.setString(2, kata); lp.setString(3, kata);
            lr = lp.executeQuery();
            while (lr.next()) {
                boolean isFornas = lr.getString("is_fornas") != null;
                if (isFornas) cntFornas++;
                cntTotal++;
                tabMode.addRow(new Object[]{
                    isFornas,
                    lr.getString("kode_brng"),
                    lr.getString("nama_brng"),
                    lr.getString("nm_jenis"),
                    lr.getString("satuan"),
                    lr.getString("restriksi"),
                    lr.getString("keterangan")
                });
            }
            LblStatus.setText("Total: " + cntTotal + " obat  ·  Fornas: " + cntFornas +
                              "  ·  Non-Fornas: " + (cntTotal - cntFornas));
        } catch (Exception e) {
            System.out.println("Notif loadData : " + e);
            JOptionPane.showMessageDialog(this, "Gagal load: " + e.getMessage());
        } finally {
            try { if (lr != null) lr.close(); } catch (Exception ignore) {}
            try { if (lp != null) lp.close(); } catch (Exception ignore) {}
            loading = false;
        }
    }

    /** Simpan perubahan 1 baris ke tabel obat_fornas.
     *  Pakai Connection LOKAL supaya tidak konflik dengan operasi lain. */
    private void saveRow(int r) {
        if (loading) return;
        if (r < 0 || r >= tabMode.getRowCount()) return;
        String kode      = (String) tabMode.getValueAt(r, 1);
        if (kode == null || kode.trim().isEmpty()) return;
        boolean fornas   = Boolean.TRUE.equals(tabMode.getValueAt(r, 0));
        String restriksi = tabMode.getValueAt(r, 5) == null ? "" : tabMode.getValueAt(r, 5).toString();
        String keter     = tabMode.getValueAt(r, 6) == null ? "" : tabMode.getValueAt(r, 6).toString();
        Connection cx = null;
        PreparedStatement lp = null;
        try {
            cx = koneksiDB.condb();
            try { cx.setAutoCommit(true); } catch (Exception ignore) {}
            if (fornas) {
                lp = cx.prepareStatement(
                    "insert into obat_fornas (kode_brng, restriksi, keterangan) values (?,?,?) "+
                    "on duplicate key update restriksi=values(restriksi), keterangan=values(keterangan)");
                lp.setString(1, kode);
                lp.setString(2, restriksi);
                lp.setString(3, keter);
                lp.executeUpdate();
            } else {
                lp = cx.prepareStatement("delete from obat_fornas where kode_brng=?");
                lp.setString(1, kode);
                lp.executeUpdate();
            }
            System.out.println("saveRow OK  kode="+kode+"  fornas="+fornas);
        } catch (Exception e) {
            System.out.println("Notif saveRow ["+kode+"] : " + e);
            JOptionPane.showMessageDialog(this, "Gagal simpan "+kode+": " + e.getMessage());
        } finally {
            try { if (lp != null) lp.close(); } catch (Exception ignore) {}
        }
    }

    /** Ceklis / uncheck kolom Fornas untuk SEMUA baris yang tampil sekarang, langsung simpan (batch).
     *  Pakai Connection LOKAL supaya autoCommit tidak bocor ke operasi lain. */
    private void ceklisSemuaFornas(boolean fornas) {
        int total = tabMode.getRowCount();
        if (total == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data yang bisa diceklis.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                (fornas ? "Ceklis " : "Hapus ceklis ") + total + " obat sebagai Fornas?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loading = true;
        Connection cx = null;
        PreparedStatement psIns = null, psDel = null;
        int berhasil = 0;
        try {
            cx = koneksiDB.condb();
            cx.setAutoCommit(false);
            psIns = cx.prepareStatement("insert ignore into obat_fornas (kode_brng) values (?)");
            psDel = cx.prepareStatement("delete from obat_fornas where kode_brng=?");
            for (int r = 0; r < total; r++) {
                String kode = (String) tabMode.getValueAt(r, 1);
                if (kode == null || kode.trim().isEmpty()) continue;
                tabMode.setValueAt(fornas, r, 0);
                if (fornas) { psIns.setString(1, kode); psIns.addBatch(); }
                else        { psDel.setString(1, kode); psDel.addBatch(); }
                berhasil++;
            }
            if (fornas) psIns.executeBatch(); else psDel.executeBatch();
            cx.commit();
            System.out.println("ceklisSemuaFornas OK  fornas="+fornas+"  total="+berhasil);
        } catch (Exception e) {
            try { if (cx != null) cx.rollback(); } catch (Exception ignore) {}
            System.out.println("Notif ceklisSemuaFornas : " + e);
            JOptionPane.showMessageDialog(this, "Gagal: " + e.getMessage());
        } finally {
            try { if (psIns != null) psIns.close(); } catch (Exception ignore) {}
            try { if (psDel != null) psDel.close(); } catch (Exception ignore) {}
            try { if (cx != null) cx.setAutoCommit(true); } catch (Exception ignore) {}
            loading = false;
            setCursor(Cursor.getDefaultCursor());
        }
        // reload SETELAH finally (setelah loading=false) supaya listener tidak double-trigger
        JOptionPane.showMessageDialog(this,
            (fornas?"Berhasil tandai ":"Berhasil hapus tanda ") + berhasil + " obat sebagai Fornas.");
        loadData();
    }

    /** Bulk set semua row yang selected. */
    private void bulkSet(boolean fornas) {
        int[] rows = tbData.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Pilih dulu baris (Ctrl+klik untuk multi-select).");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                (fornas ? "Tandai " : "Hapus dari ") + "Fornas untuk " + rows.length + " obat?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            koneksi = koneksiDB.condb();
            PreparedStatement psIns = koneksi.prepareStatement(
                "insert ignore into obat_fornas (kode_brng) values (?)");
            PreparedStatement psDel = koneksi.prepareStatement(
                "delete from obat_fornas where kode_brng=?");
            for (int r : rows) {
                String kode = (String) tabMode.getValueAt(r, 1);
                if (fornas) { psIns.setString(1, kode); psIns.executeUpdate(); }
                else        { psDel.setString(1, kode); psDel.executeUpdate(); }
            }
            psIns.close(); psDel.close();
            JOptionPane.showMessageDialog(this, "Berhasil update " + rows.length + " obat.");
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal bulk: " + e.getMessage());
        }
    }

    public void isCek() {
        BtnBulkFornas.setEnabled(akses.getdarurat_stok());
        BtnBulkNon.setEnabled(akses.getdarurat_stok());
    }

    private void closeRs() {
        try { if (rs != null) rs.close(); } catch (Exception ignore) {}
        try { if (ps != null) ps.close(); } catch (Exception ignore) {}
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgObatFornas d = new DlgObatFornas(new javax.swing.JFrame(), true);
            d.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
            });
            d.setVisible(true);
        });
    }

    private widget.InternalFrame internalFrame1;
    private widget.panelisi panelTop, panelToolbar;
    private widget.ScrollPane Scroll;
    private widget.Table tbData;
    private widget.ComboBox cmbFilter;
    private widget.TextBox TCari;
    private widget.Button BtnCari, BtnBulkFornas, BtnBulkNon, BtnKeluar, BtnPilihSemua, BtnUncheckSemua;
    private widget.Label LblStatus;
}
