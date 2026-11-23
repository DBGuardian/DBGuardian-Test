package com.dbguardian.test.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
        return findById(order.getId()).orElseThrow(IllegalStateException::new);
    }

    public List<Order> listOrders() {
        return orders.selectList(Wrappers.<Order>lambdaQuery().orderByDesc(Order::getId));
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(orders.selectById(id));
    }

    public List<Order> findByUserId(Long userId) {
        return orders.selectList(Wrappers.<Order>lambdaQuery().eq(Order::getUserId, userId).orderByDesc(Order::getId));
    }

    public Optional<Order> findByOrderNo(String orderNo) {
        return Optional.ofNullable(orders.selectOne(Wrappers.<Order>lambdaQuery().eq(Order::getOrderNo, orderNo)));
    }

    @Transactional
    public Optional<Order> findByIdFromMaster(Long id) {
        return findById(id);
    }

    @Transactional
    public Optional<Order> update(Long id, Order order) {
        orders.update(null, Wrappers.<Order>lambdaUpdate()
                .eq(Order::getId, id)
                .set(Order::getOrderNo, order.getOrderNo())
                .set(Order::getUserId, order.getUserId())
                .set(Order::getAmount, order.getAmount())
                .set(Order::getStatus, order.getStatus()));
        return findById(id);
    }

    @Transactional
    public boolean delete(Long id) {
        return orders.deleteById(id) != 0;
    }
}
