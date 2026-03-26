package model;

import java.math.BigDecimal;

/**
 * Ceza — 'cezalar' tablosunu temsil eden model sınıfı.
 *
 * Sütun eşleşmesi:
 *   id             → id
 *   kullanici_id   → kullaniciId
 *   odunc_id       → oduncId
 *   ceza_miktari   → cezaMiktari
 *   aciklama       → aciklama
 *   odendi_mi      → odendiMi
 *
 * NOT: kullaniciAdi JOIN ile doldurulur (DB sütunu değil).
 */
public class Ceza {

    // ---------------------------------------------------------------
    // Alanlar
    // ---------------------------------------------------------------
    private int        id;
    private int        kullaniciId;
    private int        oduncId;
    private BigDecimal cezaMiktari;
    private String     aciklama;
    private boolean    odendiMi;

    // JOIN alanı
    private String kullaniciAdi;

    // ---------------------------------------------------------------
    // Constructor'lar
    // ---------------------------------------------------------------
    public Ceza() {}

    public Ceza(int kullaniciId, int oduncId, BigDecimal cezaMiktari, String aciklama) {
        this.kullaniciId = kullaniciId;
        this.oduncId     = oduncId;
        this.cezaMiktari = cezaMiktari;
        this.aciklama    = aciklama;
        this.odendiMi    = false;
    }

    // ---------------------------------------------------------------
    // Getter / Setter
    // ---------------------------------------------------------------
    public int getId()          { return id; }
    public void setId(int id)   { this.id = id; }

    public int getKullaniciId()               { return kullaniciId; }
    public void setKullaniciId(int v)         { this.kullaniciId = v; }

    public int getOduncId()             { return oduncId; }
    public void setOduncId(int oduncId) { this.oduncId = oduncId; }

    public BigDecimal getCezaMiktari()                { return cezaMiktari; }
    public void setCezaMiktari(BigDecimal v)          { this.cezaMiktari = v; }

    public String getAciklama()               { return aciklama; }
    public void setAciklama(String aciklama)  { this.aciklama = aciklama; }

    public boolean isOdendiMi()               { return odendiMi; }
    public void setOdendiMi(boolean odendiMi) { this.odendiMi = odendiMi; }

    public String getKullaniciAdi()           { return kullaniciAdi; }
    public void setKullaniciAdi(String v)     { this.kullaniciAdi = v; }

    @Override
    public String toString() {
        return "Ceza{id=" + id + ", kullaniciId=" + kullaniciId +
                ", cezaMiktari=" + cezaMiktari + ", odendiMi=" + odendiMi + '}';
    }
}
