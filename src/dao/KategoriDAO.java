package dao;

import model.Kategori;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * KategoriDAO — 'kategoriler' tablosu için veri erişim sınıfı.
 * Tüm sorgular PreparedStatement ile SQL Injection'a karşı korunmaktadır.
 */
public class KategoriDAO {

    private static final String SQL_TUMUNU_GETIR =
            "SELECT id, kategori_adi FROM kategoriler ORDER BY kategori_adi";

    private static final String SQL_EKLE =
            "INSERT INTO kategoriler (kategori_adi) VALUES (?)";

    private static final String SQL_GUNCELLE =
            "UPDATE kategoriler SET kategori_adi = ? WHERE id = ?";

    private static final String SQL_SIL =
            "DELETE FROM kategoriler WHERE id = ?";

    // ---------------------------------------------------------------
    // Listeleme
    // ---------------------------------------------------------------

    /**
     * Tüm kategorileri alfabetik sıraya göre getirir.
     *
     * @return {@link Kategori} listesi; tablo boşsa boş liste
     */
    public List<Kategori> tumunuGetir() {
        List<Kategori> liste = new ArrayList<>();
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_TUMUNU_GETIR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(new Kategori(rs.getInt("id"), rs.getString("kategori_adi")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Kategoriler getirilirken hata: " + e.getMessage(), e);
        }
        return liste;
    }

    // ---------------------------------------------------------------
    // Ekleme
    // ---------------------------------------------------------------

    /**
     * Yeni bir kategori ekler.
     *
     * @param ad eklenecek kategori adı
     * @return oluşturulan {@link Kategori} nesnesi (ID dahil)
     * @throws IllegalArgumentException ad boşsa
     */
    public Kategori ekle(String ad) {
        if (ad == null || ad.trim().isEmpty()) {
            throw new IllegalArgumentException("Kategori adı boş olamaz.");
        }
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(
                SQL_EKLE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ad.trim());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Kategori(rs.getInt(1), ad.trim());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Kategori eklenirken hata: " + e.getMessage(), e);
        }
        throw new RuntimeException("Kategori eklendi fakat ID alınamadı.");
    }

    // ---------------------------------------------------------------
    // Güncelleme
    // ---------------------------------------------------------------

    /**
     * Mevcut bir kategorinin adını günceller.
     *
     * @param id      güncellenecek kategori ID'si
     * @param yeniAd  yeni kategori adı
     * @return güncelleme başarılıysa {@code true}
     */
    public boolean guncelle(int id, String yeniAd) {
        if (yeniAd == null || yeniAd.trim().isEmpty()) throw new IllegalArgumentException("Kategori adı boş olamaz.");
        Connection baglanti = DBConnection.getBaglanti();
        try (PreparedStatement ps = baglanti.prepareStatement(SQL_GUNCELLE)) {
            ps.setString(1, yeniAd.trim());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Kategori güncellenirken hata: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // Silme
    // ---------------------------------------------------------------

    /**
     * Belirtilen ID'li kategoriyi siler.
     *
     * @param id silinecek kategori ID'si
     * @return silme başarılıysa {@code true}
     */
    public boolean sil(int id) {
        Connection baglanti = DBConnection.getBaglanti();
        try (PreparedStatement ps = baglanti.prepareStatement(SQL_SIL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Kategori silinirken hata: " + e.getMessage(), e);
        }
    }
}
