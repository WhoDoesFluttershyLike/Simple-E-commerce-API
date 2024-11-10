package com.example.Cart.Service.controller;

import com.example.Cart.Service.entity.Cart;
import com.example.Cart.Service.entity.CartItem;
import com.example.Cart.Service.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    @PostMapping
    public Cart createCart() {
        return cartService.createCart();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cart> getCartById(@PathVariable Long id) {
        return cartService.getCartById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<Cart> addItemToCart(
            @PathVariable Long cartId, @RequestBody CartItem item) {

        return ResponseEntity.ok(cartService.addItemToCart(cartId, item));
    }




    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<Cart> updateItemQuantity(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @RequestParam int quantity) {

        return ResponseEntity.ok(cartService.updateItemQuantity(cartId, itemId, quantity));
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long cartId,
            @PathVariable Long itemId) {

        cartService.removeItemFromCart(cartId, itemId);
        return ResponseEntity.noContent().build();
    }
}
