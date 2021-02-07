SET foreign_key_checks = 0;

-- LIVE incident - Had to shut the app when these jobs were going on as maintenance mode hasnt got fast clear ...
 
update t_t_file_job set `status`='failed' where status='in_progress' and `job_id` in (73708, 73739, 73740, 56324, 57174, 57666, 64039, 66056, 66428, 66858, 66947, 69872);

update job set `status`='failed' where `id` in (73708, 73739, 73740, 56324, 57174, 57666, 64039, 66056, 66428, 66858, 66947, 69872);

-- 2nd maintenance mode and kill

update t_t_file_job set `status`='failed' where status='queued' and job_id=73759;

update job set `status`='failed' where `id`=73759;

-- reverting the QC gen flow - refer releasenotes for incident summary

UPDATE `flowelement` SET `active`=0, `dependencies`='[\"U5\"]' WHERE `id`='U18';
UPDATE `flowelement` SET `active`=0 WHERE `id`='U19';
UPDATE `flowelement` SET `active`=0 WHERE `id`='U20';

UPDATE `flowelement` SET `dependencies`='[\"U4\", \"U5\"]' WHERE `id`='U6';
UPDATE `flowelement` SET `deprecated`=0, `task_config`='{\"destination_id\": \"bru-qc\", \"pathname_regex\": \".*\\\\.mkv$\"}' WHERE `id`='U14';

SET foreign_key_checks = 1;