use dwara;
alter table request
add file_id int as (details->'$.file_id') stored after details;

alter table request
add index (file_id);