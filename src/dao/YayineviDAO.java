package dao;

import model.YayinEvi;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * YayineviDAO — 'yayin_evleri' tablosu için veri erişim sınıfı.
 * Tüm sorgular PreparedStatement ile SQL Injection'a karşı korunmaktadır.
 */
public class YayineviDAO {

    private static final String SQL_TUMUNU_GETIR =
            "SELECT id, yayinevi_adi FROM yayin_evleri ORDER BY yayinevi_adi";

    private static final String SQL_EKLE =
            "INSERT INTO yayin_evleri (yayinevi_adi) VALUES (?)";

    private static final String SQL_GUNCELLE =
            "UPDATE yayin_evleri SET yayinevi_adi = ? WHERE id = ?";

    private static final String SQL_SIL =
            "DELETE FROM yayin_evleri WHERE id = ?";

    // ---------------------------------------------------------------
    // Listeleme
    // ---------------------------------------------------------------

    /**
     * Tüm yayınevlerini alfabetik sıraya göre getirir.
     *
     * @return {@link YayinEvi} listesi
     */
    public List<YayinEvi> tumunuGetir() {
        List<YayinEvi> liste = new ArrayList<>();
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_TUMUNU_GETIR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(new YayinEvi(rs.getInt("id"), rs.getString("yayinevi_adi")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Yayınevleri getirilirken hata: " + e.getMessage(), e);
        }
        return liste;
    }

    // ---------------------------------------------------------------
    // Ekleme
    // ---------------------------------------------------------------

    /**
     * Yeni bir yayınevi ekler.
     *
     * @param ad eklenecek yayınevi adı
     * @return oluşturulan {@link YayinEvi} nesnesi (ID dahil)
     * @throws IllegalArgumentException ad boşsa
     */
    public YayinEvi ekle(String ad) {
        if (ad == null || ad.trim().isEmpty()) {
            throw new IllegalArgumentException("Yayınevi adı boş olamaz.");
        }
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(
                SQL_EKLE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ad.trim());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new YayinEvi(rs.getInt(1), ad.trim());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Yayınevi eklenirken hata: " + e.getMessage(), e);
        }
        throw new RuntimeException("Yayınevi eklendi fakat ID alınamadı.");
    }

    // ---------------------------------------------------------------
    // Güncelleme
    // ---------------------------------------------------------------

    /**
     * Mevcut bir yayınevinin adını günceller.
     *
     * @param id     güncellenecek yayınevi ID'si
     * @param yeniAd yeni yayınevi adı
     * @return güncelleme başarılıysa {@code true}
     */
    public boolean guncelle(int id, String yeniAd) {
        if (yeniAd == null || yeniAd.trim().isEmpty()) throw new IllegalArgumentException("Yayınevi adı boş olamaz.");
        Connection baglanti = DBConnection.getBaglanti();
        try (PreparedStatement ps = baglanti.prepareStatement(SQL_GUNCELLE)) {
            ps.setString(1, yeniAd.trim());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Yayınevi güncellenirken hata: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // Silme
    // ---------------------------------------------------------------

    /**
     * Belirtilen ID'li yayınevini siler.
     *
     * @param id silinecek yayınevi ID'si
     * @return silme başarılıysa {@code true}
     */
    public boolean sil(int id) {
        Connection baglanti = DBConnection.getBaglanti();
        try (PreparedStatement ps = baglanti.prepareStatement(SQL_SIL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Yayınevi silinirken hata: " + e.getMessage(), e);
        }
    }
}
