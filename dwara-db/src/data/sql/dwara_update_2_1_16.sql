UPDATE `action` SET `type`='complex' WHERE `id`='rewrite';

INSERT INTO `processingtask` (`id`, `filetype_id`, `max_errors`, `output_artifactclass_suffix`) VALUES ('video-digi-2020-mkv-mov-gen', 'mkv', '1', '');

UPDATE `artifact1` SET `file_count`='5' WHERE `id`='1985';

UPDATE `artifact1` SET `file_count`='3' WHERE `id`='28798';

-- update query for some artifacts ingested as edited but not matching the sequence pattern and hence missing out on extracted prevseqcode
UPDATE `artifact1` SET `prev_sequence_code`=replace(name, CONCAT(sequence_code, '_'),'') 
WHERE prev_sequence_code is null and artifactclass_id like 'video-digi-2020%' and artifactclass_id not like '%-proxy-low' and deleted=0;

-- for rewriting defective tapes
update artifact1_volume set status="current" where volume_id="R29816L7" and status is null;
update artifact1_volume set status="current" where volume_id="R39805L7" and status is null;

-- we triggered a rewrite request even before running the above query so had mark the request failed
update request set status="marked_failed" where id=71516;
update request set status="marked_failed" where id=71625;


-- priv3 seq config bugfix
UPDATE `sequence` SET `current_number`='1000', `ending_number`='1999', `starting_number`='1001' WHERE `id`='priv3-1';
UPDATE `sequence` SET `current_number`='2000', `ending_number`='2999', `starting_number`='2001' WHERE `id`='priv3-2';

-- Setting new hash for password admin(dwara) user - from Dong anna (Gmail login changes)
UPDATE `user` SET `hash`='vhmYtFGaTFmdph2U' WHERE `id`='1';

-- Volume schema changes 
UPDATE `volume` SET `healthstatus`='normal' WHERE id in SELECT id FROM (SELECT id FROM volume where defective=0 and suspect=0) as c;
UPDATE `volume` SET `healthstatus`='suspect' WHERE id in SELECT id FROM (SELECT id FROM volume where suspect=1) as c;
UPDATE `volume` SET `healthstatus`='defective' WHERE id in SELECT id FROM (SELECT id FROM volume where defective=1) as c;

-- delete columns defective and suspect from db
ALTER TABLE `volume` DROP COLUMN `suspect`;
ALTER TABLE `volume` DROP COLUMN `defective`;

-- some invalid requests not validated properly - validation fixed
UPDATE `request` SET `status`='failed' WHERE `id` in ('72358','72359','72360','72361','72363','72362','72364');

-- request table column name
-- ALTER TABLE `request` DROP FOREIGN KEY `FKe1oxajjb60tj4ehjm3d7kbiai`;
-- ALTER TABLE `request` CHANGE COLUMN `requested_by_id` `requested_by` INT(11) NULL DEFAULT NULL ;
-- ALTER TABLE `request` ADD CONSTRAINT `FKe1oxajjb60tj4ehjm3d7kbiai` FOREIGN KEY (`requested_by`) REFERENCES `user` (`id`);