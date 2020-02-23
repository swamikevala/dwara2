create database dwara;

create user 'dwara'@'localhost' identified by 'dwara123';

GRANT ALL PRIVILEGES ON * . * TO 'dwara'@'localhost';

flush privileges;

