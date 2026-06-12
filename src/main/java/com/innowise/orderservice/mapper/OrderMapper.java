package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderItemResponseDto;
import com.innowise.orderservice.dto.OrderRequestDto;
import com.innowise.orderservice.dto.OrderResponseDto;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toEntity(OrderRequestDto dto);

    OrderResponseDto toDto(Order entity);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    OrderItemResponseDto toItemDto(OrderItem entity);
}
