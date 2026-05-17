/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzacetakantrianloket;

import javax.swing.*;
import java.awt.*;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.AWTException;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import fungsi.sekuel;
import fungsi.akses;
import fungsi.validasi;
import fungsi.koneksiDB;

public class frmUtama extends JFrame {
    private JLabel labelWaktu;
    private JLabel labelNomorA;
    private JLabel labelNomorB;
    private final validasi Valid = new validasi();
    private final sekuel Sequel = new sekuel();

    private ImageIcon printIcon;
    private JTextField txtNoKartu;
    private final ImageIcon hospitalIcon = new ImageIcon(getClass().getResource("/picture/hospital.png"));
    private final ImageIcon clockIcon = new ImageIcon(getClass().getResource("/picture/clock.png"));

    public frmUtama() {
        setTitle("Antrian Loket");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(220, 235, 245));
        setLayout(new BorderLayout(20, 10));

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                revalidate();
                repaint();
            }
        });

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createAntrianPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        updateWaktu();
        autonomer();
        autonomer2();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                txtNoKartu.requestFocusInWindow();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 102, 153));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(new Color(0, 102, 153));
        JLabel iconLabel = new JLabel(hospitalIcon);
        titlePanel.add(iconLabel);

        JLabel info = new JLabel(
                "<html><b style='font-size:18pt;color:white;'>RSUD KOTA BAUBAU</b><br/><span style='color:white;'>RS Umum Daerah Kota Bau Bau. Alamat. Jl. Drs. H. La Ode Manarfa<br/>Telp. 082292595705</span></html>");
        info.setFont(new Font("Tahoma", Font.PLAIN, 18));
        titlePanel.add(info);
        panel.add(titlePanel, BorderLayout.WEST);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        timePanel.setBackground(new Color(0, 102, 153));
        JLabel clockLabel = new JLabel(clockIcon);
        timePanel.add(clockLabel);

        labelWaktu = new JLabel("", SwingConstants.RIGHT);
        labelWaktu.setFont(new Font("Tahoma", Font.BOLD, 24));
        labelWaktu.setForeground(Color.WHITE);
        timePanel.add(labelWaktu);
        panel.add(timePanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createAntrianPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 40, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setBackground(new Color(235, 245, 255));

        labelNomorA = new JLabel("A001", SwingConstants.CENTER);
        JPanel panelA = createPanelAntrian("A", "Nomor Antrian Prioritas", labelNomorA, new Color(0, 153, 76));

        labelNomorB = new JLabel("B001", SwingConstants.CENTER);
        JPanel panelB = createPanelAntrian("B", "Nomor Antrian Biasa", labelNomorB, new Color(0, 102, 204));

        panel.add(panelA);
        panel.add(panelB);

        return panel;
    }

    private JPanel createPanelAntrian(String prefix, String keterangan, JLabel labelNomor, Color warnaKeterangan) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(warnaKeterangan.darker(), 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel labelJudul = new JLabel(keterangan, SwingConstants.CENTER);
        labelJudul.setFont(new Font("Tahoma", Font.BOLD, 36));
        labelJudul.setForeground(warnaKeterangan.darker());
        panel.add(labelJudul, BorderLayout.NORTH);

        labelNomor.setFont(new Font("Tahoma", Font.BOLD, 250));
        labelNomor.setForeground(warnaKeterangan.darker());
        panel.add(labelNomor, BorderLayout.CENTER);

        JButton btnCetak = new JButton();
        btnCetak.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnCetak.setOpaque(true);

        ImageIcon originalPrintIcon = new ImageIcon(getClass().getResource("/picture/PrinterSettings.png"));
        int iconSize = labelNomor.getFont().getSize() / 3;
        Image scaledImg = originalPrintIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
        printIcon = new ImageIcon(scaledImg);

        JLabel icon = new JLabel(printIcon);
        JLabel label = new JLabel("CETAK");
        label.setFont(new Font("Tahoma", Font.BOLD, iconSize));
        label.setForeground(Color.WHITE);

        btnCetak.add(icon);
        btnCetak.add(label);
        btnCetak.setBackground(warnaKeterangan);
        btnCetak.setFocusPainted(false);
        btnCetak.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCetak.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(warnaKeterangan.darker(), 2),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)));

        btnCetak.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCetak.setBackground(warnaKeterangan.brighter());
                btnCetak.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 3),
                        BorderFactory.createEmptyBorder(20, 30, 20, 30)));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCetak.setBackground(warnaKeterangan);
                btnCetak.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(warnaKeterangan.darker(), 2),
                        BorderFactory.createEmptyBorder(15, 25, 15, 25)));
            }
        });

        btnCetak.addActionListener(e -> {
            String nomor = labelNomor.getText().substring(1);
            if (Sequel.menyimpantf("antripendaftaran_nomor",
                    "'" + prefix + "','" + nomor + "','0',concat(current_date(),' ',current_time()),''",
                    "Nomor Antrian")) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Map<String, Object> param = new HashMap<>();
                param.put("namars", akses.getnamars());
                param.put("alamatrs", akses.getalamatrs());
                param.put("kotars", akses.getkabupatenrs());
                param.put("propinsirs", akses.getpropinsirs());
                param.put("kontakrs", akses.getkontakrs());
                param.put("emailrs", akses.getemailrs());
                Valid.MyReportqry("rptAntriLoket.jasper", "report", "::[ Antrian Loket ]::",
                        "select antripendaftaran_nomor.jam,antripendaftaran_nomor.nomor from antripendaftaran_nomor where LEFT(antripendaftaran_nomor.jam,10)=current_date and antripendaftaran_nomor.nomor='"
                                + nomor + "' ",
                        param);
//        Valid.MyReportqry("rptAntriLoket.jasper", "report", "::[ Antrian Loket ]::",
//        "select antripendaftaran_nomor.jam, antripendaftaran_nomor.nomor " +
//        "from antripendaftaran_nomor " +
//        "where antripendaftaran_nomor.nomor='" + nomor + "' ", 
//        param);
                this.setCursor(Cursor.getDefaultCursor());
                if (prefix.equals("A")) {
                    autonomer();
                } else {
                    autonomer2();
                }
            }
        });

        panel.add(btnCetak, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        panel.setBackground(new Color(220, 235, 245));

        // --- Area scan kartu JKN ---
        JPanel scanPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        scanPanel.setBackground(new Color(220, 235, 245));

        JLabel lblScan = new JLabel("No. Kartu JKN :");
        lblScan.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblScan.setForeground(new Color(0, 80, 130));

        txtNoKartu = new JTextField(18);
        txtNoKartu.setFont(new Font("Tahoma", Font.BOLD, 18));
        txtNoKartu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 90, 50), 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        txtNoKartu.setToolTipText("Scan atau ketik nomor kartu JKN lalu Enter");
        txtNoKartu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    bukaFingerPrint(txtNoKartu.getText().trim());
                }
            }
        });

        JButton btnFinger = new JButton("  FINGER PRINT  ");
        btnFinger.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnFinger.setBackground(new Color(0, 130, 80));
        btnFinger.setForeground(Color.WHITE);
        btnFinger.setFocusPainted(false);
        btnFinger.setOpaque(true);
        btnFinger.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFinger.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 90, 50), 2),
                BorderFactory.createEmptyBorder(10, 30, 10, 30)));
        btnFinger.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnFinger.setBackground(new Color(0, 160, 100));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnFinger.setBackground(new Color(0, 130, 80));
            }
        });
        btnFinger.addActionListener(e -> bukaFingerPrint(txtNoKartu.getText().trim()));

        scanPanel.add(lblScan);
        scanPanel.add(txtNoKartu);
        scanPanel.add(btnFinger);

        // --- Tombol Mobile JKN ---
        JButton btnQR = new JButton("  CHECK-IN MOBILE JKN  ");
        btnQR.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnQR.setBackground(new Color(0, 153, 204));
        btnQR.setForeground(Color.WHITE);
        btnQR.setFocusPainted(false);
        btnQR.setOpaque(true);
        btnQR.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnQR.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 153), 2),
                BorderFactory.createEmptyBorder(10, 30, 10, 30)));
        btnQR.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnQR.setBackground(new Color(0, 120, 170));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnQR.setBackground(new Color(0, 153, 204));
            }
        });
        btnQR.addActionListener(e -> showQrMobileJKN());

        panel.add(scanPanel);
        panel.add(btnQR);
        return panel;
    }

    private void showQrMobileJKN() {
        JDialog dialog = new JDialog(this, "Check-In Mobile JKN", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        JLabel labelJudul = new JLabel(
                "<html><div style='text-align:center;'><b>UNTUK PASIEN PENGGUNA LAYANAN MOBILE JKN</b><br/>HARAP SCAN BARCODE</div></html>",
                SwingConstants.CENTER);
        labelJudul.setFont(new Font("Tahoma", Font.BOLD, 18));
        labelJudul.setForeground(new Color(0, 80, 130));
        labelJudul.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        dialog.add(labelJudul, BorderLayout.NORTH);

        JLabel labelQR = new JLabel("", SwingConstants.CENTER);
        labelQR.setPreferredSize(new Dimension(350, 350));
        File qrFile = new File("setting/qr_mobile_jkn.png");
        if (qrFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(qrFile);
                Image scaled = img.getScaledInstance(320, 320, Image.SCALE_SMOOTH);
                labelQR.setIcon(new ImageIcon(scaled));
            } catch (Exception ex) {
                labelQR.setText("<html><center>Gagal memuat gambar QR.<br/>Periksa file setting/qr_mobile_jkn.png</center></html>");
            }
        } else {
            labelQR.setText("<html><center><b>File QR Code belum tersedia.</b><br/><br/>"
                    + "Letakkan file gambar QR Code dari BPJS<br/>"
                    + "dengan nama: <b>qr_mobile_jkn.png</b><br/>"
                    + "di folder: <b>setting/</b></center></html>");
            labelQR.setFont(new Font("Tahoma", Font.PLAIN, 14));
            labelQR.setForeground(new Color(180, 50, 50));
            labelQR.setBorder(BorderFactory.createLineBorder(new Color(180, 50, 50), 2));
        }
        labelQR.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 30, 5, 30),
                labelQR.getBorder()));
        dialog.add(labelQR, BorderLayout.CENTER);

        JButton btnTutup = new JButton("TUTUP");
        btnTutup.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnTutup.setBackground(new Color(180, 50, 50));
        btnTutup.setForeground(Color.WHITE);
        btnTutup.setFocusPainted(false);
        btnTutup.setOpaque(true);
        btnTutup.setBorder(BorderFactory.createEmptyBorder(8, 30, 8, 30));
        btnTutup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTutup.addActionListener(e -> dialog.dispose());
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBtn.setBackground(Color.WHITE);
        panelBtn.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
        panelBtn.add(btnTutup);
        dialog.add(panelBtn, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void bukaFingerPrint(String noKartu) {
        if (noKartu.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Scan kartu JKN terlebih dahulu sebelum membuka aplikasi Finger Print",
                    "Perhatian", JOptionPane.WARNING_MESSAGE);
            txtNoKartu.requestFocus();
            return;
        }
        String urlaplikasi = koneksiDB.URLAPLIKASIFINGERPRINT();
        if (urlaplikasi.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Path aplikasi finger print belum dikonfigurasi.\nTambahkan URLAPLIKASIFINGERPRINT di setting/database.xml",
                    "Konfigurasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new Thread(() -> {
            try {
                Runtime.getRuntime().exec(urlaplikasi);
                String url  = koneksiDB.URLFINGER();
                String user = koneksiDB.USERFINGER();
                String pass = koneksiDB.PASSFINGER();
                Robot robot = new Robot();
                if (!url.isEmpty()) {
                    Thread.sleep(1500);
                    pasteClipboard(robot, url);
                    robot.keyPress(KeyEvent.VK_TAB); robot.keyRelease(KeyEvent.VK_TAB);
                    robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER);
                }
                if (!user.isEmpty()) {
                    Thread.sleep(1500);
                    pasteClipboard(robot, user);
                    robot.keyPress(KeyEvent.VK_TAB); robot.keyRelease(KeyEvent.VK_TAB);
                    Thread.sleep(500);
                    pasteClipboard(robot, pass);
                    robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER);
                }
                Thread.sleep(1000);
                pasteClipboard(robot, noKartu);
                SwingUtilities.invokeLater(() -> {
                    txtNoKartu.setText("");
                    txtNoKartu.requestFocus();
                });
            } catch (IOException | AWTException | InterruptedException ex) {
                System.out.println("Finger Print: " + ex);
            }
        }).start();
    }

    private void pasteClipboard(Robot robot, String text) {
        StringSelection sel = new StringSelection(text);
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(sel, sel);
        robot.keyPress(KeyEvent.VK_CONTROL); robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V); robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    private void updateWaktu() {
        Timer timer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd-MM-yyyy HH:mm:ss",
                    new Locale("id", "ID"));
            labelWaktu.setText(now.format(formatter));
        });
        timer.start();
    }

    private void autonomer() {
        Valid.autoNomer3(
                "select ifnull(MAX(CONVERT(antripendaftaran_nomor.nomor,signed)),0) from antripendaftaran_nomor where LEFT(antripendaftaran_nomor.jam,10)=current_date() and prefix='A' ",
                "A", 1, labelNomorA);
    }

    private void autonomer2() {
        Valid.autoNomer3(
                "select ifnull(MAX(CONVERT(antripendaftaran_nomor.nomor,signed)),0) from antripendaftaran_nomor where LEFT(antripendaftaran_nomor.jam,10)=current_date() and prefix='B'",
                "B", 1, labelNomorB);
    }

    public static void fadeIn(JFrame frame) {
        try {
            for (float i = 0f; i <= 1f; i += 0.05f) {
                Thread.sleep(30);
                frame.setOpacity(i);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frmUtama frame = new frmUtama();
            frame.setOpacity(0f);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            new Thread(() -> fadeIn(frame)).start();
        });
    }
}