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

INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('4', 'video-proxy-flow', null, 'video-proxy-low-gen', null, null, 4, 1, 1);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('5', 'video-proxy-flow', null, 'video-mam-update', null, '[4]', 5, 1, 1);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('6', 'video-proxy-flow', null, null, 'archive-flow', '[4]', 6, 1, 1);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U1', 'video-proxy-flow', null, 'video-proxy-low-gen', null, null, 4, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U2', 'video-proxy-flow', null, 'video-mam-update', null, '["U1"]', 5, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U3', 'video-proxy-flow', null, null, 'archive-flow', '["U1"]', 6, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U4', 'video-digi-2020-flow', null, 'video-digi-2020-header-extract', null, null, 1, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U5', 'video-digi-2020-flow', null, 'video-digi-2020-preservation-gen', null, null, 2, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U6', 'video-digi-2020-flow', null, 'checksum-gen', null, '["U4","U5"]', 3, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U7', 'video-digi-2020-flow', null, 'file-delete', null, '["U6"]', 4, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U8', 'video-digi-2020-flow', null, null, 'video-digi-2020-archive-flow', '["U7"]', 5, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U9', 'video-digi-2020-flow', null, 'video-mkv-pfr-metadata-extract', null, '["U7"]', 6, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U10', 'video-digi-2020-flow', null, null, 'video-proxy-flow', '["U7"]', 7, 1, 0);
-- Need a custom video-digi-2020-archive-flow, since the core archive-flow starts with checksum-gen task which we do not need at this step
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U11', 'video-digi-2020-archive-flow', 'write', null, null, null, 1, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U12', 'video-digi-2020-archive-flow', 'restore', null, null, '["U11"]', 2, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U13', 'video-digi-2020-archive-flow', null, 'checksum-verify', null, '["U12"]', 3, 1, 0);
