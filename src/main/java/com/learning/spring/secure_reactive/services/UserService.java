package com.learning.spring.secure_reactive.services;

import com.learning.spring.secure_reactive.models.Album;
import com.learning.spring.secure_reactive.models.User;
import com.learning.spring.secure_reactive.models.entity.UserEntity;
import com.learning.spring.secure_reactive.models.request.CreateUserRequest;
import com.learning.spring.secure_reactive.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebClient webClient;
    private final Sinks.Many<User> usersSink;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       WebClient webClient,
                       Sinks.Many<User> usersSink) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.webClient = webClient;
        this.usersSink = usersSink;
    }

    public Mono<User> createUser(Mono<CreateUserRequest> createUserRequestMono) {
        return createUserRequestMono
                .flatMap(this::convertToEntity)
                .flatMap(user -> userRepository.save(user))
                .mapNotNull(this::convertToModel)
                .doOnSuccess(usersSink::tryEmitNext);
    }

    public Mono<User> getUserById(UUID id, String include, String jwt) {
        return userRepository
                .findById(id)
                .mapNotNull(this::convertToModel)
                .flatMap(user -> {
                    if (include != null && include.contains("albums")) {
                        return includeUserAlbums(user, jwt);
                    }
                    return Mono.just(user);
                });
    }

    public Flux<User> getAllUser(Pageable pageable) {
        return userRepository
                .findAllBy(pageable)
                .mapNotNull(this::convertToModel);
    }

    public Flux<User> streamUser() {
        return usersSink.asFlux()
                .publish()
                .autoConnect(1);
    }

    private Mono<UserEntity> convertToEntity(CreateUserRequest createUserRequest) {
        return Mono.fromCallable(() -> {
            UserEntity userEntity = new UserEntity();
            BeanUtils.copyProperties(createUserRequest, userEntity);
            userEntity.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
            return userEntity;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private User convertToModel(UserEntity userEntity) {
        User user = new User();
        BeanUtils.copyProperties(userEntity, user);
        return user;
    }

    private Mono<User> includeUserAlbums(User user, String jwt) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .port(8084)
                        .path("/albums")
                        .queryParam("userId", user.getId())
                        .build())
                .header("Authorization", jwt)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    return Mono.error(new RuntimeException("Albums not found for user"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    return Mono.error(new RuntimeException("Server error while fetching albums"));
                })
                .bodyToFlux(Album.class)
                .collectList()
                .map(albums -> {
                    user.setAlbums(albums);
                    return user;
                })
                .onErrorResume(e -> {
                    log.error("Error fetching albums : {}", e.getMessage());
                    return Mono.just(user);
                });
    }
}
