package model;

/**
 * YayinEvi — 'yayin_evleri' tablosunu temsil eden model sınıfı.
 * JComboBox'ta kullanım için toString() 'yayinevi_adi' döndürür.
 */
public class YayinEvi {
    private int    id;
    private String yayineviAdi;

    public YayinEvi() {}
    public YayinEvi(int id, String yayineviAdi) { this.id = id; this.yayineviAdi = yayineviAdi; }

    public int    getId()                   { return id; }
    public void   setId(int id)             { this.id = id; }
    public String getYayineviAdi()          { return yayineviAdi; }
    public void   setYayineviAdi(String v)  { this.yayineviAdi = v; }

    /** JComboBox bu değeri görüntüler. */
    @Override public String toString()      { return yayineviAdi; }
}
