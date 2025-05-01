package com.learning.spring.secure_reactive.services;

import com.learning.spring.secure_reactive.models.User;
import com.learning.spring.secure_reactive.models.entity.UserEntity;
import com.learning.spring.secure_reactive.models.request.CreateUserRequest;
import com.learning.spring.secure_reactive.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WebClient webClient;

    private Sinks.Many<User> usersSinks;

    @BeforeEach
    void setUp() {
        usersSinks = Sinks.many().multicast().onBackpressureBuffer();
        userService = new UserService(userRepository, passwordEncoder, webClient, usersSinks);
    }

    @Test
    void createUser_withValidRequest_return_createdUserDetail() {
        CreateUserRequest request = new CreateUserRequest()
                .setFirstName("testFName")
                .setLastName("lastName")
                .setEmail("testEmail@mail.com")
                .setPassword("1234567890");
        UserEntity savedEntity = new UserEntity()
                .setId(UUID.randomUUID())
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setEmail(request.getEmail())
                .setPassword(request.getPassword());

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(savedEntity));
        Mono<User> result = userService.createUser(Mono.just(request));

        StepVerifier.create(result)
                .expectNextMatches(res -> res.getId().equals(savedEntity.getId())
                        && res.getFirstName().equals(savedEntity.getFirstName())
                        && res.getLastName().equals(savedEntity.getLastName())
                        && res.getEmail().equals(savedEntity.getEmail())
                )
                .verifyComplete();
        verify(userRepository, times(1)).save(any(UserEntity.class));

        /* Alternative way */
//        User response = result.block();
//        assertEquals(savedEntity.getId(), Objects.requireNonNull(response).getId());
//        assertEquals(savedEntity.getFirstName(), Objects.requireNonNull(response).getFirstName());
//        assertEquals(savedEntity.getLastName(), Objects.requireNonNull(response).getLastName());
    }

    @Test
    void createUser_withValidRequest_return_emitsEventToSink() {
        CreateUserRequest request = new CreateUserRequest()
                .setFirstName("testFName")
                .setLastName("lastName")
                .setEmail("testEmail@mail.com")
                .setPassword("1234567890");
        UserEntity savedEntity = new UserEntity()
                .setId(UUID.randomUUID())
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setEmail(request.getEmail())
                .setPassword(request.getPassword());

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(savedEntity));

        Flux<User> result = usersSinks.asFlux();
        StepVerifier.create(userService.createUser(Mono.just(request))
                        .thenMany(usersSinks.asFlux().take(1))
                )
                .expectNextMatches(user -> user.getId().equals(savedEntity.getId())
                        && user.getFirstName().equals(savedEntity.getFirstName())
                        && user.getLastName().equals(savedEntity.getLastName()))
                .verifyComplete();
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void getUserById_withValidRequest_return_userResponse() {
        UUID userId = UUID.randomUUID();
        UserEntity savedEntity = new UserEntity()
                .setId(userId)
                .setFirstName("testFName")
                .setLastName("lastName")
                .setEmail("testEmail@mail.com")
                .setPassword("1234567890");

        when(userRepository.findById(userId)).thenReturn(Mono.just(savedEntity));

        Mono<User> result = userService.getUserById(userId, null, "jwt-token");
        StepVerifier.create(result)
                .expectNextMatches(user -> user.getId().equals(savedEntity.getId())
                        && user.getFirstName().equals(savedEntity.getFirstName())
                        && user.getLastName().equals(savedEntity.getLastName())
                        && user.getEmail().equals(savedEntity.getEmail())
                        && user.getAlbums() == null
                )
                .verifyComplete();
        verify(userRepository, times(1)).findById(userId);
        verify(webClient, never()).get();
    }
}
