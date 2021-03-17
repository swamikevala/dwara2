-- LIVE incident --
-- The file size fix with dwara_update_2_1_7.sql had a glitch for usecases where there are no files in file1 table for an artifact 
-- e.g., edited where only we save parent dir and stems files 
-- This sql patch fixes it...

UPDATE
   artifact1 as a
SET
   a.total_size = (
       SELECT sum(f.size)
       FROM file1 as f
       WHERE a.id = f.artifact_id and f.size is not null
       GROUP BY f.artifact_id
   ) where a.total_size is null;
   
UPDATE artifact1  SET total_size = 0 where total_size is null;
   
update file1 as f join artifact1 a on f.artifact_id = a.id
set f.size = a.total_size
where f.pathname = a.name;


UPDATE `file1` SET `size`='1221080907114' WHERE `id`='264782';

UPDATE `artifact1` SET `total_size`='1221080907114' WHERE `id`=9983;