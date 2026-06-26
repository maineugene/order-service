package com.innowise.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(
        Long id,
        Long userId,
        String status,
        BigDecimal totalPrice,
        UserDto user,
        List<OrderItemResponseDto> orderItems
) {
}
