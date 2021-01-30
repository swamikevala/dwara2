-- move task configuration to flowelement table
-- ALTER TABLE `flowelement` ADD `task_config` json;

-- add new flow for video-edit-pub proxy gen (since it has a different config)
INSERT INTO `flow` (`id`, `description`) VALUES
(`video-edit-proxy-flow`, `modified video-proxy-flow targeting specific subfolder`);

-- add flowelement entries for video-edit-proxy-flow
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `flow_ref_id`, `processingtask_id`, `task_config`) VALUES 
('U15', 1, null, 0, 1, 'video-edit-proxy-flow', null, 'video-proxy-low-gen', '{\"pathname_regex\": \".*/Outputs?/[^/]+\\\\.mov$\"}'),
('U16', 1, null, 0, 2, 'video-edit-proxy-flow', null, 'video-mam-update', null),
('U17', 1, null, 0, 3, 'video-edit-proxy-flow', 'archive-flow', null, null);

-- move all task configs to flowelement.task_config
UPDATE `flowelement` SET `task_config` = '{\"create_held_jobs\": true}' where `id` = 'U11';
UPDATE `flowelement` SET `task_config` = '{\"create_held_jobs\": true}' where `id` = 'U10';
UPDATE `flowelement` SET `task_config` = '{\"create_held_jobs\": true}' where `id` = 'U9';
UPDATE `flowelement` SET `task_config` = '{\"pathname_regex\": \".*\\\\.mxf$\"}' where `id` = 'U7';
UPDATE `flowelement` SET `task_config` = '{\"output_path\": \"/\"}' where `id` = 'U5';
UPDATE `flowelement` SET `task_config` = '{\"destination_id\": \"test-ffv1\", \"pathname_regex\": \".*.mkv$\"}' where `id` = 'U14';

-- update action_artifactclass_flow table 
UPDATE `action_artifactclass_flow` SET `flow_id` = 'video-edit-proxy-flow' where `artifactclass_id` like 'video-edit-%';

-- remove artifactclass_task table
DROP TABLE `artifactclass_task`;

-- define new filetype for QC video
INSERT INTO `filtype` (`id`, `description`) VALUES
('video-digi-2020-mkv-h264', 'Digitized miniDV files, Matroska compresssed h264 for QC');

-- add entry to extension_filetype
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES
(1, 'mkv', 'video-digi-2020-mkv-h264');

-- create new processing task for QC video generation
INSERT INTO `processingtask` (`id`, `description`, `filetype_id`, `max_errors`, `output_artifactclass_suffix`, `output_filetype_id`) VALUES
('video-digi-2020-qc-gen', 'generate medium resolution h264 video for QC', 'video-digi-2020-mxf-v210', 1, '', 'video-digi-2020-mkv-h264'); 

-- renumber display order for video-digi-2020-flow
UPDATE `flowelement` SET `display_order` = `display_order` + 2 WHERE `display_order` > 4 and `flow_id` = 'video-digi-2020-flow';
UPDATE `flowelement` SET `display_order` = 4, `dependencies` = '["U4", "U5", "U18"]' WHERE `id` = 'U6' and `flow_id` = 'video-digi-2020-flow';

-- create new flow elements for QC video generation, copying, and deletion
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `processingtask_id`) VALUES 
('U18', 1, null, 0, 3, 'video-digi-2020-flow', 'video-digi-2020-qc-gen'),
('U19', 1, '["U18"]', 0, 5, 'video-digi-2020-flow', 'file-copy');
('U20', 1, '["U19"]', 0, 6, 'video-digi-2020-flow', 'file-delete');



