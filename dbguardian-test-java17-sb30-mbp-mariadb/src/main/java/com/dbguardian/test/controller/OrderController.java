package com.dbguardian.test.controller;

import com.dbguardian.test.entity.Order;
import com.dbguardian.test.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@RequestBody Order order) {
        return orders.create(order);
    }

    @GetMapping("/list")
    public List<Order> listOrders() {
        return orders.listOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> findById(@PathVariable Long id) {
        return ResponseEntity.of(orders.findById(id));
    }

    @GetMapping("/by-user/{userId}")
    public List<Order> findByUserId(@PathVariable Long userId) {
        return orders.findByUserId(userId);
    }

    @GetMapping("/by-order-no/{orderNo}")
    public ResponseEntity<Order> findByOrderNo(@PathVariable String orderNo) {
        return ResponseEntity.of(orders.findByOrderNo(orderNo));
    }

    @GetMapping("/master/{id}")
    public ResponseEntity<Order> findByIdFromMaster(@PathVariable Long id) {
        return ResponseEntity.of(orders.findByIdFromMaster(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> update(@PathVariable Long id, @RequestBody Order order) {
        return ResponseEntity.of(orders.update(id, order));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return orders.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
