UPDATE `libraryclass` SET `path_prefix`='/data/ingested' WHERE `path_prefix`='C:\\data\\ingested';
UPDATE `libraryclass` SET `path_prefix`='/data/ingested' WHERE `path_prefix`='C:\\data\\user';
UPDATE `libraryclass` SET `path_prefix`='/data/transcoded' WHERE `path_prefix`='C:\\data\\transcoded';

UPDATE `tapedrive` SET `job_id`=NULL WHERE `id`>'13000';