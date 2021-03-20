-- T154 was ingested in public and put on hold but need to be written to priv2, hence this script  

-- update artifactclass
UPDATE `request` SET `details`='{\"body\": {\"stagedFiles\": [{\"name\": \"T154\", \"path\": \"/data/dwara/user/prasadcorp/ingest/video-digi-2020-priv2\"}], \"artifactclass\": \"video-digi-2020-priv2\"}}' WHERE `id`='3778';
UPDATE `request` SET `details`='{\"staged_filename\": \"T154\", \"staged_filepath\": \"/data/dwara/user/prasadcorp/ingest/video-digi-2020-priv2\", \"artifactclass_id\": \"video-digi-2020-priv2\"}' WHERE `id`='3779';
UPDATE `artifact1` SET `artifactclass_id`='video-digi-2020-priv2' WHERE `id`='3280';

-- update sequence count
UPDATE `sequence` SET `current_number`='5351' WHERE `id`='video-digi-2020-grp';

-- update sequence
UPDATE `artifact1` SET `name`= replace(name, 'VD772', 'VDX5351'), `sequence_code`='VDX5351' WHERE `id`='3280';
update file1 set pathname = replace(pathname, 'VD772', 'VDX5351'), pathname_checksum = unhex(sha1(pathname)) where artifact_id=3280;
update t_file set pathname = replace(pathname, 'VD772', 'VDX5351'), pathname_checksum = unhex(sha1(pathname)) where artifact_id=3280;
-- update job table
UPDATE `job` SET `group_volume_id`='X1' WHERE `id`='28066';
UPDATE `job` SET `group_volume_id`='X2' WHERE `id`='28067';
UPDATE `job` SET `group_volume_id`='X3' WHERE `id`='28068';

-- delete the proxy jobs
delete from job where id in (28069, 28070);

-- mv the physical folder
-- cd /data/dwara/staged; mv VD772_T154 VDX5351_T154
