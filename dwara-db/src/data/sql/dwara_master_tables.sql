-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dwara_v3_test
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
  `name` varchar(255) DEFAULT NULL,
  `async` bit(1) DEFAULT NULL,
  `complex` bit(1) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `storagetask_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_p6dhhp25fj7w2vok63k0vrsv` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action`
--

LOCK TABLES `action` WRITE;
/*!40000 ALTER TABLE `action` DISABLE KEYS */;
INSERT INTO `action` VALUES (1,'ingest','','',NULL,NULL),(2,'restore','','\0',NULL,2),(17,'format','','\0',NULL,7),(18,'finalize','','\0',NULL,8);
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
-- Table structure for table `actionelement`
--

DROP TABLE IF EXISTS `actionelement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `actionelement` (
  `id` int(11) NOT NULL,
  `action_id` int(11) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `artifactclass_id` int(11) DEFAULT NULL,
  `display_order` int(11) DEFAULT NULL,
  `encryption` bit(1) DEFAULT NULL,
  `processingtask_id` int(11) DEFAULT NULL,
  `storagetask_id` int(11) DEFAULT NULL,
  `volume_id` int(11) DEFAULT NULL,
  `output_artifactclass_id` int(11) DEFAULT NULL,
  `actionelement_ref_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqdits3p9d5xdjo4xsn30vo0hc` (`action_id`,`artifactclass_id`,`storagetask_id`,`processingtask_id`,`volume_id`),
  KEY `FKa45pj6kronqjpd2cevestewo7` (`output_artifactclass_id`),
  CONSTRAINT `FKa45pj6kronqjpd2cevestewo7` FOREIGN KEY (`output_artifactclass_id`) REFERENCES `artifactclass` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actionelement`
--

LOCK TABLES `actionelement` WRITE;
/*!40000 ALTER TABLE `actionelement` DISABLE KEYS */;
INSERT INTO `actionelement` VALUES (1,1,'',1,1,'\0',0,1,1,NULL,NULL),(2,1,'',1,2,'\0',0,3,1,NULL,1),(3,1,'',1,3,'\0',0,1,3,NULL,NULL);
/*!40000 ALTER TABLE `actionelement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `archiveformat`
--

DROP TABLE IF EXISTS `archiveformat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `archiveformat` (
  `id` int(11) NOT NULL,
  `blocksize` int(11) DEFAULT NULL,
  `filesize_increase_const` int(11) DEFAULT NULL,
  `filesize_increase_rate` float DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `restore_verify` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_oxw11uxyv2yaaj6ks295v2nog` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archiveformat`
--

LOCK TABLES `archiveformat` WRITE;
/*!40000 ALTER TABLE `archiveformat` DISABLE KEYS */;
INSERT INTO `archiveformat` VALUES (1,512,NULL,NULL,'tar',''),(2,512,2048,0.125,'bru_existing','\0'),(3,1024,2048,0.125,'bru','\0');
/*!40000 ALTER TABLE `archiveformat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifactclass`
--

DROP TABLE IF EXISTS `artifactclass`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifactclass` (
  `id` int(11) NOT NULL,
  `concurrent_volume_copies` bit(1) DEFAULT NULL,
  `display_order` int(11) DEFAULT NULL,
  `domain_id` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `path_prefix` varchar(255) DEFAULT NULL,
  `source` bit(1) DEFAULT NULL,
  `uid` varchar(255) DEFAULT NULL,
  `sequence_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_s7y47pljhs9d362xem78xxmd2` (`name`),
  UNIQUE KEY `UK_jr3pbriv62bo0yi8t0rdjokea` (`uid`),
  KEY `FKeynrnq0kfcuqn53tklcqexghk` (`sequence_id`),
  CONSTRAINT `FKeynrnq0kfcuqn53tklcqexghk` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifactclass`
--

LOCK TABLES `artifactclass` WRITE;
/*!40000 ALTER TABLE `artifactclass` DISABLE KEYS */;
INSERT INTO `artifactclass` VALUES (1,'\0',1,1,'pub-video','C:\\data\\ingested','','pub-video',1);
/*!40000 ALTER TABLE `artifactclass` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifactclass_action_user`
--

DROP TABLE IF EXISTS `artifactclass_action_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifactclass_action_user` (
  `action_id` int(11) NOT NULL,
  `artifactclass_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`action_id`,`artifactclass_id`,`user_id`),
  KEY `FK50220j7rqiw5bqmox91swc49k` (`artifactclass_id`),
  KEY `FKg3emxoq9y0uf3a383s0pkifvb` (`user_id`),
  CONSTRAINT `FK50220j7rqiw5bqmox91swc49k` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`),
  CONSTRAINT `FKg3emxoq9y0uf3a383s0pkifvb` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKn2cj0wmkfkpajkmqfp7mi6hbd` FOREIGN KEY (`action_id`) REFERENCES `action` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifactclass_action_user`
--

LOCK TABLES `artifactclass_action_user` WRITE;
/*!40000 ALTER TABLE `artifactclass_action_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `artifactclass_action_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifactclass_destinationpath`
--

DROP TABLE IF EXISTS `artifactclass_destinationpath`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifactclass_destinationpath` (
  `artifactclass_id` int(11) NOT NULL,
  `destinationpath_id` int(11) NOT NULL,
  PRIMARY KEY (`artifactclass_id`,`destinationpath_id`),
  KEY `FKiawpyene8ecuogy3t339uiht1` (`destinationpath_id`),
  CONSTRAINT `FKiawpyene8ecuogy3t339uiht1` FOREIGN KEY (`destinationpath_id`) REFERENCES `destinationpath` (`id`),
  CONSTRAINT `FKqndnumpsqs99havps9lkauxog` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifactclass_destinationpath`
--

LOCK TABLES `artifactclass_destinationpath` WRITE;
/*!40000 ALTER TABLE `artifactclass_destinationpath` DISABLE KEYS */;
/*!40000 ALTER TABLE `artifactclass_destinationpath` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `checksumtype`
--

DROP TABLE IF EXISTS `checksumtype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `checksumtype` (
  `id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_fkn0j06g5iba6tdciaj6fp7vt` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `checksumtype`
--

LOCK TABLES `checksumtype` WRITE;
/*!40000 ALTER TABLE `checksumtype` DISABLE KEYS */;
INSERT INTO `checksumtype` VALUES (1,NULL,'sha256');
/*!40000 ALTER TABLE `checksumtype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `destinationpath`
--

DROP TABLE IF EXISTS `destinationpath`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `destinationpath` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_1bocrqv9fijed2bytswl4ldea` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `destinationpath`
--

LOCK TABLES `destinationpath` WRITE;
/*!40000 ALTER TABLE `destinationpath` DISABLE KEYS */;
/*!40000 ALTER TABLE `destinationpath` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `device`
--

DROP TABLE IF EXISTS `device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `device` (
  `id` int(11) NOT NULL,
  `details` longtext,
  `devicetype_id` int(11) DEFAULT NULL,
  `manufacturer` varchar(255) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `uid` varchar(255) DEFAULT NULL,
  `warranty_expiry_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4776vaiywo1kdis4lp8jkm0av` (`serial_number`),
  UNIQUE KEY `UK_bym2ir5cd5feay02tryi5dv1a` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device`
--

LOCK TABLES `device` WRITE;
/*!40000 ALTER TABLE `device` DISABLE KEYS */;
INSERT INTO `device` VALUES (1,'{\"generation\":0,\"autoloader_id\":0,\"autoloader_address\":0,\"standalone\":false,\"slots\":24,\"max_drives\":3,\"generations_supported\":[6,7]}',2,NULL,NULL,NULL,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400',NULL),(2,'{\"type\":\"LTO\",\"generation\":7,\"readable_volumetype_ids\":[6,7],\"writeable_volumetype_ids\":[6,7],\"autoloader_id\":1,\"autoloader_address\":0,\"standalone\":false,\"slots\":0,\"max_drives\":0}',1,NULL,NULL,NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst',NULL),(3,'{\"type\":\"LTO\",\"generation\":7,\"readable_volumetype_ids\":[6,7],\"writeable_volumetype_ids\":[6,7],\"autoloader_id\":1,\"autoloader_address\":1,\"standalone\":false,\"slots\":0,\"max_drives\":0}',1,NULL,NULL,NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst',NULL),(4,'{\"type\":\"LTO\",\"generation\":7,\"readable_volumetype_ids\":[6,7],\"writeable_volumetype_ids\":[6,7],\"autoloader_id\":1,\"autoloader_address\":2,\"standalone\":false,\"slots\":0,\"max_drives\":0}',1,NULL,NULL,NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1970448833-nst',NULL);
/*!40000 ALTER TABLE `device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `devicetype`
--

DROP TABLE IF EXISTS `devicetype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `devicetype` (
  `id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_56gq61oiox49y2bo8lrn42pqj` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `devicetype`
--

LOCK TABLES `devicetype` WRITE;
/*!40000 ALTER TABLE `devicetype` DISABLE KEYS */;
INSERT INTO `devicetype` VALUES (1,NULL,'tape_drive'),(2,NULL,'tape_autoloader');
/*!40000 ALTER TABLE `devicetype` ENABLE KEYS */;
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
INSERT INTO `domain` VALUES (1,'','1');
/*!40000 ALTER TABLE `domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dwara_sequences`
--

DROP TABLE IF EXISTS `dwara_sequences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dwara_sequences` (
  `primary_key_fields` varchar(255) NOT NULL,
  `current_val` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`primary_key_fields`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dwara_sequences`
--

LOCK TABLES `dwara_sequences` WRITE;
/*!40000 ALTER TABLE `dwara_sequences` DISABLE KEYS */;
INSERT INTO `dwara_sequences` VALUES ('archive_id',0),('badfile_id',0),('failure_id',0),('job_id',22),('request_id',23),('subrequest_id',23),('t_activedevice_id',1);
/*!40000 ALTER TABLE `dwara_sequences` ENABLE KEYS */;
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gmfbyygelvk6j16w8p3h54a9m` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extension`
--

LOCK TABLES `extension` WRITE;
/*!40000 ALTER TABLE `extension` DISABLE KEYS */;
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
  `extension_id` int(11) NOT NULL,
  `filetype_id` int(11) NOT NULL,
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
/*!40000 ALTER TABLE `extension_filetype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `filetype`
--

DROP TABLE IF EXISTS `filetype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `filetype` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gonw2ifoyhnht1949bpyitog7` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `filetype`
--

LOCK TABLES `filetype` WRITE;
/*!40000 ALTER TABLE `filetype` DISABLE KEYS */;
/*!40000 ALTER TABLE `filetype` ENABLE KEYS */;
UNLOCK TABLES;

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
INSERT INTO `hibernate_sequence` VALUES (7),(7),(7),(7);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location` (
  `id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `restore_default` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sahixf1v7f7xns19cbg12d946` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
INSERT INTO `location` VALUES (1,'LTO Room','LR',''),(2,'Triangle Block','TB','\0'),(3,'III Tennesse','IIIT','\0');
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
/*!40000 ALTER TABLE `priorityband` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `processingtask`
--

DROP TABLE IF EXISTS `processingtask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `processingtask` (
  `id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `max_errors` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `filetype_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ta18voxyw85sovvievpj3l3l7` (`name`),
  KEY `FKio0nausiwh38wjh2l1dr8dqh6` (`filetype_id`),
  CONSTRAINT `FKio0nausiwh38wjh2l1dr8dqh6` FOREIGN KEY (`filetype_id`) REFERENCES `filetype` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `processingtask`
--

LOCK TABLES `processingtask` WRITE;
/*!40000 ALTER TABLE `processingtask` DISABLE KEYS */;
/*!40000 ALTER TABLE `processingtask` ENABLE KEYS */;
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
INSERT INTO `sequence` VALUES (1,NULL,'\0',10000,NULL);
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_reccgx9nr0a8dwv201t44l6pd` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status`
--

LOCK TABLES `status` WRITE;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
/*!40000 ALTER TABLE `status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storagelevel`
--

DROP TABLE IF EXISTS `storagelevel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storagelevel` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_i8kyg2v5hnpuf29shqwpkjgdi` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storagelevel`
--

LOCK TABLES `storagelevel` WRITE;
/*!40000 ALTER TABLE `storagelevel` DISABLE KEYS */;
INSERT INTO `storagelevel` VALUES (1,'block'),(2,'file'),(3,'object');
/*!40000 ALTER TABLE `storagelevel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storagetask`
--

DROP TABLE IF EXISTS `storagetask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storagetask` (
  `id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_366gxsifdm2p1m6hxvxkn8y0t` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storagetask`
--

LOCK TABLES `storagetask` WRITE;
/*!40000 ALTER TABLE `storagetask` DISABLE KEYS */;
INSERT INTO `storagetask` VALUES (1,NULL,'write'),(2,NULL,'restore'),(3,NULL,'verify');
/*!40000 ALTER TABLE `storagetask` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storagetype`
--

DROP TABLE IF EXISTS `storagetype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storagetype` (
  `id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_a1t3a427mbwfws1anrs7y5el2` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storagetype`
--

LOCK TABLES `storagetype` WRITE;
/*!40000 ALTER TABLE `storagetype` DISABLE KEYS */;
INSERT INTO `storagetype` VALUES (1,NULL,'tape'),(2,NULL,'disk'),(3,NULL,'cloud');
/*!40000 ALTER TABLE `storagetype` ENABLE KEYS */;
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
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `volume`
--

DROP TABLE IF EXISTS `volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `volume` (
  `id` int(11) NOT NULL,
  `capacity` bigint(20) DEFAULT NULL,
  `checksumtype_id` int(11) DEFAULT NULL,
  `details` longtext,
  `finalized` bit(1) DEFAULT NULL,
  `imported` bit(1) DEFAULT NULL,
  `storagelevel_id` int(11) DEFAULT NULL,
  `storagetype_id` int(11) DEFAULT NULL,
  `uid` varchar(255) DEFAULT NULL,
  `volumetype_id` int(11) DEFAULT NULL,
  `archiveformat_id` int(11) DEFAULT NULL,
  `location_id` int(11) DEFAULT NULL,
  `volume_ref_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_mti1y5awoy6kh7txrapn7ux85` (`uid`),
  KEY `FKsw7cga5kgm5yqs2sfpq9hdidv` (`archiveformat_id`),
  KEY `FK5k6g9ueuvb8e330dfvr88agfk` (`location_id`),
  KEY `FKqg73ij1twrjwxpc1me5xhpa21` (`volume_ref_id`),
  CONSTRAINT `FK5k6g9ueuvb8e330dfvr88agfk` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FKqg73ij1twrjwxpc1me5xhpa21` FOREIGN KEY (`volume_ref_id`) REFERENCES `volume` (`id`),
  CONSTRAINT `FKsw7cga5kgm5yqs2sfpq9hdidv` FOREIGN KEY (`archiveformat_id`) REFERENCES `archiveformat` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `volume`
--

LOCK TABLES `volume` WRITE;
/*!40000 ALTER TABLE `volume` DISABLE KEYS */;
INSERT INTO `volume` VALUES (1,2500000000000,1,NULL,'\0','\0',1,1,'V5A',2,3,1,NULL),(2,2500000000000,1,'{\"barcoded\":true,\"blocksize\":1024,\"generation\":7}','\0','\0',1,1,'V5A001',1,3,1,1),(3,2500000000000,1,NULL,'\0','\0',2,2,'V5B',2,1,2,NULL),(4,2500000000000,1,'{\"barcoded\":true,\"blocksize\":1024,\"generation\":7}','\0','\0',2,2,'V5B001',1,1,2,3),(5,6000000000000,1,'{\"barcoded\":true,\"blocksize\":1024,\"generation\":7}','\0','\0',2,2,'V5B002',1,1,2,3),(6,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'V4A',2,NULL,NULL,NULL),(7,6000000000000,1,NULL,'\0','\0',1,1,'V4A001',1,3,1,6);
/*!40000 ALTER TABLE `volume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `volumetype`
--

DROP TABLE IF EXISTS `volumetype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `volumetype` (
  `id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_lns7a9t2d48p58dllixud5yxa` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `volumetype`
--

LOCK TABLES `volumetype` WRITE;
/*!40000 ALTER TABLE `volumetype` DISABLE KEYS */;
INSERT INTO `volumetype` VALUES (1,NULL,'physical'),(2,NULL,'group'),(3,NULL,'provisioned');
/*!40000 ALTER TABLE `volumetype` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-06-19 12:49:34
