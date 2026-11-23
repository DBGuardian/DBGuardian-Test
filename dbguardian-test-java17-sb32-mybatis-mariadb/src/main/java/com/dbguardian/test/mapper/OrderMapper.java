package com.dbguardian.test.mapper;

import com.dbguardian.test.entity.Order;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

public interface OrderMapper {

    String SELECT_ORDER = "SELECT id, order_no, user_id, amount, status, create_time, update_time FROM t_order";

    @Select(SELECT_ORDER + " WHERE id = #{id}")
    Optional<Order> selectById(@Param("id") Long id);

    @Select(SELECT_ORDER + " WHERE order_no = #{orderNo}")
    Optional<Order> selectByOrderNo(@Param("orderNo") String orderNo);

    @Select(SELECT_ORDER + " WHERE user_id = #{userId} ORDER BY id DESC")
    List<Order> selectByUserId(@Param("userId") Long userId);

    @Select(SELECT_ORDER + " ORDER BY id DESC")
    List<Order> selectAll();

    @Insert("INSERT INTO t_order (order_no, user_id, amount, status) VALUES (#{orderNo}, #{userId}, #{amount}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Order order);

    @Update("UPDATE t_order SET order_no = #{orderNo}, user_id = #{userId}, amount = #{amount}, "
            + "status = #{status}, update_time = CURRENT_TIMESTAMP(6) WHERE id = #{id}")
    int update(Order order);

    @Delete("DELETE FROM t_order WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
