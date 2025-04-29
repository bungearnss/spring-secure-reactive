package com.learning.spring.secure_reactive.services;

import com.learning.spring.secure_reactive.models.User;
import com.learning.spring.secure_reactive.models.entity.UserEntity;
import com.learning.spring.secure_reactive.models.request.CreateUserRequest;
import com.learning.spring.secure_reactive.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Sinks.Many<User> usersSink;

    public UserService(Sinks.Many<User> usersSink) {
        this.usersSink = usersSink;
    }

    public Mono<User> createUser(Mono<CreateUserRequest> createUserRequestMono) {
        return createUserRequestMono
                .flatMap(this::convertToEntity)
                .flatMap(user -> userRepository.save(user))
                .mapNotNull(this::convertToModel)
                .doOnSuccess(usersSink::tryEmitNext);
    }

    public Mono<User> getUserById(UUID id) {
        return userRepository
                .findById(id)
                .mapNotNull(this::convertToModel);
    }

    public Flux<User> getAllUser(Pageable pageable) {
        return userRepository
                .findAllBy(pageable)
                .mapNotNull(this::convertToModel);
    }

    public Flux<User> streamUser(){
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
}
