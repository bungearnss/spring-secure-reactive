package com.learning.spring.secure_reactive.services;

import com.learning.spring.secure_reactive.models.User;
import com.learning.spring.secure_reactive.models.entity.UserEntity;
import com.learning.spring.secure_reactive.models.request.CreateUserRequest;
import com.learning.spring.secure_reactive.repositories.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Mono<User> createUser(Mono<CreateUserRequest> createUserRequestMono) {
        return createUserRequestMono
                .map(this::convertToEntity)
                .flatMap(user -> userRepository.save(user))
                .mapNotNull(this::convertToModel);
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

    private UserEntity convertToEntity(CreateUserRequest createUserRequest) {
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(createUserRequest, userEntity);
        return userEntity;
    }

    private User convertToModel(UserEntity userEntity) {
        User user = new User();
        BeanUtils.copyProperties(userEntity, user);
        return user;
    }
}
