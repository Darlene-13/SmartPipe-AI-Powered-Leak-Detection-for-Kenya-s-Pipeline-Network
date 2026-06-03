-- Seed default operator account (password: Admin@1234)
INSERT INTO users (first_name, last_name, username, password, role)
VALUES (
    'Darlene',
    'Wendy',
    'darlene',
    '$2a$12$jH0Ldoz4Ss2/Ras9IbqpCekL2Cjr/TnQ1FEKDSe/rlee8bIHdiQNK',
    'ROLE_OPERATOR'
)
ON CONFLICT (username) DO NOTHING;