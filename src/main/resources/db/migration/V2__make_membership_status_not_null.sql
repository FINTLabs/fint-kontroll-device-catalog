UPDATE device_group_memberships
SET membership_status = 'ACTIVE'
WHERE membership_status IS NULL;

UPDATE device_group_memberships
SET membership_status_changed = CURRENT_TIMESTAMP
WHERE membership_status_changed IS NULL;

ALTER TABLE device_group_memberships
    ALTER COLUMN membership_status SET NOT NULL,
    ALTER COLUMN membership_status_changed SET NOT NULL;
