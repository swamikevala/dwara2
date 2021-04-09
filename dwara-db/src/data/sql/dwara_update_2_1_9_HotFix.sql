-- LIVE incident --
-- The drive needed cleaning and the restore job got hung for more than 2 days 
-- This sql patch retrieves the drive back for us

UPDATE `job` SET `message`='Manually set to failed. Job taking more than 2 days', `status`='failed' WHERE `id`='230651';
DELETE FROM `t_activedevice` WHERE `id`='103926';

-- Following jobs were inprogress when dwara got killed
155992 - mamupdate
230651 - restore hang 
228715, 231063, 232859, 238637, 238639
select * from job where id in (, 228715, 230651, 231063, 232859, 238637, 238639);

UPDATE `job` SET `status`='failed' WHERE status = 'in_progress';
UPDATE `t_t_file_job` SET `status`='failed' WHERE job_id in  (228715, 231063, 232859, 238637, 238639) and status = 'in_progress';
DELETE FROM `t_activedevice`;