package com.learning.spring.secure_reactive.middlewares.services;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface AuthenticationService {
    Mono<Map<String, String>> authentication(String username, String password);
}
