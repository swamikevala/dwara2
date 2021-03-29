use dwara;
SET foreign_key_checks = 0; 

UPDATE `location` SET `description` = 'LTO Room' WHERE (`id` = 'lto-room');
DELETE FROM `location` WHERE (`id` != 'lto-room');

INSERT INTO `location` (`id`, `default`, `description`) VALUES ('sk-office1', b'0', 'SK Office - 1st');
INSERT INTO `location` (`id`, `default`, `description`) VALUES ('t-block2', b'0', 'T Block - 2nd');
INSERT INTO `location` (`id`, `default`, `description`) VALUES ('t-block3', b'0', 'T Block - 3rd');

UPDATE `copy` SET `location_id` = 'sk-office1' WHERE (`id` = 1);
UPDATE `copy` SET `location_id` = 't-block2' WHERE (`id` = 2);
UPDATE `copy` SET `location_id` = 't-block3' WHERE (`id` = 3);

SET foreign_key_checks = 1; 