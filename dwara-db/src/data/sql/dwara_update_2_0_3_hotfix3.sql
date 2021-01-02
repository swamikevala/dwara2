SET foreign_key_checks = 0; 

--UPDATE `version` SET `version`='2.1.2' WHERE `version`='2.0.3';

INSERT INTO `destination` (`id`, `path`, `use_buffering`) VALUES ('bru', '/data/bru', 0);

INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (17, 'video-digi-2020-pub', NULL, 'file-copy', '{\"pathname_regex\": \".*.mkv$\", \"destination_id\" : \"bru\"}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (18, 'video-digi-2020-priv1', NULL, 'file-copy', '{\"pathname_regex\": \".*.mkv$\", \"destination_id\" : \"bru\"}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (19, 'video-digi-2020-priv2', NULL, 'file-copy', '{\"pathname_regex\": \".*.mkv$\", \"destination_id\" : \"bru\"}');

INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `processingtask_id`) VALUES ('U14', 1, '[\"U5\"]', 0, '14', 'video-digi-2020-flow', 'file-copy');

-- TODO : fix artifact file count, file size for digi*
	

SET foreign_key_checks = 1; 
