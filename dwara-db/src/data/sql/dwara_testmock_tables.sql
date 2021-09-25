/* NOTE: In local if you are using a prod db dump then please update Devices table too */

-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dwara
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
-- Table structure for table `device`
--

DROP TABLE IF EXISTS `device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `device` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `defective` bit(1) DEFAULT NULL,
  `details` json DEFAULT NULL,
  `manufacturer` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `model` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `serial_number` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `warranty_expiry_date` datetime(6) DEFAULT NULL,
  `wwn_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `employed_date` datetime(6) DEFAULT NULL,
  `retired_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4776vaiywo1kdis4lp8jkm0av` (`serial_number`),
  UNIQUE KEY `UK_rybeolllge5fl2xeefjy2gi27` (`wwn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device`
--

LOCK TABLES `device` WRITE;
/*!40000 ALTER TABLE `device` DISABLE KEYS */;
INSERT INTO `device` (`id`, `defective`, `details`, `manufacturer`, `model`, `serial_number`, `status`, `type`, `warranty_expiry_date`, `wwn_id`, `employed_date`, `retired_date`) VALUES ('lto6-1','\0','{\"type\": \"LTO-6\", \"standalone\": false, \"autoloader_id\": \"xl80\"}','IBM','Ultrium HH6','YR10WT083802','offline','tape_drive','2021-03-28 00:00:00.000000','/dev/tape/by-id/scsi-35000e111c5aa70e7-nst',NULL,NULL),('lto7-1','\0','{\"type\": \"LTO-7\", \"standalone\": false, \"autoloader_id\": \"xl80\", \"autoloader_address\": 1}','IBM','Ultrium HH7','YX10WT103623','online','tape_drive','2022-04-15 00:00:00.000000','/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst',NULL,NULL),('lto7-2','','{\"type\": \"LTO-7\", \"standalone\": false, \"autoloader_id\": \"xl80\"}','IBM','Ultrium HH7','YX10WT134623','offline','tape_drive','2023-07-12 00:00:00.000000',NULL,NULL,NULL),('lto7-3','','{\"type\": \"LTO-7\", \"standalone\": false, \"autoloader_id\": \"xl80\"}','IBM','Ultrium HH7','YX10WT096005','offline','tape_drive','2022-12-31 00:00:00.000000','',NULL,'2021-02-20 12:00:00.000000'),('lto7-4','\0','{\"type\": \"LTO-7\", \"standalone\": false, \"autoloader_id\": \"xl80\", \"autoloader_address\": 3}','IBM','Ultrium HH7','YX1097011322','offline','tape_drive','2023-07-12 00:00:00.000000','/dev/tape/by-id/scsi-35000e111c5aa70d3-nst',NULL,NULL),('lto7-5','\0','{\"type\": \"LTO-7\", \"standalone\": false, \"autoloader_id\": \"xl80\", \"autoloader_address\": 2}','IBM','Ultrium HH7','YX10WT093970','online','tape_drive','2023-07-12 00:00:00.000000','/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1970448833-nst',NULL,NULL),('lto7-6','\0','{\"type\": \"LTO-7\", \"standalone\": false, \"autoloader_id\": \"xl80\", \"autoloader_address\": 4}','IBM','Ultrium HH7','YX10WT103628','offline','tape_drive','2023-07-12 00:00:00.000000','/dev/tape/by-id/scsi-35000e111c5aa70dd-nst',NULL,NULL),('lto7-7','\0','{\"type\": \"LTO-7\", \"standalone\": false, \"autoloader_id\": \"xl80\", \"autoloader_address\": 0}','IBM','Ultrium HH7','YX10WT124812','online','tape_drive','2022-12-31 00:00:00.000000','/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst','2021-02-20 12:00:00.000000',NULL),('xl80','\0','{\"slots\": 80, \"max_drives\": 6, \"generations_supported\": [6, 7]}','Overland Tandberg','Neo XL80','DE68102376','online','tape_autoloader','2022-03-30 00:00:00.000000','/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400',NULL,NULL);
/*!40000 ALTER TABLE `device` ENABLE KEYS */;
UNLOCK TABLES;

/* Devices end */

--
-- Dumping events for database 'dwara'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-09-24 16:34:16


-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dwara
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
  `empty` bit(1) NOT NULL,
  `s_num` int(11) DEFAULT NULL,
  `storage_element_no` int(11) DEFAULT NULL,
  `tapedrive_uid` varchar(255) DEFAULT NULL,
  `tapelibrary_uid` varchar(255) DEFAULT NULL,
  `volume_tag` varchar(255) DEFAULT NULL,
  `mock_mt_status_id` int(11) DEFAULT NULL,
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
INSERT INTO `zmock_data_transfer_element` (`id`, `empty`, `s_num`, `storage_element_no`, `tapedrive_uid`, `tapelibrary_uid`, `volume_tag`, `mock_mt_status_id`) VALUES (1,'',0,NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst','/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400',NULL,1),(2,'',1,NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst','/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400',NULL,2),(3,'',2,NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1970448833-nst','/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400',NULL,3);
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
INSERT INTO `zmock_mt_status` (`id`, `block_number`, `busy`, `file_number`, `is_write_protected`, `ready`, `soft_error_count`, `status_code`) VALUES (1,-1,'\0',-1,'\0','\0',NULL,1),(2,-1,'\0',-1,'\0','\0',NULL,1),(3,-1,'\0',-1,'\0','\0',NULL,1);
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
  `empty` bit(1) NOT NULL,
  `import_export` bit(1) NOT NULL,
  `s_no` int(11) NOT NULL,
  `tapelibrary_uid` varchar(255) DEFAULT NULL,
  `volume_tag` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zmock_storage_element`
--

LOCK TABLES `zmock_storage_element` WRITE;
/*!40000 ALTER TABLE `zmock_storage_element` DISABLE KEYS */;
INSERT INTO `zmock_storage_element` (`id`, `empty`, `import_export`, `s_no`, `tapelibrary_uid`, `volume_tag`) VALUES (1,'\0','\0',1,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','E10015L7'),(2,'\0','\0',2,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','X10007L7'),(3,'\0','\0',3,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','E10006L7'),(4,'\0','\0',4,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','R10029L7'),(5,'\0','\0',5,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','R30019L7'),(6,'\0','\0',6,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','E30015L7'),(7,'\0','\0',7,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','X10004L7'),(8,'\0','\0',8,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','E10008L7'),(9,'\0','\0',9,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','V5A999L7'),(10,'\0','\0',10,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','V5B001'),(11,'\0','\0',11,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','N10001'),(12,'\0','\0',12,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','N20001L7'),(13,'\0','\0',13,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400','N20002L7');
/*!40000 ALTER TABLE `zmock_storage_element` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'dwara'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-09-24 16:31:47
