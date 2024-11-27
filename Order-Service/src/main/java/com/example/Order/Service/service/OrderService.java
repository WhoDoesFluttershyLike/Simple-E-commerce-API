package com.example.Order.Service.service;

import com.example.Order.Service.dto.CartDTO;
import com.example.Order.Service.entity.Order;
import com.example.Order.Service.entity.OrderItem;
import com.example.Order.Service.exception.ResourceNotFoundException;
import com.example.Order.Service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient webClient;
    @Autowired

    public OrderService(OrderRepository orderRepository, WebClient.Builder webClientBuilder) {
        this.orderRepository = orderRepository;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build(); //cart-service
    }

    public Order createOrder(Long userId){
        CartDTO cart = webClient.get()
                .uri("http://cart-service/carts/{userId}", userId)
                .retrieve()
                .bodyToMono(CartDTO.class)
                .block();

        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("CREATED");
        List<OrderItem> orderItems = cart.getItems().stream().map(item -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setOrder(order);
            return orderItem;
        }).toList();
        order.setItems(orderItems);
        order.setTotalPrice(cart.getTotalPrice());

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

}
