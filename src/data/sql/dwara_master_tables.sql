-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dwara_v2_fromtest
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
-- Table structure for table `action`
--

DROP TABLE IF EXISTS `action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action` (
  `id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action`
--

LOCK TABLES `action` WRITE;
/*!40000 ALTER TABLE `action` DISABLE KEYS */;
/*!40000 ALTER TABLE `action` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `action_user`
--

DROP TABLE IF EXISTS `action_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action_user` (
  `permission_level` int(11) DEFAULT NULL,
  `action_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`action_id`,`user_id`),
  KEY `FK6bb095i1f07tksvwtiya0ckmp` (`user_id`),
  CONSTRAINT `FK6bb095i1f07tksvwtiya0ckmp` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKh6bm2brktaltqqbj4nm6ar1uw` FOREIGN KEY (`action_id`) REFERENCES `action` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action_user`
--

LOCK TABLES `action_user` WRITE;
/*!40000 ALTER TABLE `action_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `action_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `application`
--

DROP TABLE IF EXISTS `application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `application` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `application`
--

LOCK TABLES `application` WRITE;
/*!40000 ALTER TABLE `application` DISABLE KEYS */;
INSERT INTO `application` VALUES (1001,'MAM');
/*!40000 ALTER TABLE `application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `error`
--

DROP TABLE IF EXISTS `error`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `error` (
  `id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `error`
--

LOCK TABLES `error` WRITE;
/*!40000 ALTER TABLE `error` DISABLE KEYS */;
/*!40000 ALTER TABLE `error` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `extension`
--

DROP TABLE IF EXISTS `extension`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `extension` (
  `id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extension`
--

LOCK TABLES `extension` WRITE;
/*!40000 ALTER TABLE `extension` DISABLE KEYS */;
INSERT INTO `extension` VALUES (3001,'Some MP4 description','MP4'),(3002,'Some MOV description','MOV'),(3003,'Some MTS description','MTS'),(3004,'d','JPG'),(3005,'d','TIF'),(3006,'d','NEF'),(3007,'d','XMP'),(3008,'test','MP4_FFPROBE_OUT');
/*!40000 ALTER TABLE `extension` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `extension_taskfiletype`
--

DROP TABLE IF EXISTS `extension_taskfiletype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `extension_taskfiletype` (
  `sidecar` bit(1) DEFAULT NULL,
  `extension_id` int(11) NOT NULL,
  `taskfiletype_id` int(11) NOT NULL,
  PRIMARY KEY (`extension_id`,`taskfiletype_id`),
  KEY `FKf58juk4evbosj8b5410yel2rc` (`taskfiletype_id`),
  CONSTRAINT `FK3f9m8l6s81amps2vho91mi2bk` FOREIGN KEY (`extension_id`) REFERENCES `extension` (`id`),
  CONSTRAINT `FKf58juk4evbosj8b5410yel2rc` FOREIGN KEY (`taskfiletype_id`) REFERENCES `taskfiletype` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extension_taskfiletype`
--

LOCK TABLES `extension_taskfiletype` WRITE;
/*!40000 ALTER TABLE `extension_taskfiletype` DISABLE KEYS */;
INSERT INTO `extension_taskfiletype` VALUES ('\0',3001,4001),('\0',3001,4004),('\0',3002,4001),('\0',3003,4001),('',3004,4001),('\0',3004,4003),('',3004,4004),('\0',3005,4003),('\0',3006,4003),('',3007,4003),('',3008,4001),('',3008,4004);
/*!40000 ALTER TABLE `extension_taskfiletype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `extension_taskfiletype_view`
--

DROP TABLE IF EXISTS `extension_taskfiletype_view`;
/*!50001 DROP VIEW IF EXISTS `extension_taskfiletype_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `extension_taskfiletype_view` AS SELECT 
 1 AS `taskfiletype_name`,
 1 AS `extension_name`,
 1 AS `sidecar`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` VALUES (1),(1),(1),(1),(1),(1),(1);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `libraryclass`
--

DROP TABLE IF EXISTS `libraryclass`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `libraryclass` (
  `id` int(11) NOT NULL,
  `concurrent_copies` bit(1) DEFAULT NULL,
  `display_order` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `path_prefix` varchar(255) DEFAULT NULL,
  `source` bit(1) DEFAULT NULL,
  `taskfiletype_id` int(11) DEFAULT NULL,
  `sequence_id` int(11) DEFAULT NULL,
  `generator_task_id` int(11) DEFAULT NULL,
  `taskset_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn5fr92avsm6pmbimp0ncrpp3r` (`generator_task_id`),
  KEY `FKqt6xlxpgp8emoqm5ur9on83hb` (`sequence_id`),
  KEY `FK195mnrphn1n5xob4axm60ulbo` (`taskfiletype_id`),
  KEY `FKe3httal1gv9tdr9de3ov670oi` (`taskset_id`),
  CONSTRAINT `FK195mnrphn1n5xob4axm60ulbo` FOREIGN KEY (`taskfiletype_id`) REFERENCES `taskfiletype` (`id`),
  CONSTRAINT `FKe3httal1gv9tdr9de3ov670oi` FOREIGN KEY (`taskset_id`) REFERENCES `taskset` (`id`),
  CONSTRAINT `FKn5fr92avsm6pmbimp0ncrpp3r` FOREIGN KEY (`generator_task_id`) REFERENCES `task` (`id`),
  CONSTRAINT `FKqt6xlxpgp8emoqm5ur9on83hb` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `libraryclass`
--

LOCK TABLES `libraryclass` WRITE;
/*!40000 ALTER TABLE `libraryclass` DISABLE KEYS */;
INSERT INTO `libraryclass` VALUES (5001,'\0',0,'pub-video','/data/ingested','',4001,9001,NULL,19001),(5002,'\0',0,'pub-audio','/data/ingested','',4002,9002,NULL,19001),(5003,'\0',0,'private1-video','/data/ingested','',4001,0,NULL,19001),(5004,'\0',0,'Private2-Video','/data/ingested','',4001,0,NULL,19001),(5005,'\0',0,'Private3-Video','/data/ingested','',4001,0,NULL,19001),(5006,'\0',0,'Private1-Audio','/data/ingested','',4002,0,NULL,19001),(5007,'\0',0,'PreviewProxy-Video','/data/transcoded','\0',4004,9007,18004,19001),(5008,'\0',0,'MezzanineProxy-Video','/data/transcoded','\0',0,9008,18017,19001),(5009,'\0',0,'Private(ALL)PreviewProxy-Video - common private libclass for all ???','/data/transcoded','\0',0,9007,NULL,19001),(5010,'\0',0,'Private(ALL)MezzanineProxy-Video','/data/transcoded','\0',0,9008,NULL,19001),(5011,'\0',0,'Private(ALL)PreviewProxy-Audio','/data/transcoded','\0',0,9007,NULL,19001),(5012,'\0',0,'Private(ALL)MezzanineProxy-Audio','/data/transcoded','\0',0,9008,NULL,19001),(5013,'\0',0,'Private2PreviewProxy-Video','/data/transcoded','\0',0,9007,NULL,19001);
/*!40000 ALTER TABLE `libraryclass` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `libraryclass_action_user`
--

DROP TABLE IF EXISTS `libraryclass_action_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `libraryclass_action_user` (
  `action_id` int(11) NOT NULL,
  `libraryclass_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`action_id`,`libraryclass_id`,`user_id`),
  KEY `FK2for7rk58kfy0x57i91a7uprf` (`libraryclass_id`),
  KEY `FKshel16obo8si3exbclp4hox4g` (`user_id`),
  CONSTRAINT `FK2for7rk58kfy0x57i91a7uprf` FOREIGN KEY (`libraryclass_id`) REFERENCES `libraryclass` (`id`),
  CONSTRAINT `FKshel16obo8si3exbclp4hox4g` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKsl1653ubpv26e4mv5j5d81ak5` FOREIGN KEY (`action_id`) REFERENCES `action` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `libraryclass_action_user`
--

LOCK TABLES `libraryclass_action_user` WRITE;
/*!40000 ALTER TABLE `libraryclass_action_user` DISABLE KEYS */;
INSERT INTO `libraryclass_action_user` VALUES (5001,8001,21001);
/*!40000 ALTER TABLE `libraryclass_action_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `libraryclass_property`
--

DROP TABLE IF EXISTS `libraryclass_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `libraryclass_property` (
  `optional` bit(1) DEFAULT NULL,
  `position` int(11) DEFAULT NULL,
  `libraryclass_id` int(11) NOT NULL,
  `property_id` int(11) NOT NULL,
  PRIMARY KEY (`libraryclass_id`,`property_id`),
  KEY `FKed9u1lntsjddsnf3wdkatd8pu` (`property_id`),
  CONSTRAINT `FKed9u1lntsjddsnf3wdkatd8pu` FOREIGN KEY (`property_id`) REFERENCES `property` (`id`),
  CONSTRAINT `FKlr8xbfgqw5o6oantcj3ggu2xc` FOREIGN KEY (`libraryclass_id`) REFERENCES `libraryclass` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `libraryclass_property`
--

LOCK TABLES `libraryclass_property` WRITE;
/*!40000 ALTER TABLE `libraryclass_property` DISABLE KEYS */;
INSERT INTO `libraryclass_property` VALUES ('\0',1,5001,7001),('',2,5001,7002),('\0',3,5001,7003),('\0',4,5001,7004),('',5,5001,7005),('\0',5,5001,7006),('\0',1,5002,7001),('',2,5002,7002),('\0',3,5002,7003),('\0',4,5002,7004);
/*!40000 ALTER TABLE `libraryclass_property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `libraryclass_tapeset`
--

DROP TABLE IF EXISTS `libraryclass_tapeset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `libraryclass_tapeset` (
  `encrypted` bit(1) DEFAULT NULL,
  `libraryclass_id` int(11) NOT NULL,
  `tapeset_id` int(11) NOT NULL,
  `task_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`libraryclass_id`,`tapeset_id`),
  KEY `FKb8t9f6bdsm0kgij5ggiq61r98` (`tapeset_id`),
  KEY `FKix06si8be8poyrdyush2qh8x3` (`task_id`),
  CONSTRAINT `FK8u6cdqoge4t4v2gqifrnxj2b9` FOREIGN KEY (`libraryclass_id`) REFERENCES `libraryclass` (`id`),
  CONSTRAINT `FKb8t9f6bdsm0kgij5ggiq61r98` FOREIGN KEY (`tapeset_id`) REFERENCES `tapeset` (`id`),
  CONSTRAINT `FKix06si8be8poyrdyush2qh8x3` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `libraryclass_tapeset`
--

LOCK TABLES `libraryclass_tapeset` WRITE;
/*!40000 ALTER TABLE `libraryclass_tapeset` DISABLE KEYS */;
INSERT INTO `libraryclass_tapeset` VALUES ('\0',5001,15001,18001),('\0',5001,15002,18002),('\0',5001,15003,18003),('\0',5007,15010,18006);
/*!40000 ALTER TABLE `libraryclass_tapeset` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `libraryclass_targetvolume`
--

DROP TABLE IF EXISTS `libraryclass_targetvolume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `libraryclass_targetvolume` (
  `libraryclass_id` int(11) NOT NULL,
  `targetvolume_id` int(11) NOT NULL,
  PRIMARY KEY (`libraryclass_id`,`targetvolume_id`),
  KEY `FKifo8by7466mt4gefy391l4xi9` (`targetvolume_id`),
  CONSTRAINT `FKgfnmk13674und4okec2nd2d1j` FOREIGN KEY (`libraryclass_id`) REFERENCES `libraryclass` (`id`),
  CONSTRAINT `FKifo8by7466mt4gefy391l4xi9` FOREIGN KEY (`targetvolume_id`) REFERENCES `targetvolume` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `libraryclass_targetvolume`
--

LOCK TABLES `libraryclass_targetvolume` WRITE;
/*!40000 ALTER TABLE `libraryclass_targetvolume` DISABLE KEYS */;
INSERT INTO `libraryclass_targetvolume` VALUES (5001,17001);
/*!40000 ALTER TABLE `libraryclass_targetvolume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `priorityband`
--

DROP TABLE IF EXISTS `priorityband`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `priorityband` (
  `id` int(11) NOT NULL,
  `end` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `optimize_tape_access` bit(1) DEFAULT NULL,
  `start` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `priorityband`
--

LOCK TABLES `priorityband` WRITE;
/*!40000 ALTER TABLE `priorityband` DISABLE KEYS */;
INSERT INTO `priorityband` VALUES (6001,10,'Band 1','',5);
/*!40000 ALTER TABLE `priorityband` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property`
--

DROP TABLE IF EXISTS `property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `property` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `regex` varchar(255) DEFAULT NULL,
  `replace_char_space` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property`
--

LOCK TABLES `property` WRITE;
/*!40000 ALTER TABLE `property` DISABLE KEYS */;
INSERT INTO `property` VALUES (7001,'event','[a-zA-Z0-9-]+','_'),(7002,'event2','[a-zA-Z0-9-]+','_'),(7003,'location','[a-zA-Z0-9-]+','_'),(7004,'date','(?:0[1-9]|[12][0-9]|3[01])-(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec])-(?:19|20)\\d\\d)','_'),(7005,'deviceinfo1','[a-zA-Z0-9-]+','_'),(7006,'deviceinfo2','[a-zA-Z0-9-]+','_');
/*!40000 ALTER TABLE `property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence` (
  `id` int(11) NOT NULL,
  `extraction_regex` varchar(255) DEFAULT NULL,
  `keep_extracted_code` bit(1) DEFAULT NULL,
  `last_number` int(11) DEFAULT NULL,
  `prefix` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sequence`
--

LOCK TABLES `sequence` WRITE;
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
INSERT INTO `sequence` VALUES (9001,NULL,'\0',24533,NULL),(9002,'^[A-Z]{1,2}[\\\\d]{1,4}','\0',999,'A'),(9003,NULL,'\0',555,'Z'),(9004,'^[A-Z]{1,2}[\\\\d]{1,4}','\0',333,NULL),(9005,'^[\\\\d]{1,5}','',0,NULL),(9006,'^[Z\\\\d]{1,6}','',0,NULL),(9007,NULL,'\0',0,'L'),(9008,NULL,'\0',0,'M');
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `status`
--

DROP TABLE IF EXISTS `status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `status` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status`
--

LOCK TABLES `status` WRITE;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
INSERT INTO `status` VALUES (10001,'queued'),(10002,'in_progress'),(10003,'completed '),(10004,'partially_completed'),(10005,'completed_with_failures'),(10006,'on_hold'),(10007,'skipped'),(10008,'cancelled'),(10009,'aborted'),(10010,'failed'),(10011,'marked_completed');
/*!40000 ALTER TABLE `status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storageformat`
--

DROP TABLE IF EXISTS `storageformat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storageformat` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storageformat`
--

LOCK TABLES `storageformat` WRITE;
/*!40000 ALTER TABLE `storageformat` DISABLE KEYS */;
INSERT INTO `storageformat` VALUES (11001,'BRU'),(11002,'ZIP'),(11003,'LTFS');
/*!40000 ALTER TABLE `storageformat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tape`
--

DROP TABLE IF EXISTS `tape`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tape` (
  `id` int(11) NOT NULL,
  `barcode` varchar(255) DEFAULT NULL,
  `blocksize` int(11) DEFAULT NULL,
  `finalized` bit(1) DEFAULT NULL,
  `tapeset_id` int(11) DEFAULT NULL,
  `tapetype_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7ownyqvia57xxb9beuq4aas23` (`tapeset_id`),
  KEY `FKqx75p35kbopxlxdrvbjj504kk` (`tapetype_id`),
  CONSTRAINT `FK7ownyqvia57xxb9beuq4aas23` FOREIGN KEY (`tapeset_id`) REFERENCES `tapeset` (`id`),
  CONSTRAINT `FKqx75p35kbopxlxdrvbjj504kk` FOREIGN KEY (`tapetype_id`) REFERENCES `tapetype` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tape`
--

LOCK TABLES `tape` WRITE;
/*!40000 ALTER TABLE `tape` DISABLE KEYS */;
INSERT INTO `tape` VALUES (12001,'V5A001',256000,'',15001,16002),(12002,'V5A003',256000,'\0',15001,16002),(12003,'V5B003',256000,'\0',15002,16002),(12004,'V5C003',256000,'\0',15003,16002),(12005,'UA001',256000,'\0',15004,16002),(12006,'UB001',256000,'\0',15005,16002),(12007,'UC001',256000,'\0',15006,16002),(12008,'PVA001',256000,'\0',15007,16002),(12009,'VLA003',256000,'\0',15010,16002),(12010,'VMA001',256000,'\0',15012,16002);
/*!40000 ALTER TABLE `tape` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tapedrive`
--

DROP TABLE IF EXISTS `tapedrive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tapedrive` (
  `id` int(11) NOT NULL,
  `device_wwid` varchar(255) DEFAULT NULL,
  `element_address` int(11) DEFAULT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `drive_status` varchar(255) DEFAULT NULL,
  `tape_id` int(11) DEFAULT NULL,
  `tapelibrary_id` int(11) DEFAULT NULL,
  `job_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_pa9lyfv1barxj480rk2t6ko2h` (`device_wwid`),
  UNIQUE KEY `UK_p6fsras5m76fle6fv7m6b9mc5` (`element_address`),
  UNIQUE KEY `UK_5s7ejy9d7byhyb9sdtf52acv9` (`serial_number`),
  KEY `FK273ysg7la64bkln9nei58lj73` (`tape_id`),
  KEY `FKkccsth8ndqf87jm5ygh9nl7px` (`tapelibrary_id`),
  KEY `FK7tctjfbctcy5a21gk7gjixljv` (`job_id`),
  CONSTRAINT `FK273ysg7la64bkln9nei58lj73` FOREIGN KEY (`tape_id`) REFERENCES `tape` (`id`),
  CONSTRAINT `FK7tctjfbctcy5a21gk7gjixljv` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`),
  CONSTRAINT `FKkccsth8ndqf87jm5ygh9nl7px` FOREIGN KEY (`tapelibrary_id`) REFERENCES `tapelibrary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tapedrive`
--

LOCK TABLES `tapedrive` WRITE;
/*!40000 ALTER TABLE `tapedrive` DISABLE KEYS */;
INSERT INTO `tapedrive` VALUES (13001,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst',0,'1497199456','AVAILABLE',12009,14001,265),(13002,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst',1,'1684087499','AVAILABLE',12003,14001,261),(13003,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1970448833-nst',2,'1970448833','AVAILABLE',12004,14001,262);
/*!40000 ALTER TABLE `tapedrive` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tapelibrary`
--

DROP TABLE IF EXISTS `tapelibrary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tapelibrary` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `slots` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tapelibrary`
--

LOCK TABLES `tapelibrary` WRITE;
/*!40000 ALTER TABLE `tapelibrary` DISABLE KEYS */;
INSERT INTO `tapelibrary` VALUES (14001,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400',24);
/*!40000 ALTER TABLE `tapelibrary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tapeset`
--

DROP TABLE IF EXISTS `tapeset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tapeset` (
  `id` int(11) NOT NULL,
  `barcode_prefix` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `storageformat_id` int(11) DEFAULT NULL,
  `copy_number` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9wvcfebto423b5840gwjru09e` (`storageformat_id`),
  CONSTRAINT `FK9wvcfebto423b5840gwjru09e` FOREIGN KEY (`storageformat_id`) REFERENCES `storageformat` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tapeset`
--

LOCK TABLES `tapeset` WRITE;
/*!40000 ALTER TABLE `tapeset` DISABLE KEYS */;
INSERT INTO `tapeset` VALUES (15001,'V5A','V5 Copy1 Tape pool with Bru format',11001,1),(15002,'V5B','V5 Copy2 Tape pool with Zip format',11001,2),(15003,'V5C','V5 Copy3 Tape pool with LTFS format',11001,3),(15004,'UA','U Copy1 Tape pool with Bru format',11001,1),(15005,'UB','U Copy2 Tape pool with Zip format',11002,2),(15006,'UC','U Copy3 Tape pool with LTFS format',11003,3),(15007,'PVA','PV Copy1 Tape pool with Bru format',11001,1),(15008,'PVB','PV Copy2 Tape pool with Zip format',11002,2),(15009,'PVC','PV Copy3 Tape pool with LTFS format',11003,3),(15010,'VLA','V Prev proxy Copy1 Tape pool',11001,1),(15012,'VMA','V Mezz proxy Copy1 Tape pool',11001,1);
/*!40000 ALTER TABLE `tapeset` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tapetype`
--

DROP TABLE IF EXISTS `tapetype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tapetype` (
  `id` int(11) NOT NULL,
  `capacity` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tapetype`
--

LOCK TABLES `tapetype` WRITE;
/*!40000 ALTER TABLE `tapetype` DISABLE KEYS */;
INSERT INTO `tapetype` VALUES (16001,'60000000000','LTO 6'),(16002,'70000000000','LTO 7');
/*!40000 ALTER TABLE `tapetype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `targetvolume`
--

DROP TABLE IF EXISTS `targetvolume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `targetvolume` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `targetvolume`
--

LOCK TABLES `targetvolume` WRITE;
/*!40000 ALTER TABLE `targetvolume` DISABLE KEYS */;
INSERT INTO `targetvolume` VALUES (17001,'Some Name','/data/RESTORED');
/*!40000 ALTER TABLE `targetvolume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task` (
  `id` int(11) NOT NULL,
  `max_errors` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `taskfiletype_id` int(11) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `application_id` int(11) DEFAULT NULL,
  `storage` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8pfqcr004sgufca7fmjex73r5` (`taskfiletype_id`),
  KEY `FKkj95at7t3jbjg0nng1bw44asr` (`application_id`),
  CONSTRAINT `FK8pfqcr004sgufca7fmjex73r5` FOREIGN KEY (`taskfiletype_id`) REFERENCES `taskfiletype` (`id`),
  CONSTRAINT `FKkj95at7t3jbjg0nng1bw44asr` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task`
--

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
INSERT INTO `task` VALUES (18001,0,'V5 Raw Copy 1',NULL,NULL,NULL,NULL),(18002,0,'V5 Raw Copy 2',NULL,NULL,NULL,NULL),(18003,0,'V5 Raw Copy 3',NULL,NULL,NULL,NULL),(18004,0,'video_low_resolution_transcoding',4001,NULL,NULL,NULL),(18005,20,'mam_update',4001,NULL,1001,NULL),(18006,0,'Preview Proxy Copy 1',NULL,NULL,NULL,NULL),(18007,0,'Mezz Proxy Copy 1',NULL,NULL,NULL,NULL),(18008,0,'U Raw Copy 1',NULL,NULL,NULL,NULL),(18009,0,'U Raw Copy 2',NULL,NULL,NULL,NULL),(18010,0,'audio_transcoding',4002,NULL,NULL,NULL),(18011,0,'copy_to_transcript_server',4002,NULL,NULL,NULL),(18012,0,'Restore',NULL,NULL,NULL,NULL),(18013,0,'P2 Raw Copy 1',NULL,NULL,NULL,NULL),(18014,0,'P2 Raw Copy 2 Encrypted',NULL,NULL,NULL,NULL),(18015,0,'P2 Preview Proxy Copy 1',NULL,NULL,NULL,NULL),(18016,0,'P2 Preview Proxy Copy 2 Encrypted',NULL,NULL,NULL,NULL),(18017,0,'video_medium_resolution_transcoding',4001,NULL,NULL,NULL);
/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_taskset`
--

DROP TABLE IF EXISTS `task_taskset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task_taskset` (
  `task_id` int(11) NOT NULL,
  `taskset_id` int(11) NOT NULL,
  `pre_task_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`task_id`,`taskset_id`),
  KEY `FKaf1knt9osuouyecla1w4q12tg` (`pre_task_id`),
  KEY `FKiix4r1iq1lmaq28oledm2txx7` (`taskset_id`),
  CONSTRAINT `FKaf1knt9osuouyecla1w4q12tg` FOREIGN KEY (`pre_task_id`) REFERENCES `task` (`id`),
  CONSTRAINT `FKb2k2drxyrnnopsaaxl976vh2k` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`),
  CONSTRAINT `FKiix4r1iq1lmaq28oledm2txx7` FOREIGN KEY (`taskset_id`) REFERENCES `taskset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_taskset`
--

LOCK TABLES `task_taskset` WRITE;
/*!40000 ALTER TABLE `task_taskset` DISABLE KEYS */;
INSERT INTO `task_taskset` VALUES (18001,19001,NULL),(18002,19001,NULL),(18003,19001,NULL),(18004,19001,NULL),(18005,19001,18004),(18006,19001,18004);
/*!40000 ALTER TABLE `task_taskset` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `taskfiletype`
--

DROP TABLE IF EXISTS `taskfiletype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `taskfiletype` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taskfiletype`
--

LOCK TABLES `taskfiletype` WRITE;
/*!40000 ALTER TABLE `taskfiletype` DISABLE KEYS */;
INSERT INTO `taskfiletype` VALUES (4001,'Video'),(4002,'Audio'),(4003,'Photo'),(4004,'PREV_PROXY'),(4005,'MEZZ_PROXY');
/*!40000 ALTER TABLE `taskfiletype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `taskset`
--

DROP TABLE IF EXISTS `taskset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `taskset` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taskset`
--

LOCK TABLES `taskset` WRITE;
/*!40000 ALTER TABLE `taskset` DISABLE KEYS */;
INSERT INTO `taskset` VALUES (19001,'Video ingest workflow'),(19002,'Audio ingest workflow'),(19003,'Private2 Video ingest workflow');
/*!40000 ALTER TABLE `taskset` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `hash` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `priorityband_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gj2fy3dcix7ph7k8684gka40c` (`name`),
  KEY `FK73fruhbqpgrll696tv7y2ygxh` (`priorityband_id`),
  CONSTRAINT `FK73fruhbqpgrll696tv7y2ygxh` FOREIGN KEY (`priorityband_id`) REFERENCES `priorityband` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (21001,'$2a$10$70nZ.1zvmmgAXQZ5qDFHxe08eTijEejJ5HRZAtwRcPuMjw4MfRley','pgurumurthy',6001);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_restore_file`
--

DROP TABLE IF EXISTS `v_restore_file`;
/*!50001 DROP VIEW IF EXISTS `v_restore_file`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `v_restore_file` AS SELECT 
 1 AS `tape_id`,
 1 AS `tapeset_id`,
 1 AS `file_id`,
 1 AS `library_id`,
 1 AS `targetvolume_id`,
 1 AS `requesttype_id`,
 1 AS `user_id`,
 1 AS `libraryclass_id`,
 1 AS `libraryclass_name`,
 1 AS `file_pathname`,
 1 AS `file_size`,
 1 AS `file_crc`,
 1 AS `tape_barcode`,
 1 AS `tape_blocksize`,
 1 AS `tape_finalized`,
 1 AS `tapeset_copy_number`,
 1 AS `storageformat_id`,
 1 AS `storageformat_name`,
 1 AS `file_tape_block`,
 1 AS `file_tape_offset`,
 1 AS `file_tape_encrypted`,
 1 AS `file_tape_deleted`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `extension_taskfiletype_view`
--

/*!50001 DROP VIEW IF EXISTS `extension_taskfiletype_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `extension_taskfiletype_view` AS select `taskfiletype`.`name` AS `taskfiletype_name`,`extension`.`name` AS `extension_name`,`extension_taskfiletype`.`sidecar` AS `sidecar` from ((`extension_taskfiletype` join `taskfiletype` on((`taskfiletype`.`id` = `extension_taskfiletype`.`taskfiletype_id`))) join `extension` on((`extension`.`id` = `extension_taskfiletype`.`extension_id`))) order by `taskfiletype`.`id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_restore_file`
--

/*!50001 DROP VIEW IF EXISTS `v_restore_file`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_restore_file` AS select `tape`.`id` AS `tape_id`,`tapeset`.`id` AS `tapeset_id`,`file`.`id` AS `file_id`,`library`.`id` AS `library_id`,`libraryclass_targetvolume`.`targetvolume_id` AS `targetvolume_id`,`libraryclass_requesttype_user`.`requesttype_id` AS `requesttype_id`,`libraryclass_requesttype_user`.`user_id` AS `user_id`,`libraryclass`.`id` AS `libraryclass_id`,`libraryclass`.`name` AS `libraryclass_name`,`file`.`pathname` AS `file_pathname`,`file`.`size` AS `file_size`,`file`.`crc` AS `file_crc`,`tape`.`barcode` AS `tape_barcode`,`tape`.`blocksize` AS `tape_blocksize`,`tape`.`finalized` AS `tape_finalized`,`tapeset`.`copy_number` AS `tapeset_copy_number`,`storageformat`.`id` AS `storageformat_id`,`storageformat`.`name` AS `storageformat_name`,`file_tape`.`block` AS `file_tape_block`,`file_tape`.`offset` AS `file_tape_offset`,`file_tape`.`encrypted` AS `file_tape_encrypted`,`file_tape`.`deleted` AS `file_tape_deleted` from ((((((((`file_tape` join `tape` on((`file_tape`.`tape_id` = `tape`.`id`))) join `tapeset` on((`tape`.`tapeset_id` = `tapeset`.`id`))) join `file` on((`file`.`id` = `file_tape`.`file_id`))) join `library` on((`file`.`library_id` = `library`.`id`))) join `storageformat` on((`storageformat`.`id` = `tapeset`.`storageformat_id`))) join `libraryclass` on((`libraryclass`.`id` = `library`.`libraryclass_id`))) join `libraryclass_targetvolume` on((`libraryclass_targetvolume`.`libraryclass_id` = `library`.`libraryclass_id`))) join `libraryclass_requesttype_user` on((`libraryclass_requesttype_user`.`libraryclass_id` = `library`.`libraryclass_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-04-09 21:32:33
