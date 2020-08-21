-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: localhost    Database: dwara_v4_test
-- ------------------------------------------------------
-- Server version	5.7.28

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
-- Table structure for table `action_user`
--

DROP TABLE IF EXISTS `action_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action_user` (
  `permission_level` int(11) DEFAULT NULL,
  `action_id` varchar(255) NOT NULL,
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
INSERT INTO `actionelement` VALUES (1,'','pub-video','ingest',1,'\0','checksum-generation',NULL,NULL,NULL),(2,'','pub-video','ingest',2,'\0',NULL,'write','N1',NULL),(3,'','pub-video','ingest',3,'\0',NULL,'verify','N1',NULL),(4,'','pub-video','ingest',4,'\0',NULL,'write','N2',NULL),(5,'','pub-video','ingest',5,'\0',NULL,'verify','N2',NULL),(6,'\0','pub-video','ingest',6,'\0',NULL,'write','N3',NULL),(7,'\0','pub-video','ingest',7,'\0',NULL,'verify','N3',NULL),(8,'','pub-video','ingest',8,'\0','previewproxy-video-transcoding',NULL,NULL,'previewproxy-video'),(9,'','pub-video','ingest',9,'\0','mam-updation',NULL,NULL,NULL);
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
INSERT INTO `artifactclass` VALUES ('previewproxy-video','\0',2,'1','previewproxy-video','/data/transcoded','\0',7),('pub-video','\0',1,'1','pub-video','/data/ingested','',1);
/*!40000 ALTER TABLE `artifactclass` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifactclass_action_user`
--

DROP TABLE IF EXISTS `artifactclass_action_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifactclass_action_user` (
  `action_id` varchar(255) NOT NULL,
  `artifactclass_id` varchar(255) NOT NULL,
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
INSERT INTO `artifactclass_action_user` VALUES ('ingest','pub-video',1);
/*!40000 ALTER TABLE `artifactclass_action_user` ENABLE KEYS */;
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
INSERT INTO `device` VALUES ('DEV_LTO5_1','\0','{\"type\": \"LTO\", \"standalone\": false, \"autoloader_id\": \"DEV_XL80\", \"autoloader_address\": 0}','tape_drive',NULL,NULL,NULL,'online',NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_0777143630-nst'),('DEV_LTO5_2','\0','{\"type\": \"LTO\", \"standalone\": false, \"autoloader_id\": \"DEV_XL80\", \"autoloader_address\": 1}','tape_drive',NULL,NULL,NULL,'online',NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_0005618080-nst'),('DEV_LTO5_3','\0','{\"type\": \"LTO\", \"standalone\": false, \"autoloader_id\": \"DEV_XL80\", \"autoloader_address\": 2}','tape_drive',NULL,NULL,NULL,'online',NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_0574047035-nst'),('DEV_LTO5_4','\0','{\"type\": \"LTO\", \"standalone\": false, \"autoloader_id\": \"DEV_XL80\", \"autoloader_address\": 3}','tape_drive',NULL,NULL,NULL,'online',NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_0257880063-nst'),('DEV_XL80','\0','{\"slots\": 24, \"max_drives\": 3, \"generations_supported\": [6, 7]}','tape_autoloader',NULL,NULL,NULL,'online',NULL,'/dev/tape/by-id/scsi-1IBM_03584L32_0000077866630400');
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
  `ignore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gmfbyygelvk6j16w8p3h54a9m` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extension`
--

LOCK TABLES `extension` WRITE;
/*!40000 ALTER TABLE `extension` DISABLE KEYS */;
INSERT INTO `extension` VALUES ('',NULL,NULL,NULL),('ABC','ABC desc','ABC',NULL),('BIM',NULL,NULL,NULL),('JPG','d','JPG',NULL),('LRV',NULL,NULL,NULL),('MOV','Some MOV description','MOV',NULL),('MP4','Some MP4 description','MP4',NULL),('MP4_FFPROBE_OUT','test','MP4_FFPROBE_OUT',NULL),('MTB','some msg','MTB',NULL),('MTS','Some MTS description','MTS',NULL),('MXF','MXF container','MXF',NULL),('NEF','d','NEF',NULL),('sav',NULL,NULL,NULL),('THM',NULL,NULL,NULL),('TIF','d','TIF',NULL),('txt',NULL,NULL,NULL),('XML',NULL,NULL,NULL),('XMP','d','XMP',NULL);
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
INSERT INTO `extension_filetype` VALUES ('\0','ABC','video'),('\0','JPG','photo'),('','JPG','video'),('','JPG','video_prev_proxy'),('\0','MOV','video'),('\0','MP4','video'),('\0','MP4','video_prev_proxy'),('','MP4_FFPROBE_OUT','video'),('','MP4_FFPROBE_OUT','video_prev_proxy'),('\0','MTB','video'),('\0','MTS','video'),('\0','MXF','video'),('\0','NEF','photo'),('\0','TIF','photo'),('','XMP','photo');
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
INSERT INTO `priorityband` VALUES (1,10,'Band 1','',5);
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
  `ending_number` int(11) DEFAULT NULL,
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
INSERT INTO `sequence` VALUES (1,NULL,'\0',NULL,'\0','N',NULL,'artifact',13,NULL),(2,'^[A-Z]{1,2}[\\\\d]{1,4}','\0',999,'\0','A',NULL,'artifact',NULL,NULL),(3,NULL,'\0',555,'\0','Z',NULL,'artifact',NULL,NULL),(4,'^[A-Z]{1,2}[\\\\d]{1,4}','\0',333,'\0',NULL,NULL,'artifact',NULL,NULL),(5,'^[\\\\d]{1,5}','',0,'\0',NULL,NULL,'artifact',NULL,NULL),(6,'^[Z\\\\d]{1,6}','',0,'\0',NULL,NULL,'artifact',NULL,NULL),(7,NULL,'\0',0,'\0','L',NULL,'artifact',NULL,NULL),(8,NULL,'\0',0,'\0','M',NULL,'artifact',NULL,NULL),(9,NULL,'\0',NULL,'\0','P',NULL,'artifact',13,NULL),(10,NULL,'\0',NULL,'\0','X',NULL,'artifact',13,NULL),(11,NULL,NULL,10004,'\0','N',10000,'volume',NULL,18999),(12,NULL,NULL,20003,'\0','N',20000,'volume',NULL,29999),(13,NULL,NULL,24534,'',NULL,24500,'artifact',NULL,NULL),(14,NULL,NULL,19000,'\0','P',19000,'volume',NULL,19999);
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
  `email` varchar(255) DEFAULT NULL,
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
INSERT INTO `user` VALUES (1,'$2a$10$70nZ.1zvmmgAXQZ5qDFHxe08eTijEejJ5HRZAtwRcPuMjw4MfRley','pgurumurthy',1,NULL);
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
  `imported` bit(1) DEFAULT NULL,
  `storagelevel` varchar(255) DEFAULT NULL,
  `storagesubtype` varchar(255) DEFAULT NULL,
  `storagetype` varchar(255) DEFAULT NULL,
  `volumetype` varchar(255) DEFAULT NULL,
  `archiveformat_id` varchar(255) DEFAULT NULL,
  `location_id` varchar(255) DEFAULT NULL,
  `volume_ref_id` varchar(255) DEFAULT NULL,
  `sequence_id` int(11) DEFAULT NULL,
  `initialized_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsw7cga5kgm5yqs2sfpq9hdidv` (`archiveformat_id`),
  KEY `FK5k6g9ueuvb8e330dfvr88agfk` (`location_id`),
  KEY `FKqg73ij1twrjwxpc1me5xhpa21` (`volume_ref_id`),
  KEY `FK94srhb48x080eknhc0yx0ad2o` (`sequence_id`),
  CONSTRAINT `FK5k6g9ueuvb8e330dfvr88agfk` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK94srhb48x080eknhc0yx0ad2o` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`),
  CONSTRAINT `FKqg73ij1twrjwxpc1me5xhpa21` FOREIGN KEY (`volume_ref_id`) REFERENCES `volume` (`id`),
  CONSTRAINT `FKsw7cga5kgm5yqs2sfpq9hdidv` FOREIGN KEY (`archiveformat_id`) REFERENCES `archiveformat` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `volume`
--

LOCK TABLES `volume` WRITE;
/*!40000 ALTER TABLE `volume` DISABLE KEYS */;
INSERT INTO `volume` VALUES ('IMP1',2500000000000,'sha256',NULL,'','','block',NULL,'tape','group','bru','LR',NULL,NULL,NULL),('N1',6000000000000,'sha256',NULL,'\0','\0','block',NULL,'tape','group','tar','LR',NULL,11,NULL),('N2',2500000000000,'sha256',NULL,'\0','\0','block',NULL,'tape','group','bru','TB',NULL,12,NULL),('N3',2500000000000,'sha256',NULL,'\0','\0','file',NULL,'disk','group',NULL,'IIIT',NULL,NULL,NULL),('P1',6000000000000,'sha256',NULL,'\0','\0','block',NULL,'tape','group','bru','LR',NULL,NULL,NULL);
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

-- Dump completed on 2020-08-21 15:13:03
