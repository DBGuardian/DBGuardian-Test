package com.test.controller;

import com.test.model.Order;
import com.test.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @GetMapping("/list")
    public List<Order> listOrders() { return orderService.list(); }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) { return orderService.getOrderById(id); }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable Long userId) { return orderService.getOrdersByUserId(userId); }

    @PostMapping
    public Order createOrder(@RequestBody Order order) { return orderService.saveOrder(order); }

    @PutMapping("/{id}")
    public boolean updateOrder(@PathVariable Long id, @RequestBody Order order) {
        order.setId(id);
        return orderService.updateById(order);
    }

    @DeleteMapping("/{id}")
    public boolean deleteOrder(@PathVariable Long id) { return orderService.removeById(id); }
}