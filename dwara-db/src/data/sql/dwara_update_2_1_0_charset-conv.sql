set foreign_key_checks = 0; 

alter database `dwara` character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `action` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `action_artifactclass_flow` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `action_artifactclass_user` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `archiveformat` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact1` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact1_volume` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact2` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact2_volume` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact_sequence` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass_destination` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass_task` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass_volume` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `badfile` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `copy` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `destination` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `device` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `domain` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `dwara_sequences` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `error` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `extension` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `extension_filetype` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `file1` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `file1_volume` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `file2` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `file2_volume` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `file_sequence` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `filetype` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `flow` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `flowelement` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `job` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `jobrun` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `location` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `priorityband` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `processingfailure` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `processingtask` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `request` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `sequence` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `t_activedevice` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `t_file_job` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `user` character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `version` character set utf8mb4 collate utf8mb4_unicode_520_ci;

-- drop foreign keys
alter table `action_artifactclass_flow` drop foreign key `FKidvy2kx9wbuxydhl4ggyd8wjj`;
alter table `action_artifactclass_user` drop foreign key `FKau5h2q88roe24d6ven9yd01lq`;
alter table `action_artifactclass_user` drop foreign key `FKb2xnsp7o5cfj343eepjd2pe5y`;
alter table `artifact1` drop foreign key `FKgpwy8dq7gm1uwoelg3ltdbqps`;
alter table `artifact2` drop foreign key `FKmecotjocalex0re2c11qhhjxg`;
alter table `artifactclass` drop foreign key `FKm2307v6xaw32qecrwjkl6lvsu`;
alter table `artifactclass_destination` drop foreign key `FK4djwomf3rwcpmsggge0379hs6`;
alter table `artifactclass_task` drop foreign key `FK7fufhtcsqji1l21eapxfn73g5`;
alter table `artifactclass_volume` drop foreign key `FK4fehk3uq1pgc8xasxs6s6xq6d`;
alter table `artifact1_volume` drop foreign key `FKe7h7plb2lwn3lw30yer7s49mp`;
alter table `artifact2_volume` drop foreign key `FKk1192bccv052dy1m1kmlacwnf`;
alter table `artifactclass_volume` drop foreign key `FKqusi30rwg38bm03d7sxk2t79t`;
alter table `file1_volume` drop foreign key `FK9atcbgsf7x40lcryf7ee6gnnf`;
alter table `file2_volume` drop foreign key `FK93nbo63ch4nudlvjicxva7tjb`;
alter table `job` drop foreign key `FKc1pbmvtiril369gwx8mhr1pnv`;
alter table `job` drop foreign key `FKjca24lxmd2ip2qo0yvtikpxgo`;
alter table `jobrun` drop foreign key `FKolnq6mkqede1xb94hj9y4i4dk`;
alter table `t_activedevice` drop foreign key `FKnt92eoeekg7yoelmtb9y6diq9`;
alter table `volume` drop foreign key `FK571cos3ontc2q3bc72h4ns8gp`;
alter table `artifactclass` drop foreign key `FKeynrnq0kfcuqn53tklcqexghk`;
alter table `sequence` drop foreign key `FK5v8kpxq28gqfbgrsmaermgfrc`;
alter table `volume` drop foreign key `FK94srhb48x080eknhc0yx0ad2o`;
alter table `artifactclass_destination` drop foreign key `FK90k9ovbcafjeemb32gwnr845u`;
alter table `copy` drop foreign key `FK7v9890w0s8toh9n4a8tlvh9l4`;
alter table `volume` drop foreign key `FK5k6g9ueuvb8e330dfvr88agfk`;
alter table `extension_filetype` drop foreign key `FK2tsonlt8ut5at1khmlxop9tog`;
alter table `extension_filetype` drop foreign key `FKisal8u7vwfumc2r09bdxekdw6`;
alter table `job` drop foreign key `FKb9ygn20vju9eu9pmrjxo3txwq`;
alter table `jobrun` drop foreign key `FKev07laqltq760kispd77it81e`;
alter table `t_activedevice` drop foreign key `FK7yrwas32jid6fmu1vdppn7t3t`;
alter table `volume` drop foreign key `FKsw7cga5kgm5yqs2sfpq9hdidv`;
alter table `volume` drop foreign key `FK8teoqqr29pkmx2kde364jhwms`;

-- drop unique indexes
alter table `location` drop index `UK_bvtps7leip9hi2pjp928b64bo`;

-- not possible to have 3072 byte index for pathname - use separate sha1 col instead
alter table `file1` drop index `UK_q47hfd5q1dwgsnr1bwhfsjagl`;
alter table `file2` drop index `UK_j3chk4j82n4f1fis7rtfxpuhn`;

alter table `action` modify `id` varbinary(255);
alter table `action` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `action` modify `description` varbinary(255);
alter table `action` modify `description` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `action` modify `type` varbinary(255);
alter table `action` modify `type` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `action_artifactclass_flow` modify `artifactclass_id` varbinary(255);
alter table `action_artifactclass_flow` modify `artifactclass_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `action_artifactclass_flow` modify `flow_id` varbinary(255);
alter table `action_artifactclass_flow` modify `flow_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `action_artifactclass_flow` modify `action_id` varbinary(255);
alter table `action_artifactclass_flow` modify `action_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `action_artifactclass_user` modify `action_id` varbinary(255);
alter table `action_artifactclass_user` modify `action_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `action_artifactclass_user` modify `artifactclass_id` varbinary(255);
alter table `action_artifactclass_user` modify `artifactclass_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `archiveformat` modify `id` varbinary(255);
alter table `archiveformat` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `archiveformat` modify `description` varbinary(255);
alter table `archiveformat` modify `description` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `artifact1` modify `file_structure_md5` varbinary(255);
alter table `artifact1` modify `file_structure_md5` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact1` modify `name` varbinary(255);
alter table `artifact1` modify `name` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact1` modify `prev_sequence_code` varbinary(255);
alter table `artifact1` modify `prev_sequence_code` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact1` modify `sequence_code` varbinary(255);
alter table `artifact1` modify `sequence_code` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact1` modify `artifactclass_id` varbinary(255);
alter table `artifact1` modify `artifactclass_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `artifact1_volume` modify `name` varbinary(255);
alter table `artifact1_volume` modify `name` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact1_volume` modify `volume_id` varbinary(255);
alter table `artifact1_volume` modify `volume_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `artifact2` modify `file_structure_md5` varbinary(255);
alter table `artifact2` modify `file_structure_md5` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact2` modify `name` varbinary(255);
alter table `artifact2` modify `name` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact2` modify `prev_sequence_code` varbinary(255);
alter table `artifact2` modify `prev_sequence_code` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact2` modify `sequence_code` varbinary(255);
alter table `artifact2` modify `sequence_code` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact2` modify `artifactclass_id` varbinary(255);
alter table `artifact2` modify `artifactclass_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `artifact2_volume` modify `name` varbinary(255);
alter table `artifact2_volume` modify `name` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifact2_volume` modify `volume_id` varbinary(255);
alter table `artifact2_volume` modify `volume_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `artifactclass` modify `id` varbinary(255);
alter table `artifactclass` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass` modify `description` varbinary(255);
alter table `artifactclass` modify `description` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass` modify `path_prefix` varbinary(255);
alter table `artifactclass` modify `path_prefix` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass` modify `artifactclass_ref_id` varbinary(255);
alter table `artifactclass` modify `artifactclass_ref_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass` modify `sequence_id` varbinary(255);
alter table `artifactclass` modify `sequence_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `artifactclass_destination` modify `artifactclass_id` varbinary(255);
alter table `artifactclass_destination` modify `artifactclass_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass_destination` modify `destination_id` varbinary(255);
alter table `artifactclass_destination` modify `destination_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `artifactclass_task` modify `processingtask_id` varbinary(255);
alter table `artifactclass_task` modify `processingtask_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass_task` modify `storagetask_action_id` varbinary(255);
alter table `artifactclass_task` modify `storagetask_action_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass_task` modify `artifactclass_id` varbinary(255);
alter table `artifactclass_task` modify `artifactclass_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `artifactclass_volume` modify `artifactclass_id` varbinary(255);
alter table `artifactclass_volume` modify `artifactclass_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `artifactclass_volume` modify `volume_id` varbinary(255);
alter table `artifactclass_volume` modify `volume_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `badfile` modify `reason` varbinary(255);
alter table `badfile` modify `reason` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `copy` modify `location_id` varbinary(255);
alter table `copy` modify `location_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `destination` modify `id` varbinary(255);
alter table `destination` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `destination` modify `path` varbinary(255);
alter table `destination` modify `path` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `device` modify `id` varbinary(255);
alter table `device` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `device` modify `manufacturer` varbinary(255);
alter table `device` modify `manufacturer` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `device` modify `model` varbinary(255);
alter table `device` modify `model` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `device` modify `serial_number` varbinary(255);
alter table `device` modify `serial_number` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `device` modify `status` varbinary(255);
alter table `device` modify `status` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `device` modify `type` varbinary(255);
alter table `device` modify `type` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `device` modify `wwn_id` varbinary(255);
alter table `device` modify `wwn_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `domain` modify `name` varbinary(255);
alter table `domain` modify `name` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `dwara_sequences` modify `primary_key_fields` varbinary(255);
alter table `dwara_sequences` modify `primary_key_fields` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `error` modify `description` varbinary(255);
alter table `error` modify `description` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `extension` modify `id` varbinary(255);
alter table `extension` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `extension` modify `description` varbinary(255);
alter table `extension` modify `description` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `extension_filetype` modify `extension_id` varbinary(255);
alter table `extension_filetype` modify `extension_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `extension_filetype` modify `filetype_id` varbinary(255);
alter table `extension_filetype` modify `filetype_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `file1` modify `pathname` varbinary(3072);
alter table `file1` modify `pathname` varchar(16384) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `file1_volume` modify `volume_id` varbinary(255);
alter table `file1_volume` modify `volume_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `file2` modify `pathname` varbinary(3072);
alter table `file2` modify `pathname` varchar(16384) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `file2_volume` modify `volume_id` varbinary(255);
alter table `file2_volume` modify `volume_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `filetype` modify `id` varbinary(255);
alter table `filetype` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `filetype` modify `description` varbinary(255);
alter table `filetype` modify `description` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `flow` modify `id` varbinary(255);
alter table `flow` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `flow` modify `description` varbinary(255);
alter table `flow` modify `description` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `flowelement` modify `id` varbinary(255);
alter table `flowelement` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `flowelement` modify `flow_id` varbinary(255);
alter table `flowelement` modify `flow_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `flowelement` modify `flow_ref_id` varbinary(255);
alter table `flowelement` modify `flow_ref_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `flowelement` modify `processingtask_id` varbinary(255);
alter table `flowelement` modify `processingtask_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `flowelement` modify `storagetask_action_id` varbinary(255);
alter table `flowelement` modify `storagetask_action_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `job` modify `message` longblob;
alter table `job` modify `message` longtext character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `job` modify `processingtask_id` varbinary(255);
alter table `job` modify `processingtask_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `job` modify `status` varbinary(255);
alter table `job` modify `status` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `job` modify `storagetask_action_id` varbinary(255);
alter table `job` modify `storagetask_action_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `job` modify `device_id` varbinary(255);
alter table `job` modify `device_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `job` modify `flowelement_id` varbinary(255);
alter table `job` modify `flowelement_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `job` modify `group_volume_id` varbinary(255);
alter table `job` modify `group_volume_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `job` modify `volume_id` varbinary(255);
alter table `job` modify `volume_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `jobrun` modify `message` longblob;
alter table `jobrun` modify `message` longtext character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `jobrun` modify `status` varbinary(255);
alter table `jobrun` modify `status` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `jobrun` modify `device_id` varbinary(255);
alter table `jobrun` modify `device_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `jobrun` modify `volume_id` varbinary(255);
alter table `jobrun` modify `volume_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `location` modify `id` varbinary(255);
alter table `location` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `location` modify `description` varbinary(255);
alter table `location` modify `description` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `priorityband` modify `name` varbinary(255);
alter table `priorityband` modify `name` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `processingfailure` modify `reason` longblob;
alter table `processingfailure` modify `reason` longtext character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `processingtask` modify `id` varbinary(255);
alter table `processingtask` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `processingtask` modify `description` varbinary(255);
alter table `processingtask` modify `description` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `processingtask` modify `filetype_id` varbinary(255);
alter table `processingtask` modify `filetype_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `processingtask` modify `output_artifactclass_suffix` varbinary(255);
alter table `processingtask` modify `output_artifactclass_suffix` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `processingtask` modify `output_filetype_id` varbinary(255);
alter table `processingtask` modify `output_filetype_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `request` modify `action_id` varbinary(255);
alter table `request` modify `action_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `request` modify `external_ref` varbinary(255);
alter table `request` modify `external_ref` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `request` modify `status` varbinary(255);
alter table `request` modify `status` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `request` modify `type` varbinary(255);
alter table `request` modify `type` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `sequence` modify `id` varbinary(255);
alter table `sequence` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `sequence` modify `code_regex` varbinary(255);
alter table `sequence` modify `code_regex` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `sequence` modify `number_regex` varbinary(255);
alter table `sequence` modify `number_regex` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `sequence` modify `prefix` varbinary(255);
alter table `sequence` modify `prefix` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `sequence` modify `type` varbinary(255);
alter table `sequence` modify `type` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `sequence` modify `sequence_ref_id` varbinary(255);
alter table `sequence` modify `sequence_ref_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `t_activedevice` modify `device_id` varbinary(255);
alter table `t_activedevice` modify `device_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `t_activedevice` modify `volume_id` varbinary(255);
alter table `t_activedevice` modify `volume_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `t_file_job` modify `status` varbinary(255);
alter table `t_file_job` modify `status` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `user` modify `email` varbinary(255);
alter table `user` modify `email` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `user` modify `hash` varbinary(255);
alter table `user` modify `hash` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `user` modify `name` varbinary(255);
alter table `user` modify `name` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `version` modify `version` varbinary(255);
alter table `version` modify `version` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

alter table `volume` modify `id` varbinary(255);
alter table `volume` modify `id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `volume` modify `checksumtype` varbinary(255);
alter table `volume` modify `checksumtype` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `volume` modify `storagelevel` varbinary(255);
alter table `volume` modify `storagelevel` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `volume` modify `storagesubtype` varbinary(255);
alter table `volume` modify `storagesubtype` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `volume` modify `storagetype` varbinary(255);
alter table `volume` modify `storagetype` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `volume` modify `type` varbinary(255);
alter table `volume` modify `type` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `volume` modify `uuid` varbinary(255);
alter table `volume` modify `uuid` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `volume` modify `archiveformat_id` varbinary(255);
alter table `volume` modify `archiveformat_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `volume` modify `group_ref_id` varbinary(255);
alter table `volume` modify `group_ref_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `volume` modify `location_id` varbinary(255);
alter table `volume` modify `location_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;
alter table `volume` modify `sequence_id` varbinary(255);
alter table `volume` modify `sequence_id` varchar(255) character set utf8mb4 collate utf8mb4_unicode_520_ci;

-- add foreign keys
alter table `action_artifactclass_flow` add foreign key (`action_id`) references `action`(`id`);
alter table `action_artifactclass_user` add foreign key (`action_id`) references `action`(`id`);
alter table `action_artifactclass_user` add foreign key (`artifactclass_id`) references `artifactclass`(`id`);
alter table `artifact1` add foreign key (`artifactclass_id`) references `artifactclass`(`id`);
alter table `artifact2` add foreign key (`artifactclass_id`) references `artifactclass`(`id`);
alter table `artifactclass` add foreign key (`artifactclass_ref_id`) references `artifactclass`(`id`);
alter table `artifactclass_destination` add foreign key (`artifactclass_id`) references `artifactclass`(`id`);
alter table `artifactclass_task` add foreign key (`artifactclass_id`) references `artifactclass`(`id`);
alter table `artifactclass_volume` add foreign key (`artifactclass_id`) references `artifactclass`(`id`);
alter table `artifact1_volume` add foreign key (`volume_id`) references `volume`(`id`);
alter table `artifact2_volume` add foreign key (`volume_id`) references `volume`(`id`);
alter table `artifactclass_volume` add foreign key (`volume_id`) references `volume`(`id`);
alter table `file1_volume` add foreign key (`volume_id`) references `volume`(`id`);
alter table `file2_volume` add foreign key (`volume_id`) references `volume`(`id`);
alter table `job` add foreign key (`group_volume_id`) references `volume`(`id`);
alter table `job` add foreign key (`volume_id`) references `volume`(`id`);
alter table `jobrun` add foreign key (`volume_id`) references `volume`(`id`);
alter table `t_activedevice` add foreign key (`volume_id`) references `volume`(`id`);
alter table `volume` add foreign key (`group_ref_id`) references `volume`(`id`);
alter table `artifactclass` add foreign key (`sequence_id`) references `sequence`(`id`);
alter table `sequence` add foreign key (`sequence_ref_id`) references `sequence`(`id`);
alter table `volume` add foreign key (`sequence_id`) references `sequence`(`id`);
alter table `artifactclass_destination` add foreign key (`destination_id`) references `destination`(`id`);
alter table `copy` add foreign key (`location_id`) references `location`(`id`);
alter table `volume` add foreign key (`location_id`) references `location`(`id`);
alter table `extension_filetype` add foreign key (`extension_id`) references `extension`(`id`);
alter table `extension_filetype` add foreign key (`filetype_id`) references `filetype`(`id`);
alter table `job` add foreign key (`device_id`) references `device`(`id`);
alter table `jobrun` add foreign key (`device_id`) references `device`(`id`);
alter table `t_activedevice` add foreign key (`device_id`) references `device`(`id`);
alter table `volume` add foreign key (`archiveformat_id`) references `archiveformat`(`id`);
alter table `volume` add foreign key (`copy_id`) references `copy`(`id`);

set foreign_key_checks = 1; 