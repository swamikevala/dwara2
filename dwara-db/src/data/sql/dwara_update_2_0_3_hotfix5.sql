SET foreign_key_checks = 0;

INSERT INTO `sequence` (`id`, `code_regex`, `force_match`, `group`, `keep_code`, `number_regex`, `prefix`, `type`, `replace_code`) VALUES ('video-edit-pub-proxy-low', '^Z\\d+(?=_)', 1, 0, 0, '(?<=^Z)\\d+(?=_)', 'ZL', 'artifact', 1);

UPDATE `artifactclass` SET `config`='{\"pathname_regex\": \"^([^/]+/?){1,2}$|^[^/]+/Output[s]?/.+.mov$\"}' WHERE `id`='video-edit-pub';
INSERT INTO `artifactclass` (`id`, `concurrent_volume_copies`, `description`, `display_order`, `domain_id`, `import_only`, `path_prefix`, `source`, `artifactclass_ref_id`, `sequence_id`) VALUES ('video-edit-pub-proxy-low', 0, '', '16', '1', 0, '/data/dwara/transcoded', 0, 'video-edit-pub', 'video-edit-pub-proxy-low');

UPDATE `artifactclass_volume` SET `volume_id`='??' WHERE `artifactclass_id`='video-edit-pub';
INSERT INTO `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) VALUES (1, 0, 'video-edit-pub-proxy-low', 'G1');
INSERT INTO `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) VALUES (1, 0, 'video-edit-pub-proxy-low', 'G2');

SET foreign_key_checks = 1;
