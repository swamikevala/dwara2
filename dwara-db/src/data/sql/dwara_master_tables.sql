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
-- Table structure for table `action`
--

DROP TABLE IF EXISTS `action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action` (
  `id` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action`
--

LOCK TABLES `action` WRITE;
/*!40000 ALTER TABLE `action` DISABLE KEYS */;
INSERT INTO `action` VALUES ('abort',NULL,'sync'),('cancel',NULL,'sync'),('delete',NULL,'sync'),('diagnostics',NULL,'sync'),('finalize',NULL,'storage_task'),('hold',NULL,'sync'),('import',NULL,'storage_task'),('ingest',NULL,'complex'),('initialize',NULL,'storage_task'),('list',NULL,'sync'),('map_tapedrives',NULL,'storage_task'),('migrate',NULL,'storage_task'),('process',NULL,'complex'),('release',NULL,'sync'),('rename',NULL,'sync'),('restore',NULL,'storage_task'),('restore_process',NULL,'complex'),('rewrite',NULL,'storage_task'),('verify',NULL,'storage_task'),('write',NULL,'storage_task');
/*!40000 ALTER TABLE `action` ENABLE KEYS */;
UNLOCK TABLES;

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
INSERT INTO `action_artifactclass_flow` VALUES ('','ingest','video-pub','archive-flow'),('\0','ingest','video-pub','video-audio-flow'),('','ingest','video-pub','video-proxy-flow');
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
INSERT INTO `action_artifactclass_user` VALUES ('ingest','video-pub',1),('ingest','video-pub',2),('ingest','video-pub',3);
/*!40000 ALTER TABLE `action_artifactclass_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `archiveformat`
--

DROP TABLE IF EXISTS `archiveformat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `archiveformat` (
  `id` varchar(255) NOT NULL,
  `blocksize` int(11) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `filesize_increase_const` int(11) DEFAULT NULL,
  `filesize_increase_rate` float DEFAULT NULL,
  `restore_verify` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archiveformat`
--

LOCK TABLES `archiveformat` WRITE;
/*!40000 ALTER TABLE `archiveformat` DISABLE KEYS */;
INSERT INTO `archiveformat` VALUES ('bru',2048,'BRU TOlis',2048,0.125,'\0'),('tar',512,'Tar (posix)',NULL,NULL,'');
/*!40000 ALTER TABLE `archiveformat` ENABLE KEYS */;
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
INSERT INTO `artifactclass` VALUES ('video-pub','\0','',1,'1','\0','C:\\data\\staged','',NULL,'video-pub'),('video-pub-proxy-low','\0','',13,'1','\0','C:\\data\\transcoded','\0','video-pub','video-pub-proxy-low');
/*!40000 ALTER TABLE `artifactclass` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifactclass_destination`
--

DROP TABLE IF EXISTS `artifactclass_destination`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifactclass_destination` (
  `artifactclass_id` varchar(255) NOT NULL,
  `destination_id` varchar(255) NOT NULL,
  PRIMARY KEY (`artifactclass_id`,`destination_id`),
  KEY `FK90k9ovbcafjeemb32gwnr845u` (`destination_id`),
  CONSTRAINT `FK4djwomf3rwcpmsggge0379hs6` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`),
  CONSTRAINT `FK90k9ovbcafjeemb32gwnr845u` FOREIGN KEY (`destination_id`) REFERENCES `destination` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifactclass_destination`
--

LOCK TABLES `artifactclass_destination` WRITE;
/*!40000 ALTER TABLE `artifactclass_destination` DISABLE KEYS */;
/*!40000 ALTER TABLE `artifactclass_destination` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifactclass_processingtask`
--

DROP TABLE IF EXISTS `artifactclass_processingtask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifactclass_processingtask` (
  `pathname_regex` varchar(255) DEFAULT NULL,
  `artifactclass_id` varchar(255) NOT NULL,
  `processingtask_id` varchar(255) NOT NULL,
  PRIMARY KEY (`artifactclass_id`,`processingtask_id`),
  KEY `FK1q37r86vrlmw3369ulkvwq9fd` (`processingtask_id`),
  CONSTRAINT `FK1q37r86vrlmw3369ulkvwq9fd` FOREIGN KEY (`processingtask_id`) REFERENCES `processingtask` (`id`),
  CONSTRAINT `FK57rsmknm7l28y9wbbf2dcgms3` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifactclass_processingtask`
--

LOCK TABLES `artifactclass_processingtask` WRITE;
/*!40000 ALTER TABLE `artifactclass_processingtask` DISABLE KEYS */;
/*!40000 ALTER TABLE `artifactclass_processingtask` ENABLE KEYS */;
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
INSERT INTO `artifactclass_volume` VALUES ('','\0','video-pub','R1'),('','\0','video-pub','R2'),('\0','\0','video-pub','R3'),('','\0','video-pub-proxy-low','G1'),('','\0','video-pub-proxy-low','G2'),('\0','\0','video-pub-proxy-low','G3');
/*!40000 ALTER TABLE `artifactclass_volume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `copy`
--

DROP TABLE IF EXISTS `copy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `copy` (
  `id` int(11) NOT NULL,
  `location_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7v9890w0s8toh9n4a8tlvh9l4` (`location_id`),
  CONSTRAINT `FK7v9890w0s8toh9n4a8tlvh9l4` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `copy`
--

LOCK TABLES `copy` WRITE;
/*!40000 ALTER TABLE `copy` DISABLE KEYS */;
INSERT INTO `copy` VALUES (3,'india-offsite-1'),(1,'lto-room'),(4,'lto-room'),(2,'t-block');
/*!40000 ALTER TABLE `copy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `destination`
--

DROP TABLE IF EXISTS `destination`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `destination` (
  `id` varchar(255) NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  `use_buffering` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_jx1awo0uegy8wrvmdwtjojkbm` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `destination`
--

LOCK TABLES `destination` WRITE;
/*!40000 ALTER TABLE `destination` DISABLE KEYS */;
INSERT INTO `destination` VALUES ('local','/data/RESTORED','\0'),('san-test','/mnt/san/test','');
/*!40000 ALTER TABLE `destination` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `domain`
--

DROP TABLE IF EXISTS `domain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `domain` (
  `id` int(11) NOT NULL,
  `default` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ga2sqp4lboblqv6oks9oryd9q` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `domain`
--

LOCK TABLES `domain` WRITE;
/*!40000 ALTER TABLE `domain` DISABLE KEYS */;
INSERT INTO `domain` VALUES (1,'','main'),(2,'\0','dept-backup');
/*!40000 ALTER TABLE `domain` ENABLE KEYS */;
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
INSERT INTO `extension` VALUES ('',NULL,NULL),('8bf',NULL,NULL),('abc',NULL,NULL),('AppleDouble',NULL,NULL),('BIM',NULL,NULL),('CR2',NULL,NULL),('cues','Prasad - Extracted index from mkv','\0'),('DS_Store',NULL,NULL),('ftr','Prasad - Extract header from the source mxf','\0'),('hdr','Prasad - Extract header from both mxf and mkv','\0'),('jpg',NULL,NULL),('LRV',NULL,NULL),('md5','Prasad - md5 file','\0'),('mkv','Prasad - Metroska','\0'),('mov','','\0'),('mp3','','\0'),('mp4','','\0'),('mp4_ffprobe_out',NULL,NULL),('mxf','Prasad - raw uncompressed source file','\0'),('PPN',NULL,''),('prak',NULL,''),('qcr','Prasad - QC report file','\0'),('sav',NULL,NULL),('SMI',NULL,''),('THM',NULL,NULL),('txt',NULL,NULL),('wav','','\0'),('XML',NULL,NULL);
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
INSERT INTO `extension_filetype` VALUES ('','cues','video-prasad-preservation'),('','hdr','video-prasad-preservation'),('','jpg','video-proxy'),('','md5','video-prasad-uncompressed'),('\0','mkv','video'),('\0','mkv','video-prasad-preservation'),('\0','mov','video'),('\0','mp3','audio'),('\0','mp4','video'),('\0','mp4','video-proxy'),('','mp4_ffprobe_out','video-proxy'),('\0','mxf','video'),('\0','mxf','video-prasad-uncompressed'),('','qcr','video-prasad-uncompressed'),('\0','wav','audio');
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
INSERT INTO `filetype` VALUES ('audio','Audio Files'),('video','Video Files'),('video-prasad-preservation','Video Preservation Copy - Prasad MXF - MKV etc'),('video-prasad-uncompressed','Video Raw from Prasad Corp'),('video-proxy','Video proxies');
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
INSERT INTO `flow` VALUES ('archive-flow','cksum-gen, write, verify'),('video-proxy-flow','video transcoding, mam update, proxy archiving');
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
INSERT INTO `flowelement` VALUES (1,'',NULL,1,'checksum-gen',NULL,'archive-flow',NULL),(2,'',NULL,2,NULL,'write','archive-flow',NULL),(3,'','[1, 2]',3,NULL,'verify','archive-flow',NULL),(4,'',NULL,4,'video-proxy-low-gen',NULL,'video-proxy-flow',NULL),(5,'','[4]',5,'video-mam-update',NULL,'video-proxy-flow',NULL),(6,'','[4]',6,NULL,NULL,'video-proxy-flow','archive-flow');
/*!40000 ALTER TABLE `flowelement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location` (
  `id` varchar(255) NOT NULL,
  `default` bit(1) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_bvtps7leip9hi2pjp928b64bo` (`description`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
INSERT INTO `location` VALUES ('india-offsite-1','\0','India Offsite Location 1'),('lto-room','','IYC Archives LTO Room'),('t-block','\0','IYC Triangle Block');
/*!40000 ALTER TABLE `location` ENABLE KEYS */;
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ash8838axsg24ngytu9ktcu9` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `priorityband`
--

LOCK TABLES `priorityband` WRITE;
/*!40000 ALTER TABLE `priorityband` DISABLE KEYS */;
INSERT INTO `priorityband` VALUES (1,10,'','',5);
/*!40000 ALTER TABLE `priorityband` ENABLE KEYS */;
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
INSERT INTO `processingtask` VALUES ('checksum-gen','generate sha256 file checksums and update db','_all_',0,NULL),('video-mam-update','move proxy files to mam server and add xml metadata to mam','video-proxy',0,NULL),('video-proxy-low-gen','generate low resolution video proxies (with thumbnail and metadata xml)','video',10,'-proxy-low');
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
INSERT INTO `sequence` VALUES ('generated-1',NULL,10500,19999,'\0','\0','\0',NULL,'G',10001,'volume',NULL),('generated-2',NULL,20500,29999,'\0','\0','\0',NULL,'G',20001,'volume',NULL),('generated-3',NULL,30500,39999,'\0','\0','\0',NULL,'G',30001,'volume',NULL),('original-1',NULL,10500,19999,'\0','\0','\0',NULL,'R',10001,'volume',NULL),('original-2',NULL,20500,29999,'\0','\0','\0',NULL,'R',20001,'volume',NULL),('original-3',NULL,30500,39999,'\0','\0','\0',NULL,'R',30001,'volume',NULL),('video-grp',NULL,22276,-1,'\0','','\0',NULL,NULL,1,'artifact',NULL),('video-pub','^\\d+(?=_)',NULL,NULL,'\0','\0','\0','^\\d+(?=_)','V',NULL,'artifact','video-grp'),('video-pub-proxy-low','^V\\d+(?=_)',NULL,NULL,'','\0','\0','(?<=V)\\d+(?=_)','VL',NULL,'artifact',NULL);
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
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
INSERT INTO `user` VALUES (1,NULL,'$2a$10$dK7sU4vBShzsD6UcPXGxcuBfMYf6Ljm9MuQG0Iqx0uAEcfXYtJpiO','swamikevala',1),(2,NULL,'$2a$10$70nZ.1zvmmgAXQZ5qDFHxe08eTijEejJ5HRZAtwRcPuMjw4MfRley','pgurumurthy',1),(3,NULL,'$2a$10$XzZL/LTESpJ2L7.LTWL3.enor29Unjqsshvgb.OjdO0zhbQpSV6zC','maajeevapushpa',1),(4,NULL,NULL,'dwara',1);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `version`
--

DROP TABLE IF EXISTS `version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `version` (
  `version` varchar(255) NOT NULL,
  PRIMARY KEY (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `version`
--

LOCK TABLES `version` WRITE;
/*!40000 ALTER TABLE `version` DISABLE KEYS */;
INSERT INTO `version` VALUES ('2.0.2');
/*!40000 ALTER TABLE `version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `volume`
--

DROP TABLE IF EXISTS `volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `volume` (
  `id` varchar(255) NOT NULL,
  `capacity` bigint(20) DEFAULT NULL,
  `checksumtype` varchar(255) DEFAULT NULL,
  `defective` bit(1) DEFAULT NULL,
  `details` json DEFAULT NULL,
  `finalized` bit(1) DEFAULT NULL,
  `imported` bit(1) DEFAULT NULL,
  `initialized_at` datetime(6) DEFAULT NULL,
  `storagelevel` varchar(255) DEFAULT NULL,
  `storagesubtype` varchar(255) DEFAULT NULL,
  `storagetype` varchar(255) DEFAULT NULL,
  `suspect` bit(1) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `archiveformat_id` varchar(255) DEFAULT NULL,
  `copy_id` int(11) DEFAULT NULL,
  `group_ref_id` varchar(255) DEFAULT NULL,
  `location_id` varchar(255) DEFAULT NULL,
  `sequence_id` varchar(255) DEFAULT NULL,
  `uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsw7cga5kgm5yqs2sfpq9hdidv` (`archiveformat_id`),
  KEY `FK8teoqqr29pkmx2kde364jhwms` (`copy_id`),
  KEY `FK571cos3ontc2q3bc72h4ns8gp` (`group_ref_id`),
  KEY `FK5k6g9ueuvb8e330dfvr88agfk` (`location_id`),
  KEY `FK94srhb48x080eknhc0yx0ad2o` (`sequence_id`),
  CONSTRAINT `FK571cos3ontc2q3bc72h4ns8gp` FOREIGN KEY (`group_ref_id`) REFERENCES `volume` (`id`),
  CONSTRAINT `FK5k6g9ueuvb8e330dfvr88agfk` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK8teoqqr29pkmx2kde364jhwms` FOREIGN KEY (`copy_id`) REFERENCES `copy` (`id`),
  CONSTRAINT `FK94srhb48x080eknhc0yx0ad2o` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`),
  CONSTRAINT `FKsw7cga5kgm5yqs2sfpq9hdidv` FOREIGN KEY (`archiveformat_id`) REFERENCES `archiveformat` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `volume`
--

LOCK TABLES `volume` WRITE;
/*!40000 ALTER TABLE `volume` DISABLE KEYS */;
INSERT INTO `volume` VALUES ('G1',NULL,'sha256','\0','{\"blocksize\": 262144}','\0','\0',NULL,'block',NULL,'tape','\0','group','tar',1,NULL,NULL,'generated-1',NULL),('G2',NULL,'sha256','\0','{\"blocksize\": 262144}','\0','\0',NULL,'block',NULL,'tape','\0','group','bru',2,NULL,NULL,'generated-2',NULL),('G3',NULL,'sha256','\0','{\"blocksize\": 262144}','\0','\0',NULL,'block',NULL,'tape','\0','group','tar',3,NULL,NULL,'generated-3',NULL),('R1',NULL,'sha256','\0','{\"blocksize\": 262144}','\0','\0',NULL,'block',NULL,'tape','\0','group','tar',1,NULL,NULL,'original-1',NULL),('R2',NULL,'sha256','\0','{\"blocksize\": 262144}','\0','\0',NULL,'block',NULL,'tape','\0','group','bru',2,NULL,NULL,'original-2',NULL),('R3',NULL,'sha256','\0','{\"blocksize\": 262144}','\0','\0',NULL,'block',NULL,'tape','\0','group','tar',3,NULL,NULL,'original-3',NULL);
/*!40000 ALTER TABLE `volume` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-11-14 20:37:00
