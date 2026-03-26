package model;

/**
 * KitapKopyasi — 'kitap_kopyalari' tablosunu temsil eden model sınıfı.
 *
 * Sütun eşleşmesi:
 *   id       → id
 *   kitap_id → kitapId
 *   barkod   → barkod
 *   kondisyon → kondisyon   ('IYI', 'ORTA', 'KOTU')
 *   durum    → durum        ('MUSAIT', 'ODUNCTE', 'REZERVELI')
 *
 * NOT: JOIN sorgularından kitabın başlığını tutmak üzere 'kitapBaslik'
 * denormalize alanı da tanımlanmıştır (veritabanı sütunu değildir).
 */
public class KitapKopyasi {

    // ---------------------------------------------------------------
    // Veritabanı sütun sabitleri — durum değerleri
    // ---------------------------------------------------------------
    public static final String DURUM_MUSAIT   = "MUSAIT";
    public static final String DURUM_ODUNCTE  = "ODUNCTE";
    public static final String DURUM_REZERVELI = "REZERVELI";

    public static final String KONDISYON_IYI  = "IYI";
    public static final String KONDISYON_ORTA = "ORTA";
    public static final String KONDISYON_KOTU = "KOTU";

    // ---------------------------------------------------------------
    // Alanlar
    // ---------------------------------------------------------------
    private int    id;
    private int    kitapId;
    private String barkod;
    private String kondisyon;
    private String durum;

    // JOIN sorgusundan gelen ek alan (DB sütunu değil)
    private String kitapBaslik;

    // ---------------------------------------------------------------
    // Constructor'lar
    // ---------------------------------------------------------------

    /** Boş constructor — JDBC ResultSet doldurması için */
    public KitapKopyasi() {}

    /** Temel alanlarla constructor */
    public KitapKopyasi(int id, int kitapId, String barkod,
                        String kondisyon, String durum) {
        this.id        = id;
        this.kitapId   = kitapId;
        this.barkod    = barkod;
        this.kondisyon = kondisyon;
        this.durum     = durum;
    }

    // ---------------------------------------------------------------
    // Getter / Setter
    // ---------------------------------------------------------------

    public int getId()          { return id; }
    public void setId(int id)   { this.id = id; }

    public int getKitapId()              { return kitapId; }
    public void setKitapId(int kitapId)  { this.kitapId = kitapId; }

    public String getBarkod()               { return barkod; }
    public void setBarkod(String barkod)    { this.barkod = barkod; }

    public String getKondisyon()                  { return kondisyon; }
    public void setKondisyon(String kondisyon)     { this.kondisyon = kondisyon; }

    public String getDurum()              { return durum; }
    public void setDurum(String durum)    { this.durum = durum; }

    public String getKitapBaslik()                     { return kitapBaslik; }
    public void setKitapBaslik(String kitapBaslik)     { this.kitapBaslik = kitapBaslik; }

    /** Kopyanın ödünç verilebilir durumda olup olmadığını kontrol eder. */
    public boolean isMusait() {
        return DURUM_MUSAIT.equalsIgnoreCase(this.durum);
    }

    @Override
    public String toString() {
        return "KitapKopyasi{id=" + id +
               ", kitapId=" + kitapId +
               ", barkod='" + barkod + '\'' +
               ", kondisyon='" + kondisyon + '\'' +
               ", durum='" + durum + '\'' + '}';
    }
}
