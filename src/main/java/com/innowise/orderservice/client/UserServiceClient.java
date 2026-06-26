package com.innowise.orderservice.client;

import com.innowise.orderservice.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {

    @GetMapping("/api/users")
    @CircuitBreaker(name = "userServiceBreaker", fallbackMethod = "fallbackGetUser")
    UserDto getUserByEmail(@RequestParam("email") String email);

    default UserDto fallbackGetUser(String email, Throwable throwable){
        return new UserDto(0L, email, "Unknown User (Service Unavailable)");
    }
}
