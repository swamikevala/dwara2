SET foreign_key_checks = 0; 

-- SEQUENCE --
INSERT INTO sequence (id, `type`, prefix, code_regex, number_regex, `group`, starting_number, ending_number, current_number, sequence_ref_id, force_match, keep_code, replace_code) VALUES 
('video-digi-2020-edit-grp','artifact',null,null,null,1,1,-1,0,null,0,0,null),
('video-digi-2020-edit-pub','artifact','ZD','^ZP?\\d+',null,0,null,null,null,'video-digi-2020-edit-grp',1,0,0),
('video-digi-2020-edit-priv2','artifact','ZDX','^Z[PY]?\\d+',null,0,null,null,null,'video-digi-2020-edit-grp',1,0,0),
('video-digi-2020-edit-pub-proxy-low','artifact','ZDL','^ZD\\d+(?=_)','(?<=^ZD)\\d+(?=_)',0,null,null,null,null,1,0,1),
('video-digi-2020-edit-priv2-proxy-low','artifact','ZDXL','^ZDX\\d+(?=_)','(?<=^ZDX)\\d+(?=_)',0,null,null,null,null,1,0,1);

-- ARTIFACTCLASS --
INSERT INTO artifactclass (id, `description`, domain_id, sequence_id, source, concurrent_volume_copies, display_order, path_prefix, artifactclass_ref_id, import_only, config) VALUES 
('video-digi-2020-edit-pub','',1,'video-digi-2020-edit-pub',1,1,10,'/data/dwara/staged',null,0,null),
('video-digi-2020-edit-priv1','',1,'video-digi-2020-edit-pub',1,1,11,'/data/dwara/staged',null,0,null),
('video-digi-2020-edit-priv2','',1,'video-digi-2020-edit-priv2',1,1,12,'/data/dwara/staged',null,0,null),
('video-digi-2020-edit-pub-proxy-low','',1,'video-digi-2020-edit-pub-proxy-low',0,1,0,'/data/dwara/transcoded','video-digi-2020-edit-pub',0,null),
('video-digi-2020-edit-priv1-proxy-low','',1,'video-digi-2020-edit-pub-proxy-low',0,1,0,'/data/dwara/transcoded','video-digi-2020-edit-priv1',0,null),
('video-digi-2020-edit-priv2-proxy-low','',1,'video-digi-2020-edit-priv2-proxy-low',0,1,0,'/data/dwara/transcoded','video-digi-2020-edit-priv2',0,null);


-- ARTIFACTCLASS_VOLUME --
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES 
('video-digi-2020-edit-pub', 'E1', 0, 1),
('video-digi-2020-edit-priv1', 'E1', 0, 1),
('video-digi-2020-edit-priv2', 'X1', 0, 1),
('video-digi-2020-edit-pub', 'E2', 0, 1),
('video-digi-2020-edit-priv1', 'E2', 0, 1),
('video-digi-2020-edit-priv2', 'X2', 0, 1),
('video-digi-2020-edit-pub', 'E3', 0, 1),
('video-digi-2020-edit-priv1', 'E3', 0, 1),
('video-digi-2020-edit-priv2', 'X3', 0, 1),
('video-digi-2020-edit-pub-proxy-low', 'G1', 0, 1),
('video-digi-2020-edit-priv1-proxy-low', 'G1', 0, 1),
('video-digi-2020-edit-priv2-proxy-low', 'X1', 0, 1),
('video-digi-2020-edit-pub-proxy-low', 'G2', 0, 1),
('video-digi-2020-edit-priv1-proxy-low', 'G2', 0, 1),
('video-digi-2020-edit-priv2-proxy-low', 'X2', 0, 1);

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO action_artifactclass_user (action_id, artifactclass_id, user_id) VALUES 
('ingest', 'video-digi-2020-edit-pub', 8),
('ingest', 'video-digi-2020-edit-priv1', 8),
('ingest', 'video-digi-2020-edit-priv2', 8);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES 
('ingest', 'video-digi-2020-edit-pub', 'video-digi-2020-flow', 1),
('ingest', 'video-digi-2020-edit-priv1', 'video-digi-2020-flow', 1),
('ingest', 'video-digi-2020-edit-priv2', 'video-digi-2020-flow', 1);

SET foreign_key_checks = 1; 