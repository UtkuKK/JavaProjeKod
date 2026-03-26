package dao;

import model.Ceza;
import util.DBConnection;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CezaDAO — 'cezalar' tablosu ile ilgili tüm veritabanı işlemleri.
 *
 * Tüm sorgular PreparedStatement ile SQL Injection'a karşı korunmaktadır.
 */
public class CezaDAO {

    // ---------------------------------------------------------------
    // SQL Sabitleri
    // ---------------------------------------------------------------

    /** Belirli bir kullanıcıya ait tüm cezaları listeler. */
    private static final String SQL_KULLANICI_CEZALARI =
            "SELECT c.id, c.kullanici_id, c.odunc_id, c.ceza_miktari, " +
            "       c.aciklama, c.odendi_mi, k.ad_Soyad " +
            "FROM cezalar c " +
            "JOIN kullanicilar k ON c.kullanici_id = k.id " +
            "WHERE c.kullanici_id = ? " +
            "ORDER BY c.id DESC";

    /** Tüm cezaları yönetici için getirir. */
    private static final String SQL_TUM_CEZALAR =
            "SELECT c.id, c.kullanici_id, c.odunc_id, c.ceza_miktari, " +
            "       c.aciklama, c.odendi_mi, k.ad_Soyad " +
            "FROM cezalar c " +
            "JOIN kullanicilar k ON c.kullanici_id = k.id " +
            "ORDER BY c.odendi_mi ASC, c.id DESC";

    /** Ödenmemiş cezaları listeler (yönetici için). */
    private static final String SQL_ODENMEMIS_CEZALAR =
            "SELECT c.id, c.kullanici_id, c.odunc_id, c.ceza_miktari, " +
            "       c.aciklama, c.odendi_mi, k.ad_Soyad " +
            "FROM cezalar c " +
            "JOIN kullanicilar k ON c.kullanici_id = k.id " +
            "WHERE c.odendi_mi = FALSE " +
            "ORDER BY c.id DESC";

    /** Ceza ödendi olarak işaretle. */
    private static final String SQL_CEZA_ODE =
            "UPDATE cezalar SET odendi_mi = TRUE WHERE id = ?";

    /** Ceza ekle. */
    private static final String SQL_CEZA_EKLE =
            "INSERT INTO cezalar (kullanici_id, odunc_id, ceza_miktari, aciklama, odendi_mi) " +
            "VALUES (?, ?, ?, ?, FALSE)";

    // ---------------------------------------------------------------
    // Listeleme
    // ---------------------------------------------------------------

    /**
     * Belirtilen kullanıcının tüm cezalarını getirir.
     *
     * @param kullaniciId cezaları alınacak kullanıcı ID'si
     * @return {@link Ceza} listesi
     */
    public List<Ceza> getirCezalarByKullanici(int kullaniciId) {
        List<Ceza> liste = new ArrayList<>();
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_KULLANICI_CEZALARI)) {
            ps.setInt(1, kullaniciId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(satirCezayaDonustur(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Kullanıcı cezaları getirilirken hata: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Tüm cezaları getirir (yönetici görünümü).
     *
     * @return tüm {@link Ceza} kayıtları
     */
    public List<Ceza> tumCezalariGetir() {
        return cezaListesiGetir(SQL_TUM_CEZALAR);
    }

    /**
     * Yalnızca ödenmemiş cezaları getirir.
     *
     * @return ödenmemiş {@link Ceza} listesi
     */
    public List<Ceza> odenmemisleriGetir() {
        return cezaListesiGetir(SQL_ODENMEMIS_CEZALAR);
    }

    private List<Ceza> cezaListesiGetir(String sql) {
        List<Ceza> liste = new ArrayList<>();
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(satirCezayaDonustur(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Cezalar getirilirken hata: " + e.getMessage(), e);
        }
        return liste;
    }

    // ---------------------------------------------------------------
    // Ödeme
    // ---------------------------------------------------------------

    /**
     * Belirtilen ID'li cezayı "ödendi" olarak işaretler.
     *
     * @param cezaId ödenecek cezanın ID'si
     * @return işlem başarılıysa {@code true}
     */
    public boolean cezaOde(int cezaId) {
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_CEZA_ODE)) {
            ps.setInt(1, cezaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ceza ödenirken hata: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // Ekleme (OduncDAO tarafından çağrılır)
    // ---------------------------------------------------------------

    /**
     * Yeni bir ceza kaydı oluşturur.
     * Bu metod genellikle {@link OduncDAO#iadeAl} tarafından otomatik çağrılır.
     *
     * @param ceza eklenecek {@link Ceza} nesnesi
     * @return ekleme başarılıysa {@code true}
     */
    public boolean cezaEkle(Ceza ceza) {
        Connection baglanti = DBConnection.getBaglanti();

        try (PreparedStatement ps = baglanti.prepareStatement(SQL_CEZA_EKLE)) {
            ps.setInt(1, ceza.getKullaniciId());
            ps.setInt(2, ceza.getOduncId());
            ps.setBigDecimal(3, ceza.getCezaMiktari());
            ps.setString(4, ceza.getAciklama());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ceza eklenirken hata: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // Yardımcı
    // ---------------------------------------------------------------
    private Ceza satirCezayaDonustur(ResultSet rs) throws SQLException {
        Ceza c = new Ceza();
        c.setId(rs.getInt("id"));
        c.setKullaniciId(rs.getInt("kullanici_id"));
        c.setOduncId(rs.getInt("odunc_id"));
        c.setCezaMiktari(rs.getBigDecimal("ceza_miktari"));
        c.setAciklama(rs.getString("aciklama"));
        c.setOdendiMi(rs.getBoolean("odendi_mi"));
        c.setKullaniciAdi(rs.getString("ad_Soyad"));
        return c;
    }
}
