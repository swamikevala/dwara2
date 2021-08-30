-- *** Already done - start ***

-- mbuffer turned off temporarily to investigate and fix longsized restores
UPDATE `destination` SET `use_buffering`=0 WHERE `id`='san-video';
UPDATE `destination` SET `use_buffering`=0 WHERE `id`='san-video1';

DELETE FROM `file1` WHERE `id` in (
select id from (select id from file1 where pathname like '/data/dwara/restored%' or pathname like '/mnt/san%' 
) as a);

-- *** Already done - end ***

-- some artifacts had filecount and size not set properly - Queries to fix and verify 
select * from artifact1 a where a.total_size=0 and a.deleted=0;

select * from artifact1 a where a.total_size=0 and a.artifactclass_id='video-pub' and a.deleted=0;

UPDATE
   artifact1 as a
SET
   a.total_size = (
       SELECT sum(f.size)
       FROM file1 as f
       WHERE a.id = f.artifact_id and f.directory = 0 and f.deleted = 0
       GROUP BY f.artifact_id
   ) where a.total_size=0 and a.artifactclass_id='video-pub' and a.deleted=0;

select * from artifact1 a where a.id in (
38981,
38982,
38983,
38984,
38985,
38986,
38987,
38988,
40445,
42495
);

select * from file1 as f join artifact1 a on f.artifact_id = a.id
where f.size = 0 and a.artifactclass_id='video-pub' and f.pathname = a.name;
   
update file1 as f join artifact1 a on f.artifact_id = a.id
set f.size = a.total_size
where f.size = 0 and a.artifactclass_id='video-pub' and f.pathname = a.name;

select * from file1 as f join artifact1 a on f.artifact_id = a.id where a.id in (
38981,
38982,
38983,
38984,
38985,
38986,
38987,
38988,
40445,
42495
) and f.pathname = a.name;


-- Adding config for IT Infra Backup
SET foreign_key_checks = 0;

INSERT INTO `sequence` (`id`, `type`, `prefix`, `code_regex`, `number_regex`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`, `force_match`, `keep_code`, `replace_code`) VALUES 
('dept-it-infra', 'artifact', 'BI', null, null, 0, 1, -1, 0, null, 0, 0, 0);

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `domain_id`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('dept-it-infra', 'IT Infra backup', 1, 'dept-it-infra', 1, 1, 1, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"(?!)\"}');
 
-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('dept-it-infra', 'E1', 0, 1),
('dept-it-infra', 'E2', 0, 1),
('dept-it-infra', 'E3', 0, 1);

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'dept-it-infra', 1),
('ingest', 'dept-it-infra', 2),
('ingest', 'dept-it-infra', 3),
('ingest', 'dept-it-infra', 6);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'dept-it-infra', 'archive-flow', 1);

SET foreign_key_checks = 1;

