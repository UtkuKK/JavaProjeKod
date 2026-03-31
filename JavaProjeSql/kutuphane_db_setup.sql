create database kutuphane_DB;
use kutuphane_DB;

/*1kullanicilar*/
Create table kullanicilar (
id INT AUTO_INCREMENT PRIMARY KEY,
ad_Soyad varchar(100),
email varchar(100) unique not null,
sifre varchar(50) not null ,
rol varchar(30) default 'UYE',
durum varchar(30) default 'AKTIF'
);

/*2kategoriler*/
CREATE TABLE kategoriler (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kategori_adi VARCHAR(100) NOT NULL
);

/*3yazarlar*/
CREATE TABLE yazarlar (
    id INT AUTO_INCREMENT PRIMARY KEY,
    yazar_adi VARCHAR(100) NOT NULL
);

/*4yayınEvi*/
CREATE TABLE yayin_evleri (
    id INT AUTO_INCREMENT PRIMARY KEY,
    yayinevi_adi VARCHAR(150) NOT NULL
);

/*5Kitaplar*/
CREATE TABLE kitaplar (
    id INT AUTO_INCREMENT PRIMARY KEY,
    baslik VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    yayin_yili INT,
    kategori_id INT,
    yazar_id INT,
    yayinevi_id INT,
    FOREIGN KEY (kategori_id) REFERENCES kategoriler(id),
    FOREIGN KEY (yazar_id) REFERENCES yazarlar(id),
    FOREIGN KEY (yayinevi_id) REFERENCES yayin_evleri(id)
);

/*6FizikselKitapKopyaları*/
CREATE TABLE kitap_kopyalari (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kitap_id INT NOT NULL,
    barkod VARCHAR(50) UNIQUE NOT NULL,
    kondisyon VARCHAR(50) DEFAULT 'İYİ', 
    durum VARCHAR(50) DEFAULT 'MEVCUT', 
    FOREIGN KEY (kitap_id) REFERENCES kitaplar(id)
);

/*7ÖdünçAlma*/
CREATE TABLE odunc_islemleri (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kopya_id INT NOT NULL,
    kullanici_id INT NOT NULL,
    verilis_tarihi DATE NOT NULL,
    teslim_tarihi DATE NOT NULL, 
    gercek_teslim_tarihi DATE,
    durum VARCHAR(50) DEFAULT 'ACTIVE', 
    FOREIGN KEY (kopya_id) REFERENCES kitap_kopyalari(id),
    FOREIGN KEY (kullanici_id) REFERENCES kullanicilar(id)
);

/*8Cezalar*/
CREATE TABLE cezalar (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kullanici_id INT NOT NULL,
    odunc_id INT NOT NULL,
    ceza_miktari DECIMAL(10,2) NOT NULL,
    aciklama VARCHAR(255), 
    odendi_mi BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (kullanici_id) REFERENCES kullanicilar(id),
    FOREIGN KEY (odunc_id) REFERENCES odunc_islemleri(id)
);

/*9Rezervasyonlar*/
CREATE TABLE rezervasyonlar (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kitap_id INT NOT NULL,
    kullanici_id INT NOT NULL,
    rezervasyon_tarihi DATE NOT NULL,
    durum VARCHAR(50) DEFAULT 'PENDING', 
    FOREIGN KEY (kitap_id) REFERENCES kitaplar(id),
    FOREIGN KEY (kullanici_id) REFERENCES kullanicilar(id)
);




	