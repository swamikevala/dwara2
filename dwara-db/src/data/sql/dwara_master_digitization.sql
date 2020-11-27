-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: frm_dev_orig
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
-- Table structure for table `action_artifactclass_flow`
--

DROP TABLE IF EXISTS `action_artifactclass_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action_artifactclass_flow` (
  `active` bit(1) DEFAULT NULL,
  `action_id` varchar(255) NOT NULL,
  `artifactclass_id` varchar(255) NOT NULL,
  `flow_id` varchar(255) NOT NULL,
  PRIMARY KEY (`action_id`,`artifactclass_id`,`flow_id`),
  KEY `FKcflfmq5vxei13li2ekktey3f9` (`artifactclass_id`),
  KEY `FKhkpj2wrenaktnes4te6fda426` (`flow_id`),
  CONSTRAINT `FKcflfmq5vxei13li2ekktey3f9` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`),
  CONSTRAINT `FKhkpj2wrenaktnes4te6fda426` FOREIGN KEY (`flow_id`) REFERENCES `flow` (`id`),
  CONSTRAINT `FKidvy2kx9wbuxydhl4ggyd8wjj` FOREIGN KEY (`action_id`) REFERENCES `action` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action_artifactclass_flow`
--

LOCK TABLES `action_artifactclass_flow` WRITE;
/*!40000 ALTER TABLE `action_artifactclass_flow` DISABLE KEYS */;
INSERT INTO `action_artifactclass_flow` VALUES ('','ingest','prasad-pub','video-prasad-flow'),('','ingest','prasad-pub','video-prasad-raw-flow');
/*!40000 ALTER TABLE `action_artifactclass_flow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `action_artifactclass_user`
--

DROP TABLE IF EXISTS `action_artifactclass_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action_artifactclass_user` (
  `action_id` varchar(255) NOT NULL,
  `artifactclass_id` varchar(255) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`action_id`,`artifactclass_id`,`user_id`),
  KEY `FKb2xnsp7o5cfj343eepjd2pe5y` (`artifactclass_id`),
  KEY `FKqis923yolu1jdttrv43snq4dl` (`user_id`),
  CONSTRAINT `FKau5h2q88roe24d6ven9yd01lq` FOREIGN KEY (`action_id`) REFERENCES `action` (`id`),
  CONSTRAINT `FKb2xnsp7o5cfj343eepjd2pe5y` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`),
  CONSTRAINT `FKqis923yolu1jdttrv43snq4dl` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action_artifactclass_user`
--

LOCK TABLES `action_artifactclass_user` WRITE;
/*!40000 ALTER TABLE `action_artifactclass_user` DISABLE KEYS */;
INSERT INTO `action_artifactclass_user` VALUES ('ingest','prasad-pub',2),('ingest','prasad-pub',3),('ingest','prasad-pub',4);
/*!40000 ALTER TABLE `action_artifactclass_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifactclass`
--

DROP TABLE IF EXISTS `artifactclass`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifactclass` (
  `id` varchar(255) NOT NULL,
  `concurrent_volume_copies` bit(1) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `display_order` int(11) DEFAULT NULL,
  `domain_id` varchar(255) DEFAULT NULL,
  `import_only` bit(1) DEFAULT NULL,
  `path_prefix` varchar(255) DEFAULT NULL,
  `source` bit(1) DEFAULT NULL,
  `artifactclass_ref_id` varchar(255) DEFAULT NULL,
  `sequence_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm2307v6xaw32qecrwjkl6lvsu` (`artifactclass_ref_id`),
  KEY `FKeynrnq0kfcuqn53tklcqexghk` (`sequence_id`),
  CONSTRAINT `FKeynrnq0kfcuqn53tklcqexghk` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`),
  CONSTRAINT `FKm2307v6xaw32qecrwjkl6lvsu` FOREIGN KEY (`artifactclass_ref_id`) REFERENCES `artifactclass` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifactclass`
--

LOCK TABLES `artifactclass` WRITE;
/*!40000 ALTER TABLE `artifactclass` DISABLE KEYS */;
INSERT INTO `artifactclass` VALUES ('prasad-pub','\0','',42,'1','\0','/data/staged','',NULL,'prasad-pub'),('prasad-pub-hdr-ftr','\0','',62,'1','\0','/data/transcoded','\0','prasad-pub','prasad-pub-hdr-ftr'),('prasad-pub-preservation','\0','',63,'1','\0','/data/transcoded','\0','prasad-pub','prasad-pub-preservation'),('prasad-pub-proxy-low','\0','',64,'1','\0','/data/transcoded','\0','prasad-pub-preservation','prasad-pub-proxy-low');
/*!40000 ALTER TABLE `artifactclass` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifactclass_volume`
--

DROP TABLE IF EXISTS `artifactclass_volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifactclass_volume` (
  `active` bit(1) DEFAULT NULL,
  `encrypted` bit(1) DEFAULT NULL,
  `artifactclass_id` varchar(255) NOT NULL,
  `volume_id` varchar(255) NOT NULL,
  PRIMARY KEY (`artifactclass_id`,`volume_id`),
  KEY `FKqusi30rwg38bm03d7sxk2t79t` (`volume_id`),
  CONSTRAINT `FK4fehk3uq1pgc8xasxs6s6xq6d` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`),
  CONSTRAINT `FKqusi30rwg38bm03d7sxk2t79t` FOREIGN KEY (`volume_id`) REFERENCES `volume` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifactclass_volume`
--

LOCK TABLES `artifactclass_volume` WRITE;
/*!40000 ALTER TABLE `artifactclass_volume` DISABLE KEYS */;
INSERT INTO `artifactclass_volume` VALUES ('','\0','prasad-pub-hdr-ftr','R1'),('','\0','prasad-pub-hdr-ftr','R2'),('','\0','prasad-pub-hdr-ftr','R3'),('','\0','prasad-pub-preservation','R1'),('','\0','prasad-pub-preservation','R2'),('','\0','prasad-pub-preservation','R3'),('','\0','prasad-pub-proxy-low','G1'),('','\0','prasad-pub-proxy-low','G2'),('','\0','prasad-pub-proxy-low','G3');
/*!40000 ALTER TABLE `artifactclass_volume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `extension`
--

DROP TABLE IF EXISTS `extension`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `extension` (
  `id` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `ignore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extension`
--

LOCK TABLES `extension` WRITE;
/*!40000 ALTER TABLE `extension` DISABLE KEYS */;
INSERT INTO `extension` VALUES ('',NULL,NULL),('cues','Prasad - Extracted index from mkv','\0'),('ftr','Prasad - Extract header from the source mxf','\0'),('hdr','Prasad - Extract header from both mxf and mkv','\0'),('md5','Prasad - md5 file','\0'),('mkv','Prasad - Metroska','\0'),('mxf','Prasad - raw uncompressed source file','\0'),('qcr','Prasad - QC report file','\0');
/*!40000 ALTER TABLE `extension` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `extension_filetype`
--

DROP TABLE IF EXISTS `extension_filetype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `extension_filetype` (
  `sidecar` bit(1) DEFAULT NULL,
  `extension_id` varchar(255) NOT NULL,
  `filetype_id` varchar(255) NOT NULL,
  PRIMARY KEY (`extension_id`,`filetype_id`),
  KEY `FKisal8u7vwfumc2r09bdxekdw6` (`filetype_id`),
  CONSTRAINT `FK2tsonlt8ut5at1khmlxop9tog` FOREIGN KEY (`extension_id`) REFERENCES `extension` (`id`),
  CONSTRAINT `FKisal8u7vwfumc2r09bdxekdw6` FOREIGN KEY (`filetype_id`) REFERENCES `filetype` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extension_filetype`
--

LOCK TABLES `extension_filetype` WRITE;
/*!40000 ALTER TABLE `extension_filetype` DISABLE KEYS */;
INSERT INTO `extension_filetype` VALUES ('','cues','video-prasad-preservation'),('','hdr','video-prasad-preservation'),('','md5','video-prasad-uncompressed'),('\0','mkv','video-prasad-preservation'),('\0','mxf','video-prasad-uncompressed'),('','qcr','video-prasad-uncompressed');
/*!40000 ALTER TABLE `extension_filetype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `filetype`
--

DROP TABLE IF EXISTS `filetype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `filetype` (
  `id` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `filetype`
--

LOCK TABLES `filetype` WRITE;
/*!40000 ALTER TABLE `filetype` DISABLE KEYS */;
INSERT INTO `filetype` VALUES ('video-prasad-preservation','Video Preservation Copy - Prasad MXF - MKV etc'),('video-prasad-uncompressed','Video Raw from Prasad Corp');
/*!40000 ALTER TABLE `filetype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flow`
--

DROP TABLE IF EXISTS `flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flow` (
  `id` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flow`
--

LOCK TABLES `flow` WRITE;
/*!40000 ALTER TABLE `flow` DISABLE KEYS */;
INSERT INTO `flow` VALUES ('video-prasad-flow','video compressed preservation file generation with extracted header and cues'),('video-prasad-raw-flow','extract header and footer from uncompressed source raw video');
/*!40000 ALTER TABLE `flow` ENABLE KEYS */;
UNLOCK TABLES;

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
  `flow_id` varchar(255) NOT NULL,
  `flow_ref_id` varchar(255) DEFAULT NULL,
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
INSERT INTO `flowelement` VALUES (21,'',NULL,21,'video-dgzn-checksum-verify',NULL,'video-prasad-raw-flow',NULL),(22,'','[21]',22,'video-header-footer-extraction',NULL,'video-prasad-raw-flow',NULL),(23,'','[22]',23,NULL,NULL,'video-prasad-raw-flow','archive-flow'),(24,'','[21]',24,'video-preservation-gen',NULL,'video-prasad-flow',NULL),(25,'','[24]',25,NULL,NULL,'video-prasad-flow','archive-flow'),(26,'','[24]',26,NULL,NULL,'video-prasad-flow','video-proxy-flow');
/*!40000 ALTER TABLE `flowelement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `processingtask`
--

DROP TABLE IF EXISTS `processingtask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `processingtask` (
  `id` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `filetype_id` varchar(255) DEFAULT NULL,
  `max_errors` int(11) DEFAULT NULL,
  `output_artifactclass_suffix` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `processingtask`
--

LOCK TABLES `processingtask` WRITE;
/*!40000 ALTER TABLE `processingtask` DISABLE KEYS */;
INSERT INTO `processingtask` VALUES ('video-dgzn-checksum-verify','impl specific to dig only. Henc dig in the Name of the task. verifies if checksum supplied in the MD5 file matches with the MD5 of the video file','video-prasad-uncompressed',NULL,NULL),('video-header-footer-extraction','extracts header and footer from raw source uncompressed video','video-prasad-uncompressed',10,'-hdr-ftr'),('video-preservation-gen','generates compressed ffv1 video files (with header and cues)','video-prasad-uncompressed',10,'-preservation');
/*!40000 ALTER TABLE `processingtask` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence` (
  `id` varchar(255) NOT NULL,
  `code_regex` varchar(255) DEFAULT NULL,
  `current_number` int(11) DEFAULT NULL,
  `ending_number` int(11) DEFAULT NULL,
  `force_match` bit(1) DEFAULT NULL,
  `group` bit(1) DEFAULT NULL,
  `keep_code` bit(1) DEFAULT NULL,
  `number_regex` varchar(255) DEFAULT NULL,
  `prefix` varchar(255) DEFAULT NULL,
  `starting_number` int(11) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `sequence_ref_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5v8kpxq28gqfbgrsmaermgfrc` (`sequence_ref_id`),
  CONSTRAINT `FK5v8kpxq28gqfbgrsmaermgfrc` FOREIGN KEY (`sequence_ref_id`) REFERENCES `sequence` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sequence`
--

LOCK TABLES `sequence` WRITE;
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
INSERT INTO `sequence` VALUES ('prasad-pub','^\\d+(?=_)',NULL,NULL,'\0','\0','\0','^\\d+(?=_)','P',NULL,'artifact','video-grp'),('prasad-pub-hdr-ftr','^P\\d+(?=_)',NULL,NULL,'','\0','\0','(?<=^P)\\d+(?=_)','PH',NULL,'artifact',NULL),('prasad-pub-preservation','^P\\d+(?=_)',NULL,NULL,'','\0','\0','(?<=^P)\\d+(?=_)','PP',NULL,'artifact',NULL),('prasad-pub-proxy-low','^PP\\d+(?=_)',NULL,NULL,'','\0','\0','(?<=PP)\\d+(?=_)','PL',NULL,'artifact',NULL);
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-11-15 15:15:50
