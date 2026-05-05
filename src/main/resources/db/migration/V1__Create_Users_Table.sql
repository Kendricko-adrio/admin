-- =============================================
-- V1: Create Users Table
-- Electronic Trading Admin Module
-- =============================================

CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL,
    first_name VARCHAR(100),
    last_name  VARCHAR(100),
    email      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email)
);

-- Indexes for fast lookups on login / search
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email    ON users (email);
