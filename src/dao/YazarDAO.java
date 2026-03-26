package dao;

import model.Yazar;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * YazarDAO — 'yazarlar' tablosu için veri erişim sınıfı.
 * Tüm sorgular PreparedStatement ile SQL Injection'a karşı korunmaktadır.
 */
public class YazarDAO {

    private static final String SQL_TUMUNU_GETIR =
            "SELECT id, yazar_adi FROM yazarlar ORDER BY yazar_adi";

    private static final String SQL_EKLE =
            "INSERT INTO yazarlar (yazar_adi) VALUES (?)";

    private static final String SQL_GUNCELLE =
            "UPDATE yazarlar SET yazar_adi = ? WHERE id = ?";

    private static final String SQL_SIL =
            "DELETE FROM yazarlar WHERE id = ?";

    // ---------------------------------------------------------------
    // Listeleme
    // ---------------------------------------------------------------

    /**
     * Tüm yazarları alfabetik sıraya göre getirir.
     *
     * @return {@link Yazar} listesi
     */
    public List<Yazar> tumunuGetir() {
        List<Yazar> liste = new ArrayList<>();
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_TUMUNU_GETIR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(new Yazar(rs.getInt("id"), rs.getString("yazar_adi")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Yazarlar getirilirken hata: " + e.getMessage(), e);
        }
        return liste;
    }

    // ---------------------------------------------------------------
    // Ekleme
    // ---------------------------------------------------------------

    /**
     * Yeni bir yazar ekler.
     *
     * @param ad eklenecek yazar adı
     * @return oluşturulan {@link Yazar} nesnesi (ID dahil)
     * @throws IllegalArgumentException ad boşsa
     */
    public Yazar ekle(String ad) {
        if (ad == null || ad.trim().isEmpty()) {
            throw new IllegalArgumentException("Yazar adı boş olamaz.");
        }
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(
                SQL_EKLE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ad.trim());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Yazar(rs.getInt(1), ad.trim());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Yazar eklenirken hata: " + e.getMessage(), e);
        }
        throw new RuntimeException("Yazar eklendi fakat ID alınamadı.");
    }

    // ---------------------------------------------------------------
    // Güncelleme
    // ---------------------------------------------------------------

    /**
     * Mevcut bir yazarın adını günceller.
     *
     * @param id     güncellenecek yazar ID'si
     * @param yeniAd yeni yazar adı
     * @return güncelleme başarılıysa {@code true}
     */
    public boolean guncelle(int id, String yeniAd) {
        if (yeniAd == null || yeniAd.trim().isEmpty()) throw new IllegalArgumentException("Yazar adı boş olamaz.");
        Connection baglanti = DBConnection.getBaglanti();
        try (PreparedStatement ps = baglanti.prepareStatement(SQL_GUNCELLE)) {
            ps.setString(1, yeniAd.trim());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Yazar güncellenirken hata: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // Silme
    // ---------------------------------------------------------------

    /**
     * Belirtilen ID'li yazarı siler.
     *
     * @param id silinecek yazar ID'si
     * @return silme başarılıysa {@code true}
     */
    public boolean sil(int id) {
        Connection baglanti = DBConnection.getBaglanti();
        try (PreparedStatement ps = baglanti.prepareStatement(SQL_SIL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Yazar silinirken hata: " + e.getMessage(), e);
        }
    }
}
