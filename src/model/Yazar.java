package model;

/**
 * Yazar — 'yazarlar' tablosunu temsil eden model sınıfı.
 * JComboBox'ta kullanım için toString() 'yazar_adi' döndürür.
 */
public class Yazar {
    private int    id;
    private String yazarAdi;

    public Yazar() {}
    public Yazar(int id, String yazarAdi) { this.id = id; this.yazarAdi = yazarAdi; }

    public int    getId()                  { return id; }
    public void   setId(int id)            { this.id = id; }
    public String getYazarAdi()            { return yazarAdi; }
    public void   setYazarAdi(String v)    { this.yazarAdi = v; }

    /** JComboBox bu değeri görüntüler. */
    @Override public String toString()     { return yazarAdi; }
}
