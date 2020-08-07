-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dwara_v5_test
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
INSERT INTO `action` VALUES ('abort',NULL,'sync'),('cancel',NULL,'sync'),('delete',NULL,'sync'),('diagnostics',NULL,'sync'),('finalize',NULL,'storage_task'),('format',NULL,'storage_task'),('hold',NULL,'sync'),('import',NULL,'storage_task'),('ingest',NULL,'complex'),('list',NULL,'sync'),('map_tapedrives',NULL,'storage_task'),('migrate',NULL,'storage_task'),('process',NULL,'complex'),('release',NULL,'sync'),('rename',NULL,'sync'),('restore',NULL,'storage_task'),('restore_process',NULL,'complex'),('rewrite',NULL,'storage_task'),('verify',NULL,'storage_task'),('write',NULL,'storage_task');
/*!40000 ALTER TABLE `action` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `actionelement`
--

DROP TABLE IF EXISTS `actionelement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `actionelement` (
  `id` int(11) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `artifactclass_id` varchar(255) DEFAULT NULL,
  `complex_action_id` varchar(255) DEFAULT NULL,
  `display_order` int(11) DEFAULT NULL,
  `encryption` bit(1) DEFAULT NULL,
  `processingtask_id` varchar(255) DEFAULT NULL,
  `storagetask_action_id` varchar(255) DEFAULT NULL,
  `volume_id` varchar(255) DEFAULT NULL,
  `output_artifactclass_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKctjlo837d7w4lee0vk0cnyg4k` (`complex_action_id`,`artifactclass_id`,`storagetask_action_id`,`processingtask_id`,`volume_id`),
  KEY `FKa45pj6kronqjpd2cevestewo7` (`output_artifactclass_id`),
  CONSTRAINT `FKa45pj6kronqjpd2cevestewo7` FOREIGN KEY (`output_artifactclass_id`) REFERENCES `artifactclass` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actionelement`
--

LOCK TABLES `actionelement` WRITE;
/*!40000 ALTER TABLE `actionelement` DISABLE KEYS */;
INSERT INTO `actionelement` VALUES (1,'','pub-video','ingest',1,'\0','checksum-generation',NULL,NULL,NULL),(2,'','pub-video','ingest',2,'\0',NULL,'write','N1',NULL),(3,'','pub-video','ingest',3,'\0',NULL,'verify','N1',NULL),(4,'\0','pub-video','ingest',4,'\0',NULL,'write','N2',NULL),(5,'\0','pub-video','ingest',5,'\0',NULL,'verify','N2',NULL),(6,'\0','pub-video','ingest',6,'\0',NULL,'write','N3',NULL),(7,'\0','pub-video','ingest',7,'\0',NULL,'verify','N3',NULL),(8,'\0','pub-video','ingest',8,'\0','previewproxy-video-transcoding',NULL,NULL,'previewproxy-video'),(9,'\0','pub-video','ingest',9,'\0','mam-updation',NULL,NULL,NULL);
/*!40000 ALTER TABLE `actionelement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `actionelement_map`
--

DROP TABLE IF EXISTS `actionelement_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `actionelement_map` (
  `id` int(11) NOT NULL,
  `id_ref` int(11) NOT NULL,
  PRIMARY KEY (`id`,`id_ref`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actionelement_map`
--

LOCK TABLES `actionelement_map` WRITE;
/*!40000 ALTER TABLE `actionelement_map` DISABLE KEYS */;
INSERT INTO `actionelement_map` VALUES (3,1),(3,2),(5,1),(5,4),(7,1),(7,6),(9,8);
/*!40000 ALTER TABLE `actionelement_map` ENABLE KEYS */;
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
INSERT INTO `archiveformat` VALUES ('bru',2048,NULL,2048,0.125,'\0'),('tar',512,NULL,NULL,NULL,'');
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
  `display_order` int(11) DEFAULT NULL,
  `domain_id` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `path_prefix` varchar(255) DEFAULT NULL,
  `source` bit(1) DEFAULT NULL,
  `sequence_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_s7y47pljhs9d362xem78xxmd2` (`name`),
  KEY `FKeynrnq0kfcuqn53tklcqexghk` (`sequence_id`),
  CONSTRAINT `FKeynrnq0kfcuqn53tklcqexghk` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifactclass`
--

LOCK TABLES `artifactclass` WRITE;
/*!40000 ALTER TABLE `artifactclass` DISABLE KEYS */;
INSERT INTO `artifactclass` VALUES ('previewproxy-video','\0',2,'1','previewproxy-video','C:\\data\\transcoded','\0',7),('pub-video','\0',1,'1','pub-video','C:\\data\\ingested','',1);
/*!40000 ALTER TABLE `artifactclass` ENABLE KEYS */;
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `destination`
--

LOCK TABLES `destination` WRITE;
/*!40000 ALTER TABLE `destination` DISABLE KEYS */;
/*!40000 ALTER TABLE `destination` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `device`
--

DROP TABLE IF EXISTS `device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `device` (
  `id` varchar(255) NOT NULL,
  `defective` bit(1) DEFAULT NULL,
  `details` json DEFAULT NULL,
  `devicetype` varchar(255) DEFAULT NULL,
  `manufacturer` varchar(255) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `warranty_expiry_date` datetime(6) DEFAULT NULL,
  `wwn_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4776vaiywo1kdis4lp8jkm0av` (`serial_number`),
  UNIQUE KEY `UK_rybeolllge5fl2xeefjy2gi27` (`wwn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device`
--

LOCK TABLES `device` WRITE;
/*!40000 ALTER TABLE `device` DISABLE KEYS */;
INSERT INTO `device` VALUES ('LTO5_1','\0','{\"type\": \"LTO\", \"standalone\": false, \"autoloader_id\": \"XL80\", \"autoloader_address\": 0}','tape_drive',NULL,NULL,NULL,'online',NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst'),('LTO5_2','\0','{\"type\": \"LTO\", \"standalone\": false, \"autoloader_id\": \"XL80\", \"autoloader_address\": 1}','tape_drive',NULL,NULL,NULL,'online',NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst'),('LTO5_3','\0','{\"type\": \"LTO\", \"standalone\": false, \"autoloader_id\": \"XL80\", \"autoloader_address\": 2}','tape_drive',NULL,NULL,NULL,'online',NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1970448833-nst'),('XL80','\0','{\"slots\": 24, \"max_drives\": 3, \"generations_supported\": [6, 7]}','tape_autoloader',NULL,NULL,NULL,'online',NULL,'/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400');
/*!40000 ALTER TABLE `device` ENABLE KEYS */;
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
INSERT INTO `domain` VALUES (1,'','main'),(2,'\0','other');
/*!40000 ALTER TABLE `domain` ENABLE KEYS */;
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
INSERT INTO `extension` VALUES ('ABC','ABC desc','ABC'),('JPG','d','JPG'),('MOV','Some MOV description','MOV'),('MP4','Some MP4 description','MP4'),('MP4_FFPROBE_OUT','test','MP4_FFPROBE_OUT'),('MTB','some msg','MTB'),('MTS','Some MTS description','MTS'),('NEF','d','NEF'),('TIF','d','TIF'),('XMP','d','XMP');
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
INSERT INTO `extension_filetype` VALUES ('\0','ABC','video'),('\0','JPG','photo'),('','JPG','video'),('','JPG','video_prev_proxy'),('\0','MOV','video'),('\0','MP4','video'),('\0','MP4','video_prev_proxy'),('','MP4_FFPROBE_OUT','video'),('','MP4_FFPROBE_OUT','video_prev_proxy'),('\0','MTB','video'),('\0','MTS','video'),('\0','NEF','photo'),('\0','TIF','photo'),('','XMP','photo');
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
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gonw2ifoyhnht1949bpyitog7` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `filetype`
--

LOCK TABLES `filetype` WRITE;
/*!40000 ALTER TABLE `filetype` DISABLE KEYS */;
INSERT INTO `filetype` VALUES ('audio','audio',NULL),('photo','photo',NULL),('video','video',NULL),('video_prev_proxy','video_prev_proxy',NULL);
/*!40000 ALTER TABLE `filetype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location` (
  `id` varchar(255) NOT NULL,
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
INSERT INTO `location` VALUES ('IIIT','III Tennesse','\0'),('LR','LTO Room',''),('TB','Triangle Block','\0');
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
  `id` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `max_errors` int(11) DEFAULT NULL,
  `filetype_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKio0nausiwh38wjh2l1dr8dqh6` (`filetype_id`),
  CONSTRAINT `FKio0nausiwh38wjh2l1dr8dqh6` FOREIGN KEY (`filetype_id`) REFERENCES `filetype` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `processingtask`
--

LOCK TABLES `processingtask` WRITE;
/*!40000 ALTER TABLE `processingtask` DISABLE KEYS */;
INSERT INTO `processingtask` VALUES ('checksum-generation','Generates checksum for ingested files ',0,NULL),('mam-updation',NULL,20,'video'),('previewproxy-video-transcoding','Transcodes to low resolution previewproxy video',0,'video');
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
  `artifact_extraction_regex` varchar(255) DEFAULT NULL,
  `artifact_keep_code` bit(1) DEFAULT NULL,
  `current_number` int(11) DEFAULT NULL,
  `group` bit(1) DEFAULT NULL,
  `prefix` varchar(255) DEFAULT NULL,
  `starting_number` int(11) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `sequence_ref_id` int(11) DEFAULT NULL,
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
INSERT INTO `sequence` VALUES (1,NULL,'\0',24533,'\0',NULL,NULL,'artifact',NULL),(2,'^[A-Z]{1,2}[\\\\d]{1,4}','\0',999,'\0','A',NULL,'artifact',NULL),(3,NULL,'\0',555,'\0','Z',NULL,'artifact',NULL),(4,'^[A-Z]{1,2}[\\\\d]{1,4}','\0',333,'\0',NULL,NULL,'artifact',NULL),(5,'^[\\\\d]{1,5}','',0,'\0',NULL,NULL,'artifact',NULL),(6,'^[Z\\\\d]{1,6}','',0,'\0',NULL,NULL,'artifact',NULL),(7,NULL,'\0',0,'\0','L',NULL,'artifact',NULL),(8,NULL,'\0',0,'\0','M',NULL,'artifact',NULL),(9,NULL,'\0',0,'\0','V1',NULL,'artifact',NULL),(10,NULL,'\0',0,'\0','V2',NULL,'artifact',NULL);
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
  `id` varchar(255) NOT NULL,
  `capacity` bigint(20) DEFAULT NULL,
  `checksumtype` varchar(255) DEFAULT NULL,
  `details` json DEFAULT NULL,
  `finalized` bit(1) DEFAULT NULL,
  `formatted_at` datetime(6) DEFAULT NULL,
  `imported` bit(1) DEFAULT NULL,
  `storagelevel` varchar(255) DEFAULT NULL,
  `storagesubtype` varchar(255) DEFAULT NULL,
  `storagetype` varchar(255) DEFAULT NULL,
  `volumetype` varchar(255) DEFAULT NULL,
  `archiveformat_id` varchar(255) DEFAULT NULL,
  `location_id` varchar(255) DEFAULT NULL,
  `volume_ref_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
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
INSERT INTO `volume` VALUES ('IMP1',2500000000000,'sha256',NULL,'',NULL,'','block',NULL,'tape','group','bru','LR',NULL),('N1',6000000000000,'sha256',NULL,'\0',NULL,'\0','block',NULL,'tape','group','tar','LR',NULL),('N10001',6000000000000,'sha256','{\"barcoded\": true, \"blocksize\": 524288, \"generation\": 7}','\0',NULL,'\0','block','lto7','tape','physical','tar','LR','N1'),('N2',2500000000000,'sha256',NULL,'\0',NULL,'\0','block',NULL,'tape','group','bru','TB',NULL),('N3',2500000000000,'sha256',NULL,'\0',NULL,'\0','file',NULL,'disk','group',NULL,'IIIT',NULL),('P1',6000000000000,'sha256',NULL,'\0',NULL,'\0','block',NULL,'tape','group','bru','LR',NULL);
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

-- Dump completed on 2020-08-07 13:34:31
