package model;

/**
 * Kitap — 'kitaplar' tablosunu temsil eden model sınıfı.
 *
 * Sütun eşleşmesi:
 *   id           → id
 *   baslik       → baslik
 *   isbn         → isbn
 *   yayin_yili   → yayinYili
 *   kategori_id  → kategoriId
 *   yazar_id     → yazarId
 *   yayinevi_id  → yayineviId
 *
 * NOT: JOIN sorguları için yazar_adi, kategori_adi, yayinevi_adi gibi
 * denormalize alanlar da tutulmaktadır (veritabanı sütunu değildir).
 */
public class Kitap {

    // ---------------------------------------------------------------
    // Alanlar — veritabanı sütunlarına karşılık gelir
    // ---------------------------------------------------------------
    private int    id;
    private String baslik;
    private String isbn;
    private int    yayinYili;
    private int    kategoriId;
    private int    yazarId;
    private int    yayineviId;

    // JOIN sorgularından gelen ek okunaklı isimler (DB sütunu değil)
    private String kategoriAdi;
    private String yazarAdi;
    private String yayineviAdi;

    // ---------------------------------------------------------------
    // Constructor'lar
    // ---------------------------------------------------------------

    /** Boş constructor — JDBC ResultSet doldurması için */
    public Kitap() {}

    /** Temel alanlarla constructor (ID dahil) */
    public Kitap(int id, String baslik, String isbn, int yayinYili,
                 int kategoriId, int yazarId, int yayineviId) {
        this.id         = id;
        this.baslik     = baslik;
        this.isbn       = isbn;
        this.yayinYili  = yayinYili;
        this.kategoriId = kategoriId;
        this.yazarId    = yazarId;
        this.yayineviId = yayineviId;
    }

    // ---------------------------------------------------------------
    // Getter / Setter
    // ---------------------------------------------------------------

    public int getId()            { return id; }
    public void setId(int id)     { this.id = id; }

    public String getBaslik()               { return baslik; }
    public void setBaslik(String baslik)    { this.baslik = baslik; }

    public String getIsbn()             { return isbn; }
    public void setIsbn(String isbn)    { this.isbn = isbn; }

    public int getYayinYili()                  { return yayinYili; }
    public void setYayinYili(int yayinYili)    { this.yayinYili = yayinYili; }

    public int getKategoriId()                   { return kategoriId; }
    public void setKategoriId(int kategoriId)    { this.kategoriId = kategoriId; }

    public int getYazarId()                { return yazarId; }
    public void setYazarId(int yazarId)    { this.yazarId = yazarId; }

    public int getYayineviId()                   { return yayineviId; }
    public void setYayineviId(int yayineviId)    { this.yayineviId = yayineviId; }

    public String getKategoriAdi()                     { return kategoriAdi; }
    public void setKategoriAdi(String kategoriAdi)     { this.kategoriAdi = kategoriAdi; }

    public String getYazarAdi()                  { return yazarAdi; }
    public void setYazarAdi(String yazarAdi)     { this.yazarAdi = yazarAdi; }

    public String getYayineviAdi()                     { return yayineviAdi; }
    public void setYayineviAdi(String yayineviAdi)     { this.yayineviAdi = yayineviAdi; }

    @Override
    public String toString() {
        return "Kitap{id=" + id +
               ", baslik='" + baslik + '\'' +
               ", isbn='" + isbn + '\'' +
               ", yayinYili=" + yayinYili + '}';
    }
    // Mevcut değişkenlerin altına şunu ekle:
    private String barkodlar;

    // Getter ve Setter metotları:
    public String getBarkodlar() { return barkodlar; }
    public void setBarkodlar(String barkodlar) { this.barkodlar = barkodlar; }
}
