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
select sr.* from request sr where json_extract(sr.details, '$.staged_filename') like binary @binaryold;

UPDATE `request` sr SET sr.`details`= replace(sr.details, json_extract(sr.details, '$.staged_filename'), replace(json_extract(sr.details, '$.staged_filename'),@old,@new)) where json_extract(sr.details, '$.staged_filename') like binary @binaryold;

select sr.* from request sr where json_extract(sr.details, '$.staged_filename') like binary @binarynew;

-- update user request
select json_extract(ur.details, '$.body.stagedFiles[0].name') from request ur where json_extract(ur.details, '$.body.stagedFiles[0].name')  like binary @binaryold;

UPDATE `request` ur SET `details`= replace(ur.details, json_extract(ur.details, '$.body.stagedFiles[0].name'), replace(json_extract(ur.details, '$.body.stagedFiles[0].name'),@old,@new)) where json_extract(ur.details, '$.body.stagedFiles[0].name')  like binary @binaryold;

select json_extract(ur.details, '$.body.stagedFiles[0].name') from request ur where json_extract(ur.details, '$.body.stagedFiles[0].name')  like binary @binarynew;


-- update video-edit-priv1-proxy-low sequence
UPDATE `artifactclass` SET `sequence_id`='video-edit-pub-proxy-low' WHERE `id`='video-edit-priv1-proxy-low';

SET foreign_key_checks = 1;
