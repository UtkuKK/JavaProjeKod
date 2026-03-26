package model;

/**
 * Kullanici — 'kullanicilar' tablosunu temsil eden model sınıfı.
 *
 * Sütun eşleşmesi:
 *   id         → id
 *   ad_Soyad   → adSoyad
 *   email      → email
 *   sifre      → sifre
 *   rol        → rol        ('YONETICI' veya 'UYE')
 *   durum      → durum      ('AKTIF' veya 'PASIF')
 */
public class Kullanici {

    // ---------------------------------------------------------------
    // Alanlar
    // ---------------------------------------------------------------
    private int    id;
    private String adSoyad;
    private String email;
    private String sifre;
    private String rol;    // "YONETICI" | "UYE"
    private String durum;  // "AKTIF"    | "PASIF"

    // ---------------------------------------------------------------
    // Constructor'lar
    // ---------------------------------------------------------------

    /** Boş constructor — JDBC ResultSet doldurması için */
    public Kullanici() {}

    /** Tüm alanlarla tam constructor */
    public Kullanici(int id, String adSoyad, String email,
                     String sifre, String rol, String durum) {
        this.id      = id;
        this.adSoyad = adSoyad;
        this.email   = email;
        this.sifre   = sifre;
        this.rol     = rol;
        this.durum   = durum;
    }

    // ---------------------------------------------------------------
    // Getter / Setter
    // ---------------------------------------------------------------

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }

    public String getAdSoyad()               { return adSoyad; }
    public void setAdSoyad(String adSoyad)   { this.adSoyad = adSoyad; }

    public String getEmail()              { return email; }
    public void setEmail(String email)    { this.email = email; }

    public String getSifre()              { return sifre; }
    public void setSifre(String sifre)    { this.sifre = sifre; }

    public String getRol()            { return rol; }
    public void setRol(String rol)    { this.rol = rol; }

    public String getDurum()              { return durum; }
    public void setDurum(String durum)    { this.durum = durum; }

    // ---------------------------------------------------------------
    // Yardımcı metodlar
    // ---------------------------------------------------------------

    /** Kullanıcının yönetici olup olmadığını döndürür. */
    public boolean isYonetici() {
        return "YONETICI".equalsIgnoreCase(this.rol);
    }

    /** Hesabın aktif olup olmadığını döndürür. */
    public boolean isAktif() {
        return "AKTIF".equalsIgnoreCase(this.durum);
    }

    @Override
    public String toString() {
        return "Kullanici{id=" + id +
               ", adSoyad='" + adSoyad + '\'' +
               ", email='" + email + '\'' +
               ", rol='" + rol + '\'' +
               ", durum='" + durum + '\'' + '}';
    }
}
