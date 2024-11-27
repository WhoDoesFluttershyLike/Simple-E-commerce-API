package com.example.Order.Service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CartDTO {
    private List<CartItemDTO> items;
    private Double totalPrice;
}
