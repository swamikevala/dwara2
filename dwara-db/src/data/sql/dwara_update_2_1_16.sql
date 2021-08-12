UPDATE `action` SET `type`='complex' WHERE `id`='rewrite';

INSERT INTO `processingtask` (`id`, `filetype_id`, `max_errors`, `output_artifactclass_suffix`) VALUES ('video-digi-2020-mkv-mov-gen', 'mkv', '1', '');

UPDATE `artifact1` SET `file_count`='5' WHERE `id`='1985';

UPDATE `artifact1` SET `file_count`='3' WHERE `id`='28798';

-- update query for some artifacts ingested as edited but not matching the sequence pattern and hence missing out on extracted prevseqcode
UPDATE `artifact1` SET `prev_sequence_code`=replace(name, CONCAT(sequence_code, '_'),'') 
WHERE prev_sequence_code is null and artifactclass_id like 'video-digi-2020%' and artifactclass_id not like '%-proxy-low' and deleted=0;

