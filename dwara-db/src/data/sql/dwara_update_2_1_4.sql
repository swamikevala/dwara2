SET foreign_key_checks = 0;

-- LIVE incident - Had to shut the app when these jobs were going on ...
 
update t_t_file_job set `status`='failed' where status='in_progress' and `job_id` in (73708, 73739, 73740, 56324, 57174, 57666, 64039, 66056, 66428, 66858, 66947, 69872);

update job set `status`='failed' where `id` in (73708, 73739, 73740, 56324, 57174, 57666, 64039, 66056, 66428, 66858, 66947, 69872);

SET foreign_key_checks = 1;