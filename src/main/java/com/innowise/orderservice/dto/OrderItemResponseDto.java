package com.innowise.orderservice.dto;

public record OrderItemResponseDto(
        Long id,
        Long itemId,
        String itemName,
        Integer quantity
) {
}
