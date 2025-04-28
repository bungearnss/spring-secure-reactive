package com.learning.spring.secure_reactive.repositories;

import com.learning.spring.secure_reactive.models.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, UUID> {
    Flux<UserEntity> findAllBy(Pageable pageable);
}
