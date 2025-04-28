package com.learning.spring.secure_reactive.controllers;

import com.learning.spring.secure_reactive.models.User;
import com.learning.spring.secure_reactive.models.request.CreateUserRequest;
import com.learning.spring.secure_reactive.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<User>> createUser(@RequestBody @Valid Mono<CreateUserRequest> createUserRequest) {
        return userService.createUser(createUserRequest)
                .map(user -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .location(URI.create("/users/" + user.getId()))
                        .body(user)
                );
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable("userId") UUID userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.status(HttpStatus.OK).body(user))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));
    }

    @GetMapping
    public Flux<User> getAllUser(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        if (page > 0) page = page - 1;
        Pageable pageable = PageRequest.of(page, limit);
        return userService.getAllUser(pageable);
    }
}
