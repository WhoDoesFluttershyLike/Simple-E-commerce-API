package com.example.Order.Service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDTO {
    private Long productId;
    private Integer quantity;
}
