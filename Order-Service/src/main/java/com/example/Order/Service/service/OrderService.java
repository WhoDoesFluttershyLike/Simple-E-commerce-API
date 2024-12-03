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

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient cartWebClient;

    private final WebClient paymentWebClient;

    @Autowired

    public OrderService(OrderRepository orderRepository, WebClient.Builder webClientBuilder, WebClient webClient) {
        this.orderRepository = orderRepository;
        this.cartWebClient = webClientBuilder.baseUrl("http://localhost:8081").build(); //cart-service
        this.paymentWebClient = webClient; //payment-service
    }

    public Order createOrder(Long userId){
        CartDTO cart = cartWebClient.get()
                .uri("/carts/{userId}", userId)
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

    public Order payment(Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        String startPayment = initiate(order.getId(), order.getUserId(), order.getTotalPrice(), "USD");
        String response = confirm(startPayment);

        if ("SUCCESS".equals(response)) {
            order.setStatus("COMPLETED");
        } else {
            order.setStatus("FAILED");
        }
        return orderRepository.save(order);
    }

    public String initiate(Long orderId, Long userId, Double amount, String currency){
        return paymentWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("localhost")
                        .port(8084)
                        .path("/payments/initiate")
                        .queryParam("orderId", orderId)
                        .queryParam("userId", userId)
                        .queryParam("amount", amount)
                        .queryParam("currency", currency)
                        .build()
                )
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
    }

    public String confirm(String id){
        return paymentWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("localhost")
                        .port(8084)
                        .path("/payments/confirm")
                        .queryParam("paymentIntentId", id)
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();
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
