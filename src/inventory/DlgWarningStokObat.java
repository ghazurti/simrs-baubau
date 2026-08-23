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
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Peringatan Stok Obat Kritis (dinamis).
 *
 * Berbeda dengan {@link DlgDaruratStok} yang membandingkan stok dengan
 * `databarang.stokminimal` (nilai statis), dialog ini menghitung
 * rata-rata pemakaian obat dari {@code detail_pemberian_obat} beberapa
 * bulan terakhir dan menandai obat yang stoknya diprediksi tidak
 * cukup ~1 bulan ke depan.
 *
 * Basis default: 3 bulan terakhir (dapat diubah lewat combobox periode).
 * Filter: bangsal/gudang tertentu atau seluruh gudang.
 */
public class DlgWarningStokObat extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private final NumberFormat fmt = NumberFormat.getInstance(new Locale("id","ID"));
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private final java.util.List<String> kodeBangsal = new java.util.ArrayList<>();

    public DlgWarningStokObat(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setSize(1100, 640);
        setLocationRelativeTo(parent);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No","Kode","Nama Obat","Jenis","Stok Sekarang","Satuan",
            "Rata2 / Bulan","Rata2 / Hari","Sisa Hari","Stok Minimal","Status"
        }) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbData.setModel(tabMode);
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbData.setPreferredScrollableViewportSize(new Dimension(900, 400));
        int[] widths = {45, 90, 260, 100, 100, 60, 100, 100, 80, 90, 130};
        for (int i = 0; i < widths.length; i++) {
            TableColumn c = tbData.getColumnModel().getColumn(i);
            c.setPreferredWidth(widths[i]);
        }
        tbData.setDefaultRenderer(Object.class, new WarnaTable());
        tbData.getColumnModel().getColumn(10).setCellRenderer(new StatusRenderer());
        // Center-align kolom angka
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int idx : new int[]{0,4,6,7,8,9}) {
            tbData.getColumnModel().getColumn(idx).setCellRenderer(center);
        }

        cmbBulan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "1 bulan terakhir", "3 bulan terakhir", "6 bulan terakhir", "12 bulan terakhir"
        }));
        cmbBulan.setSelectedIndex(1); // default 3 bulan

        cmbFilterStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "Hanya Kritis (sisa < 1 bulan)",
            "Kritis + Sangat Kritis (sisa < 2 minggu)",
            "Sangat Kritis saja (sisa < 1 minggu)",
            "Semua obat (termasuk aman)"
        }));
        cmbFilterStatus.setSelectedIndex(0);

        loadBangsal();
        runBackground(this::prosesCari);
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
        setTitle("Peringatan Stok Obat");

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240,245,235)),
            "::[ Peringatan Stok Obat (berdasarkan pemakaian aktual) ]::",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new java.awt.Font("Tahoma",0,11), new java.awt.Color(50,50,50)));
        internalFrame1.setLayout(new java.awt.BorderLayout(1,1));

        panelTop.setPreferredSize(new Dimension(100, 45));
        panelTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10));

        widget.Label lblG = new widget.Label(); lblG.setText("Gudang :");
        lblG.setPreferredSize(new Dimension(55, 23));
        cmbBangsal = new widget.ComboBox(); cmbBangsal.setPreferredSize(new Dimension(220, 23));
        widget.Label lblP = new widget.Label(); lblP.setText("Periode :");
        lblP.setPreferredSize(new Dimension(55, 23));
        cmbBulan = new widget.ComboBox(); cmbBulan.setPreferredSize(new Dimension(150, 23));
        widget.Label lblS = new widget.Label(); lblS.setText("Filter :");
        lblS.setPreferredSize(new Dimension(45, 23));
        cmbFilterStatus = new widget.ComboBox(); cmbFilterStatus.setPreferredSize(new Dimension(240, 23));

        BtnCari = new widget.Button(); BtnCari.setText("Cari"); BtnCari.setMnemonic('C');
        BtnCari.setPreferredSize(new Dimension(80, 23));
        BtnCari.addActionListener(e -> runBackground(this::prosesCari));

        panelTop.add(lblG); panelTop.add(cmbBangsal);
        panelTop.add(lblP); panelTop.add(cmbBulan);
        panelTop.add(lblS); panelTop.add(cmbFilterStatus);
        panelTop.add(BtnCari);

        internalFrame1.add(panelTop, java.awt.BorderLayout.PAGE_START);

        Scroll.setViewportView(tbData);
        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelToolbar.setPreferredSize(new Dimension(100, 45));
        panelToolbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10));

        BtnPrint  = new widget.Button(); BtnPrint.setText("Cetak"); BtnPrint.setMnemonic('T');
        BtnPrint.setPreferredSize(new Dimension(100, 25));
        BtnPrint.addActionListener(e -> cetak());
        BtnKeluar = new widget.Button(); BtnKeluar.setText("Keluar"); BtnKeluar.setMnemonic('K');
        BtnKeluar.setPreferredSize(new Dimension(100, 25));
        BtnKeluar.addActionListener(e -> dispose());
        LblRingkasan = new widget.Label();
        LblRingkasan.setPreferredSize(new Dimension(700, 25));
        LblRingkasan.setFont(new java.awt.Font("Tahoma", 1, 12));

        panelToolbar.add(BtnPrint);
        panelToolbar.add(LblRingkasan);
        panelToolbar.add(BtnKeluar);
        internalFrame1.add(panelToolbar, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        pack();
    }

    /** Renderer kolom Status: warna latar sesuai tingkat kritis. */
    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            String s = v == null ? "" : v.toString();
            java.awt.Color bg, fg = java.awt.Color.WHITE;
            if      (s.contains("SANGAT KRITIS")) bg = new java.awt.Color(198, 40, 40);   // merah
            else if (s.contains("KRITIS"))        bg = new java.awt.Color(255, 143, 0);   // oranye
            else if (s.contains("PERHATIAN"))     bg = new java.awt.Color(255, 202, 40);  // kuning
            else if (s.contains("AMAN"))          bg = new java.awt.Color(76, 175, 80);   // hijau
            else if (s.contains("BELUM DIPAKAI")) { bg = new java.awt.Color(238, 238, 238); fg = new java.awt.Color(97,97,97); }
            else                                  bg = t.getBackground();
            if (sel) { l.setBackground(t.getSelectionBackground()); l.setForeground(t.getSelectionForeground()); }
            else     { l.setBackground(bg); l.setForeground(fg); }
            l.setOpaque(true);
            return l;
        }
    }

    // =====================================================================
    // DATA
    // =====================================================================
    private void loadBangsal() {
        cmbBangsal.removeAllItems();
        kodeBangsal.clear();
        cmbBangsal.addItem("-- Semua Gudang --");
        kodeBangsal.add("");
        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(
                "select kd_bangsal, nm_bangsal from bangsal where status='1' order by nm_bangsal");
            rs = ps.executeQuery();
            while (rs.next()) {
                kodeBangsal.add(rs.getString("kd_bangsal"));
                cmbBangsal.addItem(rs.getString("nm_bangsal") + " (" + rs.getString("kd_bangsal") + ")");
            }
        } catch (Exception e) {
            System.out.println("Notif loadBangsal : " + e);
        } finally { closeRs(); }
    }

    private int getBulanPeriode() {
        switch (cmbBulan.getSelectedIndex()) {
            case 0: return 1;
            case 1: return 3;
            case 2: return 6;
            case 3: return 12;
            default: return 3;
        }
    }

    /**
     * Query utama: LEFT JOIN databarang ke agregat stok per bangsal
     * dan agregat pemakaian dari detail_pemberian_obat N bulan terakhir.
     */
    private void prosesCari() {
        Valid.tabelKosong(tabMode);
        int bulan = getBulanPeriode();
        int idx = cmbBangsal.getSelectedIndex();
        String kdBangsal = (idx <= 0 || idx >= kodeBangsal.size()) ? "" : kodeBangsal.get(idx);
        int filterMode = cmbFilterStatus.getSelectedIndex();

        String filterStok, filterPakai;
        if (kdBangsal.isEmpty()) {
            filterStok  = "";
            filterPakai = "";
        } else {
            filterStok  = " where gb.kd_bangsal='" + esc(kdBangsal) + "'";
            filterPakai = " and dpo.kd_bangsal='" + esc(kdBangsal) + "'";
        }

        String sql =
            "select db.kode_brng, db.nama_brng, db.stokminimal, "+
            "  ifnull(jenis.nama,'') as nm_jenis, "+
            "  ifnull(kodesatuan.satuan,'') as satuan, "+
            "  ifnull(gb.stok_sekarang,0) as stok_sekarang, "+
            "  ifnull(pmk.qty_total,0)    as qty_total "+
            "from databarang db "+
            "left join jenis      on jenis.kdjns=db.kdjns "+
            "left join kodesatuan on kodesatuan.kode_sat=db.kode_sat "+
            "left join (select kode_brng, sum(stok) as stok_sekarang "+
            "           from gudangbarang gb " + filterStok + " group by kode_brng) gb "+
            "     on gb.kode_brng=db.kode_brng "+
            "left join (select dpo.kode_brng, sum(dpo.jml) as qty_total "+
            "           from detail_pemberian_obat dpo "+
            "           where dpo.tgl_perawatan >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) "+
            filterPakai + " group by dpo.kode_brng) pmk "+
            "     on pmk.kode_brng=db.kode_brng "+
            "where db.status='1'";

        try {
            koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(sql);
            ps.setInt(1, bulan);
            rs = ps.executeQuery();
            int no = 1, cntSangat = 0, cntKritis = 0, cntPerhatian = 0, cntAman = 0, cntBelum = 0;
            while (rs.next()) {
                double stok    = rs.getDouble("stok_sekarang");
                double qtyTot  = rs.getDouble("qty_total");
                double qtyBln  = bulan > 0 ? (qtyTot / bulan) : 0;
                double qtyHri  = qtyBln / 30.0;
                double sisaHr  = qtyHri > 0 ? (stok / qtyHri) : Double.MAX_VALUE;
                double stokMin = rs.getDouble("stokminimal");

                String status;
                if (qtyBln <= 0)             { status = "BELUM DIPAKAI"; cntBelum++; }
                else if (sisaHr < 7)         { status = "SANGAT KRITIS"; cntSangat++; }
                else if (sisaHr < 30)        { status = "KRITIS";        cntKritis++; }
                else if (sisaHr < 60)        { status = "PERHATIAN";     cntPerhatian++; }
                else                          { status = "AMAN";          cntAman++; }

                // Apply filter mode
                boolean tampil;
                switch (filterMode) {
                    case 0: tampil = "KRITIS".equals(status) || "SANGAT KRITIS".equals(status); break;
                    case 1: tampil = "KRITIS".equals(status) || "SANGAT KRITIS".equals(status); break; // sama utk sekarang
                    case 2: tampil = "SANGAT KRITIS".equals(status); break;
                    case 3: tampil = true; break;
                    default: tampil = true;
                }
                if (filterMode == 1) tampil = "KRITIS".equals(status) || "SANGAT KRITIS".equals(status);
                if (!tampil) continue;

                tabMode.addRow(new Object[]{
                    no++,
                    rs.getString("kode_brng"),
                    rs.getString("nama_brng"),
                    rs.getString("nm_jenis"),
                    fmt.format(stok),
                    rs.getString("satuan"),
                    fmt.format(Math.round(qtyBln * 10.0) / 10.0),
                    fmt.format(Math.round(qtyHri * 100.0) / 100.0),
                    sisaHr == Double.MAX_VALUE ? "-" : (sisaHr > 999 ? "999+" : fmt.format(Math.round(sisaHr * 10.0) / 10.0)),
                    fmt.format(stokMin),
                    status
                });
            }
            LblRingkasan.setText(String.format(
                "Sangat Kritis: %d  ·  Kritis: %d  ·  Perhatian: %d  ·  Aman: %d  ·  Belum dipakai: %d  ·  (basis: %d bulan)",
                cntSangat, cntKritis, cntPerhatian, cntAman, cntBelum, bulan));
        } catch (Exception e) {
            System.out.println("Notif prosesCari : " + e);
            JOptionPane.showMessageDialog(this, "Gagal query: " + e.getMessage());
        } finally { closeRs(); }
    }

    // =====================================================================
    // CETAK — pakai temporary + jasper generic seperti pola Khanza
    // =====================================================================
    private void cetak() {
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk dicetak."); return;
        }
        try {
            Sequel.queryu("delete from temporary where temp37='" + akses.getalamatip() + "'");
            for (int r = 0; r < tabMode.getRowCount(); r++) {
                Sequel.menyimpan("temporary","'" + r + "','" +
                    esc(tabMode.getValueAt(r,0).toString()) + "','" +
                    esc(tabMode.getValueAt(r,1).toString()) + "','" +
                    esc(tabMode.getValueAt(r,2).toString()) + "','" +
                    esc(tabMode.getValueAt(r,3).toString()) + "','" +
                    esc(tabMode.getValueAt(r,4).toString()) + "','" +
                    esc(tabMode.getValueAt(r,5).toString()) + "','" +
                    esc(tabMode.getValueAt(r,6).toString()) + "','" +
                    esc(tabMode.getValueAt(r,7).toString()) + "','" +
                    esc(tabMode.getValueAt(r,8).toString()) + "','" +
                    esc(tabMode.getValueAt(r,9).toString()) + "','" +
                    esc(tabMode.getValueAt(r,10).toString()) +
                    "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','" +
                    akses.getalamatip() + "'","Warning Stok Obat");
            }
            Map<String,Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("periode", "Basis pemakaian: " + getBulanPeriode() + " bulan terakhir");
            param.put("gudang", cmbBangsal.getSelectedItem() == null ? "-" : cmbBangsal.getSelectedItem().toString());
            Valid.MyReportqry("rptWarningStokObat.jasper", "report",
                "::[ Peringatan Stok Obat ]::",
                "select * from temporary where temporary.temp37='" + akses.getalamatip() +
                "' order by cast(temporary.no as unsigned)", param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal cetak: " + e.getMessage());
        }
    }

    // =====================================================================
    // UTIL
    // =====================================================================
    public void isCek() {
        BtnPrint.setEnabled(akses.getdarurat_stok());
    }

    private static String esc(String s) { return s == null ? "" : s.replace("'", "''"); }
    private void closeRs() {
        try { if (rs != null) rs.close(); } catch (Exception ignore) {}
        try { if (ps != null) ps.close(); } catch (Exception ignore) {}
    }

    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) return;
        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            executor.submit(() -> {
                try { task.run(); }
                finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> { if (isDisplayable()) setCursor(Cursor.getDefaultCursor()); });
                }
            });
        } catch (RejectedExecutionException ex) { ceksukses = false; }
    }

    @Override public void dispose() { executor.shutdownNow(); super.dispose(); }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgWarningStokObat d = new DlgWarningStokObat(new javax.swing.JFrame(), true);
            d.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
            });
            d.setVisible(true);
        });
    }

    // Variables
    private widget.InternalFrame internalFrame1;
    private widget.panelisi panelTop, panelToolbar;
    private widget.ScrollPane Scroll;
    private widget.Table tbData;
    private widget.ComboBox cmbBangsal, cmbBulan, cmbFilterStatus;
    private widget.Button BtnCari, BtnPrint, BtnKeluar;
    private widget.Label LblRingkasan;
}
