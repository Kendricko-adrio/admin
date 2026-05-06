-- =============================================
-- V3: Add Parent Code, Category And Seed Access Rights
-- Electronic Trading Admin Module
-- =============================================

ALTER TABLE access_rights
    ADD COLUMN parent_code VARCHAR(100),
    ADD COLUMN category    VARCHAR(50);

CREATE INDEX idx_access_rights_category    ON access_rights (category);
CREATE INDEX idx_access_rights_parent_code ON access_rights (parent_code);

-- Flat seed data — 7 leaf rows only (no parent nodes, parent_code is just a label)
INSERT INTO access_rights (code, description, parent_code, category) VALUES
    ('ORDER_SUBMIT',                      'Client submit',       'CLIENT', 'ORDER'),
    ('ORDER_LOG_VIEW',                    'View',                'CLIENT', 'ORDER_LOG'),
    ('ORDER_PICK_UP',                     'Pick Up',             'TRADER', 'ORDER'),
    ('USER_VIEW',                         'View',                'ADMIN',  'USER'),
    ('USER_CREATE_UPDATE_DELETE',         'Create/Update/Delete','ADMIN',  'USER'),
    ('ACCESS_RIGHT_VIEW',                 'View',                'ADMIN',  'ACCESS_RIGHT'),
    ('ACCESS_RIGHT_CREATE_UPDATE_DELETE', 'Create/Update/Delete','ADMIN',  'ACCESS_RIGHT')
ON CONFLICT (code) DO NOTHING;
