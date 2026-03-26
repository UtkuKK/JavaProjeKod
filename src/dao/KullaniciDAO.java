package dao;

import model.Kullanici;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * KullaniciDAO — 'kullanicilar' tablosu ile ilgili tüm veritabanı
 * işlemlerini yöneten DAO (Data Access Object) sınıfıdır.
 *
 * Güvenlik: Tüm SQL sorguları SQL Injection'a karşı
 * {@link PreparedStatement} kullanılarak hazırlanmıştır.
 */
public class KullaniciDAO {

    // ---------------------------------------------------------------
    // SQL Sabitleri
    // ---------------------------------------------------------------
    private static final String SQL_GIRIS_DOGRULA =
            "SELECT id, ad_Soyad, email, sifre, rol, durum " +
            "FROM kullanicilar " +
            "WHERE email = ? AND sifre = ?";

    private static final String SQL_EMAIL_ILE_BUL =
            "SELECT id, ad_Soyad, email, sifre, rol, durum " +
            "FROM kullanicilar " +
            "WHERE email = ?";

    private static final String SQL_ID_ILE_BUL =
            "SELECT id, ad_Soyad, email, sifre, rol, durum " +
            "FROM kullanicilar " +
            "WHERE id = ?";

    private static final String SQL_TUMUNU_GETIR =
            "SELECT id, ad_Soyad, email, sifre, rol, durum " +
            "FROM kullanicilar ORDER BY id";

    private static final String SQL_EKLE =
            "INSERT INTO kullanicilar (ad_Soyad, email, sifre, rol, durum) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_DURUM_GUNCELLE =
            "UPDATE kullanicilar SET durum = ? WHERE id = ?";

    private static final String SQL_SIFRE_GUNCELLE =
            "UPDATE kullanicilar SET sifre = ? WHERE id = ?";


    // ---------------------------------------------------------------
    // Kimlik Doğrulama
    // ---------------------------------------------------------------

    /**
     * Belirtilen e-posta ve şifre ile oturum açmaya çalışır.
     *
     * @param email  kullanıcının e-posta adresi
     * @param sifre  kullanıcının şifresi (sade metin — ileride hash eklenebilir)
     * @return eşleşen {@link Kullanici} nesnesi; bulunamazsa {@code null}
     * @throws RuntimeException veritabanı hatası oluşursa
     */
    public Kullanici girisDogrula(String email, String sifre) {
        Kullanici kullanici = null;
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_GIRIS_DOGRULA)) {
            ps.setString(1, email.trim());
            ps.setString(2, sifre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kullanici = satiriKullaniciyaDonustur(rs);

                    // Hesap pasif ise null döndür (erişim engellenir)
                    if (!kullanici.isAktif()) {
                        System.out.println("[KullaniciDAO] Pasif hesap girişi denendi: " + email);
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Giriş doğrulama sırasında veritabanı hatası: " + e.getMessage(), e);
        }

        return kullanici;
    }

    // ---------------------------------------------------------------
    // Tekil Kayıt Sorgulama
    // ---------------------------------------------------------------

    /**
     * E-posta adresine göre kullanıcı getirir.
     *
     * @param email aranacak e-posta adresi
     * @return bulunan {@link Kullanici}; yoksa {@code null}
     */
    public Kullanici emailIleBul(String email) {
        Kullanici kullanici = null;
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_EMAIL_ILE_BUL)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kullanici = satiriKullaniciyaDonustur(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "E-posta ile kullanıcı getirilirken hata: " + e.getMessage(), e);
        }

        return kullanici;
    }

    /**
     * ID'ye göre kullanıcı getirir.
     *
     * @param id aranacak kullanıcı ID'si
     * @return bulunan {@link Kullanici}; yoksa {@code null}
     */
    public Kullanici idIleBul(int id) {
        Kullanici kullanici = null;
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_ID_ILE_BUL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kullanici = satiriKullaniciyaDonustur(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "ID ile kullanıcı getirilirken hata: " + e.getMessage(), e);
        }

        return kullanici;
    }

    // ---------------------------------------------------------------
    // Listeleme — tüm kullanıcılar (yönetici için)
    // ---------------------------------------------------------------

    /**
     * Tüm kullanıcıları getirir.
     *
     * @return {@link java.util.List} Kullanici listesi
     */
    public java.util.List<Kullanici> tumunuGetir() {
        java.util.List<Kullanici> liste = new java.util.ArrayList<>();
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_TUMUNU_GETIR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(satiriKullaniciyaDonustur(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Kullanıcılar getirilirken hata: " + e.getMessage(), e);
        }
        return liste;
    }

    // ---------------------------------------------------------------
    // Ekleme
    // ---------------------------------------------------------------

    /**
     * Yeni kullanıcı kaydı oluşturur.
     *
     * @param kullanici eklenecek kullanıcı
     * @return ekleme başarılıysa {@code true}
     */
    public boolean kullaniciEkle(Kullanici kullanici) {
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(
                SQL_EKLE, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kullanici.getAdSoyad());
            ps.setString(2, kullanici.getEmail());
            ps.setString(3, kullanici.getSifre());
            ps.setString(4, kullanici.getRol() != null ? kullanici.getRol() : "UYE");
            ps.setString(5, kullanici.getDurum() != null ? kullanici.getDurum() : "AKTIF");

            int satirSayisi = ps.executeUpdate();
            if (satirSayisi > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) kullanici.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Kullanıcı eklenirken hata: " + e.getMessage(), e);
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Durum Güncelleme
    // ---------------------------------------------------------------

    /**
     * Kullanıcının 'durum' sütununu günceller ('AKTIF' ↔ 'PASIF').
     *
     * @param kullaniciId hedef kullanıcının ID'si
     * @param yeniDurum   yeni durum değeri
     * @return güncelleme başarılıysa {@code true}
     */
    public boolean durumuGuncelle(int kullaniciId, String yeniDurum) {
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_DURUM_GUNCELLE)) {
            ps.setString(1, yeniDurum);
            ps.setInt(2, kullaniciId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Kullanıcı durumu güncellenirken hata: " + e.getMessage(), e);
        }
    }

    /**
     * Kullanıcının şifresini günceller.
     *
     * @param kullaniciId hedef kullanıcının ID'si
     * @param yeniSifre   yeni şifre (sade metin)
     * @return güncelleme başarılıysa {@code true}
     */
    public boolean sifreDegistir(int kullaniciId, String yeniSifre) {
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_SIFRE_GUNCELLE)) {
            ps.setString(1, yeniSifre);
            ps.setInt(2, kullaniciId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Şifre güncellenirken hata: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // Kullanıcı Silme
    // ---------------------------------------------------------------

    /**
     * Kullanıcıyı sistemden kalıcı olarak siler.
     * @param kullaniciId silinecek kullanıcının ID'si
     * @return silme başarılıysa {@code true}
     */
    public boolean kullaniciSil(int kullaniciId) {
        Connection baglanti = DBConnection.getBaglanti();
        String sql = "DELETE FROM kullanicilar WHERE id = ?";
        try (PreparedStatement ps = baglanti.prepareStatement(sql)) {
            ps.setInt(1, kullaniciId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Kullanıcı silinirken hata: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // Yardımcı Metodlar
    // ---------------------------------------------------------------

    /**
     * {@link ResultSet}'in mevcut satırını {@link Kullanici} nesnesine çevirir.
     * Bu metod her DAO metodu tarafından yeniden kullanılır (DRY prensibi).
     *
     * @param rs imleç aktif satırda olan ResultSet
     * @return doldurulmuş {@link Kullanici} nesnesi
     * @throws SQLException JDBC okuma hatası
     */
    private Kullanici satiriKullaniciyaDonustur(ResultSet rs) throws SQLException {
        Kullanici k = new Kullanici();
        k.setId(rs.getInt("id"));
        k.setAdSoyad(rs.getString("ad_Soyad"));
        k.setEmail(rs.getString("email"));
        k.setSifre(rs.getString("sifre"));
        k.setRol(rs.getString("rol"));
        k.setDurum(rs.getString("durum"));
        return k;
    }
}

