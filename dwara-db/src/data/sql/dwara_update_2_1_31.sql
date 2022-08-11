SET foreign_key_checks = 0;

INSERT INTO `destination` (`id`, `path`, `use_buffering`) VALUES ('catdv-video-index', '172.18.1.24:/data/video-index', 0);

INSERT INTO `processingtask` (`id`, `description`, `filetype_id`, `max_errors`) VALUES 
('video-index-extract', 'extracts metadata', 'video', '1');

INSERT INTO `flow` (`id`, `description`) VALUES 
('video-flow', 'video flow');

UPDATE `action_artifactclass_flow` SET `flow_id`='video-flow' WHERE `action_id`='ingest' and`artifactclass_id`='video-pub' and`flow_id`='archive-flow';

INSERT INTO `flowelement` (`id`, `active`, `deprecated`, `display_order`, `flow_id`, `processingtask_id`) VALUES 
('U100', 1, 0, '100', 'video-flow', 'video-index-extract');
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `processingtask_id`, `task_config`) VALUES 
('U101', 1, '[\"U100\"]', 0, '101', 'video-flow', 'file-copy', '{\"destination_id\": \"catdv-video-index\", \"pathname_regex\": \"\\\\.(mxf|hdr|idx|ftr)\"}');
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `flow_ref_id`) VALUES 
('U102', 1, '[\"U100\"]', 0, '102', 'video_flow', 'archive-flow');


SET foreign_key_checks = 1;