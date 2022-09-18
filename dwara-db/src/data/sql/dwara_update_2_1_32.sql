INSERT INTO `dwara3_test`.`action` (`id`, `type`) VALUES ('generate_mezzanine_proxies', 'complex');


INSERT INTO `dwara3_test`.`processingtask` (`id`, `description`, `filetype_id`, `max_errors`, `output_artifactclass_suffix`, `output_filetype_id`) VALUES ('video-proxy-mezz-gen', 'generate HD mezzanine video proxies', 'video', '10', '-proxy-mezz', 'prores-proxy-mov');
INSERT INTO `dwara3_test`.`processingtask` (`id`, `description`, `filetype_id`, `max_errors`, `output_artifactclass_suffix`, `output_filetype_id`) VALUES ('restructure-mezz-folder', 'restructure the HD mezzanine video proxy folder', 'video', '10', null, null);

INSERT INTO `dwara3_test`.`extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (0, 'mov', 'prores-proxy-mov');

INSERT INTO `dwara3_test`.`sequence` (`id`, `group`, `prefix`, `type`) VALUES ('video-pub-proxy-mezz', 0, 'VM', 'artifact');
INSERT INTO `dwara3_test`.`sequence` (`id`, `group`, `prefix`, `type`) VALUES ('video-priv2-proxy-mezz', 0, 'VXM', 'artifact');


INSERT INTO `dwara3_test`.`artifactclass` (`id`, `concurrent_volume_copies`, `description`, `display_order`, `import_only`, `path_prefix`, `source`, `artifactclass_ref_id`, `sequence_id`) VALUES ('video-pub-proxy-mezz', 1, 'video edit proxy', 2, 0, '/data/dwara/transcoded/mezzanine', 0, 'video-pub', 'video-pub-proxy-mezz');
INSERT INTO `dwara3_test`.`artifactclass` (`id`, `concurrent_volume_copies`, `description`, `display_order`, `import_only`, `path_prefix`, `source`, `artifactclass_ref_id`, `sequence_id`) VALUES ('video-priv1-proxy-mezz', 1, 'video edit proxy', 2, 0, '/data/dwara/transcoded/mezzanine', 0, 'video-priv1', 'video-pub-proxy-mezz');
INSERT INTO `dwara3_test`.`artifactclass` (`id`, `concurrent_volume_copies`, `description`, `display_order`, `import_only`, `path_prefix`, `source`, `artifactclass_ref_id`, `sequence_id`) VALUES ('video-priv2-proxy-mezz', 1, 'video edit proxy', 2, 0, '/data/dwara/transcoded/mezzanine', 0, 'video-priv2', 'video-priv2-proxy-mezz');