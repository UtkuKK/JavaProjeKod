package model;

/**
 * Kategori — 'kategoriler' tablosunu temsil eden model sınıfı.
 * JComboBox'ta kullanım için toString() 'kategori_adi' döndürür.
 */
public class Kategori {
    private int    id;
    private String kategoriAdi;

    public Kategori() {}
    public Kategori(int id, String kategoriAdi) { this.id = id; this.kategoriAdi = kategoriAdi; }

    public int    getId()                          { return id; }
    public void   setId(int id)                    { this.id = id; }
    public String getKategoriAdi()                 { return kategoriAdi; }
    public void   setKategoriAdi(String v)         { this.kategoriAdi = v; }

    /** JComboBox bu değeri görüntüler. */
    @Override public String toString()             { return kategoriAdi; }
}
