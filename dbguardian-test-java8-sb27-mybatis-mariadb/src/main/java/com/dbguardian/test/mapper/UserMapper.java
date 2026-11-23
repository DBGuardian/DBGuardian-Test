package com.dbguardian.test.mapper;

import com.dbguardian.test.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

public interface UserMapper {

    String SELECT_USER = "SELECT id, username, email, phone, status, create_time, update_time FROM t_user";

    @Select(SELECT_USER + " WHERE id = #{id}")
    Optional<User> selectById(@Param("id") Long id);

    @Select(SELECT_USER + " WHERE username = #{username}")
    Optional<User> selectByUsername(@Param("username") String username);

    @Select(SELECT_USER + " ORDER BY id DESC")
    List<User> selectAll();

    @Insert("INSERT INTO t_user (username, email, phone, status) VALUES (#{username}, #{email}, #{phone}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(User user);

    @Update("UPDATE t_user SET username = #{username}, email = #{email}, phone = #{phone}, "
            + "status = #{status}, update_time = CURRENT_TIMESTAMP(6) WHERE id = #{id}")
    int update(User user);

    @Delete("DELETE FROM t_user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT @@GLOBAL.server_id")
    long selectServerId();
}
