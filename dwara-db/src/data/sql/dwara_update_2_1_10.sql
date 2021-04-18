SET foreign_key_checks = 0; 

-- Edited (Translations) support

-- SEQUENCE --
INSERT INTO `sequence` (`id`, `type`, `prefix`, `code_regex`, `number_regex`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`, `force_match`, `keep_code`, `replace_code`) VALUES 
('video-edit-tr-grp', 'artifact', null, null, null, 1, 1, -1, 0, null, 0, 0, 0),
('video-edit-tr-pub', 'artifact', 'ZT', '^[A-Z]{3}[a-z]{3}[A-Z]{2}\\d{6}(?=_)', null, 0, null, null, null,'video-edit-tr-grp', 0, 0, 0),
('video-edit-tr-priv2', 'artifact', 'ZTX', '^[A-Z]{3}[a-z]{3}[A-Z]{2}\\d{6}(?=_)', null, 0, null, null, null,'video-edit-tr-grp', 0, 0, 0),
('video-edit-tr-pub-proxy-low', 'artifact', 'ZTL', '^ZT\\d+(?=_)', '(?<=^ZT)\\d+(?=_)', 0, null, null, null, null, 1, 0, 1),
('video-edit-tr-priv2-proxy-low', 'artifact', 'ZTXL', '^ZTX\\d+(?=_)', '(?<=^ZTX)\\d+(?=_)', 0, null, null, null, null, 1, 0, 1);

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `domain_id`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('video-edit-tr-pub', 'edited video tr', 1, 'video-edit-tr-pub', 1, 1, 1, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"^([^/]+/?){1,2}$|^[^/]+/Outputs?/[^/]+\\\\.mov$\"}'),
('video-edit-tr-priv1', 'edited video tr', 1, 'video-edit-tr-pub', 1, 1, 2, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"^([^/]+/?){1,2}$|^[^/]+/Outputs?/[^/]+\\\\.mov$\"}'),
('video-edit-tr-priv2', 'edited video tr', 1, 'video-edit-tr-priv2', 1, 1, 3, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"^([^/]+/?){1,2}$|^[^/]+/Outputs?/[^/]+\\\\.mov$\"}'),
('video-edit-tr-pub-proxy-low', 'edited video tr proxy', 1, 'video-edit-tr-pub-proxy-low', 0, 1, 0, '/data/dwara/transcoded', 'video-edit-tr-pub', 0, null),
('video-edit-tr-pub-proxy-priv1', 'edited video tr proxy', 1, 'video-edit-tr-pub-proxy-low', 0, 1, 0, '/data/dwara/transcoded', 'video-edit-tr-priv1', 0, null),
('video-edit-tr-pub-proxy-priv2', 'edited video tr proxy', 1, 'video-edit-tr-priv2-proxy-low', 0, 1, 0, '/data/dwara/transcoded', 'video-edit-tr-priv2', 0, null);

-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('video-edit-tr-pub', 'E1', 0, 1),
('video-edit-tr-pub', 'E2', 0, 1),
('video-edit-tr-pub', 'E3', 0, 1),
('video-edit-tr-priv1', 'E1', 0, 1),
('video-edit-tr-priv1', 'E2', 0, 1),
('video-edit-tr-priv1', 'E3', 0, 1),
('video-edit-tr-priv2', 'X1', 0, 1),
('video-edit-tr-priv2', 'X2', 0, 1),
('video-edit-tr-priv2', 'X3', 0, 1),
('video-edit-tr-pub-proxy-low', 'G1', 0, 1),
('video-edit-tr-pub-proxy-low', 'G2', 0, 1),
('video-edit-tr-priv1-proxy-low', 'G1', 0, 1),
('video-edit-tr-priv1-proxy-low', 'G2', 0, 1),
('video-edit-tr-priv2-proxy-low', 'G1', 0, 1),
('video-edit-tr-priv2-proxy-low', 'G2', 0, 1);

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'video-edit-tr-pub', 1),
('ingest', 'video-edit-tr-priv1', 1),
('ingest', 'video-edit-tr-priv2', 1),
('ingest', 'video-edit-tr-pub', 2),
('ingest', 'video-edit-tr-priv1', 2),
('ingest', 'video-edit-tr-priv2', 2),
('ingest', 'video-edit-tr-pub', 3),
('ingest', 'video-edit-tr-priv1', 3),
('ingest', 'video-edit-tr-priv2', 3),
('ingest', 'video-edit-tr-pub', 6),
('ingest', 'video-edit-tr-priv1', 6),
('ingest', 'video-edit-tr-priv2', 6);

-- FLOW --
INSERT INTO `flow` ( `id`, `description`) VALUES
('video-edit-tr-proxy-flow', 'modified video-proxy-flow targeting specific output paths used by ILP and GLP');

-- FLOW_ELEMENT --
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `flow_ref_id`, `processingtask_id`, `storagetask_action_id`, `task_config`) VALUES
('U26', 1, null, 0, 1, 'video-edit-tr-proxy-flow', null, 'video-proxy-low-gen', null, '{\"pathname_regex\": \"(Video Output/|Output_)[^/]+\\\\.(mov|mp4)\"}'),
('U27', 1, '["U26"]', 0, 2, 'video-edit-tr-proxy-flow', null, 'video-mam-update', null, null),
('U28', 1, '["U26"]', 0, 3, 'video-edit-tr-proxy-flow', 'archive-flow', null, null, null);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'video-edit-tr-pub', 'archive-flow', 1),
('ingest', 'video-edit-tr-pub', 'video-edit-tr-proxy-flow', 1),
('ingest', 'video-edit-tr-priv1', 'archive-flow', 1),
('ingest', 'video-edit-tr-priv1', 'video-edit-tr-proxy-flow', 1),
('ingest', 'video-edit-tr-priv2', 'archive-flow', 1),
-- setting this to inactive until we have implemented DU-396 (include/exclude flow elements)
('ingest', 'video-edit-tr-priv2', 'video-edit-tr-proxy-flow', 0);

-- flowelement cleanup
-- deprecate unused QC gen jjobs
UPDATE `flowelement` SET `deprecated` = 1 WHERE `id` = 'U18';
UPDATE `flowelement` SET `deprecated` = 1 WHERE `id` = 'U19';
UPDATE `flowelement` SET `deprecated` = 1 WHERE `id` = 'U20';
-- digi-2020 pfr-meta-extract should be dependent on preservation-gen NOT on mxf file-delete 
UPDATE `flowelement` SET `dependencies` = '["U5"]' WHERE `id` = 'U9';
-- digi-2020 pfr-meta-extract need be put on hold...
UPDATE `flowelement` SET `task_config`=NULL WHERE `id`='U9';
-- digi-2020 proxy flow should be dependent on pfr-meta-extract NOT on mxf file-delete
-- digi-2020 proxy flow should NOT be dependent on pfr-meta-extract as output artifact path of pfr = input artifact path of proxy and so files missing... So make it a dependency of video-digi-2020-preservation-gen U5 itself 
UPDATE `flowelement` SET `dependencies` = '["U5"]' WHERE `id` = 'U10';
-- pathname_regex to be configured such that it has only path inside the Artifact folder...  
UPDATE `flowelement` SET `task_config`='{\"pathname_regex\": \"mxf/[^/]+\\\\.mxf\"}' WHERE `id`='U7';
UPDATE `flowelement` SET `task_config`='{\"pathname_regex\": \"Outputs?/[^/]+\\\\.mov\"}' WHERE `id`='U15';
UPDATE `flowelement` SET `task_config`='{\"destination_id\": \"bru-qc\", \"pathname_regex\": \"[^/]+\\\\.mkv\"}' WHERE `id`='U14';


-- artifactclass config relative to artifact folder change
UPDATE `artifactclass` SET `config`='{\"pathname_regex\": \"[^/]+|Outputs?/[^/]+\\\\.mov\"}' WHERE `id` in ('video-edit-pub','video-edit-priv1','video-edit-priv2','video-edit-tr-pub','video-edit-tr-priv1','video-edit-tr-priv2');


-- todo
-- make video-edit-proxy-flow also transcode top level mp4/mov files for swami nirvichara priv2 content -- needs discussion

SET foreign_key_checks = 1;

UPDATE `dwara`.`artifactclass` SET `description` = 'edited video digi proxy' WHERE (`id` = 'video-digi-2020-edit-priv1-proxy-low');
UPDATE `dwara`.`artifactclass` SET `description` = 'audio' WHERE (`id` = 'audio-priv1');
UPDATE `dwara`.`artifactclass` SET `description` = 'audio' WHERE (`id` = 'audio-priv2');
UPDATE `dwara`.`artifactclass` SET `description` = 'audio' WHERE (`id` = 'audio-priv3');
UPDATE `dwara`.`artifactclass` SET `description` = 'audio' WHERE (`id` = 'audio-pub');
UPDATE `dwara`.`artifactclass` SET `description` = 'edited video digi' WHERE (`id` = 'video-digi-2020-edit-priv1');
UPDATE `dwara`.`artifactclass` SET `description` = 'edited video digi' WHERE (`id` = 'video-digi-2020-edit-priv2');
UPDATE `dwara`.`artifactclass` SET `description` = 'edited video digi proxy' WHERE (`id` = 'video-digi-2020-edit-priv2-proxy-low');
UPDATE `dwara`.`artifactclass` SET `description` = 'edited video digi' WHERE (`id` = 'video-digi-2020-edit-pub');
UPDATE `dwara`.`artifactclass` SET `description` = 'edited video digi proxy' WHERE (`id` = 'video-digi-2020-edit-pub-proxy-low');
UPDATE `dwara`.`artifactclass` SET `description` = 'video digi' WHERE (`id` = 'video-digi-2020-priv1');
UPDATE `dwara`.`artifactclass` SET `description` = 'video digi proxy' WHERE (`id` = 'video-digi-2020-priv1-proxy-low');
UPDATE `dwara`.`artifactclass` SET `description` = 'video digi' WHERE (`id` = 'video-digi-2020-priv2');
UPDATE `dwara`.`artifactclass` SET `description` = 'video digi proxy' WHERE (`id` = 'video-digi-2020-priv2-proxy-low');
UPDATE `dwara`.`artifactclass` SET `description` = 'video digi' WHERE (`id` = 'video-digi-2020-pub');
UPDATE `dwara`.`artifactclass` SET `description` = 'video digi proxy' WHERE (`id` = 'video-digi-2020-pub-proxy-low');
UPDATE `dwara`.`artifactclass` SET `description` = 'edited video' WHERE (`id` = 'video-edit-priv1');
UPDATE `dwara`.`artifactclass` SET `description` = 'edited video proxy' WHERE (`id` = 'video-edit-priv1-proxy-low');
UPDATE `dwara`.`artifactclass` SET `description` = 'edited video' WHERE (`id` = 'video-edit-priv2');
UPDATE `dwara`.`artifactclass` SET `description` = 'edited video proxy' WHERE (`id` = 'video-edit-priv2-proxy-low');
UPDATE `dwara`.`artifactclass` SET `description` = 'edited video' WHERE (`id` = 'video-edit-pub');
UPDATE `dwara`.`artifactclass` SET `description` = 'edited video proxy' WHERE (`id` = 'video-edit-pub-proxy-low');
UPDATE `dwara`.`artifactclass` SET `description` = 'video' WHERE (`id` = 'video-priv1');
UPDATE `dwara`.`artifactclass` SET `description` = 'video proxy' WHERE (`id` = 'video-priv1-proxy-low');
UPDATE `dwara`.`artifactclass` SET `description` = 'video' WHERE (`id` = 'video-priv2');
UPDATE `dwara`.`artifactclass` SET `description` = 'video proxy' WHERE (`id` = 'video-priv2-proxy-low');
UPDATE `dwara`.`artifactclass` SET `description` = 'video' WHERE (`id` = 'video-priv3');
UPDATE `dwara`.`artifactclass` SET `description` = 'video' WHERE (`id` = 'video-pub');
UPDATE `dwara`.`artifactclass` SET `description` = 'video proxy' WHERE (`id` = 'video-pub-proxy-low');


UPDATE `dwara`.`artifactclass` SET `display_order` = '1' WHERE (`id` = 'audio-pub');
UPDATE `dwara`.`artifactclass` SET `display_order` = '2' WHERE (`id` = 'audio-priv3');
UPDATE `dwara`.`artifactclass` SET `display_order` = '3' WHERE (`id` = 'audio-priv2');
UPDATE `dwara`.`artifactclass` SET `display_order` = '4' WHERE (`id` = 'audio-priv1');
UPDATE `dwara`.`artifactclass` SET `display_order` = '11' WHERE (`id` = 'video-pub');
UPDATE `dwara`.`artifactclass` SET `display_order` = '12' WHERE (`id` = 'video-priv1');
UPDATE `dwara`.`artifactclass` SET `display_order` = '13' WHERE (`id` = 'video-priv2');
UPDATE `dwara`.`artifactclass` SET `display_order` = '14' WHERE (`id` = 'video-priv3');
UPDATE `dwara`.`artifactclass` SET `display_order` = '16' WHERE (`id` = 'video-priv1-proxy-low');
UPDATE `dwara`.`artifactclass` SET `display_order` = '17' WHERE (`id` = 'video-priv2-proxy-low');
UPDATE `dwara`.`artifactclass` SET `display_order` = '15' WHERE (`id` = 'video-pub-proxy-low');
UPDATE `dwara`.`artifactclass` SET `display_order` = '21' WHERE (`id` = 'video-digi-2020-pub');
UPDATE `dwara`.`artifactclass` SET `display_order` = '22' WHERE (`id` = 'video-digi-2020-priv1');
UPDATE `dwara`.`artifactclass` SET `display_order` = '23' WHERE (`id` = 'video-digi-2020-priv2');
UPDATE `dwara`.`artifactclass` SET `display_order` = '24' WHERE (`id` = 'video-digi-2020-pub-proxy-low');
UPDATE `dwara`.`artifactclass` SET `display_order` = '25' WHERE (`id` = 'video-digi-2020-priv1-proxy-low');
UPDATE `dwara`.`artifactclass` SET `display_order` = '26' WHERE (`id` = 'video-digi-2020-priv2-proxy-low');
UPDATE `dwara`.`artifactclass` SET `display_order` = '31' WHERE (`id` = 'video-edit-pub');
UPDATE `dwara`.`artifactclass` SET `display_order` = '32' WHERE (`id` = 'video-edit-priv1');
UPDATE `dwara`.`artifactclass` SET `display_order` = '33' WHERE (`id` = 'video-edit-priv2');
UPDATE `dwara`.`artifactclass` SET `display_order` = '34' WHERE (`id` = 'video-edit-pub-proxy-low');
UPDATE `dwara`.`artifactclass` SET `display_order` = '35' WHERE (`id` = 'video-edit-priv1-proxy-low');
UPDATE `dwara`.`artifactclass` SET `display_order` = '36' WHERE (`id` = 'video-edit-priv2-proxy-low');
UPDATE `dwara`.`artifactclass` SET `display_order` = '41' WHERE (`id` = 'video-digi-2020-edit-pub');
UPDATE `dwara`.`artifactclass` SET `display_order` = '42' WHERE (`id` = 'video-digi-2020-edit-priv1');
UPDATE `dwara`.`artifactclass` SET `display_order` = '43' WHERE (`id` = 'video-digi-2020-edit-priv2');
UPDATE `dwara`.`artifactclass` SET `display_order` = '44' WHERE (`id` = 'video-digi-2020-edit-pub-proxy-low');
UPDATE `dwara`.`artifactclass` SET `display_order` = '45' WHERE (`id` = 'video-digi-2020-edit-priv1-proxy-low');
UPDATE `dwara`.`artifactclass` SET `display_order` = '46' WHERE (`id` = 'video-digi-2020-edit-priv2-proxy-low');

-- SET foreign_key_checks = 0; 

-- minimum_free_space configuration for tape load prompts
-- 10 TB
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\":10995116277760}' WHERE `type`='group' and `id` not in ('G1', 'G2', 'G3', 'X1', 'X2', 'X3');

-- 1 TB
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 1099511627776}' WHERE `type`='group' and `id` in ('G1', 'G2', 'G3');

-- 
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 1099511627776,  \"remove_after_job\": true}' WHERE `type`='group' and `id` in ('X1', 'X2', 'X3');

-- Commenting out as better done using programming 
-- For some reason sequencenumber after 9350 got reset to 9265 again 
-- 9264 because from 9350 it got reset to 9265  
-- Set @seqNumberToBeIncremented = (select current_number from sequence WHERE `id`='video-edit-grp') - 9264;
-- source artifacts
-- UPDATE `artifact1` SET `name`=replace(name, sequence_code, CONCAT('Z' ,(CONVERT(SUBSTRING(sequence_code, 2),DECIMAL) + @seqNumberToBeIncremented))), `sequence_code`=replace(sequence_code, sequence_code, CONCAT('Z' ,(CONVERT(SUBSTRING(sequence_code, 2),DECIMAL) + @seqNumberToBeIncremented))) WHERE `id` in (17622,17623,17624,17625,17626,17627,17628,17629,17630,17631,17632,17633,17634,17635,17636,17637,17638,17639,17640,17641,17642,17643,17644,17645,17646,17647,17648,17649,17650,17651,17652,17653,17654,17655,17656,17657,17658,17659,17660,17661,17663,17664,17665,17666,17667,17668,17669,17670,17671,17672,17673,17674,17675,17676,17677,17678,17679,17680,18090,18091,18092,18093,18094,18095,19046,19047,19048,19049,19050,19051,19052,19053,19054,19055,19056,19057,19058,19059,19060,19061,19062,19063,19064,19065,19066,19067);

-- derived artifacts
-- UPDATE `artifact1` SET -- replace(sequence_code, sequence_code, CONCAT('ZL' ,(CONVERT(SUBSTRING((select sequence_code from artifact1 WHERE artifact_ref_id in (13217)), 3),DECIMAL) + @seqNumberToBeIncremented))) from artifact1 WHERE artifact_ref_id in (13217);

-- files also

-- t_file also

-- Dont forget to reset the number to last updated number
-- UPDATE `sequence` SET `current_number`='last seq no + total dupes somehting liek (9487 + 86)' WHERE `id`='video-edit-grp';

-- SET foreign_key_checks = 1;


