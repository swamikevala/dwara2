SET foreign_key_checks = 0;

UPDATE `request` SET `status`='cancelled' WHERE `id` in (
83460,
83459,	
83457,
83456,	
83453,
83452,	
83382);
UPDATE `request` SET `status`='queued' WHERE `id`='83379';
SET foreign_key_checks = 1;

