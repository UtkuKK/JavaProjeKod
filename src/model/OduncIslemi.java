package model;

import java.time.LocalDate;

/**
 * OduncIslemi — 'odunc_islemleri' tablosunu temsil eden model sınıfı.
 *
 * Sütun eşleşmesi:
 *   id                  → id
 *   kopya_id            → kopyaId
 *   kullanici_id        → kullaniciId
 *   verilis_tarihi      → verilisTarihi
 *   teslim_tarihi       → teslimTarihi
 *   gercek_teslim_tarihi → gercekTeslimTarihi
 *   durum               → durum ('DEVAM_EDIYOR' | 'TESLIM_EDILDI')
 *
 * NOT: Tabloda tutulmayan ancak JOIN ile doldurulan 'kitapBaslik',
 * 'kullaniciAdi' ve 'barkod' alanları da tanımlanmıştır.
 */
public class OduncIslemi {

    // Durum sabitleri
    public static final String DURUM_DEVAM_EDIYOR   = "DEVAM_EDIYOR";
    public static final String DURUM_TESLIM_EDILDI  = "TESLIM_EDILDI";

    // Günlük ceza ücreti (10.00 TL/gün)
    public static final double GUNLUK_CEZA_UCRETI = 10.00;

    // ---------------------------------------------------------------
    // Alanlar
    // ---------------------------------------------------------------
    private int       id;
    private int       kopyaId;
    private int       kullaniciId;
    private LocalDate verilisTarihi;
    private LocalDate teslimTarihi;
    private LocalDate gercekTeslimTarihi;   // null ise henüz iade edilmemiş
    private String    durum;

    // JOIN ile gelen ek okunaklı alanlar (DB sütunu değil)
    private String kitapBaslik;
    private String kullaniciAdi;
    private String barkod;

    // ---------------------------------------------------------------
    // Constructor'lar
    // ---------------------------------------------------------------
    public OduncIslemi() {}

    public OduncIslemi(int id, int kopyaId, int kullaniciId,
                       LocalDate verilisTarihi, LocalDate teslimTarihi,
                       LocalDate gercekTeslimTarihi, String durum) {
        this.id                  = id;
        this.kopyaId             = kopyaId;
        this.kullaniciId         = kullaniciId;
        this.verilisTarihi       = verilisTarihi;
        this.teslimTarihi        = teslimTarihi;
        this.gercekTeslimTarihi  = gercekTeslimTarihi;
        this.durum               = durum;
    }

    // ---------------------------------------------------------------
    // Getter / Setter
    // ---------------------------------------------------------------
    public int getId()         { return id; }
    public void setId(int id)  { this.id = id; }

    public int getKopyaId()              { return kopyaId; }
    public void setKopyaId(int kopyaId)  { this.kopyaId = kopyaId; }

    public int getKullaniciId()                { return kullaniciId; }
    public void setKullaniciId(int v)          { this.kullaniciId = v; }

    public LocalDate getVerilisTarihi()                   { return verilisTarihi; }
    public void setVerilisTarihi(LocalDate v)             { this.verilisTarihi = v; }

    public LocalDate getTeslimTarihi()                    { return teslimTarihi; }
    public void setTeslimTarihi(LocalDate v)              { this.teslimTarihi = v; }

    public LocalDate getGercekTeslimTarihi()              { return gercekTeslimTarihi; }
    public void setGercekTeslimTarihi(LocalDate v)        { this.gercekTeslimTarihi = v; }

    public String getDurum()              { return durum; }
    public void setDurum(String durum)    { this.durum = durum; }

    public String getKitapBaslik()                  { return kitapBaslik; }
    public void setKitapBaslik(String v)            { this.kitapBaslik = v; }

    public String getKullaniciAdi()                 { return kullaniciAdi; }
    public void setKullaniciAdi(String v)           { this.kullaniciAdi = v; }

    public String getBarkod()             { return barkod; }
    public void setBarkod(String v)       { this.barkod = v; }

    // ---------------------------------------------------------------
    // İş Mantığı Yardımcıları
    // ---------------------------------------------------------------

    /** Gecikme gün sayısını hesaplar. Zamanında teslimde 0 döner. */
    public long gecikmeGunSayisi() {
        LocalDate teslim = gercekTeslimTarihi != null ? gercekTeslimTarihi : LocalDate.now();
        long fark = java.time.temporal.ChronoUnit.DAYS.between(teslimTarihi, teslim);
        return Math.max(0, fark);
    }

    /** Uzatılmış teslim süresi için ceza tutarını hesaplar. */
    public double cezaTutari() {
        return gecikmeGunSayisi() * GUNLUK_CEZA_UCRETI;
    }

    @Override
    public String toString() {
        return "OduncIslemi{id=" + id + ", kopyaId=" + kopyaId +
                ", kullaniciId=" + kullaniciId + ", durum='" + durum + "'}";
    }
}
