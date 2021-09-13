SET foreign_key_checks = 0;

alter table file1_volume modify archive_block BIGINT;
alter table file2_volume modify archive_block BIGINT;
alter table t_file_volume modify archive_block BIGINT;

SET foreign_key_checks = 1;