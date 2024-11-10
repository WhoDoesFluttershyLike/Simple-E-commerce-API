package com.example.Cart.Service.service;

import com.example.Cart.Service.dto.ProductDto;
import com.example.Cart.Service.entity.Cart;
import com.example.Cart.Service.entity.CartItem;
import com.example.Cart.Service.exception.ResourceNotFoundException;
import com.example.Cart.Service.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;

    private final WebClient webClient;
    @Autowired
    public CartService(CartRepository cartRepository, WebClient.Builder webClientBuilder) {
        this.cartRepository = cartRepository;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    public Cart createCart(){
        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        return cartRepository.save(cart);
    }

    public Optional<Cart> getCartById(Long id) {
        return cartRepository.findById(id);
    }

    public Cart addItemToCart(Long cartId, CartItem item) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getItems().add(item);
        updateTotalPrice(cart);
        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(Long cartId, Long itemId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        item.setQuantity(quantity);
        updateTotalPrice(cart);
        return cartRepository.save(cart);
    }

    public void removeItemFromCart(Long cartId, Long itemId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        updateTotalPrice(cart);
        cartRepository.save(cart);
    }

    private void updateTotalPrice(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getQuantity() * getProductPrice(item.getProductId()))
                .sum();
        cart.setTotalPrice(total);
    }

    private double getProductPrice(Long productId) {
        // Здесь будет запрос к другому микро-сервису, точнее Product-Service для получения цены.
        ProductDto product = webClient.get()
                .uri("/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .block();


        return product.getPrice();
    }




}
