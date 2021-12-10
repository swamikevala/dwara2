SET foreign_key_checks = 0;

-- Domain changes - missed out dropping the below table
DROP TABLE `domain`;

-- Import changes - move finalizedDate to details...
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2017-12-31 00:00:00.000000\"}', `finalized_at`=null WHERE `id`='C16139L6';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2020-05-06 00:00:00.000000\"}', `finalized_at`=null WHERE `id`='C17027L6';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2020-05-16 00:00:00.000000\"}', `finalized_at`=null WHERE `id`='C17031L7';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2010-12-29 00:00:00.000000\"}', `finalized_at`=null WHERE `id`='CA4220L4';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2020-05-14 00:00:00.000000\"}', `finalized_at`=null WHERE `id`='P17023L6';

DROP TABLE `import`;

ALTER TABLE `import_volume_artifact` ADD COLUMN `artifact_id` INT(11) NULL AFTER `message`, RENAME TO `t_artifact_volume_import`;

-- purge the completed ones
delete from t_artifact_volume_import where volume_id in ('C16139L6','C17027L6','C17031L7','CA4220L4');

-- update artifact id 
UPDATE `t_artifact_volume_import` SET `artifact_id`=49032 WHERE `artifact_name`='Z7436_Resi-Meet_HD_2016-Dec-12_Spandhahall_Edited-Files' and `volume_id`='P17023L6';	
UPDATE `t_artifact_volume_import` SET `artifact_id`=49033 WHERE `artifact_name`='Z7437_Resi-Meet_HD4K_2019-Dec-16_AYA-IYC_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49034 WHERE `artifact_name`=' Z7438_Resi-Meet_HD4K_2020-Mar-22_Nalanda-Grounds-IYC_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49035 WHERE `artifact_name`='Z7439_Meditators-Sathsang_DV_M211-To-M216_MD_M104_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49036 WHERE `artifact_name`='Z7440_Meditators-Sathsang_DV_M217-To-M223_MD_M000_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49037 WHERE `artifact_name`='Z7441_Meditators-Sathsang_DV_M224-To-M225_MD_M101_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49038 WHERE `artifact_name`='Z7442_Meditators-Sathsang_DV_M226-To-M229_MD_M000_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49039 WHERE `artifact_name`='Z7443_Meditators-Sathsang_DV_M230-To-M233_MD_M030_M100_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49040 WHERE `artifact_name`='Z7444_Meditators-Sathsang_DV_M234-To-M235_MD_M107_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49041 WHERE `artifact_name`='Z7445_Meditators-Sathsang_DV_M236-To-M238_MD_M000_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49042 WHERE `artifact_name`='Z7446_Meditators-Sathsang_DV_M239-To-M240_MD_M000_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49043 WHERE `artifact_name`='Z7447_Meditators-Sathsang_DV_M241-To-M243_MD_M000_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49044 WHERE `artifact_name`='Z7448_Meditators-Sathsang_DV_M244-To-M245_MD_M105-To-M106_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49045 WHERE `artifact_name`='Z7449_Meditators-Sathsang_DV_M246-To-M252_MD_M108-To-M109_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49046 WHERE `artifact_name`='Z7450_Meditators-Sathsang_DV_M253-To-M260_MD_M110-To-M111_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49047 WHERE `artifact_name`='Z7451_Meditators-Sathsang_DV_M261-To-M266_MD_M139_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49048 WHERE `artifact_name`='Z7452_Meditators-Sathsang_DV_M267-To-M270_MD_M113_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49049 WHERE `artifact_name`='Z7453_Meditators-Sathsang_DV_M271-To-M279_MD_M116_M117_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49050 WHERE `artifact_name`='Z7454_Meditators-Sathsang_DV_M280-To-M282_MD_N193_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49051 WHERE `artifact_name`='Z7455_Meditators-Sathsang_DV_M283_MD_M114_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49052 WHERE `artifact_name`='Z7456_Meditators-Sathsang_DV_M284-To-M288_MD_M118_M119_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49053 WHERE `artifact_name`='Z7457_Meditators-Sathsang_DV_M289_MD_M515_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49054 WHERE `artifact_name`='Z7458_Meditators-Sathsang_DV_M290-To-M293_MD_M121_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49055 WHERE `artifact_name`='Z7459_Meditators-Sathsang_DV_M294-To-M295_M615-To-M616_MD_M058_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49056 WHERE `artifact_name`='Z7460_Meditators-Sathsang_DV_M296-To-M298_Same-In_M036_MD_M057_Edited-Files' and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET `artifact_id`=49057 WHERE `artifact_name`='Z7461_Meditators-Sathsang_DV_M299-To-M302_MD-M453_AA04-To-AA05_Edited-Files' and `volume_id`='P17023L6';

-- attend to the failures...
UPDATE `t_artifact_volume_import` SET status='failed' and message='Artifact Name has special characters'  WHERE `artifact_id`=49034 and `volume_id`='P17023L6';
UPDATE `t_artifact_volume_import` SET status='failed' and message='Artifact size is less than 1MiB'  WHERE `artifact_id`=49034 and `volume_id`='P17023L6';

-- update request status 
update request set status = 'completed_failures' where action_id='import' and json_extract(details, '$.body.xmlPathname') like "%P17023L6%"

SET foreign_key_checks = 1;