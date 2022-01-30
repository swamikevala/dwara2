SET foreign_key_checks = 0;


-- new volume groups
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('BB', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '2');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('BC', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '3');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('PB', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '2');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('PC', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '3');


SET foreign_key_checks = 1;

