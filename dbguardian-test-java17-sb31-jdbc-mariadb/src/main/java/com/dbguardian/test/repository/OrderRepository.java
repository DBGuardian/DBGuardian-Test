package com.dbguardian.test.repository;

import com.dbguardian.test.entity.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private static final String SELECT = "SELECT id, order_no, user_id, amount, status, create_time, update_time FROM t_order";
    private static final RowMapper<Order> ROW_MAPPER = (rs, rowNum) -> {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setOrderNo(rs.getString("order_no"));
        order.setUserId(rs.getObject("user_id", Long.class));
        order.setAmount(rs.getBigDecimal("amount"));
        order.setStatus(rs.getString("status"));
        order.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
        order.setUpdateTime(rs.getObject("update_time", LocalDateTime.class));
        return order;
    };

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate queries;

    public OrderRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    public long insert(Order order) {
        String sql = "INSERT INTO t_order (order_no, user_id, amount, status) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, order.getOrderNo());
            statement.setObject(2, order.getUserId());
            statement.setBigDecimal(3, order.getAmount());
            statement.setString(4, order.getStatus());
            return statement;
        }, keys);
        Number key = keys.getKey();
        if (key == null) {
            throw new IllegalStateException("Insert did not return an order ID");
        }
        return key.longValue();
    }

    public Optional<Order> findById(long id) {
        return queries.query(SELECT + " WHERE id = :id", new MapSqlParameterSource("id", id), ROW_MAPPER)
                .stream().findFirst();
    }

    public Optional<Order> findByOrderNo(String orderNo) {
        return queries.query(SELECT + " WHERE order_no = :orderNo",
                new MapSqlParameterSource("orderNo", orderNo), ROW_MAPPER).stream().findFirst();
    }

    public List<Order> findByUserId(long userId) {
        return queries.query(SELECT + " WHERE user_id = :userId ORDER BY id DESC",
                new MapSqlParameterSource("userId", userId), ROW_MAPPER);
    }

    public List<Order> findAll() {
        return queries.query(SELECT + " ORDER BY id DESC", new MapSqlParameterSource(), ROW_MAPPER);
    }

    public void update(long id, Order order) {
        jdbc.update("UPDATE t_order SET order_no = ?, user_id = ?, amount = ?, status = ?, "
                        + "update_time = CURRENT_TIMESTAMP(6) WHERE id = ?",
                order.getOrderNo(), order.getUserId(), order.getAmount(), order.getStatus(), id);
    }

    public boolean delete(long id) {
        return jdbc.update("DELETE FROM t_order WHERE id = ?", id) != 0;
    }
}
