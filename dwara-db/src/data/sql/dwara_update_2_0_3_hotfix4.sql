SET foreign_key_checks = 0;

UPDATE
    request AS r
SET
    r.completed_at = (
        SELECT MAX(j.completed_at)
        FROM job j
        WHERE r.id = j.request_id
        GROUP BY r.id
    )
    where (r.status != 'queued' and r.status != 'on_hold' and r.status != 'in_progress');

    
-- need to update user requests from the system requests

SET foreign_key_checks = 1;
