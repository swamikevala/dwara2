SET foreign_key_checks = 0;

-- Domain changes - missed out dropping the below table
DROP TABLE `domain`;

-- Rewrite failed job. Request and Jobs created for Deleted artifact which shouldnt have - Potential bug in app
UPDATE `request` SET `status`='cancelled', `message`='Deleted artifact. Request shouldnt have got created in the first place.' WHERE `id`='76074';

-- Import 
-- config changes
/*  
ALTER TABLE `sequence` CHANGE COLUMN `force_match` `force_match` INT(1) NULL DEFAULT NULL ;

UPDATE `sequence` SET `force_match`='2' WHERE `id`='video-pub';
UPDATE `sequence` SET `force_match`='2' WHERE `id`='video-priv2';
UPDATE `sequence` SET `force_match`='2' WHERE `id`='video-priv3';
UPDATE `sequence` SET `force_match`='2' WHERE `id`='video-edit-pub';
UPDATE `sequence` SET `force_match`='2' WHERE `id`='video-edit-priv2';
UPDATE `sequence` SET `force_match`='0' WHERE `id`='video-digi-2010-priv2';
UPDATE `sequence` SET `force_match`='0' WHERE `id`='video-digi-2010-pub';
*/

ALTER TABLE `sequence` 
DROP COLUMN `code_regex`,
DROP COLUMN `number_regex`,
DROP COLUMN `force_match`,
DROP COLUMN `keep_code`,
DROP COLUMN `replace_code`
;
-- change prefix for 2010-raw - VC
UPDATE `sequence` SET `prefix`='VC' WHERE `id`='video-digi-2010-pub';
UPDATE `sequence` SET `prefix`='VCX' WHERE `id`='video-digi-2010-priv2';

-- add entries for 2010-edited ZC
-- SEQUENCE --
INSERT INTO sequence (id, `type`, prefix, `group`, starting_number, ending_number, current_number, sequence_ref_id) VALUES 
('video-digi-2010-edit-grp','artifact',null,1,1,-1,0,null),
('video-digi-2010-edit-pub','artifact','ZC',0,null,null,null,'video-digi-2010-edit-grp'),
('video-digi-2010-edit-priv2','artifact','ZCX',0,null,null,null,'video-digi-2010-edit-grp');

-- ARTIFACTCLASS --
INSERT INTO artifactclass (id, `description`, sequence_id, source, concurrent_volume_copies, display_order, path_prefix, artifactclass_ref_id, import_only, config) VALUES 
('video-digi-2010-edit-pub','','video-digi-2010-edit-pub',1,1,10,'/data/dwara/staged',null,0,null),
('video-digi-2010-edit-priv1','','video-digi-2010-edit-pub',1,1,11,'/data/dwara/staged',null,0,null),
('video-digi-2010-edit-priv2','','video-digi-2010-edit-priv2',1,1,12,'/data/dwara/staged',null,0,null);

-- update the already imported 5 tapes with changed schema

-- update request status 
update request set status = 'completed_failures' where action_id='import' and json_extract(details, '$.body.xmlPathname') like "%P17023L6%";
update request set status = 'completed_failures' where action_id='import' and json_extract(details, '$.body.xmlPathname') like "%C17027L6%";
update request set status = 'completed_failures' where action_id='import' and json_extract(details, '$.body.xmlPathname') like "%C17031L7%";

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

select @counter := (select count(*) from import);

-- **************** P17023L6 *************** - failed

INSERT INTO `import` (id,`artifact_id`,`artifact_name`, `volume_id`,`requeue_id` , `message`, `status`, `request_id`) 
VALUES 
(@counter := @counter +1,49032,'Z7436_Resi-Meet_HD_2016-Dec-12_Spandhahall_Edited-Files','P17023L6',1,null,'completed',80527),	
(@counter := @counter +1,49033,'Z7437_Resi-Meet_HD4K_2019-Dec-16_AYA-IYC_Edited-Files','P17023L6',1,null,'completed',80527),
-- handled in failure section (@counter := @counter +1,49034,' Z7438_Resi-Meet_HD4K_2020-Mar-22_Nalanda-Grounds-IYC_Edited-Files','P17023L6',1,'Artifact name has special characters','failed',80527),
(@counter := @counter +1,49035,'Z7439_Meditators-Sathsang_DV_M211-To-M216_MD_M104_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49036,'Z7440_Meditators-Sathsang_DV_M217-To-M223_MD_M000_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49037,'Z7441_Meditators-Sathsang_DV_M224-To-M225_MD_M101_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49038,'Z7442_Meditators-Sathsang_DV_M226-To-M229_MD_M000_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49039,'Z7443_Meditators-Sathsang_DV_M230-To-M233_MD_M030_M100_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49040,'Z7444_Meditators-Sathsang_DV_M234-To-M235_MD_M107_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49041,'Z7445_Meditators-Sathsang_DV_M236-To-M238_MD_M000_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49042,'Z7446_Meditators-Sathsang_DV_M239-To-M240_MD_M000_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49043,'Z7447_Meditators-Sathsang_DV_M241-To-M243_MD_M000_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49044,'Z7448_Meditators-Sathsang_DV_M244-To-M245_MD_M105-To-M106_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49045,'Z7449_Meditators-Sathsang_DV_M246-To-M252_MD_M108-To-M109_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49046,'Z7450_Meditators-Sathsang_DV_M253-To-M260_MD_M110-To-M111_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49047,'Z7451_Meditators-Sathsang_DV_M261-To-M266_MD_M139_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49048,'Z7452_Meditators-Sathsang_DV_M267-To-M270_MD_M113_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49049,'Z7453_Meditators-Sathsang_DV_M271-To-M279_MD_M116_M117_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49050,'Z7454_Meditators-Sathsang_DV_M280-To-M282_MD_N193_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49051,'Z7455_Meditators-Sathsang_DV_M283_MD_M114_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49052,'Z7456_Meditators-Sathsang_DV_M284-To-M288_MD_M118_M119_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49053,'Z7457_Meditators-Sathsang_DV_M289_MD_M515_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49054,'Z7458_Meditators-Sathsang_DV_M290-To-M293_MD_M121_Edited-Files','P17023L6',1,null,'completed',80527),
(@counter := @counter +1,49055,'Z7459_Meditators-Sathsang_DV_M294-To-M295_M615-To-M616_MD_M058_Edited-Files','P17023L6',1,null,'completed',80527),
-- handled in failure section (@counter := @counter +1,49056,'Z7460_Meditators-Sathsang_DV_M296-To-M298_Same-In_M036_MD_M057_Edited-Files','P17023L6',1,'Artifact size is less than 1MiB','failed',80527);
(@counter := @counter +1,49057,'Z7461_Meditators-Sathsang_DV_M299-To-M302_MD-M453_AA04-To-AA05_Edited-Files','P17023L6',1,null,'completed',80527);

-- The failures

INSERT INTO `import` (id,`artifact_id`,`artifact_name`, `volume_id`,`requeue_id` , `message`, `status`, `request_id`) 
VALUES 
(@counter := @counter +1,49034,' Z7438_Resi-Meet_HD4K_2020-Mar-22_Nalanda-Grounds-IYC_Edited-Files','P17023L6',1,'Artifact name has special characters','completed_failures',80527),
(@counter := @counter +1,49056,'Z7460_Meditators-Sathsang_DV_M296-To-M298_Same-In_M036_MD_M057_Edited-Files','P17023L6',1,'Artifact size is less than 1MiB','completed_failures',80527);


-- **************** C17027L6 *************** - failed

INSERT INTO `import` (id,`artifact_id`,`artifact_name`, `volume_id`,`requeue_id` , `message`, `status`, `request_id`) 
VALUES 
(@counter := @counter +1,48904,'24945_Challenging-Times-With-Sadhguru-For-Times-Now_Day5-Interview-With-Navika-Ep01_Shoonya-Cottage-IYC_02-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48905,'24946_Challenging-Times-With-Sadhguru-For-Times-Now_Day5-Interview-With-Navika-Ep01_Shoonya-Cottage-IYC_02-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48906,'24947_Challenging-Times-With-Sadhguru-For-Times-Now_Day5-Interview-With-Navika-Ep01_Shoonya-Cottage-IYC_02-Apr-2020_Cam3_MP4','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48907,'24948_With-Sadhguru-In-Challenging-Times-Series_Day16_SPH-Lawn-IYC_06-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48908,'24949_With-Sadhguru-In-Challenging-Times-Series_Day16_SPH-Lawn-IYC_06-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48909,'24950_With-Sadhguru-In-Challenging-Times-Series_Day16_SPH-Lawn-IYC_06-Apr-2020_Cam3_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48910,'24951_With-Sadhguru-In-Challenging-Times-Series_Day16_SPH-Lawn-IYC_06-Apr-2020_Online-Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48911,'24952_Sadhguru-Interview-With-Formula-One-Racer-Nico-Rosberg_IHS-IYC_20-Apr-2020_Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48912,'24953_With-Sadhguru-In-Challenging-Times-Series_Day34_SPH-Lawn-IYC_24-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48913,'24954_With-Sadhguru-In-Challenging-Times-Series_Day34_SPH-Lawn-IYC_24-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48914,'24955_With-Sadhguru-In-Challenging-Times-Series_Day34_SPH-Lawn-IYC_24-Apr-2020_Cam3_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48915,'24956_With-Sadhguru-In-Challenging-Times-Series_Day34_SPH-Lawn-IYC_24-Apr-2020_Online-Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48916,'24957_With-Sadhguru-In-Challenging-Times-Series_Day35_SPH-Lawn-IYC_25-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48917,'24958_With-Sadhguru-In-Challenging-Times-Series_Day35_SPH-Lawn-IYC_25-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48918,'24959_With-Sadhguru-In-Challenging-Times-Series_Day35_SPH-Lawn-IYC_25-Apr-2020_Cam3_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48919,'24960_With-Sadhguru-In-Challenging-Times-Series_Day35_SPH-Lawn-IYC_25-Apr-2020_Online-Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48920,'24961_Indian-Universities-In-Challenging-Times_Sadhguru-Interaction-With-Vice-Chancellors_Nalanda1-IYC_25-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48921,'24962_Indian-Universities-In-Challenging-Times_Sadhguru-Interaction-With-Vice-Chancellors_Nalanda1-IYC_25-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48922,'24963_Sadhguru-Interaction-With-EO-Organization-Business-Leaders_Nalanda1-IYC_25-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48923,'24964_Sadhguru-Interaction-With-EO-Organization-Business-Leaders_Nalanda1-IYC_25-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48924,'24965_With-Sadhguru-In-Challenging-Times-Series_Day36_AYA-IYC_26-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48925,'24966_With-Sadhguru-In-Challenging-Times-Series_Day36_AYA-IYC_26-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48926,'24967_With-Sadhguru-In-Challenging-Times-Series_Day36_AYA-IYC_26-Apr-2020_Cam3_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48927,'24968_With-Sadhguru-In-Challenging-Times-Series_Day36_AYA-IYC_26-Apr-2020_Online-Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48928,'24969_Erica-Peng-In-Coversation-With-Sadhguru_Haas-School-Of-Business-Berkeley-California-USA_01-May-2019_Youtube','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48929,'24970_Table-Tennis-Tournament-For-Ashramites-With-Sadhguru_Vajra-IHS-IYC_30-Mar-2020_Cam1_Z280V','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48930,'24971_Table-Tennis-Tournament-For-Ashramites-With-Sadhguru_Vajra-IHS-IYC_30-Mar-2020_Cam2_FS7','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48931,'24972_Table-Tennis-Tournament-For-Ashramites-With-Sadhguru_Vajra-IHS-IYC_30-Mar-2020_Cam3_FS7','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48932,'24973_Table-Tennis-Tournament-For-Ashramites-With-Sadhguru_Vajra-IHS-IYC_30-Mar-2020_Cam4_Z280V','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48933,'24974_Sadhguru-Shots_SPH-IYC_Apr-2020_RX100_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48934,'24975_Challenging-Times-With-Sadhguru-For-Times-Now_Day5-Interview-With-Navika-Ep02_Shoonya-Cottage-IYC_02-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48935,'24976_Challenging-Times-With-Sadhguru-For-Times-Now_Day5-Interview-With-Navika-Ep02_Shoonya-Cottage-IYC_02-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48936,'24977_Challenging-Times-With-Sadhguru-For-Times-Now_Day5-Interview-With-Navika-Ep02_Shoonya-Cottage-IYC_02-Apr-2020_Cam3_MP4','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48937,'24978_Sadhguru-Interview-With-London-Real_IHS-IYC_06-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48938,'24979_Sadhguru-Interview-With-London-Real_IHS-IYC_06-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48939,'24980_Adiyogi-Shots_112-Foot-Adiyogi-IYC_18-Apr-2020_Drone','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48940,'24981_Adiyogi-Time-Lapse-Images_112-Foot-Adiyogi-IYC_20-Apr-2020_A7S2','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48941,'24982_COVID19-Coronavirus-Relief-Activity_Vegetables-Arriving_Suryakund-Mandapam-IYC_20-Apr-2020_A7S2_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48942,'24983_Sadhguru-Visiting-Adiyogi_112-Foot-Adiyogi-IYC_20-Apr-2020_A7S2_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48943,'24984_Sadhguru-Visiting-Adiyogi_112-Foot-Adiyogi-IYC_20-Apr-2020_Drone','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48944,'24985_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_21-Apr-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48945,'24986_Shoot-For-Online-Monthly-Satsang-For-Russia_Nalanda1-IYC_21-Apr-2020_EX3','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48946,'24987_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_22-Apr-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48947,'24988_Sadhguru-Interaction-With-International-Union-For-Conservation-Of-Nature-Members_SPH-IYC_22-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48948,'24989_Sadhguru-Interaction-With-International-Union-For-Conservation-Of-Nature-Members_SPH-IYC_22-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48949,'24990_COVID19-Coronavirus-Relief-Activity_Sharing-By-Swami-Chitakasha-And-Vessel-Sanitising-Shots_Sarpa-Vasal-IYC_23-Apr-2020_A7S2_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48950,'24991_Samskriti-Bharatanatyam-Performance_AYA-IYC_23-Apr-2020_Z280V','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48951,'24992_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_24-Apr-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48952,'24993_Shoot-For-Online-Monthly-Satsang-In-Chinese_Nalanda2-IYC_24-Apr-2020_EX3','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48953,'24994_Adiyogi-Time-Lapse-Images_112-Foot-Adiyogi-IYC_26-Apr-2020_A7S2','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48954,'24995_Shoot-For-21-Day-Guided-Shambhavi_Nalanda2-IYC_26-Apr-2020_EX3','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48955,'24996_Adiyogi-Time-Lapse-Images_112-Foot-Adiyogi-IYC_27-Apr-2020_A7S2','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48956,'24997_Challenging-Times-With-Sadhguru-For-Times-Now_Day15-Interview-With-Navika_Nalanda1-IYC_27-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48957,'24998_Challenging-Times-With-Sadhguru-For-Times-Now_Day15-Interview-With-Navika_Nalanda1-IYC_27-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48958,'24999_Challenging-Times-With-Sadhguru-For-Times-Now_Day15-Interview-With-Navika_Nalanda1-IYC_27-Apr-2020_Cam3_MP4','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48959,'25000_Sadhguru-Visiting-Adiyogi_112-Foot-Adiyogi-IYC_27-Apr-2020_A7S2','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48960,'25001_With-Sadhguru-In-Challenging-Times-Series_Day37_AYA-IYC_27-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48961,'25002_With-Sadhguru-In-Challenging-Times-Series_Day37_AYA-IYC_27-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48962,'25003_With-Sadhguru-In-Challenging-Times-Series_Day37_AYA-IYC_27-Apr-2020_Cam3_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48963,'25004_With-Sadhguru-In-Challenging-Times-Series_Day37_AYA-IYC_27-Apr-2020_Online-Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48964,'25005_With-Sadhguru-In-Challenging-Times-Series_Day38_AYA-IYC_28-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48965,'25006_With-Sadhguru-In-Challenging-Times-Series_Day38_AYA-IYC_28-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48966,'25007_With-Sadhguru-In-Challenging-Times-Series_Day38_AYA-IYC_28-Apr-2020_Cam3_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48967,'25008_With-Sadhguru-In-Challenging-Times-Series_Day38_AYA-IYC_28-Apr-2020_Online-Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48968,'25009_Adiyogi-In-Rain-And-Time-Lapse-Images_112-Foot-Adiyogi-IYC_28-Apr-2020_A7S2','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48969,'25010_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_28-Apr-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48970,'25011_Samskriti-Bharatanatyam-Performance_AYA-IYC_28-Apr-2020_Z280V','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48971,'25012_Radhe-Dance-Performance-For-Cauvery-Calling-Fund-Raising_Peichin-Lee-Residence-Diamond-Bar-California-USA_19-Oct-2019_Cam1_Cellphone','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48972,'25013_Radhe-Dance-Performance-For-Cauvery-Calling-Fund-Raising_Peichin-Lee-Residence-Diamond-Bar-California-USA_19-Oct-2019_Cam2_Cellphone','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48973,'25014_Sadhguru-At-UNCCD-Office-Hosted-By-Executive-Secretary-Ibrahim-Thiaw_UN-Campus-Bonn-Germany_18-Nov-2019_AX100','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48974,'25015_Sadhguru-Talk-For-UNCCD-Staff_UN-Campus-Bonn-Germany_18-Nov-2019_AX100','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48975,'25016_IEC-With-Sadhguru_Day1-Participants-Shots_Kay-Bailey-Hutchison-Convention-Center-Dallas-TX-USA_23-Nov-2019_Panasonic_B-Rolls','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48976,'25017_IEC-With-Sadhguru_Day2-Morning-Practice-Shots_Kay-Bailey-Hutchison-Convention-Center-Dallas-TX-USA_24-Nov-2019_Cam1_Z280V','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48977,'25018_IEC-With-Sadhguru_Day2-Morning-Practice-Shots_Kay-Bailey-Hutchison-Convention-Center-Dallas-TX-USA_24-Nov-2019_Cam2_Z280V','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48978,'25019_IEC-With-Sadhguru_Day2-Morning-Practice-Shots_Kay-Bailey-Hutchison-Convention-Center-Dallas-TX-USA_24-Nov-2019_Cam3_Z280V','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48979,'25020_IEC-With-Sadhguru_Day2-Participants-Shots_Kay-Bailey-Hutchison-Convention-Center-Dallas-TX-USA_24-Nov-2019_AX-100_B-Rolls','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48980,'25021_IEC-With-Sadhguru_Day2-Participants-Shots_Kay-Bailey-Hutchison-Convention-Center-Dallas-TX-USA_24-Nov-2019_Osmo_B-Rolls','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48981,'25022_IEC-With-Sadhguru_Day2-Participants-Shots_Kay-Bailey-Hutchison-Convention-Center-Dallas-TX-USA_24-Nov-2019_Panasonic_B-Rolls','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48982,'25023_IEC-With-Sadhguru_Day2-Sharing-By-Participants_Kay-Bailey-Hutchison-Convention-Center-Dallas-TX-USA_24-Nov-2019_Z280V','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48983,'25024_Shivanga-Gents_Velliangiri-Yatra_Velliangiri-Hills-Cbe_24-Jan-2020_Cellphone','C17027L6',1,null,'completed',80526),
-- handled in failure section (@counter := @counter +1,48984,'25025_Agroforestry-Farm-Shots-From-Farmer-Suresh-Farm_ Karekura-Mandya-District_05-Feb-2020_Drone','C17027L6',1,'Artifact name has special characters','failed',80526);
(@counter := @counter +1,48985,'25026_Point-Of-View-Sharing-By-Japanese-Meditator_IYC_13-Mar-2020_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48986,'25027_Sadhguru-Message-For-Ugadi-In-Kannada_Sadhguru-Room-AYA-IYC_23-Mar-2020_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48987,'25028_Sadhguru-Message-For-Ugadi-In-Telugu_Sadhguru-Room-AYA-IYC_23-Mar-2020_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48988,'25029_Sanitising-In-Ashram-During-COVID19-Pandemic_Various-Places-IYC_23-Mar-2020_A7S2','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48989,'25030_Sadhguru-Revisiting-The-Trees-He-Planted-18-Years-Ago_Near-AV-Hall-IYC_25-Mar-2020_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48990,'25031_Sadhguru-Visiting-Adiyogi_112-Foot-Adiyogi-IYC_25-Mar-2020_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48991,'25032_Challenging-Times-With-Sadhguru-For-Times-Now_Day1_IHS-IYC_26-Mar-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48992,'25033_Challenging-Times-With-Sadhguru-For-Times-Now_Day1_IHS-IYC_26-Mar-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48993,'25034_Challenging-Times-With-Sadhguru-For-Times-Now_Day1_IHS-IYC_26-Mar-2020_Cam3_A7S2_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48994,'25035_Challenging-Times-With-Sadhguru-For-Times-Now-Day-2_IHS-IYC_27-Mar-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48995,'25036_Challenging-Times-With-Sadhguru-For-Times-Now-Day-2_IHS-IYC_27-Mar-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48996,'25037_Challenging-Times-With-Sadhguru-For-Times-Now-Day-2_IHS-IYC_27-Mar-2020_Cam3_A7S2_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48997,'25038_Sadhguru-Message-On-Coronavirus-In-Tamil_IHS-IYC_27-Mar-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48998,'25039_Sadhguru-Message-On-Coronavirus-In-Tamil_IHS-IYC_27-Mar-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,48999,'25040_Sadhguru-Message-On-Coronavirus-In-Tamil_IHS-IYC_27-Mar-2020_Cam3_A7S2_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49000,'25041_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_31-Mar-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49001,'25042_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_01-Apr-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49002,'25043_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_02-Apr-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49003,'25044_Sadhguru-Message-For-Apollo-Hospitals_Shoonya-Cottage-IYC_02-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49004,'25045_Sadhguru-Message-For-Apollo-Hospitals_Shoonya-Cottage-IYC_02-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49005,'25046_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_03-Apr-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49006,'25047_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_04-Apr-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49007,'25048_Shots-Of-Spandha-Hall-Murals_SPH-IYC_04-Apr-2020_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49008,'25049_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_05-Apr-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49009,'25050_Sadhguru-Audio-Recording-For-Social-Media_SPH-IYC_05-Apr-2020_Z280V','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49010,'25051_Challenging-Times-With-Sadhguru-For-Times-Now_Day7_IHS-IYC_06-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49011,'25052_Challenging-Times-With-Sadhguru-For-Times-Now_Day7_IHS-IYC_06-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49012,'25053_Challenging-Times-With-Sadhguru-For-Times-Now_Day7_IHS-IYC_06-Apr-2020_Cam3_A7S2_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49013,'25054_Shoot-For-Corona-Awareness-Tamil-Song_Lotus-Pond-IYC_06-Apr-2020_FS7','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49014,'25055_Shoot-For-Corona-Awareness-Tamil-Song_Lotus-Pond-IYC_06-Apr-2020_A7S2_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49015,'25056_Adiyogi-And-Moon-Shots_112-Foot-Adiyogi-IYC_08-Apr-2020_Drone_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49016,'25057_Challenging-Times-With-Sadhguru-For-Times-Now_Day9-Interview-With-Navika-Ep03_Nandi-IYC_10-Apr-2020_Cam3_MP4','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49017,'25058_With-Sadhguru-In-Challenging-Times_Sadhguru-Live-Interaction-With-FICCI-Members_IHS-IYC_12-Apr-2020_Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49018,'25059_Adiyogi-Time-Lapse-Images_112-Foot-Adiyogi-IYC_15-Apr-2020_A7S2','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49019,'25060_Sadhguru-Interaction-With-PHD-Chamber-Of-Commerce-And-Industry-Members_Nalanda1-IYC_23-Apr-2020_Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49020,'25061_With-Sadhguru-In-Challenging-Times_Sadhguru-Interaction-With-CII-Members_Nalanda1-IYC_23-Apr-2020_Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49021,'25062_With-Sadhguru-In-Challenging-Times-Series_Day34_SPH-Lawn-IYC_24-Apr-2020_Cam4_Drone','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49022,'25063_Indian-Universities-In-Challenging-Times_Sadhguru-Interaction-With-Vice-Chancellors_Nalanda1-IYC_25-Apr-2020_Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49023,'25064_Sadhguru-Interaction-With-EO-Organization-Business-Leaders_Nalanda1-IYC_25-Apr-2020_Mix','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49024,'25065_Adiyogi-Time-Lapse-Images_112-Foot-Adiyogi-IYC_29-Apr-2020_A7S2','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49025,'25066_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_29-Apr-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49026,'25067_Sadhguru-Message-For-Ajanta-Pharma_Sadhguru-Room-AYA-IYC_29-Apr-2020_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49027,'25068_Shoot-For-Linga-Bhairavi-Ritual-Training-Video_Linga-Bhairavi-IYC_29-Apr-2020_X70','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49028,'25069_With-Sadhguru-In-Challenging-Times-Series_Day39_AYA-IYC_29-Apr-2020_Cam1_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49029,'25070_With-Sadhguru-In-Challenging-Times-Series_Day39_AYA-IYC_29-Apr-2020_Cam2_FS7_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49030,'25071_With-Sadhguru-In-Challenging-Times-Series_Day39_AYA-IYC_29-Apr-2020_Cam3_Z280V_4K','C17027L6',1,null,'completed',80526),
(@counter := @counter +1,49031,'25072_With-Sadhguru-In-Challenging-Times-Series_Day39_AYA-IYC_29-Apr-2020_Online-Mix','C17027L6',1,null,'completed',80526);

INSERT INTO `import` (id,`artifact_id`,`artifact_name`, `volume_id`,`requeue_id` , `message`, `status`, `request_id`) 
VALUES 
(@counter := @counter +1,48984,'25025_Agroforestry-Farm-Shots-From-Farmer-Suresh-Farm_ Karekura-Mandya-District_05-Feb-2020_Drone','C17027L6',1,'Artifact name has special characters','completed_failures',80526);

-- **************** CA4220L4 *************** - completed 80525
INSERT INTO `import` (id,`artifact_id`,`artifact_name`, `volume_id`,`requeue_id` , `message`, `status`, `request_id`) 
VALUES 
(@counter := @counter +1,48886,'1652_IE-Thiagaraja-Stadium_Delhi_Day1-Evening-Session_16-Dec-10_Cam1','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48887,'1653_IE-Thiagaraja-Stadium_Delhi_Day1-Evening-Session_16-Dec-10_Cam2','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48888,'1654_IE-Thiagaraja-Stadium_Delhi_Day2-Evening-Session_17-Dec-10_Cam1','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48889,'1655_IE-Thiagaraja-Stadium_Delhi_Day2-Evening-Session_17-Dec-10_Cam2','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48890,'1656_IE-Thiagaraja-Stadium_Delhi_Day3-Afternoon-Session_18-Dec-10_Cam1','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48891,'1657_IE-Thiagaraja-Stadium_Delhi_Day3-Afternoon-Session_18-Dec-10_Cam2','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48892,'1658_IE-Thiagaraja-Stadium_Delhi_Day3-Morning-Session_18-Dec-10_Cam1','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48893,'1659_IE-Thiagaraja-Stadium_Delhi_Day3-Morning-Session_18-Dec-10_Cam2','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48894,'1660_IE-Thiagaraja-Stadium_Delhi_Day4_Afternoon-Initiation_19-Dec-10_Cam1','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48895,'1661_IE-Thiagaraja-Stadium_Delhi_Day4_Afternoon-Initiation_19-Dec-10_Cam2','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48896,'1662_IE-Thiagaraja-Stadium_Delhi_Day4_Evening-Session_19-Dec-10_Cam1','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48897,'1663_IE-Thiagaraja-Stadium_Delhi_Day4_Evening-Session_19-Dec-10_Cam2','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48898,'1664_IE-Thiagaraja-Stadium_Delhi_Day4_Morning-Session_19-Dec-10_Cam1','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48899,'1665_IE-Thiagaraja-Stadium_Delhi_Day4_Morning-Session_19-Dec-10_Cam2','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48900,'1666_Sharing_IE-Program_Thiagaraja-Stadium_Delhi_19-Dec-10','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48901,'1667_Mahasathsang-Palace-Ground_Bangalore_21-Dec-10_Cam1','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48902,'1668_Mahasathsang-Palace-Ground_Bangalore_21-Dec-10_Cam2','CA4220L4',1,null,'completed',80525),
(@counter := @counter +1,48903,'1669_Mahasathsang-Palace-Ground_Bangalore_21-Dec-10_Cam3','CA4220L4',1,null,'completed',80525);

-- **************** C16139L6 *************** - completed 80524
INSERT INTO `import` (id,`artifact_id`,`artifact_name`, `volume_id`,`requeue_id` , `message`, `status`, `request_id`) 
VALUES 
(@counter := @counter +1,48820,'13481_Surya-Kriya-Demo-Shoot_For-BSF-Program_Reference-Only_AYA-IYC_23-Oct-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48821,'13482_Surya-Kriya-Demo-Shoot_For-BSF-Program_Reference-Only_AYA-IYC_24-Oct-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48822,'13483_Surya-Kriya-Demo-Shoot_For-BSF-Program_Reference-Only_AYA-IYC_26-Oct-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48823,'13484_Surya-Kriya-Demo-Shoot_For-BSF-Program_AYA-IYC_28-Oct-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48824,'13485_Surya-Kriya-Demo-Shoot_For-BSF-Program_AYA-IYC_29-Oct-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48825,'13486_Throw-Ball-Match-For-Ladies_Isha-Craft-Vs-Isha-Ruchi_Mullankadu_29-Oct-2017_EX3','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48826,'13487_HYTT_Siddha-Introduction-1_Dr-Mangayar-Thayar_AYA-IYC_30-Oct-2017_EX1','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48827,'13488_HYTT_Siddha-Introduction-2_Dr-Mangayar-Thayar_AYA-IYC_30-Oct-2017_EX1','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48828,'13489_HYTT_Siddha-Introduction-3_Dr-Mangayar-Thayar_AYA-IYC_30-Oct-2017_EX1','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48829,'13490_Surya-Kriya-Demo-Shoot_For-BSF-Program_AYA-IYC_31-Oct-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48830,'13491_HYTT_Yogasanas-Demo_AYA-IYC_02-Nov-2017_EX1','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48831,'13492_HYTT_PA-Orientation_By-Ashwina-Akka_AYA-IYC_06-Nov-2017_EX3','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48832,'13493_HYTT_Participant-Sharing_SPH-IYC_08-Nov-2017_EX1','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48833,'13494_Isha-Golf-Jaunt_Morning_Ambience_Welcoming-Participants_Royal-Calcutta-Golf-Club-Kolkata_16-Dec-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48834,'13495_Isha-Golf-Jaunt_Morning_Sadhguru-Casual-Shots_Royal-Calcutta-Golf-Club_Kolkata_16-Dec-2017_Cam1_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48835,'13496_Isha-Golf-Jaunt_Morning_Sadhguru-Casual-Shots_Royal-Calcutta-Golf-Club_Kolkata_16-Dec-2017_Cam2_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48836,'13497_Isha-Golf-Jaunt_Morning_Inauguration_Royal-Calcutta-Golf-Club_Kolkata_16-Dec-2017_Cam1_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48837,'13498_Isha-Golf-Jaunt_Morning_Inauguration_Royal-Calcutta-Golf-Club_Kolkata_16-Dec-2017_Cam2_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48838,'13499_Isha-Golf-Jaunt_Morning-Noon_Sadhguru-Playing-Golf-With-Participants_Royal-Calcutta-Golf-Club_Kolkata_16-Dec-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48839,'13500_Isha-Golf-Jaunt_Evening_Interview-BTVI_Royal-Calcutta-Golf-Club-Kolkata_16-Dec-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48840,'13501_Isha-Golf-Jaunt_Evening_Interview_Neo-Sports_Royal-Calcutta-Golf-Club_Kolkata_16-Dec-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48841,'13502_Isha-Golf-Jaunt_Evening_Interview_Neo-Sports_Jaydeep-Chitlangia_Past-President-of-Indian-Golf-Union_Royal-Calcutta-Golf-Club-Kolkata_16-Dec-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48842,'13503_Isha-Golf-Jaunt_Evening_Closing-Session_Royal-Calcutta-Golf-Club-Kolkata_16-Dec-2017_Cam1_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48843,'13504_Isha-Golf-Jaunt_Evening_Closing-Session_Royal-Calcutta-Golf-Club-Kolkata_16-Dec-2017_Cam2_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48844,'13505_Isha-Golf-Jaunt_Evening_Sadhguru-Casual-Shots_Royal-Calcutta-Golf-Club-Kolkata_16-Dec-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48845,'13506_Power-of-We_ISAAME-Forum_Lions-Club_ITC-Sonar-Kolkata_17-Dec-2017_Cam1_FS7_4K','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48846,'13507_Power-of-We_ISAAME-Forum_Lions-Club_ITC-Sonar-Kolkata_17-Dec-2017_Cam2_FS7_4K','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48847,'13508_Bhairavi-Punya-Pooja_Talk_The-Westin-Kolkata-Rajarhat-Kolkata_17-Dec-2017_Cam1_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48848,'13509_Bhairavi-Punya-Pooja_Talk_The-Westin-Kolkata-Rajarhat-Kolkata_17-Dec-2017_Cam2_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48849,'13510_Bhairavi-Punya-Pooja_Talk_The-Westin-Kolkata-Rajarhat-Kolkata_17-Dec-2017_Cam3_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48850,'13511_Creating-A-Pollution-Free-Assam_Signing-of-MOU-With-Government-of-Assam_Srimanta-Sankardev-International-Auditorium-Guwahati_18-Dec-2017_Cam1_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48851,'13512_Creating-A-Pollution-Free-Assam_Signing-of-MOU-With-Government-of-Assam_Srimanta-Sankardev-International-Auditorium-Guwahati_18-Dec-2017_Cam2_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48852,'13513_Press-Meet_Sarbananda-Sonowal_CM-of-Assam_Srimanta-Sankardev-International-Auditorium-Guwahati_18-Dec-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48853,'13514_Press-Meet_Srimanta-Sankardev-International-Auditorium-Guwahati_18-Dec-2017_FS7','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48854,'13515_Saptarishi-Arati_Concert-By-Mohit-Chauhan_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam1_EX3','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48855,'13516_Saptarishi-Arati_Concert-By-Mohit-Chauhan_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam2_PMW-200','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48856,'13517_Saptarishi-Arati_Concert-By-Mohit-Chauhan_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam3_PMW-200','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48857,'13518_Saptarishi-Arati_Concert-By-Mohit-Chauhan_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam4_A7R3_4K_B-Rolls','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48858,'13519_Saptarishi-Arati_Sadhguru-Singing-With-Mohit-Chauhan_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam1_EX3','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48859,'13520_Saptarishi-Arati_Sadhguru-Singing-With-Mohit-Chauhan_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam2_PMW-200','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48860,'13521_Saptarishi-Arati_Sadhguru-Singing-With-Mohit-Chauhan_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam3_PMW-200','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48861,'13522_Saptarishi-Arati_Sadhguru-Singing-With-Mohit-Chauhan_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam4_FS7_4K','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48862,'13523_Saptarishi-Arati_Talk_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam1_EX3','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48863,'13524_Saptarishi-Arati_Talk_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam2_FS7_4K','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48864,'13525_Saptarishi-Arati_Talk_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam3_PMW-200','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48865,'13526_Saptarishi-Arati_Talk_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam4_PMW-200','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48866,'13527_Saptarishi-Arati_Talk_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam5_A7R3_4K_B-Rolls','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48867,'13528_Saptarishi-Arati_By-Kashi-Vishwanath-Temple-Priests_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam1_PMW-200','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48868,'13529_Saptarishi-Arati_By-Kashi-Vishwanath-Temple-Priests_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam2_FS7_4K','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48869,'13530_Saptarishi-Arati_By-Kashi-Vishwanath-Temple-Priests_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam3_EX1','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48870,'13531_Saptarishi-Arati_By-Kashi-Vishwanath-Temple-Priests_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam4_PMW-200','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48871,'13532_Saptarishi-Arati_By-Kashi-Vishwanath-Temple-Priests_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam5_EX3','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48872,'13533_Saptarishi-Arati_By-Kashi-Vishwanath-Temple-Priests_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam6_HDR-CX900E_B-Rolls','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48873,'13534_Saptarishi-Arati_By-Kashi-Vishwanath-Temple-Priests_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam7_A7R3_4K_B-Rolls','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48874,'13535_Saptarishi-Arati_By-Kashi-Vishwanath-Temple-Priests_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam8_Osmo_4K_B-Rolls','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48875,'13536_Saptarishi-Arati_By-Kashi-Vishwanath-Temple-Priests_112-Foot-Adiyogi-IYC_21-Dec-2017_Cam9_Drone_4K','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48876,'13537_Saptarishi-Arati_Sadhguru-Casual-Shots_Various-Activities_112-Foot-Adiyogi-IYC_21-Dec-2017_Osmo_4K','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48877,'13538_Saptarishi-Arati-By-Kashi-Vishwanath-Temple-Priests_Concert-By-Mohit-Chauhan_Sadhguru-Talk_112-Foot-Adiyogi-IYC_21-Dec-2017_Online-mix','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48878,'13539_Casual-Shots_Kashi-Vishwanath-Temple-Preists_112-Foot-Adiyogi-IYC_22-Dec-2017_EX1','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48879,'13540_Sharings-Hindi_Kashi-Vishwanath-Temple-Priests_112-Foot-Adiyogi-IYC_22-Dec-2017_EX1','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48880,'13541_Uttar-Pooja_By-Kashi-Vishwanath-Temple-Priests_112-Foot-Adiyogi-IYC_22-Dec-2017_EX3','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48881,'13542_Mystic-Kalinga_In-Conversation-With-Sadhguru-Conducted-By-Arundhathi-Subramaniam_Mayfair-Hotel-Bhubaneswar_22-Dec-2017_Cam1_FS7_4K','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48882,'13543_Mystic-Kalinga_In-Conversation-With-Sadhguru-Conducted-By-Arundhathi-Subramaniam_Mayfair-Hotel-Bhubaneswar_22-Dec-2017_Cam2_FS7_4K','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48883,'13544_Mystic-Kalinga-In-Conversation-With-Sadhguru-Conducted-By-Arundhathi-Subramaniam_Mayfair-Hotel-Bhubaneswar_22-Dec-2017_Cam3_PMW-320','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48884,'13545_Press-Meet_Mayfair-Hotel-Bhubaneswar_22-Dec-2017_PMW-320','C16139L6',1,null,'completed',80524),
(@counter := @counter +1,48885,'13546_Interview_OTV_Mayfair-Hotel-Bhubaneswar_22-Dec-2017_FS7','C16139L6',1,null,'completed',80524);

-- **************** C17031L7 *************** - failure
INSERT INTO `import` (id,`artifact_id`,`artifact_name`, `volume_id`,`requeue_id` , `message`, `status`, `request_id`) 
VALUES 
(@counter := @counter +1,48534,'25073_Sadhguru-Message-For-London-School-Of-Economics-About-Talibani-Controversy_iii-USA_02-Apr-2019_Cam1_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48535,'25074_Sadhguru-Message-For-London-School-Of-Economics-About-Talibani-Controversy_iii-USA_02-Apr-2019_Cam2_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48536,'25075_Sadhguru-Message-For-Ugadi_iii-USA_02-Apr-2019_Cam1_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48537,'25076_Sadhguru-Message-For-Ugadi_iii-USA_02-Apr-2019_Cam2_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48538,'25077_Cauvery-Calling_Day13-Chennai-Main-Event_Centenary-Auditorium-University-Of-Madras-Chennai_15-Sep-2019_Cellphone_B-Rolls','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48539,'25078_Cauvery-Calling_Day15-Closing-Event_CODISSIA-Cbe_17-Sep-2019_A7S2_B-Rolls','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48540,'25079_Cauvery-Calling_Day15-Crowd-Shots-At-Closing-Event_CODISSIA-Cbe_17-Sep-2019_Z280V_B-Rolls','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48541,'25080_Cauvery-Calling_Day15-Local-Village-People-Welcoming-Sadhguru_Coimbatore-To-Ashram_17-Sep-2019_A7S2','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48542,'25081_Cauvery-Calling_Day15-Press-Meet-At-Closing-Event_CODISSIA-Cbe_17-Sep-2019_GoPro','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48543,'25082_Cauvery-Calling_Day15-Sadhguru-Addressing-Ashramites_112-Foot-Adiyogi-IYC_17-Sep-2019_A7S2','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48544,'25083_Cauvery-Calling_Day15-Sadhguru-Arriving-Ashram_112-Foot-Adiyogi-IYC_17-Sep-2019_A7S2','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48545,'25084_Cauvery-Calling_Day15-Sadhguru-Bike-Riding-And-Addressing-Crowd-At-Various-Places_Coimbatore-To-IYC_17-Sep-2019_GoPro','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48546,'25085_Cauvery-Calling_Day15-Sadhguru-Bike-Riding-Shots_Chennai-To-Salem_17-Sep-2019_GoPro','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48547,'25086_Cauvery-Calling_Day15-Sadhguru-Farewell-Message-For-Bikers_Near-SPH-IYC_17-Sep-2019_A7S2','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48548,'25087_Cauvery-Calling_Day15-Sadhguru-Farewell-Message-For-Bikers_Near-SPH-IYC_17-Sep-2019_Cellphone','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48549,'25088_Cauvery-Calling_Day15-Sadhguru-Interview-With-Actress-Raai-Laxmi_Hotel-Radisson-Salem_17-Sep-2019_Cam3_Osmo','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48550,'25089_Cauvery-Calling_Day15-Sadhguru-Leaving-Texvalley-And-Arriving-CODISSIA_Texvalley-To-Cbe_17-Sep-2019_A7S2','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48551,'25090_Cauvery-Calling_Day15-Sadhguru-Meeting-With-Influencers_CODISSIA-Cbe_17-Sep-2019_A7S2_B-Rolls','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48552,'25091_Cauvery-Calling_Day15-Sadhguru-Meeting-With-Influencers_CODISSIA-Cbe_17-Sep-2019_Z280V_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48553,'25092_Cauvery-Calling_Day15-Sadhguru-Meeting-With-Influencers_CODISSIA-Cbe_17-Sep-2019_Z280V_B-Rolls','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48554,'25093_Cauvery-Calling_Day15-Sadhguru-Riding-And-Various-Shots_Salem-To-Coimbatore_17-Sep-2019_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48555,'25094_A-Seminar-On-Agroforestry-For-Karnataka-Farmers_Sharing-By-Farmers_Vidyaranya-Convention-Hall-Mysuru_14-Feb-2020_Cellphone','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48556,'25095_Adiyogi-Time-Lapse-Images_112-Foot-Adiyogi-IYC_26-Apr-2020_A7S2','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48557,'25096_Sadhguru-Interview-With-South-African-News-Channel-eNCA_Nalanda1-IYC_30-Apr-2020_Cam1_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48558,'25097_Sadhguru-Interview-With-South-African-News-Channel-eNCA_Nalanda1-IYC_30-Apr-2020_Cam2_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48559,'25098_With-Sadhguru-In-Challenging-Times-Series_Day40_SPH-Lawn-IYC_30-Apr-2020_Cam1_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48560,'25099_With-Sadhguru-In-Challenging-Times-Series_Day40_SPH-Lawn-IYC_30-Apr-2020_Cam2_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48561,'25100_With-Sadhguru-In-Challenging-Times-Series_Day40_SPH-Lawn-IYC_30-Apr-2020_Cam3_Z280V_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48562,'25101_With-Sadhguru-In-Challenging-Times-Series_Day40_SPH-Lawn-IYC_30-Apr-2020_Cam4_A7S2_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48563,'25102_With-Sadhguru-In-Challenging-Times-Series_Day40_SPH-Lawn-IYC_30-Apr-2020_Online-Mix','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48564,'25103_Shoot-For-Online-Monthly-Satsang_Nalanda1-IYC_30-Apr-2020_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48565,'25104_Adiyogi-Ambience-Shots-And-Time-Lapse-Images_112-Foot-Adiyogi-IYC_01-May-2020_Drone','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48566,'25105_Adiyogi-Time-Lapse-Images_112-Foot-Adiyogi-IYC_01-May-2020_A7S2','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48567,'25106_With-Sadhguru-In-Challenging-Times-Series_Day41_AYA-IYC_01-May-2020_Cam1_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48568,'25107_With-Sadhguru-In-Challenging-Times-Series_Day41_AYA-IYC_01-May-2020_Cam2_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48569,'25108_With-Sadhguru-In-Challenging-Times-Series_Day41_AYA-IYC_01-May-2020_Cam3_Z280V_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48570,'25109_With-Sadhguru-In-Challenging-Times-Series_Day41_AYA-IYC_01-May-2020_Online-Mix','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48571,'25110_Shoot-For-Linga-Bhairavi-Rituals-Training-Video_Linga-Bhairavi-IYC_01-May-2020_A6000','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48572,'25111_Sadhguru-Interaction-With-Tamilnadu-Police-Officials_Nalanda1-IYC_02-May-2020_Cam1_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48573,'25112_Sadhguru-Interaction-With-Tamilnadu-Police-Officials_Nalanda1-IYC_02-May-2020_Cam2_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48574,'25113_Sadhguru-Interaction-With-Tamilnadu-Police-Officials_Nalanda1-IYC_02-May-2020_Mix','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48575,'25114_Sadhguru-Interview-With-NDTV-For-The-Cycle-Of-Change-Show_Nalanda1-IYC_02-May-2020_Cam1_Z280V_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48576,'25115_Sadhguru-Interview-With-NDTV-For-The-Cycle-Of-Change-Show_Nalanda1-IYC_02-May-2020_Cam2_FS7_4K','C17031L7',1,null,'completed',80522),
-- Handled in failure section (@counter := @counter +1,48577,'25116_Sadhguru-Interview-With-NDTV-For-The-Cycle-Of-Change-Show_Nalanda1-IYC_02-May-2020_YouTube-Download','C17031L7',1,'Artifact size is 0','failed',80522);
(@counter := @counter +1,48578,'25118_Sadhguru-Message-For-May-Day-Event-Organized-By-Mike-Tyson_Nalanda1-IYC_02-May-2020_Cam2_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48579,'25119_With-Sadhguru-In-Challenging-Times-Series_Day42_AYA-IYC_02-May-2020_Cam1_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48580,'25120_With-Sadhguru-In-Challenging-Times-Series_Day42_AYA-IYC_02-May-2020_Cam2_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48581,'25121_With-Sadhguru-In-Challenging-Times-Series_Day42_AYA-IYC_02-May-2020_Cam3_Z280V_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48582,'25122_With-Sadhguru-In-Challenging-Times-Series_Day42_AYA-IYC_02-May-2020_Online-Mix','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48583,'25123_With-Sadhguru-In-Challenging-Times-Series_Day43_AYA-IYC_03-May-2020_Cam1_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48584,'25124_With-Sadhguru-In-Challenging-Times-Series_Day43_AYA-IYC_03-May-2020_Cam2_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48585,'25125_With-Sadhguru-In-Challenging-Times-Series_Day43_AYA-IYC_03-May-2020_Cam3_Z280V_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48586,'25126_With-Sadhguru-In-Challenging-Times-Series_Day43_AYA-IYC_03-May-2020_Cam4_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48587,'25127_With-Sadhguru-In-Challenging-Times-Series_Day43_AYA-IYC_03-May-2020_Online-Mix','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48588,'25128_Nada-Aradhana-And-Linga-Bhairavi-Arati-Live_IYC_03-May-2020_X70','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48589,'25129_Shoot-For-21-Day-Guided-Shambhavi-Tamil_Nalanda1-IYC_03-May-2020_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48590,'25130_COVID19-Coronavirus-Relief-Activity_Vegetables-Arriving_Suryakund-Mandapam-IYC_04-May-2020_A7S2','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48591,'25131_COVID19-Coronavirus-Relief-Activity_Vegetables-Arriving_Suryakund-Mandapam-IYC_04-May-2020_Drone','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48592,'25132_COVID19-Coronavirus-Relief-Activity_Vegetables-Arriving_Suryakund-Mandapam-IYC_04-May-2020_GoPro','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48593,'25133_COVID19-Coronavirus-Relief-Activity_Vegetables-Arriving_Suryakund-Mandapam-IYC_04-May-2020_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48594,'25134_Sadhguru-Playing-Frisbee_112-Foot-Adiyogi-IYC_04-May-2020_Drone','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48595,'25135_Sadhguru-Playing-Frisbee_112-Foot-Adiyogi-IYC_04-May-2020_Z280V_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48596,'25136_Sharing-By-New-Zealand-Model-Rachel-Hunter_Nalanda2-IYC_04-May-2020_A7S2_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48597,'25137_COVID19-Coronavirus-Relief-Activity_Food-Preparation_Akshaya-IYC_05-May-2020_A7S2_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48598,'25138_COVID19-Coronavirus-Relief-Activity_Food-Preparation_Akshaya-IYC_05-May-2020_GoPro','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48599,'25139_COVID19-Coronavirus-Relief-Activity_Food-Preparation_Akshaya-IYC_05-May-2020_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48600,'25140_COVID19-Coronavirus-Relief-Activity_Sharing-By-Brahmacharis-And-Volunteers_Suryakund-IYC_05-May-2020_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48601,'25141_COVID19-Coronavirus-Relief-Activity_Sharing-By-Swami-Yahu-And-Venkat_Akshaya-IYC_05-May-2020_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48602,'25142_COVID19-Coronavirus-Relief-Activity_Vegetables-Arriving_Suryakund-Mandapam-IYC_05-May-2020_Drone','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48603,'25143_COVID19-Coronavirus-Relief-Activity_Vegetables-Arriving-And-Cleaning_Suryakund-Mandapam-IYC_05-May-2020_A7S2_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48604,'25144_COVID19-Coronavirus-Relief-Activity_Vegetables-Arriving-And-Cleaning_Suryakund-Mandapam-IYC_05-May-2020_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48605,'25145_Sadhguru-Playing-Frisbee_112-Foot-Adiyogi-IYC_05-May-2020_Drone','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48606,'25146_Sadhguru-Playing-Frisbee_112-Foot-Adiyogi-IYC_05-May-2020_RX100','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48607,'25147_Sadhguru-Playing-Frisbee_112-Foot-Adiyogi-IYC_05-May-2020_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48608,'25148_Adiyogi-Divya-Darshan-Software-And-Playback-Setup-Files_IYC_06-May-2020_Content','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48609,'25149_COVID19-Coronavirus-Relief-Activity_Food-Packing_Near-Sarpavasal-IYC_06-May-2020_A7S2_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48610,'25150_COVID19-Coronavirus-Relief-Activity_Sharing-By-Volunteers_Sarpavasal-IYC_06-May-2020_Cam1_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48611,'25151_COVID19-Coronavirus-Relief-Activity_Sharing-By-Volunteers_Sarpavasal-IYC_06-May-2020_Cam2_A7S2','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48612,'25152_Shoot-For-Online-Monthly-Satsang-For-USA_Nalanda1-IYC_06-May-2020_Z280V','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48613,'25153_Adiyogi-Time-Lapse-Images_112-Foot-Adiyogi-IYC_07-May-2020_A7S2','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48614,'25154_Various-Activities-To-Prevent-Coronavirus-Infection_Various-Places-IYC_08-May-2020_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48615,'25155_Sadhguru-Interaction-With-Swachhata-Warriors_Nalanda1-IYC_08-May-2020_Cam1_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48616,'25156_Sadhguru-Interaction-With-Swachhata-Warriors_Nalanda1-IYC_08-May-2020_Cam2_FS7_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48617,'25157_Sadhguru-Interaction-With-Swachhata-Warriors_Nalanda1-IYC_08-May-2020_YouTube-Download','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48618,'25158_Sadhguru-Playing-Frisbee_112-Foot-Adiyogi-IYC_09-May-2020_A7S2_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48619,'25159_Sadhguru-Playing-Frisbee_112-Foot-Adiyogi-IYC_13-May-2020_A7S2_4K','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48620,'Z7566_Awareness-Video_Isha-Outreach_COVID19_For-community-Level-Workers_Tamil-DUB_03Mins-32Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48621,'Z7567_Awareness-Video_Isha-Outreach_COVID19_How-Soap-Kills-The-Coronavirus_Tamil-DUB_03Mins-52secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48622,'Z7568_Awareness-Video_Isha-Outreach_COVID19_How-To-Protect-Yourself-Against-COVID-19_Tamil-DUB_02Mins_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48623,'Z7569_Awareness-Video_Isha-Outreach_COVID19_Safely-Stop-Drinking-During-Lockdown_Tamil-DUB_01Min-15Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48624,'Z7570_Awareness-Video_Isha-Outreach_COVID19_When-And-How-To-Wear-Mask_Tamil-DUB_02Mins-45secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48625,'Z7571_Byte_Sadhguru-Singing-Shiv-Kailasho-Ke-Wasi-In-Car_Davos-Switzerland-21-Jan-2020_03Mins_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48626,'Z7572_Clip_COVID19_Police-Birthday-Surprise-To-A-Senior-Citizen-In-Lockdown-For-Sadhguru-Darshan_Hindi_01Min-47Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48627,'Z7573_Clip_Isha-Outreach_COVID19_Sadhguru-Auctions-His-Painting-To-Support-Corona-Relief-For-Instagram_39Secs_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48628,'Z7574_Clip_Peacock-Dancing-At-Mumbai_For-Sadhguru-Darshan-02-Apr-2020_30Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48629,'Z7575_Z7575_Clip_Rally-For-Rivers_Video-For-Mahashivaratri-2020-Stall_English_53Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48630,'Z7576_Daily-Mystic-Quote_01-Feb-2020_English_02Mins_Premiere-Pro-Trimmed','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48631,'Z7577_Daily-Mystic-Quote_01-May-2020_English_01Min-07Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48632,'Z7578_Daily-Mystic-Quote_02-May-2020_English_02Mins_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48633,'Z7579_Daily-Mystic-Quote_03-May-2020_English_01Min-22Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48634,'Z7580_Daily-Mystic-Quote_04-May-2020_English_01Min-14Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48635,'Z7581_Daily-Mystic-Quote_05-May-2020_English_55Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48636,'Z7582_Daily-Mystic-Quote_07-May-2020_English_01Min-29Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48637,'Z7583_Daily-Mystic-Quote_08-Apr-2020_English_53Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48638,'Z7584_Daily-Mystic-Quote_09-Apr-2020_English_01Min-31Secs_Premiere-Pro-Trimmed','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48639,'Z7585_Daily-Mystic-Quote_10-Apr-2020_English_02Mins-17Secs_Premiere-Pro-Trimmed','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48640,'Z7586_Daily-Mystic-Quote_11-Mar-2020_English_59Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48641,'Z7587_Daily-Mystic-Quote_14-Mar-2020_English_01Min-32Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48642,'Z7588_Daily-Mystic-Quote_15-Apr-2020_English_01Min-48Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48643,'Z7589_Daily-Mystic-Quote_16-Apr-2020_English_45Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48644,'Z7590_Daily-Mystic-Quote_17-Apr-2020_English_53Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48645,'Z7591_Daily-Mystic-Quote_20-Feb-2020_English_01Min-53Secs_MP4','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48646,'Z7592_Daily-Mystic-Quote_21-Apr-2020_English_01Min-04Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48647,'Z7593_Daily-Mystic-Quote_22-Apr-2020_English_01Min-33Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48648,'Z7594_Daily-Mystic-Quote_26-Apr-2020_English_02Mins-25Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48649,'Z7595_Daily-Mystic-Quote_27-Apr-2020_English_01MIn-51Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48650,'Z7596_Daily-Mystic-Quote_28-Apr-2020_English_02Mins-25Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48651,'Z7597_Daily-Mystic-Quote_29-Apr-2020_English_56Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48652,'Z7598_Daily-Mystic-Quote_30-Apr-2020_English_02Mins-08Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48653,'Z7599_Daily-Mystic-Quote_30-Jan-2020_English_02Mins-17Secs_Premiere-Pro-Trimmed','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48654,'Z7600_Documentary_World-Health-Day-2018-Video_Tips-To-Sleep_Tamil-DUB_14Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48655,'Z7601_Full-Talk_Sadhguru-Interaction-With-PHD-Chamber-Of-Commerce-And-Industry-Members-IYC-23-Apr-2020_English_48Mins_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48656,'Z7602_Full-Talk_Sadhgurus-Conversation-With-Doctors-And-Nurses-Of-DrReddys-Laboratories_Live-Interaction-With-Healthcare-Professionals-IYC-11-Apr-2020_English_53Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48657,'Z7603_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day1-IYC-22-Mar-2020_Tamil-AND-Telugu_41Mins-49Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48658,'Z7604_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day2-IYC-23-Mar-2020_Tamil_36Mins-09Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48659,'Z7605_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day3-IYC-24-Mar-2020_Tamil_50Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48660,'Z7606_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day4-IYC-25-Mar-2020_Tamil_41Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48661,'Z7607_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day5-IYC-26-Mar-2020_Tamil_41Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48662,'Z7608_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day6-IYC-27-Mar-2020_Tamil_44Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48663,'Z7609_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day7-IYC-28-Mar-2020_Tamil_42Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48664,'Z7610_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day8-IYC-29-Mar-2020_Tamil_41Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48665,'Z7611_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day9-IYC-30-Mar-2020_Tamil_45Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48666,'Z7612_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day10-IYC-31-Mar-2020_Tamil_45Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48667,'Z7613_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day11-IYC-01-Apr-2020_Tamil_42Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48668,'Z7614_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day12-IYC-02-Apr-2020_Tamil_40Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48669,'Z7615_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day13-IYC-03-Apr-2020_Tamil_40Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48670,'Z7616_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day14-IYC-04-Apr-2020_Tamil_42Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48671,'Z7617_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day15-IYC-05-Apr-2020_Tamil_42Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48672,'Z7618_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day16-IYC-06-Apr-2020_Tamil_42Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48673,'Z7619_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day17-IYC-07-Apr-2020_Tamil_45Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48674,'Z7620_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day18-IYC-08-Apr-2020_Tamil_40Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48675,'Z7621_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day19-IYC-09-Apr-2020_Tamil_48Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48676,'Z7622_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day20-IYC-10-Apr-2020_Tamil_41Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48677,'Z7623_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day21-IYC-11-Apr-2020_Tamil_47Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48678,'Z7624_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day22-IYC-12-Apr-2020_Tamil_42Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48679,'Z7625_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day23-IYC-13-Apr-2020_Tamil_43Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48680,'Z7626_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day24-IYC-14-Apr-2020_Tamil_43Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48681,'Z7627_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day25-IYC-15-Apr-2020_Tamil_48Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48682,'Z7628_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day26-IYC-16-Apr-2020_Tamil_45Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48683,'Z7629_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day27-IYC-17-Apr-2020_Tamil_43Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48684,'Z7630_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day28-IYC-18-Apr-2020_Tamil_46Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48685,'Z7631_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day29-IYC-19-Apr-2020_Tamil_55Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48686,'Z7632_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day30-IYC-20-Apr-2020_Tamil_43Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48687,'Z7633_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day31-IYC-21-Apr-2020_Tamil_46Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48688,'Z7634_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day32-IYC-22-Apr-2020_Tamil_43Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48689,'Z7635_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day33-IYC-23-Apr-2020_Tamil_44Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48690,'Z7636_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day34-IYC-24-Apr-2020_Tamil_48Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48691,'Z7637_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day35-IYC-25-Apr-2020_Tamil_45Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48692,'Z7638_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day36-IYC-26-Apr-2020_Tamil_59Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48693,'Z7639_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day37-IYC-27-Apr-2020_Tamil_43Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48694,'Z7640_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day38-IYC-28-Apr-2020_Tamil_45Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48695,'Z7641_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day39-IYC-29-Apr-2020_Tamil_52Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48696,'Z7642_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day40-IYC-30-Apr-2020_Tamil_47Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48697,'Z7643_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day41-IYC-01-May-2020_Tamil_46Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48698,'Z7644_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day42-IYC-02-May-2020_Tamil_48Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48699,'Z7645_Full-Talk_With-Sadhguru-In-Challenging-Times_Sadhguru-Darshan-Day43-IYC-03-May-2020_Tamil_01Hr-10Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48700,'Z7646_Fundraising-Video_COVID19_Isha-Outreach_Containing-COVID19-Whats-Isha-Been-Up-To_Tamil_03MIns-26Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48701,'Z7647_Fundraising-Video_COVID19_Isha-Outreach_Join-Ishas-COVID19-Community-Relief-Efforts_Tamil_03Mins-20Secs_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48702,'Z7648_Fundraising-Video_Isha-Outreach_COVID19_Join-Ishas-COVID19-Community-Relief-Efforts_English_03Mins-20Secs_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48703,'Z7649_Glimpses_IHS_Isha-Home-School-Celebrates-71st-Republic-Day_26-Jan-2020_English_02Mins-25Secs_Premiere-Pro_Trimmed','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48704,'Z7650_Glimpses_Indian-Yoga-Association-Convenes-At-Isha-Yoga-Center_IYC-10-Feb-2020_English_03Mins-07Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48705,'Z7651_Glimpses_With-Sadhguru-In-Challenging-Times_A-Tribute-To-All-Healthcare-Workers_English_04Mins-27Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48706,'Z7652_Glimpses_With-Sadhguru-In-Challenging-Times_A-Tribute-To-All-Healthcare-Workers_Tamil_04Mins-27Secs_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48707,'Z7653_Hatha-Yogi-Diaries-2019_Episode-07-21-Weeks-Of-Transformation_Version-02_English_13Mins-17Secs_Stems_UNAPPROVED','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48708,'Z7654_Message_COVID19_Sadhguru-Message-For-Bhakthi-TV_IYC-15-Apr-2020_English_01Mins-23Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48709,'Z7655_Message_COVID19_Sadhguru-Message-For-Hello-India-Magazine_IYC-07-Apr-2020_English_02Mins-15Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48710,'Z7656_Message_COVID19_Sadhguru-Message-On-Coronavirus-Pandemic_Dhyanalinga-14-Apr-2020_English_28Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48711,'Z7657_Monthly-Satsang-Video_Guru-Pournami_For-May-2020_Satsang-With-Sadhguru-IYC-16-Jul-2019_Tamil_15Mins-18Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48712,'Z7658_Monthly-Satsang-Video_Improvement-In-Spirituality_For-May-2020_Tamil-DUB_24Mins-11Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48713,'Z7659_Poem_Dirty-Little-Logic_Sadhguru-Darshan-Day29-IYC-19-Apr-2020_English_01Min-25Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48714,'Z7660_Poem_Lilt_With-Sadhguru-In-Challenging-Times-Series-Day13-IYC-03-Apr-2020_English_01Min-29Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48715,'Z7661_Poem_Lock-Down-Or-Lock-Up_Sadhguru-Darshan-Day29-IYC-19-Apr-2020_Tamil_01Min-37Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48716,'Z7662_Poem_Summer_Sadhguru-Darshan-Day31-IYC-21-Apr-2020_English_01Min-27Secs_MOV','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48717,'Z7663_Practice_IPC_Infinity-Guided-Meditation-From-Sadhguru-13Mins_In-The-Lap-Of-The-Master-Day1-Morning-IYC-28-Jul-2018_English_34Mins_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48718,'Z7664_Practice_IPC_Infinity-Process-AND-Infinity-Walking-AND-Infinity-Process-Guidelines_In-The-Lap-Of-The-Master-Day1-AND-Day2-Morning-IYC-28-29-Jul-2018_English_25Mins-AND-13Mins-AND-17Mins_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48719,'Z7665_Practice_IPC_Yoga-Yoga-Yogeshwaraya-Chant_With-Sadhguru-In-Challenging-Times-Day4-IYC-25-Mar-2020_English-AND-Tamil_11MIns_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48720,'Z7666_Practice_IPC_Yoga-Yoga-Yogeshwaraya-Chant-And-Isha-Kriya_With-Sadhguru-In-Challenging-Times-Day4-IYC-25-Mar-2020_English-AND-Tamil_25Mins_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48721,'Z7667_Practice_Mahashivaratri-2016_Mahashivaratri-Sadhana-Tools-For-Transformation_English-And-Telugu_17Mins_FCPX-Bundle_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48722,'Z7668_Practice_With-Sadhguru-In-Challenging-Times_Simha-Kriya_Sadhguru-Darshan-Day7-IYC-28-Mar-2020_English-AND-Tamil_12Mins_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48723,'Z7669_Promo_Athanaikkum-Aasaippadu_Book-Video_What-To-Do-To-Get-What-We-Desire_Tamil_05Mins-22Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48724,'Z7670_Promo_Death-An-Inside-Story-A-Book-For-All-Those-Who-Shall-Die_English_01MIn-11Secs_Endslide-Revised_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48725,'Z7671_Promo_How-Copper-Can-Protect-Against-Corona_English-AND-Tamil_03Mins-39Secs_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48726,'Z7672_Promo_How-To-Become-Ten-Percent-Better_Sadhguru-Interview-With-Behindwoods-IYC-18-Apr-2020_Tamil_03Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48727,'Z7673_Promo_IPC_Kadavule_Sadhguruvudan-Isha-Yoga-Chennai-18-19-Dec-2019_Tamil_01Min-25Secs_MP4','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48728,'Z7674_Promo_Jeevarasam_English_04Mins_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48729,'Z7675_Promo_Linga-Bhairavi_Yantra-Ceremony-Endslide-Updated_English_03Mins_MP4','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48730,'Z7676_Promo_Simha-Kriya_Tamil_53Secs_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48731,'Z7677_Promo_Simha-Kriya_Telugu_51Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48732,'Z7678_Promo_With-Sadhguru-In-Challenging-Times_Diya-For-Corona_Sadhguru-Darshan-Day14-IYC-04-Apr-2020_English-AND-Tamil_06Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48733,'Z7679_Sharings_IPC_Inner-Engineering-Retreat-Participants_Nalanda-IYC-08-Oct-2019_Tamil_Consolidated_UNFINISHED','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48734,'Z7680_Song_Corona-Awareness-Song-With-English-SUB_Tamil_03Mins-34secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48735,'Z7681_Song_Sounds-Of-Isha_Nada-Aradhana-Vol-4-Handpan-Guitar-And-Vocals_12Mins_Premiere-Pro-Trimmed','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48736,'Z7682_Song_Sounds-Of-Isha_Samarpan-Thank-You-Helpers-Musical-Tribute-To-The-Corona-Warriors_Hindi_03Mins-47Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48737,'Z7683_Talk_Corona-Cure-Is-The-biggest-Challenge_Sadhguru-Interview-With-Behindwoods-IYC-18-Apr-2020_Tamil_36Mins-47Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48738,'Z7684_Talk_Coronavirus-Isha-Controversies-And-More_Sadhguru-Interview-With-Actor-Santhanam-IYC-18-Apr-2020_Tamil_28Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48739,'Z7685_Talk_Do-This-One-Thing-To-Get-Clarity-Like-Sadhguru_Discourse-For-Dharma-Civilization-Foundation-USA-04-Oct-2015_English_13Mins-32Secs_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48740,'Z7686_Talk_Health-For-Healthcare-Workers_Sadhguru-Skype-Interaction-With-PGIMER-Medical-Professionals-IYC-15-Apr-2020_English_01Hr-04Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48741,'Z7687_Talk_How-Can-I-Quit-Smoking_Inner-Engineering-Coimbatore-13-Jul-2003-AND-Youth-And-Truth-Canada-12-Nov-2019_English_10Mins-25Secs_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48742,'Z7688_Talk_How-Can-You-Always-Have-Access-To-Sadhguru_Ananda-Alai-Satsang-Hyderabad-02-Aug-2009_English_07Mins-24Secs_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48743,'Z7689_Talk_How-Many-Weeks-Into-Pregnancy-Can-Abortion-Be-Considered_Prathima-Institute-Of-Medical-Sciences-Karimnagar-24-Jun-2019_Tamil_06Mins-35Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48744,'Z7690_Talk_IPC_Buddha-And-His-Enlightenment_Buddha-Poornima-USA-05-May-2012_English_17Mins-20Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48745,'Z7691_Talk_IPC_Sadhanapada-Participant-Question-On-Balance-Clarity-And-Intensity_Sadhguru-Darshan-IYC-11-Feb-2020_English_12Mins-40Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48746,'Z7692_Talk_Isha-Leadership-Academy_Video-For-ETHRWorld-Online-Session_English_07Mins_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48747,'Z7693_Talk_Mahashivaratri-2020_07-Things-You-Must-Do-And-Must-Not-Do-On-Mahashivaratri_Tamil_06Mins-11Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48748,'Z7694_Talk_Message-For-The-Russian-People-In-These-Challenging-Times_Sadhguru-Darshan-IYC-15-Mar-2020_04Mins-18Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48749,'Z7695_Talk_Nilavembu-Kashayam_Sadhguru-Live-Interaction-With-Healthcare-Professionals-IYC-11-Apr-2020_English_03Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48750,'Z7696_Talk_Press-Conference-One-Trillion-Trees-Sadhguru_World-Economic-Forum-Davos-Switzerland-22-Jan-2020_English_06Mins-49Secs_Premiere-Pro-Trimmed','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48751,'Z7697_Talk_Shivanga-Sadhana-Culmination-For-Ladies_Sadhguru-Session-IYC-08-Feb-2020_English_16Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48752,'Z7698_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-01_IYC-22-Mar-2020_English_22Mins_Unconsolidted','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48753,'Z7699_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-02_IYC-27-Mar-2020_English_21Mins-35Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48754,'Z7700_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-03_IYC-30-Mar-2020_English_21Mins-21Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48755,'Z7701_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-04_IYC-30-Mar-2020_English_24Mins-42Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48756,'Z7702_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-05_IYC-01-Apr-2020_English_21Mins-09Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48757,'Z7703_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-06_IYC-02-Apr-2020_English_25Mins-30Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48758,'Z7704_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-07_IYC-02-Apr-2020_English_24Mins-20Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48759,'Z7705_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-08_IYC-04-Apr-2020_English_25Mins-08Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48760,'Z7706_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-09_IYC-04-Apr-2020_English_25Mins-35Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48761,'Z7707_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-10_IYC-06-Apr-2020_English_24Mins-01Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48762,'Z7708_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-11_Dhyanalinga-IYC-07-Apr-2020_English_23Mins-29Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48763,'Z7709_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-12_Dhyanalinga-IYC-07-Apr-2020_English_22Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48764,'Z7710_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-13_IYC-10-Apr-2020_English_24Mins-53Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48765,'Z7711_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-14_Nandi-IYC-10-Apr-2020_English_23Mins-52Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48766,'Z7712_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-15_IYC-10-Apr-2020_English_20Mins-32Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48767,'Z7713_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-16_IYC-10-Apr-2020_English_21Mins-36Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48768,'Z7714_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-17_Dhyanalinga-IYC-14-Apr-2020_English_22Mins-56Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48769,'Z7715_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-18_Dhyanalinga-IYC-14-Apr-2020_English_21Mins-43Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48770,'Z7716_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-19_Dhyanalinga-IYC-14-Apr-2020_English_22Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48771,'Z7717_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-20_IYC-17-Apr-2020_English_25Mins-37Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48772,'Z7718_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-21_IYC-18-Apr-2020_English_25Mins-24Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48773,'Z7719_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-22_IYC-20-Apr-2020_English_23Mins-05Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48774,'Z7720_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-23_IYC-20-Apr-2020_English_21Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48775,'Z7721_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-24_IYC-22-Apr-2020_English_21Mins-37Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48776,'Z7722_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-25_IYC-23-Apr-2020_English_21Mins-32Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48777,'Z7723_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-26_Sadhguru-Darshan-IYC-23-AND-24-Apr-2020_English_21Mins-52Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48778,'Z7724_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-27_Darshan-30-Oct-2012-AND-Interview-Mumbai-09-Jun-2015_English_24Mins-57Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48779,'Z7725_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-28_English_21Mins-08Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48780,'Z7726_Talk_TIMES-NOW_Sadhguru-For-Challenging-Times-Episode-29_IYC-27-Apr-2020_English_24Mins-12Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48781,'Z7727_Talk_Upgrade-Yourself-By-10Percent-Make-The-Lockdown-Count_Sadhguru-Darshan-Day21-IYC-12-Apr-2020-AND-Sadhguru-Interview-With-India-Today-IYC-13-Apr-2020_English_03Mins-41Secs_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48782,'Z7728_Talk_Video-For-Ajantha-Pharma-Healthcare-Professionals_Sadhguru-Live-Interaction-With-Healthcare-Professionals-IYC-11-Apr-2020_English_20Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48783,'Z7729_Talk_Video-For-Operation-Earth-From-China_With-Sadhguru-In-Challenging-Times-IYC-30-Mar-2020-AND-03-Apr-2020_English_16Mins-16Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48784,'Z7730_Talk_With-Sadhguru-In-Challenging-Times_Can-Board-Games-Make-You-Meditative_Sadhguru-Darshan-Day20-IYC-10-Apr-2020_English_08Mins-45Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48785,'Z7731_Talk_With-Sadhguru-In-Challenging-Times_Can-Everyone-Experience-The-Creator_Sadhguru-Darshan-Day42-IYC-02-May-2020_English_12Mins-25Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48786,'Z7732_Talk_With-Sadhguru-In-Challenging-Times_Can-I-Disagree-With-My-Gurus-Political-Opinion_Sadhguru-Darshan-Day16-IYC-06-Apr-2020_English_18Mins_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48787,'Z7733_Talk_With-Sadhguru-In-Challenging-Times_Can-I-Have-Two-Gurus_Sadhguru-Darshan-Day25-IYC-15-Apr-2020_English_11Mins-31Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48788,'Z7734_Talk_With-Sadhguru-In-Challenging-Times_Chant-And-Isha-Kriya-Intro-For-Sadhguru-App_Sadhguru-Darshan-Day2-IYC-23-Mar-2020_English_AND-Tamil_04Mins-25Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48789,'Z7735_Talk_With-Sadhguru-In-Challenging-Times_Corona-Doesnt-Want-To-Kill-You_Sadhguru-Darshan-Day3-IYC-24-Mar-2020_English-AND-Tamil_13Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48790,'Z7736_Talk_With-Sadhguru-In-Challenging-Times_Donation-Appeal_Sadhguru-Darshan-Day14-IYC-14-Apr-2020_English_03Mins-44Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48791,'Z7737_Talk_With-Sadhguru-In-Challenging-Times_Dont-Let-Your-Aliveness-Go-Down_Sadhguru-Darshan-Day38-IYC-28-Apr-2020_English_21Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48792,'Z7738_Talk_With-Sadhguru-In-Challenging-Times_Enlightened-Person-Has-More-Karma-Than-Others_Sadhguru-Darshan-Day32-IYC-22-Apr-2020_English-AND-Tamil_18Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48793,'Z7739_Talk_With-Sadhguru-In-Challenging-Times_Home-Remedies_Sadhguru-Darshan-Day3-IYC-24-Mar-2020_Tamil_06Mins-21Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48794,'Z7740_Talk_With-Sadhguru-In-Challenging-Times_How-Does-Each-Deity-Function-Differently_Sadhguru-Darshan-Day27-IYC-17-Apr-2020_English_10Mins-24Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48795,'Z7741_Talk_With-Sadhguru-In-Challenging-Times_How-To-Connect-With-Devi-During-The-Lockdown_Sadhguru-Darshan-Day17-IYC-07-Apr-2020_English_11Mins-31Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48796,'Z7742_Talk_With-Sadhguru-In-Challenging-Times_How-To-Deal-With-Abusive-Partner_Sadhguru-Darshan-Day43-IYC-03-May-2020_English_14Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48797,'Z7743_Talk_With-Sadhguru-In-Challenging-Times_How-To-Deal-With-Domestic-Violence_Sadhguru-Darshan-Day18-IYC-08-Apr-2020_English_06Mins-30Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48798,'Z7744_Talk_With-Sadhguru-In-Challenging-Times_How-To-Deal-With-Domestic-Violence_Sadhguru-Darshan-Day19-IYC-09-Apr-2020_English_11Mins-38Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48799,'Z7745_Talk_With-Sadhguru-In-Challenging-Times_How-To-Deal-With-Lazy-Partners_Sadhguru-Darshan-Day34-IYC-24-Apr-2020_English_09Mins-06Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48800,'Z7746_Talk_With-Sadhguru-In-Challenging-Times_How-To-Experience-Sadhgurus-Presence-From-Anywhere_Sadhguru-Darshan-Day43-IYC-03-Apr-2020_English_10Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48801,'Z7747_Talk_With-Sadhguru-In-Challenging-Times_IEO-Benefits_Sadhguru-Darshan-Day12-IYC-02-Apr-2020_English_04Mins-52Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48802,'Z7748_Talk_With-Sadhguru-In-Challenging-Times_IEO-Benefits_Sadhguru-Darshan-Day12-IYC-02-Apr-2020_Tamil_04Mins-52Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48803,'Z7749_Talk_With-Sadhguru-In-Challenging-Times_Is-End-Of-The-World-Near_Sadhguru-Darshan-Day1-IYC-22-Mar-2020_English-AND-Tamil_02Mins-50Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48804,'Z7750_Talk_With-Sadhguru-In-Challenging-Times_Magic-Of-Three-Words_Sadhguru-Darshan-Day5-IYC-26-Mar-2020_English-AND-Tamil_17Mins-30Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48805,'Z7751_Talk_With-Sadhguru-In-Challenging-Times_Rockstar_Sadhguru-Darshan-Day2-IYC-23-Mar-2020_English-AND-Tamil_05Mins-19Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48806,'Z7752_Talk_With-Sadhguru-In-Challenging-Times_Should-Old-People-Live-Alone_Sadhguru-Darshan-Day39-IYC-29-Apr-2020_English_11Mins_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48807,'Z7753_Talk_With-Sadhguru-In-Challenging-Times_Simple-Process-To-Bring-Absolute-Stillness-And-Phenomenal-Dynamism_Sadhguru-Darshan-Day18-IYC-08-Apr-2020_English-AND-Tamil_22Mins_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48808,'Z7754_Talk_With-Sadhguru-In-Challenging-Times_Was-Corona-Pandemic-Predicted-Centuries-Ago_Sadhguru-Darshan-Day19-IYC-09-Apr-2020_English_09Mins-41Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48809,'Z7755_Talk_With-Sadhguru-In-Challenging-Times_What-Is-The-Difference-Between-Devotion-And-Belief_Sadhguru-Darshan-Day27-IYC-17-Apr-2020_English_07Mins-36Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48810,'Z7756_Talk_With-Sadhguru-In-Challenging-Times_What-To-Do-When-Its-Quiet-Outside-But-Noisy-Within_Sadhguru-Darshan-Day6-IYC-27-Mar-2020_English-AND-Tamil_17Mins-30Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48811,'Z7757_Talk_With-Sadhguru-In-Challenging-Times_Why-Is-Shiva-Not-Saving-Us_Sadhguru-Darshan-Day39-IYC-29-May-2020_English_05Mins-15Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48812,'Z7758_Talk_With-Sadhguru-In-Challenging-Times_Why-Loved-Ones-Fight-With-Each-Other_Sadhguru-Darshan-Day21-IYC-12-Apr-2020_English_14Mins-22Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48813,'Z7759_Talk_With-Sadhguru-In-Challenging-Times_Why-Were-Feminine-Temples-Destroyed-In-The-World_Sadhguru-Darshan-Day33-IYC-23-Apr-2020_English-AND-Tamil_11Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48814,'Z7760_Talk_With-Sadhguru-In-Challenging-Times_Why-Yellow-Media-Is-Against-Sadhguru_Sadhguru-Darshan-Day15-IYC-05-Apr-2020_Tamil_12Mins_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48815,'Z7761_Testimonial_HYTT_Manasi-Puri-Experiences-Isha-Hatha-Yoga-Teacher-Training-2019_IYC-07-Dec-2019_English_06Mins_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48816,'Z7762_Training_Linga-Bhairavi_Bhuta-Shuddhi-Vivaha-Audio-Training_English_08Mins-39Secs_Consolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48817,'Z7763_Upgrade-Videos_Linga-Bhairavi_Bhuta-Shuddhi-Vivaha-Training_Sadhguru-Session-IYC-05-Dec-2018_English_17Mins-44Secs_Unconsolidated','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48818,'Z7764_Webseries_Sadhguru-On-Great-Beings_Episode-01-Matsyendranaths-Lesson-For-Gorakhnath-On-Occult_Tamil_09Mins_Stems','C17031L7',1,null,'completed',80522),
(@counter := @counter +1,48819,'Z7765_Webseries_Sadhguru-On-Great-Beings_Episode-02-Mayamma-The-Mysterious-Lady-Saint_English_07Mins_Stems','C17031L7',1,null,'completed',80522);

-- attend to the failures...
INSERT INTO `import` (id,`artifact_id`,`artifact_name`, `volume_id`,`requeue_id` , `message`, `status`, `request_id`) 
VALUES 
(@counter := @counter +1,48577,'25116_Sadhguru-Interview-With-NDTV-For-The-Cycle-Of-Change-Show_Nalanda1-IYC_02-May-2020_YouTube-Download','C17031L7',1,'Artifact size is 0','completed_failures',80522);

INSERT INTO `dwara_sequences` (`primary_key_fields`, `current_val`) VALUES ('import_id', @counter);

-- drop table import_volume_artifact
drop table import_volume_artifact;

-- move finalizedDate to details...
/*
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2017-12-31T00:00:00\"}', `finalized_at`=null WHERE `id`='C16139L6';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2020-05-06T00:00:00\"}', `finalized_at`=null WHERE `id`='C17027L6';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2020-05-16T00:00:00\"}', `finalized_at`=null WHERE `id`='C17031L7';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2010-12-29T00:00:00\"}', `finalized_at`=null WHERE `id`='CA4220L4';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2020-05-14T00:00:00\"}', `finalized_at`=null WHERE `id`='P17023L6';
*/

ALTER TABLE `volume` 
CHANGE COLUMN `initialized_at` `first_written_at` DATETIME(6) NULL DEFAULT NULL ,
CHANGE COLUMN `finalized_at` `last_written_at` DATETIME(6) NULL DEFAULT NULL ;



-- volume end block is not needed for import
update file_volume set volume_end_block = null where volume_end_block is not null and volume_id in ('C16139L6','C17027L6','C17031L7','CA4220L4','P17023L6');

/*
 * NEED to add unique constraint on artifact.sequence_code but right now we have problem with our data - Need clean up
 * 
select sequence_code, artifactclass_id, count(sequence_code) from artifact 
-- where artifactclass_id not like '%low' 
group by sequence_code , artifactclass_id having count(sequence_code) > 1; 
ALTER TABLE `artifact` ADD UNIQUE INDEX `sequence_code_UNIQUE` (`sequence_code` ASC);
*/

SET foreign_key_checks = 1;