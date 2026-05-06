-- =============================================
-- V3: Add Hierarchy And Seed Access Rights
-- Electronic Trading Admin Module
-- =============================================

ALTER TABLE access_rights
    ADD COLUMN parent_code VARCHAR(100),
    ADD COLUMN category    VARCHAR(50);

ALTER TABLE access_rights
    ADD CONSTRAINT fk_access_rights_parent_code
        FOREIGN KEY (parent_code) REFERENCES access_rights (code);

CREATE INDEX idx_access_rights_category    ON access_rights (category);
CREATE INDEX idx_access_rights_parent_code ON access_rights (parent_code);

-- Seed data for all 15 access rights (parents must be inserted before children)
INSERT INTO access_rights (code, description, parent_code, category) VALUES
    -- CLIENT hierarchy
    ('CLIENT',                          'Client role root',    NULL,                     'CLIENT'),
    ('CLIENT.ORDER',                    'Order',               'CLIENT',                 'CLIENT'),
    ('CLIENT.ORDER.SUBMIT',             'Submit',              'CLIENT.ORDER',           'CLIENT'),
    ('CLIENT.ORDER_LOG',                'Order Log',           'CLIENT',                 'CLIENT'),
    ('CLIENT.ORDER_LOG.VIEW',           'View',                'CLIENT.ORDER_LOG',       'CLIENT'),
    -- TRADER hierarchy
    ('TRADER',                          'Trader role root',    NULL,                     'TRADER'),
    ('TRADER.ORDER',                    'Order',               'TRADER',                 'TRADER'),
    ('TRADER.ORDER.PICK_UP',            'Pick Up',             'TRADER.ORDER',           'TRADER'),
    -- ADMIN hierarchy
    ('ADMIN',                           'Admin role root',     NULL,                     'ADMIN'),
    ('ADMIN.USER',                      'User',                'ADMIN',                  'ADMIN'),
    ('ADMIN.USER.VIEW',                 'View',                'ADMIN.USER',             'ADMIN'),
    ('ADMIN.USER.CREATE_UPDATE_DELETE', 'Create/Update/Delete','ADMIN.USER',             'ADMIN'),
    ('ADMIN.ACCESS_RIGHT',              'Access Right',        'ADMIN',                  'ADMIN'),
    ('ADMIN.ACCESS_RIGHT.VIEW',         'View',                'ADMIN.ACCESS_RIGHT',     'ADMIN'),
    ('ADMIN.ACCESS_RIGHT.CREATE_UPDATE_DELETE', 'Create/Update/Delete', 'ADMIN.ACCESS_RIGHT', 'ADMIN')
ON CONFLICT (code) DO NOTHING;
