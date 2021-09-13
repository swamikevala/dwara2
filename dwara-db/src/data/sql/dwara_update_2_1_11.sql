SET foreign_key_checks = 0; 

-- To test 4th copy
-- INSERT INTO `location` (`id`, `default`, `description`) VALUES ('somewhere', 0, 'Some 4th location');
-- INSERT INTO `copy` (`id`, `location_id`) VALUES ('4', 'somewhere');
-- INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('original-4', '40500', '49999', 0, 0, 0, 'R', '40001', 'volume');
-- INSERT INTO `volume` (`id`, `checksumtype`, `defective`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `suspect`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('R4', 'sha256', 0, '{\"blocksize\": 262144, \"minimum_free_space\": 10995116277760}', 0, 0, 'block', 'tape', 0, 'group', 'tar', '4', 'original-4');
-- INSERT INTO `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) VALUES (1, 0, 'video-pub', 'R4');

-- @Swami - need to add action in schema doc
INSERT INTO `action` (`id`, `type`) VALUES ('change_artifactclass', 'sync');

-- chaged from ^[A-Z]{1,2}\d+
UPDATE `sequence` SET `code_regex`='^[0-9A-Za-z-]+' WHERE `id`='video-digi-2020-priv2';
UPDATE `sequence` SET `code_regex`='^[0-9A-Za-z-]+' WHERE `id`='video-digi-2020-pub';


UPDATE `dwara`.`artifact1` SET `prev_sequence_code`='X2' WHERE `id`='25280';
UPDATE `dwara`.`artifact1` SET `prev_sequence_code`='X3' WHERE `id`='25270';

select * from artifact1 where name REGEXP '_X2$|_X3$' and artifactclass_id not like '%-proxy-low';

UPDATE `artifact1` SET `prev_sequence_code`=replace(name, CONCAT(sequence_code, '_')) WHERE name REGEXP '_X2$|_X3$|GR1$|_GR2$|_GR3$|_GR4$|_GR5$|_GR6$|_GR12$|_GR187$|_GR188$|_GR189$|_GR43$|_GR73$|_GR74$' and artifactclass_id not like '%-proxy-low';

SET foreign_key_checks = 1;
