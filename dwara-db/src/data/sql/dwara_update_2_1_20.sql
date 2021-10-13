SET foreign_key_checks = 0;

-- delete artifact1_volume.status

UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='E1';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='E2';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='E3';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R1';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R198';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R2';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R298';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R3';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R398';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"remove_after_job\": true, \"minimum_free_space\": 1099511627776}' WHERE `id`='XX1';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"remove_after_job\": true, \"minimum_free_space\": 1099511627776}' WHERE `id`='XX2';
