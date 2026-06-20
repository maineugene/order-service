package com.innowise.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.cloud.bootstrap.enabled=true", // Разрешаем работу Spring Cloud в контексте тестов
        "spring.cloud.config.enabled=false"    // Отключаем поиск внешнего Config Server (чтобы не стучался на :8888)
})
@AutoConfigureMockMvc
@Testcontainers
class OrderServiceIntegrationTest {

    // 1. Поднимаем базу данных PostgreSQL в контейнере
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    // 2. Поднимаем полноценный и изолированный WireMock сервер в контейнере
    @Container
    static WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:3.2.0-alpine");

    @Autowired
    private MockMvc mockMvc;

    // 3. Переопределяем свойства приложения динамическими адресами из тестконтейнеров
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Подставляем параметры подключения к тестовой БД
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Перенаправляем Feign клиент (User Service) на адрес поднятого WireMock контейнера
        registry.add("user-service.url", wiremock::getBaseUrl);
    }

    @Test
    void shouldFetchOrderWithEnrichedUserInfo() throws Exception {
        // Шаг 1: Настраиваем WireMock. Если наш сервис пойдет в User Service по email, то получит этот JSON
        wiremock.importStubMapping("""
                {
                  "request": {
                    "method": "GET",
                    "urlPath": "/api/users",
                    "queryParameters": {
                      "email": { "equalTo": "test@user.com" }
                    }
                  },
                  "response": {
                    "status": 200,
                    "headers": { "Content-Type": "application/json" },
                    "body": "{\\"id\\": 99, \\"email\\": \\"test@user.com\\", \\"name\\": \\"Евгений\\"}"
                  }
                }
                """);

        // Шаг 2: Делаем запрос к нашему Order контроллеру (например, получение заказа с id = 1)
        // Примечание: Убедитесь, что в базе есть заказ с id=1 (через liquibase скрипты или setup метод),
        // либо отправьте POST запрос на создание перед этим GET запросом.
        mockMvc.perform(get("/api/orders/1")
                        .param("email", "test@user.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Проверяем, что в ответе прилетели обогащенные данные пользователя от нашей заглушки WireMock
                .andExpect(jsonPath("$.user.id").value(99))
                .andExpect(jsonPath("$.user.name").value("Евгений"))
                .andExpect(jsonPath("$.user.email").value("test@user.com"));
    }
}