package com.test.service;

import com.test.model.Order;
import com.test.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) { this.repository = repository; }

    public Order saveOrder(Order order) {
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(order.getStatus() == null ? "PENDING" : order.getStatus());
        order.setCreateTime(order.getCreateTime() == null ? now : order.getCreateTime());
        order.setUpdateTime(now);
        return repository.save(order);
    }

    public Order getOrderById(Long id) { return repository.findById(id).orElse(null); }
    public List<Order> getOrdersByUserId(Long userId) { return repository.findByUserId(userId); }
    public List<Order> list() { return repository.findAll(); }

    public boolean updateById(Order order) {
        order.setStatus(order.getStatus() == null ? "PENDING" : order.getStatus());
        order.setUpdateTime(LocalDateTime.now());
        return repository.update(order.getId(), order);
    }

    public boolean removeById(Long id) { return repository.deleteById(id); }
}