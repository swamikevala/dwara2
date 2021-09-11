-- DU-585 - XX pool need to behave like X pool - tapes should be removed immediately after all jobs are completed.
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"remove_after_job\": true, \"minimum_free_space\": 10995116277760}' WHERE `id`='XX1';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"remove_after_job\": true, \"minimum_free_space\": 10995116277760}' WHERE `id`='XX2';
