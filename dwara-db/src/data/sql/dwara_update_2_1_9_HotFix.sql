-- LIVE incident --
-- The drive needed cleaning and the restore job got hung for more than 2 days 
-- This sql patch retrieves the drive back for us

UPDATE `job` SET `message`='Manually set to failed. Job taking more than 2 days', `status`='failed' WHERE `id`='230651';
DELETE FROM `t_activedevice` WHERE `id`='103926';
-- then  ps -ef | grep dd and kill the process -- otherwise mt status and dwara's tactivedevice not in sync will happen and drive will still not be available... 

-- LIVE Incident 2 - 
-- When we were trying to retrieve the drive from the above hung job, instead of deleting the running dd process, we killed its parent process which was dwara.
-- Following jobs were inprogress when dwara got killed
-- 230651 - job in question hung restore job
-- 155992 - mamupdate
-- 228715, 231063, 232859, 238637, 238639 - some in progress processing tasks select * from job where id in (228715, 231063, 232859, 238637, 238639);

UPDATE `job` SET `status`='failed' WHERE status = 'in_progress';
UPDATE `t_t_file_job` SET `status`='failed' WHERE job_id in  (228715, 231063, 232859, 238637, 238639) and status = 'in_progress';
DELETE FROM `t_activedevice`;

UPDATE `job` SET `message`='Manually set to failed. Job blocks drive when NFS is down', `status`='failed' WHERE `id` in ('261878, 262050');
DELETE FROM `t_activedevice` WHERE `job_id` in ('261878, 262050');

-- hold other processing tasks and give preservation jobs more cpu
UPDATE job SET status='on_hold' WHERE status = 'queued' and processingtask_id != 'video-digi-2020-preservation-gen' ;
-- fail the in_progress jobs and do clean up later
UPDATE job SET status='failed' WHERE id='281749';
UPDATE job SET status='failed' WHERE id='281751';
-- revert the holded processing jobs to queued
update job set status='queued' where id in (76264,76265,76266,273298,275458,275713,275759,275791,275796,275895,276085,276105,276149,276179,276207,276213,276239,276260,276267,276299,276343,276927,277049,277085,277175,277269,277333,277469,277499,277551,277735,277790,277841,277919,277994,278026,278148,278191,278224,278281,278322,278351,278355,278387,278402,278418,278463,278477,278513,278633,278669,278693,278723,278753,278839,278870,278980,279004,279083,279089,279094,279099,279104,279109,279114,279119,279124,279129,279134,279139,279144,279149,279154,279159,279164,279169,279174,279179,279184,279189,279194,279199,279204,279209,279214,279219,279224,279229,279234,279260,279266,279271,279276,279281,279286,279291,279298,279303,279308,279313,279318,279323,279328,279333,279338,279343,279348,279353,279358,279365,279370,279375,279380,279386,279391,279396,279401,279406,279411,279416,279421,279426,279431,279436,279441,279446,279451,279456,279461,279466,279471,279476,279481,279486,279491,279496,279501,279506,279511,279516,279521,279526,279531,279536,279541,279546,279551,279556,279561,279567,279572,279577,279582,279587,279592,279597,279602,279607,279612,279617,279622,279627,279632,279637,279642,279647,279652,279658,279663,279668,279673,279679,279685,279691,279696,279701,279706,279711,279716,279721,279726,279731,279737,279742,279747,279752,279757,279762,279768,279773,279779,279784,279789,279794,279800,279805,279811,279816,279821,279826,279832,279837,279842,279847,279852,279857,279862,279867,279872,279877,279882,279887,279892,279897,279902,279907,279912,279919,279925,279930,279935,279940,279945,279950,279955,279960,279965,279970,279975,279980,279985,279990,279995,280000,280005,280010,280015,280020,280025,280030,280035,280040,280045,280050,280055,280060,280065,280070,280075,280080,280085,280090,280095,280132,280155,280160,280165,280170,280176,280181,280186,280191,280196,280201,280206,280211,280216,280221,280226,280231,280236,280242,280247,280252,280534,280712,280854,280890,281545,281550,281555,281560,281565,281571,281576,281581,282006,282007,282008);
-- hold the restore jobs so write jobs are given drive time
UPDATE job SET status='on_hold' where status = 'queued' and storagetask_action_id = 'restore' and group_volume_id is null;

UPDATE `job` SET `message`='Manually set to failed. Dwara hard shutdown', `status`='failed' WHERE `id` in (261877, 281557);
DELETE FROM `t_activedevice` WHERE `job_id` in (261877, 281557);


-- clean up photo proxy 279078
UPDATE `t_t_file_job` SET `status`='failed' where job_id=279078 and status='in_progress';
UPDATE `job` SET `status`='failed' WHERE `id`='279078';
-- requeue

-- clean up video proxy 
UPDATE `t_t_file_job` SET `status`='failed' where job_id in (277506, 276417) and status='in_progress';
UPDATE `job` SET `status`='failed' WHERE `id` in (277506, 276417);
-- requeue


 