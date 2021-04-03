SET foreign_key_checks = 0; 

-- Photopub support

-- SEQUENCE --
INSERT INTO `sequence` (`id`, `type`, `prefix`, `code_regex`, `number_regex`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`, `force_match`, `keep_code`, `replace_code`) VALUES 
('photo-grp', 'artifact', null, null, null, 1, 1, -1, 0, null, 0, 0, 0),
('photo-pub', 'artifact', 'P', null, null, 0, null, null, null,'photo-grp', 0, 0, 0),
('photo-priv2', 'artifact', 'PX', null, null, 0, null, null, null, 'photo-grp', 0, 0, 0),
('photo-edit-grp', 'artifact', null, null, null, 1, 1, -1, 0, null, 0, 0, 0),
('photo-edit-pub', 'artifact', 'PZ', null, null, 0, null, null, null, 'photo-edit-grp', 0, 0, 0),
('photo-edit-priv2', 'artifact', 'PZX', null, null, 0, null, null, null, 'photo-edit-grp', 0, 0, 0),
('photo-pub-proxy', 'artifact', 'PL', '^P\\d+(?=_)', '(?<=^P)\\d+(?=_)', 0, null, null, null, null, 1, 0, 1),
('photo-priv2-proxy', 'artifact', 'PXL', '^PX\\d+(?=_)', '(?<=^PX)\\d+(?=_)', 0, null, null, null, null, 1, 0, 1),
('photo-edit-pub-proxy', 'artifact', 'PZL', '^PZ\\d+(?=_)', '(?<=^PZ)\\d+(?=_)', 0, null, null, null, null, 1, 0, 1),
('photo-edit-priv2-proxy', 'artifact', 'PZXL', '^PZX\\d+(?=_)', '(?<=^PZX)\\d+(?=_)', 0, null, null, null, null, 1, 0, 1);


-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `domain_id`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('photo-pub', '', 1, 'photo-pub', 1, 1, 20, '/data/dwara/staged', null, 0, null),
('photo-priv2', '', 1, 'photo-priv2', 1, 1, 21, '/data/dwara/staged', null, 0, null),
('photo-edit-pub', '', 1, 'photo-edit-pub', 1, 1, 22, '/data/dwara/staged', null, 0, null),
('photo-edit-priv2', '', 1, 'photo-edit-priv2', 1, 1, 23, '/data/dwara/staged', null, 0, null),
('photo-pub-proxy', '', 1, 'photo-pub-proxy', 0, 1, 0, '/data/dwara/transcoded', 'photo-pub', 0, '{"pathname_regex": "(?!)"}'),
('photo-priv2-proxy', '', 1, 'photo-priv2-proxy', 0, 1, 0, '/data/dwara/transcoded', 'photo-priv2', 0, '{"pathname_regex": "(?!)"}'),
('photo-edit-pub-proxy', '', 1, 'photo-edit-pub-proxy', 0, 1, 0, '/data/dwara/transcoded', 'photo-edit-pub', 0, '{"pathname_regex": "(?!)"}'),
('photo-edit-priv2-proxy', '', 1, 'photo-edit-priv2-proxy', 0, 1, 0, '/data/dwara/transcoded', 'photo-edit-priv2', 0, '{"pathname_regex": "(?!)"}');

-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('photo-pub', 'R1', 0, 1),
('photo-pub', 'R2', 0, 1),
('photo-pub', 'R3', 0, 1),
('photo-priv2', 'X1', 0, 1),
('photo-priv2', 'X2', 0, 1),
('photo-priv2', 'X3', 0, 1),
('photo-edit-pub', 'E1', 0, 1),
('photo-edit-pub', 'E2', 0, 1),
('photo-edit-pub', 'E3', 0, 1),
('photo-edit-priv2', 'X1', 0, 1),
('photo-edit-priv2', 'X2', 0, 1),
('photo-edit-priv2', 'X3', 0, 1),
('photo-pub-proxy', 'G1', 0, 1),
('photo-pub-proxy', 'G2', 0, 1),
('photo-priv2-proxy', 'X1', 0, 1),
('photo-priv2-proxy', 'X2', 0, 1),
('photo-edit-pub-proxy', 'G1', 0, 1),
('photo-edit-pub-proxy', 'G2', 0, 1),
('photo-edit-priv2-proxy', 'X1', 0, 1),
('photo-edit-priv2-proxy', 'X2', 0, 1);


-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'photo-pub', 1),
('ingest', 'photo-priv2', 1),
('ingest', 'photo-edit-pub', 1),
('ingest', 'photo-edit-priv2', 1);


-- FLOW --
INSERT INTO `flow` ( `id`, `description`) VALUES
('photo-proxy-flow', 'photo proxy and thumbnail generation');


-- EXTENSION --
INSERT INTO `extension` (`id`, `description`, `ignore`) VALUES
('psd', 'photoshop image', null), 
('tif', 'tagged image file format', null), 
('nrw', 'Nikon Raw image format - similar to NEF but supports more features', null), 
('crw', 'Canon Raw image format - superceded by CR2 format', null), 
('cr3', 'Canon Raw image format - introduced in 2018', null), 
('fff', 'Hasselblad Raw image format', null), 
('sr2', 'One of the Sony Raw image file formats', null), 
('srf', 'One of the Sony Raw image file formats', null);


-- FILETYPE --
INSERT INTO `filetype` (`id`, `description`) VALUES
('photo-proxy', 'Photo proxy files');


-- EXTENSION_FILETYPE --
INSERT INTO `extension_filetype` (`filetype_id`, `extension_id`, `sidecar`, `suffix`) VALUES
('photo-proxy', 'jpg', 0, '_p'),
('photo-proxy', 'thm', 1, null),
('photo-proxy', 'xmp', 1, null),
('image', 'nef', 0, null),
('image', 'psd', 0, null),
('image', 'tif', 0, null),
('image', 'nrw', 0, null),
('image', 'crw', 0, null),
('image', 'cr3', 0, null),
('image', 'fff', 0, null),
('image', 'sr2', 0, null),
('image', 'srf', 0, null);


-- DESTINATION --
INSERT INTO `destination` (`id`, `path`, `use_buffering`) VALUES
('catdv-photo-proxy', '172.18.1.24:/data/photo-proxy', 0);

-- PROCESSINGTASK --
INSERT INTO `processingtask` (`id`, `description`, `filetype_id`, `max_errors`, `output_artifactclass_suffix`, `output_filetype_id`) VALUES
('photo-proxy-gen', 'generate low resolution photo proxy and thumbnail', 'image', 10, '-proxy', 'photo-proxy');


-- FLOW_ELEMENT --
INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `flow_ref_id`, `processingtask_id`, `storagetask_action_id`, `task_config`) VALUES
('U24', 1, null, 0, 1, 'photo-proxy-flow', null, 'photo-proxy-gen', null, null),
-- copy as a storage task change ('U25', 1, '["U24"]', 0, 2, 'photo-proxy-flow', null, 'file-copy', null, '{"destination_id": "catdv-photo-proxy"}');
('U25', 1, '["U24"]', 0, 2, 'photo-proxy-flow', null, null, 'copy', '{"destination_id": "catdv-photo-proxy"}');

-- UPDATE `flowelement` SET `processingtask_id`=NULL, `storagetask_action_id`='copy' WHERE `id`='U25';

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'photo-pub', 'archive-flow', 1),
('ingest', 'photo-pub', 'photo-proxy-flow', 1),
('ingest', 'photo-priv2', 'archive-flow', 1),
('ingest', 'photo-priv2', 'photo-proxy-flow', 0),
('ingest', 'photo-edit-pub', 'archive-flow', 1),
('ingest', 'photo-edit-pub', 'photo-proxy-flow', 1),
('ingest', 'photo-edit-priv2', 'archive-flow', 1),
('ingest', 'photo-edit-priv2', 'photo-proxy-flow', 0);


-- copy as storage task changes

-- not needed INSERT INTO `volume` (`id`, `capacity`, `checksumtype`, `defective`, `details`, `finalized`, `imported`, `initialized_at`, `storagelevel`, `storagesubtype`, `storagetype`, `suspect`, `type`, `uuid`, `archiveformat_id`, `group_ref_id`, `location_id`) VALUES ('bru', '6000000000000', 'sha256', 0, NULL, 0, 0, NULL, 'file', NULL, 'disk', 0, 'physical', 'bru', NULL, NULL, NULL);

-- UPDATE `flowelement` SET `processingtask_id`=NULL, `storagetask_action_id`='write' WHERE `id`='U14';

INSERT INTO `action` (`id`, `type`) VALUES ('copy', 'storage_task');
INSERT INTO `action` (`id`, `type`) VALUES ('marked_completed', 'sync');

SET foreign_key_checks = 1;
