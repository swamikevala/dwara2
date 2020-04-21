-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dwara_v2_test
-- ------------------------------------------------------
-- Server version	5.7.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `test_data_transfer_element`
--

DROP TABLE IF EXISTS `test_data_transfer_element`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_data_transfer_element` (
  `id` int(11) NOT NULL,
  `empty` bit(1) NOT NULL,
  `s_no` int(11) NOT NULL,
  `storage_element_no` int(11) DEFAULT NULL,
  `volume_tag` varchar(255) DEFAULT NULL,
  `tapelibrary_id` int(11) DEFAULT NULL,
  `test_mt_status_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgut4ugiuu8r4kiqc74er0belo` (`tapelibrary_id`),
  KEY `FK8asc5nr209nwdk5aqiipma83h` (`test_mt_status_id`),
  CONSTRAINT `FK8asc5nr209nwdk5aqiipma83h` FOREIGN KEY (`test_mt_status_id`) REFERENCES `test_mt_status` (`id`),
  CONSTRAINT `FKgut4ugiuu8r4kiqc74er0belo` FOREIGN KEY (`tapelibrary_id`) REFERENCES `tapelibrary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_data_transfer_element`
--

LOCK TABLES `test_data_transfer_element` WRITE;
/*!40000 ALTER TABLE `test_data_transfer_element` DISABLE KEYS */;
INSERT INTO `test_data_transfer_element` VALUES (50001,'\0',0,1,'V5A001',14001,51001),(50002,'',1,NULL,NULL,14001,51002),(50003,'',2,NULL,NULL,14001,51003);
/*!40000 ALTER TABLE `test_data_transfer_element` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_mt_status`
--

DROP TABLE IF EXISTS `test_mt_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_mt_status` (
  `id` int(11) NOT NULL,
  `block_number` int(11) DEFAULT NULL,
  `busy` bit(1) NOT NULL,
  `file_number` int(11) DEFAULT NULL,
  `is_write_protected` bit(1) NOT NULL,
  `ready` bit(1) NOT NULL,
  `soft_error_count` int(11) DEFAULT NULL,
  `status_code` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_mt_status`
--

LOCK TABLES `test_mt_status` WRITE;
/*!40000 ALTER TABLE `test_mt_status` DISABLE KEYS */;
INSERT INTO `test_mt_status` VALUES (51001,0,'\0',1,'\0','',0,0),(51002,NULL,'\0',NULL,'\0','\0',NULL,NULL),(51003,NULL,'\0',NULL,'\0','\0',NULL,NULL);
/*!40000 ALTER TABLE `test_mt_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_storage_element`
--

DROP TABLE IF EXISTS `test_storage_element`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_storage_element` (
  `id` int(11) NOT NULL,
  `empty` bit(1) NOT NULL,
  `import_export` bit(1) NOT NULL,
  `s_no` int(11) NOT NULL,
  `volume_tag` varchar(255) DEFAULT NULL,
  `tapelibrary_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKekql8aj50o7jp5d7diwe1649o` (`tapelibrary_id`),
  CONSTRAINT `FKekql8aj50o7jp5d7diwe1649o` FOREIGN KEY (`tapelibrary_id`) REFERENCES `tapelibrary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_storage_element`
--

LOCK TABLES `test_storage_element` WRITE;
/*!40000 ALTER TABLE `test_storage_element` DISABLE KEYS */;
INSERT INTO `test_storage_element` VALUES (52001,'','\0',1,NULL,14001),(52002,'\0','\0',2,'V5A002',14001),(52003,'\0','\0',3,'V5A003',14001),(52004,'\0','\0',4,'V5B003',14001),(52005,'\0','\0',5,'V5C003',14001),(52006,'\0','\0',6,'VLA003',14001),(52007,'\0','\0',7,'V4A003',14001);
/*!40000 ALTER TABLE `test_storage_element` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-04-20 17:35:34
