SET foreign_key_checks = 0;

SET @counter := <<last-id-value-in-t_file table>>;

insert ignore into t_file (`id`, `checksum`, `deleted`, `directory`, `pathname`, `size`, `artifact_id`, `file_ref_id`, `pathname_checksum`)
select @counter:= @counter + 1, g.checksum, g.deleted, g.directory, g.pathname, g.size, g.artifact_id, g.file_ref_id, g.pathname_checksum from
(
   select f.id from request r 
      join artifact1 a on a.write_request_id = r.id 
      join file1 f on f.artifact_id = a.id 
   where r.action_id="ingest" and r.type="system" and r.status not in ("cancelled", "completed")
   union distinct
   select f.id from file1 f 
      join artifact1 a on f.artifact_id = a.id 
      join artifact1_volume av on a.id = av.artifact_id
      join volume v on av.volume_id = v.id
   group by f.id
   having min(v.finalized) = 0
) as sub
join file1 g on sub.id = g.id;

insert ignore into t_file_volume (`file_id`, `archive_block`, `deleted`, `encrypted`, `verified_at`, `volume_block`, `volume_id`, `header_blocks`)
select tf.file_id, fv.archive_block, fv.deleted, fv.encrypted, fv.verified_at, fv.volume_block, fv.volume_id, fv.header_blocks 
from t_file tf 
   join file1 f on tf.pathname_checksum = f.pathname_checksum
   join file1_volume fv on f.id = fv.file_id;

-- Fix for file_ref_id using primitive int
UPDATE `t_file` SET `file_ref_id` = null WHERE (`file_ref_id` = 0); 
 
SET foreign_key_checks = 1;
