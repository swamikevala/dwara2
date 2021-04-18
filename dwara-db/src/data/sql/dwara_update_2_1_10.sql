use frm_prd_1st_mar;
UPDATE `artifactclass` SET `description` = 'edited video digi proxy' WHERE (`id` = 'video-digi-2020-edit-priv1-proxy-low');
UPDATE `artifactclass` SET `description` = 'audio' WHERE (`id` = 'audio-priv1');
UPDATE `artifactclass` SET `description` = 'audio' WHERE (`id` = 'audio-priv2');
UPDATE `artifactclass` SET `description` = 'audio' WHERE (`id` = 'audio-priv3');
UPDATE `artifactclass` SET `description` = 'audio' WHERE (`id` = 'audio-pub');
UPDATE `artifactclass` SET `description` = 'edited video digi' WHERE (`id` = 'video-digi-2020-edit-priv1');
UPDATE `artifactclass` SET `description` = 'edited video digi' WHERE (`id` = 'video-digi-2020-edit-priv2');
UPDATE `artifactclass` SET `description` = 'edited video digi proxy' WHERE (`id` = 'video-digi-2020-edit-priv2-proxy-low');
UPDATE `artifactclass` SET `description` = 'edited video digi' WHERE (`id` = 'video-digi-2020-edit-pub');
UPDATE `artifactclass` SET `description` = 'edited video digi proxy' WHERE (`id` = 'video-digi-2020-edit-pub-proxy-low');
UPDATE `artifactclass` SET `description` = 'video digi' WHERE (`id` = 'video-digi-2020-priv1');
UPDATE `artifactclass` SET `description` = 'video digi proxy' WHERE (`id` = 'video-digi-2020-priv1-proxy-low');
UPDATE `artifactclass` SET `description` = 'video digi' WHERE (`id` = 'video-digi-2020-priv2');
UPDATE `artifactclass` SET `description` = 'video digi proxy' WHERE (`id` = 'video-digi-2020-priv2-proxy-low');
UPDATE `artifactclass` SET `description` = 'video digi' WHERE (`id` = 'video-digi-2020-pub');
UPDATE `artifactclass` SET `description` = 'video digi proxy' WHERE (`id` = 'video-digi-2020-pub-proxy-low');
UPDATE `artifactclass` SET `description` = 'edited video' WHERE (`id` = 'video-edit-priv1');
UPDATE `artifactclass` SET `description` = 'edited video proxy' WHERE (`id` = 'video-edit-priv1-proxy-low');
UPDATE `artifactclass` SET `description` = 'edited video' WHERE (`id` = 'video-edit-priv2');
UPDATE `artifactclass` SET `description` = 'edited video proxy' WHERE (`id` = 'video-edit-priv2-proxy-low');
UPDATE `artifactclass` SET `description` = 'edited video' WHERE (`id` = 'video-edit-pub');
UPDATE `artifactclass` SET `description` = 'edited video proxy' WHERE (`id` = 'video-edit-pub-proxy-low');
UPDATE `artifactclass` SET `description` = 'video' WHERE (`id` = 'video-priv1');
UPDATE `artifactclass` SET `description` = 'video proxy' WHERE (`id` = 'video-priv1-proxy-low');
UPDATE `artifactclass` SET `description` = 'video' WHERE (`id` = 'video-priv2');
UPDATE `artifactclass` SET `description` = 'video proxy' WHERE (`id` = 'video-priv2-proxy-low');
UPDATE `artifactclass` SET `description` = 'video' WHERE (`id` = 'video-priv3');
UPDATE `artifactclass` SET `description` = 'video' WHERE (`id` = 'video-pub');
UPDATE `artifactclass` SET `description` = 'video proxy' WHERE (`id` = 'video-pub-proxy-low');
UPDATE `artifactclass` SET `description` = 'edited photo' WHERE (`id` = 'photo-edit-priv2');
UPDATE `artifactclass` SET `description` = 'edited photo proxy' WHERE (`id` = 'photo-edit-priv2-proxy');
UPDATE `artifactclass` SET `description` = 'edited photo' WHERE (`id` = 'photo-edit-pub');
UPDATE `artifactclass` SET `description` = 'edited photo proxy' WHERE (`id` = 'photo-edit-pub-proxy');
UPDATE `artifactclass` SET `description` = 'photo' WHERE (`id` = 'photo-priv2');
UPDATE `artifactclass` SET `description` = 'photo proxy' WHERE (`id` = 'photo-priv2-proxy');
UPDATE `artifactclass` SET `description` = 'photo' WHERE (`id` = 'photo-pub');
UPDATE `artifactclass` SET `description` = 'photo proxy' WHERE (`id` = 'photo-pub-proxy');



UPDATE `artifactclass` SET `display_order` = '1' WHERE (`id` like 'audio-p%');

UPDATE `artifactclass` SET `display_order` = '2' WHERE (`id` like 'video-p%');
UPDATE `artifactclass` SET `display_order` = '3' WHERE (`id` like 'video-digi-2020-p%');
UPDATE `artifactclass` SET `display_order` = '4' WHERE (`id` like 'video-digi-2020-edit-p%');
UPDATE `artifactclass` SET `display_order` = '5' WHERE (`id` like 'video-edit-p%');
UPDATE `artifactclass` SET `display_order` = '6' WHERE (`id` like 'video-edit-tr-p%');

UPDATE `artifactclass` SET `display_order` = '7' WHERE (`id` like 'photo-p%');
UPDATE `artifactclass` SET `display_order` = '8' WHERE (`id` like 'photo-edit-p%');


