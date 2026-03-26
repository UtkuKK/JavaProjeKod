package dao;

import model.Kitap;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KitapDAO {

    // ---------------------------------------------------------------
    // SQL Sabitleri (BARKODLAR EKLENDİ)
    // ---------------------------------------------------------------

    private static final String SQL_TUMU_GETIR =
            "SELECT k.id, k.baslik, k.isbn, k.yayin_yili, " +
                    "       k.kategori_id, k.yazar_id, k.yayinevi_id, " +
                    "       y.yazar_adi, kat.kategori_adi, ye.yayinevi_adi, " +
                    "       (SELECT GROUP_CONCAT(barkod SEPARATOR ', ') FROM kitap_kopyalari WHERE kitap_id = k.id) as barkodlar " +
                    "FROM kitaplar k " +
                    "LEFT JOIN yazarlar       y   ON k.yazar_id     = y.id " +
                    "LEFT JOIN kategoriler    kat ON k.kategori_id  = kat.id " +
                    "LEFT JOIN yayin_evleri   ye  ON k.yayinevi_id  = ye.id " +
                    "ORDER BY k.baslik";

    private static final String SQL_ARA =
            "SELECT k.id, k.baslik, k.isbn, k.yayin_yili, " +
                    "       k.kategori_id, k.yazar_id, k.yayinevi_id, " +
                    "       y.yazar_adi, kat.kategori_adi, ye.yayinevi_adi, " +
                    "       (SELECT GROUP_CONCAT(barkod SEPARATOR ', ') FROM kitap_kopyalari WHERE kitap_id = k.id) as barkodlar " +
                    "FROM kitaplar k " +
                    "LEFT JOIN yazarlar       y   ON k.yazar_id     = y.id " +
                    "LEFT JOIN kategoriler    kat ON k.kategori_id  = kat.id " +
                    "LEFT JOIN yayin_evleri   ye  ON k.yayinevi_id  = ye.id " +
                    "WHERE LOWER(k.baslik) LIKE ? " +
                    "   OR LOWER(k.isbn)   LIKE ? " +
                    "   OR LOWER(y.yazar_adi) LIKE ? " +
                    "ORDER BY k.baslik";

    private static final String SQL_EKLE =
            "INSERT INTO kitaplar (baslik, isbn, yayin_yili, kategori_id, yazar_id, yayinevi_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_KOPYA_EKLE =
            "INSERT INTO kitap_kopyalari (kitap_id, barkod, durum) VALUES (?, ?, 'MUSAIT')";

    private static final String SQL_ID_ILE_BUL =
            "SELECT k.id, k.baslik, k.isbn, k.yayin_yili, " +
                    "       k.kategori_id, k.yazar_id, k.yayinevi_id, " +
                    "       y.yazar_adi, kat.kategori_adi, ye.yayinevi_adi, " +
                    "       (SELECT GROUP_CONCAT(barkod SEPARATOR ', ') FROM kitap_kopyalari WHERE kitap_id = k.id) as barkodlar " +
                    "FROM kitaplar k " +
                    "LEFT JOIN yazarlar       y   ON k.yazar_id    = y.id " +
                    "LEFT JOIN kategoriler    kat ON k.kategori_id = kat.id " +
                    "LEFT JOIN yayin_evleri   ye  ON k.yayinevi_id = ye.id " +
                    "WHERE k.id = ?";

    private static final String SQL_GUNCELLE =
            "UPDATE kitaplar SET baslik=?, isbn=?, yayin_yili=?, " +
                    "kategori_id=?, yazar_id=?, yayinevi_id=? WHERE id=?";

    private static final String SQL_SIL =
            "DELETE FROM kitaplar WHERE id=?";

    // ---------------------------------------------------------------
    // Metodlar
    // ---------------------------------------------------------------

    public List<Kitap> tumunuGetir() {
        List<Kitap> liste = new ArrayList<>();
        Connection baglanti = DBConnection.getBaglanti();
        try (PreparedStatement ps = baglanti.prepareStatement(SQL_TUMU_GETIR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(satiriKitabaDonustur(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Kitaplar getirilirken hata: " + e.getMessage(), e);
        }
        return liste;
    }

    public Kitap idIleBul(int id) {
        Connection baglanti = DBConnection.getBaglanti();
        try (PreparedStatement ps = baglanti.prepareStatement(SQL_ID_ILE_BUL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return satiriKitabaDonustur(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("ID ile kitap getirilirken hata: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Kitap> kitapAra(String aramaMetni) {
        List<Kitap> liste = new ArrayList<>();
        if (aramaMetni == null || aramaMetni.trim().isEmpty()) return tumunuGetir();

        String arama = "%" + aramaMetni.trim().toLowerCase() + "%";
        Connection baglanti = DBConnection.getBaglanti();
        try (PreparedStatement ps = baglanti.prepareStatement(SQL_ARA)) {
            ps.setString(1, arama);
            ps.setString(2, arama);
            ps.setString(3, arama);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(satiriKitabaDonustur(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Kitap aranırken hata: " + e.getMessage(), e);
        }
        return liste;
    }

    // YENİ EKLEME MANTIĞI: Kitabı ekler, başarılı olursa peşine istenen sayıda BARKODLU KOPYA ekler.
    public boolean kitapEkle(Kitap kitap, int kopyaSayisi) {
        Connection baglanti = null;
        try {
            baglanti = DBConnection.getBaglanti();
            baglanti.setAutoCommit(false); // Transaction başlatıyoruz

            int yeniKitapId = -1;
            // 1. Kitabı Ekle
            try (PreparedStatement ps = baglanti.prepareStatement(SQL_EKLE, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, kitap.getBaslik().trim());
                ps.setString(2, kitap.getIsbn().trim());
                ps.setInt(3, kitap.getYayinYili());
                if (kitap.getKategoriId() > 0) ps.setInt(4, kitap.getKategoriId()); else ps.setNull(4, Types.INTEGER);
                if (kitap.getYazarId() > 0)    ps.setInt(5, kitap.getYazarId());    else ps.setNull(5, Types.INTEGER);
                if (kitap.getYayineviId() > 0) ps.setInt(6, kitap.getYayineviId()); else ps.setNull(6, Types.INTEGER);

                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) yeniKitapId = rs.getInt(1);
                    }
                }
            }

            // 2. Kitap eklendiyse ona kopyaSayisi kadar Kopya ve Barkod oluştur
            if (yeniKitapId != -1 && kopyaSayisi > 0) {
                try (PreparedStatement psKopya = baglanti.prepareStatement(SQL_KOPYA_EKLE)) {
                    for (int i = 0; i < kopyaSayisi; i++) {
                        String rastgeleBarkod = "BRK-" + (int)(Math.random() * 900000 + 100000);
                        psKopya.setInt(1, yeniKitapId);
                        psKopya.setString(2, rastgeleBarkod);
                        psKopya.addBatch();
                    }
                    psKopya.executeBatch();
                }
            }

            baglanti.commit(); // İkisi de başarılı, onayla!
            return true;

        } catch (SQLException e) {
            try { if (baglanti != null) baglanti.rollback(); } catch (SQLException ex) {} // Hata varsa geri al
            throw new RuntimeException("Kitap eklenirken hata: " + e.getMessage(), e);
        } finally {
            try { if (baglanti != null) baglanti.setAutoCommit(true); } catch (SQLException ex) {}
        }
    }

    public boolean kitapGuncelle(Kitap kitap) {
        Connection baglanti = DBConnection.getBaglanti();
        try (PreparedStatement ps = baglanti.prepareStatement(SQL_GUNCELLE)) {
            ps.setString(1, kitap.getBaslik().trim());
            ps.setString(2, kitap.getIsbn().trim());
            ps.setInt(3, kitap.getYayinYili());
            if (kitap.getKategoriId() > 0) ps.setInt(4, kitap.getKategoriId()); else ps.setNull(4, Types.INTEGER);
            if (kitap.getYazarId() > 0)    ps.setInt(5, kitap.getYazarId());    else ps.setNull(5, Types.INTEGER);
            if (kitap.getYayineviId() > 0) ps.setInt(6, kitap.getYayineviId()); else ps.setNull(6, Types.INTEGER);
            ps.setInt(7, kitap.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Kitap güncellenirken hata: " + e.getMessage(), e);
        }
    }

    public boolean kitapSil(int kitapId) {
        Connection baglanti = null;
        try {
            baglanti = DBConnection.getBaglanti();
            baglanti.setAutoCommit(false); // İşlemi başlat

            // 1. Kitabın kopyalarına ait "cezalar" tablosundaki kayıtları sil
            try (PreparedStatement psCezalar = baglanti.prepareStatement(
                    "DELETE FROM cezalar WHERE odunc_id IN (SELECT id FROM odunc_islemleri WHERE kopya_id IN (SELECT id FROM kitap_kopyalari WHERE kitap_id=?))")) {
                psCezalar.setInt(1, kitapId);
                psCezalar.executeUpdate();
            }

            // 2. Kopyalara ait "odunc_islemleri" kayıtlarını sil
            try (PreparedStatement psOduncler = baglanti.prepareStatement(
                    "DELETE FROM odunc_islemleri WHERE kopya_id IN (SELECT id FROM kitap_kopyalari WHERE kitap_id=?)")) {
                psOduncler.setInt(1, kitapId);
                psOduncler.executeUpdate();
            }

            // 3. Kopyaları kitap_kopyalari tablosundan sil
            try (PreparedStatement psKopya = baglanti.prepareStatement("DELETE FROM kitap_kopyalari WHERE kitap_id=?")) {
                psKopya.setInt(1, kitapId);
                psKopya.executeUpdate();
            }

            // 4. Kopyalar ve geçmiş başarıyla silindiyse kitabı sil
            try (PreparedStatement ps = baglanti.prepareStatement(SQL_SIL)) {
                ps.setInt(1, kitapId);
                int sonuc = ps.executeUpdate();
                baglanti.commit(); // Başarılı
                return sonuc > 0;
            }
        } catch (SQLException e) {
            try { if (baglanti != null) baglanti.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException("Kitap silinirken hata: " + e.getMessage(), e);
        } finally {
            try { if (baglanti != null) baglanti.setAutoCommit(true); } catch (SQLException ex) {}
        }
    }

    // Yardımcı Metod: Barkodları da okuyacak şekilde güncellendi
    private Kitap satiriKitabaDonustur(ResultSet rs) throws SQLException {
        Kitap k = new Kitap();
        k.setId(rs.getInt("id"));
        k.setBaslik(rs.getString("baslik"));
        k.setIsbn(rs.getString("isbn"));
        k.setYayinYili(rs.getInt("yayin_yili"));
        k.setKategoriId(rs.getInt("kategori_id"));
        k.setYazarId(rs.getInt("yazar_id"));
        k.setYayineviId(rs.getInt("yayinevi_id"));
        k.setYazarAdi(rs.getString("yazar_adi"));
        k.setKategoriAdi(rs.getString("kategori_adi"));
        k.setYayineviAdi(rs.getString("yayinevi_adi"));

        // Veritabanından gelen virgüllü barkod listesini modele set ediyoruz
        k.setBarkodlar(rs.getString("barkodlar"));
        return k;
    }
    // Sadece MUSAIT durumundaki kitapların barkodlarını ve başlıklarını getirir
    public List<String> getMusaitBarkodlar() {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT kk.barkod, k.baslik FROM kitap_kopyalari kk " +
                "JOIN kitaplar k ON kk.kitap_id = k.id " +
                "WHERE kk.durum = 'MUSAIT'";
        try (Connection baglanti = DBConnection.getBaglanti();
             Statement stmt = baglanti.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Ekranda güzel görünsün diye: "BRK-123456 - Suç ve Ceza" şeklinde birleştiriyoruz
                liste.add(rs.getString("barkod") + " - " + rs.getString("baslik"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }
}