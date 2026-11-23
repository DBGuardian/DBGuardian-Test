package com.dbguardian.test.service;

import java.util.List;
import java.util.Optional;

import com.dbguardian.test.entity.Order;
import com.dbguardian.test.repository.OrderRepository;
import io.dbguardian.annotation.ForceMaster;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional
    public Order create(Order order) {
        return orders.save(order);
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

    @ForceMaster
    @Transactional
    public Optional<Order> findByIdFromMaster(Long id) {
        return orders.findById(id);
    }

    @Transactional
    public Optional<Order> update(Long id, Order input) {
        return orders.findById(id).map(order -> {
            order.setOrderNo(input.getOrderNo());
            order.setUserId(input.getUserId());
            order.setAmount(input.getAmount());
            order.setStatus(input.getStatus());
            return order;
        });
    }

    @Transactional
    public boolean delete(Long id) {
        return orders.findById(id).map(order -> {
            orders.delete(order);
            return true;
        }).orElse(false);
    }
}
