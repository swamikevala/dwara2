-- LIVE incident --
-- The drive needed cleaning and the restore job got hung for more than 2 days 
-- This sql patch retrieves the drive back for us

UPDATE `job` SET `message`='Manually set to failed. Job taking more than 2 days', `status`='failed' WHERE `id`='230651';
DELETE FROM `t_activedevice` WHERE `id`='103926';

-- LIVE Incident 2 - 
-- When we were trying to retrieve the drive from the above hung job, instead of deleting the running dd process, we killed its parent process which was dwara.
-- Following jobs were inprogress when dwara got killed
-- 230651 - job in question hung restore job
-- 155992 - mamupdate
-- 228715, 231063, 232859, 238637, 238639 - some in progress processing tasks select * from job where id in (228715, 231063, 232859, 238637, 238639);

UPDATE `job` SET `status`='failed' WHERE status = 'in_progress';
UPDATE `t_t_file_job` SET `status`='failed' WHERE job_id in  (228715, 231063, 232859, 238637, 238639) and status = 'in_progress';
DELETE FROM `t_activedevice`;