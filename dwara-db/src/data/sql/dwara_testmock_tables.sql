-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dwara_v4_test
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
-- Table structure for table `zmock_data_transfer_element`
--

DROP TABLE IF EXISTS `zmock_data_transfer_element`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zmock_data_transfer_element` (
  `id` int(11) NOT NULL,
  `tapedrive_uid` varchar(255) DEFAULT NULL,
  `tapelibrary_uid` varchar(255) DEFAULT NULL,
  `mock_mt_status_id` int(11) DEFAULT NULL,
  `empty` bit(1) NOT NULL,
  `s_no` int(11) NOT NULL,
  `storage_element_no` int(11) DEFAULT NULL,
  `volume_tag` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_9jwvsxvlmpvnpdaafm6vmct69` (`tapedrive_uid`),
  KEY `FKr6393m3xyc3idgevwc2actprj` (`mock_mt_status_id`),
  CONSTRAINT `FKr6393m3xyc3idgevwc2actprj` FOREIGN KEY (`mock_mt_status_id`) REFERENCES `zmock_mt_status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zmock_data_transfer_element`
--

LOCK TABLES `zmock_data_transfer_element` WRITE;
/*!40000 ALTER TABLE `zmock_data_transfer_element` DISABLE KEYS */;
INSERT INTO `zmock_data_transfer_element` VALUES (1,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst','/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400',1,'\0',0,11,'V5C001'),(2,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst','/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400',2,'',1,NULL,NULL),(3,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1970448833-nst','/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400',3,'',2,NULL,NULL);
/*!40000 ALTER TABLE `zmock_data_transfer_element` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `zmock_mt_status`
--

DROP TABLE IF EXISTS `zmock_mt_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zmock_mt_status` (
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
-- Dumping data for table `zmock_mt_status`
--

LOCK TABLES `zmock_mt_status` WRITE;
/*!40000 ALTER TABLE `zmock_mt_status` DISABLE KEYS */;
INSERT INTO `zmock_mt_status` VALUES (1,0,'\0',1,'\0','',NULL,3),(2,-1,'\0',-1,'\0','\0',NULL,1),(3,-1,'\0',-1,'\0','\0',NULL,1);
/*!40000 ALTER TABLE `zmock_mt_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `zmock_storage_element`
--

DROP TABLE IF EXISTS `zmock_storage_element`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zmock_storage_element` (
  `id` int(11) NOT NULL,
  `tapelibrary_uid` varchar(255) DEFAULT NULL,
  `empty` bit(1) NOT NULL,
  `import_export` bit(1) NOT NULL,
  `s_no` int(11) NOT NULL,
  `volume_tag` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zmock_storage_element`
--

LOCK TABLES `zmock_storage_element` WRITE;
/*!40000 ALTER TABLE `zmock_storage_element` DISABLE KEYS */;
INSERT INTO `zmock_storage_element` VALUES (1,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','\0','\0',1,'V5A001'),(2,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','\0','\0',2,'V5A002'),(3,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','\0','\0',3,'V5A003'),(4,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','\0','\0',4,'V5B003'),(5,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','\0','\0',5,'V5C003'),(6,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','\0','\0',6,'VLA003'),(7,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','\0','\0',7,'V4A003'),(8,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','\0','\0',8,'V5A004'),(9,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','\0','\0',9,'V5A999L7'),(10,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','\0','\0',10,'V5B001'),(11,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','','\0',11,NULL);
/*!40000 ALTER TABLE `zmock_storage_element` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-07-17 21:32:04
