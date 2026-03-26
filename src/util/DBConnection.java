package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection — Veritabanı bağlantılarını yöneten merkezi sınıf.
 * * ESKİ SORUN: Singleton yapısı nedeniyle bağlantı koptuğunda session hataları alınıyordu.
 * YENİ ÇÖZÜM: getBaglanti() her çağrıldığında taze bir bağlantı oluşturur.
 * Bu sayede uygulama saatlerce açık kalsa bile bağlantı hatası almazsın.
 */
public class DBConnection {

    // Veritabanı bağlantı bilgileri (Şifre ve URL senin sistemine göre ayarlı)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/kutuphane_db?useSSL=false&serverTimezone=Europe/Istanbul&allowPublicKeyRetrieval=true";
    private static final String DB_KULLANICI = "root";
    private static final String DB_SIFRE = "12345";

    /**
     * Veritabanına taze bir bağlantı açar ve döndürür.
     * Artık statik bir nesne tutmadığı için bağlantı bayatlaması (stale connection) yaşanmaz.
     *
     * @return aktif {@link Connection} nesnesi
     */
    public static Connection getBaglanti() {
        try {
            // MySQL JDBC sürücüsünü sisteme tanıtıyoruz
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Sürücü yöneticisinden taze bir bağlantı talep ediyoruz
            Connection yeniBaglanti = DriverManager.getConnection(DB_URL, DB_KULLANICI, DB_SIFRE);

            // Konsola bilgi mesajı (Hata takibi için iyi olur)
            // System.out.println("[DBConnection] Taze bağlantı başarıyla oluşturuldu.");

            return yeniBaglanti;

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Sürücüsü bulunamadı! Library (Kütüphane) ayarlarını kontrol et.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Veritabanına bağlanılamadı! MySQL sunucusunun çalıştığından emin ol.", e);
        }
    }

    /**
     * Eski kodlarla uyumluluk için bırakılmıştır.
     * Artık her metod kendi bağlantısını kapatacağı için bu metot boştadır.
     */
    public static void baglantiKapat() {
        // Singleton yapısı kalktığı için bu metot artık işlevsizdir.
    }
}