update artifactclass set path_prefix = replace(path_prefix, '/dwara/', '/'); 
update destination set path = replace(path, '/dwara/', '/'); 
delete from destination where id = 'san-video1';

INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES ('ingest', 'video-digi-2020-pub', '3');

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
  `manufacturer` varchar(255) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
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
INSERT INTO `device` (`id`, `defective`, `details`, `manufacturer`, `model`, `serial_number`, `status`, `type`, `warranty_expiry_date`, `wwn_id`) VALUES ('DEV_LTO5_1','\0','{\"type\": \"LTO-5\", \"standalone\": false, \"autoloader_id\": \"DEV_XL80\", \"autoloader_address\": 0}',NULL,NULL,NULL,'offline','tape_drive',NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_0777143630-nst'),('DEV_LTO5_2','\0','{\"type\": \"LTO-5\", \"standalone\": false, \"autoloader_id\": \"DEV_XL80\", \"autoloader_address\": 1}',NULL,NULL,NULL,'online','tape_drive',NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_0005618080-nst'),('DEV_LTO5_3','\0','{\"type\": \"LTO-5\", \"standalone\": false, \"autoloader_id\": \"DEV_XL80\", \"autoloader_address\": 2}',NULL,NULL,NULL,'offline','tape_drive',NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_0574047035-nst'),('DEV_LTO5_4','\0','{\"type\": \"LTO-5\", \"standalone\": false, \"autoloader_id\": \"DEV_XL80\", \"autoloader_address\": 3}',NULL,NULL,NULL,'not_found','tape_drive',NULL,'/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_0257880063-nst'),('DEV_LTO5_5','\0','{\"type\": \"LTO-5\", \"standalone\": false, \"autoloader_id\": \"DEV_XL80\", \"autoloader_address\": 4}',NULL,NULL,NULL,'not_found','tape_drive',NULL,'/dev/tape/by-id/Dummy1-scsi-1IBM_ULT3580-TD5_0257880063-nst'),('DEV_LTO5_6','\0','{\"type\": \"LTO-5\", \"standalone\": false, \"autoloader_id\": \"DEV_XL80\", \"autoloader_address\": 5}',NULL,NULL,NULL,'not_found','tape_drive',NULL,'/dev/tape/by-id/Dummy2-scsi-1IBM_ULT3580-TD5_0257880063-nst'),('DEV_XL80','\0','{\"slots\": 24, \"max_drives\": 3, \"generations_supported\": [6, 7]}',NULL,NULL,NULL,'online','tape_autoloader',NULL,'/dev/tape/by-id/scsi-1IBM_03584L32_0000077866630400');
/*!40000 ALTER TABLE `device` ENABLE KEYS */;
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
INSERT INTO `volume` (`id`, `capacity`, `checksumtype`, `defective`, `details`, `finalized`, `imported`, `initialized_at`, `storagelevel`, `storagesubtype`, `storagetype`, `suspect`, `type`, `archiveformat_id`, `copy_id`, `group_ref_id`, `location_id`, `sequence_id`, `uuid`) VALUES ('B1',NULL,'sha256','\0',NULL,'\0','\0',NULL,'block',NULL,'tape','\0','group','tar',1,NULL,NULL,'dept-backup-1',NULL),('B10501L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 262144}','\0','\0','2020-09-23 03:20:52.112000','block','LTO-5','tape','\0','physical','tar',NULL,'B1','lto-room',NULL,'58874484-2df4-4232-95f2-dc5453f35311'),('B2',NULL,'sha256','\0',NULL,'\0','\0',NULL,'block',NULL,'tape','\0','group','bru',2,NULL,NULL,'dept-backup-2',NULL),('B20501L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 262144}','\0','\0','2020-09-23 03:21:52.184000','block','LTO-5','tape','\0','physical','bru',NULL,'B2','lto-room',NULL,'a3f85ed2-6a4d-41bf-85e5-8d4043313538'),('B3',NULL,'sha256','\0',NULL,'\0','\0',NULL,'block',NULL,'tape','\0','group','tar',3,NULL,NULL,'dept-backup-3',NULL),('B30501L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 262144}','\0','\0','2020-09-23 03:22:52.251000','block','LTO-5','tape','','physical','tar',NULL,'B3','lto-room',NULL,'5e96585e-ffe5-4e6a-88b7-0adf6f9db996'),('G1',NULL,'sha256','\0','{\"blocksize\": 524288}','\0','\0',NULL,'block',NULL,'tape','\0','group','tar',1,NULL,NULL,'generated-1',NULL),('G10501L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-09-13 21:11:05.933000','block','LTO-5','tape','\0','physical','tar',NULL,'G1','lto-room',NULL,'a22fa52a-d464-4042-b657-7ab66b24e1ec'),('G2',NULL,'sha256','\0','{\"blocksize\": 524288}','\0','\0',NULL,'block',NULL,'tape','\0','group','bru',2,NULL,NULL,'generated-2',NULL),('G20501L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-12-13 13:42:03.027000','block','LTO-5','tape','\0','physical','bru',NULL,'G2','lto-room',NULL,'aea162c3-a55d-43bb-bd55-a1121da0f4d1'),('G3',NULL,'sha256','\0','{\"blocksize\": 524288}','\0','\0',NULL,'block',NULL,'tape','\0','group','tar',3,NULL,NULL,'generated-3',NULL),('G30501L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-12-13 14:18:05.131000','block','LTO-5','tape','\0','physical','tar',NULL,'G3','lto-room',NULL,'d03789a9-310d-4da7-b81b-49f5e24f196e'),('R1',NULL,'sha256','\0','{\"blocksize\": 524288}','\0','\0',NULL,'block',NULL,'tape','\0','group','tar',1,NULL,NULL,'original-1',NULL),('R10501L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-09-13 21:13:05.991000','block','LTO-5','tape','\0','physical','tar',NULL,'R1','lto-room',NULL,'4c457268-cc31-453b-9e04-b9f2d51aa61a'),('R10502L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-09-14 14:06:14.652000','block','LTO-5','tape','\0','physical','tar',NULL,'R1','lto-room',NULL,'798b1bec-3176-4568-af76-2b8879fba4f8'),('R10503L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-09-14 14:07:14.675000','block','LTO-5','tape','','physical','tar',NULL,'R1','lto-room',NULL,'5aaf6840-d77f-45aa-b20a-1e415c595e53'),('R10504L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-09-21 15:35:47.559000','block','LTO-5','tape','','physical','tar',NULL,'R1','lto-room',NULL,'976b9d38-10e8-4e12-a92c-8e89eb73814c'),('R10505L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','','\0','2020-09-22 16:32:07.635000','block','LTO-5','tape','\0','physical','tar',NULL,'R1','lto-room',NULL,'1a90429a-198e-43ef-9fb8-7a9586397d40'),('R10507L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','','\0','2020-10-23 14:02:32.938000','block','LTO-5','tape','\0','physical','tar',NULL,'R1','lto-room',NULL,'7dfdfabe-73f7-4ec9-85dc-a0d23495fcdf'),('R10508L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-10-24 17:17:52.201000','block','LTO-5','tape','\0','physical','tar',NULL,'R1','lto-room',NULL,'5834c866-2e18-4d1b-ba40-b5f2e2f7853f'),('R10509L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-11-05 17:31:01.530000','block','LTO-5','tape','\0','physical','tar',NULL,'R1','lto-room',NULL,'c3011580-b908-4143-b6f1-d3c1aeb41d0b'),('R2',NULL,'sha256','\0','{\"blocksize\": 524288}','\0','\0',NULL,'block',NULL,'tape','\0','group','bru',2,NULL,NULL,'original-2',NULL),('R20501L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-09-13 21:12:05.968000','block','LTO-5','tape','\0','physical','bru',NULL,'R2','lto-room',NULL,'25ce281c-1912-42bf-ae76-3f3af8a7e10b'),('R20502L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-09-22 00:41:50.567000','block','LTO-5','tape','\0','physical','bru',NULL,'R2','lto-room',NULL,'5d18a76f-3a11-45df-a37c-b944e60c7967'),('R20503L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-09-24 00:05:47.895000','block','LTO-5','tape','\0','physical','bru',NULL,'R2','lto-room',NULL,'a732379d-2e69-4133-947a-d3dd4f7a8ff4'),('R3',NULL,'sha256','\0','{\"blocksize\": 524288}','\0','\0',NULL,'block',NULL,'tape','\0','group','tar',3,NULL,NULL,'original-3',NULL),('R30501L5',1500000000000,'sha256','\0','{\"barcoded\": true, \"blocksize\": 524288}','\0','\0','2020-12-13 13:41:32.955000','block','LTO-5','tape','\0','physical','tar',NULL,'R3','lto-room',NULL,'aaa08dfc-684c-4186-b8d1-77955166eaed'),('X1',NULL,'sha256','\0',NULL,'\0','\0',NULL,'block',NULL,'tape','\0','group','tar',1,NULL,NULL,'priv2-1',NULL),('X2',NULL,'sha256','\0',NULL,'\0','\0',NULL,'block',NULL,'tape','\0','group','bru',2,NULL,NULL,'priv2-2',NULL),('X3',NULL,'sha256','\0',NULL,'\0','\0',NULL,'block',NULL,'tape','\0','group','tar',3,NULL,NULL,'priv2-3',NULL);


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
  `replace_code` bit(1) DEFAULT NULL,
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
INSERT INTO `sequence` (`id`, `code_regex`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `number_regex`, `prefix`, `starting_number`, `type`, `sequence_ref_id`, `replace_code`) VALUES ('audio-grp',NULL,0,-1,'\0','','\0',NULL,NULL,1,'artifact',NULL,NULL),('audio-priv2','^[ a-zA-Z0-9\\-()]+(?=_)',NULL,NULL,'\0','\0','\0',NULL,'AX',NULL,'artifact','audio-grp','\0'),('audio-priv3','^X\\d+(?=_)',NULL,NULL,'\0','\0','\0','(?<=^X)\\d+(?=_)','AY',NULL,'artifact',NULL,'\0'),('audio-pub','^[ a-zA-Z0-9\\-()]+(?=_)',NULL,NULL,'\0','\0','\0',NULL,'A',NULL,'artifact','audio-grp','\0'),('dept-backup','^D\\d+(?=_)',0,-1,'\0','\0','\0','(?<=^D)\\d+(?=_)','D',1,'artifact',NULL,'\0'),('dept-backup-1',NULL,10000,19999,'\0','\0','\0',NULL,'B',10001,'volume',NULL,NULL),('dept-backup-2',NULL,20000,29999,'\0','\0','\0',NULL,'B',20001,'volume',NULL,NULL),('dept-backup-3',NULL,30000,39999,'\0','\0','\0',NULL,'B',30001,'volume',NULL,NULL),('generated-1',NULL,10002,19999,'\0','\0','\0',NULL,'G',10001,'volume',NULL,NULL),('generated-2',NULL,20002,29999,'\0','\0','\0',NULL,'G',20001,'volume',NULL,NULL),('generated-3',NULL,30000,39999,'\0','\0','\0',NULL,'G',30001,'volume',NULL,NULL),('original-1',NULL,10004,19999,'\0','\0','\0',NULL,'R',10001,'volume',NULL,NULL),('original-2',NULL,20004,29999,'\0','\0','\0',NULL,'R',20001,'volume',NULL,NULL),('original-3',NULL,30004,39999,'\0','\0','\0',NULL,'R',30001,'volume',NULL,NULL),('priv2-1',NULL,10001,19999,'\0','\0','\0',NULL,'X',10001,'volume',NULL,NULL),('priv2-2',NULL,20001,29999,'\0','\0','\0',NULL,'X',20001,'volume',NULL,NULL),('priv2-3',NULL,30001,39999,'\0','\0','\0',NULL,'X',30001,'volume',NULL,NULL),('priv3-1',NULL,10000,19999,'\0','\0','\0',NULL,'Y',10001,'volume',NULL,NULL),('priv3-2',NULL,20000,29999,'\0','\0','\0',NULL,'Y',20001,'volume',NULL,NULL),('video-digi-2020-grp',NULL,17,-1,'\0','','\0',NULL,NULL,1,'artifact',NULL,NULL),('video-digi-2020-pub','^\\d+(?=_)',NULL,NULL,'\0','\0','\0','^\\d+(?=_)','D',NULL,'artifact','video-digi-2020-grp',''),('video-digi-2020-pub-proxy-low','^D\\d+(?=_)',NULL,NULL,'\0','\0','\0','(?<=D)\\d+(?=_)','DL',NULL,'artifact',NULL,'\0'),('video-edit-global','^ZG\\d+(?=_)',0,-1,'\0','\0','','(?<=^ZG)\\d+(?=_)','ZG',1,'artifact',NULL,'\0'),('video-edit-grp',NULL,9000,-1,'\0','','\0',NULL,NULL,1,'artifact',NULL,NULL),('video-edit-priv2','^Z\\d+(?=_)',NULL,NULL,'\0','\0','\0','(?<=^Z)\\d+(?=_)','ZX',NULL,'artifact','video-edit-grp','\0'),('video-edit-pub','^Z\\d+(?=_)',NULL,NULL,'\0','\0','',NULL,'Z',NULL,'artifact','video-edit-grp','\0'),('video-grp',NULL,27822,-1,'\0','','\0',NULL,NULL,1,'artifact',NULL,NULL),('video-priv2','^\\d+(?=_)',NULL,NULL,'\0','\0','\0','^\\d+(?=_)','VX',NULL,'artifact','video-grp','\0'),('video-priv2-proxy-low','^VX\\d+(?=_)',NULL,NULL,'','\0','\0','(?<=^VX)\\d+(?=_)','VXL',NULL,'artifact',NULL,'\0'),('video-priv3','^\\d+(?=_)',NULL,NULL,'\0','\0','\0','^\\d+(?=_)','VY',NULL,'artifact',NULL,'\0'),('video-pub','^\\d+(?=_)',NULL,NULL,'\0','\0','\0','^\\d+(?=_)','V',NULL,'artifact','video-grp','\0'),('video-pub-proxy-low','^V\\d+(?=_)',NULL,NULL,'','\0','\0','(?<=^V)\\d+(?=_)','VL',NULL,'artifact',NULL,'\0');
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;
