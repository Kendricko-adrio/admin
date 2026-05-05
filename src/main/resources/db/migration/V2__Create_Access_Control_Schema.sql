-- =============================================
-- V2: Create Access Control Schema
-- Electronic Trading Admin Module
-- =============================================

-- 1. Access Rights
CREATE TABLE access_rights (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(100) NOT NULL,
    description VARCHAR(255),

    CONSTRAINT uq_access_rights_code UNIQUE (code)
);

CREATE INDEX idx_access_rights_code ON access_rights (code);

-- 2. Groups
CREATE TABLE groups (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_groups_name UNIQUE (name)
);

-- 3. Junction: Group ↔ Access Right (many-to-many)
CREATE TABLE group_access_rights (
    group_id        BIGINT NOT NULL,
    access_right_id BIGINT NOT NULL,

    PRIMARY KEY (group_id, access_right_id),
    CONSTRAINT fk_gar_group        FOREIGN KEY (group_id)        REFERENCES groups (id)         ON DELETE CASCADE,
    CONSTRAINT fk_gar_access_right FOREIGN KEY (access_right_id) REFERENCES access_rights (id)  ON DELETE CASCADE
);

-- 4. Junction: User ↔ Group (many-to-many)
CREATE TABLE user_groups (
    user_id  BIGINT NOT NULL,
    group_id BIGINT NOT NULL,

    PRIMARY KEY (user_id, group_id),
    CONSTRAINT fk_ug_user  FOREIGN KEY (user_id)  REFERENCES users (id)  ON DELETE CASCADE,
    CONSTRAINT fk_ug_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE
);
