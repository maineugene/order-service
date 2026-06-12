package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.OrderRequestDto;
import com.innowise.orderservice.dto.OrderResponseDto;
import com.innowise.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> create(@Valid @RequestBody OrderRequestDto dto, @RequestParam String email){
        return new ResponseEntity<>(orderService.createOrder(dto, email), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getById(@PathVariable Long id, @RequestParam String email) {
        return ResponseEntity.ok(orderService.getOrderById(id, email));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getAll(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(statuses, from, to, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> update(@PathVariable Long id, @Valid @RequestBody OrderRequestDto dto, @RequestParam String email) {
        return ResponseEntity.ok(orderService.updateOrder(id, dto, email));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}
