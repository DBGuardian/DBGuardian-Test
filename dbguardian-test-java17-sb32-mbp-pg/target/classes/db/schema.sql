DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    status SMALLINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_t_user_username UNIQUE (username)
);

CREATE TABLE t_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_t_order_order_no UNIQUE (order_no),
    CONSTRAINT fk_t_order_user FOREIGN KEY (user_id) REFERENCES t_user(id)
);

CREATE INDEX idx_t_order_user_id ON t_order(user_id);

INSERT INTO t_user (username, email, phone, status) VALUES
('admin', 'admin@test.com', '13800138000', 1),
('user1', 'user1@test.com', '13800138001', 1),
('user2', 'user2@test.com', '13800138002', 1)
ON CONFLICT (username) DO NOTHING;

INSERT INTO t_order (order_no, user_id, amount, status) VALUES
('ORD20260509001', 1, 100.00, 'COMPLETED'),
('ORD20260509002', 1, 200.00, 'PAID'),
('ORD20260509003', 2, 150.00, 'PENDING')
ON CONFLICT (order_no) DO NOTHING;

-- 主库：SELECT pg_current_wal_lsn();
-- 从库：SELECT pg_is_in_recovery(), pg_last_wal_receive_lsn(), pg_last_wal_replay_lsn();