package com.dbguardian.test.repository;

import java.util.List;
import java.util.Optional;

import com.dbguardian.test.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    Optional<Order> findByOrderNo(String orderNo);
}
