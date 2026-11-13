package rekammedis;

import fungsi.koneksiDB;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Monitoring isian RME & jam datang dokter, dipisah Rawat Jalan dan Rawat Inap.
 *
 * Jam datang = login SIMRS pertama (tabel tracker). Bila dokter tidak login,
 * dipakai SOAP pertama yang ditulis dokter sendiri, lalu order pertama atas nama dokter
 * (resep/lab/rad - bisa saja diketik perawat, jadi ditandai).
 * Hanya untuk Admin Utama.
 */
public final class RMMonitoringRMEDokter extends javax.swing.JDialog {

    private final validasi Valid = new validasi();
    private final widget.Tanggal DTPCari1 = new widget.Tanggal(), DTPCari2 = new widget.Tanggal();
    private final widget.TextBox TCari = new widget.TextBox(), TToleransi = new widget.TextBox();
    private final widget.TabPane tab = new widget.TabPane();
    private final JLabel LInfo = new JLabel(" ");

    private static final String[] KOLOM_RALAN = {
        "Tanggal", "Kode", "Dokter", "Poli", "Jadwal Mulai", "Login Pertama", "SOAP Pertama (Dokter)", "Order Pertama (a.n. Dokter)",
        "Isian Terakhir", "Jam Datang", "Sumber Jam Datang", "Terlambat (menit)", "Pasien", "SOAP oleh Dokter", "SOAP oleh Perawat",
        "% SOAP Dokter", "Resep", "Lab", "Radiologi", "Tindakan", "Resume"
    };
    private static final int[] LEBAR_RALAN = {75, 60, 200, 150, 75, 75, 95, 105, 75, 70, 115, 80, 50, 75, 80, 75, 50, 40, 60, 55, 50};

    private static final String[] KOLOM_RANAP = {
        "Tanggal", "Kode", "Dokter", "Login Pertama", "SOAP/CPPT Pertama (Dokter)", "Order Pertama (a.n. Dokter)", "Isian Terakhir",
        "Jam Datang", "Sumber Jam Datang", "Pasien DPJP Dirawat", "CPPT oleh Dokter", "% CPPT Dokter", "Visite/Tindakan",
        "Resep", "Lab", "Radiologi", "Resume Pulang"
    };
    private static final int[] LEBAR_RANAP = {75, 60, 200, 75, 120, 105, 75, 70, 115, 95, 75, 75, 75, 50, 40, 60, 75};

    private static final String[] KOLOM_DETAIL = {"Jam", "Jenis Isian", "No.Rawat", "Pasien", "Isi"};
    private static final int[] LEBAR_DETAIL = {65, 170, 120, 180, 900};

    private final Bagian ralan = new Bagian(true), ranap = new Bagian(false);

    public RMMonitoringRMEDokter(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Monitoring RME & Kedatangan Dokter");
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        widget.InternalFrame frame = new widget.InternalFrame();
        frame.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(240, 245, 235)),
                "::[ Monitoring RME & Kedatangan Dokter ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new Color(50, 50, 50)));
        frame.setLayout(new BorderLayout(1, 1));

        tab.addTab(".: Rawat Jalan :.", ralan.panel);
        tab.addTab(".: Rawat Inap :.", ranap.panel);
        tab.addChangeListener(e -> tampilAktif());
        frame.add(tab, BorderLayout.CENTER);

        widget.panelisi bawah = new widget.panelisi();
        bawah.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        bawah.setPreferredSize(new Dimension(44, 44));
        bawah.add(label("Tanggal :", 55));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setPreferredSize(new Dimension(90, 23));
        bawah.add(DTPCari1);
        bawah.add(label("s.d.", 25));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setPreferredSize(new Dimension(90, 23));
        bawah.add(DTPCari2);
        bawah.add(label("Dokter/Poli :", 75));
        TCari.setPreferredSize(new Dimension(180, 23));
        TCari.addActionListener(e -> tampilAktif());
        bawah.add(TCari);
        bawah.add(label("Toleransi :", 62));
        TToleransi.setText("30");
        TToleransi.setPreferredSize(new Dimension(35, 23));
        TToleransi.setToolTipText("Toleransi keterlambatan dari jam mulai jadwal praktek (menit)");
        bawah.add(TToleransi);
        bawah.add(label("menit", 35));
        bawah.add(tombol("Tampilkan", "/picture/accept.png", e -> tampilAktif()));
        bawah.add(tombol("Simpan Excel", "/picture/Search-16x16.png", e -> simpanExcel()));
        bawah.add(tombol("Keluar", "/picture/exit.png", e -> dispose()));
        bawah.add(LInfo);
        frame.add(bawah, BorderLayout.PAGE_END);

        getContentPane().add(frame, BorderLayout.CENTER);
        setSize(1000, 650);
    }

    private static widget.Label label(String teks, int lebar) {
        widget.Label l = new widget.Label();
        l.setText(teks);
        l.setPreferredSize(new Dimension(lebar, 23));
        return l;
    }

    private static widget.Button tombol(String teks, String ikon, java.awt.event.ActionListener aksi) {
        widget.Button b = new widget.Button();
        b.setIcon(new javax.swing.ImageIcon(RMMonitoringRMEDokter.class.getResource(ikon)));
        b.setText(teks);
        b.setPreferredSize(new Dimension(teks.length() * 7 + 40, 30));
        b.addActionListener(aksi);
        return b;
    }

    private int toleransi() {
        try {
            return Integer.parseInt(TToleransi.getText().trim());
        } catch (Exception e) {
            return 30;
        }
    }

    private void tampilAktif() {
        (tab.getSelectedIndex() == 0 ? ralan : ranap).tampil();
    }

    // ------------------------------------------------------------------ bagian per tab
    private final class Bagian {
        final boolean isRalan;
        final DefaultTableModel modelRingkas, modelDetail;
        final widget.Table tbRingkas = new widget.Table(), tbDetail = new widget.Table();
        final JPanel panel = new JPanel(new BorderLayout());
        final JLabel judulDetail = new JLabel(" Detail isian : pilih baris dokter di atas");

        Bagian(boolean isRalan) {
            this.isRalan = isRalan;
            String[] kolom = isRalan ? KOLOM_RALAN : KOLOM_RANAP;
            modelRingkas = new DefaultTableModel(null, kolom) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            modelDetail = new DefaultTableModel(null, KOLOM_DETAIL) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            siapkan(tbRingkas, modelRingkas, isRalan ? LEBAR_RALAN : LEBAR_RANAP);
            siapkan(tbDetail, modelDetail, LEBAR_DETAIL);
            tbRingkas.setDefaultRenderer(Object.class, new Pewarna(isRalan));
            tbRingkas.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    tampilDetail();
                }
            });

            widget.ScrollPane s1 = new widget.ScrollPane();
            s1.setViewportView(tbRingkas);
            widget.ScrollPane s2 = new widget.ScrollPane();
            s2.setViewportView(tbDetail);
            JPanel bawah = new JPanel(new BorderLayout());
            judulDetail.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));
            bawah.add(judulDetail, BorderLayout.NORTH);
            bawah.add(s2, BorderLayout.CENTER);
            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, s1, bawah);
            split.setResizeWeight(0.6);
            split.setDividerLocation(330);
            panel.add(split, BorderLayout.CENTER);
            JLabel ket = new JLabel(isRalan
                    ? " Jam Datang = login SIMRS pertama; bila tidak login dipakai SOAP pertama dokter, lalu order pertama (resep/lab/rad, bisa diketik perawat). Merah = terlambat melebihi toleransi / SOAP dokter < 50% pasien."
                    : " Jam Datang = login SIMRS pertama; bila tidak login dipakai CPPT pertama dokter, lalu order pertama a.n. dokter. Merah = CPPT oleh dokter < 50% pasien DPJP.");
            ket.setFont(new java.awt.Font("Tahoma", 0, 11));
            ket.setForeground(new Color(90, 90, 90));
            panel.add(ket, BorderLayout.NORTH);
        }

        private void siapkan(widget.Table t, DefaultTableModel m, int[] lebar) {
            t.setModel(m);
            t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < lebar.length; i++) {
                t.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
            }
        }

        void tampil() {
            final String t1 = Valid.SetTgl(DTPCari1.getSelectedItem() + ""), t2 = Valid.SetTgl(DTPCari2.getSelectedItem() + "");
            final String cari = "%" + TCari.getText().trim() + "%";
            final int tol = toleransi();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            LInfo.setText("Memuat data...");
            new Thread(() -> {
                List<Object[]> baris = new ArrayList<>();
                String error = null;
                try {
                    Connection k = koneksiDB.condb();
                    try (PreparedStatement ps = k.prepareStatement(isRalan ? SQL_RALAN : SQL_RANAP)) {
                        int n = 1;
                        if (!isRalan) {
                            for (int i = 0; i < 6; i++) {
                                ps.setString(n++, t1);
                                ps.setString(n++, t2);
                            }
                            ps.setString(n++, cari);
                        } else {
                            ps.setString(n++, t1);
                            ps.setString(n++, t2);
                            ps.setString(n++, cari);
                            ps.setString(n++, cari);
                        }
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                baris.add(isRalan ? barisRalan(rs, tol) : barisRanap(rs));
                            }
                        }
                    }
                } catch (Exception e) {
                    error = e.getMessage();
                    System.out.println("Notif Monitoring RME : " + e);
                }
                final String err = error;
                SwingUtilities.invokeLater(() -> {
                    Valid.tabelKosong(modelRingkas);
                    Valid.tabelKosong(modelDetail);
                    for (Object[] b : baris) {
                        modelRingkas.addRow(b);
                    }
                    LInfo.setText("Record : " + baris.size());
                    setCursor(Cursor.getDefaultCursor());
                    if (err != null) {
                        JOptionPane.showMessageDialog(null, "Gagal memuat data : " + err);
                    }
                });
            }).start();
        }

        void tampilDetail() {
            int r = tbRingkas.getSelectedRow();
            if (r < 0) {
                return;
            }
            final String tgl = modelRingkas.getValueAt(r, 0).toString();
            final String kd = modelRingkas.getValueAt(r, 1).toString();
            judulDetail.setText(" Detail isian : " + modelRingkas.getValueAt(r, 2) + "  -  " + tgl);
            new Thread(() -> {
                List<Object[]> baris = new ArrayList<>();
                try {
                    Connection k = koneksiDB.condb();
                    String sql = isRalan ? SQL_DETAIL_RALAN : SQL_DETAIL_RANAP;
                    try (PreparedStatement ps = k.prepareStatement(sql)) {
                        int jml = ps.getParameterMetaData().getParameterCount();
                        // pola parameter selalu (kd_dokter, tanggal) berulang
                        for (int i = 1; i <= jml; i++) {
                            ps.setString(i, i % 2 == 1 ? kd : tgl);
                        }
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                baris.add(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif Detail RME : " + e);
                }
                SwingUtilities.invokeLater(() -> {
                    Valid.tabelKosong(modelDetail);
                    for (Object[] b : baris) {
                        modelDetail.addRow(b);
                    }
                });
            }).start();
        }
    }

    // ------------------------------------------------------------------ olah baris
    private static String jam(ResultSet rs, String kolom) throws Exception {
        String s = rs.getString(kolom);
        if (s == null || s.startsWith("99:99")) {
            return ""; // penanda kosong dari least(...)
        }
        return s.length() > 8 ? s.substring(0, 8) : s;
    }

    private static String terkecil(String... jam) {
        String hasil = "";
        for (String j : jam) {
            if (!j.isEmpty() && (hasil.isEmpty() || j.compareTo(hasil) < 0)) {
                hasil = j;
            }
        }
        return hasil;
    }

    private static String terbesar(String... jam) {
        String hasil = "";
        for (String j : jam) {
            if (!j.isEmpty() && j.compareTo(hasil) > 0) {
                hasil = j;
            }
        }
        return hasil;
    }

    private static String persen(int a, int b) {
        return b <= 0 ? "-" : Math.round(a * 100.0 / b) + "%";
    }

    private static Object[] barisRalan(ResultSet rs, int tol) throws Exception {
        String login = jam(rs, "login"), soap = jam(rs, "soap_min");
        String order = terkecil(jam(rs, "resep_min"), jam(rs, "lab_min"), jam(rs, "rad_min"), jam(rs, "tind_min_raw"));
        String akhir = terbesar(jam(rs, "soap_max"), jam(rs, "resep_max"), jam(rs, "lab_max"), jam(rs, "rad_max"), jam(rs, "tind_max"));
        String datang, sumber;
        if (!login.isEmpty()) {
            datang = login;
            sumber = "Login SIMRS";
        } else if (!soap.isEmpty()) {
            datang = soap;
            sumber = "SOAP dokter (tdk login)";
        } else if (!order.isEmpty()) {
            datang = order;
            sumber = "Order a.n. dokter *";
        } else {
            datang = "";
            sumber = "Tidak ada isian";
        }
        String jadwal = jam(rs, "jadwal"), telat = "";
        if (!jadwal.isEmpty() && !datang.isEmpty()) {
            long menit = java.time.Duration.between(LocalTime.parse(jadwal), LocalTime.parse(datang)).toMinutes();
            telat = menit > 0 ? String.valueOf(menit) : "0";
        }
        int pasien = rs.getInt("pasien"), soapDr = rs.getInt("soap_dr");
        return new Object[]{
            rs.getString("tgl"), rs.getString("kd_dokter"), rs.getString("nm_dokter"), rs.getString("poli"), jadwal, login, soap, order,
            akhir, datang, sumber, telat, pasien, soapDr, rs.getInt("soap_pr"), persen(soapDr, pasien),
            rs.getInt("resep"), rs.getInt("lab"), rs.getInt("rad"), rs.getInt("tindakan"), rs.getInt("resume")
        };
    }

    private static Object[] barisRanap(ResultSet rs) throws Exception {
        String login = jam(rs, "login"), soap = jam(rs, "soap_min");
        String order = terkecil(jam(rs, "resep_min"), jam(rs, "lab_min"), jam(rs, "rad_min"), jam(rs, "visite_min_raw"));
        String akhir = terbesar(jam(rs, "soap_max"), jam(rs, "resep_max"), jam(rs, "visite_max"));
        String datang, sumber;
        if (!login.isEmpty()) {
            datang = login;
            sumber = "Login SIMRS";
        } else if (!soap.isEmpty()) {
            datang = soap;
            sumber = "CPPT dokter (tdk login)";
        } else if (!order.isEmpty()) {
            datang = order;
            sumber = "Order a.n. dokter *";
        } else {
            datang = "";
            sumber = "Tidak ada isian";
        }
        int dpjp = rs.getInt("pasien_dpjp"), soapDr = rs.getInt("soap_dr");
        return new Object[]{
            rs.getString("tgl"), rs.getString("kd_dokter"), rs.getString("nm_dokter"), login, soap, order, akhir, datang, sumber,
            dpjp, soapDr, persen(soapDr, dpjp), rs.getInt("visite"), rs.getInt("resep"), rs.getInt("lab"), rs.getInt("rad"), rs.getInt("resume")
        };
    }

    /** Merah: terlambat melebihi toleransi / persentase SOAP dokter di bawah 50%. */
    private final class Pewarna extends DefaultTableCellRenderer {
        private final boolean isRalan;

        Pewarna(boolean isRalan) {
            this.isRalan = isRalan;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean fokus, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, fokus, r, c);
            boolean merah = false;
            String nama = t.getColumnName(c);
            String isi = v == null ? "" : v.toString();
            if (nama.startsWith("Terlambat") && !isi.isEmpty()) {
                try {
                    merah = Integer.parseInt(isi) > toleransi();
                } catch (Exception e) {
                    merah = false;
                }
            } else if (nama.startsWith("%") && isi.endsWith("%")) {
                merah = Integer.parseInt(isi.replace("%", "")) < 50;
            } else if (nama.startsWith("Sumber") && (isi.endsWith("*") || isi.startsWith("Tidak"))) {
                merah = true;
            }
            if (!sel) {
                comp.setBackground(r % 2 == 1 ? new Color(245, 250, 240) : Color.WHITE);
                comp.setForeground(merah ? new Color(200, 0, 0) : new Color(50, 50, 50));
            }
            comp.setFont(comp.getFont().deriveFont(merah ? java.awt.Font.BOLD : java.awt.Font.PLAIN));
            return comp;
        }
    }

    // ------------------------------------------------------------------ simpan ke Excel (CSV)
    private void simpanExcel() {
        Bagian b = tab.getSelectedIndex() == 0 ? ralan : ranap;
        if (b.modelRingkas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Data masih kosong, klik Tampilkan dulu.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Monitoring_RME_Dokter_" + (b.isRalan ? "Ralan" : "Ranap") + "_"
                + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "_sd_" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fc.getSelectedFile()), StandardCharsets.UTF_8))) {
            w.print('﻿'); // BOM agar Excel membaca UTF-8
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < b.modelRingkas.getColumnCount(); c++) {
                sb.append(c > 0 ? ";" : "").append(csv(b.modelRingkas.getColumnName(c)));
            }
            w.println(sb);
            for (int r = 0; r < b.modelRingkas.getRowCount(); r++) {
                sb.setLength(0);
                for (int c = 0; c < b.modelRingkas.getColumnCount(); c++) {
                    Object v = b.modelRingkas.getValueAt(r, c);
                    sb.append(c > 0 ? ";" : "").append(csv(v == null ? "" : v.toString()));
                }
                w.println(sb);
            }
            JOptionPane.showMessageDialog(null, "Tersimpan : " + fc.getSelectedFile().getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan : " + e.getMessage());
        }
    }

    private static String csv(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    // ------------------------------------------------------------------ SQL
    private static final String SQL_RALAN =
        "select r.tgl_registrasi tgl, r.kd_dokter, d.nm_dokter, group_concat(distinct pl.nm_poli separator ', ') poli, count(distinct r.no_rawat) pasien,"
        + " (select min(j.jam_mulai) from jadwal j where j.kd_dokter=r.kd_dokter and j.hari_kerja=ELT(DAYOFWEEK(r.tgl_registrasi),'AKHAD','SENIN','SELASA','RABU','KAMIS','JUMAT','SABTU')) jadwal,"
        + " (select min(t.jam_login) from tracker t where t.nip=r.kd_dokter and t.tgl_login=r.tgl_registrasi) login,"
        + " (select count(distinct p.no_rawat) from pemeriksaan_ralan p inner join reg_periksa r2 on r2.no_rawat=p.no_rawat where r2.kd_dokter=r.kd_dokter and r2.tgl_registrasi=r.tgl_registrasi and r2.status_lanjut='Ralan' and p.nip=r.kd_dokter) soap_dr,"
        + " (select count(distinct p.no_rawat) from pemeriksaan_ralan p inner join reg_periksa r2 on r2.no_rawat=p.no_rawat where r2.kd_dokter=r.kd_dokter and r2.tgl_registrasi=r.tgl_registrasi and r2.status_lanjut='Ralan' and p.nip<>r.kd_dokter) soap_pr,"
        + " (select min(p.jam_rawat) from pemeriksaan_ralan p where p.nip=r.kd_dokter and p.tgl_perawatan=r.tgl_registrasi) soap_min,"
        + " (select max(p.jam_rawat) from pemeriksaan_ralan p where p.nip=r.kd_dokter and p.tgl_perawatan=r.tgl_registrasi) soap_max,"
        + " (select count(*) from resep_obat o where o.kd_dokter=r.kd_dokter and o.tgl_peresepan=r.tgl_registrasi and o.status='ralan') resep,"
        + " (select min(o.jam_peresepan) from resep_obat o where o.kd_dokter=r.kd_dokter and o.tgl_peresepan=r.tgl_registrasi and o.status='ralan') resep_min,"
        + " (select max(o.jam_peresepan) from resep_obat o where o.kd_dokter=r.kd_dokter and o.tgl_peresepan=r.tgl_registrasi and o.status='ralan') resep_max,"
        + " (select count(*) from permintaan_lab l where l.dokter_perujuk=r.kd_dokter and l.tgl_permintaan=r.tgl_registrasi and l.status='ralan') lab,"
        + " (select min(l.jam_permintaan) from permintaan_lab l where l.dokter_perujuk=r.kd_dokter and l.tgl_permintaan=r.tgl_registrasi and l.status='ralan') lab_min,"
        + " (select max(l.jam_permintaan) from permintaan_lab l where l.dokter_perujuk=r.kd_dokter and l.tgl_permintaan=r.tgl_registrasi and l.status='ralan') lab_max,"
        + " (select count(*) from permintaan_radiologi l where l.dokter_perujuk=r.kd_dokter and l.tgl_permintaan=r.tgl_registrasi and l.status='ralan') rad,"
        + " (select min(l.jam_permintaan) from permintaan_radiologi l where l.dokter_perujuk=r.kd_dokter and l.tgl_permintaan=r.tgl_registrasi and l.status='ralan') rad_min,"
        + " (select max(l.jam_permintaan) from permintaan_radiologi l where l.dokter_perujuk=r.kd_dokter and l.tgl_permintaan=r.tgl_registrasi and l.status='ralan') rad_max,"
        + " (select count(*) from rawat_jl_dr v where v.kd_dokter=r.kd_dokter and v.tgl_perawatan=r.tgl_registrasi)+(select count(*) from rawat_jl_drpr v where v.kd_dokter=r.kd_dokter and v.tgl_perawatan=r.tgl_registrasi) tindakan,"
        + " least(ifnull((select min(v.jam_rawat) from rawat_jl_dr v where v.kd_dokter=r.kd_dokter and v.tgl_perawatan=r.tgl_registrasi),'99:99:99'),"
        + "       ifnull((select min(v.jam_rawat) from rawat_jl_drpr v where v.kd_dokter=r.kd_dokter and v.tgl_perawatan=r.tgl_registrasi),'99:99:99')) tind_min_raw,"
        + " greatest(ifnull((select max(v.jam_rawat) from rawat_jl_dr v where v.kd_dokter=r.kd_dokter and v.tgl_perawatan=r.tgl_registrasi),''),"
        + "       ifnull((select max(v.jam_rawat) from rawat_jl_drpr v where v.kd_dokter=r.kd_dokter and v.tgl_perawatan=r.tgl_registrasi),'')) tind_max,"
        + " (select count(*) from resume_pasien rp inner join reg_periksa r2 on r2.no_rawat=rp.no_rawat where rp.kd_dokter=r.kd_dokter and r2.tgl_registrasi=r.tgl_registrasi) resume"
        + " from reg_periksa r inner join dokter d on d.kd_dokter=r.kd_dokter inner join poliklinik pl on pl.kd_poli=r.kd_poli"
        + " where r.status_lanjut='Ralan' and r.stts<>'Batal' and r.tgl_registrasi between ? and ? and (d.nm_dokter like ? or pl.nm_poli like ?)"
        + " group by r.tgl_registrasi, r.kd_dokter order by r.tgl_registrasi, d.nm_dokter";

    private static final String SQL_RANAP =
        "select x.tgl, x.kd kd_dokter, d.nm_dokter,"
        + " (select min(t.jam_login) from tracker t where t.nip=x.kd and t.tgl_login=x.tgl) login,"
        + " (select count(distinct dp.no_rawat) from dpjp_ranap dp inner join kamar_inap ki on ki.no_rawat=dp.no_rawat where dp.kd_dokter=x.kd and ki.tgl_masuk<=x.tgl"
        + "   and (ki.tgl_keluar>=x.tgl or ki.tgl_keluar='0000-00-00' or ki.tgl_keluar is null)) pasien_dpjp,"
        + " (select count(distinct p.no_rawat) from pemeriksaan_ranap p where p.nip=x.kd and p.tgl_perawatan=x.tgl) soap_dr,"
        + " (select min(p.jam_rawat) from pemeriksaan_ranap p where p.nip=x.kd and p.tgl_perawatan=x.tgl) soap_min,"
        + " (select max(p.jam_rawat) from pemeriksaan_ranap p where p.nip=x.kd and p.tgl_perawatan=x.tgl) soap_max,"
        + " (select count(*) from rawat_inap_dr v where v.kd_dokter=x.kd and v.tgl_perawatan=x.tgl)+(select count(*) from rawat_inap_drpr v where v.kd_dokter=x.kd and v.tgl_perawatan=x.tgl) visite,"
        + " least(ifnull((select min(v.jam_rawat) from rawat_inap_dr v where v.kd_dokter=x.kd and v.tgl_perawatan=x.tgl),'99:99:99'),"
        + "       ifnull((select min(v.jam_rawat) from rawat_inap_drpr v where v.kd_dokter=x.kd and v.tgl_perawatan=x.tgl),'99:99:99')) visite_min_raw,"
        + " greatest(ifnull((select max(v.jam_rawat) from rawat_inap_dr v where v.kd_dokter=x.kd and v.tgl_perawatan=x.tgl),''),"
        + "       ifnull((select max(v.jam_rawat) from rawat_inap_drpr v where v.kd_dokter=x.kd and v.tgl_perawatan=x.tgl),'')) visite_max,"
        + " (select count(*) from resep_obat o where o.kd_dokter=x.kd and o.tgl_peresepan=x.tgl and o.status='ranap') resep,"
        + " (select min(o.jam_peresepan) from resep_obat o where o.kd_dokter=x.kd and o.tgl_peresepan=x.tgl and o.status='ranap') resep_min,"
        + " (select max(o.jam_peresepan) from resep_obat o where o.kd_dokter=x.kd and o.tgl_peresepan=x.tgl and o.status='ranap') resep_max,"
        + " (select count(*) from permintaan_lab l where l.dokter_perujuk=x.kd and l.tgl_permintaan=x.tgl and l.status='ranap') lab,"
        + " (select min(l.jam_permintaan) from permintaan_lab l where l.dokter_perujuk=x.kd and l.tgl_permintaan=x.tgl and l.status='ranap') lab_min,"
        + " (select count(*) from permintaan_radiologi l where l.dokter_perujuk=x.kd and l.tgl_permintaan=x.tgl and l.status='ranap') rad,"
        + " (select min(l.jam_permintaan) from permintaan_radiologi l where l.dokter_perujuk=x.kd and l.tgl_permintaan=x.tgl and l.status='ranap') rad_min,"
        + " (select count(distinct rp.no_rawat) from resume_pasien_ranap rp inner join kamar_inap ki on ki.no_rawat=rp.no_rawat where rp.kd_dokter=x.kd and ki.tgl_keluar=x.tgl and ki.stts_pulang<>'Pindah Kamar') resume"
        + " from ("
        + "   select p.nip kd, p.tgl_perawatan tgl from pemeriksaan_ranap p inner join dokter dd on dd.kd_dokter=p.nip where p.tgl_perawatan between ? and ?"
        + "   union select kd_dokter, tgl_perawatan from rawat_inap_dr where tgl_perawatan between ? and ?"
        + "   union select kd_dokter, tgl_perawatan from rawat_inap_drpr where tgl_perawatan between ? and ?"
        + "   union select kd_dokter, tgl_peresepan from resep_obat where status='ranap' and tgl_peresepan between ? and ?"
        + "   union select dokter_perujuk, tgl_permintaan from permintaan_lab where status='ranap' and tgl_permintaan between ? and ?"
        + "   union select dokter_perujuk, tgl_permintaan from permintaan_radiologi where status='ranap' and tgl_permintaan between ? and ?"
        + " ) x inner join dokter d on d.kd_dokter=x.kd where d.nm_dokter like ? order by x.tgl, d.nm_dokter";

    // detail: setiap blok memakai parameter (kd_dokter, tanggal) berurutan
    private static final String SQL_DETAIL_RALAN =
        "select * from ("
        + " select cast(t.jam_login as char) jam,'Login SIMRS' jenis,'' no_rawat,'' pasien,'Login aplikasi SIMRS' isi from tracker t where t.nip=? and t.tgl_login=?"
        + " union all select cast(p.jam_rawat as char),'SOAP oleh Dokter',p.no_rawat,ps.nm_pasien,"
        + "   concat('S: ',ifnull(p.keluhan,''),' | O: ',ifnull(p.pemeriksaan,''),' | A: ',ifnull(p.penilaian,''),' | P: ',ifnull(p.rtl,''),' | Instruksi: ',ifnull(p.instruksi,''))"
        + "   from pemeriksaan_ralan p inner join reg_periksa r on r.no_rawat=p.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where p.nip=? and p.tgl_perawatan=?"
        + " union all select cast(p.jam_rawat as char),concat('SOAP oleh ',ifnull(pg.nama,p.nip)),p.no_rawat,ps.nm_pasien,"
        + "   concat('S: ',ifnull(p.keluhan,''),' | O: ',ifnull(p.pemeriksaan,''),' | A: ',ifnull(p.penilaian,''),' | P: ',ifnull(p.rtl,''))"
        + "   from pemeriksaan_ralan p inner join reg_periksa r on r.no_rawat=p.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis left join pegawai pg on pg.nik=p.nip"
        + "   where r.kd_dokter=? and r.tgl_registrasi=? and r.status_lanjut='Ralan' and p.nip<>r.kd_dokter"
        + " union all select cast(o.jam_peresepan as char),'Resep (a.n. dokter)',o.no_rawat,ps.nm_pasien,"
        + "   concat(o.no_resep,' : ',ifnull((select group_concat(concat(b.nama_brng,' ',rd.jml,' (',ifnull(rd.aturan_pakai,''),')') separator '; ') from resep_dokter rd inner join databarang b on b.kode_brng=rd.kode_brng where rd.no_resep=o.no_resep),'racikan'))"
        + "   from resep_obat o inner join reg_periksa r on r.no_rawat=o.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where o.kd_dokter=? and o.tgl_peresepan=? and o.status='ralan'"
        + " union all select cast(l.jam_permintaan as char),'Permintaan Lab',l.no_rawat,ps.nm_pasien,"
        + "   concat(l.noorder,' : ',ifnull((select group_concat(j.nm_perawatan separator '; ') from permintaan_pemeriksaan_lab pp inner join jns_perawatan_lab j on j.kd_jenis_prw=pp.kd_jenis_prw where pp.noorder=l.noorder),''))"
        + "   from permintaan_lab l inner join reg_periksa r on r.no_rawat=l.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where l.dokter_perujuk=? and l.tgl_permintaan=? and l.status='ralan'"
        + " union all select cast(l.jam_permintaan as char),'Permintaan Radiologi',l.no_rawat,ps.nm_pasien,"
        + "   concat(l.noorder,' : ',ifnull((select group_concat(j.nm_perawatan separator '; ') from permintaan_pemeriksaan_radiologi pp inner join jns_perawatan_radiologi j on j.kd_jenis_prw=pp.kd_jenis_prw where pp.noorder=l.noorder),''))"
        + "   from permintaan_radiologi l inner join reg_periksa r on r.no_rawat=l.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where l.dokter_perujuk=? and l.tgl_permintaan=? and l.status='ralan'"
        + " union all select cast(v.jam_rawat as char),'Tindakan Dokter',v.no_rawat,ps.nm_pasien,j.nm_perawatan"
        + "   from rawat_jl_dr v inner join jns_perawatan j on j.kd_jenis_prw=v.kd_jenis_prw inner join reg_periksa r on r.no_rawat=v.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where v.kd_dokter=? and v.tgl_perawatan=?"
        + " union all select cast(v.jam_rawat as char),'Tindakan Dokter & Perawat',v.no_rawat,ps.nm_pasien,j.nm_perawatan"
        + "   from rawat_jl_drpr v inner join jns_perawatan j on j.kd_jenis_prw=v.kd_jenis_prw inner join reg_periksa r on r.no_rawat=v.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where v.kd_dokter=? and v.tgl_perawatan=?"
        + " union all select '-','Resume Medis',rp.no_rawat,ps.nm_pasien,concat('Diagnosa: ',ifnull(rp.diagnosa_utama,''),' | Obat pulang: ',ifnull(rp.obat_pulang,''))"
        + "   from resume_pasien rp inner join reg_periksa r on r.no_rawat=rp.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where rp.kd_dokter=? and r.tgl_registrasi=?"
        + ") z order by z.jam";

    private static final String SQL_DETAIL_RANAP =
        "select * from ("
        + " select cast(t.jam_login as char) jam,'Login SIMRS' jenis,'' no_rawat,'' pasien,'Login aplikasi SIMRS' isi from tracker t where t.nip=? and t.tgl_login=?"
        + " union all select cast(p.jam_rawat as char),'CPPT oleh Dokter',p.no_rawat,ps.nm_pasien,"
        + "   concat('S: ',ifnull(p.keluhan,''),' | O: ',ifnull(p.pemeriksaan,''),' | A: ',ifnull(p.penilaian,''),' | P: ',ifnull(p.rtl,''),' | Instruksi: ',ifnull(p.instruksi,''))"
        + "   from pemeriksaan_ranap p inner join reg_periksa r on r.no_rawat=p.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where p.nip=? and p.tgl_perawatan=?"
        + " union all select cast(o.jam_peresepan as char),'Resep (a.n. dokter)',o.no_rawat,ps.nm_pasien,"
        + "   concat(o.no_resep,' : ',ifnull((select group_concat(concat(b.nama_brng,' ',rd.jml,' (',ifnull(rd.aturan_pakai,''),')') separator '; ') from resep_dokter rd inner join databarang b on b.kode_brng=rd.kode_brng where rd.no_resep=o.no_resep),'racikan'))"
        + "   from resep_obat o inner join reg_periksa r on r.no_rawat=o.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where o.kd_dokter=? and o.tgl_peresepan=? and o.status='ranap'"
        + " union all select cast(l.jam_permintaan as char),'Permintaan Lab',l.no_rawat,ps.nm_pasien,"
        + "   concat(l.noorder,' : ',ifnull((select group_concat(j.nm_perawatan separator '; ') from permintaan_pemeriksaan_lab pp inner join jns_perawatan_lab j on j.kd_jenis_prw=pp.kd_jenis_prw where pp.noorder=l.noorder),''))"
        + "   from permintaan_lab l inner join reg_periksa r on r.no_rawat=l.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where l.dokter_perujuk=? and l.tgl_permintaan=? and l.status='ranap'"
        + " union all select cast(l.jam_permintaan as char),'Permintaan Radiologi',l.no_rawat,ps.nm_pasien,"
        + "   concat(l.noorder,' : ',ifnull((select group_concat(j.nm_perawatan separator '; ') from permintaan_pemeriksaan_radiologi pp inner join jns_perawatan_radiologi j on j.kd_jenis_prw=pp.kd_jenis_prw where pp.noorder=l.noorder),''))"
        + "   from permintaan_radiologi l inner join reg_periksa r on r.no_rawat=l.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where l.dokter_perujuk=? and l.tgl_permintaan=? and l.status='ranap'"
        + " union all select cast(v.jam_rawat as char),'Visite/Tindakan Dokter',v.no_rawat,ps.nm_pasien,j.nm_perawatan"
        + "   from rawat_inap_dr v inner join jns_perawatan_inap j on j.kd_jenis_prw=v.kd_jenis_prw inner join reg_periksa r on r.no_rawat=v.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where v.kd_dokter=? and v.tgl_perawatan=?"
        + " union all select cast(v.jam_rawat as char),'Tindakan Dokter & Perawat',v.no_rawat,ps.nm_pasien,j.nm_perawatan"
        + "   from rawat_inap_drpr v inner join jns_perawatan_inap j on j.kd_jenis_prw=v.kd_jenis_prw inner join reg_periksa r on r.no_rawat=v.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis where v.kd_dokter=? and v.tgl_perawatan=?"
        + " union all select cast(ki.jam_keluar as char),'Resume Pulang',rp.no_rawat,ps.nm_pasien,concat('Diagnosa: ',ifnull(rp.diagnosa_utama,''),' | Keadaan: ',ifnull(rp.keadaan,''))"
        + "   from resume_pasien_ranap rp inner join kamar_inap ki on ki.no_rawat=rp.no_rawat inner join reg_periksa r on r.no_rawat=rp.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis"
        + "   where rp.kd_dokter=? and ki.tgl_keluar=? and ki.stts_pulang<>'Pindah Kamar'"
        + ") z order by z.jam";
}
