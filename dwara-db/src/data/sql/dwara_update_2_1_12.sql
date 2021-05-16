SET foreign_key_checks = 0; 


Set @old = '-HI8-';
Set @new = '-Hi8-';
Set @binaryold = concat('%',@old,'%');
Set @binarynew = concat('%',@new,'%');

-- update artifact
select * from artifact1 where prev_sequence_code like binary (@binaryold);

UPDATE `artifact1` SET `prev_sequence_code`= replace(prev_sequence_code,@old,@new) where prev_sequence_code like binary (@binaryold);

select * from artifact1 where prev_sequence_code like binary (@binarynew);

-- update system request
select sr.* from request sr where sr.id in (select write_request_id from artifact1 where prev_sequence_code like binary (@binarynew));

UPDATE `request` sr SET sr.`details`= replace(sr.details, json_extract(sr.details, '$.staged_filename'), replace(json_extract(sr.details, '$.staged_filename'),@old,@new)) where sr.id in (select write_request_id from artifact1 where prev_sequence_code like binary (@binarynew));

select sr.* from request sr where sr.id in (select write_request_id from artifact1 where prev_sequence_code like binary (@binarynew));

-- update user request
select json_extract(ur.details, '$.body.stagedFiles[0].name') from request ur where ur.id in (select * from(select sr.request_ref_id from request sr where sr.id in (select write_request_id from artifact1 where prev_sequence_code like binary (@binarynew))) tblTmp);

UPDATE `request` ur SET `details`= replace(ur.details, json_extract(ur.details, '$.body.stagedFiles[0].name'), replace(json_extract(ur.details, '$.body.stagedFiles[0].name'),@old,@new)) where ur.id in (select * from(select sr.request_ref_id from request sr where sr.id in (select write_request_id from artifact1 where prev_sequence_code like binary (@binarynew))) tblTmp);

select json_extract(ur.details, '$.body.stagedFiles[0].name') from request ur where ur.id in (select * from(select sr.request_ref_id from request sr where sr.id in (select write_request_id from artifact1 where prev_sequence_code like binary (@binarynew))) tblTmp);


SET foreign_key_checks = 1;
