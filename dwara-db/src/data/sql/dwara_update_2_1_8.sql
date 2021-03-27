UPDATE `copy` SET `location_id` = 'lto-room';
UPDATE `volume` set `location_id` = 'lto-room' where `location_id` is not null;
DELETE FROM `location` WHERE (`id` != 'lto-room');

UPDATE `location` SET `description` = 'SK Office - 1st' WHERE (`id` = 'lto-room');
INSERT INTO `location` (`id`, `default`, `description`) VALUES ('t-block2', b'0', 'T Block - 2nd');
INSERT INTO `location` (`id`, `default`, `description`) VALUES ('t-block3', b'0', 'T Block - 3rd');

UPDATE `copy` SET `location_id` = 't-block2' WHERE (`id` = 2);
UPDATE `copy` SET `location_id` = 't-block3' WHERE (`id` = 3);

