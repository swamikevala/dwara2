use `dwara`;
UPDATE `file1_volume` SET `header_blocks`='3' WHERE `header_blocks`=null and (`volume_id` like 'R1%' or `volume_id` like 'R3%' or `volume_id` like 'G1%');