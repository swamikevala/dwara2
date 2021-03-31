-- LIVE incident --
-- Following pfr extraction jobs missed out updating file table records for the idx and hdr file it generated
-- 210716
-- 211117
-- 211265
-- 211552
-- 211721
-- Found it during healthcheck

INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (1, 'hdr', 'video-proxy');
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (1, 'idx', 'video-proxy');