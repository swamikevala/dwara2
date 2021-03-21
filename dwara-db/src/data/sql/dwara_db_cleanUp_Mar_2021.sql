-- Edited failures
UPDATE `request` SET `status`='failed' WHERE `id`='28880';
UPDATE `request` SET `status`='failed', `completed_at`='2021-01-21 12:52:39.435000' WHERE `id`='7588';
UPDATE `request` SET `status`='failed' WHERE `id`='7587';
UPDATE `request` SET `status`='failed' WHERE `id`='7586';

-- Edited priv2 failures

UPDATE `request` SET `status`='failed' WHERE `id`='24023';
UPDATE `request` SET `status`='failed' WHERE `id`='24024';

UPDATE `request` SET `status`='cancelled' WHERE action_id='finalize' and status='failed' and (json_extract(details, '$.volume_id') != 'E10003L7' && json_extract(details, '$.volume_id') != 'E30003L7');

