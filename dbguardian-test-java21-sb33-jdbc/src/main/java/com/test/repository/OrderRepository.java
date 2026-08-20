package com.test.repository;

import com.test.model.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {
    private static final String COLUMNS = "id, order_no, user_id, amount, status, create_time, update_time";
    private static final RowMapper<Order> MAPPER = (rs, rowNum) -> {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setOrderNo(rs.getString("order_no"));
        order.setUserId(rs.getLong("user_id"));
        order.setAmount(rs.getBigDecimal("amount"));
        order.setStatus(rs.getString("status"));
        order.setCreateTime(toLocalDateTime(rs.getTimestamp("create_time")));
        order.setUpdateTime(toLocalDateTime(rs.getTimestamp("update_time")));
        return order;
    };

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    public OrderRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
        this.jdbc = jdbc;
        this.namedJdbc = namedJdbc;
    }

    public Order save(Order order) {
        String sql = "INSERT INTO t_order (order_no, user_id, amount, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, order.getOrderNo());
            statement.setLong(2, order.getUserId());
            statement.setBigDecimal(3, order.getAmount());
            statement.setString(4, order.getStatus());
            statement.setTimestamp(5, Timestamp.valueOf(order.getCreateTime()));
            statement.setTimestamp(6, Timestamp.valueOf(order.getUpdateTime()));
            return statement;
        }, keys);
        if (keys.getKey() != null) order.setId(keys.getKey().longValue());
        return order;
    }

    public Optional<Order> findById(long id) {
        return namedJdbc.query("SELECT " + COLUMNS + " FROM t_order WHERE id = :id",
                new MapSqlParameterSource("id", id), MAPPER).stream().findFirst();
    }

    public List<Order> findAll() {
        return namedJdbc.query("SELECT " + COLUMNS + " FROM t_order ORDER BY id", new MapSqlParameterSource(), MAPPER);
    }

    public List<Order> findByUserId(long userId) {
        return namedJdbc.query("SELECT " + COLUMNS + " FROM t_order WHERE user_id = :userId ORDER BY id",
                new MapSqlParameterSource("userId", userId), MAPPER);
    }

    public boolean update(long id, Order order) {
        return jdbc.update("UPDATE t_order SET order_no = ?, user_id = ?, amount = ?, status = ?, update_time = ? WHERE id = ?",
                order.getOrderNo(), order.getUserId(), order.getAmount(), order.getStatus(),
                Timestamp.valueOf(order.getUpdateTime()), id) == 1;
    }

    public boolean deleteById(long id) { return jdbc.update("DELETE FROM t_order WHERE id = ?", id) == 1; }

    private static LocalDateTime toLocalDateTime(Timestamp value) { return value == null ? null : value.toLocalDateTime(); }
}