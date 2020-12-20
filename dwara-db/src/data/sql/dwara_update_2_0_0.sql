-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: localhost    Database: dwara
-- ------------------------------------------------------
-- Server version	5.7.31-log

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
INSERT INTO `action` (`id`, `description`, `type`) VALUES ('abort',NULL,'sync'),('cancel',NULL,'sync'),('delete',NULL,'sync'),('diagnostics',NULL,'sync'),('finalize',NULL,'storage_task'),('finalize_process',NULL,'complex'),('hold',NULL,'sync'),('import',NULL,'storage_task'),('ingest',NULL,'complex'),('initialize',NULL,'storage_task'),('list',NULL,'sync'),('map_tapedrives',NULL,'storage_task'),('migrate',NULL,'storage_task'),('process',NULL,'complex'),('release',NULL,'sync'),('rename',NULL,'sync'),('restore',NULL,'storage_task'),('rewrite',NULL,'storage_task'),('verify',NULL,'storage_task'),('write',NULL,'storage_task');
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
INSERT INTO `action_artifactclass_flow` (`active`, `action_id`, `artifactclass_id`, `flow_id`) VALUES ('','ingest','audio-priv1','archive-flow'),('','ingest','audio-priv2','archive-flow'),('','ingest','audio-priv3','archive-flow'),('','ingest','audio-pub','archive-flow'),('','ingest','dept-backup','archive-flow'),('','ingest','video-edit-priv1','archive-flow'),('','ingest','video-edit-priv1','video-proxy-flow'),('','ingest','video-edit-priv2','archive-flow'),('\0','ingest','video-edit-priv2','video-proxy-flow'),('','ingest','video-edit-pub','archive-flow'),('','ingest','video-edit-pub','video-proxy-flow'),('','ingest','video-priv1','archive-flow'),('','ingest','video-priv1','video-proxy-flow'),('','ingest','video-priv2','archive-flow'),('\0','ingest','video-priv2','video-proxy-flow'),('','ingest','video-priv3','archive-flow'),('','ingest','video-pub','archive-flow'),('','ingest','video-pub','video-proxy-flow');
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
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES ('ingest','audio-priv1',2),('ingest','audio-priv2',2),('ingest','audio-pub',2),('ingest','dept-backup',2),('ingest','video-edit-global',2),('ingest','video-edit-priv1',2),('ingest','video-edit-priv2',2),('ingest','video-edit-pub',2),('ingest','video-priv1',2),('ingest','video-priv2',2),('ingest','video-pub',2),('ingest','audio-priv1',3),('ingest','audio-priv2',3),('ingest','audio-pub',3),('ingest','dept-backup',3),('ingest','video-edit-priv1',3),('ingest','video-edit-priv2',3),('ingest','video-edit-pub',3),('ingest','video-priv1',3),('ingest','video-priv2',3),('ingest','video-pub',3),('ingest','audio-priv1',4),('ingest','audio-priv2',4),('ingest','audio-priv3',4),('ingest','audio-pub',4),('ingest','dept-backup',4),('ingest','video-edit-priv1',4),('ingest','video-edit-priv2',4),('ingest','video-edit-pub',4),('ingest','video-priv1',4),('ingest','video-priv2',4),('ingest','video-priv3',4),('ingest','video-pub',4),('ingest','video-edit-global',5),('ingest','video-edit-priv1',5),('ingest','video-edit-pub',5),('ingest','video-priv1',5),('ingest','video-priv2',5),('ingest','video-pub',5);
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
INSERT INTO `archiveformat` (`id`, `blocksize`, `description`, `filesize_increase_const`, `filesize_increase_rate`, `restore_verify`) VALUES ('bru',2048,'BRU TOlis',2048,0.125,'\0'),('tar',512,'Tar (posix)',NULL,NULL,'');
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
  `domain_id` int(11) DEFAULT NULL,
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
INSERT INTO `artifactclass` (`id`, `concurrent_volume_copies`, `description`, `display_order`, `domain_id`, `import_only`, `path_prefix`, `source`, `artifactclass_ref_id`, `sequence_id`) VALUES ('audio-priv1','\0','',17,1,'\0','/data/dwara/staged','',NULL,'audio-pub'),('audio-priv2','\0','',18,1,'\0','/data/dwara/staged','',NULL,'audio-priv2'),('audio-priv3','\0','',3,1,'\0','/data/dwara/staged','',NULL,'audio-priv3'),('audio-pub','\0','',16,1,'\0','/data/dwara/staged','',NULL,'audio-pub'),('dept-backup','\0','',28,2,'\0','/data/dwara/staged','',NULL,'dept-backup'),('video-edit-global','\0',NULL,27,1,'\0','/data/dwara/staged','',NULL,'video-edit-global'),('video-edit-priv1','\0','',11,1,'\0','/data/dwara/staged','',NULL,'video-edit-pub'),('video-edit-priv2','\0','',12,1,'\0','/data/dwara/staged','',NULL,'video-edit-priv2'),('video-edit-pub','\0','',10,1,'\0','/data/dwara/staged','',NULL,'video-edit-pub'),('video-priv1','\0','',2,1,'\0','/data/dwara/staged','',NULL,'video-pub'),('video-priv1-proxy-low','\0','',14,1,'\0','/data/dwara/transcoded','\0','video-priv1','video-pub-proxy-low'),('video-priv2','\0','',3,1,'\0','/data/dwara/staged','',NULL,'video-priv2'),('video-priv2-proxy-low','\0','',15,1,'\0','/data/dwara/transcoded','\0','video-priv2','video-priv2-proxy-low'),('video-priv3','\0','',3,1,'\0','/data/dwara/staged','',NULL,'video-priv3'),('video-pub','\0','',1,1,'\0','/data/dwara/staged','',NULL,'video-pub'),('video-pub-proxy-low','\0','',13,1,'\0','/data/dwara/transcoded','\0','video-pub','video-pub-proxy-low');
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
INSERT INTO `artifactclass_processingtask` (`pathname_regex`, `artifactclass_id`, `processingtask_id`) VALUES ('.*/Outputs?/[^/]*.mov$','video-edit-priv1','video-proxy-low-gen'),('.*/Outputs?/[^/]*.mov$','video-edit-priv2','video-proxy-low-gen'),('.*/Outputs?/[^/]*.mov$','video-edit-pub','video-proxy-low-gen');
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
INSERT INTO `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) VALUES ('','\0','audio-priv1','R1'),('','\0','audio-priv1','R2'),('','\0','audio-priv1','R3'),('','\0','audio-priv2','X1'),('','\0','audio-priv2','X2'),('','\0','audio-priv2','X3'),('','\0','audio-priv3','Y1'),('','\0','audio-priv3','Y2'),('','\0','audio-pub','R1'),('','\0','audio-pub','R2'),('','\0','audio-pub','R3'),('','\0','dept-backup','B1'),('','\0','dept-backup','B2'),('','\0','dept-backup','B3'),('','\0','video-edit-priv1','G1'),('','\0','video-edit-priv1','G2'),('','\0','video-edit-priv1','G3'),('','\0','video-edit-priv2','X1'),('','\0','video-edit-priv2','X2'),('','\0','video-edit-priv2','X3'),('','\0','video-edit-pub','G1'),('','\0','video-edit-pub','G2'),('','\0','video-edit-pub','G3'),('','\0','video-priv1','R1'),('','\0','video-priv1','R2'),('','\0','video-priv1','R3'),('','\0','video-priv1-proxy-low','G1'),('','\0','video-priv1-proxy-low','G2'),('','\0','video-priv2','X1'),('','\0','video-priv2','X2'),('','\0','video-priv2','X3'),('','\0','video-priv2-proxy-low','X1'),('','\0','video-priv2-proxy-low','X2'),('','\0','video-priv3','Y1'),('','\0','video-priv3','Y2'),('','\0','video-pub','R1'),('','\0','video-pub','R2'),('','\0','video-pub','R3'),('','\0','video-pub-proxy-low','G1'),('','\0','video-pub-proxy-low','G2');
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
INSERT INTO `copy` (`id`, `location_id`) VALUES (3,'india-offsite-1'),(1,'lto-room'),(2,'t-block');
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
INSERT INTO `destination` (`id`, `path`, `use_buffering`) VALUES ('local','/data/dwara/restored','\0'),('san-video','/mnt/san/video/LTO_Restore/public',''),('san-video1','/mnt/san/video1','');
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
INSERT INTO `domain` (`id`, `default`, `name`) VALUES (1,'','main'),(2,'\0','dept-backup');
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
  `ignore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extension`
--

LOCK TABLES `extension` WRITE;
/*!40000 ALTER TABLE `extension` DISABLE KEYS */;
INSERT INTO `extension` (`id`, `description`, `ignore`) VALUES ('',NULL,NULL),('16 In Conversation 98055269794',NULL,NULL),('37 Dry Run Sakshi TV 98947221568',NULL,NULL),('39 Dry Run Sakshi TV 98947221568',NULL,NULL),('3gp',NULL,NULL),('45 Dry Run Sakshi TV 98947221568',NULL,NULL),('46 In Conversation 98055269794',NULL,NULL),('47 Dry Run Sakshi TV 98947221568',NULL,NULL),('arw','Image captured by a Sony digital camera, based on the TIFF specification; contains raw, uncompressed image data. (Different Sony camera models may store raw images using the same \".arw\" file extension, but with a different format).','\0'),('avi',NULL,NULL),('BAK',NULL,NULL),('bdm','Sony binary information file created by AVCHD video camera',''),('bim','Sony XDCAM real-time metadata file, which contains timecode and codec information designed to be read by an application during playback and, as such, is not human readable.',''),('bin','Generic binary file',''),('bk',NULL,NULL),('BNP',NULL,NULL),('BridgeLabelsAndRatings',NULL,NULL),('BridgeSort',NULL,NULL),('BUP',NULL,NULL),('conf',NULL,NULL),('CPF',NULL,NULL),('cpi','Sony binary metadata file corresponding to an AVCHD video clip',''),('cr2','Canon RAW Image file','\0'),('ctg','Catalog index file created by Canon cameras. Contains info about the number of images stored in each folder on a memory card',''),('db',NULL,NULL),('dng','Universal RAW image format for saving digital photos in an uncompressed format','\0'),('heic','High Efficiency Image Format (HEIF), a file format commonly used to store photos on mobile devices. It may contain a single image or a sequence of images along with corresponding metadata. HEIC files may also appear as .HEIF files.','\0'),('HIF',NULL,NULL),('hprj',NULL,NULL),('IFO',NULL,NULL),('ind','Sony file placed on a media card when formatted with a Sony device',''),('ini',NULL,NULL),('INP',NULL,NULL),('insv',NULL,NULL),('INT',NULL,NULL),('jpg','','\0'),('list',NULL,NULL),('lrv','GoPro low resolution video proxy',''),('m3u','Multimedia playlist',''),('m4a','MPEG-4 format audio file encoded with Advanced Audio Coding (AAC) codec','\0'),('m4v',NULL,NULL),('mhl',NULL,NULL),('MIF',NULL,NULL),('mov','','\0'),('mov_thumbnail','Thumbnail picture for a mov video file',''),('mp3','','\0'),('mp4','Short for MPEG-4 Part 14, a container format based on the QuickTime File Format (QTFF) used by .mov and .qt files','\0'),('mp4_ffprobe_out','','\0'),('mpl','Sony binary playlist file created by AVCHD video camera',''),('mts','Advanced Video Coding High Definition (AVCHD) video file','\0'),('mxf','','\0'),('NEF',NULL,NULL),('png',' Portable Network Graphic (PNG) format. Contains a bitmap compressed with lossless compression similar to a .GIF file.  ','\0'),('ppn','Sony picture pointer file, not human readable, which includes the position of each frame. The MPEG-2 codec used by XDCAM EX, utilizes variable bit rate encoding, so the positions of frames are not as easy to determine as with constant bit rate encoding.',''),('pptx',NULL,NULL),('sav','GoPro file (purpose unknown)',''),('smi','Sony XDCAM clip information file: contains links to the mp4 and bim files, information on the audio and video codecs, and the clip\'s starting and ending timecode values',''),('SRT',NULL,NULL),('thm','GoPro thumbnail image',''),('txt',NULL,NULL),('url',NULL,NULL),('VOB',NULL,NULL),('wav','','\0'),('webm',NULL,NULL),('xml','Extensible Metadata Language format commonly used for metadata',''),('xmp',NULL,NULL);
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
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES ('\0','arw','image'),('\0','cr2','image'),('\0','dng','image'),('\0','heic','image'),('\0','jpg','image'),('','jpg','video-proxy'),('\0','m4a','audio'),('\0','mov','video'),('\0','mp3','audio'),('\0','mp4','video'),('\0','mp4','video-proxy'),('','mp4_ffprobe_out','video-proxy'),('\0','mts','video'),('\0','mxf','video'),('\0','png','image'),('\0','wav','audio');
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
INSERT INTO `filetype` (`id`, `description`) VALUES ('audio','Audio Files'),('image','Image files'),('video','Video Files'),('video-proxy','Video Proxy Files');
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
INSERT INTO `flow` (`id`, `description`) VALUES ('archive-flow','cksum-gen, write, verify'),('video-proxy-flow','video transcoding, mam update, proxy archiving');
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
  `flow_id` varchar(255) DEFAULT NULL,
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
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `display_order`, `processingtask_id`, `storagetask_action_id`, `flow_id`, `flow_ref_id`) VALUES (1,'',NULL,1,'checksum-gen',NULL,'archive-flow',NULL),(2,'',NULL,2,NULL,'write','archive-flow',NULL),(3,'','[1, 2]',3,NULL,'verify','archive-flow',NULL),(4,'',NULL,4,'video-proxy-low-gen',NULL,'video-proxy-flow',NULL),(5,'','[4]',5,'video-mam-update',NULL,'video-proxy-flow',NULL),(6,'','[4]',6,NULL,NULL,'video-proxy-flow','archive-flow');
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
INSERT INTO `location` (`id`, `default`, `description`) VALUES ('india-offsite-1','\0','India Offsite Location 1'),('lto-room','','IYC Archives LTO Room'),('t-block','\0','IYC Triangle Block');
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
INSERT INTO `priorityband` (`id`, `end`, `name`, `optimize_tape_access`, `start`) VALUES (1,10,'','',5);
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
INSERT INTO `processingtask` (`id`, `description`, `filetype_id`, `max_errors`, `output_artifactclass_suffix`) VALUES ('checksum-gen','generate sha256 file checksums and update db','_all_',0,NULL),('video-mam-update','move proxy files to mam server and add xml metadata to mam','video-proxy',0,NULL),('video-proxy-low-gen','generate low resolution video proxies (with thumbnail and metadata xml)','video',10,'-proxy-low');
/*!40000 ALTER TABLE `processingtask` ENABLE KEYS */;
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
INSERT INTO `user` (`id`, `email`, `hash`, `name`, `priorityband_id`) VALUES (1,NULL,NULL,'dwara',1),(2,'swami.kevala@ishafoundation.org','$2a$10$iKAM40YhIAcBWG96W.6NOuQP2GrnbGpKpGbZk8HikYRNMAthcAFg.','swamikevala',1),(3,'prakash.gurumurthy@ishafoundation.org','$2a$10$70nZ.1zvmmgAXQZ5qDFHxe08eTijEejJ5HRZAtwRcPuMjw4MfRley','pgurumurthy',1),(4,'maa.jeevapushpa@ishafoundation.org','$2a$10$XzZL/LTESpJ2L7.LTWL3.enor29Unjqsshvgb.OjdO0zhbQpSV6zC','maajeevapushpa',1),(5,'ramkumar.j@ishafoundation.org','$2a$10$17Jl0H5mcDlIEgPWu5lqk.t.rE.5LrU.6wqDkQbHCkBKlmUfjC18u','ramkumarj',1),(6,NULL,'$2a$10$tXuvjCO3WJ.rmKq45/cHdOnqkvEtzTo5j2eX7w92sYDCZoFizbds2','maahitasi',1),(7,NULL,'$2a$10$E93gqXU/sHI.Lt55649U9ujBg2on5gT1VweGFQqimyaBuX7jnTs1O','dhananjay',1);
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
INSERT INTO `version` (`version`) VALUES ('2.0.2');
/*!40000 ALTER TABLE `version` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-12-20 15:38:25
