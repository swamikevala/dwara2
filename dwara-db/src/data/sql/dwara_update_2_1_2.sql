SET foreign_key_checks = 0;

-- Bug in our porting scripts in earlier release where the t.file_ref_id was referring to the file table and not the t_file table 
update t_file tf1 join file1 f on tf1.file_ref_id = f.id join t_file tf2 on tf2.pathname_checksum = f.pathname_checksum set tf1.file_ref_id = tf2.id;

update t_file_sequence set next_val = 114400;

-- There were digi artifacts queued put on hold so when app is online they dont get dequeued until digi is remediated 
UPDATE `job` SET `status`='on_hold' WHERE `id` in(27548, 27549, 30230, 30231, 30244, 30245, 30569, 30570, 30575, 30576, 30624, 30625, 30640, 30641, 30731, 30732, 30762, 30763, 31671, 31672, 31680, 31681, 31715, 31716, 31723, 31724, 31751, 31752, 31777, 31778, 31786, 31787, 31836, 31837, 31866, 31867, 31877, 31878, 31921, 31922, 31956, 31957, 31976, 31977, 32052, 32053, 32088, 32089, 32093, 32094, 32119, 32120, 33579, 33580, 35301, 35302, 36205, 36206, 36279, 36280, 37641, 37642, 37658, 37659, 37725, 37726, 38223, 38224, 42679, 42680);	




SET foreign_key_checks = 1;
