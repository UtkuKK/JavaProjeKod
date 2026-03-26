package dao;

import model.Ceza;
import model.OduncIslemi;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * OduncDAO — Ödünç verme ve iade işlemlerini yöneten DAO sınıfı.
 *
 * İş Mantığı:
 *  • oduncVer : kopya_id için 'odunc_islemleri'ne INSERT; kopya durumunu 'ODUNCTE' yap.
 *  • iadeAl   : gercek_teslim_tarihi'ni güncelle; kopya durumunu 'MUSAIT' yap;
 *               gecikmeli iade varsa 'cezalar'a otomatik INSERT yap.
 *
 * Atomiklik: Her iki metod da birden fazla tabloya dokunur; hata durumunda
 * Connection.rollback() ile tüm değişiklikler geri alınır.
 *
 * Tüm sorgular PreparedStatement ile SQL Injection'a karşı korunmaktadır.
 */
public class OduncDAO {

    // ---------------------------------------------------------------
    // SQL Sabitleri
    // ---------------------------------------------------------------

    private static final String SQL_ODUNC_VER =
            "INSERT INTO odunc_islemleri " +
            "(kopya_id, kullanici_id, verilis_tarihi, teslim_tarihi, durum) " +
            "VALUES (?, ?, ?, ?, 'DEVAM_EDIYOR')";

    private static final String SQL_KOPYA_DURUM_GUNCELLE =
            "UPDATE kitap_kopyalari SET durum = ? WHERE id = ?";

    private static final String SQL_KOPYA_ID_AL =
            "SELECT id FROM kitap_kopyalari WHERE barkod = ? AND durum = 'MUSAIT'";

    private static final String SQL_IADE_GERCEK_TARIH =
            "UPDATE odunc_islemleri " +
            "SET gercek_teslim_tarihi = ?, durum = 'TESLIM_EDILDI' " +
            "WHERE id = ?";

    private static final String SQL_ODUNC_DETAY =
            "SELECT o.id, o.kopya_id, o.kullanici_id, o.verilis_tarihi, " +
            "       o.teslim_tarihi, o.gercek_teslim_tarihi, o.durum, " +
            "       kk.barkod, k.baslik AS kitap_baslik, ku.ad_Soyad " +
            "FROM odunc_islemleri o " +
            "JOIN kitap_kopyalari kk ON o.kopya_id       = kk.id " +
            "JOIN kitaplar        k  ON kk.kitap_id      = k.id " +
            "JOIN kullanicilar    ku ON o.kullanici_id   = ku.id " +
            "WHERE o.id = ?";

    private static final String SQL_DEVAM_EDEN_TUMU =
            "SELECT o.id, o.kopya_id, o.kullanici_id, o.verilis_tarihi, " +
            "       o.teslim_tarihi, o.gercek_teslim_tarihi, o.durum, " +
            "       kk.barkod, k.baslik AS kitap_baslik, ku.ad_Soyad " +
            "FROM odunc_islemleri o " +
            "JOIN kitap_kopyalari kk ON o.kopya_id     = kk.id " +
            "JOIN kitaplar        k  ON kk.kitap_id    = k.id " +
            "JOIN kullanicilar    ku ON o.kullanici_id = ku.id " +
            "WHERE o.durum = 'DEVAM_EDIYOR' " +
            "ORDER BY o.teslim_tarihi";

    private static final String SQL_KULLANICI_ODUNCLERI =
            "SELECT o.id, o.kopya_id, o.kullanici_id, o.verilis_tarihi, " +
            "       o.teslim_tarihi, o.gercek_teslim_tarihi, o.durum, " +
            "       kk.barkod, k.baslik AS kitap_baslik, ku.ad_Soyad " +
            "FROM odunc_islemleri o " +
            "JOIN kitap_kopyalari kk ON o.kopya_id     = kk.id " +
            "JOIN kitaplar        k  ON kk.kitap_id    = k.id " +
            "JOIN kullanicilar    ku ON o.kullanici_id = ku.id " +
            "WHERE o.kullanici_id = ? " +
            "ORDER BY o.id DESC";

    private static final String SQL_CEZA_EKLE =
            "INSERT INTO cezalar (kullanici_id, odunc_id, ceza_miktari, aciklama, odendi_mi) " +
            "VALUES (?, ?, ?, ?, FALSE)";

    // ---------------------------------------------------------------
    // Ödünç Verme
    // ---------------------------------------------------------------

    /**
     * Belirtilen barkodlu kopyayı kullanıcıya ödünç verir.
     *
     * <ol>
     *   <li>Barkodla kopyayı bulur; 'MUSAIT' değilse hata fırlatır.</li>
     *   <li>'odunc_islemleri'ne INSERT yapar.</li>
     *   <li>'kitap_kopyalari' durumunu 'ODUNCTE' olarak günceller.</li>
     *   <li>Herhangi bir adımda hata oluşursa bütün işlemi geri alır.</li>
     * </ol>
     *
     * @param barkod      ödünç verilecek kopyanın barkodu
     * @param kullaniciId ödünç alan kullanıcının ID'si
     * @param teslimTarihi beklenen iade tarihi
     * @return oluşturulan ödünç işleminin ID'si
     * @throws IllegalStateException kopya müsait değilse veya bulunamazsa
     */
    public int oduncVer(String barkod, int kullaniciId, LocalDate teslimTarihi) {
        Connection baglanti = DBConnection.getBaglanti();
        int oduncId = -1;

        try {
            baglanti.setAutoCommit(false);  // Transaction başlat

            // 1. Müsait kopyayı bul
            int kopyaId;
            try (PreparedStatement ps = baglanti.prepareStatement(SQL_KOPYA_ID_AL)) {
                ps.setString(1, barkod.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        baglanti.rollback();
                        throw new IllegalStateException(
                                "'" + barkod + "' barkodlu kopya bulunamadı veya şu anda müsait değil.");
                    }
                    kopyaId = rs.getInt("id");
                }
            }

            // 2. Ödünç kaydı oluştur
            try (PreparedStatement ps = baglanti.prepareStatement(
                    SQL_ODUNC_VER, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, kopyaId);
                ps.setInt(2, kullaniciId);
                ps.setDate(3, Date.valueOf(LocalDate.now()));
                ps.setDate(4, Date.valueOf(teslimTarihi));
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) oduncId = rs.getInt(1);
                }
            }

            // 3. Kopya durumunu güncelle → ODUNCTE
            try (PreparedStatement ps = baglanti.prepareStatement(SQL_KOPYA_DURUM_GUNCELLE)) {
                ps.setString(1, "ODUNCTE");
                ps.setInt(2, kopyaId);
                ps.executeUpdate();
            }

            baglanti.commit();  // Tüm adımlar başarılı

        } catch (IllegalStateException e) {
            throw e;
        } catch (SQLException e) {
            try { baglanti.rollback(); } catch (SQLException ex) { /* yoksay */ }
            throw new RuntimeException("Ödünç verme işlemi başarısız: " + e.getMessage(), e);
        } finally {
            try { baglanti.setAutoCommit(true); } catch (SQLException ex) { /* yoksay */ }
        }

        return oduncId;
    }

    // ---------------------------------------------------------------
    // İade Alma
    // ---------------------------------------------------------------

    /**
     * Belirtilen ödünç işlemini iade olarak kapatır.
     *
     * <ol>
     *   <li>Ödünç kaydını getirir; zaten iade edildiyse hata fırlatır.</li>
     *   <li>'gercek_teslim_tarihi'ni bugün olarak günceller; durumu 'TESLIM_EDILDI' yapar.</li>
     *   <li>Kopyayı 'MUSAIT' durumuna döndürür.</li>
     *   <li>Gecikme varsa (bugün > teslim_tarihi) otomatik ceza oluşturur ve 'cezalar'a ekler.</li>
     *   <li>Hata durumunda tüm değişiklikler geri alınır.</li>
     * </ol>
     *
     * @param oduncId iade edilecek ödünç işleminin ID'si
     * @return gecikme yoksa {@code null}, varsa oluşturulan {@link Ceza} nesnesi
     * @throws IllegalStateException ödünç kaydı bulunamazsa veya zaten iade edildiyse
     */
    public Ceza iadeAl(int oduncId) {
        Connection baglanti = DBConnection.getBaglanti();
        Ceza olusanCeza = null;

        try {
            baglanti.setAutoCommit(false);

            // 1. Ödünç kaydını getir
            OduncIslemi odunc = idIleBul(oduncId);
            if (odunc == null) {
                baglanti.rollback();
                throw new IllegalStateException("Ödünç işlemi bulunamadı: ID=" + oduncId);
            }
            if (OduncIslemi.DURUM_TESLIM_EDILDI.equals(odunc.getDurum())) {
                baglanti.rollback();
                throw new IllegalStateException("Bu kitap zaten iade edilmiş.");
            }

            LocalDate bugun = LocalDate.now();

            // 2. İade kaydını güncelle
            try (PreparedStatement ps = baglanti.prepareStatement(SQL_IADE_GERCEK_TARIH)) {
                ps.setDate(1, Date.valueOf(bugun));
                ps.setInt(2, oduncId);
                ps.executeUpdate();
            }

            // 3. Kopyayı MUSAIT durumuna getir
            try (PreparedStatement ps = baglanti.prepareStatement(SQL_KOPYA_DURUM_GUNCELLE)) {
                ps.setString(1, "MUSAIT");
                ps.setInt(2, odunc.getKopyaId());
                ps.executeUpdate();
            }

            // 4. Gecikme cezası hesapla ve ekle
            odunc.setGercekTeslimTarihi(bugun);
            long gecikmeGun = odunc.gecikmeGunSayisi();
            if (gecikmeGun > 0) {
                BigDecimal cezaTutar = BigDecimal.valueOf(odunc.cezaTutari());
                String aciklama = gecikmeGun + " gün gecikme nedeniyle otomatik ceza (" +
                        OduncIslemi.GUNLUK_CEZA_UCRETI + " TL/gün).";

                try (PreparedStatement ps = baglanti.prepareStatement(SQL_CEZA_EKLE)) {
                    ps.setInt(1, odunc.getKullaniciId());
                    ps.setInt(2, oduncId);
                    ps.setBigDecimal(3, cezaTutar);
                    ps.setString(4, aciklama);
                    ps.executeUpdate();
                }

                olusanCeza = new Ceza(odunc.getKullaniciId(), oduncId, cezaTutar, aciklama);
            }

            baglanti.commit();

        } catch (IllegalStateException e) {
            throw e;
        } catch (SQLException e) {
            try { baglanti.rollback(); } catch (SQLException ex) { /* yoksay */ }
            throw new RuntimeException("İade işlemi başarısız: " + e.getMessage(), e);
        } finally {
            try { baglanti.setAutoCommit(true); } catch (SQLException ex) { /* yoksay */ }
        }

        return olusanCeza;
    }

    // ---------------------------------------------------------------
    // Listeleme
    // ---------------------------------------------------------------

    /**
     * Devam eden tüm ödünç işlemlerini listeler (yönetici görünümü).
     */
    public List<OduncIslemi> devamEdenleriGetir() {
        return oduncListesiGetir(SQL_DEVAM_EDEN_TUMU, -1);
    }

    /**
     * Belirtilen kullanıcının tüm ödünç geçmişini getirir.
     *
     * @param kullaniciId kullanıcı ID'si
     */
    public List<OduncIslemi> kullaniciOduncleriniGetir(int kullaniciId) {
        return oduncListesiGetir(SQL_KULLANICI_ODUNCLERI, kullaniciId);
    }

    private List<OduncIslemi> oduncListesiGetir(String sql, int parametre) {
        List<OduncIslemi> liste = new ArrayList<>();
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(sql)) {
            if (parametre > 0) ps.setInt(1, parametre);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(satirOdunceDonustur(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ödünç listesi getirilirken hata: " + e.getMessage(), e);
        }
        return liste;
    }

    // ---------------------------------------------------------------
    // Tekil Getir
    // ---------------------------------------------------------------

    /**
     * ID'ye göre ödünç işlemini getirir.
     */
    public OduncIslemi idIleBul(int id) {
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_ODUNC_DETAY)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return satirOdunceDonustur(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ödünç işlemi getirilirken hata: " + e.getMessage(), e);
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Yardımcı
    // ---------------------------------------------------------------
    private OduncIslemi satirOdunceDonustur(ResultSet rs) throws SQLException {
        OduncIslemi o = new OduncIslemi();
        o.setId(rs.getInt("id"));
        o.setKopyaId(rs.getInt("kopya_id"));
        o.setKullaniciId(rs.getInt("kullanici_id"));

        Date vd = rs.getDate("verilis_tarihi");
        if (vd != null) o.setVerilisTarihi(vd.toLocalDate());

        Date td = rs.getDate("teslim_tarihi");
        if (td != null) o.setTeslimTarihi(td.toLocalDate());

        Date gtd = rs.getDate("gercek_teslim_tarihi");
        if (gtd != null) o.setGercekTeslimTarihi(gtd.toLocalDate());

        o.setDurum(rs.getString("durum"));

        // JOIN alanları — sütun yoksa sessizce atla
        try { o.setBarkod(rs.getString("barkod")); } catch (SQLException ignored) {}
        try { o.setKitapBaslik(rs.getString("kitap_baslik")); } catch (SQLException ignored) {}
        try { o.setKullaniciAdi(rs.getString("ad_Soyad")); } catch (SQLException ignored) {}

        return o;
    }
}
