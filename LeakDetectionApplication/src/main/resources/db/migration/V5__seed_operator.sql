-- Seed default operator account (password: Admin@1234)
INSERT INTO users (first_name, last_name, username, password, role)
VALUES ('Darlene', 'Wendy', 'darlene', '$2b$10$OJs5gV1lvICbbgVlStnZ.O.ienV05dNd0oz80.DrUYTvZouQe/tyS', 'ROLE_OPERATOR')
    ON CONFLICT (username) DO NOTHING;