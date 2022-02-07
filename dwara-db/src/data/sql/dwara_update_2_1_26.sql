SET foreign_key_checks = 0;

INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (0, 'mp4', 'audio');

UPDATE `action_artifactclass_flow` SET `active`=1 WHERE `action_id`='ingest' and`artifactclass_id`='audio-priv2' and`flow_id`='audio-proxy-flow';

SET foreign_key_checks = 1;

