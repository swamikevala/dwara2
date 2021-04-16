SET foreign_key_checks = 0; 

-- minimum_free_space configuration 
-- 10 TB
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\":10995116277760}' WHERE `type`='group' and `id` not in ('G1', 'G2', 'G3', 'X1', 'X2', 'X3');

-- 1 TB
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 1099511627776}' WHERE `type`='group' and `id` in ('G1', 'G2', 'G3', 'X1', 'X2', 'X3');

-- Commenting out as better done using programming 
-- For some reason sequencenumber after 9350 got reset to 9265 again 
-- 9264 because from 9350 it got reset to 9265  
-- Set @seqNumberToBeIncremented = (select current_number from sequence WHERE `id`='video-edit-grp') - 9264;
-- source artifacts
-- UPDATE `artifact1` SET `name`=replace(name, sequence_code, CONCAT('Z' ,(CONVERT(SUBSTRING(sequence_code, 2),DECIMAL) + @seqNumberToBeIncremented))), `sequence_code`=replace(sequence_code, sequence_code, CONCAT('Z' ,(CONVERT(SUBSTRING(sequence_code, 2),DECIMAL) + @seqNumberToBeIncremented))) WHERE `id` in (17622,17623,17624,17625,17626,17627,17628,17629,17630,17631,17632,17633,17634,17635,17636,17637,17638,17639,17640,17641,17642,17643,17644,17645,17646,17647,17648,17649,17650,17651,17652,17653,17654,17655,17656,17657,17658,17659,17660,17661,17663,17664,17665,17666,17667,17668,17669,17670,17671,17672,17673,17674,17675,17676,17677,17678,17679,17680,18090,18091,18092,18093,18094,18095,19046,19047,19048,19049,19050,19051,19052,19053,19054,19055,19056,19057,19058,19059,19060,19061,19062,19063,19064,19065,19066,19067);

-- derived artifacts
-- UPDATE `artifact1` SET -- replace(sequence_code, sequence_code, CONCAT('ZL' ,(CONVERT(SUBSTRING((select sequence_code from artifact1 WHERE artifact_ref_id in (13217)), 3),DECIMAL) + @seqNumberToBeIncremented))) from artifact1 WHERE artifact_ref_id in (13217);

-- files also

-- t_file also

-- Dont forget to reset the number to last updated number
-- UPDATE `sequence` SET `current_number`='last seq no + total dupes somehting liek (9487 + 86)' WHERE `id`='video-edit-grp';

SET foreign_key_checks = 1;
