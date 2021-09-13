-- Edited failures
UPDATE `request` SET `status`='failed' WHERE `id`='28880';
UPDATE `request` SET `status`='failed', `completed_at`='2021-01-21 12:52:39.435000' WHERE `id`='7588';
UPDATE `request` SET `status`='failed' WHERE `id`='7587';
UPDATE `request` SET `status`='failed' WHERE `id`='7586';

-- Edited priv2 failures

UPDATE `request` SET `status`='failed' WHERE `id`='24023';
UPDATE `request` SET `status`='failed' WHERE `id`='24024';

UPDATE `request` SET `status`='cancelled' WHERE action_id='finalize' and status='failed' and (json_extract(details, '$.volume_id') != 'E10003L7' && json_extract(details, '$.volume_id') != 'E30003L7');

-- delete the failed qc gen job and resetting system request to queued
-- BC64
DELETE FROM processingfailure WHERE job_id=73776;
DELETE FROM t_t_file_job WHERE job_id=73776;
DELETE FROM `job` WHERE `id`=73776;
UPDATE `request` SET `status`='queued' WHERE `id`='12775';

-- BC60
DELETE FROM t_t_file_job WHERE job_id=73740;
DELETE FROM `job` WHERE `id`=73740; 
UPDATE `request` SET `status`='queued' WHERE `id`='12763';


-- Reset the proxy job to queued I23 
DELETE from jobrun where job_id=194419;
DELETE from jobrun where job_id=194420;
DELETE FROM `job` where id in (14790, 14791, 14792, 14793, 194418, 194419, 194420, 194421);
UPDATE `job` SET `status`='queued' WHERE `id`='13768'; -- We should have executed this first as executing the above moved the artifact to completed status and deleted the files...

-- Should we clean up the artifact and file entries too??? 
