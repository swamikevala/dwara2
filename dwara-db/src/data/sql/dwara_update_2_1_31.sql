SET foreign_key_checks = 0;

INSERT INTO `destination` (`id`, `path`, `use_buffering`) VALUES ('catdv-video-index', '172.18.1.24:/data/video-index', 0);

INSERT INTO `processingtask` (`id`, `description`, `filetype_id`, `max_errors`, `output_artifactclass_suffix`) VALUES 
('video-index-extract', 'extracts metadata', 'video', '1', '-proxy-low');

INSERT INTO `flow` (`id`, `description`) VALUES 
('video-flow', 'video flow'), ('video-index-extraction-flow', 'video index extraction flow');

-- UPDATE `action_artifactclass_flow` SET `flow_id`='video-flow' WHERE `action_id`='ingest' and`artifactclass_id`='video-pub' and`flow_id`='archive-flow';
INSERT INTO `action_artifactclass_flow` (`artifactclass_id`, `flow_id`, `active`, `action_id`) VALUES ('video-pub', 'video-index-extraction-flow', 1, 'ingest');


INSERT INTO `flowelement` (`id`, `active`, `deprecated`, `display_order`, `flow_id`, `processingtask_id`) VALUES 
('U112', 1, 0, '4', 'video-index-extraction-flow', 'video-index-extract');
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `processingtask_id`, `task_config`) VALUES 
('U114', 1, '[\"U112\"]', 0, '5', 'video-index-extraction-flow', 'file-copy', '{\"destination_id\": \"catdv-video-index\", \"pathname_regex\": \".*(\\\\.hdr|\\\\.idx|\\\\.ftr)\"}');

-- UPDATE `flowelement` SET `dependencies`='[\"U1\",\"U112\"]' WHERE `id`='U3';


-- UPDATE `dwara_dev`.`flowelement` SET `flow_id`='video-index-extraction-flow' WHERE `id`='U112';
-- UPDATE `dwara_dev`.`flowelement` SET `flow_id`='video-index-extraction-flow' WHERE `id`='U114';


-- UPDATE `flowelement` SET `dependencies`='[\"U1\",\"U112\"]' WHERE `id`='U2';


-- INSERT INTO `flowelement` (`id`, `active`, `deprecated`, `display_order`, `flow_id`, `processingtask_id`) VALUES 
-- ('U100', 1, 0, '100', 'video-flow', 'video-index-extract');
-- INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `processingtask_id`, `task_config`) VALUES 
-- ('U101', 1, '[\"U100\"]', 0, '101', 'video-flow', 'file-copy', '{\"destination_id\": \"catdv-video-index\", \"pathname_regex\": \"\\\\.(mxf|hdr|idx|ftr)\"}');
-- INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `flow_ref_id`) VALUES 
-- ('U102', 1, '[\"U100\"]', 0, '102', 'video-flow', 'archive-flow');

SET foreign_key_checks = 1;