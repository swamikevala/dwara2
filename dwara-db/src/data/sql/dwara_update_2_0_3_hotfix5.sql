SET foreign_key_checks = 0;

UPDATE `from_prd`.`artifactclass` SET `config`='{\"pathname_regex\": \"^([^/]+/?){1,2}$|^[^/]+/Output[s]?/.+.mov$\"}' WHERE `id`='video-pub';

SET foreign_key_checks = 1;
