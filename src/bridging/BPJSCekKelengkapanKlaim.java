package bridging;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class BPJSCekKelengkapanKlaim extends javax.swing.JDialog {

    private DefaultTableModel tabModel;
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ===== KOMPONEN UI =====
    private JPanel panelJudul, panelFilter, panelTable, panelLegend;
    private JLabel lblJudul, lblDari, lblSampai, lblJenis, lblCari;
    private JLabel lblTotal, lblLengkap, lblTidakLengkap;
    private com.toedter.calendar.JDateChooser dcDari, dcSampai;
    private JComboBox<String> cbJenis;
    private JTextField tfCari;
    private JButton btnCari, btnTutup;
    private JTable tbData;
    private JScrollPane scrollPane;

    public BPJSCekKelengkapanKlaim(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setupTable();
        setupEvents();
        setTanggalDefault();
        tampilkanData();
    }

    private void initComponents() {
        setTitle("Cek Kelengkapan Data Klaim BPJS");
        setLayout(new java.awt.BorderLayout(5, 5));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ---- Panel Judul ----
        panelJudul = new JPanel();
        panelJudul.setBackground(new Color(31, 97, 141));
        panelJudul.setPreferredSize(new Dimension(0, 50));
        lblJudul = new JLabel("CEK KELENGKAPAN DATA KLAIM BPJS");
        lblJudul.setForeground(Color.WHITE);
        lblJudul.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelJudul.add(lblJudul);
        add(panelJudul, java.awt.BorderLayout.NORTH);

        // ---- Panel Filter ----
        panelFilter = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 5));
        panelFilter.setBorder(BorderFactory.createTitledBorder("Filter"));

        lblDari = new JLabel("Tanggal SEP Dari:");
        dcDari = new com.toedter.calendar.JDateChooser();
        dcDari.setPreferredSize(new Dimension(120, 25));
        dcDari.setDateFormatString("yyyy-MM-dd");

        lblSampai = new JLabel("s/d");
        dcSampai = new com.toedter.calendar.JDateChooser();
        dcSampai.setPreferredSize(new Dimension(120, 25));
        dcSampai.setDateFormatString("yyyy-MM-dd");

        lblJenis = new JLabel("Jenis:");
        cbJenis = new JComboBox<>(new String[]{"Semua", "Ralan", "Ranap"});
        cbJenis.setPreferredSize(new Dimension(90, 25));

        lblCari = new JLabel("Cari:");
        tfCari = new JTextField(15);
        tfCari.setDocument(new batasInput((byte) 100).getKata(tfCari));

        btnCari = new JButton("Tampilkan");
        btnCari.setBackground(new Color(31, 97, 141));
        btnCari.setForeground(Color.WHITE);
        btnCari.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnCari.setOpaque(true);
        btnCari.setBorderPainted(false);

        btnTutup = new JButton("Tutup");
        btnTutup.setBackground(new Color(192, 57, 43));
        btnTutup.setForeground(Color.WHITE);
        btnTutup.setOpaque(true);
        btnTutup.setBorderPainted(false);

        panelFilter.add(lblDari);
        panelFilter.add(dcDari);
        panelFilter.add(lblSampai);
        panelFilter.add(dcSampai);
        panelFilter.add(lblJenis);
        panelFilter.add(cbJenis);
        panelFilter.add(lblCari);
        panelFilter.add(tfCari);
        panelFilter.add(btnCari);
        panelFilter.add(btnTutup);

        // ---- Panel Legend ----
        panelLegend = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 3));
        JLabel lblLegendTitle = new JLabel("Keterangan: ");
        lblLegendTitle.setFont(new Font("SansSerif", Font.BOLD, 11));

        JLabel lblHijau = new JLabel("  ██  Lengkap");
        lblHijau.setForeground(new Color(39, 174, 96));
        lblHijau.setFont(new Font("SansSerif", Font.BOLD, 11));

        JLabel lblMerah = new JLabel("  ██  Tidak Lengkap");
        lblMerah.setForeground(new Color(192, 57, 43));
        lblMerah.setFont(new Font("SansSerif", Font.BOLD, 11));

        lblTotal = new JLabel("Total: 0");
        lblLengkap = new JLabel("| Lengkap: 0");
        lblTidakLengkap = new JLabel("| Tidak Lengkap: 0");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblLengkap.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblTidakLengkap.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblTidakLengkap.setForeground(new Color(192, 57, 43));

        panelLegend.add(lblLegendTitle);
        panelLegend.add(lblHijau);
        panelLegend.add(lblMerah);
        panelLegend.add(Box.createHorizontalStrut(30));
        panelLegend.add(lblTotal);
        panelLegend.add(lblLengkap);
        panelLegend.add(lblTidakLengkap);

        JPanel panelNorth = new JPanel(new java.awt.BorderLayout());
        panelNorth.add(panelJudul, java.awt.BorderLayout.NORTH);
        panelNorth.add(panelFilter, java.awt.BorderLayout.CENTER);
        panelNorth.add(panelLegend, java.awt.BorderLayout.SOUTH);

        getContentPane().setLayout(new java.awt.BorderLayout(5, 5));
        getContentPane().add(panelNorth, java.awt.BorderLayout.NORTH);

        // ---- Table ----
        tabModel = new DefaultTableModel(null, new Object[]{
            "No", "No.Rawat", "No.RM", "Nama Pasien", "Jenis", "Tgl.SEP",
            "SEP", "Diagnosa", "Prosedur", "Resume", "Tgl.Pulang", "Status"
        }) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tbData = new JTable(tabModel);
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbData.setPreferredScrollableViewportSize(new Dimension(900, 400));
        tbData.setRowHeight(22);
        tbData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPane = new JScrollPane(tbData);
        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

        setSize(1100, 650);
        setLocationRelativeTo(null);
    }

    private void setupTable() {
        int[] widths = {35, 120, 90, 200, 60, 100, 60, 70, 70, 70, 100, 120};
        String[] cols = {"No","No.Rawat","No.RM","Nama Pasien","Jenis","Tgl.SEP",
                         "SEP","Diagnosa","Prosedur","Resume","Tgl.Pulang","Status"};
        for (int i = 0; i < cols.length; i++) {
            TableColumn col = tbData.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
        }

        tbData.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    Object status = table.getValueAt(row, 11);
                    if ("LENGKAP".equals(status)) {
                        c.setBackground(new Color(212, 239, 223));
                    } else {
                        c.setBackground(new Color(250, 219, 216));
                    }
                }
                // Kolom status bold
                if (column == 11) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    Object status = table.getValueAt(row, 11);
                    if ("LENGKAP".equals(status)) {
                        c.setForeground(new Color(39, 174, 96));
                    } else {
                        c.setForeground(new Color(192, 57, 43));
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }

    private void setupEvents() {
        btnCari.addActionListener(e -> tampilkanData());

        btnTutup.addActionListener(e -> dispose());

        tfCari.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) tampilkanData();
            }
            @Override public void keyPressed(KeyEvent e) {}
        });

        addWindowListener(new WindowListener() {
            @Override public void windowClosing(WindowEvent e) { executor.shutdownNow(); }
            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e) {}
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });
    }

    private void setTanggalDefault() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        dcSampai.setDate(cal.getTime());
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        dcDari.setDate(cal.getTime());
    }

    private void tampilkanData() {
        if (dcDari.getDate() == null || dcSampai.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Tanggal harus diisi!");
            return;
        }

        executor.submit(() -> {
            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                tabModel.setRowCount(0);
            });

            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                String tglDari = sdf.format(dcDari.getDate());
                String tglSampai = sdf.format(dcSampai.getDate());
                String jenis = cbJenis.getSelectedItem().toString();
                String cari = tfCari.getText().trim();

                String sqlJenis = "";
                if ("Ralan".equals(jenis)) sqlJenis = " AND bs.jnspelayanan='2'";
                else if ("Ranap".equals(jenis)) sqlJenis = " AND bs.jnspelayanan='1'";

                String sqlCari = "";
                if (!cari.isEmpty()) {
                    sqlCari = " AND (bs.no_sep LIKE ? OR bs.no_rawat LIKE ? OR bs.nomr LIKE ? OR bs.nama_pasien LIKE ?)";
                }

                String sql =
                    "SELECT " +
                    "  bs.no_rawat, bs.nomr, bs.nama_pasien," +
                    "  IF(bs.jnspelayanan='1','Ranap','Ralan') AS jenis," +
                    "  bs.tglsep, bs.no_sep," +
                    "  (SELECT COUNT(*) FROM diagnosa_pasien dp WHERE dp.no_rawat=bs.no_rawat) AS jml_diagnosa," +
                    "  (SELECT COUNT(*) FROM prosedur_pasien pp WHERE pp.no_rawat=bs.no_rawat) AS jml_prosedur," +
                    "  IF(bs.jnspelayanan='1'," +
                    "    (SELECT COUNT(*) FROM resume_pasien_ranap rr WHERE rr.no_rawat=bs.no_rawat)," +
                    "    (SELECT COUNT(*) FROM resume_pasien rp WHERE rp.no_rawat=bs.no_rawat)" +
                    "  ) AS ada_resume," +
                    "  IF(bs.jnspelayanan='1', bs.tglpulang, NULL) AS tgl_pulang" +
                    " FROM bridging_sep bs" +
                    " WHERE bs.tglsep BETWEEN ? AND ?" +
                    sqlJenis + sqlCari +
                    " ORDER BY bs.tglsep DESC, bs.no_rawat";

                ps = koneksi.prepareStatement(sql);
                int idx = 1;
                ps.setString(idx++, tglDari);
                ps.setString(idx++, tglSampai);
                if (!cari.isEmpty()) {
                    String like = "%" + cari + "%";
                    ps.setString(idx++, like);
                    ps.setString(idx++, like);
                    ps.setString(idx++, like);
                    ps.setString(idx++, like);
                }

                rs = ps.executeQuery();

                int no = 0, totalLengkap = 0, totalTidakLengkap = 0;
                while (rs.next()) {
                    no++;
                    String noRawat   = rs.getString("no_rawat");
                    String noRM      = rs.getString("nomr");
                    String nama      = rs.getString("nama_pasien");
                    String jenisStr  = rs.getString("jenis");
                    String tglSep    = rs.getString("tglsep");
                    String noSep     = rs.getString("no_sep");
                    int jmlDiagnosa  = rs.getInt("jml_diagnosa");
                    int jmlProsedur  = rs.getInt("jml_prosedur");
                    int adaResume    = rs.getInt("ada_resume");
                    String tglPulang = rs.getString("tgl_pulang");
                    boolean isRanap  = "Ranap".equals(jenisStr);

                    String sepStatus    = noSep != null && !noSep.isEmpty() ? "Ada" : "Tidak";
                    String diagnosaSts  = jmlDiagnosa > 0 ? "Ada" : "Tidak";
                    String prosedurSts  = isRanap ? (jmlProsedur > 0 ? "Ada" : "Tidak") : (jmlProsedur > 0 ? "Ada" : "-");
                    String resumeSts    = adaResume > 0 ? "Ada" : "Tidak";
                    String tglPulangSts = isRanap ? (tglPulang != null && !tglPulang.isEmpty() ? tglPulang : "Belum") : "-";

                    boolean lengkap;
                    if (isRanap) {
                        lengkap = "Ada".equals(sepStatus) && jmlDiagnosa > 0
                                && jmlProsedur > 0 && adaResume > 0
                                && tglPulang != null && !tglPulang.isEmpty();
                    } else {
                        lengkap = "Ada".equals(sepStatus) && jmlDiagnosa > 0
                                && adaResume > 0;
                    }

                    String status = lengkap ? "LENGKAP" : "TIDAK LENGKAP";
                    if (lengkap) totalLengkap++; else totalTidakLengkap++;

                    final Object[] row = new Object[]{
                        no, noRawat, noRM, nama, jenisStr, tglSep,
                        sepStatus, diagnosaSts, prosedurSts, resumeSts,
                        tglPulangSts, status
                    };
                    SwingUtilities.invokeLater(() -> tabModel.addRow(row));
                }

                final int fTotal = no;
                final int fLengkap = totalLengkap;
                final int fTidak = totalTidakLengkap;
                SwingUtilities.invokeLater(() -> {
                    lblTotal.setText("Total: " + fTotal);
                    lblLengkap.setText("| Lengkap: " + fLengkap);
                    lblTidakLengkap.setText("| Tidak Lengkap: " + fTidak);
                    setCursor(Cursor.getDefaultCursor());
                    if (fTotal == 0) {
                        JOptionPane.showMessageDialog(BPJSCekKelengkapanKlaim.this,
                            "Tidak ada data SEP pada rentang tanggal tersebut.\n" +
                            "Pastikan data SEP sudah ada di tabel bridging_sep.",
                            "Data Kosong", JOptionPane.INFORMATION_MESSAGE);
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(BPJSCekKelengkapanKlaim.this,
                        "Error mengambil data:\n" + ex.getMessage() +
                        "\n\nPeriksa koneksi database dan nama tabel.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }
}
