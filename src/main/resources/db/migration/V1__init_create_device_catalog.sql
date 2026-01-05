-- DEVICE
CREATE TABLE IF NOT EXISTS devices (
                                      id                       BIGSERIAL PRIMARY KEY,
                                      source_id                VARCHAR(255) NOT NULL,
                                      serial_number            VARCHAR(255) NOT NULL,
                                      data_object_id           VARCHAR(255),
                                      name                     VARCHAR(255),
                                      is_private_property      BOOLEAN,
                                      is_shared                BOOLEAN,
                                      status                   VARCHAR(50),
                                      status_changed           TIMESTAMP,
                                      device_type              VARCHAR(100) NOT NULL,
                                      platform                 VARCHAR(100) NOT NULL,
                                      administrator_org_unit_id VARCHAR(255),
                                      owner_org_unit_id         VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS ix_device_serial_number ON devices(serial_number);
CREATE INDEX IF NOT EXISTS ix_device_status ON devices(status);

-- DEVICE_GROUPS
CREATE TABLE IF NOT EXISTS device_groups (
                                             id          bigserial PRIMARY KEY,
                                             source_id   VARCHAR(255) NOT NULL,
                                             name        VARCHAR(255) NOT NULL,
                                             org_unit_id VARCHAR(255),
                                             platform    VARCHAR(100) NOT NULL,
                                             device_type VARCHAR(100) NOT NULL,
                                             no_of_members BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_device_groups_source_id ON device_groups(source_id);

-- DEVICE_GROUP_MEMBERSHIP
CREATE TABLE IF NOT EXISTS device_group_memberships (
                                                       device_group_id bigint not null,
                                                       device_id bigint not null,
                                                       membership_status            VARCHAR(50),
                                                       membership_status_changed    TIMESTAMP,
                                                       primary key (device_group_id, device_id),

                                                       CONSTRAINT fk_dgm_device
                                                           FOREIGN KEY (device_id) REFERENCES devices(id),

                                                       CONSTRAINT fk_dgm_device_group
                                                           FOREIGN KEY (device_group_id) REFERENCES device_groups(id)
);

CREATE INDEX IF NOT EXISTS ix_dgm_device_id ON device_group_memberships(device_id);
CREATE INDEX IF NOT EXISTS ix_dgm_device_group_id ON device_group_memberships(device_group_id);