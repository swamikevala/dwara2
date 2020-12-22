-- preprod env alterations
truncate artifact1;
truncate artifact1_volume;
truncate artifact2;
truncate artifact2_volume;
truncate artifact_sequence;
truncate dwara_sequences;
truncate file1;
truncate file1_volume;
truncate file2;
truncate file2_volume;
truncate file1_volume;
truncate file_sequence;
truncate job;
truncate jobrun;
truncate request;
truncate badfile;
truncate processingfailure;
truncate t_file_job;
truncate t_activedevice;

update artifactclass set path_prefix = replace(path_prefix, '/dwara/', '/dwara-preprod/'); 

-- change volume barcode sequences R -> S, G -> H, X -> W
update volume set id = replace(id, 'R', 'S');
update volume set id = replace(id, 'G', 'H');
update volume set id = replace(id, 'X', 'W');

update artifactclass_volume set volume_id = replace(volume_id, 'R', 'S');
update artifactclass_volume set volume_id = replace(volume_id, 'G', 'H');
update artifactclass_volume set volume_id = replace(volume_id, 'X', 'W');

update sequence set prefix = replace(prefix, 'R', 'S') where type="volume";
update sequence set prefix = replace(prefix, 'G', 'H') where type="volume";
update sequence set prefix = replace(prefix, 'X', 'W') where type="volume";

-- repoint to Test SAN mount
update destination set path = replace(path, '/dwara/', '/dwara-preprod/'); 
update destination set path = '/mnt/san/test' where id = 'san-video';
delete from destination where id = 'san-video1';