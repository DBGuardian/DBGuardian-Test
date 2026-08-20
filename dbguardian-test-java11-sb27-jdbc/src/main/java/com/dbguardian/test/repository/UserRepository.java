package com.dbguardian.test.repository;

import com.dbguardian.test.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final String COLUMNS =
            "id, username, email, phone, status, create_time, update_time";

    private static final RowMapper<User> USER_ROW_MAPPER = (resultSet, rowNum) -> {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setStatus(resultSet.getInt("status"));
        user.setCreateTime(toLocalDateTime(resultSet.getTimestamp("create_time")));
        user.setUpdateTime(toLocalDateTime(resultSet.getTimestamp("update_time")));
        return user;
    };

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate,
                          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public User save(User user) {
        String sql = "INSERT INTO t_user "
                + "(username, email, phone, status, create_time, update_time) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setInt(4, user.getStatus());
            statement.setTimestamp(5, Timestamp.valueOf(user.getCreateTime()));
            statement.setTimestamp(6, Timestamp.valueOf(user.getUpdateTime()));
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            user.setId(key.longValue());
        }
        return user;
    }

    public Optional<User> findById(long id) {
        String sql = "SELECT " + COLUMNS + " FROM t_user WHERE id = :id";
        List<User> users = namedParameterJdbcTemplate.query(
                sql,
                new MapSqlParameterSource("id", id),
                USER_ROW_MAPPER);
        return users.stream().findFirst();
    }

    public List<User> findAll() {
        return namedParameterJdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM t_user ORDER BY id",
                new MapSqlParameterSource(),
                USER_ROW_MAPPER);
    }

    private static LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }
}