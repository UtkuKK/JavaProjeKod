package dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Rezervasyon;
import util.DBConnection;

public class RezervasyonDAO {

    public RezervasyonDAO() {
        tabloyuBastanOlusturEgerYoksa();
    }

    private void tabloyuBastanOlusturEgerYoksa() {
        String sql = "CREATE TABLE IF NOT EXISTS rezervasyonlar (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "kullanici_id INT NOT NULL, " +
                "kitap_id INT NOT NULL, " +
                "rezervasyon_tarihi DATE NOT NULL, " +
                "durum VARCHAR(20) DEFAULT 'BEKLIYOR', " +
                "FOREIGN KEY (kullanici_id) REFERENCES kullanicilar(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (kitap_id) REFERENCES kitaplar(id) ON DELETE CASCADE" +
                ")";
        try (Connection c = DBConnection.getBaglanti(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException e) { System.err.println("Rezervasyon tablosu oluşturulurken hata: " + e.getMessage()); }
    }

    public boolean rezerveEt(int kullaniciId, int kitapId) {
        // Kontrol: Bu kitaba ait toplam KOPYA sayısı ve mevcut BEKLEYEN rezervasyon sayısı nedir?
        int kopyaSayisi = 0;
        int bekleyenYedekSayisi = 0;

        try (Connection c = DBConnection.getBaglanti()) {
            // 1) Kullanıcı bu kitabı zaten rezerve etmiş mi?
            String ch1 = "SELECT id FROM rezervasyonlar WHERE kitap_id=? AND kullanici_id=? AND durum='BEKLIYOR'";
            try (PreparedStatement ps1 = c.prepareStatement(ch1)) {
                ps1.setInt(1, kitapId);
                ps1.setInt(2, kullaniciId);
                try (ResultSet rs1 = ps1.executeQuery()) {
                    if (rs1.next()) throw new RuntimeException("Zaten bu kitap için aktif bir rezervasyonunuz bulunuyor.");
                }
            }

            // 2) Toplam Kopya sayısı
            try (PreparedStatement ps2 = c.prepareStatement("SELECT COUNT(id) FROM kitap_kopyalari WHERE kitap_id=?")) {
                ps2.setInt(1, kitapId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) kopyaSayisi = rs2.getInt(1);
                }
            }

            // 3) Aktif bekleyen rezervasyonlar
            try (PreparedStatement ps3 = c.prepareStatement("SELECT COUNT(id) FROM rezervasyonlar WHERE kitap_id=? AND durum='BEKLIYOR'")) {
                ps3.setInt(1, kitapId);
                try (ResultSet rs3 = ps3.executeQuery()) {
                    if (rs3.next()) bekleyenYedekSayisi = rs3.getInt(1);
                }
            }
        } catch (SQLException e) { throw new RuntimeException("Kontrol esnasında hata: " + e.getMessage(), e); }

        if (bekleyenYedekSayisi >= kopyaSayisi) {
            throw new RuntimeException("Bu kitap için tahsis edilen maksimum rezervasyon sınırına (" + kopyaSayisi + " adet yedek kontenjanı) ulaşıldı.");
        }

        String sql = "INSERT INTO rezervasyonlar (kullanici_id, kitap_id, rezervasyon_tarihi, durum) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getBaglanti(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, kullaniciId);
            ps.setInt(2, kitapId);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setString(4, Rezervasyon.DURUM_BEKLIYOR);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException("Rezervasyon eklenemedi: " + e.getMessage(), e); }
    }

    public boolean iptalEt(int rezervasyonId) {
        String sql = "UPDATE rezervasyonlar SET durum = ? WHERE id = ?";
        try (Connection c = DBConnection.getBaglanti(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Rezervasyon.DURUM_IPTAL);
            ps.setInt(2, rezervasyonId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException("Rezervasyon iptal hatası: " + e.getMessage(), e); }
    }

    public boolean tamamla(int rezervasyonId) {
        String sql = "UPDATE rezervasyonlar SET durum = ? WHERE id = ?";
        try (Connection c = DBConnection.getBaglanti(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Rezervasyon.DURUM_TAMAMLANDI);
            ps.setInt(2, rezervasyonId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException("Rezervasyon tamamlama hatası: " + e.getMessage(), e); }
    }

    public List<Rezervasyon> bekleyenleriGetir() {
        List<Rezervasyon> liste = new ArrayList<>();
        String sql = "SELECT r.*, k.baslik, kul.ad_soyad FROM rezervasyonlar r " +
                "JOIN kitaplar k ON r.kitap_id = k.id " +
                "JOIN kullanicilar kul ON r.kullanici_id = kul.id " +
                "WHERE r.durum = 'BEKLIYOR' ORDER BY r.rezervasyon_tarihi DESC";
        try (Connection c = DBConnection.getBaglanti(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Rezervasyon r = new Rezervasyon();
                r.setId(rs.getInt("id"));
                r.setKullaniciId(rs.getInt("kullanici_id"));
                r.setKitapId(rs.getInt("kitap_id"));
                r.setRezervasyonTarihi(rs.getDate("rezervasyon_tarihi").toLocalDate());
                r.setDurum(rs.getString("durum"));
                r.setKitapBaslik(rs.getString("baslik"));
                r.setKullaniciAdi(rs.getString("ad_soyad"));
                liste.add(r);
            }
        } catch (SQLException e) { throw new RuntimeException("Rezervasyon listelenemedi: " + e.getMessage(), e); }
        return liste;
    }

    public Rezervasyon getIlkBekleyenRezervasyon(int kitapId) {
        String sql = "SELECT r.*, k.baslik, kul.ad_soyad FROM rezervasyonlar r " +
                "JOIN kitaplar k ON r.kitap_id = k.id " +
                "JOIN kullanicilar kul ON r.kullanici_id = kul.id " +
                "WHERE r.kitap_id = ? AND r.durum = 'BEKLIYOR' ORDER BY r.rezervasyon_tarihi ASC LIMIT 1";
        try (Connection c = DBConnection.getBaglanti(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, kitapId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Rezervasyon r = new Rezervasyon();
                    r.setId(rs.getInt("id"));
                    r.setKullaniciId(rs.getInt("kullanici_id"));
                    r.setKitapId(rs.getInt("kitap_id"));
                    r.setRezervasyonTarihi(rs.getDate("rezervasyon_tarihi").toLocalDate());
                    r.setDurum(rs.getString("durum"));
                    r.setKitapBaslik(rs.getString("baslik"));
                    r.setKullaniciAdi(rs.getString("ad_soyad"));
                    return r;
                }
            }
        } catch (SQLException e) { throw new RuntimeException("Rezervasyon kontrol hatası: " + e.getMessage(), e); }
        return null;
    }
}
