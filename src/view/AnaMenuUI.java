package view;

import dao.*;
import model.*;
import util.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * AnaMenuUI — 'YONETICI' rolüne sahip kullanıcılar için ana yönetim paneli.
 *
 * Sekmeler:
 *  1. Kitap Yönetimi      — listeleme, arama, ekleme (JComboBox ile), silme
 *  2. Kullanıcı Yönetimi  — listeleme, ekleme, aktif/pasif toggle
 *  3. Ödünç İşlemleri     — ödünç ver (barkod), iade al, gecikme takibi
 *  4. Sistem Ayarları     — kategori / yazar / yayınevi ekleme ve listeleme
 */
public class AnaMenuUI extends JFrame {

    // ---------------------------------------------------------------
    // Renk & Font Sabitleri
    // ---------------------------------------------------------------
    static final Color RENK_ARKA_PLAN    = new Color(18, 18, 30);
    static final Color RENK_PANEL        = new Color(28, 28, 45);
    static final Color RENK_KART         = new Color(38, 38, 58);
    static final Color RENK_VURGU        = new Color(99, 102, 241);
    static final Color RENK_VURGU_HOVER  = new Color(124, 127, 255);
    static final Color RENK_TEHLIKE      = new Color(220, 53, 69);
    static final Color RENK_BASARI       = new Color(40, 167, 69);
    static final Color RENK_METIN        = new Color(235, 235, 245);
    static final Color RENK_METIN_SOLUK  = new Color(148, 148, 175);
    static final Color RENK_SINIR        = new Color(55, 55, 80);
    static final Color RENK_TABLO_SECIM  = new Color(60, 60, 100);
    static final Color RENK_TABLO_SATIR1 = new Color(28, 28, 45);
    static final Color RENK_TABLO_SATIR2 = new Color(33, 33, 52);

    static final Font FONT_BASLIK  = new Font("Segoe UI", Font.BOLD,  20);
    static final Font FONT_ETIKET  = new Font("Segoe UI", Font.BOLD,  13);
    static final Font FONT_NORMAL  = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font FONT_BUTON   = new Font("Segoe UI", Font.BOLD,  13);
    static final Font FONT_KUCUK   = new Font("Segoe UI", Font.PLAIN, 11);

    private static final DateTimeFormatter TARIH_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // ---------------------------------------------------------------
    // State & DAOs
    // ---------------------------------------------------------------
    private final Kullanici    aktifKullanici;
    private final KitapDAO     kitapDAO     = new KitapDAO();
    private final KullaniciDAO kullaniciDAO = new KullaniciDAO();
    private final OduncDAO     oduncDAO     = new OduncDAO();
    private final KategoriDAO  kategoriDAO  = new KategoriDAO();
    private final YazarDAO     yazarDAO     = new YazarDAO();
    private final YayineviDAO  yayineviDAO  = new YayineviDAO();

    // Kitap Yönetimi
    private JTable            kitapTablosu;
    private DefaultTableModel kitapModeliTablo;
    private JTextField        kitapAramaAlani;

    // Kullanıcı Yönetimi
    private JTable            kullaniciTablosu;
    private DefaultTableModel kullaniciModeliTablo;

    // Ödünç İşlemleri
    private JTable            oduncTablosu;
    private DefaultTableModel oduncModeliTablo;

    // Sistem Ayarları — tablolar lookupKartiOlustur içinde yerel olarak yönetilir
    private DefaultTableModel kategoriModeli,   yazarModeli,   yayineviModeli;

    // ==========================================
    private JComboBox<String> musaitBarkodlarCB;
    // ==========================================

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public AnaMenuUI(Kullanici kullanici) {
        this.aktifKullanici = kullanici;
        pencereAyarla();
        bilesenleriOlustur();
    }

    // ---------------------------------------------------------------
    // Pencere
    // ---------------------------------------------------------------
    private void pencereAyarla() {
        setTitle("Kütüphane Yönetim Sistemi — Yönetici Paneli");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1024, 680));
        getContentPane().setBackground(RENK_ARKA_PLAN);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { DBConnection.baglantiKapat(); }
        });
    }

    private void bilesenleriOlustur() {
        setLayout(new BorderLayout());
        add(ustCubuguOlustur(), BorderLayout.NORTH);
        add(sekmePaneliOlustur(), BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    // Üst Çubuk
    // ---------------------------------------------------------------
    private JPanel ustCubuguOlustur() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(RENK_KART);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, RENK_SINIR),
                new EmptyBorder(14, 20, 14, 20)));

        JPanel sol = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); sol.setOpaque(false);
        JLabel logo = new JLabel("📚"); logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        JLabel baslik = new JLabel("Kütüphane Yönetim Sistemi");
        baslik.setFont(FONT_BASLIK); baslik.setForeground(RENK_METIN);
        JLabel rol = new JLabel(" — YÖNETİCİ");
        rol.setFont(new Font("Segoe UI", Font.BOLD, 13)); rol.setForeground(new Color(250, 204, 21));
        sol.add(logo); sol.add(baslik); sol.add(rol);

        JPanel sag = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); sag.setOpaque(false);
        String ad = aktifKullanici.getAdSoyad() != null ? aktifKullanici.getAdSoyad() : aktifKullanici.getEmail();
        JLabel merhaba = new JLabel("Merhaba, " + ad);
        merhaba.setFont(FONT_KUCUK); merhaba.setForeground(RENK_METIN_SOLUK);
        JButton cikis = renkliButonOlustur("Çıkış Yap", RENK_TEHLIKE, RENK_TEHLIKE.brighter());
        cikis.addActionListener(e -> cikisYap());
        sag.add(merhaba); sag.add(cikis);

        panel.add(sol, BorderLayout.WEST);
        panel.add(sag, BorderLayout.EAST);
        return panel;
    }

    // ---------------------------------------------------------------
    // Sekmeli Panel
    // ---------------------------------------------------------------
    private JTabbedPane sekmePaneliOlustur() {
        JTabbedPane tp = new JTabbedPane();
        tp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tp.setBackground(RENK_KART);
        tp.setForeground(Color.WHITE);


        // setUI metodu ile "BasicTabbedPaneUI" sınıfını eziyoruz (Override ediyoruz).
        // Yani Java'ya "Sekmeleri kendi bildiğin gibi değil, benim kurallarımla çiz" diyoruz.
        tp.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {

            // 1. KURAL: Sekmenin Arka Planını Nasıl Boyayacağız?
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g;
                // Antialiasing: Yazıların ve köşelerin pikselli (tırtıklı) değil, pürüzsüz görünmesini sağlar.
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Eğer sekme seçiliyse (isSelected) mavi (RENK_VURGU) yap,
                // seçili değilse koyu lacivert (RENK_KART) yap.
                g2.setColor(isSelected ? RENK_VURGU : RENK_KART);
                g2.fillRect(x, y, w, h); // Belirlenen renk ile o dikdörtgen alanı doldur.
            }

            // 2. KURAL: Sekmenin Kenarlığını (Border) Nasıl Çizeceğiz?
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                // Windows normalde buraya 3 boyutlu gibi duran gri metalik çizgiler çizer.
                // Biz o çizgilerin rengini, arka planımızla AYNI RENK yaparak onları "Görünmez" kılıyoruz. (Flat Design mantığı)
                g.setColor(RENK_ARKA_PLAN);
                g.drawRect(x, y, w, h);
            }

            // 3. KURAL: Odaklanma (Focus) Çizgisini Nasıl Çizeceğiz?
            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // Eski programlarda bir yere tıkladığında etrafında kesik kesik (dotted) bir çizgi çıkar.
                // Bu metodun içini BOŞ bırakarak o iğrenç çizginin çizilmesini engelliyoruz!
            }
        });

        // Sekme isimlerinin sağına soluna bilerek boşluk ("   ") koyduk.
        // Böylece yazılar kutunun kenarlarına yapışmaz, ferah ve geniş butonlar elde ederiz.
        tp.addTab("   📖 Kitap Yönetimi   ",      kitapYonetimiPaneliOlustur());
        tp.addTab("   👤 Kullanıcı Yönetimi   ",  kullaniciYonetimiPaneliOlustur());
        tp.addTab("   🔄 Ödünç İşlemleri   ",     oduncIslemleriPaneliOlustur());
        tp.addTab("   ⚙️ Sistem Ayarları   ",      sistemAyarlariPaneliOlustur());

        // Sekme değiştiğinde yapılacak işlemler
        tp.addChangeListener(e -> {
            if (tp.getSelectedIndex() == 2) { // 3. sekme: Ödünç İşlemleri
                musaitBarkodlariYukle();
                istatistikleriGuncelle();
            }
        });
        return tp;
    }

    // ================================================================
    // ================================================================
    // SEKME 1 — KİTAP YÖNETİMİ
    // ================================================================
    private JPanel kitapYonetimiPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(RENK_ARKA_PLAN); panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel arac = new JPanel(new BorderLayout(12, 0));
        arac.setBackground(RENK_ARKA_PLAN); arac.setBorder(new EmptyBorder(0, 0, 10, 0));

        kitapAramaAlani = tekAlaniOlustur();
        kitapAramaAlani.setToolTipText("Başlık, ISBN veya yazar adı ile arama");
        kitapAramaAlani.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { kitaplariAra(kitapAramaAlani.getText()); }
        });
        JPanel aramaWrap = new JPanel(new BorderLayout(6, 0)); aramaWrap.setOpaque(false);
        JLabel aramaIkon = new JLabel("🔍"); aramaIkon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        aramaWrap.add(aramaIkon, BorderLayout.WEST); aramaWrap.add(kitapAramaAlani, BorderLayout.CENTER);

        JPanel butonlar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); butonlar.setOpaque(false);
        JButton ekle   = renkliButonOlustur("+ Ekle",   RENK_BASARI,  RENK_BASARI.brighter());
        JButton sil    = renkliButonOlustur("✕ Sil",    RENK_TEHLIKE, RENK_TEHLIKE.brighter());
        JButton yenile = renkliButonOlustur("⟳ Yenile", RENK_VURGU,   RENK_VURGU_HOVER);
        ekle.addActionListener(e   -> kitapEkleDiyaloguGoster());
        sil.addActionListener(e    -> seciliKitabiSil());
        yenile.addActionListener(e -> { kitapAramaAlani.setText(""); kitaplariYukle(); });
        butonlar.add(yenile); butonlar.add(sil); butonlar.add(ekle);

        arac.add(aramaWrap, BorderLayout.CENTER); arac.add(butonlar, BorderLayout.EAST);

        // AHA BURASI DEĞİŞTİ: Barkodlar sütunu eklendi
        String[] s = {"ID", "Başlık", "Yazar", "ISBN", "Yayın Yılı", "Kategori", "Yayınevi", "Barkodlar"};
        kitapModeliTablo = new DefaultTableModel(s, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        kitapTablosu = standartTablo(kitapModeliTablo);
        kitapTablosu.setRowSorter(new TableRowSorter<>(kitapModeliTablo));

        // AHA BURASI DEĞİŞTİ: Genişlikler yeni sütuna göre ayarlandı
        int[] w = {40, 230, 140, 110, 80, 110, 110, 100};
        for (int i = 0; i < w.length; i++) kitapTablosu.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        panel.add(arac, BorderLayout.NORTH); panel.add(tabloSarici(kitapTablosu), BorderLayout.CENTER);
        kitaplariYukle();
        return panel;
    }

    private void kitaplariYukle() {
        new SwingWorker<List<Kitap>, Void>() {
            @Override protected List<Kitap> doInBackground() { return kitapDAO.tumunuGetir(); }
            @Override protected void done() {
                try {
                    kitapModeliTablo.setRowCount(0);
                    for (Kitap k : get()) kitapModeliTablo.addRow(new Object[]{
                            k.getId(), k.getBaslik(), nvl(k.getYazarAdi()), k.getIsbn(),
                            k.getYayinYili() > 0 ? k.getYayinYili() : "—",
                            nvl(k.getKategoriAdi()), nvl(k.getYayineviAdi()),
                            nvl(k.getBarkodlar()) // İŞTE BARKOD BURADA EKLENİYOR
                    });
                } catch (Exception ex) { hataGoster("Kitaplar yüklenemedi: " + ex.getMessage()); }
            }
        }.execute();
    }

    private void kitaplariAra(String metin) {
        new SwingWorker<List<Kitap>, Void>() {
            @Override protected List<Kitap> doInBackground() { return kitapDAO.kitapAra(metin); }
            @Override protected void done() {
                try {
                    kitapModeliTablo.setRowCount(0);
                    for (Kitap k : get()) kitapModeliTablo.addRow(new Object[]{
                            k.getId(), k.getBaslik(), nvl(k.getYazarAdi()), k.getIsbn(),
                            k.getYayinYili() > 0 ? k.getYayinYili() : "—",
                            nvl(k.getKategoriAdi()), nvl(k.getYayineviAdi()),
                            nvl(k.getBarkodlar()) // ARAMA SONUCUNA DA BARKOD EKLENDİ
                    });
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    /**
     * Kitap Ekle diyaloğu — Kategori, Yazar ve Yayınevi alanları JComboBox
     * kullanır; veriler DB'den gerçek zamanlı çekilir.
     */
    private void kitapEkleDiyaloguGoster() {
        // --- Önce lookup verilerini arka planda çek ---
        JDialog yukleniyorDialog = yukleniyor("Listeler yükleniyor…");
        yukleniyorDialog.setVisible(true);

        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                return new Object[]{
                        kategoriDAO.tumunuGetir(),
                        yazarDAO.tumunuGetir(),
                        yayineviDAO.tumunuGetir()
                };
            }
            @Override protected void done() {
                yukleniyorDialog.dispose();
                try {
                    Object[] sonuclar = get();
                    @SuppressWarnings("unchecked") List<Kategori>  kategoriler = (List<Kategori>)  sonuclar[0];
                    @SuppressWarnings("unchecked") List<Yazar>     yazarlar    = (List<Yazar>)     sonuclar[1];
                    @SuppressWarnings("unchecked") List<YayinEvi>  yayinevleri = (List<YayinEvi>)  sonuclar[2];

                    kitapEkleDiyalogGoster(kategoriler, yazarlar, yayinevleri);
                } catch (Exception ex) { hataGoster("Liste yüklenemedi: " + ex.getMessage()); }
            }
        }.execute();
    }

    private void kitapEkleDiyalogGoster(List<Kategori> kategoriler,
                                         List<Yazar>     yazarlar,
                                         List<YayinEvi>  yayinevleri) {
        JDialog d = new JDialog(this, "Yeni Kitap Ekle", true);
        d.setSize(560, 530); d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout()); d.getContentPane().setBackground(RENK_KART);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(RENK_KART); form.setBorder(new EmptyBorder(22, 26, 14, 26));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(6, 4, 6, 4);

        // --- Düz metin alanları ---
        JTextField baslikAlani = tekAlaniOlustur();
        JTextField isbnAlani   = tekAlaniOlustur();
        JTextField yilAlani    = tekAlaniOlustur();

        // --- JComboBox'lar (model nesnelerini tutar) ---
        JComboBox<Kategori>  kategoriCB  = new JComboBox<>();
        JComboBox<Yazar>     yazarCB     = new JComboBox<>();
        JComboBox<YayinEvi>  yayineviCB  = new JComboBox<>();

        // Boş seçenek (ilk öğe — zorunlu değil)
        kategoriCB.addItem(new Kategori(0, "— Seçiniz —"));
        kategoriler.forEach(kategoriCB::addItem);

        yazarCB.addItem(new Yazar(0, "— Seçiniz —"));
        yazarlar.forEach(yazarCB::addItem);

        yayineviCB.addItem(new YayinEvi(0, "— Seçiniz —"));
        yayinevleri.forEach(yayineviCB::addItem);

        // ComboBox stilleri
        for (JComboBox<?> cb : new JComboBox[]{kategoriCB, yazarCB, yayineviCB}) {
            stilComboBox(cb);
            cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        }

        // --- JSpinner (Kopya Sayısı) ---
        JSpinner kopyaSayisiSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        kopyaSayisiSpinner.setFont(FONT_NORMAL); 
        kopyaSayisiSpinner.setToolTipText("Sistemde bu kitaba ait kaç adet fiziksel kopya (barkod) oluşturulacak?");
        kopyaSayisiSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // --- Form satırları ---
        String[]  etiketler = {"Başlık *", "ISBN *", "Yayın Yılı", "Kategori", "Yazar", "Yayınevi", "Kopya Sayısı"};
        Component[] bilesen = {baslikAlani, isbnAlani, yilAlani, kategoriCB, yazarCB, yayineviCB, kopyaSayisiSpinner};

        for (int i = 0; i < etiketler.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.30;
            JLabel lbl = new JLabel(etiketler[i]); lbl.setFont(FONT_ETIKET); lbl.setForeground(RENK_METIN_SOLUK);
            form.add(lbl, g);
            g.gridx = 1; g.weightx = 0.70;
            form.add(bilesen[i], g);
        }

        // Alt not + "Sistem Ayarları'ndan" ipucu
        g.gridx = 0; g.gridy = etiketler.length; g.gridwidth = 2; g.weightx = 1.0;
        JLabel not = new JLabel("<html>* Zorunlu alan. &nbsp; Listede görmediğiniz yazar/kategori/yayınevi için<br>" +
                "<b>Sistem Ayarları</b> sekmesinden önce ekleyebilirsiniz.</html>");
        not.setFont(new Font("Segoe UI", Font.ITALIC, 10)); not.setForeground(RENK_METIN_SOLUK);
        not.setBorder(new EmptyBorder(4, 0, 0, 0));
        form.add(not, g);

        // --- Butonlar ---
        JPanel butonlar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        butonlar.setBackground(RENK_KART);
        JButton iptal  = renkliButonOlustur("İptal",  new Color(80, 80, 110), new Color(100, 100, 130));
        JButton kaydet = renkliButonOlustur("Kaydet", RENK_BASARI, RENK_BASARI.brighter());

        iptal.addActionListener(e -> d.dispose());
        kaydet.addActionListener(e -> {
            // Doğrulama
            if (baslikAlani.getText().trim().isEmpty() || isbnAlani.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Başlık ve ISBN zorunludur.", "Eksik Alan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Kitap k = new Kitap();
            k.setBaslik(baslikAlani.getText().trim());
            k.setIsbn(isbnAlani.getText().trim());
            try {
                k.setYayinYili(yilAlani.getText().trim().isEmpty() ? 0 : Integer.parseInt(yilAlani.getText().trim()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Yayın yılı sayısal olmalıdır.", "Hata", JOptionPane.ERROR_MESSAGE); return;
            }
            // ID'leri seçilen nesnelerden al (0 = seçilmedi → null saklanır)
            Kategori  secKat = (Kategori)  kategoriCB.getSelectedItem();
            Yazar     secYaz = (Yazar)     yazarCB.getSelectedItem();
            YayinEvi  secYev = (YayinEvi)  yayineviCB.getSelectedItem();
            k.setKategoriId(secKat  != null ? secKat.getId()  : 0);
            k.setYazarId   (secYaz  != null ? secYaz.getId()  : 0);
            k.setYayineviId(secYev  != null ? secYev.getId()  : 0);
            
            int kopyaSayisi = (Integer) kopyaSayisiSpinner.getValue();

            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return kitapDAO.kitapEkle(k, kopyaSayisi); }
                @Override protected void done() {
                    try {
                        if (get()) { d.dispose(); kitaplariYukle(); basariMesaji("Kitap ve " + kopyaSayisi + " adet fiziksel kopyası başarıyla eklendi."); }
                        else hataGoster("Kitap eklenemedi.");
                    } catch (Exception ex) { hataGoster("Hata: " + ex.getMessage()); }
                }
            }.execute();
        });
        butonlar.add(iptal); butonlar.add(kaydet);
        d.add(form, BorderLayout.CENTER); d.add(butonlar, BorderLayout.SOUTH); d.setVisible(true);
    }

    private void seciliKitabiSil() {
        int satir = kitapTablosu.getSelectedRow();
        if (satir < 0) { JOptionPane.showMessageDialog(this, "Silmek için bir kitap seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE); return; }
        int    id     = (int)    kitapModeliTablo.getValueAt(kitapTablosu.convertRowIndexToModel(satir), 0);
        String baslik = (String) kitapModeliTablo.getValueAt(kitapTablosu.convertRowIndexToModel(satir), 1);
        
        String uyariMesaji = "<html><body><h3 style='margin-top:0; color:#DC2626;'>⚠️ Dikkat: Kritik Silme İşlemi!</h3>" +
                             "<b>\"" + baslik + "\"</b> adlı kitabı sistemden silmek üzeresiniz.<br><br>" +
                             "Eğer bu kitabı silerseniz şunlar da <b>kalıcı olarak silinecektir:</b><br>" +
                             "&bull; Kitabın tüm kopyaları (barkodları),<br>" +
                             "&bull; Geçmiş ve aktif <b>tüm ödünç/iade kayıtları</b>,<br>" +
                             "&bull; Bu ödünç işlemlerine uygulanmış tüm <b>cezalar</b>.<br><br>" +
                             "<i>Sadece test kitaplarını veya veri tabanını temizlemek için onay veriniz.<br>" +
                             "Emin misiniz?</i></body></html>";

        if (JOptionPane.showConfirmDialog(this, uyariMesaji,
                "Kalıcı Silme Onayı", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return kitapDAO.kitapSil(id); }
                @Override protected void done() {
                    try { 
                        if (get()) { kitaplariYukle(); basariMesaji("Kitap ve tüm bağlantılı verileri (geçmiş) başarıyla silindi."); }
                        else hataGoster("Silme başarısız.");
                    } catch (Exception ex) { 
                        Throwable t = ex.getCause() != null ? ex.getCause() : ex;
                        hataGoster("Bir hata oluştu: " + t.getMessage()); 
                    }
                }
            }.execute();
        }
    }

    // ================================================================
    // SEKME 2 — KULLANICI YÖNETİMİ
    // ================================================================
    private JPanel kullaniciYonetimiPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(RENK_ARKA_PLAN); panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel arac = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); arac.setOpaque(false);
        JButton yenile = renkliButonOlustur("⟳ Yenile",          RENK_VURGU,                 RENK_VURGU_HOVER);
        JButton ekle   = renkliButonOlustur("+ Kullanıcı Ekle",  RENK_BASARI,                RENK_BASARI.brighter());
        JButton durum  = renkliButonOlustur("⚡ Durum Değiştir",  new Color(180,120,30),      new Color(210,150,50));
        yenile.addActionListener(e -> kullanicilariYukle());
        ekle.addActionListener(e   -> kullaniciEkleDiyaloguGoster());
        durum.addActionListener(e  -> seciliKullaniciDurumDegistir());
        arac.add(yenile); arac.add(durum); arac.add(ekle);

        String[] s = {"ID", "Ad Soyad", "E-posta", "Rol", "Durum"};
        kullaniciModeliTablo = new DefaultTableModel(s, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        kullaniciTablosu = standartTablo(kullaniciModeliTablo);
        kullaniciTablosu.setRowSorter(new TableRowSorter<>(kullaniciModeliTablo));
        int[] w = {40, 200, 230, 100, 80};
        for (int i = 0; i < w.length; i++) kullaniciTablosu.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        panel.add(arac, BorderLayout.NORTH); panel.add(tabloSarici(kullaniciTablosu), BorderLayout.CENTER);
        kullanicilariYukle();
        return panel;
    }

    private void kullanicilariYukle() {
        new SwingWorker<List<Kullanici>, Void>() {
            @Override protected List<Kullanici> doInBackground() { return kullaniciDAO.tumunuGetir(); }
            @Override protected void done() {
                try {
                    kullaniciModeliTablo.setRowCount(0);
                    for (Kullanici k : get()) kullaniciModeliTablo.addRow(
                            new Object[]{k.getId(), nvl(k.getAdSoyad()), k.getEmail(), k.getRol(), k.getDurum()});
                } catch (Exception ex) { hataGoster("Kullanıcılar yüklenemedi: " + ex.getMessage()); }
            }
        }.execute();
    }

    private void kullaniciEkleDiyaloguGoster() {
        JDialog d = new JDialog(this, "Yeni Kullanıcı Ekle", true);
        d.setSize(440, 310); d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout()); d.getContentPane().setBackground(RENK_KART);

        JTextField[] alanlar = {tekAlaniOlustur(), tekAlaniOlustur(), tekAlaniOlustur()};
        String[] etiketler = {"Ad Soyad *", "E-posta *", "Şifre *"};
        JComboBox<String> rolCB = new JComboBox<>(new String[]{"UYE", "YONETICI"});
        stilComboBox(rolCB);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(RENK_KART); form.setBorder(new EmptyBorder(22, 26, 14, 26));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(6, 4, 6, 4);
        for (int i = 0; i < 3; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.35;
            JLabel l = new JLabel(etiketler[i]); l.setFont(FONT_ETIKET); l.setForeground(RENK_METIN_SOLUK); form.add(l, g);
            g.gridx = 1; g.weightx = 0.65; form.add(alanlar[i], g);
        }
        g.gridx = 0; g.gridy = 3; g.weightx = 0.35;
        JLabel rl = new JLabel("Rol"); rl.setFont(FONT_ETIKET); rl.setForeground(RENK_METIN_SOLUK); form.add(rl, g);
        g.gridx = 1; g.weightx = 0.65; form.add(rolCB, g);

        JPanel butonlar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        butonlar.setBackground(RENK_KART);
        JButton iptal  = renkliButonOlustur("İptal",  new Color(80,80,110), new Color(100,100,130));
        JButton kaydet = renkliButonOlustur("Kaydet", RENK_BASARI, RENK_BASARI.brighter());
        iptal.addActionListener(e -> d.dispose());
        kaydet.addActionListener(e -> {
            if (alanlar[0].getText().trim().isEmpty() || alanlar[1].getText().trim().isEmpty() || alanlar[2].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Zorunlu alanlar doldurun.", "Eksik Alan", JOptionPane.WARNING_MESSAGE); return;
            }
            Kullanici yeni = new Kullanici();
            yeni.setAdSoyad(alanlar[0].getText().trim()); yeni.setEmail(alanlar[1].getText().trim());
            yeni.setSifre(alanlar[2].getText().trim()); yeni.setRol((String) rolCB.getSelectedItem()); yeni.setDurum("AKTIF");
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return kullaniciDAO.kullaniciEkle(yeni); }
                @Override protected void done() {
                    try { if (get()) { d.dispose(); kullanicilariYukle(); basariMesaji("Kullanıcı eklendi."); }
                    } catch (Exception ex) { hataGoster("Hata: " + ex.getMessage()); }
                }
            }.execute();
        });
        butonlar.add(iptal); butonlar.add(kaydet);
        d.add(form, BorderLayout.CENTER); d.add(butonlar, BorderLayout.SOUTH); d.setVisible(true);
    }

    private void seciliKullaniciDurumDegistir() {
        int satir = kullaniciTablosu.getSelectedRow();
        if (satir < 0) { JOptionPane.showMessageDialog(this, "Lütfen bir kullanıcı seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE); return; }
        int    id          = (int)    kullaniciModeliTablo.getValueAt(kullaniciTablosu.convertRowIndexToModel(satir), 0);
        String mevcutDurum = (String) kullaniciModeliTablo.getValueAt(kullaniciTablosu.convertRowIndexToModel(satir), 4);
        String yeniDurum   = "AKTIF".equals(mevcutDurum) ? "PASIF" : "AKTIF";
        if (JOptionPane.showConfirmDialog(this, "Kullanıcı durumu '" + yeniDurum + "' yapılsın mı?",
                "Durum Değiştir", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return kullaniciDAO.durumuGuncelle(id, yeniDurum); }
                @Override protected void done() {
                    try { if (get()) kullanicilariYukle(); } catch (Exception ex) { hataGoster(ex.getMessage()); }
                }
            }.execute();
        }
    }

    // ================================================================
    // SEKME 3 — ÖDÜNÇ İŞLEMLERİ
    // ================================================================

    // İstatistik levelları
    private JLabel statToplamKitap, statAktifOdunc, statGecikmis;

    private JPanel oduncIslemleriPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(RENK_ARKA_PLAN); panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // --- ÜST: İstatistik Kartları ---
        JPanel statPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        statPanel.setOpaque(false);
        statToplamKitap = istatistikEtiketiOlustur("0");
        statAktifOdunc  = istatistikEtiketiOlustur("0");
        statGecikmis    = istatistikEtiketiOlustur("0");
        statPanel.add(istatistikKartiOlustur("📚 Sistemdeki Toplam Kitap", statToplamKitap, new Color(59, 130, 246)));
        statPanel.add(istatistikKartiOlustur("🔄 Devam Eden Ödünçler", statAktifOdunc, new Color(16, 185, 129)));
        statPanel.add(istatistikKartiOlustur("⚠️ Gecikmiş İadeler", statGecikmis, RENK_TEHLIKE));

        // --- ORTA: Formlar ---
        JPanel formPaneli = new JPanel(new GridLayout(1, 2, 16, 0));
        formPaneli.setOpaque(false);
        formPaneli.add(oduncVerFormuOlustur());
        formPaneli.add(iadeAlFormuOlustur());

        // Üst panel (İstatistik + Formlar)
        JPanel ustWrap = new JPanel(new BorderLayout(0, 16));
        ustWrap.setOpaque(false);
        ustWrap.add(statPanel, BorderLayout.NORTH);
        ustWrap.add(formPaneli, BorderLayout.CENTER);

        // --- ALT: Tablo ---
        JLabel baslik = new JLabel("  Devam Eden Ödünç İşlemleri");
        baslik.setFont(FONT_ETIKET); baslik.setForeground(RENK_METIN);
        JButton yenile = renkliButonOlustur("⟳ Yenile", RENK_VURGU, RENK_VURGU_HOVER);
        yenile.addActionListener(e -> { aktifOduncleriYukle(); istatistikleriGuncelle(); });
        JPanel altBaslik = new JPanel(new BorderLayout()); altBaslik.setOpaque(false);
        altBaslik.add(baslik, BorderLayout.WEST); altBaslik.add(yenile, BorderLayout.EAST);
        altBaslik.setBorder(new EmptyBorder(0, 0, 6, 0));

        String[] s = {"ID", "Kitap", "Barkod", "Kullanıcı", "Verildi", "Son Teslim", "Gecikme (gün)"};
        oduncModeliTablo = new DefaultTableModel(s, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        oduncTablosu = standartTablo(oduncModeliTablo);
        int[] w = {40, 250, 100, 180, 90, 90, 100};
        for (int i = 0; i < w.length; i++) oduncTablosu.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        JPanel altPanel = new JPanel(new BorderLayout(0, 6)); altPanel.setOpaque(false);
        altPanel.add(altBaslik, BorderLayout.NORTH); altPanel.add(tabloSarici(oduncTablosu), BorderLayout.CENTER);

        panel.add(ustWrap, BorderLayout.NORTH);
        panel.add(altPanel, BorderLayout.CENTER);

        aktifOduncleriYukle();
        istatistikleriGuncelle();
        return panel;
    }

    private JLabel istatistikEtiketiOlustur(String deger) {
        JLabel l = new JLabel(deger);
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        l.setForeground(Color.WHITE);
        return l;
    }

    private JPanel istatistikKartiOlustur(String baslik, JLabel degerLabel, Color renk) {
        JPanel kart = new JPanel(new BorderLayout(0, 4));
        kart.setBackground(RENK_KART);
        kart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, renk), // Sol tarafa kalın renkli çizgi
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel lbl = new JLabel(baslik);
        lbl.setFont(FONT_ETIKET);
        lbl.setForeground(RENK_METIN_SOLUK);

        kart.add(lbl, BorderLayout.NORTH);
        kart.add(degerLabel, BorderLayout.CENTER);
        return kart;
    }

    private void istatistikleriGuncelle() {
        new SwingWorker<int[], Void>() {
            @Override protected int[] doInBackground() {
                try {
                    int toplamKitap = kitapDAO.tumunuGetir().size();
                    List<OduncIslemi> aktifler = oduncDAO.devamEdenleriGetir();
                    int aktifSayisi = aktifler.size();
                    int gecikenSayisi = 0;
                    for (OduncIslemi o : aktifler) {
                        if (o.gecikmeGunSayisi() > 0) gecikenSayisi++;
                    }
                    return new int[]{toplamKitap, aktifSayisi, gecikenSayisi};
                } catch (Exception e) { return new int[]{0,0,0}; }
            }
            @Override protected void done() {
                try {
                    int[] sonuclar = get();
                    statToplamKitap.setText(String.valueOf(sonuclar[0]));
                    statAktifOdunc.setText(String.valueOf(sonuclar[1]));
                    statGecikmis.setText(String.valueOf(sonuclar[2]));
                } catch (Exception ignored) {}
            }
        }.execute();
    }
    // ================================================================

    private JPanel oduncVerFormuOlustur() {
        JPanel kart = kartPaneliOlustur("📤  Ödünç Ver");

        // --- BARKOD ALANI ARTIK JCOMBOBOX ---
        musaitBarkodlarCB = new JComboBox<>();
        stilComboBox(musaitBarkodlarCB);
        musaitBarkodlariYukle(); // Listeyi veritabanından çeken metod

        JTextField kullaniciAlani = tekAlaniOlustur(); kullaniciAlani.setToolTipText("Kullanıcı ID");
        JTextField teslimAlani    = tekAlaniOlustur(); teslimAlani.setToolTipText("gg.aa.yyyy");
        teslimAlani.setText(LocalDate.now().plusDays(14).format(TARIH_FORMAT));

        kart.add(formSatiri("Barkod *",        musaitBarkodlarCB));
        kart.add(formSatiri("Kullanıcı ID *",  kullaniciAlani));
        kart.add(formSatiri("Teslim Tarihi *", teslimAlani));
        kart.add(Box.createVerticalStrut(8));

        JButton btn = renkliButonOlustur("Ödünç Ver", RENK_BASARI, RENK_BASARI.brighter());
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.addActionListener(e -> {
            Object secilen = musaitBarkodlarCB.getSelectedItem();
            if (secilen == null || secilen.toString().contains("— Seçiniz") || secilen.toString().contains("Müsait Kitap Yok")) {
                JOptionPane.showMessageDialog(this, "Lütfen listeden müsait bir kitap seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE); return;
            }

            // "BRK-123456 - Suç ve Ceza" yazısından sadece barkodu koparıp alıyoruz
            String barkod = secilen.toString().split(" - ")[0].trim();
            String kulId  = kullaniciAlani.getText().trim();
            String teslim = teslimAlani.getText().trim();

            if (kulId.isEmpty() || teslim.isEmpty()) { JOptionPane.showMessageDialog(this, "Tüm alanlar zorunludur.", "Uyarı", JOptionPane.WARNING_MESSAGE); return; }
            LocalDate td; int kid;
            try { td = LocalDate.parse(teslim, TARIH_FORMAT); } catch (DateTimeParseException ex) { JOptionPane.showMessageDialog(this, "Tarih formatı: gg.aa.yyyy", "Hata", JOptionPane.ERROR_MESSAGE); return; }
            if (td.isBefore(LocalDate.now())) { JOptionPane.showMessageDialog(this, "Teslim tarihi geçmişte olamaz.", "Uyarı", JOptionPane.WARNING_MESSAGE); return; }
            try { kid = Integer.parseInt(kulId); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Kullanıcı ID sayısal olmalıdır.", "Hata", JOptionPane.ERROR_MESSAGE); return; }

            final int kIdF = kid; final LocalDate tdF = td;
            new SwingWorker<Integer, Void>() {
                @Override protected Integer doInBackground() { return oduncDAO.oduncVer(barkod, kIdF, tdF); }
                @Override protected void done() {
                    try {
                        String msj = String.format("<html><body><h3 style='margin-top:0; color:#10B981;'>✅ Ödünç Başarıyla Oluşturuldu</h3>" +
                                "<b>İşlem Numarası:</b> %d<br><br>" +
                                "🗓️ <b>Veriliş Tarihi:</b> %s<br>" +
                                "📆 <b>Son Teslim:</b> <span style='color:#EF4444;'><b>%s</b></span><br><br>" +
                                "<i>Kitabın zamanında iade edilmesi önemlidir.</i></body></html>",
                                get(), LocalDate.now().format(TARIH_FORMAT), tdF.format(TARIH_FORMAT));
                        
                        JOptionPane.showMessageDialog(AnaMenuUI.this, msj, "İşlem Başarılı", JOptionPane.INFORMATION_MESSAGE);
                        
                        kullaniciAlani.setText("");
                        teslimAlani.setText(LocalDate.now().plusDays(14).format(TARIH_FORMAT));
                        aktifOduncleriYukle();
                        musaitBarkodlariYukle(); // Listeyi güncelle ki verdiğimiz kitap oradan kaybolsun!
                        istatistikleriGuncelle();
                    }
                    catch (Exception ex) { hataGoster("Ödünç verilemedi: " + ex.getCause().getMessage()); }
                }
            }.execute();
        });
        kart.add(btn);
        return kart;
    }

    // ComboBox'ı güncelleyen yardımcı metod
    public void musaitBarkodlariYukle() {
        if (musaitBarkodlarCB == null) return;
        new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() { return kitapDAO.getMusaitBarkodlar(); }
            @Override protected void done() {
                try {
                    musaitBarkodlarCB.removeAllItems();
                    List<String> list = get();
                    if (list.isEmpty()) {
                        musaitBarkodlarCB.addItem("— Müsait Kitap Yok —");
                    } else {
                        musaitBarkodlarCB.addItem("— Seçiniz —");
                        list.forEach(musaitBarkodlarCB::addItem);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private JPanel iadeAlFormuOlustur() {
        JPanel kart = kartPaneliOlustur("📥  İade Al");
        JTextField oduncIdAlani = tekAlaniOlustur();
        kart.add(formSatiri("Ödünç ID *", oduncIdAlani));
        kart.add(Box.createVerticalStrut(6));
        JLabel bilgi = new JLabel("<html>İade tarihi bugün olarak kaydedilir.<br>Gecikme varsa ceza otomatik hesaplanır.</html>");
        bilgi.setFont(new Font("Segoe UI", Font.ITALIC, 11)); bilgi.setForeground(RENK_METIN_SOLUK);
        bilgi.setAlignmentX(Component.LEFT_ALIGNMENT); bilgi.setBorder(new EmptyBorder(2, 2, 8, 0));
        kart.add(bilgi);
        JButton btn = renkliButonOlustur("İade Al", RENK_VURGU, RENK_VURGU_HOVER);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.addActionListener(e -> {
            String idStr = oduncIdAlani.getText().trim();
            if (idStr.isEmpty()) { JOptionPane.showMessageDialog(this, "Ödünç ID giriniz.", "Uyarı", JOptionPane.WARNING_MESSAGE); return; }
            int oid; try { oid = Integer.parseInt(idStr); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "ID sayısal olmalıdır.", "Hata", JOptionPane.ERROR_MESSAGE); return; }

            int oduncIdF = oid;
            OduncIslemi detay = oduncDAO.idIleBul(oduncIdF);
            if (detay == null) {
                JOptionPane.showMessageDialog(this, "Ödünç işlemi bulunamadı. Lütfen geçerli bir ID girin.", "Hata", JOptionPane.ERROR_MESSAGE); return;
            }

            new SwingWorker<Ceza, Void>() {
                @Override protected Ceza doInBackground() { return oduncDAO.iadeAl(oduncIdF); }
                @Override protected void done() {
                    try {
                        Ceza ceza = get();
                        String verilisStr = detay.getVerilisTarihi() != null ? detay.getVerilisTarihi().format(TARIH_FORMAT) : "?";
                        String iadeStr   = LocalDate.now().format(TARIH_FORMAT);

                        if (ceza != null) {
                            String cMsj = String.format("<html><body><h3 style='margin-top:0; color:#F59E0B;'>⚠️ İade Alındı (Gecikmeli)</h3>" +
                                "🗓️ <b>Veriliş Tarihi:</b> %s<br>" +
                                "📆 <b>İade Edilen Tarih:</b> %s<br><br>" +
                                "<div style='background-color:#FEF2F2; color:#DC2626; padding:8px; border-left:4px solid #DC2626;'>" +
                                "<b>Uygulanan Ceza:</b> %s TL<br>" +
                                "<i>%s</i></div></body></html>",
                                verilisStr, iadeStr, ceza.getCezaMiktari(), ceza.getAciklama());

                            JOptionPane.showMessageDialog(AnaMenuUI.this, cMsj, "İade & Ceza", JOptionPane.WARNING_MESSAGE);
                        } else {
                            String bMsj = String.format("<html><body><h3 style='margin-top:0; color:#10B981;'>✅ İade Alındı (Zamanında)</h3>" +
                                "🗓️ <b>Veriliş Tarihi:</b> %s<br>" +
                                "📆 <b>İade Edilen Tarih:</b> %s<br><br>" +
                                "<i>Kitap tam vaktinde iade edildi, ceza işlemi uygulanmadı.</i></body></html>",
                                verilisStr, iadeStr);
                                
                            JOptionPane.showMessageDialog(AnaMenuUI.this, bMsj, "İade İşlemi Başarılı", JOptionPane.INFORMATION_MESSAGE);
                        }
                        oduncIdAlani.setText("");
                        aktifOduncleriYukle();
                        musaitBarkodlariYukle();
                        istatistikleriGuncelle();
                    } catch (Exception ex) { hataGoster("İade alınamadı: " + ex.getCause().getMessage()); }
                }
            }.execute();
        });
        kart.add(btn);
        return kart;
    }

    private void aktifOduncleriYukle() {
        new SwingWorker<List<OduncIslemi>, Void>() {
            @Override protected List<OduncIslemi> doInBackground() { return oduncDAO.devamEdenleriGetir(); }
            @Override protected void done() {
                try {
                    oduncModeliTablo.setRowCount(0);
                    for (OduncIslemi o : get()) {
                        long gec = o.gecikmeGunSayisi();
                        oduncModeliTablo.addRow(new Object[]{o.getId(), nvl(o.getKitapBaslik()), nvl(o.getBarkod()),
                                nvl(o.getKullaniciAdi()),
                                o.getVerilisTarihi() != null ? o.getVerilisTarihi().format(TARIH_FORMAT) : "—",
                                o.getTeslimTarihi()  != null ? o.getTeslimTarihi().format(TARIH_FORMAT)  : "—",
                                gec > 0 ? gec + " ⚠️" : "0"});
                    }
                } catch (Exception ex) { hataGoster("Liste yüklenemedi: " + ex.getMessage()); }
            }
        }.execute();
    }

    // ================================================================
    // SEKME 4 — SİSTEM AYARLARI
    // ================================================================
    private JPanel sistemAyarlariPaneliOlustur() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 14, 0));
        panel.setBackground(RENK_ARKA_PLAN); panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        panel.add(lookupKartiOlustur("📂  Kategoriler", "Kategori adı",
                kategoriModeli = lookupModeli(),
                ad  -> { Kategori k = kategoriDAO.ekle(ad); kategoriModeli.addRow(new Object[]{k.getId(), k.getKategoriAdi()}); },
                () -> kategoriDAO.tumunuGetir().forEach(k -> kategoriModeli.addRow(new Object[]{k.getId(), k.getKategoriAdi()})),
                (id, ad) -> kategoriDAO.guncelle(id, ad),
                id  -> kategoriDAO.sil(id)
        ));

        panel.add(lookupKartiOlustur("✍️  Yazarlar", "Yazar adı",
                yazarModeli = lookupModeli(),
                ad  -> { Yazar y = yazarDAO.ekle(ad); yazarModeli.addRow(new Object[]{y.getId(), y.getYazarAdi()}); },
                () -> yazarDAO.tumunuGetir().forEach(y -> yazarModeli.addRow(new Object[]{y.getId(), y.getYazarAdi()})),
                (id, ad) -> yazarDAO.guncelle(id, ad),
                id  -> yazarDAO.sil(id)
        ));

        panel.add(lookupKartiOlustur("🏢  Yayınevleri", "Yayınevi adı",
                yayineviModeli = lookupModeli(),
                ad  -> { YayinEvi yv = yayineviDAO.ekle(ad); yayineviModeli.addRow(new Object[]{yv.getId(), yv.getYayineviAdi()}); },
                () -> yayineviDAO.tumunuGetir().forEach(yv -> yayineviModeli.addRow(new Object[]{yv.getId(), yv.getYayineviAdi()})),
                (id, ad) -> yayineviDAO.guncelle(id, ad),
                id  -> yayineviDAO.sil(id)
        ));

        return panel;
    }

    /**
     * Ortak lookup kartı fabrikası (Kategoriler / Yazarlar / Yayınevleri).
     * Tüm metin renkleri koyu tema için açıkça belirlenmiştir.
     *
     * @param baslik    kart başlığı
     * @param yerTutar  için arama alanı placeholder ipucu
     * @param model     täble model
     * @param ekleFn    yeni ad ekleme: Consumer&lt;String ad&gt;
     * @param yukle     listeyi (yeniden) yükleme Runnable
     * @param guncelFn  kaydı güncelleme: BiConsumer&lt;int id, String yeniAd&gt;
     * @param silFn     kaydı silme: Consumer&lt;int id&gt;
     */
    private JPanel lookupKartiOlustur(
            String baslik,
            String yerTutar,
            DefaultTableModel model,
            java.util.function.Consumer<String> ekleFn,
            Runnable yukle,
            java.util.function.BiFunction<Integer, String, Boolean> guncelFn,
            java.util.function.Function<Integer, Boolean> silFn) {

        // Ana kart paneli
        JPanel kart = new JPanel(new BorderLayout(0, 8));
        kart.setBackground(RENK_KART);
        kart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RENK_SINIR),
                new EmptyBorder(14, 14, 14, 14)));

        // --- Başlık ---
        JLabel lbl = new JLabel(baslik);
        lbl.setFont(FONT_ETIKET);
        lbl.setForeground(RENK_METIN);              // açık renk — kritik
        lbl.setBackground(RENK_KART);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));

        // --- Tablo (renk açıkça ayarlanmış standartTablo ile) ---
        JTable tablo = standartTablo(model);
        tablo.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablo.getColumnModel().getColumn(1).setPreferredWidth(220);
        // Tablo arka planı ve başlık rengi standartTablo'da zaten ayarlı;
        // ScrollPane viewport'unu da ekstra açıkça ayarlıyoruz:
        JScrollPane scroll = new JScrollPane(tablo);
        scroll.getViewport().setBackground(RENK_TABLO_SATIR1);
        scroll.setBorder(BorderFactory.createLineBorder(RENK_SINIR));
        // Ek: scroll kenar arka planı
        scroll.setBackground(RENK_KART);
        scroll.getViewport().setOpaque(true);

        // --- Ekle satırı ---
        JTextField adAlani = tekAlaniOlustur();
        adAlani.setToolTipText(yerTutar);

        JButton ekleBtn    = renkliButonOlustur("+ Ekle",   RENK_BASARI, RENK_BASARI.brighter());
        JButton duzenleBtn = renkliButonOlustur("✎ Düzenle", RENK_VURGU,  RENK_VURGU_HOVER);
        JButton silBtn     = renkliButonOlustur("✕ Sil",     RENK_TEHLIKE, RENK_TEHLIKE.brighter());
        JButton yenileBtn  = renkliButonOlustur("\u27F3",      new Color(70,70,100), new Color(90,90,130));

        // Ekle
        Runnable ekleAction = () -> {
            String ad = adAlani.getText().trim();
            if (ad.isEmpty()) { JOptionPane.showMessageDialog(kart, "Ad boş olamaz.", "Uyarı", JOptionPane.WARNING_MESSAGE); return; }
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() { ekleFn.accept(ad); return null; }
                @Override protected void done() {
                    try { get(); adAlani.setText(""); } catch (Exception ex) { hataGoster("Eklenemedi: " + ex.getMessage()); }
                }
            }.execute();
        };
        ekleBtn.addActionListener(e -> ekleAction.run());
        adAlani.addActionListener(e -> ekleAction.run());   // Enter

        // Düzenle
        duzenleBtn.addActionListener(e -> {
            int satir = tablo.getSelectedRow();
            if (satir < 0) { JOptionPane.showMessageDialog(kart, "Düzenlemek için bir satır seçin.", "Seçim Yok", JOptionPane.WARNING_MESSAGE); return; }
            int    id       = (int)    model.getValueAt(satir, 0);
            String mevcutAd = (String) model.getValueAt(satir, 1);
            String yeniAd = JOptionPane.showInputDialog(kart,
                    "Yeni adı girin:", mevcutAd);
            if (yeniAd == null || yeniAd.trim().isEmpty()) return;  // iptal
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return guncelFn.apply(id, yeniAd.trim()); }
                @Override protected void done() {
                    try {
                        if (get()) model.setValueAt(yeniAd.trim(), satir, 1);
                        else hataGoster("Güncelleme başarısız.");
                    } catch (Exception ex) { hataGoster("Hata: " + ex.getMessage()); }
                }
            }.execute();
        });

        // Sil
        silBtn.addActionListener(e -> {
            int satir = tablo.getSelectedRow();
            if (satir < 0) { JOptionPane.showMessageDialog(kart, "Silmek için bir satır seçin.", "Seçim Yok", JOptionPane.WARNING_MESSAGE); return; }
            int    id  = (int)    model.getValueAt(satir, 0);
            String ad  = (String) model.getValueAt(satir, 1);
            if (JOptionPane.showConfirmDialog(kart,
                    "\"" + ad + "\" silinsin mi?", "Silme Onayi",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return silFn.apply(id); }
                @Override protected void done() {
                    try {
                        if (get()) model.removeRow(satir);
                        else hataGoster("Silme başarısız. Kayıt başka tablolarda kullanılıyor olabilir.");
                    } catch (Exception ex) { hataGoster("Hata: " + ex.getMessage()); }
                }
            }.execute();
        });

        // Yenile
        yenileBtn.addActionListener(e -> {
            model.setRowCount(0);
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() { yukle.run(); return null; }
                @Override protected void done() { try { get(); } catch (Exception ignored) {} }
            }.execute();
        });

        // Buton satırı: sol = Ekle alanı + Ekle btn; sağ = Düzenle + Sil + Yenile
        JPanel ekleWrap = new JPanel(new BorderLayout(5, 0)); ekleWrap.setOpaque(false);
        ekleWrap.add(adAlani, BorderLayout.CENTER); ekleWrap.add(ekleBtn, BorderLayout.EAST);

        JPanel aksiyonlar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); aksiyonlar.setOpaque(false);
        aksiyonlar.add(duzenleBtn); aksiyonlar.add(silBtn); aksiyonlar.add(yenileBtn);

        JPanel altSatir = new JPanel(new BorderLayout(8, 0));
        altSatir.setBackground(RENK_KART);
        altSatir.add(ekleWrap,   BorderLayout.CENTER);
        altSatir.add(aksiyonlar, BorderLayout.EAST);
        altSatir.setBorder(new EmptyBorder(6, 0, 0, 0));

        kart.add(lbl,      BorderLayout.NORTH);
        kart.add(scroll,   BorderLayout.CENTER);
        kart.add(altSatir, BorderLayout.SOUTH);

        // İlk yükleme
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() { yukle.run(); return null; }
            @Override protected void done() { try { get(); } catch (Exception ex) { hataGoster(ex.getMessage()); } }
        }.execute();

        return kart;
    }

    private DefaultTableModel lookupModeli() {
        return new DefaultTableModel(new String[]{"ID", "Ad"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    // ---------------------------------------------------------------
    // Çıkış
    // ---------------------------------------------------------------
    private void cikisYap() {
        if (JOptionPane.showConfirmDialog(this, "Oturumu kapatmak istediğinizden emin misiniz?",
                "Çıkış", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dispose(); new GirisUI().setVisible(true);
        }
    }

    // ---------------------------------------------------------------
    // Yardımcı UI Fabrikaları (tüm UI sınıflarından erişilebilir)
    // ---------------------------------------------------------------
    static JButton renkliButonOlustur(String metin, Color normal, Color hover) {
        JButton b = new JButton(metin);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setForeground(Color.WHITE);
        b.setBackground(normal); b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(normal); }
        });
        return b;
    }

    private JTextField tekAlaniOlustur() {
        JTextField alan = new JTextField();
        alan.setFont(FONT_NORMAL); alan.setForeground(RENK_METIN); alan.setBackground(RENK_PANEL);
        alan.setCaretColor(RENK_METIN);
        alan.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(RENK_SINIR), new EmptyBorder(7, 9, 7, 9)));
        alan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return alan;
    }

    private void stilComboBox(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
        cb.setBackground(new Color(45, 45, 65)); // Tablo header/satır rengine çok yakın lacivert
        cb.setForeground(Color.WHITE);
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RENK_SINIR),
                new EmptyBorder(4, 6, 4, 6)
        ));

        // Listenin içini özel boyamak için renderer (Hem Kitap Ekle hem Barkod listesi için çalışır)
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                
                if (isSelected) {
                    label.setBackground(RENK_VURGU);       // Mavi seçim efekti
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(new Color(45, 45, 65)); // Normal (Koyu Lâcivert arka plan)
                    label.setForeground(Color.WHITE);           // Metin BEMBEYAZ
                }
                label.setBorder(new EmptyBorder(10, 10, 10, 10)); // Ferah ferah listelensin
                return label;
            }
        });
    }

    private JTable standartTablo(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(FONT_NORMAL);
        t.setForeground(RENK_METIN);
        t.setBackground(RENK_TABLO_SATIR1);
        t.setSelectionBackground(RENK_TABLO_SECIM);
        t.setSelectionForeground(Color.WHITE);
        t.setGridColor(RENK_SINIR);


        // 1. SATIR VE SÜTUN AYARLARI
        t.setRowHeight(36); // Satır yüksekliğini artırdık. Daracık excel hücresi gibi durmasın, nefes alsın.
        t.setShowVerticalLines(false); // Dikey çizgileri gizledik. Modern web sitelerinde tabloların sadece yatay çizgileri olur.
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 2. TABLO BAŞLIKLARI (HEADER) AYARI
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setReorderingAllowed(false); // Sütunların fareyle sürüklenip yer değiştirmesini engelledik (düzen bozulmasın diye).

        // DefaultTableCellRenderer: Tablonun başlık hücrelerini boyayan fırçamız.
        javax.swing.table.DefaultTableCellRenderer headerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(45, 45, 65)); // Başlıkların arka planını gövdeden bir tık daha farklı bir lacivert yaptık.
        headerRenderer.setForeground(Color.WHITE); // Başlık yazısı bembeyaz.
        headerRenderer.setHorizontalAlignment(JLabel.CENTER); // "ID", "Başlık" gibi yazıları hücrenin tam ORTASINA hizaladık.
        headerRenderer.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5)); // Başlık kutusunun içinden biraz boşluk verdik.

        // Bu fırçayı tablodaki tüm sütunların başlıklarına uyguluyoruz.
        for (int i = 0; i < t.getModel().getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // 3. TABLO İÇİ HÜCRELERİN (VERİLERİN) AYARI
        javax.swing.table.DefaultTableCellRenderer cellRenderer = new javax.swing.table.DefaultTableCellRenderer();
        // Yazılar çizgiye sıfıra sıfır yapışmasın diye sağdan ve soldan 10 piksel görünmez boşluk (padding) ekledik.
        cellRenderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Bu ayarı tablodaki tüm veri hücrelerine (Object.class) uyguluyoruz.
        t.setDefaultRenderer(Object.class, cellRenderer);


        return t;
    }

    private JScrollPane tabloSarici(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.getViewport().setBackground(RENK_TABLO_SATIR1); sp.setBorder(BorderFactory.createLineBorder(RENK_SINIR));
        return sp;
    }

    private JPanel kartPaneliOlustur(String baslik) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(RENK_KART);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(RENK_SINIR), new EmptyBorder(16, 18, 16, 18)));
        JLabel lbl = new JLabel(baslik); lbl.setFont(FONT_ETIKET); lbl.setForeground(RENK_METIN);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT); lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.add(lbl);
        return p;
    }

    private JPanel formSatiri(String etiket, JComponent alan) {
        JPanel s = new JPanel(new BorderLayout(8, 0)); s.setOpaque(false);
        s.setAlignmentX(Component.LEFT_ALIGNMENT); s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        s.setBorder(new EmptyBorder(0, 0, 6, 0));
        JLabel l = new JLabel(etiket); l.setFont(FONT_ETIKET); l.setForeground(RENK_METIN_SOLUK);
        l.setPreferredSize(new Dimension(120, 30));
        s.add(l, BorderLayout.WEST); s.add(alan, BorderLayout.CENTER);
        return s;
    }

    /** Kısa bir "yükleniyor" dialog'u (lookup fetch sırasında). */
    private JDialog yukleniyor(String mesaj) {
        JDialog d = new JDialog(this, "", false);
        d.setUndecorated(true); d.setSize(220, 54); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(RENK_KART);
        p.setBorder(BorderFactory.createLineBorder(RENK_SINIR));
        JLabel l = new JLabel(mesaj); l.setFont(FONT_NORMAL); l.setForeground(RENK_METIN);
        p.add(l); d.setContentPane(p);
        return d;
    }

    private String nvl(String s) { return s != null ? s : "—"; }
    private void hataGoster(String m)   { JOptionPane.showMessageDialog(AnaMenuUI.this, m, "Hata", JOptionPane.ERROR_MESSAGE); }
    private void basariMesaji(String m) { JOptionPane.showMessageDialog(AnaMenuUI.this, m, "Başarılı", JOptionPane.INFORMATION_MESSAGE); }
}
