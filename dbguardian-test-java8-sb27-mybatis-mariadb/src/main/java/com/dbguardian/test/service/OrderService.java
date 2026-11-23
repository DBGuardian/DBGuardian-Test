package com.dbguardian.test.service;

import com.dbguardian.test.entity.Order;
import com.dbguardian.test.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderMapper orders;

    public OrderService(OrderMapper orders) {
        this.orders = orders;
    }

    @Transactional
    public Order create(Order order) {
        orders.insert(order);
        return orders.selectById(order.getId()).orElseThrow(IllegalStateException::new);
    }

    public List<Order> listOrders() {
        return orders.selectAll();
    }

    public Optional<Order> findById(Long id) {
        return orders.selectById(id);
    }

    public List<Order> findByUserId(Long userId) {
        return orders.selectByUserId(userId);
    }

    public Optional<Order> findByOrderNo(String orderNo) {
        return orders.selectByOrderNo(orderNo);
    }

    @Transactional
    public Optional<Order> findByIdFromMaster(Long id) {
        return orders.selectById(id);
    }

    @Transactional
    public Optional<Order> update(Long id, Order order) {
        order.setId(id);
        orders.update(order);
        return orders.selectById(id);
    }

    @Transactional
    public boolean delete(Long id) {
        return orders.deleteById(id) != 0;
    }
}
