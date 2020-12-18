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
  `id` varchar(255) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `dependencies` json DEFAULT NULL,
  `deprecated` bit(1) DEFAULT NULL,
  `display_order` int(11) DEFAULT NULL,
  `flow_id` varchar(255) DEFAULT NULL,
  `flow_ref_id` varchar(255) DEFAULT NULL,
  `processingtask_id` varchar(255) DEFAULT NULL,
  `storagetask_action_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowelement`
--

LOCK TABLES `flowelement` WRITE;
/*!40000 ALTER TABLE `flowelement` DISABLE KEYS */;
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('4', 'video-proxy-flow', null, 'video-proxy-low-gen', null, null, 4, 1, 1);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('5', 'video-proxy-flow', null, 'video-mam-update', null, '[4]', 5, 1, 1);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('6', 'video-proxy-flow', null, null, 'archive-flow', '[4]', 6, 1, 1);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U1', 'video-proxy-flow', null, 'video-proxy-low-gen', null, null, 4, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U2', 'video-proxy-flow', null, 'video-mam-update', null, '["U1"]', 5, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U3', 'video-proxy-flow', null, null, 'archive-flow', '["U1"]', 6, 1, 0);

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
