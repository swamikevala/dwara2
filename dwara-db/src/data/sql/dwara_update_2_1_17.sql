-- *** Already done - start ***

-- mbuffer turned off temporarily to investigate and fix longsized restores
UPDATE `destination` SET `use_buffering`=0 WHERE `id`='san-video';
UPDATE `destination` SET `use_buffering`=0 WHERE `id`='san-video1';

-- *** Already done - end ***
