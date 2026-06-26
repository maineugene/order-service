package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.dto.OrderRequestDto;
import com.innowise.orderservice.dto.OrderResponseDto;
import com.innowise.orderservice.dto.UserDto;
import com.innowise.orderservice.exception.ResourceNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.Order;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.repository.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;

    public OrderResponseDto createOrder(OrderRequestDto dto, String email){
        Order order = orderMapper.toEntity(dto);
        Order saved = orderRepository.save(order);
        return enrichWithUser(orderMapper.toDto(saved), email);
    }

    public OrderResponseDto getOrderById(Long id, String email){
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return enrichWithUser(orderMapper.toDto(order), email);
    }

    public Page<OrderResponseDto> getOrders(List<String> statuses, Instant from, Instant to, Pageable pageable) {
        Specification<Order> spec = Specification.where(OrderSpecification.hasStatuses(statuses))
                .and(OrderSpecification.createdBetween(from, to))
                .and(OrderSpecification.notDeleted());
        return orderRepository.findAll(spec, pageable).map(orderMapper::toDto);
    }

    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderRequestDto dto, String email) {
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(dto.status());
        return enrichWithUser(orderMapper.toDto(order), email);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setDeleted(true);
    }

    private OrderResponseDto enrichWithUser(OrderResponseDto responseDto, String email) {
        UserDto user = userServiceClient.getUserByEmail(email);
        return new OrderResponseDto(responseDto.id(), responseDto.userId(), responseDto.status(),
                responseDto.totalPrice(), user, responseDto.orderItems());
    }
}
