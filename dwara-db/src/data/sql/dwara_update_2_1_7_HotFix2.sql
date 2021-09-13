-- LIVE incident --
-- The file size fix with dwara_update_2_1_7_HotFix1.sql had a glitch for edited artifacts which doesnt have any file entries in file1 table but only subfolders as we dont save files in file1 but only for stems directory
-- e.g., edited where only we save parent dir and stems files 
-- This sql patch fixes it...

UPDATE
   artifact1 as a
SET
   a.total_size = (
       SELECT sum(f.size)
       FROM t_file as f
       WHERE a.id = f.artifact_id and f.directory = 0 and f.deleted = 0
       GROUP BY f.artifact_id
   )
where artifactclass_id like 'video-edit%' and not artifactclass_id like '%proxy%';

update file1 as f join artifact1 a on f.artifact_id = a.id set f.size = a.total_size where f.pathname = a.name ;

ALTER TABLE `t_file` 
CHANGE COLUMN `symlink_path` `symlink_path` VARCHAR(4096) NULL DEFAULT NULL ;

ALTER TABLE `file1` 
CHANGE COLUMN `symlink_path` `symlink_path` VARCHAR(4096) NULL DEFAULT NULL ;

ALTER TABLE `file2` 
CHANGE COLUMN `symlink_path` `symlink_path` VARCHAR(4096) NULL DEFAULT NULL ;

UPDATE artifact1  SET total_size = 0 where total_size is null;