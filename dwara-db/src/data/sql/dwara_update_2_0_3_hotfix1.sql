SET foreign_key_checks = 0; 

INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-priv1', 'archive-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-priv1', 'video-proxy-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-priv2', 'archive-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-priv2', 'video-proxy-flow', 0);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-priv3', 'archive-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-edit-pub', 'archive-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-edit-pub', 'video-proxy-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-edit-priv1', 'archive-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-edit-priv1', 'video-proxy-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-edit-priv2', 'archive-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-edit-priv2', 'video-proxy-flow', 0);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'audio-pub', 'archive-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'audio-priv1', 'archive-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'audio-priv2', 'archive-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'audio-priv3', 'archive-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'dept-backup', 'archive-flow', 1);

SET foreign_key_checks = 1; 