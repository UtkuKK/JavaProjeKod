package model;

import java.time.LocalDate;

/**
 * Rezervasyon — 'rezervasyonlar' tablosunu temsil eden model sınıfı.
 */
public class Rezervasyon {

    public static final String DURUM_BEKLIYOR   = "BEKLIYOR";
    public static final String DURUM_TAMAMLANDI = "TAMAMLANDI";
    public static final String DURUM_IPTAL      = "IPTAL";

    private int id;
    private int kullaniciId;
    private int kitapId;
    private LocalDate rezervasyonTarihi;
    private String durum;

    // Arayüzde göstermek için ek bilgiler (JOIN)
    private String kitapBaslik;
    private String kullaniciAdi;

    public Rezervasyon() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getKullaniciId() { return kullaniciId; }
    public void setKullaniciId(int kullaniciId) { this.kullaniciId = kullaniciId; }

    public int getKitapId() { return kitapId; }
    public void setKitapId(int kitapId) { this.kitapId = kitapId; }

    public LocalDate getRezervasyonTarihi() { return rezervasyonTarihi; }
    public void setRezervasyonTarihi(LocalDate rezervasyonTarihi) { this.rezervasyonTarihi = rezervasyonTarihi; }

    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }

    public String getKitapBaslik() { return kitapBaslik; }
    public void setKitapBaslik(String kitapBaslik) { this.kitapBaslik = kitapBaslik; }

    public String getKullaniciAdi() { return kullaniciAdi; }
    public void setKullaniciAdi(String kullaniciAdi) { this.kullaniciAdi = kullaniciAdi; }

    @Override
    public String toString() {
        return "Rezervasyon{" + "id=" + id + ", kitap=" + kitapBaslik + ", kullanici=" + kullaniciAdi + ", durum=" + durum + '}';
    }
}
