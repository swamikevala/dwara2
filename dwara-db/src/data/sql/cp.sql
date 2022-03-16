-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: cp2
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
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action`
--

LOCK TABLES `action` WRITE;
/*!40000 ALTER TABLE `action` DISABLE KEYS */;
INSERT INTO `action` (`id`, `description`, `type`) VALUES
('abort',NULL,'sync'),
('cancel',NULL,'sync'),
('change_artifactclass',NULL,'sync'),
('checksum-verify',NULL,'storage_task'),
('delete',NULL,'sync'),
('diagnostics',NULL,'sync'),
('finalize',NULL,'storage_task'),
('hold',NULL,'sync'),
('import',NULL,'storage_task'),
('ingest',NULL,'complex'),
('initialize',NULL,'storage_task'),
('list',NULL,'sync'),
('map_tapedrives',NULL,'storage_task'),
('marked_completed',NULL,'sync'),
('migrate',NULL,'storage_task'),
('process',NULL,'complex'),
('release',NULL,'sync'),
('rename',NULL,'sync'),
('restore',NULL,'storage_task'),
('restore_process',NULL,'complex'),
('rewrite',NULL,'complex'),
('write',NULL,'storage_task');
/*!40000 ALTER TABLE `action` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `action_artifactclass_flow`
--

DROP TABLE IF EXISTS `action_artifactclass_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action_artifactclass_flow` (
  `artifactclass_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `flow_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `action_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  PRIMARY KEY (`action_id`,`artifactclass_id`,`flow_id`),
  CONSTRAINT `action_artifactclass_flow_ibfk_1` FOREIGN KEY (`action_id`) REFERENCES `action` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action_artifactclass_flow`
--

LOCK TABLES `action_artifactclass_flow` WRITE;
/*!40000 ALTER TABLE `action_artifactclass_flow` DISABLE KEYS */;
INSERT INTO `action_artifactclass_flow` (`artifactclass_id`, `flow_id`, `active`, `action_id`) VALUES
('audio','audio-proxy-flow',1,'ingest'),
('audio','cp-archive-flow',1,'ingest'),
('photo','cp-archive-flow',1,'ingest'),
('video','audio-extraction-flow',0,'ingest'),
('video','cp-archive-flow',1,'ingest'),
('video','video-mezz-proxy-flow',1,'ingest');
/*!40000 ALTER TABLE `action_artifactclass_flow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `action_artifactclass_user`
--

DROP TABLE IF EXISTS `action_artifactclass_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action_artifactclass_user` (
  `action_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `artifactclass_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`action_id`,`artifactclass_id`,`user_id`),
  KEY `FKqis923yolu1jdttrv43snq4dl` (`user_id`),
  KEY `artifactclass_id` (`artifactclass_id`),
  CONSTRAINT `FKqis923yolu1jdttrv43snq4dl` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `action_artifactclass_user_ibfk_1` FOREIGN KEY (`action_id`) REFERENCES `action` (`id`),
  CONSTRAINT `action_artifactclass_user_ibfk_2` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action_artifactclass_user`
--

LOCK TABLES `action_artifactclass_user` WRITE;
/*!40000 ALTER TABLE `action_artifactclass_user` DISABLE KEYS */;
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES
('ingest','audio',2),
('ingest','photo',2),
('ingest','video',2);
/*!40000 ALTER TABLE `action_artifactclass_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `archiveformat`
--

DROP TABLE IF EXISTS `archiveformat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `archiveformat` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `blocksize` int(11) DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `filesize_increase_const` int(11) DEFAULT NULL,
  `filesize_increase_rate` float DEFAULT NULL,
  `restore_verify` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archiveformat`
--

LOCK TABLES `archiveformat` WRITE;
/*!40000 ALTER TABLE `archiveformat` DISABLE KEYS */;
/*!40000 ALTER TABLE `archiveformat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifact`
--

DROP TABLE IF EXISTS `artifact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifact` (
  `id` int(11) NOT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `file_count` int(11) DEFAULT NULL,
  `file_structure_md5` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `prev_sequence_code` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `sequence_code` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `total_size` bigint(20) DEFAULT NULL,
  `artifactclass_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `q_latest_request_id` int(11) DEFAULT NULL,
  `write_request_id` int(11) DEFAULT NULL,
  `artifact_ref_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_97fwtvys0xoje0vs83reoqcx8` (`name`),
  KEY `IDXeydk5ttukyynoxm7iar76hj6t` (`total_size`),
  KEY `FKe0pe7q87rswnyu1fnswefshbg` (`q_latest_request_id`),
  KEY `FKc2kwrw5id2ivdsghou4it51iw` (`write_request_id`),
  KEY `FKoy3b1r1e9nkxpjcfuhorl86xp` (`artifact_ref_id`),
  KEY `artifactclass_id` (`artifactclass_id`),
  KEY `IDXgwh51cqjtrp3v2bxqkwbh59u` (`total_size`),
  CONSTRAINT `FKc2kwrw5id2ivdsghou4it51iw` FOREIGN KEY (`write_request_id`) REFERENCES `request` (`id`),
  CONSTRAINT `FKe0pe7q87rswnyu1fnswefshbg` FOREIGN KEY (`q_latest_request_id`) REFERENCES `request` (`id`),
  CONSTRAINT `FKoy3b1r1e9nkxpjcfuhorl86xp` FOREIGN KEY (`artifact_ref_id`) REFERENCES `artifact` (`id`),
  CONSTRAINT `artifact_ibfk_1` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifact`
--

LOCK TABLES `artifact` WRITE;
/*!40000 ALTER TABLE `artifact` DISABLE KEYS */;
/*!40000 ALTER TABLE `artifact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifact_label`
--

DROP TABLE IF EXISTS `artifact_label`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifact_label` (
  `tag` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `artifact_id` int(11) NOT NULL,
  PRIMARY KEY (`tag`,`artifact_id`),
  KEY `FKhq5nj7h5l37p33iubuxp77fny` (`artifact_id`),
  CONSTRAINT `FKhq5nj7h5l37p33iubuxp77fny` FOREIGN KEY (`artifact_id`) REFERENCES `artifact` (`id`),
  CONSTRAINT `FKiuiaqptaxnvnj1ef4ouhatrq9` FOREIGN KEY (`tag`) REFERENCES `label` (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifact_label`
--

LOCK TABLES `artifact_label` WRITE;
/*!40000 ALTER TABLE `artifact_label` DISABLE KEYS */;
/*!40000 ALTER TABLE `artifact_label` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifact_sequence`
--

DROP TABLE IF EXISTS `artifact_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifact_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifact_sequence`
--

LOCK TABLES `artifact_sequence` WRITE;
/*!40000 ALTER TABLE `artifact_sequence` DISABLE KEYS */;
INSERT INTO `artifact_sequence` (`next_val`) VALUES
(1),
(1);
/*!40000 ALTER TABLE `artifact_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifact_volume`
--

DROP TABLE IF EXISTS `artifact_volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifact_volume` (
  `artifact_id` int(11) NOT NULL,
  `details` json DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `job_id` int(11) DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`artifact_id`,`volume_id`),
  KEY `FK77jxrosqjrg29avfegxkyqqba` (`job_id`),
  KEY `volume_id` (`volume_id`),
  CONSTRAINT `FK77jxrosqjrg29avfegxkyqqba` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`),
  CONSTRAINT `artifact_volume_ibfk_1` FOREIGN KEY (`volume_id`) REFERENCES `volume` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifact_volume`
--

LOCK TABLES `artifact_volume` WRITE;
/*!40000 ALTER TABLE `artifact_volume` DISABLE KEYS */;
/*!40000 ALTER TABLE `artifact_volume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifactclass`
--

DROP TABLE IF EXISTS `artifactclass`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifactclass` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `concurrent_volume_copies` bit(1) DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `display_order` int(11) DEFAULT NULL,
  `import_only` bit(1) DEFAULT NULL,
  `path_prefix` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `source` bit(1) DEFAULT NULL,
  `artifactclass_ref_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `sequence_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `config` json DEFAULT NULL,
  `auto_ingest` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `artifactclass_ref_id` (`artifactclass_ref_id`),
  KEY `sequence_id` (`sequence_id`),
  CONSTRAINT `artifactclass_ibfk_1` FOREIGN KEY (`artifactclass_ref_id`) REFERENCES `artifactclass` (`id`),
  CONSTRAINT `artifactclass_ibfk_2` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifactclass`
--

LOCK TABLES `artifactclass` WRITE;
/*!40000 ALTER TABLE `artifactclass` DISABLE KEYS */;
INSERT INTO `artifactclass` (`id`, `concurrent_volume_copies`, `description`, `display_order`, `import_only`, `path_prefix`, `source`, `artifactclass_ref_id`, `sequence_id`, `config`, `auto_ingest`) VALUES
('audio',1,'audio',4,0,'staged',1,NULL,'audio',NULL,NULL),
('audio-encrypt',1,'encrypted audio',5,0,'encrypted',0,'audio','audio-encrypt',NULL,NULL),
('audio-proxy',1,'audio proxy',6,0,'transcoded',0,'audio','audio-proxy',NULL,NULL),
('photo',1,'photo',7,0,'staged',1,NULL,'photo',NULL,NULL),
('photo-encrypt',1,'photo',8,0,'encrypted',0,'photo','photo-encrypt',NULL,NULL),
('video',1,'high resolution video',1,0,'staged',1,NULL,'video',NULL,NULL),
('video-encrypt',1,'encrypted high resolution video',2,0,'encrypted',0,'video','video-encrypt',NULL,NULL),
('video-mezz-proxy',1,'mezzanine proxy video',3,0,'proxy',0,'video','video-mezz-proxy',NULL,NULL);
/*!40000 ALTER TABLE `artifactclass` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifactclass_destination`
--

DROP TABLE IF EXISTS `artifactclass_destination`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifactclass_destination` (
  `artifactclass_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `destination_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  PRIMARY KEY (`artifactclass_id`,`destination_id`),
  KEY `destination_id` (`destination_id`),
  CONSTRAINT `artifactclass_destination_ibfk_1` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`),
  CONSTRAINT `artifactclass_destination_ibfk_2` FOREIGN KEY (`destination_id`) REFERENCES `destination` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifactclass_destination`
--

LOCK TABLES `artifactclass_destination` WRITE;
/*!40000 ALTER TABLE `artifactclass_destination` DISABLE KEYS */;
/*!40000 ALTER TABLE `artifactclass_destination` ENABLE KEYS */;
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
  `artifactclass_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  PRIMARY KEY (`artifactclass_id`,`volume_id`),
  KEY `volume_id` (`volume_id`),
  CONSTRAINT `artifactclass_volume_ibfk_1` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`),
  CONSTRAINT `artifactclass_volume_ibfk_2` FOREIGN KEY (`volume_id`) REFERENCES `volume` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifactclass_volume`
--

LOCK TABLES `artifactclass_volume` WRITE;
/*!40000 ALTER TABLE `artifactclass_volume` DISABLE KEYS */;
INSERT INTO `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) VALUES
(1,0,'audio','A'),
(1,0,'audio-encrypt','B'),
(1,0,'audio-proxy','C'),
(1,0,'photo','A'),
(1,0,'photo-encrypt','B'),
(1,0,'video','A'),
(1,0,'video','B'),
(0,0,'video-encrypt','B'),
(1,0,'video-mezz-proxy','M');
/*!40000 ALTER TABLE `artifactclass_volume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `badfile`
--

DROP TABLE IF EXISTS `badfile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `badfile` (
  `id` int(11) NOT NULL,
  `reason` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `badfile`
--

LOCK TABLES `badfile` WRITE;
/*!40000 ALTER TABLE `badfile` DISABLE KEYS */;
/*!40000 ALTER TABLE `badfile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clip`
--

DROP TABLE IF EXISTS `clip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clip` (
  `id` int(11) NOT NULL,
  `clip_ref_id` int(11) DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `file_id` int(11) DEFAULT NULL,
  `fps` int(11) DEFAULT NULL,
  `in_time` int(11) DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `notes` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `out_time` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clip`
--

LOCK TABLES `clip` WRITE;
/*!40000 ALTER TABLE `clip` DISABLE KEYS */;
/*!40000 ALTER TABLE `clip` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clip_cliplist`
--

DROP TABLE IF EXISTS `clip_cliplist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clip_cliplist` (
  `id` int(11) NOT NULL,
  `clip_id` int(11) NOT NULL,
  `cliplist_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clip_cliplist`
--

LOCK TABLES `clip_cliplist` WRITE;
/*!40000 ALTER TABLE `clip_cliplist` DISABLE KEYS */;
/*!40000 ALTER TABLE `clip_cliplist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clip_list`
--

DROP TABLE IF EXISTS `clip_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clip_list` (
  `id` int(11) NOT NULL,
  `created_on` datetime(6) DEFAULT NULL,
  `createdby` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clip_list`
--

LOCK TABLES `clip_list` WRITE;
/*!40000 ALTER TABLE `clip_list` DISABLE KEYS */;
/*!40000 ALTER TABLE `clip_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clip_mamtag`
--

DROP TABLE IF EXISTS `clip_mamtag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clip_mamtag` (
  `id` int(11) NOT NULL,
  `clip_id` int(11) NOT NULL,
  `mamtag_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clip_mamtag`
--

LOCK TABLES `clip_mamtag` WRITE;
/*!40000 ALTER TABLE `clip_mamtag` DISABLE KEYS */;
/*!40000 ALTER TABLE `clip_mamtag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clip_tag`
--

DROP TABLE IF EXISTS `clip_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clip_tag` (
  `id` int(11) NOT NULL,
  `clip_id` int(11) NOT NULL,
  `tag_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clip_tag`
--

LOCK TABLES `clip_tag` WRITE;
/*!40000 ALTER TABLE `clip_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `clip_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `copy`
--

DROP TABLE IF EXISTS `copy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `copy` (
  `id` int(11) NOT NULL,
  `location_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `location_id` (`location_id`),
  CONSTRAINT `copy_ibfk_1` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `copy`
--

LOCK TABLES `copy` WRITE;
/*!40000 ALTER TABLE `copy` DISABLE KEYS */;
INSERT INTO `copy` (`id`, `location_id`) VALUES
(1,'sk-office1'),
(2,'t-block2'),
(3,'t-block3');
/*!40000 ALTER TABLE `copy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `destination`
--

DROP TABLE IF EXISTS `destination`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `destination` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `path` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `use_buffering` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_jx1awo0uegy8wrvmdwtjojkbm` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
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
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `defective` bit(1) DEFAULT NULL,
  `details` json DEFAULT NULL,
  `manufacturer` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `model` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `serial_number` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `warranty_expiry_date` datetime(6) DEFAULT NULL,
  `wwn_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `employed_date` datetime(6) DEFAULT NULL,
  `retired_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4776vaiywo1kdis4lp8jkm0av` (`serial_number`),
  UNIQUE KEY `UK_rybeolllge5fl2xeefjy2gi27` (`wwn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device`
--

LOCK TABLES `device` WRITE;
/*!40000 ALTER TABLE `device` DISABLE KEYS */;
/*!40000 ALTER TABLE `device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dwara_sequences`
--

DROP TABLE IF EXISTS `dwara_sequences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dwara_sequences` (
  `primary_key_fields` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `current_val` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`primary_key_fields`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dwara_sequences`
--

LOCK TABLES `dwara_sequences` WRITE;
/*!40000 ALTER TABLE `dwara_sequences` DISABLE KEYS */;
INSERT INTO `dwara_sequences` (`primary_key_fields`, `current_val`) VALUES
('import_id',0),
('job_id',0),
('request_id',0),
('t_activedevice_id',0);
/*!40000 ALTER TABLE `dwara_sequences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `error`
--

DROP TABLE IF EXISTS `error`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `error` (
  `id` int(11) NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
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
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `ignore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extension`
--

LOCK TABLES `extension` WRITE;
/*!40000 ALTER TABLE `extension` DISABLE KEYS */;
INSERT INTO `extension` (`id`, `description`, `ignore`) VALUES
('',NULL,NULL),
('~dbf',NULL,NULL),
('~prj',NULL,NULL),
('~rtree',NULL,NULL),
('~shp',NULL,NULL),
('~shx',NULL,NULL),
('050f',NULL,NULL),
('0538',NULL,NULL),
('1',NULL,NULL),
('16 In Conversation 98055269794',NULL,NULL),
('2',NULL,NULL),
('360',NULL,NULL),
('37 Dry Run Sakshi TV 98947221568',NULL,NULL),
('39 Dry Run Sakshi TV 98947221568',NULL,NULL),
('3gp',NULL,NULL),
('45 Dry Run Sakshi TV 98947221568',NULL,NULL),
('46 In Conversation 98055269794',NULL,NULL),
('47 Dry Run Sakshi TV 98947221568',NULL,NULL),
('51',NULL,NULL),
('aac',NULL,NULL),
('AAE',NULL,NULL),
('aecb',NULL,NULL),
('aep',NULL,NULL),
('ai',NULL,NULL),
('AIS',NULL,NULL),
('apk',NULL,NULL),
('arw','Image captured by a Sony digital camera, based on the TIFF specification; contains raw, uncompressed image data. (Different Sony camera models may store raw images using the same \".arw\" file extension, but with a different format).',0),
('avi',NULL,NULL),
('avj',NULL,NULL),
('avu',NULL,NULL),
('backup',NULL,NULL),
('BAK',NULL,NULL),
('bdm','Sony binary information file created by AVCHD video camera',1),
('bim','Sony XDCAM real-time metadata file, which contains timecode and codec information designed to be read by an application during playback and, as such, is not human readable.',1),
('bin','Generic binary file',1),
('bk',NULL,NULL),
('BNP',NULL,NULL),
('BridgeLabelsAndRatings',NULL,NULL),
('BridgeSort',NULL,NULL),
('BUP',NULL,NULL),
('Cab',NULL,NULL),
('caf',NULL,NULL),
('calib',NULL,NULL),
('camcalib',NULL,NULL),
('CED',NULL,NULL),
('CFG',NULL,NULL),
('classid',NULL,NULL),
('conf',NULL,NULL),
('CPF',NULL,NULL),
('cpg',NULL,NULL),
('cpi','Sony binary metadata file corresponding to an AVCHD video clip',1),
('cr2','Canon RAW Image file',0),
('cr3','Canon Raw image format - introduced in 2018',NULL),
('created',NULL,NULL),
('crw','Canon Raw image format - superceded by CR2 format',NULL),
('csv',NULL,NULL),
('ctg','Catalog index file created by Canon cameras. Contains info about the number of images stored in each folder on a memory card',1),
('cube',NULL,NULL),
('DAT',NULL,NULL),
('db',NULL,NULL),
('dbf',NULL,NULL),
('device',NULL,NULL),
('directoryStoreFile',NULL,NULL),
('divx',NULL,NULL),
('dng','Universal RAW image format for saving digital photos in an uncompressed format',0),
('doc',NULL,NULL),
('docx',NULL,NULL),
('download',NULL,NULL),
('drp',NULL,NULL),
('drx',NULL,NULL),
('ds_store',NULL,NULL),
('dvd',NULL,NULL),
('evt',NULL,NULL),
('exe',NULL,NULL),
('fcp',NULL,NULL),
('fcpbun-BwPkvs',NULL,NULL),
('fcpbun-StHtLV',NULL,NULL),
('fcpbundl-2yerjl',NULL,NULL),
('fcpevent',NULL,NULL),
('fcpxml',NULL,NULL),
('fff','Hasselblad Raw image format',NULL),
('flexolibrary',NULL,NULL),
('fls',NULL,NULL),
('ftr','File footer',0),
('gif',NULL,NULL),
('gis',NULL,NULL),
('gps',NULL,NULL),
('hdr','File header',0),
('heic','High Efficiency Image Format (HEIF), a file format commonly used to store photos on mobile devices. It may contain a single image or a sequence of images along with corresponding metadata. HEIC files may also appear as .HEIF files.',0),
('HIF',NULL,NULL),
('hls',NULL,NULL),
('hprj',NULL,NULL),
('htm',NULL,NULL),
('html',NULL,NULL),
('hvc1',NULL,NULL),
('idx','PFR index file',0),
('IFO',NULL,NULL),
('img',NULL,NULL),
('ind','Sony file placed on a media card when formatted with a Sony device',1),
('indexArrays',NULL,NULL),
('indexCompactDirectory',NULL,NULL),
('indexDirectory',NULL,NULL),
('indexGroups',NULL,NULL),
('indexHead',NULL,NULL),
('indexIds',NULL,NULL),
('indexPositions',NULL,NULL),
('indexPositionTable',NULL,NULL),
('indexPostings',NULL,NULL),
('indexTermIds',NULL,NULL),
('indexUpdates',NULL,NULL),
('ini',NULL,NULL),
('INP',NULL,NULL),
('insp',NULL,NULL),
('insv',NULL,NULL),
('INT',NULL,NULL),
('intermediate',NULL,NULL),
('jfif',NULL,NULL),
('jpeg',NULL,NULL),
('jpg','',0),
('JSN',NULL,NULL),
('Lion',NULL,NULL),
('list',NULL,NULL),
('lnk',NULL,NULL),
('loc',NULL,NULL),
('lock',NULL,NULL),
('lock-info',NULL,NULL),
('log','Log file',0),
('lpcontainer',NULL,NULL),
('lpindex',NULL,NULL),
('LRF',NULL,NULL),
('lrtpreview',NULL,NULL),
('lrv','GoPro low resolution video proxy',1),
('m3u','Multimedia playlist',1),
('m4a','MPEG-4 format audio file encoded with Advanced Audio Coding (AAC) codec',0),
('m4v',NULL,NULL),
('map',NULL,NULL),
('md5','MD5 checksum',0),
('mhl',NULL,NULL),
('MIF',NULL,NULL),
('mkv','Matroska',0),
('modified',NULL,NULL),
('mov','',0),
('mov_thumbnail','Thumbnail picture for a mov video file',1),
('MP2',NULL,NULL),
('mp3','',0),
('mp4',NULL,NULL),
('mp4_ffprobe_out','',0),
('mpeg',NULL,NULL),
('mpg',NULL,NULL),
('mpl','Sony binary playlist file created by AVCHD video camera',1),
('mts','Advanced Video Coding High Definition (AVCHD) video file',0),
('mxf','',0),
('NEF',NULL,NULL),
('nrw','Nikon Raw image format - similar to NEF but supports more features',NULL),
('ogg','',NULL),
('opt',NULL,NULL),
('ort',NULL,NULL),
('orttrj',NULL,NULL),
('otf',NULL,NULL),
('pdf',NULL,NULL),
('pfncopy',NULL,NULL),
('pkf',NULL,NULL),
('plist',NULL,NULL),
('png',' Portable Network Graphic (PNG) format. Contains a bitmap compressed with lossless compression similar to a .GIF file.  ',0),
('ppn','Sony picture pointer file, not human readable, which includes the position of each frame. The MPEG-2 codec used by XDCAM EX, utilizes variable bit rate encoding, so the positions of frames are not as easy to determine as with constant bit rate encoding.',1),
('pptx',NULL,NULL),
('prj',NULL,NULL),
('properties',NULL,NULL),
('prproj',NULL,NULL),
('psd','photoshop image',NULL),
('qc','Digitization Quality Control file',0),
('RAF',NULL,NULL),
('reapeaks',NULL,NULL),
('report',NULL,NULL),
('RPL',NULL,NULL),
('rtree',NULL,NULL),
('sav','GoPro file (purpose unknown)',1),
('SCR',NULL,NULL),
('ses',NULL,NULL),
('sha',NULL,NULL),
('shadow',NULL,NULL),
('shadowIndexArrays',NULL,NULL),
('shadowIndexCompactDirectory',NULL,NULL),
('shadowIndexDirectory',NULL,NULL),
('shadowIndexGroups',NULL,NULL),
('shadowIndexHead',NULL,NULL),
('shadowIndexPositionTable',NULL,NULL),
('shadowIndexTermIds',NULL,NULL),
('shp',NULL,NULL),
('shx',NULL,NULL),
('sig',NULL,NULL),
('smbdeleteAAA100000005d9f0b',NULL,NULL),
('smbdeleteAAA230000004fd4da',NULL,NULL),
('smbdeleteAAA380000005207b9',NULL,NULL),
('smi','Sony XDCAM clip information file: contains links to the mp4 and bim files, information on the audio and video codecs, and the clip\'s starting and ending timecode values',1),
('SnowLeopard',NULL,NULL),
('spu',NULL,NULL),
('sr2','One of the Sony Raw image file formats',NULL),
('srf','One of the Sony Raw image file formats',NULL),
('SRT',NULL,NULL),
('state',NULL,NULL),
('supported',NULL,NULL),
('tag',NULL,NULL),
('thm','GoPro thumbnail image',1),
('tif','tagged image file format',NULL),
('tiff',NULL,NULL),
('tmRecordhdr',NULL,NULL),
('trg',NULL,NULL),
('ts',NULL,NULL),
('ttf',NULL,NULL),
('txt',NULL,NULL),
('updates',NULL,NULL),
('url',NULL,NULL),
('VOB',NULL,NULL),
('VR_dji_iILZy7',NULL,NULL),
('vtt',NULL,NULL),
('wav','',0),
('webm',NULL,NULL),
('xlsx',NULL,NULL),
('xml','Extensible Metadata Language format commonly used for metadata',1),
('xmp',NULL,NULL),
('zfs',NULL,NULL),
('zip',NULL,NULL),
('zoom',NULL,NULL);
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
  `extension_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `filetype_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `suffix` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`extension_id`,`filetype_id`),
  KEY `filetype_id` (`filetype_id`),
  CONSTRAINT `extension_filetype_ibfk_1` FOREIGN KEY (`extension_id`) REFERENCES `extension` (`id`),
  CONSTRAINT `extension_filetype_ibfk_2` FOREIGN KEY (`filetype_id`) REFERENCES `filetype` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extension_filetype`
--

LOCK TABLES `extension_filetype` WRITE;
/*!40000 ALTER TABLE `extension_filetype` DISABLE KEYS */;
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`, `suffix`) VALUES
(0,'aac','audio',NULL),
(0,'aac','audio-video',NULL),
(0,'arw','image',NULL),
(0,'avi','audio-video',NULL),
(0,'avi','video',NULL),
(0,'cr2','image',NULL),
(0,'cr3','image',NULL),
(0,'crw','image',NULL),
(0,'dng','image',NULL),
(0,'fff','image',NULL),
(1,'ftr','video-digi-2020-src',NULL),
(1,'hdr','video-digi-2020-mkv-ffv1',NULL),
(1,'hdr','video-digi-2020-src',NULL),
(1,'hdr','video-proxy',NULL),
(0,'heic','image',NULL),
(1,'idx','video-digi-2020-mkv-ffv1',NULL),
(1,'idx','video-proxy',NULL),
(0,'jpg','image',NULL),
(0,'jpg','photo-proxy','_p'),
(1,'jpg','video-proxy',NULL),
(1,'log','video-digi-2020-src',NULL),
(0,'m4a','audio',NULL),
(0,'m4a','audio-video',NULL),
(1,'md5','video-digi-2020-src',NULL),
(0,'mkv','audio-video',NULL),
(0,'mkv','mkv',NULL),
(0,'mkv','video',NULL),
(0,'mkv','video-digi-2020-mkv-ffv1',NULL),
(0,'mkv','video-digi-2020-mkv-h264',NULL),
(0,'mov','audio-video',NULL),
(0,'mov','video',NULL),
(0,'mov','video-digi-2020-src',NULL),
(0,'mp3','audio',NULL),
(0,'mp3','audio-proxy',NULL),
(0,'mp3','audio-video',NULL),
(0,'mp4','audio-video',NULL),
(0,'mp4','video',NULL),
(0,'mp4','video-proxy',NULL),
(1,'mp4_ffprobe_out','video-proxy',NULL),
(0,'mts','audio-video',NULL),
(0,'mts','video',NULL),
(0,'mxf','audio-video',NULL),
(0,'mxf','mxf',NULL),
(0,'mxf','video',NULL),
(0,'mxf','video-digi-2020-src',NULL),
(0,'nef','image',NULL),
(0,'nrw','image',NULL),
(0,'ogg','audio',NULL),
(0,'ogg','audio-video',NULL),
(0,'png','image',NULL),
(0,'psd','image',NULL),
(1,'qc','video-digi-2020-src',NULL),
(0,'sr2','image',NULL),
(0,'srf','image',NULL),
(1,'thm','photo-proxy',NULL),
(0,'tif','image',NULL),
(0,'wav','audio',NULL),
(0,'wav','audio-video',NULL),
(1,'xmp','image',NULL),
(1,'xmp','photo-proxy',NULL);
/*!40000 ALTER TABLE `extension_filetype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file`
--

DROP TABLE IF EXISTS `file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `file` (
  `id` int(11) NOT NULL,
  `checksum` varbinary(32) DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `pathname` varchar(4096) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `pathname_checksum` varbinary(20) DEFAULT NULL,
  `size` bigint(20) DEFAULT NULL,
  `artifact_id` int(11) DEFAULT NULL,
  `file_ref_id` int(11) DEFAULT NULL,
  `directory` bit(1) DEFAULT NULL,
  `symlink_file_id` int(11) DEFAULT NULL,
  `symlink_path` varchar(4096) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `bad` bit(1) DEFAULT NULL,
  `reason` longtext COLLATE utf8mb4_unicode_520_ci,
  `diff` varchar(1) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pathname_checksum_UNIQUE` (`pathname_checksum`),
  KEY `FK7hv10ce46q97ed4myv5mojtkn` (`artifact_id`),
  KEY `FK86dptfka0rvlen44s6acy4sgl` (`file_ref_id`),
  CONSTRAINT `FK7hv10ce46q97ed4myv5mojtkn` FOREIGN KEY (`artifact_id`) REFERENCES `artifact` (`id`),
  CONSTRAINT `FK86dptfka0rvlen44s6acy4sgl` FOREIGN KEY (`file_ref_id`) REFERENCES `file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file`
--

LOCK TABLES `file` WRITE;
/*!40000 ALTER TABLE `file` DISABLE KEYS */;
/*!40000 ALTER TABLE `file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_sequence`
--

DROP TABLE IF EXISTS `file_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `file_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_sequence`
--

LOCK TABLES `file_sequence` WRITE;
/*!40000 ALTER TABLE `file_sequence` DISABLE KEYS */;
INSERT INTO `file_sequence` (`next_val`) VALUES
(1),
(1);
/*!40000 ALTER TABLE `file_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_volume`
--

DROP TABLE IF EXISTS `file_volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `file_volume` (
  `file_id` int(11) NOT NULL,
  `archive_block` bigint(20) DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `encrypted` bit(1) DEFAULT NULL,
  `verified_at` datetime(6) DEFAULT NULL,
  `volume_start_block` int(11) DEFAULT NULL,
  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `header_blocks` int(11) DEFAULT NULL,
  `hardlink_file_id` int(11) DEFAULT NULL,
  `volume_end_block` int(11) DEFAULT NULL,
  `diff` varchar(1) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`file_id`,`volume_id`),
  KEY `volume_id` (`volume_id`),
  CONSTRAINT `file_volume_ibfk_1` FOREIGN KEY (`volume_id`) REFERENCES `volume` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_volume`
--

LOCK TABLES `file_volume` WRITE;
/*!40000 ALTER TABLE `file_volume` DISABLE KEYS */;
/*!40000 ALTER TABLE `file_volume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_volume_diff`
--

DROP TABLE IF EXISTS `file_volume_diff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `file_volume_diff` (
  `file_id` int(11) NOT NULL,
  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `checksum` tinyblob,
  `size` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`file_id`,`volume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_volume_diff`
--

LOCK TABLES `file_volume_diff` WRITE;
/*!40000 ALTER TABLE `file_volume_diff` DISABLE KEYS */;
/*!40000 ALTER TABLE `file_volume_diff` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `filetype`
--

DROP TABLE IF EXISTS `filetype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `filetype` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `filetype`
--

LOCK TABLES `filetype` WRITE;
/*!40000 ALTER TABLE `filetype` DISABLE KEYS */;
INSERT INTO `filetype` (`id`, `description`) VALUES
('audio','Audio Files'),
('audio-proxy','Audio proxy files'),
('audio-video','combination of both audio n video'),
('image','Image files'),
('mkv','Matroska video file'),
('mxf','MXF Video File'),
('photo-proxy','Photo proxy files'),
('video','Video Files'),
('video-digi-2020-mkv-ffv1','Digitized miniDV files, Matroska compressed ffv1 for preservation'),
('video-digi-2020-mkv-h264','Digitized miniDV files, Matroska compresssed h264 for QC'),
('video-digi-2020-src','Digitized miniDV files, MXF uncompressed v210 original'),
('video-proxy','Video Proxy Files');
/*!40000 ALTER TABLE `filetype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flow`
--

DROP TABLE IF EXISTS `flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flow` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flow`
--

LOCK TABLES `flow` WRITE;
/*!40000 ALTER TABLE `flow` DISABLE KEYS */;
INSERT INTO `flow` (`id`, `description`) VALUES
('audio-extraction-flow','extract audio from video, copy to gdrive'),
('audio-proxy-flow','gen-proxy, archive'),
('cp-archive-flow','cksum-gen, write, verify'),
('encryption-flow','encrypted-gen, encrypted-cksum-gen, decrypt, verify-decrypt, write, verify'),
('video-mezz-proxy-flow','copy, write, verify');
/*!40000 ALTER TABLE `flow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowelement`
--

DROP TABLE IF EXISTS `flowelement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flowelement` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `dependencies` json DEFAULT NULL,
  `deprecated` bit(1) DEFAULT NULL,
  `display_order` int(11) DEFAULT NULL,
  `flow_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `flow_ref_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `processingtask_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `storagetask_action_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `task_config` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowelement`
--

LOCK TABLES `flowelement` WRITE;
/*!40000 ALTER TABLE `flowelement` DISABLE KEYS */;
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `flow_ref_id`, `processingtask_id`, `storagetask_action_id`, `task_config`) VALUES
('U1',1,NULL,0,1,'cp-archive-flow',NULL,'checksum-gen',NULL,NULL),
('U2',1,NULL,0,2,'cp-archive-flow',NULL,NULL,'write',NULL),
('U3',1,'[\"U1\", \"U2\"]',0,3,'cp-archive-flow',NULL,'checksum-verify',NULL,NULL),
('U4',0,'[\"U1\"]',0,4,'cp-archive-flow','encryption-flow',NULL,NULL,NULL),
('U5',1,NULL,0,5,'video-mezz-proxy-flow',NULL,'mezz-proxy-handler',NULL,'{\"exclude_if\": {\"artifactname_regex\": \".*(_Insta360).*\"}}'),
-- ('U5',1,NULL,0,5,'video-mezz-proxy-flow',NULL,'mezz-proxy-file-copy',NULL,'{\"pathname_regex\": \"M4ROOT/SUB/.*\", \"include_if\": {\"artifactname_regex\": \".*(_FX3|_A7S3).*\"}}'),
-- ('U6',1,NULL,0,6,'video-mezz-proxy-flow',NULL,'mezz-proxy-gen',NULL,'{\"include_if\": {\"artifactname_regex\": \".*(_FS7|_Drone|_Insta360|_GoPro).*\"}}'),
('U7',1,'[\"U5\"]',0,7,'video-mezz-proxy-flow','cp-archive-flow',NULL,NULL,NULL),
('U11',1,NULL,0,11,'audio-proxy-flow',NULL,'audio-proxy-low-gen',NULL,NULL),
('U12',1,'[\"U11\"]',0,12,'audio-proxy-flow','cp-archive-flow',NULL,NULL,NULL),
/*!40000 ALTER TABLE `flowelement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` (`next_val`) VALUES
(1),
(1),
(1),
(1),
(1);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `import`
--

DROP TABLE IF EXISTS `import`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `import` (
  `id` int(11) NOT NULL,
  `artifact_id` int(11) DEFAULT NULL,
  `artifact_name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `message` longtext COLLATE utf8mb4_unicode_520_ci,
  `requeue_id` int(11) DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `request_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpbitdw04d7tuwii88j89166gs` (`request_id`),
  CONSTRAINT `FKpbitdw04d7tuwii88j89166gs` FOREIGN KEY (`request_id`) REFERENCES `request` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `import`
--

LOCK TABLES `import` WRITE;
/*!40000 ALTER TABLE `import` DISABLE KEYS */;
/*!40000 ALTER TABLE `import` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job` (
  `id` int(11) NOT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `dependencies` json DEFAULT NULL,
  `encrypted` bit(1) DEFAULT NULL,
  `input_artifact_id` int(11) DEFAULT NULL,
  `message` longtext COLLATE utf8mb4_unicode_520_ci,
  `output_artifact_id` int(11) DEFAULT NULL,
  `processingtask_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `storagetask_action_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `device_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `flowelement_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `group_volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `request_id` int(11) DEFAULT NULL,
  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK60kq6m4aqdk9l351noyqo2ega` (`request_id`),
  KEY `group_volume_id` (`group_volume_id`),
  KEY `volume_id` (`volume_id`),
  KEY `device_id` (`device_id`),
  CONSTRAINT `FK60kq6m4aqdk9l351noyqo2ega` FOREIGN KEY (`request_id`) REFERENCES `request` (`id`),
  CONSTRAINT `job_ibfk_1` FOREIGN KEY (`group_volume_id`) REFERENCES `volume` (`id`),
  CONSTRAINT `job_ibfk_2` FOREIGN KEY (`volume_id`) REFERENCES `volume` (`id`),
  CONSTRAINT `job_ibfk_3` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job`
--

LOCK TABLES `job` WRITE;
/*!40000 ALTER TABLE `job` DISABLE KEYS */;
/*!40000 ALTER TABLE `job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jobrun`
--

DROP TABLE IF EXISTS `jobrun`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jobrun` (
  `requeue_id` int(11) NOT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `message` longtext COLLATE utf8mb4_unicode_520_ci,
  `started_at` datetime(6) DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `job_id` int(11) NOT NULL,
  `device_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`job_id`,`requeue_id`),
  KEY `volume_id` (`volume_id`),
  KEY `device_id` (`device_id`),
  CONSTRAINT `FKkwxotxdln7suts4rprbmctu83` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`),
  CONSTRAINT `jobrun_ibfk_1` FOREIGN KEY (`volume_id`) REFERENCES `volume` (`id`),
  CONSTRAINT `jobrun_ibfk_2` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jobrun`
--

LOCK TABLES `jobrun` WRITE;
/*!40000 ALTER TABLE `jobrun` DISABLE KEYS */;
/*!40000 ALTER TABLE `jobrun` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `label`
--

DROP TABLE IF EXISTS `label`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `label` (
  `tag` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  PRIMARY KEY (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `label`
--

LOCK TABLES `label` WRITE;
/*!40000 ALTER TABLE `label` DISABLE KEYS */;
/*!40000 ALTER TABLE `label` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `default` bit(1) DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
INSERT INTO `location` (`id`, `default`, `description`) VALUES
('lto-room',0,'LTO Room'),
('rally',1,'CP team on Road'),
('sk-office1',0,'SK Office - 1st'),
('t-block2',0,'T Block - 2nd'),
('t-block3',0,'T Block - 3rd');
/*!40000 ALTER TABLE `location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mamtag`
--

DROP TABLE IF EXISTS `mamtag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mamtag` (
  `id` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mamtag`
--

LOCK TABLES `mamtag` WRITE;
/*!40000 ALTER TABLE `mamtag` DISABLE KEYS */;
/*!40000 ALTER TABLE `mamtag` ENABLE KEYS */;
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
  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `optimize_tape_access` bit(1) DEFAULT NULL,
  `start` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ash8838axsg24ngytu9ktcu9` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `priorityband`
--

LOCK TABLES `priorityband` WRITE;
/*!40000 ALTER TABLE `priorityband` DISABLE KEYS */;
INSERT INTO `priorityband` (`id`, `end`, `name`, `optimize_tape_access`, `start`) VALUES
(1,10,'',1,5);
/*!40000 ALTER TABLE `priorityband` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `processingfailure`
--

DROP TABLE IF EXISTS `processingfailure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `processingfailure` (
  `file_id` int(11) NOT NULL,
  `reason` longtext COLLATE utf8mb4_unicode_520_ci,
  `job_id` int(11) NOT NULL,
  PRIMARY KEY (`file_id`,`job_id`),
  KEY `FK7w30yvbgwauw4am3nrvtufrrn` (`job_id`),
  CONSTRAINT `FK7w30yvbgwauw4am3nrvtufrrn` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `processingfailure`
--

LOCK TABLES `processingfailure` WRITE;
/*!40000 ALTER TABLE `processingfailure` DISABLE KEYS */;
/*!40000 ALTER TABLE `processingfailure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `processingtask`
--

DROP TABLE IF EXISTS `processingtask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `processingtask` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `filetype_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `max_errors` int(11) DEFAULT NULL,
  `output_artifactclass` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `output_filetype_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `processingtask`
--

LOCK TABLES `processingtask` WRITE;
/*!40000 ALTER TABLE `processingtask` DISABLE KEYS */;
INSERT INTO `processingtask` (`id`, `description`, `filetype_id`, `max_errors`, `output_artifactclass`, `output_filetype_id`) VALUES
('audio-proxy-low-gen','generate audio proxies','audio-video',1,'audio-proxy',NULL),
('copy-gdrive','copies the artifact to gdrive','_all_',1,NULL,NULL),
('decrypted-gen','decrypt encrypted files','_all_',1,'video',NULL),
('encrypted-gen','generate encrypted files','_all_',1,'video-encrypt',NULL),
-- ('mezz-proxy-file-copy','copies a subset of the artifact to a different folder in local','video',1,'video-mezz-proxy',NULL),
-- ('mezz-proxy-gen','generate mezz proxies','video',1,'video-mezz-proxy',NULL),
('mezz-proxy-handler','for certain type of artifacts copies a subset of the artifact to a different folder and for certain other type generates mezz proxies','video',1,'video-mezz-proxy',NULL);

/*!40000 ALTER TABLE `processingtask` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request`
--

DROP TABLE IF EXISTS `request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request` (
  `id` int(11) NOT NULL,
  `action_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `details` json DEFAULT NULL,
  `file_id` int(11) GENERATED ALWAYS AS (json_extract(`details`,'$.file_id')) STORED,
  `external_ref` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `requested_at` datetime(6) DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `request_ref_id` int(11) DEFAULT NULL,
  `requested_by_id` int(11) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `message` longtext COLLATE utf8mb4_unicode_520_ci,
  `priority` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3x31onos55r2f8ql8e61e1mtl` (`request_ref_id`),
  KEY `FKe1oxajjb60tj4ehjm3d7kbiai` (`requested_by_id`),
  KEY `file_id` (`file_id`),
  CONSTRAINT `FK3x31onos55r2f8ql8e61e1mtl` FOREIGN KEY (`request_ref_id`) REFERENCES `request` (`id`),
  CONSTRAINT `FKe1oxajjb60tj4ehjm3d7kbiai` FOREIGN KEY (`requested_by_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request`
--

LOCK TABLES `request` WRITE;
/*!40000 ALTER TABLE `request` DISABLE KEYS */;
/*!40000 ALTER TABLE `request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_approval`
--

DROP TABLE IF EXISTS `request_approval`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request_approval` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `approval_date` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `approval_status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `approver` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `approver_email` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` int(11) DEFAULT NULL,
  `destination_path` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `details` json DEFAULT NULL,
  `external_refid` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `priority` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `requested_by` int(11) DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_approval`
--

LOCK TABLES `request_approval` WRITE;
/*!40000 ALTER TABLE `request_approval` DISABLE KEYS */;
/*!40000 ALTER TABLE `request_approval` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_tag`
--

DROP TABLE IF EXISTS `request_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request_tag` (
  `tag` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `request_id` int(11) NOT NULL,
  PRIMARY KEY (`tag`,`request_id`),
  KEY `FK4qg9kwomafwde09eeel2cjtyt` (`request_id`),
  CONSTRAINT `FK4qg9kwomafwde09eeel2cjtyt` FOREIGN KEY (`request_id`) REFERENCES `request` (`id`),
  CONSTRAINT `FKf5onaci18u8pst8yx2idvtxta` FOREIGN KEY (`tag`) REFERENCES `tag` (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_tag`
--

LOCK TABLES `request_tag` WRITE;
/*!40000 ALTER TABLE `request_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `request_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `id` int(11) NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_8sewwnpamngi6b1dwaa88askk` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` (`id`, `description`, `name`) VALUES
(1,'Administrator - has all priviliges','admin'),
(2,'Users with ingest previliges','ingester'),
(3,'Users with restore previliges','restorer'),
(4,'Read only privileges','readonly');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_user`
--

DROP TABLE IF EXISTS `role_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role_user` (
  `role_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`role_id`,`user_id`),
  KEY `FK4320p8bgvumlxjkohtbj214qi` (`user_id`),
  CONSTRAINT `FK4320p8bgvumlxjkohtbj214qi` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKiqpmjd2qb4rdkej916ymonic6` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_user`
--

LOCK TABLES `role_user` WRITE;
/*!40000 ALTER TABLE `role_user` DISABLE KEYS */;
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES
(1,2),
(2,2),
(3,2),
(1,3),
(2,3),
(3,3),
(1,4),
(2,4),
(3,4);
/*!40000 ALTER TABLE `role_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `current_number` int(11) DEFAULT NULL,
  `ending_number` int(11) DEFAULT NULL,
  `group` bit(1) DEFAULT NULL,
  `prefix` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `starting_number` int(11) DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `sequence_ref_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `sequence_ref_id` (`sequence_ref_id`),
  CONSTRAINT `sequence_ibfk_1` FOREIGN KEY (`sequence_ref_id`) REFERENCES `sequence` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sequence`
--

LOCK TABLES `sequence` WRITE;
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `group`, `prefix`, `starting_number`, `type`, `sequence_ref_id`) VALUES
('audio',0,-1,0,'A',1,'artifact',NULL),
('audio-encrypt',0,-1,0,'AE',1,'artifact',NULL),
('audio-proxy',0,-1,0,'AL',1,'artifact',NULL),
('high-res',100,200,0,'A',1,'volume',NULL),
('high-res-encrypted',100,200,0,'B',1,'volume',NULL),
('photo',0,-1,0,'P',1,'artifact',NULL),
('photo-encrypt',0,-1,0,'PE',1,'artifact',NULL),
('proxy',100,200,0,'M',1,'volume',NULL),
('video',0,-1,0,'V',1,'artifact',NULL),
('video-encrypt',0,-1,0,'VE',1,'artifact',NULL),
('video-mezz-proxy',0,-1,0,'VM',1,'artifact',NULL);
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_activedevice`
--

DROP TABLE IF EXISTS `t_activedevice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_activedevice` (
  `id` int(11) NOT NULL,
  `device_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `job_id` int(11) DEFAULT NULL,
  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsivvtx1lmsnaq9tqp1iqnntin` (`job_id`),
  KEY `volume_id` (`volume_id`),
  KEY `device_id` (`device_id`),
  CONSTRAINT `FKsivvtx1lmsnaq9tqp1iqnntin` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`),
  CONSTRAINT `t_activedevice_ibfk_1` FOREIGN KEY (`volume_id`) REFERENCES `volume` (`id`),
  CONSTRAINT `t_activedevice_ibfk_2` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_activedevice`
--

LOCK TABLES `t_activedevice` WRITE;
/*!40000 ALTER TABLE `t_activedevice` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_activedevice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_file`
--

DROP TABLE IF EXISTS `t_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_file` (
  `id` int(11) NOT NULL,
  `checksum` varbinary(32) DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `directory` bit(1) DEFAULT NULL,
  `pathname` varchar(4096) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `size` bigint(20) DEFAULT NULL,
  `artifact_id` int(11) DEFAULT NULL,
  `file_ref_id` int(11) DEFAULT NULL,
  `pathname_checksum` varbinary(20) DEFAULT NULL,
  `symlink_file_id` int(11) DEFAULT NULL,
  `symlink_path` varchar(4096) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `bad` bit(1) DEFAULT NULL,
  `reason` longtext COLLATE utf8mb4_unicode_520_ci,
  `diff` varchar(1) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pathname_checksum_UNIQUE` (`pathname_checksum`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_file`
--

LOCK TABLES `t_file` WRITE;
/*!40000 ALTER TABLE `t_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_file_job`
--

DROP TABLE IF EXISTS `t_file_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_file_job` (
  `file_id` int(11) NOT NULL,
  `artifact_id` int(11) DEFAULT NULL,
  `pid` int(11) DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `job_id` int(11) NOT NULL,
  PRIMARY KEY (`file_id`,`job_id`),
  KEY `FK1kml96e6bp0oef8qh5o2ikd6k` (`job_id`),
  CONSTRAINT `FK1kml96e6bp0oef8qh5o2ikd6k` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_file_job`
--

LOCK TABLES `t_file_job` WRITE;
/*!40000 ALTER TABLE `t_file_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_file_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_file_sequence`
--

DROP TABLE IF EXISTS `t_file_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_file_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_file_sequence`
--

LOCK TABLES `t_file_sequence` WRITE;
/*!40000 ALTER TABLE `t_file_sequence` DISABLE KEYS */;
INSERT INTO `t_file_sequence` (`next_val`) VALUES
(1);
/*!40000 ALTER TABLE `t_file_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_file_volume`
--

DROP TABLE IF EXISTS `t_file_volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_file_volume` (
  `file_id` int(11) NOT NULL,
  `archive_block` bigint(20) DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `encrypted` bit(1) DEFAULT NULL,
  `header_blocks` int(11) DEFAULT NULL,
  `verified_at` datetime(6) DEFAULT NULL,
  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `hardlink_file_id` int(11) DEFAULT NULL,
  `volume_end_block` int(11) DEFAULT NULL,
  `volume_start_block` int(11) DEFAULT NULL,
  `diff` varchar(1) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`file_id`,`volume_id`),
  KEY `FKh7kyf6itjkgd0g2bjvxf52os` (`volume_id`),
  CONSTRAINT `FKh7kyf6itjkgd0g2bjvxf52os` FOREIGN KEY (`volume_id`) REFERENCES `volume` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_file_volume`
--

LOCK TABLES `t_file_volume` WRITE;
/*!40000 ALTER TABLE `t_file_volume` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_file_volume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_restorebucket`
--

DROP TABLE IF EXISTS `t_restorebucket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_restorebucket` (
  `id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `approval_date` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `approval_status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `approver` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `approver_email` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` int(11) DEFAULT NULL,
  `destination_path` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `details` json DEFAULT NULL,
  `external_refid` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `priority` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `requested_by` int(11) DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_restorebucket`
--

LOCK TABLES `t_restorebucket` WRITE;
/*!40000 ALTER TABLE `t_restorebucket` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_restorebucket` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_t_file_job`
--

DROP TABLE IF EXISTS `t_t_file_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_t_file_job` (
  `file_id` int(11) NOT NULL,
  `artifact_id` int(11) DEFAULT NULL,
  `pid` int(11) DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `job_id` int(11) NOT NULL,
  PRIMARY KEY (`file_id`,`job_id`),
  KEY `FKfaxo06v1ggucav5tws5wupxhy` (`job_id`),
  CONSTRAINT `FKfaxo06v1ggucav5tws5wupxhy` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_t_file_job`
--

LOCK TABLES `t_t_file_job` WRITE;
/*!40000 ALTER TABLE `t_t_file_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_t_file_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tag` (
  `tag` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  PRIMARY KEY (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `hash` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `priorityband_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gj2fy3dcix7ph7k8684gka40c` (`name`),
  KEY `FK73fruhbqpgrll696tv7y2ygxh` (`priorityband_id`),
  CONSTRAINT `FK73fruhbqpgrll696tv7y2ygxh` FOREIGN KEY (`priorityband_id`) REFERENCES `priorityband` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` (`id`, `email`, `hash`, `name`, `priorityband_id`) VALUES
(1,NULL,NULL,'dwara',1),
(2,'swami.kevala@ishafoundation.org','$2a$10$pAdeP0JmEbI01uQ75GQ09O0WxFIorj.eyOhy59sXAZpQ6IUHLRmCC','swamikevala',1),
-- Shambho!
(3,'prakash.gurumurthy@ishafoundation.org','$2a$10$bEzYpk//yR1er/0PmJD8y.uYC8WqpJIx8cvrTzZyxOou2llIokEt2','pgurumurthy',1),
(4,'maa.jeevapushpa@ishafoundation.org','$2a$10$y.HcLi.CgQqMWL7rOfLeZe1i9IVrTJ.G6AK9eMR8ftZ8rRhn0l8vO','maajeevapushpa',1),
(17,NULL,'$2a$10$HoaQ542oRYe2yy0fHMYxHOmbeFFnK4OQpTaLDaho9tuBEn2PPuSUm','dongtruong',1),
(22,NULL,'$2a$10$nYOgsyz6ik7uw4U1JKQ4tOstzdzVihyInrFt2llg0lHVd45Mf9NKy','video',1);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `version`
--

DROP TABLE IF EXISTS `version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `version` (
  `version` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  PRIMARY KEY (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `version`
--

LOCK TABLES `version` WRITE;
/*!40000 ALTER TABLE `version` DISABLE KEYS */;
INSERT INTO `version` (`version`) VALUES
('2.1.1');
/*!40000 ALTER TABLE `version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `volume`
--

DROP TABLE IF EXISTS `volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `volume` (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `capacity` bigint(20) DEFAULT NULL,
  `checksumtype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `details` json DEFAULT NULL,
  `finalized` bit(1) DEFAULT NULL,
  `imported` bit(1) DEFAULT NULL,
  `first_written_at` datetime(6) DEFAULT NULL,
  `storagelevel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `storagesubtype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `storagetype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `archiveformat_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `copy_id` int(11) DEFAULT NULL,
  `group_ref_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `location_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `sequence_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `last_written_at` datetime(6) DEFAULT NULL,
  `used_capacity` bigint(20) DEFAULT NULL,
  `healthstatus` varchar(255) DEFAULT NULL,
  `lifecyclestage` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `group_ref_id` (`group_ref_id`),
  KEY `sequence_id` (`sequence_id`),
  KEY `location_id` (`location_id`),
  KEY `archiveformat_id` (`archiveformat_id`),
  KEY `copy_id` (`copy_id`),
  CONSTRAINT `volume_ibfk_1` FOREIGN KEY (`group_ref_id`) REFERENCES `volume` (`id`),
  CONSTRAINT `volume_ibfk_2` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`),
  CONSTRAINT `volume_ibfk_3` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`),
  CONSTRAINT `volume_ibfk_4` FOREIGN KEY (`archiveformat_id`) REFERENCES `archiveformat` (`id`),
  CONSTRAINT `volume_ibfk_5` FOREIGN KEY (`copy_id`) REFERENCES `copy` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `volume`
--

LOCK TABLES `volume` WRITE;
/*!40000 ALTER TABLE `volume` DISABLE KEYS */;
INSERT INTO `volume` (`id`, `capacity`, `checksumtype`, `details`, `finalized`, `imported`, `first_written_at`, `storagelevel`, `storagesubtype`, `storagetype`, `type`, `uuid`, `archiveformat_id`, `copy_id`, `group_ref_id`, `location_id`, `sequence_id`, `last_written_at`, `used_capacity`, `healthstatus`, `lifecyclestage`) VALUES
('A',NULL,'sha256','{\"minimum_free_space\": 2199023255552}',0,0,NULL,'file',NULL,'disk','group',NULL,NULL,1,NULL,NULL,'high-res',NULL,NULL,NULL,NULL),
('A101',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139faa',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A102',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fab',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A103',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fac',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A104',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fad',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A105',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fae',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A106',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139faf',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A107',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fag',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A108',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fah',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A109',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fai',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A110',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139faj',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A111',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fak',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A112',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fal',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A113',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fam',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A114',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fan',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A115',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fao',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A116',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fap',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A117',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139faq',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A118',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139far',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A119',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fas',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('A120',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fat',NULL,NULL,'A','rally',NULL,NULL,0,'normal','active'),
('B',NULL,'sha256','{\"minimum_free_space\": 2199023255552}',0,0,NULL,'file',NULL,'disk','group',NULL,NULL,2,NULL,NULL,'high-res-encrypted',NULL,NULL,NULL,NULL),
('B101',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fba',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B102',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbb',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B103',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbc',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B104',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbd',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B105',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbe',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B106',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbf',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B107',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbg',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B108',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbh',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B109',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbi',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B110',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbj',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B111',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbk',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B112',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbl',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B113',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbm',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B114',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbn',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('B115',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fbo',NULL,NULL,'B','rally',NULL,NULL,0,'normal','active'),
('M',NULL,'sha256','{\"minimum_free_space\": 2199023255552}',0,0,NULL,'file',NULL,'disk','group',NULL,NULL,1,NULL,NULL,'proxy',NULL,NULL,NULL,NULL),
('M101',4000000000000,'sha256','{\"mountpoint\": \"/Volumes\"}',0,0,NULL,'file',NULL,'disk','physical','2c147bed-0f1a-4b7d-bfe6-efc984139fca',NULL,NULL,'M','rally',NULL,NULL,0,'normal','active');
/*!40000 ALTER TABLE `volume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'cp2'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-03-14  1:33:49
