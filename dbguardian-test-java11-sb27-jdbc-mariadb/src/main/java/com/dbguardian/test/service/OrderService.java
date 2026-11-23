package com.dbguardian.test.service;

import com.dbguardian.test.entity.Order;
import com.dbguardian.test.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional
    public Order create(Order order) {
        long id = orders.insert(order);
        return orders.findById(id).orElseThrow(() -> new IllegalStateException("Inserted order not found: " + id));
    }

    public List<Order> listOrders() {
        return orders.findAll();
    }

    public Optional<Order> findById(Long id) {
        return orders.findById(id);
    }

    public List<Order> findByUserId(Long userId) {
        return orders.findByUserId(userId);
    }

    public Optional<Order> findByOrderNo(String orderNo) {
        return orders.findByOrderNo(orderNo);
    }

    @Transactional
    public Optional<Order> findByIdFromMaster(Long id) {
        return orders.findById(id);
    }

    @Transactional
    public Optional<Order> update(Long id, Order order) {
        orders.update(id, order);
        return orders.findById(id);
    }

    @Transactional
    public boolean delete(Long id) {
        return orders.delete(id);
    }
}
