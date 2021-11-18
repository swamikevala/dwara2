SET foreign_key_checks = 0; 

update artifactclass set path_prefix = replace(path_prefix, '/dwara/', '/dwara-preprod/'); 

-- change volume barcode sequences R -> S, G -> H, X -> W, E -> F
update volume set id = replace(id, 'R', 'S');
update volume set id = replace(id, 'G', 'H');
update volume set id = replace(id, 'X', 'W');
update volume set id = replace(id, 'E', 'F');

update volume set group_ref_id = replace(group_ref_id, 'R', 'S');
update volume set group_ref_id = replace(group_ref_id, 'G', 'H');
update volume set group_ref_id = replace(group_ref_id, 'X', 'W');
update volume set group_ref_id = replace(group_ref_id, 'E', 'F');

update artifactclass_volume set volume_id = replace(volume_id, 'R', 'S');
update artifactclass_volume set volume_id = replace(volume_id, 'G', 'H');
update artifactclass_volume set volume_id = replace(volume_id, 'X', 'W');
update artifactclass_volume set volume_id = replace(volume_id, 'E', 'F');

update sequence set prefix = replace(prefix, 'R', 'S') where type="volume";
update sequence set prefix = replace(prefix, 'G', 'H') where type="volume";
update sequence set prefix = replace(prefix, 'X', 'W') where type="volume";
update sequence set prefix = replace(prefix, 'E', 'F') where type="volume";

-- repoint to Test SAN mount
update destination set path = replace(path, '/dwara/', '/dwara-preprod/'); 
update destination set path = '/mnt/san/test' where id = 'san-video';
delete from destination where id = 'san-video1';

/* NEED TO UNCOMMENT EITHER OF THE ONE
-- preprod env alterations
drop table dwara_sequences;
drop table artifact_sequence;
drop table file_sequence;

truncate artifact1;
truncate artifact1_volume;
truncate artifact2;
truncate artifact2_volume;
truncate artifact1_label;
truncate file1;
truncate file1_volume;
truncate file2;
truncate file2_volume;
truncate file1_volume;
truncate job;
truncate jobrun;
truncate request;
truncate badfile;
truncate processingfailure;
truncate t_file_job;
truncate t_activedevice;
*/
/*

# Set all in_progress jobs to marked_failed
UPDATE `job` SET `status` = 'marked_failed', `completed_at` = now(), `message` = 'Running jobs at the time of prod db backup. Manually set by test env rebase script.' WHERE (`status` = 'in_progress');
UPDATE `job` SET `status` = 'marked_failed', `completed_at` = now(), `message` = 'Running jobs at the time of prod db backup. Manually set by test env rebase script.' WHERE (`status` = 'in_progress');

# Set all in_progress files to marked _failed
UPDATE `t_t_file_job` SET `status` = 'marked_failed' WHERE (`status` = 'in_progress');

UPDATE `volume` SET `imported`=1 WHERE `id`in ('C1','C2','C3','CA','CB','CC','P1','P2');

update job set volume_id = replace(volume_id, 'R', 'S');
update job set volume_id = replace(volume_id, 'G', 'H');
update job set volume_id = replace(volume_id, 'X', 'W');
update job set volume_id = replace(volume_id, 'E', 'F');

update artifact1_volume set volume_id = replace(volume_id, 'R', 'S');
update artifact1_volume set volume_id = replace(volume_id, 'G', 'H');
update artifact1_volume set volume_id = replace(volume_id, 'X', 'W');
update artifact1_volume set volume_id = replace(volume_id, 'E', 'F');


update file1_volume set volume_id = replace(volume_id, 'R', 'S');
update file1_volume set volume_id = replace(volume_id, 'G', 'H');
update file1_volume set volume_id = replace(volume_id, 'X', 'W');
update file1_volume set volume_id = replace(volume_id, 'E', 'F');


update t_file_volume set volume_id = replace(volume_id, 'R', 'S');
update t_file_volume set volume_id = replace(volume_id, 'G', 'H');
update t_file_volume set volume_id = replace(volume_id, 'X', 'W');
update t_file_volume set volume_id = replace(volume_id, 'E', 'F');

**/

SET foreign_key_checks = 1; 