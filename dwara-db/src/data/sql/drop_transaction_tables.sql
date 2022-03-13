-- use dwara2_test;
SET FOREIGN_KEY_CHECKS=0;
truncate artifact;
truncate artifact_volume;
truncate artifact_label;
truncate file;
truncate file_volume;
truncate t_file;
truncate t_file_volume;
truncate job;
truncate jobrun;
truncate request;
truncate badfile;
truncate processingfailure;
truncate t_t_file_job;
truncate t_activedevice;

/*
DROP TABLE 
`artifact`, 
`artifact_volume`, 
`badfile`, 
`file`, 
`file_volume`, 
`job`, 
`jobrun`, 
`processingfailure`, 
`request`, 
`t_file`, 
`t_file_volume`, 
`t_activedevice`,
`t_t_file_job`;
*/
SET FOREIGN_KEY_CHECKS=1;