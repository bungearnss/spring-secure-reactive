package com.learning.spring.secure_reactive.controllers;

import com.learning.spring.secure_reactive.middlewares.TestSecurityConfig;
import com.learning.spring.secure_reactive.models.User;
import com.learning.spring.secure_reactive.models.request.CreateUserRequest;
import com.learning.spring.secure_reactive.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = {UserController.class})
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void createUser_withValidateRequest_return_createdStatusAndUserDetails() {
        CreateUserRequest request = new CreateUserRequest()
                .setFirstName("testFName")
                .setLastName("lastName")
                .setEmail("testEmail@mail.com")
                .setPassword("1234567890");
        UUID userId = UUID.randomUUID();
        String expectedLocation = "/users/" + userId;
        User expectedUser = new User()
                .setId(userId)
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setEmail(request.getEmail());

        when(userService.createUser(any())).thenReturn(Mono.just(expectedUser));
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location(expectedLocation)
                .expectBody(User.class)
                .value(res -> {
                    assertEquals(expectedUser.getId(), res.getId());
                    assertEquals(expectedUser.getFirstName(), res.getFirstName());
                    assertEquals(expectedUser.getLastName(), res.getLastName());
                    assertEquals(expectedUser.getEmail(), res.getEmail());
                });
        verify(userService, times(1)).createUser(any());
    }

    @Test
    void createUser_withValidateRequest_return_badRequest(){
        CreateUserRequest request = new CreateUserRequest()
                .setFirstName("testFName")
                .setLastName("lastName")
                .setEmail("testEmail@mail.com")
                .setPassword("123");

        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).createUser(any());
    }

    @Test
    void createUser_withValidateRequest_return_internalServerError(){
        CreateUserRequest request = new CreateUserRequest()
                .setFirstName("testFName")
                .setLastName("lastName")
                .setEmail("testEmail@mail.com")
                .setPassword("1234567890");
        when(userService.createUser(any())).thenReturn(Mono.error(new RuntimeException("Service error")));
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.instance").isEqualTo("/users")
                .jsonPath("$.detail").isEqualTo("Service error");

        verify(userService, times(1)).createUser(any());
    }
}
