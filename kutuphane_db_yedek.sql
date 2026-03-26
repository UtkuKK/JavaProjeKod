-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: kutuphane_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cezalar`
--

DROP TABLE IF EXISTS `cezalar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cezalar` (
  `id` int NOT NULL AUTO_INCREMENT,
  `kullanici_id` int NOT NULL,
  `odunc_id` int NOT NULL,
  `ceza_miktari` decimal(10,2) NOT NULL,
  `aciklama` varchar(255) DEFAULT NULL,
  `odendi_mi` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `kullanici_id` (`kullanici_id`),
  KEY `odunc_id` (`odunc_id`),
  CONSTRAINT `cezalar_ibfk_1` FOREIGN KEY (`kullanici_id`) REFERENCES `kullanicilar` (`id`),
  CONSTRAINT `cezalar_ibfk_2` FOREIGN KEY (`odunc_id`) REFERENCES `odunc_islemleri` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cezalar`
--

LOCK TABLES `cezalar` WRITE;
/*!40000 ALTER TABLE `cezalar` DISABLE KEYS */;
/*!40000 ALTER TABLE `cezalar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kategoriler`
--

DROP TABLE IF EXISTS `kategoriler`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kategoriler` (
  `id` int NOT NULL AUTO_INCREMENT,
  `kategori_adi` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kategoriler`
--

LOCK TABLES `kategoriler` WRITE;
/*!40000 ALTER TABLE `kategoriler` DISABLE KEYS */;
INSERT INTO `kategoriler` VALUES (2,'Dünya Klasikleri');
/*!40000 ALTER TABLE `kategoriler` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kitap_kopyalari`
--

DROP TABLE IF EXISTS `kitap_kopyalari`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kitap_kopyalari` (
  `id` int NOT NULL AUTO_INCREMENT,
  `kitap_id` int NOT NULL,
  `barkod` varchar(50) NOT NULL,
  `kondisyon` varchar(50) DEFAULT 'İYİ',
  `durum` varchar(50) DEFAULT 'MEVCUT',
  PRIMARY KEY (`id`),
  UNIQUE KEY `barkod` (`barkod`),
  KEY `kitap_id` (`kitap_id`),
  CONSTRAINT `kitap_kopyalari_ibfk_1` FOREIGN KEY (`kitap_id`) REFERENCES `kitaplar` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kitap_kopyalari`
--

LOCK TABLES `kitap_kopyalari` WRITE;
/*!40000 ALTER TABLE `kitap_kopyalari` DISABLE KEYS */;
INSERT INTO `kitap_kopyalari` VALUES (1,4,'BRK-392598','İYİ','ODUNCTE'),(2,5,'BRK-123087','İYİ','ODUNCTE'),(5,8,'BRK-667726','İYİ','MUSAIT'),(18,10,'BRK-264169','İYİ','MUSAIT'),(19,10,'BRK-299452','İYİ','MUSAIT'),(20,10,'BRK-112892','İYİ','MUSAIT'),(21,10,'BRK-387614','İYİ','MUSAIT'),(22,10,'BRK-767565','İYİ','MUSAIT'),(23,10,'BRK-541512','İYİ','MUSAIT');
/*!40000 ALTER TABLE `kitap_kopyalari` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kitaplar`
--

DROP TABLE IF EXISTS `kitaplar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kitaplar` (
  `id` int NOT NULL AUTO_INCREMENT,
  `baslik` varchar(200) NOT NULL,
  `isbn` varchar(20) NOT NULL,
  `yayin_yili` int DEFAULT NULL,
  `kategori_id` int DEFAULT NULL,
  `yazar_id` int DEFAULT NULL,
  `yayinevi_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `isbn` (`isbn`),
  KEY `kategori_id` (`kategori_id`),
  KEY `yazar_id` (`yazar_id`),
  KEY `yayinevi_id` (`yayinevi_id`),
  CONSTRAINT `kitaplar_ibfk_1` FOREIGN KEY (`kategori_id`) REFERENCES `kategoriler` (`id`),
  CONSTRAINT `kitaplar_ibfk_2` FOREIGN KEY (`yazar_id`) REFERENCES `yazarlar` (`id`),
  CONSTRAINT `kitaplar_ibfk_3` FOREIGN KEY (`yayinevi_id`) REFERENCES `yayin_evleri` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kitaplar`
--

LOCK TABLES `kitaplar` WRITE;
/*!40000 ALTER TABLE `kitaplar` DISABLE KEYS */;
INSERT INTO `kitaplar` VALUES (4,'Suç Ve Ceza','1234567891234',2020,2,1,1),(5,'Sefiller','1478523697894',2023,2,2,1),(8,'Don Kişot','4789786789123',2017,2,3,1),(10,'abcd','1234567897896',0,2,1,1);
/*!40000 ALTER TABLE `kitaplar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kullanicilar`
--

DROP TABLE IF EXISTS `kullanicilar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kullanicilar` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ad_Soyad` varchar(100) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `sifre` varchar(50) NOT NULL,
  `rol` varchar(30) DEFAULT 'UYE',
  `durum` varchar(30) DEFAULT 'AKTIF',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kullanicilar`
--

LOCK TABLES `kullanicilar` WRITE;
/*!40000 ALTER TABLE `kullanicilar` DISABLE KEYS */;
INSERT INTO `kullanicilar` VALUES (1,'Admin','admin@mail.com','12345','YONETICI','AKTIF'),(2,'Utku','Utku@mail.com','123456789','UYE','AKTIF'),(4,'deneme1','2121','1','UYE','AKTIF'),(5,'deneme2','aa','a','UYE','AKTIF'),(6,'a','a','a','UYE','AKTIF'),(8,'aa','aab','aa','UYE','AKTIF'),(9,'bab','aba','ababa','UYE','AKTIF');
/*!40000 ALTER TABLE `kullanicilar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `odunc_islemleri`
--

DROP TABLE IF EXISTS `odunc_islemleri`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `odunc_islemleri` (
  `id` int NOT NULL AUTO_INCREMENT,
  `kopya_id` int NOT NULL,
  `kullanici_id` int NOT NULL,
  `verilis_tarihi` date NOT NULL,
  `teslim_tarihi` date NOT NULL,
  `gercek_teslim_tarihi` date DEFAULT NULL,
  `durum` varchar(50) DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  KEY `kopya_id` (`kopya_id`),
  KEY `kullanici_id` (`kullanici_id`),
  CONSTRAINT `odunc_islemleri_ibfk_1` FOREIGN KEY (`kopya_id`) REFERENCES `kitap_kopyalari` (`id`),
  CONSTRAINT `odunc_islemleri_ibfk_2` FOREIGN KEY (`kullanici_id`) REFERENCES `kullanicilar` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `odunc_islemleri`
--

LOCK TABLES `odunc_islemleri` WRITE;
/*!40000 ALTER TABLE `odunc_islemleri` DISABLE KEYS */;
INSERT INTO `odunc_islemleri` VALUES (1,1,2,'2026-03-26','2026-04-09','2026-03-26','TESLIM_EDILDI'),(2,2,1,'2026-03-26','2026-04-09','2026-03-26','TESLIM_EDILDI'),(4,2,2,'2026-03-26','2026-04-09','2026-03-26','TESLIM_EDILDI'),(5,1,2,'2026-03-26','2026-04-09',NULL,'DEVAM_EDIYOR'),(6,2,2,'2026-03-26','2026-03-27',NULL,'DEVAM_EDIYOR'),(7,5,2,'2026-03-26','2026-04-05','2026-03-26','TESLIM_EDILDI');
/*!40000 ALTER TABLE `odunc_islemleri` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rezervasyonlar`
--

DROP TABLE IF EXISTS `rezervasyonlar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rezervasyonlar` (
  `id` int NOT NULL AUTO_INCREMENT,
  `kitap_id` int NOT NULL,
  `kullanici_id` int NOT NULL,
  `rezervasyon_tarihi` date NOT NULL,
  `durum` varchar(50) DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `kitap_id` (`kitap_id`),
  KEY `kullanici_id` (`kullanici_id`),
  CONSTRAINT `rezervasyonlar_ibfk_1` FOREIGN KEY (`kitap_id`) REFERENCES `kitaplar` (`id`),
  CONSTRAINT `rezervasyonlar_ibfk_2` FOREIGN KEY (`kullanici_id`) REFERENCES `kullanicilar` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rezervasyonlar`
--

LOCK TABLES `rezervasyonlar` WRITE;
/*!40000 ALTER TABLE `rezervasyonlar` DISABLE KEYS */;
INSERT INTO `rezervasyonlar` VALUES (1,8,2,'2026-03-26','IPTAL'),(2,8,1,'2026-03-26','TAMAMLANDI'),(3,5,1,'2026-03-26','TAMAMLANDI'),(4,5,2,'2026-03-26','IPTAL'),(5,10,1,'2026-03-26','IPTAL'),(6,10,1,'2026-03-26','IPTAL'),(7,10,1,'2026-03-26','BEKLIYOR'),(8,10,2,'2026-03-26','BEKLIYOR'),(9,10,4,'2026-03-26','BEKLIYOR'),(10,10,5,'2026-03-26','BEKLIYOR'),(11,10,6,'2026-03-26','BEKLIYOR'),(12,10,8,'2026-03-26','BEKLIYOR');
/*!40000 ALTER TABLE `rezervasyonlar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yayin_evleri`
--

DROP TABLE IF EXISTS `yayin_evleri`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `yayin_evleri` (
  `id` int NOT NULL AUTO_INCREMENT,
  `yayinevi_adi` varchar(150) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yayin_evleri`
--

LOCK TABLES `yayin_evleri` WRITE;
/*!40000 ALTER TABLE `yayin_evleri` DISABLE KEYS */;
INSERT INTO `yayin_evleri` VALUES (1,'YKY');
/*!40000 ALTER TABLE `yayin_evleri` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yazarlar`
--

DROP TABLE IF EXISTS `yazarlar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `yazarlar` (
  `id` int NOT NULL AUTO_INCREMENT,
  `yazar_adi` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yazarlar`
--

LOCK TABLES `yazarlar` WRITE;
/*!40000 ALTER TABLE `yazarlar` DISABLE KEYS */;
INSERT INTO `yazarlar` VALUES (1,'Dostoyevski'),(2,'Victor Hugo'),(3,'Cervantes');
/*!40000 ALTER TABLE `yazarlar` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-26 22:10:56
