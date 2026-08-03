-- ===============================================
-- DBGuardian PostgreSQL MyBatis 读写分离测试数据库初始化脚本
-- 需要分别在主库和从库的目标数据库中执行
-- ===============================================

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

COMMENT ON TABLE t_user IS '用户表';
COMMENT ON COLUMN t_user.id IS '用户ID';
COMMENT ON COLUMN t_user.username IS '用户名';
COMMENT ON COLUMN t_user.email IS '邮箱';
COMMENT ON COLUMN t_user.phone IS '手机号';
COMMENT ON COLUMN t_user.status IS '状态：0-禁用，1-正常';
COMMENT ON COLUMN t_user.create_time IS '创建时间';
COMMENT ON COLUMN t_user.update_time IS '更新时间';

INSERT INTO t_user (username, email, phone, status) VALUES
('admin', 'admin@test.com', '13800138000', 1),
('user1', 'user1@test.com', '13800138001', 1),
('user2', 'user2@test.com', '13800138002', 1)
ON CONFLICT (username) DO NOTHING;

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

COMMENT ON TABLE t_order IS '订单表';
COMMENT ON COLUMN t_order.id IS '订单ID';
COMMENT ON COLUMN t_order.order_no IS '订单号';
COMMENT ON COLUMN t_order.user_id IS '用户ID';
COMMENT ON COLUMN t_order.amount IS '订单金额';
COMMENT ON COLUMN t_order.status IS '状态：PENDING-待处理，PAID-已支付，COMPLETED-已完成，CANCELLED-已取消';
COMMENT ON COLUMN t_order.create_time IS '创建时间';
COMMENT ON COLUMN t_order.update_time IS '更新时间';

INSERT INTO t_order (order_no, user_id, amount, status) VALUES
('ORD20260509001', 1, 100.00, 'COMPLETED'),
('ORD20260509002', 1, 200.00, 'PAID'),
('ORD20260509003', 2, 150.00, 'PENDING')
ON CONFLICT (order_no) DO NOTHING;

-- PostgreSQL 复制状态观测 SQL：
-- 主库：SELECT pg_current_wal_lsn();
-- 从库：SELECT pg_is_in_recovery(), pg_last_wal_receive_lsn(), pg_last_wal_replay_lsn();