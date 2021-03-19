-- Z140 was ingested in public and put on hold but need to be written to priv2, hence this script  

Set @artifactId = 14445;

-- update artifactclass
UPDATE `request` SET `details`='{\"body\": {\"stagedFiles\": [{\"name\": \"Z140\", \"path\": \"/data/dwara/user/prasadcorp/ingest/video-digi-2020-edit-priv2\"}], \"artifactclass\": \"video-digi-2020-edit-priv2\"}}' WHERE `id`='26804';
UPDATE `request` SET `details`='{\"staged_filename\": \"Z140\", \"staged_filepath\": \"/data/dwara/user/prasadcorp/ingest/video-digi-2020-edit-priv2\", \"artifactclass_id\": \"video-digi-2020-edit-priv2\"}' WHERE `id`='26805';
UPDATE `artifact1` SET `artifactclass_id`='video-digi-2020-edit-priv2' WHERE `id`=@artifactId;

-- update sequence count
UPDATE `sequence` SET `current_number`=' check this out in the morning on the value 390' WHERE `id`='video-digi-2020-edit-grp';

-- update sequence
UPDATE `artifact1` SET `name`= replace(name, 'ZD183', 'ZDX390'), `sequence_code`='ZDX390' WHERE `id`=@artifactId;
update file1 set pathname = replace(pathname, 'ZD183', 'ZDX390'), pathname_checksum = unhex(sha1(pathname)) where artifact_id=@artifactId;
update t_file set pathname = replace(pathname, 'ZD183', 'ZDX390'), pathname_checksum = unhex(sha1(pathname)) where artifact_id=@artifactId;

-- update job table
UPDATE `job` SET `group_volume_id`='X1' WHERE `id`='157512';
UPDATE `job` SET `group_volume_id`='X2' WHERE `id`='157513';
UPDATE `job` SET `group_volume_id`='X3' WHERE `id`='157514';

-- delete the proxy jobs
delete from job where id in (157515, 157516);

-- mv the physical folder
-- cd /data/dwara/staged; mv ZD183_Z140 ZDX390_Z140
