-- Replace device table entries with VTL drives
TRUNCATE TABLE device;

INSERT INTO device (id, `type`, wwn_id, `status`, defective, serial_number, warranty_expiry_date, manufacturer, model, details) VALUES ('vtl', 'tape_autoloader', '/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400', 'online', 0, 'DE68102376', '2022-03-30', 'Overland Tandberg', 'Neo XL80', '{"slots": 80, "max_drives": 6, "generations_supported": [6, 7]}');
INSERT INTO device (id, `type`, wwn_id, `status`, defective, serial_number, warranty_expiry_date, manufacturer, model, details) VALUES ('lto5-1', 'tape_drive', '/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst', 'online', 0, 'YR10WT083802', '2021-03-28', 'IBM', 'Ultrium HH5', '{"type": "LTO-5", "standalone": false, "autoloader_id": "vtl", "autoloader_address": 0}');
INSERT INTO device (id, `type`, wwn_id, `status`, defective, serial_number, warranty_expiry_date, manufacturer, model, details) VALUES ('lto5-2', 'tape_drive', '/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst', 'online', 0, 'YX1097011322', '2023-07-12', 'IBM', 'Ultrium HH5', '{"type": "LTO-5", "standalone": false, "autoloader_id": "vtl", "autoloader_address": 1}');
INSERT INTO device (id, `type`, wwn_id, `status`, defective, serial_number, warranty_expiry_date, manufacturer, model, details) VALUES ('lto5-3', 'tape_drive', '/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1970448833-nst', 'online', 0, 'YX10WT134623', '2023-07-12', 'IBM', 'Ultrium HH5', '{"type": "LTO-5", "standalone": false, "autoloader_id": "vtl", "autoloader_address": 2}');
INSERT INTO device (id, `type`, wwn_id, `status`, defective, serial_number, warranty_expiry_date, manufacturer, model, details) VALUES ('lto5-4', 'tape_drive', '/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1468597605-nst', 'online', 0, 'YX10WT103623', '2022-04-15', 'IBM', 'Ultrium HH5', '{"type": "LTO-5", "standalone": false, "autoloader_id": "vtl", "autoloader_address": 3}');

-- Mark all existing volumes as finalized
UPDATE `volume` SET `finalized` = 1 WHERE `type` = 'physical';

-- Query to get volume ids of next tapes in all groups (to generate VTL tapes)
/*
SELECT 1, group_concat(concat(substring(x.`id`, 1, 1), substring(x.`id`, 2, 5) + 1) separator ' ') 
FROM (select max(`id`) as id from `volume` where `type` = 'physical' group by `group_ref_id`) x;
*/