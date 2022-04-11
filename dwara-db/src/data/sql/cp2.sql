-- V29 issue 
-- Swami Kevala mentioned that he could not find the artifact in A101 disk and found something in the trash can... 
-- NOTE: V29 and others failed once earlier, because A101 was pulled out when jobs were in progress and had to be requeued.
-- We dont know how it went missing but definitely logs shows write is sucessful and we didnt have the verify to follow right after write logic then.
-- So verify job only would get processed later but then the disk was detached and even all previous verify jobs on that disk failed along with V29 verify 
 
 
-- Swami tried bringing it from trashcan to A101 and showed a weird prompt and we werent sure if V29 in A101 is good. So we decided to delete it from A101 and redo. 
-- We could have done checksum-verify like we tried in B101 but went without waiting for the verify result

-- job 237 deleted
DELETE FROM `job` WHERE `id`='237';

-- completed job 202 put back to queue
UPDATE `job` SET `status`='queued' WHERE `id`='202';

-- When disks were attached checksum-verifier had a bug that would mark the file completed even without verifying
UPDATE `job` SET `status`='queued' WHERE `id` in ('224','225','226','227');


UPDATE `dwara`.`job` SET `status`='on_hold' WHERE `id`='275';

-- mark the volumes suspsect so we can continue with the rest

UPDATE `copy` SET `location_id`='rally' WHERE `id`='1';
UPDATE `copy` SET `location_id`='vol' WHERE `id`='2';
DELETE FROM `copy` WHERE `id`='3';

DELETE FROM `location` WHERE `id`in ('lto-room','sk-office1','t-block2','t-block3');
INSERT INTO `location` (`id`, `default`, `description`) VALUES ('vol', 0, 'With Volunteer');


-- Some bad file causing the job to fail stagnating the artifact in staged. Had to do the bad file process 31st Mar 2022

UPDATE `file` SET `bad`=1, `reason`='0 sized file. Karthik anna confirmed this file can be deleted' WHERE `pathname` in (select pathname from t_file where id = '42147');
UPDATE `t_file` SET `bad`=1, `reason`='0 sized file. Karthik anna confirmed this file can be deleted' WHERE `id`='42147';

DELETE FROM `t_t_file_job` WHERE `file_id`='42147';

UPDATE `job` SET `status`='marked_completed' WHERE `id`='775';

-- create dependent job using api - but actually missed out creating the dependent jobs...

UPDATE `request` SET `status`='queued' WHERE `id` in ('212','210'); -- update both system and user request to queued

