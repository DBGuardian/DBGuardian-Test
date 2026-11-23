package com.dbguardian.test.repository;

import com.dbguardian.test.entity.User;
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
public class UserRepository {

    private static final String SELECT = "SELECT id, username, email, phone, status, create_time, update_time FROM t_user";
    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setStatus(rs.getInt("status"));
        user.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
        user.setUpdateTime(rs.getObject("update_time", LocalDateTime.class));
        return user;
    };

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate queries;

    public UserRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    public long insert(User user) {
        String sql = "INSERT INTO t_user (username, email, phone, status) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setObject(4, user.getStatus());
            return statement;
        }, keys);
        Number key = keys.getKey();
        if (key == null) {
            throw new IllegalStateException("Insert did not return a user ID");
        }
        return key.longValue();
    }

    public Optional<User> findById(long id) {
        return queries.query(SELECT + " WHERE id = :id", new MapSqlParameterSource("id", id), ROW_MAPPER)
                .stream().findFirst();
    }

    public Optional<User> findByUsername(String username) {
        return queries.query(SELECT + " WHERE username = :username",
                new MapSqlParameterSource("username", username), ROW_MAPPER).stream().findFirst();
    }

    public List<User> findAll() {
        return queries.query(SELECT + " ORDER BY id DESC", new MapSqlParameterSource(), ROW_MAPPER);
    }

    public void update(long id, User user) {
        jdbc.update("UPDATE t_user SET username = ?, email = ?, phone = ?, status = ?, "
                        + "update_time = CURRENT_TIMESTAMP(6) WHERE id = ?",
                user.getUsername(), user.getEmail(), user.getPhone(), user.getStatus(), id);
    }

    public boolean delete(long id) {
        return jdbc.update("DELETE FROM t_user WHERE id = ?", id) != 0;
    }

    public long serverId() {
        return queries.queryForObject("SELECT @@GLOBAL.server_id", new MapSqlParameterSource(), Long.class);
    }
}
