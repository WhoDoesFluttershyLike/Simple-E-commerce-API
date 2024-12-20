package com.example.Order.Service.controller;

import com.example.Order.Service.entity.Order;
import com.example.Order.Service.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.http.HttpClient;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    @PostMapping("/{userId}")
    public RedirectView createOrder(@PathVariable Long userId) {
        Order order = orderService.createOrder(userId);
        return new RedirectView("/orders/initiate-payment/"+order.getId());
        //return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/initiate-payment/{orderId}")
    public ResponseEntity<Order> initiatePayment(@PathVariable Long orderId){
        Order order = orderService.payment(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }
}
