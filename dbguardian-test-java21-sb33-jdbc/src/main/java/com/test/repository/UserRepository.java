package com.test.repository;

import com.test.model.User;
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
public class UserRepository {
    private static final String COLUMNS = "id, username, email, phone, status, create_time, update_time";
    private static final RowMapper<User> MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setStatus(rs.getInt("status"));
        user.setCreateTime(toLocalDateTime(rs.getTimestamp("create_time")));
        user.setUpdateTime(toLocalDateTime(rs.getTimestamp("update_time")));
        return user;
    };

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    public UserRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
        this.jdbc = jdbc;
        this.namedJdbc = namedJdbc;
    }

    public User save(User user) {
        String sql = "INSERT INTO t_user (username, email, phone, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setInt(4, user.getStatus());
            statement.setTimestamp(5, Timestamp.valueOf(user.getCreateTime()));
            statement.setTimestamp(6, Timestamp.valueOf(user.getUpdateTime()));
            return statement;
        }, keys);
        if (keys.getKey() != null) user.setId(keys.getKey().longValue());
        return user;
    }

    public Optional<User> findById(long id) {
        return namedJdbc.query("SELECT " + COLUMNS + " FROM t_user WHERE id = :id",
                new MapSqlParameterSource("id", id), MAPPER).stream().findFirst();
    }

    public List<User> findAll() {
        return namedJdbc.query("SELECT " + COLUMNS + " FROM t_user ORDER BY id", new MapSqlParameterSource(), MAPPER);
    }

    public boolean update(long id, User user) {
        return jdbc.update("UPDATE t_user SET username = ?, email = ?, phone = ?, status = ?, update_time = ? WHERE id = ?",
                user.getUsername(), user.getEmail(), user.getPhone(), user.getStatus(),
                Timestamp.valueOf(user.getUpdateTime()), id) == 1;
    }

    public boolean deleteById(long id) { return jdbc.update("DELETE FROM t_user WHERE id = ?", id) == 1; }

    private static LocalDateTime toLocalDateTime(Timestamp value) { return value == null ? null : value.toLocalDateTime(); }
}