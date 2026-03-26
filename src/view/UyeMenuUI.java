package view;

import dao.CezaDAO;
import dao.KitapDAO;
import dao.OduncDAO;
import model.Ceza;
import model.Kitap;
import model.Kullanici;
import model.OduncIslemi;
import util.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * UyeMenuUI — 'UYE' rolüne sahip kullanıcılar için ana panel.
 *
 * Sekmeler:
 *  1. Kitap Ara           — başlık / ISBN / yazar araması
 *  2. Ödünçlerim          — mevcut ve geçmiş ödünç kayıtları, ceza bilgisi
 *  3. Rezervasyonlarım    — Phase 4 stub
 */
public class UyeMenuUI extends JFrame {

    private static final Color RENK_ARKA_PLAN    = AnaMenuUI.RENK_ARKA_PLAN;
    private static final Color RENK_PANEL        = AnaMenuUI.RENK_PANEL;
    private static final Color RENK_KART         = AnaMenuUI.RENK_KART;
    private static final Color RENK_VURGU        = AnaMenuUI.RENK_VURGU;
    private static final Color RENK_VURGU_HOVER  = AnaMenuUI.RENK_VURGU_HOVER;
    private static final Color RENK_TEHLIKE      = AnaMenuUI.RENK_TEHLIKE;
    private static final Color RENK_METIN        = AnaMenuUI.RENK_METIN;
    private static final Color RENK_METIN_SOLUK  = AnaMenuUI.RENK_METIN_SOLUK;
    private static final Color RENK_SINIR        = AnaMenuUI.RENK_SINIR;
    private static final Color RENK_TABLO_SATIR1 = AnaMenuUI.RENK_TABLO_SATIR1;
    private static final Color RENK_TABLO_SECIM  = AnaMenuUI.RENK_TABLO_SECIM;

    private static final Font FONT_BASLIK  = AnaMenuUI.FONT_BASLIK;
    private static final Font FONT_ETIKET  = AnaMenuUI.FONT_ETIKET;
    private static final Font FONT_NORMAL  = AnaMenuUI.FONT_NORMAL;
    private static final Font FONT_KUCUK   = AnaMenuUI.FONT_KUCUK;

    private static final DateTimeFormatter TARIH_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // ---------------------------------------------------------------
    // State & DAOs
    // ---------------------------------------------------------------
    private final Kullanici aktifKullanici;
    private final KitapDAO  kitapDAO  = new KitapDAO();
    private final OduncDAO  oduncDAO  = new OduncDAO();
    private final CezaDAO   cezaDAO   = new CezaDAO();

    // Sekme 1 — Kitap Arama
    private JTable            kitapTablosu;
    private DefaultTableModel kitapModeliTablo;
    private JTextField        aramaAlani;

    // Sekme 2 — Ödünçlerim
    private JTable            oduncTablosu;
    private DefaultTableModel oduncModeliTablo;
    private JTable            cezaTablosu;
    private DefaultTableModel cezaModeliTablo;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public UyeMenuUI(Kullanici kullanici) {
        this.aktifKullanici = kullanici;
        pencereAyarla();
        bilesenleriOlustur();
    }

    // ---------------------------------------------------------------
    // Pencere
    // ---------------------------------------------------------------
    private void pencereAyarla() {
        setTitle("Kütüphane Sistemi — Üye Paneli");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(850, 540));
        getContentPane().setBackground(RENK_ARKA_PLAN);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { DBConnection.baglantiKapat(); }
        });
    }

    private void bilesenleriOlustur() {
        setLayout(new BorderLayout());
        add(ustCubuguOlustur(), BorderLayout.NORTH);
        add(icerikPaneliOlustur(), BorderLayout.CENTER);
        add(durumCubuguOlustur(), BorderLayout.SOUTH);
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
        JLabel baslik = new JLabel("Kütüphane Sistemi"); baslik.setFont(FONT_BASLIK); baslik.setForeground(RENK_METIN);
        JLabel rol = new JLabel(" — ÜYE PANELİ"); rol.setFont(new Font("Segoe UI", Font.BOLD, 13)); rol.setForeground(new Color(52, 211, 153));
        sol.add(logo); sol.add(baslik); sol.add(rol);

        JPanel sag = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); sag.setOpaque(false);
        String ad = aktifKullanici.getAdSoyad() != null ? aktifKullanici.getAdSoyad() : aktifKullanici.getEmail();
        JLabel merhaba = new JLabel("Hoş geldiniz, " + ad); merhaba.setFont(FONT_KUCUK); merhaba.setForeground(RENK_METIN_SOLUK);
        JButton cikis = AnaMenuUI.renkliButonOlustur("Çıkış Yap", RENK_TEHLIKE, RENK_TEHLIKE.brighter());
        cikis.addActionListener(e -> cikisYap());
        sag.add(merhaba); sag.add(cikis);

        panel.add(sol, BorderLayout.WEST);
        panel.add(sag, BorderLayout.EAST);
        return panel;
    }

    // ---------------------------------------------------------------
    // İçerik Alanı
    // ---------------------------------------------------------------
    private JPanel icerikPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(RENK_ARKA_PLAN);
        panel.setBorder(new EmptyBorder(14, 14, 10, 14));

        // Karşılama kartı
        panel.add(karsılamaKartiOlustur(), BorderLayout.NORTH);

        JTabbedPane sekmeler = new JTabbedPane();
        sekmeler.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sekmeler.setBackground(RENK_ARKA_PLAN);
        sekmeler.addTab("🔍  Kitap Ara",        kitapAramaPaneliOlustur());
        sekmeler.addTab("📋  Ödünçlerim",       odunclerimPaneliOlustur());
        sekmeler.addTab("🔖  Rezervasyonlarım", placeHolder("Rezervasyonlarınız — Phase 4'te gelecek"));

        // Sekme değişiminde ödünç listesini yenile
        sekmeler.addChangeListener(e -> {
            if (sekmeler.getSelectedIndex() == 1) oduncleriYukle();
        });

        panel.add(sekmeler, BorderLayout.CENTER);
        return panel;
    }

    private JPanel karsılamaKartiOlustur() {
        JPanel kart = new JPanel(new BorderLayout());
        kart.setBackground(new Color(40, 40, 80));
        kart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RENK_VURGU, 1), new EmptyBorder(10, 16, 10, 16)));
        String ad = aktifKullanici.getAdSoyad() != null ? aktifKullanici.getAdSoyad() : aktifKullanici.getEmail();
        JLabel bilgi = new JLabel("<html><b>Merhaba, " + ad + "!</b>  " +
                "Kitap aramak için <b>Kitap Ara</b> sekmesini, ödünçleriniz için <b>Ödünçlerim</b> sekmesini kullanabilirsiniz.</html>");
        bilgi.setFont(FONT_NORMAL); bilgi.setForeground(RENK_METIN);
        kart.add(bilgi);
        return kart;
    }

    // ================================================================
    // SEKME 1 — KİTAP ARAMA
    // ================================================================
    private JPanel kitapAramaPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(RENK_ARKA_PLAN); panel.setBorder(new EmptyBorder(12, 8, 8, 8));

        JPanel aramaPanel = new JPanel(new BorderLayout(8, 0));
        aramaPanel.setOpaque(false); aramaPanel.setBorder(new EmptyBorder(0, 0, 8, 0));
        JLabel ikon = new JLabel("🔍"); ikon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        aramaAlani = tekAlaniOlustur();
        aramaAlani.setToolTipText("Başlık, ISBN veya yazar adı giriniz…");
        aramaAlani.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { kitaplariAra(aramaAlani.getText()); }
        });
        JButton araBtn = AnaMenuUI.renkliButonOlustur("Ara", RENK_VURGU, RENK_VURGU_HOVER);
        araBtn.addActionListener(e -> kitaplariAra(aramaAlani.getText()));
        aramaPanel.add(ikon, BorderLayout.WEST); aramaPanel.add(aramaAlani, BorderLayout.CENTER); aramaPanel.add(araBtn, BorderLayout.EAST);

        String[] s = {"ID", "Başlık", "Yazar", "ISBN", "Yayın Yılı", "Kategori", "Yayınevi"};
        kitapModeliTablo = new DefaultTableModel(s, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        kitapTablosu = standartTablo(kitapModeliTablo);
        int[] w = {40, 280, 160, 110, 80, 110, 130};
        for (int i = 0; i < w.length; i++) kitapTablosu.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        panel.add(aramaPanel, BorderLayout.NORTH);
        panel.add(tabloSarici(kitapTablosu), BorderLayout.CENTER);
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
                            nvl(k.getKategoriAdi()), nvl(k.getYayineviAdi())});
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
                            nvl(k.getKategoriAdi()), nvl(k.getYayineviAdi())});
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ================================================================
    // SEKME 2 — ÖDÜNÇLERİM
    // ================================================================
    private JPanel odunclerimPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(RENK_ARKA_PLAN); panel.setBorder(new EmptyBorder(12, 8, 8, 8));

        // Üst panel: Ödünç geçmişi
        JLabel oduncBaslik = new JLabel("  Ödünç Geçmişim");
        oduncBaslik.setFont(FONT_ETIKET); oduncBaslik.setForeground(RENK_METIN);

        JButton yenileBtn = AnaMenuUI.renkliButonOlustur("⟳ Yenile", RENK_VURGU, RENK_VURGU_HOVER);
        yenileBtn.addActionListener(e -> oduncleriYukle());

        JPanel baslikCubugu = new JPanel(new BorderLayout());
        baslikCubugu.setOpaque(false);
        baslikCubugu.add(oduncBaslik, BorderLayout.WEST);
        baslikCubugu.add(yenileBtn,   BorderLayout.EAST);
        baslikCubugu.setBorder(new EmptyBorder(0, 0, 6, 0));

        String[] oduncSutunlar = {"ID", "Kitap", "Barkod", "Verildi", "Son Teslim", "İade Edildi", "Durum"};
        oduncModeliTablo = new DefaultTableModel(oduncSutunlar, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        oduncTablosu = standartTablo(oduncModeliTablo);
        int[] ow = {40, 250, 100, 90, 90, 90, 110};
        for (int i = 0; i < ow.length; i++) oduncTablosu.getColumnModel().getColumn(i).setPreferredWidth(ow[i]);

        JPanel oduncPanel = new JPanel(new BorderLayout(0, 6));
        oduncPanel.setOpaque(false);
        oduncPanel.add(baslikCubugu, BorderLayout.NORTH);
        oduncPanel.add(tabloSarici(oduncTablosu), BorderLayout.CENTER);

        // Alt panel: Cezalarım
        JLabel cezaBaslik = new JLabel("  Cezalarım");
        cezaBaslik.setFont(FONT_ETIKET); cezaBaslik.setForeground(new Color(239, 68, 68));
        cezaBaslik.setBorder(new EmptyBorder(8, 0, 6, 0));

        String[] cezaSutunlar = {"ID", "Ödünç ID", "Tutar (TL)", "Açıklama", "Ödendi mi?"};
        cezaModeliTablo = new DefaultTableModel(cezaSutunlar, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cezaTablosu = standartTablo(cezaModeliTablo);
        int[] cw = {40, 80, 90, 350, 90};
        for (int i = 0; i < cw.length; i++) cezaTablosu.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        JPanel cezaPanel = new JPanel(new BorderLayout(0, 4));
        cezaPanel.setOpaque(false);
        cezaPanel.add(cezaBaslik, BorderLayout.NORTH);
        cezaPanel.add(tabloSarici(cezaTablosu), BorderLayout.CENTER);
        cezaPanel.setPreferredSize(new Dimension(0, 180));

        // İkiye böl
        JSplitPane bolme = new JSplitPane(JSplitPane.VERTICAL_SPLIT, oduncPanel, cezaPanel);
        bolme.setDividerLocation(280);
        bolme.setOpaque(false); bolme.setBackground(RENK_ARKA_PLAN);
        bolme.setBorder(null); bolme.setDividerSize(6);

        panel.add(bolme, BorderLayout.CENTER);
        oduncleriYukle();
        return panel;
    }

    private void oduncleriYukle() {
        // Ödünçleri yükle
        new SwingWorker<List<OduncIslemi>, Void>() {
            @Override protected List<OduncIslemi> doInBackground() {
                return oduncDAO.kullaniciOduncleriniGetir(aktifKullanici.getId());
            }
            @Override protected void done() {
                try {
                    oduncModeliTablo.setRowCount(0);
                    for (OduncIslemi o : get()) {
                        String durum = OduncIslemi.DURUM_DEVAM_EDIYOR.equals(o.getDurum())
                                ? (o.gecikmeGunSayisi() > 0 ? "⚠️ GECİKMELİ" : "📖 Devam Ediyor")
                                : "✅ Teslim Edildi";
                        oduncModeliTablo.addRow(new Object[]{
                                o.getId(), nvl(o.getKitapBaslik()), nvl(o.getBarkod()),
                                tarih(o.getVerilisTarihi()), tarih(o.getTeslimTarihi()),
                                o.getGercekTeslimTarihi() != null ? tarih(o.getGercekTeslimTarihi()) : "—",
                                durum});
                    }
                } catch (Exception ex) { hataGoster("Ödünçler yüklenemedi: " + ex.getMessage()); }
            }
        }.execute();

        // Cezaları yükle
        new SwingWorker<List<Ceza>, Void>() {
            @Override protected List<Ceza> doInBackground() {
                return cezaDAO.getirCezalarByKullanici(aktifKullanici.getId());
            }
            @Override protected void done() {
                try {
                    cezaModeliTablo.setRowCount(0);
                    for (Ceza c : get()) cezaModeliTablo.addRow(new Object[]{
                            c.getId(), c.getOduncId(),
                            c.getCezaMiktari() + " TL",
                            nvl(c.getAciklama()),
                            c.isOdendiMi() ? "✅ Evet" : "❌ Hayır"});
                } catch (Exception ex) { hataGoster("Cezalar yüklenemedi: " + ex.getMessage()); }
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Durum Çubuğu
    // ---------------------------------------------------------------
    private JPanel durumCubuguOlustur() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        panel.setBackground(RENK_KART);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RENK_SINIR));
        JLabel lbl = new JLabel("Kütüphane Yönetim Sistemi v1.0  |  Üye Paneli");
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11)); lbl.setForeground(RENK_METIN_SOLUK);
        panel.add(lbl);
        return panel;
    }

    // ---------------------------------------------------------------
    // Çıkış
    // ---------------------------------------------------------------
    private void cikisYap() {
        if (JOptionPane.showConfirmDialog(this, "Oturumu kapatmak istediğinizden emin misiniz?",
                "Çıkış", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dispose();
            new GirisUI().setVisible(true);
        }
    }

    // ---------------------------------------------------------------
    // Yardımcı Metotlar
    // ---------------------------------------------------------------
    private JTextField tekAlaniOlustur() {
        JTextField alan = new JTextField();
        alan.setFont(FONT_NORMAL); alan.setForeground(RENK_METIN);
        alan.setBackground(RENK_PANEL); alan.setCaretColor(RENK_METIN);
        alan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RENK_SINIR), new EmptyBorder(7, 9, 7, 9)));
        return alan;
    }

    private JTable standartTablo(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(FONT_NORMAL); t.setForeground(RENK_METIN); t.setBackground(RENK_TABLO_SATIR1);
        t.setSelectionBackground(RENK_TABLO_SECIM); t.setSelectionForeground(RENK_METIN);
        t.setGridColor(RENK_SINIR); t.setRowHeight(30); t.setShowVerticalLines(false);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(FONT_ETIKET); t.getTableHeader().setBackground(RENK_KART);
        t.getTableHeader().setForeground(RENK_METIN);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, RENK_SINIR));
        return t;
    }

    private JScrollPane tabloSarici(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.getViewport().setBackground(RENK_TABLO_SATIR1);
        sp.setBorder(BorderFactory.createLineBorder(RENK_SINIR));
        return sp;
    }

    private JPanel placeHolder(String mesaj) {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(RENK_ARKA_PLAN);
        JLabel l = new JLabel(mesaj, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.ITALIC, 15)); l.setForeground(RENK_METIN_SOLUK);
        p.add(l); return p;
    }

    private String nvl(String s)                                   { return s != null ? s : "—"; }
    private String tarih(java.time.LocalDate d)                    { return d != null ? d.format(TARIH_FORMAT) : "—"; }
    private void hataGoster(String m)                              {
        JOptionPane.showMessageDialog(UyeMenuUI.this, m, "Hata", JOptionPane.ERROR_MESSAGE);
    }
}
