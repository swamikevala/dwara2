UPDATE `dwara`.`copy` SET `location_id` = 'lto-room';
UPDATE `dwara`.`volume` set `location_id` = 'lto-room' where `location_id` is not null;
DELETE FROM `dwara`.`location` WHERE (`id` != 'lto-room');

UPDATE `dwara`.`location` SET `description` = 'SK Office - 1st' WHERE (`id` = 'lto-room');
INSERT INTO `dwara`.`location` (`id`, `default`, `description`) VALUES ('t-block2', b'0', 'T Block - 2nd');
INSERT INTO `dwara`.`location` (`id`, `default`, `description`) VALUES ('t-block3', b'0', 'T Block - 3rd');

UPDATE `dwara`.`copy` SET `location_id` = 't-block2' WHERE (`id` = 2);
UPDATE `dwara`.`copy` SET `location_id` = 't-block3' WHERE (`id` = 3);

