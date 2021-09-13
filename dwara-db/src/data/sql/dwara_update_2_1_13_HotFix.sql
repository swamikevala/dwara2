SET foreign_key_checks = 0; 

INSERT INTO `role` (`id`, `description`, `name`) VALUES ('1', 'Administrator -  has all priviliges', 'admin');
INSERT INTO `role` (`id`, `description`, `name`) VALUES ('2', 'Users with ingest priviliges', 'ingester');
INSERT INTO `role` (`id`, `description`, `name`) VALUES ('3', 'Users with restore priviliges', 'restorer');


INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('1', '2');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('1', '3');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('1', '4');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('1', '6');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('1', '7');

INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('2', '2');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('2', '3');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('2', '4');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('2', '6');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('2', '7');

INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('3', '2');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('3', '3');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('3', '4');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('3', '6');
INSERT INTO `role_user` (`role_id`, `user_id`) VALUES ('3', '7');

SET foreign_key_checks = 1;

