-- LIVE incident --
-- The drive needed cleaning and the restore job got hung for more than 2 days 
-- This sql patch retrieves the drive back for us

UPDATE `job` SET `message`='Manually set to failed. Job stuck occupying drive', `status`='failed' WHERE `id` in (277277,281037);
DELETE FROM `t_activedevice` WHERE `job_id`in (277277,281037);
-- then  ps -ef | grep dd and kill the process -- otherwise mt status and dwara's tactivedevice not in sync will happen and drive will still not be available... 

-- LIVE Incident  
-- Disk space problem
-- only preservation processing jobs
UPDATE `job` SET `status`='on_hold' WHERE status = 'queued' and processingtask_id != 'video-digi-2020-preservation-gen' ;

UPDATE `job` SET `status`='failed' WHERE `id`='281749';
UPDATE `job` SET `status`='failed' WHERE `id`='281751';

-- only requests that would complete faster --

-- no san restore requests
UPDATE job SET status='on_hold' where status = 'queued' and storagetask_action_id = 'restore' and group_volume_id is null;
update `job` SET `status`='queued' WHERE status='on_hold' and storagetask_action_id='restore' and group_volume_id is null;

-- clean up 279078
UPDATE `t_t_file_job` SET `status`='failed' where job_id=279078 and status='in_progress';
UPDATE `job` SET `status`='failed' WHERE `id`='279078';
-- requeue

UPDATE `artifactclass` SET `config` = '{"pathname_regex": "[^/]+|Video Output/[^/]+\\\\.(mov|mp4)"}' where `id` = 'video-edit-tr-pub';
UPDATE `artifactclass` SET `config` = '{"pathname_regex": "[^/]+|Video Output/[^/]+\\\\.(mov|mp4)"}' where `id` = 'video-edit-tr-priv1';
UPDATE `artifactclass` SET `config` = '{"pathname_regex": "[^/]+|Video Output/[^/]+\\\\.(mov|mp4)"}' where `id` = 'video-edit-tr-priv2';

UPDATE `flowelement` SET `task_config` = '{"pathname_regex": "(Video Output/|Output_)[^/]+\\\\.(mov|mp4)"}' where `id` = 'U26';