-- BM9 cancelled - rogue artifact - so marked the failed jobs as completed so next of queued jobs will get picked up... 
UPDATE `job` SET `status`='completed' WHERE `id` in ('492037','492038','492040');
UPDATE `request` SET `status`='completed' WHERE `id`='77612';


-- ROLLOUT DAY appends
-- during rollout this processing job had lot of files queued for processing - delaying the app shutdown - so had to put it on hold so ProcessingJobManager skips the queued files
update job set status = 'on_hold' where id = 494329;

-- after rollout we put back the job to queued state
update job set status = 'queued' where id = 494329;

-- When Rewrite jobs were queued which generally in 1000+ viewing tape UI caused all the jobs to be loaded for storage job meta wrapping pulling the DB and hence system down - so had to put the jobs on_hold and once fixed will set the jobs back to queued  
update request set status='queued' where action_id='rewrite' and type ='system' and status='on_hold';
update job set status='queued' where status='on_hold' and storagetask_action_id='restore';

-- LIVE Incident - Rewrite jobs were queued but requests were on_hold thus causing the tapeView to pull the system down again - had to hard stop the app when the requeued write job 492808 in_progress
-- So had to run the following queries later...
DELETE FROM `t_activedevice` WHERE `id`='231200';
update job set status = 'queued' where id = 492808;

ALTER TABLE `file1_volume` CHANGE COLUMN `volume_block` `volume_start_block` INT(11) NULL DEFAULT NULL ;
ALTER TABLE `file2_volume` CHANGE COLUMN `volume_block` `volume_start_block` INT(11) NULL DEFAULT NULL ;
