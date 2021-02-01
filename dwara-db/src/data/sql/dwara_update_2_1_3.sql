SET foreign_key_checks = 0;

-- move task configuration to flowelement table
ALTER TABLE `flowelement` ADD `task_config` json;

-- add new flow for video-edit-pub proxy gen (since it has a different config)
INSERT INTO `flow` (`id`, `description`) VALUES
('video-edit-proxy-flow', 'modified video-proxy-flow targeting specific subfolder'),
('video-digi-2020-proxy-flow', 'modified video-proxy-flow created on_hold');

-- add flowelement entries for video-edit-proxy-flow
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `flow_ref_id`, `processingtask_id`, `task_config`) VALUES 
('U15', 1, null, 0, 1, 'video-edit-proxy-flow', null, 'video-proxy-low-gen', '{\"pathname_regex\": \".*/Outputs?/[^/]+\\\\.mov$\"}'),
('U16', 1, null, 0, 2, 'video-edit-proxy-flow', null, 'video-mam-update', null),
('U17', 1, null, 0, 3, 'video-edit-proxy-flow', 'archive-flow', null, null);

-- point digi proxy flow_ref_id to new digi proxy flow
UPDATE `flowelement` SET `flow_ref_id` = 'video-digi-2020-proxy-flow' where `id` = 'U10';

-- move all task configs to flowelement.task_config
UPDATE `flowelement` SET `task_config` = '{\"create_held_jobs\": true}' where `id` = 'U11';
-- UPDATE `flowelement` SET `task_config` = '{\"create_held_jobs\": true}' where `id` = 'U1';
UPDATE `flowelement` SET `task_config` = '{\"create_held_jobs\": true}' where `id` = 'U9';
UPDATE `flowelement` SET `task_config` = '{\"pathname_regex\": \".*\\\\.mxf$\"}' where `id` = 'U7';
UPDATE `flowelement` SET `task_config` = '{\"output_path\": \"/\"}' where `id` = 'U5';
UPDATE `flowelement` SET `deprecated` = 1 where `id` = 'U14';

-- update action_artifactclass_flow table 
UPDATE `action_artifactclass_flow` SET `flow_id` = 'video-edit-proxy-flow' where `artifactclass_id` like 'video-edit-%' and `flow_id`='video-proxy-flow';

-- remove artifactclass_task table
DROP TABLE `artifactclass_task`;

-- define new filetype for QC video
INSERT INTO `filetype` (`id`, `description`) VALUES
('video-digi-2020-mkv-h264', 'Digitized miniDV files, Matroska compresssed h264 for QC');

-- add entry to extension_filetype
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES
(0, 'mkv', 'video-digi-2020-mkv-h264');

-- create new processing task for QC video generation
INSERT INTO `processingtask` (`id`, `description`, `filetype_id`, `max_errors`, `output_artifactclass_suffix`, `output_filetype_id`) VALUES
('video-digi-2020-qc-gen', 'generate medium resolution h264 video for QC', 'video-digi-2020-mxf-v210', 1, '', 'video-digi-2020-mkv-h264'); 

-- renumber display order for video-digi-2020-flow
UPDATE `flowelement` SET `display_order` = `display_order` + 2 WHERE `display_order` > 4 and `flow_id` = 'video-digi-2020-flow';
UPDATE `flowelement` SET `display_order` = 4, `dependencies` = '["U4", "U5", "U18"]' WHERE `id` = 'U6' and `flow_id` = 'video-digi-2020-flow';

-- create new flow elements for QC video generation, copying, and deletion
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `processingtask_id`, `task_config`) VALUES 
('U18', 1, null, 0, 3, 'video-digi-2020-flow', 'video-digi-2020-qc-gen', '{\"output_path\": \"/qc\"}'),
('U19', 1, '["U18"]', 0, 5, 'video-digi-2020-flow', 'file-copy', '{\"destination_id\": \"test-qc\", \"pathname_regex\": \".*/qc/.*\\\\.mkv$\"}'),
('U20', 1, '["U6"]', 0, 6, 'video-digi-2020-flow', 'file-delete', '{\"pathname_regex\": \".*/qc/.*\\\\.mkv$\"}');

-- create new flow elements for digi proxy generation (since it requires a non-default configuration)
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `flow_ref_id`, `processingtask_id`, `task_config`) VALUES 
('U21', 1, null, 0, 1, 'video-digi-2020-proxy-flow', null, 'video-proxy-low-gen', '{\"create_held_jobs\": true}'),
('U22', 1, '["U21"]', 0, 2, 'video-digi-2020-proxy-flow', null, 'video-mam-update', null),
('U23', 1, '["U21"]', 0, 3, 'video-digi-2020-proxy-flow', 'archive-flow', null, null);

-- update old flow element ids for existing digi records (job table)
UPDATE `job` j JOIN `artifact1` a ON j.`input_artifact_id` = a.`id` SET j.`flowelement_id` = 'U21' where j.`flowelement_id` = 'U1' and a.`artifactclass_id` like 'video-digi-2020-%';
UPDATE `job` j JOIN `artifact1` a ON j.`input_artifact_id` = a.`id` SET j.`flowelement_id` = 'U22' where j.`flowelement_id` = 'U2' and a.`artifactclass_id` like 'video-digi-2020-%';

INSERT INTO `destination` (`id`, `path`, `use_buffering`) VALUES ('test-qc', '172.18.1.200:/data/qc', 0);

SET foreign_key_checks = 1;