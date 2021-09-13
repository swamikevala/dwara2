-- prev_sequence_code missed out getting updated because of sequence.coderegex for video-digi-2020-edit* 
UPDATE `artifact1` SET `prev_sequence_code`=replace(name, CONCAT(sequence_code, '_')) WHERE name REGEXP '_Z-DVCAM' and artifactclass_id not like '%-proxy-low';
UPDATE `artifact1` SET `prev_sequence_code`='Z-DVCAM-832' WHERE `id`='37327';
UPDATE `artifact1` SET `prev_sequence_code`='Z-DVCAM-1165' WHERE `id`='37328';
UPDATE `artifact1` SET `prev_sequence_code`='GR13' WHERE `id`='27665';
UPDATE `artifact1` SET `prev_sequence_code`='GR41' WHERE `id`='29194';


-- M66A is an one off rare seq code our regex wont pick up so manually fixing it
UPDATE `artifact1` SET `prev_sequence_code`='M66A' WHERE `id`='27918';




