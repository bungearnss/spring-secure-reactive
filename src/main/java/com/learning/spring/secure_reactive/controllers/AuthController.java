package com.learning.spring.secure_reactive.controllers;

import com.learning.spring.secure_reactive.middlewares.services.AuthenticationService;
import com.learning.spring.secure_reactive.models.request.AuthRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public Mono<ResponseEntity<Object>> login(@RequestBody @Valid Mono<AuthRequest> authRequestMono) {
        return authRequestMono
                .flatMap(authRequest ->
                        authenticationService.authentication(authRequest.getEmail(), authRequest.getPassword()))
                .map(authenticationResultMap -> ResponseEntity.ok()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationResultMap.get("token"))
                        .header("UserId", authenticationResultMap.get("userId"))
                        .build());
    }
}
