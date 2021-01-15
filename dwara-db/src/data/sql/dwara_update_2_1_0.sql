SET foreign_key_checks = 0;

UPDATE `version` SET `version`='2.1.0' WHERE `version`='2.0.3';

-- Finalization requests missing type for the system generated thus missing out on the scheduled status updation
UPDATE `request` SET `type`='system' WHERE `action_id`='finalize' and `type` is null;

-- TODO update completed at for finalization request

-- File size reconcilation script goes here
UPDATE
   artifact1 as a
SET
   a.total_size = (
       SELECT sum(f.size)
       FROM file1 as f
       WHERE a.id = f.artifact_id and f.directory = 0
       GROUP BY f.artifact_id
   );
   
update file1 as f join artifact1 a on f.artifact_id = a.id
set f.size = a.total_size
where f.pathname = a.name;

ALTER TABLE `file1` ADD COLUMN `pathname_checksum` VARBINARY(20) NULL DEFAULT NULL AFTER `pathname`,
ADD UNIQUE INDEX `pathname_checksum_UNIQUE` (`pathname_checksum` ASC);
ALTER TABLE `file2` ADD COLUMN `pathname_checksum` VARBINARY(20) NULL DEFAULT NULL AFTER `pathname`,
ADD UNIQUE INDEX `pathname_checksum_UNIQUE` (`pathname_checksum` ASC);
CREATE TABLE `t_file` (
  `id` int(11) NOT NULL,
  `checksum` varbinary(32) DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `directory` bit(1) DEFAULT NULL,
  `pathname` varchar(4096) DEFAULT NULL,
  `size` bigint(20) DEFAULT NULL,
  `artifact_id` int(11) DEFAULT NULL,
  `file_ref_id` int(11) DEFAULT NULL,
  `pathname_checksum` varbinary(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pathname_checksum_UNIQUE` (`pathname_checksum`)
);

ALTER TABLE `file1` CHANGE COLUMN `checksum` `checksum` VARBINARY(32) NULL DEFAULT NULL ;
ALTER TABLE `file2` CHANGE COLUMN `checksum` `checksum` VARBINARY(32) NULL DEFAULT NULL ;


-- file*.pathname_checksum for the existing records script goes here
update file1 set pathname_checksum = unhex(sha1(pathname));

-- sequence
delete from `sequence` where `id` like 'dept-backup%';
delete from `sequence` where `id` = 'video-edit-global';
insert into `sequence` (`id`, `code_regex`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `number_regex`, `prefix`, `starting_number`, `type`, `sequence_ref_id`, `replace_code`) values ('edited-1', null, 10000, 19999, 0, 0, 0, null, 'E', 10001, 'volume', null, null);
insert into `sequence` (`id`, `code_regex`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `number_regex`, `prefix`, `starting_number`, `type`, `sequence_ref_id`, `replace_code`) values ('edited-2', null, 20000, 29999, 0, 0, 0, null, 'E', 20001, 'volume', null, null);
insert into `sequence` (`id`, `code_regex`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `number_regex`, `prefix`, `starting_number`, `type`, `sequence_ref_id`, `replace_code`) values ('edited-3', null, 30000, 39999, 0, 0, 0, null, 'E', 30001, 'volume', null, null);
insert into `sequence` (`id`, `code_regex`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `number_regex`, `prefix`, `starting_number`, `type`, `sequence_ref_id`, `replace_code`) values ('video-edit-pub-proxy-low', '^Z\\d+(?=_)', null, null, 1, 0, 0, '(?<=^Z)\\d+(?=_)', 'ZL', null, 'artifact', null, 1);
insert into `sequence` (`id`, `code_regex`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `number_regex`, `prefix`, `starting_number`, `type`, `sequence_ref_id`, `replace_code`) values ('video-edit-priv2-proxy-low', '^ZX\\d+(?=_)', null, null, 1, 0, 0, '(?<=^ZX)\\d+(?=_)', 'ZXL', null, 'artifact', null, 1);


-- artifactclass
delete from `artifactclass` where `id` = 'dept-backup';
delete from `artifactclass` where `id` = 'video-edit-global';
insert into `artifactclass` (`id`, `concurrent_volume_copies`, `description`, `display_order`, `domain_id`, `import_only`, `path_prefix`, `source`, `artifactclass_ref_id`, `sequence_id`) values ('video-edit-pub-proxy-low', 1, '' , 0, 1, 0, '/data/dwara/transcoded', 0, 'video-edit-pub', 'video-edit-pub-proxy-low');
insert into `artifactclass` (`id`, `concurrent_volume_copies`, `description`, `display_order`, `domain_id`, `import_only`, `path_prefix`, `source`, `artifactclass_ref_id`, `sequence_id`) values ('video-edit-priv1-proxy-low', 1, '' , 0, 1, 0, '/data/dwara/transcoded', 0, 'video-edit-priv1', 'video-edit-priv1-proxy-low');
insert into `artifactclass` (`id`, `concurrent_volume_copies`, `description`, `display_order`, `domain_id`, `import_only`, `path_prefix`, `source`, `artifactclass_ref_id`, `sequence_id`) values ('video-edit-priv2-proxy-low', 1, '' , 0, 1, 0, '/data/dwara/transcoded', 0, 'video-edit-priv2', 'video-edit-priv2-proxy-low');
update `artifactclass` set `concurrent_volume_copies` = 1, `display_order` = 4, `config` = '{\"pathname_regex\": \"^([^/]+/?){1,2}$|^[^/]+/Outputs?/[^/]+\\\\.mov$\"}' where `id` = 'video-edit-pub';
update `artifactclass` set `concurrent_volume_copies` = 1, `display_order` = 5, `config` = '{\"pathname_regex\": \"^([^/]+/?){1,2}$|^[^/]+/Outputs?/[^/]+\\\\.mov$\"}' where `id` = 'video-edit-priv1';
update `artifactclass` set `concurrent_volume_copies` = 1, `display_order` = 6, `config` = '{\"pathname_regex\": \"^([^/]+/?){1,2}$|^[^/]+/Outputs?/[^/]+\\\\.mov$\"}' where `id` = 'video-edit-priv2';
update `artifactclass` set `display_order` = 7 where `id` = 'video-digi-2020-pub';
update `artifactclass` set `display_order` = 8 where `id` = 'video-digi-2020-priv1';
update `artifactclass` set `display_order` = 9 where `id` = 'video-digi-2020-priv2';
update `artifactclass` set `display_order` = 0 where `id` like '%-proxy-low';
update `artifactclass` set `concurrent_volume_copies` = 1, `display_order` = 100 where `id` = 'video-priv3';
update `artifactclass` set `concurrent_volume_copies` = 1, `display_order` = 101 where `id` = 'audio-priv3';

-- artifactclass_task
delete from `artifactclass_task` where (`id` = 1);
insert into `artifactclass_task` (`id`, `config`, `processingtask_id`, `artifactclass_id`) VALUES (20, '{\"pathname_regex\": \".*/Outputs?/[^/]+\\\\.mov$\"}', 'video-proxy-low-gen', 'video-edit-pub');
insert into `artifactclass_task` (`id`, `config`, `processingtask_id`, `artifactclass_id`) VALUES (21, '{\"pathname_regex\": \".*/Outputs?/[^/]+\\\\.mov$\"}', 'video-proxy-low-gen', 'video-edit-priv1');
insert into `artifactclass_task` (`id`, `config`, `processingtask_id`, `artifactclass_id`) VALUES (22, '{\"pathname_regex\": \".*/Outputs?/[^/]+\\\\.mov$\"}', 'video-proxy-low-gen', 'video-edit-priv2');

-- action_artifactclass_user

delete from `action_artifactclass_user` where `artifactclass_id` = 'dept-backup';
delete from `action_artifactclass_user` where `artifactclass_id` = 'video-edit-global';

-- artifactclass_volume
delete from `artifactclass_volume` where `artifactclass_id` = 'dept-backup';
update `artifactclass_volume` set `volume_id` = 'E1' where `artifactclass_id` = 'video-edit-pub' and `volume_id` = 'G1';
update `artifactclass_volume` set `volume_id` = 'E2' where `artifactclass_id` = 'video-edit-pub' and `volume_id` = 'G2';
update `artifactclass_volume` set `volume_id` = 'E3' where `artifactclass_id` = 'video-edit-pub' and `volume_id` = 'G3';
update `artifactclass_volume` set `volume_id` = 'E1' where `artifactclass_id` = 'video-edit-priv1' and `volume_id` = 'G1';
update `artifactclass_volume` set `volume_id` = 'E2' where `artifactclass_id` = 'video-edit-priv1' and `volume_id` = 'G2';
update `artifactclass_volume` set `volume_id` = 'E3' where `artifactclass_id` = 'video-edit-priv1' and `volume_id` = 'G3';
insert into `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) values (1, 0, 'video-edit-pub-proxy-low', 'G1');
insert into `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) values (1, 0, 'video-edit-pub-proxy-low', 'G2');
insert into `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) values (1, 0, 'video-edit-priv1-proxy-low', 'G1');
insert into `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) values (1, 0, 'video-edit-priv1-proxy-low', 'G2');
insert into `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) values (1, 0, 'video-edit-priv2-proxy-low', 'X1');
insert into `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) values (1, 0, 'video-edit-priv2-proxy-low', 'X2');



-- action_artifactclass_flow
-- nothing to add/modify as already needed records are added...

-- volume
delete from `volume` where `id` in ('B1', 'B2', 'B3');
insert into `volume` (`id`, `checksumtype`, `defective`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `suspect`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) values ('E1', 'sha256', 0, '{\"blocksize\": 262144}', 0, 0, 'block', 'tape', 0, 'group', 'tar', 1, 'edited-1');
insert into `volume` (`id`, `checksumtype`, `defective`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `suspect`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) values ('E2', 'sha256', 0, '{\"blocksize\": 262144}', 0, 0, 'block', 'tape', 0, 'group', 'bru', 2, 'edited-2');
insert into `volume` (`id`, `checksumtype`, `defective`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `suspect`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) values ('E3', 'sha256', 0, '{\"blocksize\": 262144}', 0, 0, 'block', 'tape', 0, 'group', 'tar', 3, 'edited-3');


SET foreign_key_checks = 1;
