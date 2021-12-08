SET foreign_key_checks = 0;

-- Domain changes - missed out dropping the below table
DROP TABLE `domain`;

-- Import changes - move finalizedDate to details...
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2017-12-31 00:00:00.000000\"}', `finalized_at`=null WHERE `id`='C16139L6';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2020-05-06 00:00:00.000000\"}', `finalized_at`=null WHERE `id`='C17027L6';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2020-05-16 00:00:00.000000\"}', `finalized_at`=null WHERE `id`='C17031L7';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2010-12-29 00:00:00.000000\"}', `finalized_at`=null WHERE `id`='CA4220L4';
UPDATE `volume` SET `details`='{\"barcoded\": true, \"blocksize\": 1048576, \"written_at\": \"2020-05-14 00:00:00.000000\"}', `finalized_at`=null WHERE `id`='P17023L6';

SET foreign_key_checks = 1;