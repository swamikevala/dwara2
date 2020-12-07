SET FOREIGN_KEY_CHECKS=0;
-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: frm_dev
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
-- Table structure for table `flowelement`
--

DROP TABLE IF EXISTS `flowelement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flowelement` (
  `id` int(11) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `dependencies` json DEFAULT NULL,
  `display_order` int(11) DEFAULT NULL,
  `processingtask_id` varchar(255) DEFAULT NULL,
  `storagetask_action_id` varchar(255) DEFAULT NULL,
  `flow_id` varchar(255) DEFAULT NULL,
  `flow_ref_id` varchar(255) DEFAULT NULL,
  `deprecated` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg569mrqbx358sk1yjbt597yor` (`flow_id`),
  KEY `FK15fjogr6a6dv8j94xv7h82u34` (`flow_ref_id`),
  CONSTRAINT `FK15fjogr6a6dv8j94xv7h82u34` FOREIGN KEY (`flow_ref_id`) REFERENCES `flow` (`id`),
  CONSTRAINT `FKg569mrqbx358sk1yjbt597yor` FOREIGN KEY (`flow_id`) REFERENCES `flow` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowelement`
--

LOCK TABLES `flowelement` WRITE;
/*!40000 ALTER TABLE `flowelement` DISABLE KEYS */;
INSERT INTO `flowelement` VALUES (1,'',NULL,1,'checksum-gen',NULL,'archive-flow',NULL,'\0'),(2,'',NULL,2,NULL,'write','archive-flow',NULL,'\0'),(3,'','[2]',3,NULL,'restore','archive-flow',NULL,'\0'),(4,'','[1, 3]',4,'checksum-verify',NULL,'archive-flow',NULL,'\0'),(5,'',NULL,5,'video-proxy-low-gen',NULL,'video-proxy-flow',NULL,'\0'),(6,'','[5]',6,'video-mam-update',NULL,'video-proxy-flow',NULL,'\0'),(7,'','[5]',7,NULL,NULL,'video-proxy-flow','archive-flow','\0'),(22,'',NULL,22,'video-header-footer-extraction',NULL,'video-digitization-flow',NULL,'\0'),(23,'',NULL,23,'video-preservation-gen',NULL,'video-digitization-flow',NULL,'\0'),(24,'','[23]',24,'bru-copier',NULL,'video-digitization-flow',NULL,'\0'),(25,'','[23]',25,'checksum-gen',NULL,'video-digitization-flow',NULL,'\0'),(26,'','[25]',26,'mxf-exclusion',NULL,'video-digitization-flow',NULL,'\0'),(27,'','[26]',27,NULL,NULL,'video-digitization-flow','video-digitization-archive-flow','\0'),(28,'','[26]',28,NULL,NULL,'video-digitization-flow','video-proxy-flow','\0'),(30,'',NULL,30,NULL,'write','video-digitization-archive-flow',NULL,'\0'),(31,'','[30]',31,NULL,'restore','video-digitization-archive-flow',NULL,'\0'),(32,'','[31]',32,'checksum-verify',NULL,'video-digitization-archive-flow',NULL,'\0');
/*!40000 ALTER TABLE `flowelement` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-12-06 23:09:15
