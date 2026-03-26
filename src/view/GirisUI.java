package view;

import dao.KullaniciDAO;
import model.Kullanici;
import util.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * GirisUI — Kütüphane Yönetim Sistemi için Swing tabanlı giriş ekranı.

 * Özellikler:
 *  • Modern, koyu arka planlı tasarım
 *  • E-posta ve şifre alanı (şifre göster/gizle butonu)
 *  • Enter tuşu ile hızlı giriş
 *  • Rol bazlı yönlendirme: YONETICI → AnaMenuUI
 *                             UYE     → UyeMenuUI
 */
public class GirisUI extends JFrame {

    // ---------------------------------------------------------------
    // Renk Paleti (koyu, profesyonel tema)
    // ---------------------------------------------------------------
    private static final Color RENK_ARKA_PLAN   = new Color(18, 18, 30);
    private static final Color RENK_PANEL        = new Color(28, 28, 45);
    private static final Color RENK_KART         = new Color(38, 38, 58);
    private static final Color RENK_VURGU        = new Color(99, 102, 241);   // Indigo
    private static final Color RENK_VURGU_HOVER  = new Color(124, 127, 255);
    private static final Color RENK_METIN        = new Color(235, 235, 245);
    private static final Color RENK_METIN_SOLUK  = new Color(148, 148, 175);
    private static final Color RENK_HATA         = new Color(239, 68, 68);
    private static final Color RENK_SINIR        = new Color(55, 55, 80);

    private static final Font  FONT_BASLIK  = new Font("Segoe UI", Font.BOLD,  26);
    private static final Font  FONT_ALT     = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font  FONT_ETIKET  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font  FONT_ALAN    = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font  FONT_BUTON   = new Font("Segoe UI", Font.BOLD,  14);

    // ---------------------------------------------------------------
    // Bileşenler
    // ---------------------------------------------------------------
    private JTextField  emailAlani;
    private JPasswordField sifreAlani;
    private JCheckBox   sifreGosterCheckbox;
    private JButton     girisButonu;
    private JLabel      hataMesajiEtiketi;

    // ---------------------------------------------------------------
    // DAO
    // ---------------------------------------------------------------
    private final KullaniciDAO kullaniciDAO;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public GirisUI() {
        this.kullaniciDAO = new KullaniciDAO();
        pencereAyarla();
        bilesenleriOlustur();
        olaylariDinle();
    }

    // ---------------------------------------------------------------
    // Pencere Ayarları
    // ---------------------------------------------------------------
    private void pencereAyarla() {
        setTitle("Kütüphane Yönetim Sistemi — Giriş");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 540);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(RENK_ARKA_PLAN);
        setLayout(new GridBagLayout());
    }

    // ---------------------------------------------------------------
    // Bileşen Oluşturma
    // ---------------------------------------------------------------
    private void bilesenleriOlustur() {
        // --- Ana kart paneli ---
        JPanel kartPanel = new JPanel();
        kartPanel.setLayout(new BoxLayout(kartPanel, BoxLayout.Y_AXIS));
        kartPanel.setBackground(RENK_KART);
        kartPanel.setBorder(new EmptyBorder(40, 44, 40, 44));

        // Altta hafif gölge etkisi için kart bir iç panel içine alınır
        JPanel golgePanel = new JPanel(new BorderLayout());
        golgePanel.setBackground(RENK_ARKA_PLAN);
        golgePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RENK_SINIR, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        golgePanel.add(kartPanel, BorderLayout.CENTER);

        // --- Logo / Başlık ---
        JLabel logoEtiketi = new JLabel("📚", SwingConstants.CENTER);
        logoEtiketi.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        logoEtiketi.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoEtiketi.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel baslikEtiketi = new JLabel("Kütüphane Sistemi", SwingConstants.CENTER);
        baslikEtiketi.setFont(FONT_BASLIK);
        baslikEtiketi.setForeground(RENK_METIN);
        baslikEtiketi.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel altBaslikEtiketi = new JLabel("Hesabınıza giriş yapın", SwingConstants.CENTER);
        altBaslikEtiketi.setFont(FONT_ALT);
        altBaslikEtiketi.setForeground(RENK_METIN_SOLUK);
        altBaslikEtiketi.setAlignmentX(Component.CENTER_ALIGNMENT);
        altBaslikEtiketi.setBorder(new EmptyBorder(4, 0, 28, 0));

        // --- E-posta alanı ---
        JLabel emailEtiketi = new JLabel("E-posta Adresi");
        emailEtiketi.setFont(FONT_ETIKET);
        emailEtiketi.setForeground(RENK_METIN_SOLUK);
        emailEtiketi.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailEtiketi.setBorder(new EmptyBorder(0, 2, 6, 0));

        emailAlani = new JTextField();
        stilAlanUygula(emailAlani);

        // --- Şifre alanı ---
        JLabel sifreEtiketi = new JLabel("Şifre");
        sifreEtiketi.setFont(FONT_ETIKET);
        sifreEtiketi.setForeground(RENK_METIN_SOLUK);
        sifreEtiketi.setAlignmentX(Component.CENTER_ALIGNMENT);
        sifreEtiketi.setBorder(new EmptyBorder(16, 2, 6, 0));

        sifreAlani = new JPasswordField();
        stilAlanUygula(sifreAlani);

        // --- Şifreyi göster checkbox ---
        sifreGosterCheckbox = new JCheckBox("Şifreyi göster");
        sifreGosterCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sifreGosterCheckbox.setForeground(RENK_METIN_SOLUK);
        sifreGosterCheckbox.setBackground(RENK_KART);
        sifreGosterCheckbox.setFocusPainted(false);
        sifreGosterCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        sifreGosterCheckbox.setBorder(new EmptyBorder(6, 2, 14, 0));

        // --- Hata mesajı etiketi ---
        hataMesajiEtiketi = new JLabel(" ");
        hataMesajiEtiketi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hataMesajiEtiketi.setForeground(RENK_HATA);
        hataMesajiEtiketi.setAlignmentX(Component.CENTER_ALIGNMENT);
        hataMesajiEtiketi.setHorizontalAlignment(SwingConstants.CENTER);
        hataMesajiEtiketi.setBorder(new EmptyBorder(0, 0, 10, 0));

        // --- Giriş butonu ---
        girisButonu = new JButton("Giriş Yap");
        girisButonu.setFont(FONT_BUTON);
        girisButonu.setForeground(Color.WHITE);
        girisButonu.setBackground(RENK_VURGU);
        girisButonu.setFocusPainted(false);
        girisButonu.setBorderPainted(false);
        girisButonu.setOpaque(true);
        girisButonu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        girisButonu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        girisButonu.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hover efekti
        girisButonu.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                girisButonu.setBackground(RENK_VURGU_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                girisButonu.setBackground(RENK_VURGU);
            }
        });

        // --- Alt bilgi etiketi ---
        JLabel altBilgiEtiketi = new JLabel("Kütüphane Yönetim Sistemi v1.0", SwingConstants.CENTER);
        altBilgiEtiketi.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        altBilgiEtiketi.setForeground(RENK_METIN_SOLUK);
        altBilgiEtiketi.setAlignmentX(Component.CENTER_ALIGNMENT);
        altBilgiEtiketi.setBorder(new EmptyBorder(20, 0, 0, 0));

        // --- Bileşenleri karta ekle ---
        kartPanel.add(logoEtiketi);
        kartPanel.add(baslikEtiketi);
        kartPanel.add(altBaslikEtiketi);
        kartPanel.add(emailEtiketi);
        kartPanel.add(emailAlani);
        kartPanel.add(sifreEtiketi);
        kartPanel.add(sifreAlani);
        kartPanel.add(sifreGosterCheckbox);
        kartPanel.add(hataMesajiEtiketi);
        kartPanel.add(girisButonu);
        kartPanel.add(altBilgiEtiketi);

        // --- Ana pencereye ekle ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 30, 20, 30);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(golgePanel, gbc);
    }

    // ---------------------------------------------------------------
    // Olay Dinleyicileri
    // ---------------------------------------------------------------
    private void olaylariDinle() {
        // Giriş butonu tıklama
        girisButonu.addActionListener(e -> girisYap());

        // Enter tuşu ile giriş (her iki alanda da)
        ActionListener enterAktifi = e -> girisYap();
        emailAlani.addActionListener(enterAktifi);
        sifreAlani.addActionListener(enterAktifi);

        // Şifreyi göster / gizle
        sifreGosterCheckbox.addItemListener(e -> {
            if (sifreGosterCheckbox.isSelected()) {
                sifreAlani.setEchoChar((char) 0);  // Şifreyi görünür yap
            } else {
                sifreAlani.setEchoChar('●');       // Şifreyi gizle
            }
        });

        // Uygulama kapanırken DB bağlantısını kapat
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DBConnection.baglantiKapat();
            }
        });
    }

    // ---------------------------------------------------------------
    // Giriş Mantığı
    // ---------------------------------------------------------------
    private void girisYap() {
        String email = emailAlani.getText().trim();
        String sifre = new String(sifreAlani.getPassword());

        // Boş alan kontrolü
        if (email.isEmpty() || sifre.isEmpty()) {
            hataMesajiGoster("E-posta ve şifre alanları boş bırakılamaz.");
            return;
        }

        // Basit e-posta formatı kontrolü
        if (!email.contains("@")) {
            hataMesajiGoster("Geçerli bir e-posta adresi girin.");
            return;
        }

        girisButonu.setEnabled(false);
        girisButonu.setText("Doğrulanıyor...");
        hataMesajiEtiketi.setText(" ");

        // Giriş işlemini arka planda yap (UI donmaması için)
        SwingWorker<Kullanici, Void> isci = new SwingWorker<>() {
            @Override
            protected Kullanici doInBackground() {
                return kullaniciDAO.girisDogrula(email, sifre);
            }

            @Override
            protected void done() {
                try {
                    Kullanici kullanici = get();
                    if (kullanici != null) {
                        girisBasarili(kullanici);
                    } else {
                        hataMesajiGoster("E-posta veya şifre hatalı. Lütfen tekrar deneyin.");
                    }
                } catch (Exception ex) {
                    hataMesajiGoster("Bağlantı hatası: " + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    girisButonu.setEnabled(true);
                    girisButonu.setText("Giriş Yap");
                }
            }
        };

        isci.execute();
    }

    /**
     * Başarılı giriş sonrası kullanıcının rolüne göre ilgili dashboard'a yönlendirir.
     */
    private void girisBasarili(Kullanici kullanici) {
        dispose();  // Giriş ekranını kapat

        if (kullanici.isYonetici()) {
            // YONETICI → Ana Yönetici Paneli
            AnaMenuUI anaMenu = new AnaMenuUI(kullanici);
            anaMenu.setVisible(true);
        } else {
            // UYE → Üye Paneli
            UyeMenuUI uyeMenu = new UyeMenuUI(kullanici);
            uyeMenu.setVisible(true);
        }
    }

    /**
     * Hata mesajını ekrandaki etikette kırmızı olarak gösterir.
     */
    private void hataMesajiGoster(String mesaj) {
        hataMesajiEtiketi.setText(mesaj);
    }

    // ---------------------------------------------------------------
    // Yardımcı Stiller
    // ---------------------------------------------------------------

    /**
     * Hem {@link JTextField} hem {@link JPasswordField} için ortak görsel stili uygular.
     */
    private void stilAlanUygula(JTextField alan) {
        alan.setFont(FONT_ALAN);
        alan.setForeground(RENK_METIN);
        alan.setBackground(RENK_PANEL);
        alan.setCaretColor(RENK_METIN);
        alan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RENK_SINIR, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        alan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        alan.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    // ---------------------------------------------------------------
    // main — Uygulamanın başlangıç noktası
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        // Sistem görünümü (Windows / macOS native)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Varsayılan L&F ile devam et
        }

        SwingUtilities.invokeLater(() -> {
            GirisUI girisEkrani = new GirisUI();
            girisEkrani.setVisible(true);
        });
    }
}
