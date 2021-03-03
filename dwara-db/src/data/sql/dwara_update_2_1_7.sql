SET foreign_key_checks = 0; 

UPDATE
   artifact1 as a
SET
   a.total_size = (
       SELECT sum(f.size)
       FROM file1 as f
       WHERE a.id = f.artifact_id and f.directory = 0 and f.deleted = 0
       GROUP BY f.artifact_id
   );
   
update file1 as f join artifact1 a on f.artifact_id = a.id
set f.size = a.total_size
where f.pathname = a.name;

SET foreign_key_checks = 1; 