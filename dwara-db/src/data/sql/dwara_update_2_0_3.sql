use `dwara`;
ensure job table flowelement constraint is dropped in prod and also datatype is String 
drop table action_artifactclass_flow;
drop table flowelement;
drop table flow;
drop table artifactclass_processingtask;
create table artifactclass_task;
update job table removing flowelement constraint;
File.directory 