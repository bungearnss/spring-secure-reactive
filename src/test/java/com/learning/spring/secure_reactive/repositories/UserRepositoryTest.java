package com.learning.spring.secure_reactive.repositories;

import com.learning.spring.secure_reactive.models.entity.UserEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

@DataR2dbcTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRepositoryTest {

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    void setUp() {
        UserEntity user1 = new UserEntity()
                .setId(UUID.randomUUID())
                .setFirstName("user1_firstName")
                .setLastName("user1_lastName")
                .setEmail("testEmail1@mail.com")
                .setPassword("1234567890");

        UserEntity user2 = new UserEntity()
                .setId(UUID.randomUUID())
                .setFirstName("user2_firstName")
                .setLastName("user2_lastName")
                .setEmail("testEmail2@mail.com")
                .setPassword("1234567890");

        String insertSql = "INSERT INTO users (id, first_name, last_name, email, password) VALUES " +
                "(:id, :firstName, :lastName, :email, :password)";

        Flux.just(user1, user2)
                .concatMap(user -> databaseClient.sql(insertSql)
                        .bind("id", user.getId())
                        .bind("firstName", user.getFirstName())
                        .bind("lastName", user.getLastName())
                        .bind("email", user.getEmail())
                        .bind("password", user.getPassword())
                        .fetch()
                        .rowsUpdated())
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @AfterAll
    void tearDown() {
        databaseClient.sql("TRUNCATE TABLE users")
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findByEmail_withEmailThatExists_returns_matchingUser() {
        String emailToFind = "testEmail1@mail.com";
        StepVerifier.create(userRepository.findByEmail(emailToFind))
                .expectNextMatches(user -> user.getEmail().equals(emailToFind))
                .verifyComplete();
    }

    @Test
    void findByEmail_withEmailDoesNotExists_returns_empty() {
        String emailToFind = "testEmail1123@mail.com";
        StepVerifier.create(userRepository.findByEmail(emailToFind))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findAllBy_withValidPageable_return_results() {
        Pageable pageable = PageRequest.of(0, 2);
        StepVerifier.create(userRepository.findAllBy(pageable))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findAllBy_withNonExistentPage_return_Empty() {
        Pageable pageable = PageRequest.of(10, 2);
        StepVerifier.create(userRepository.findAllBy(pageable))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void save_withEmailThatExists_return_failed() {
        UserEntity user = new UserEntity()
                .setId(null)
                .setFirstName("user3_firstName")
                .setLastName("user3_lastName")
                .setEmail("testEmail2@mail.com")
                .setPassword("1234567890");
        userRepository.save(user)
                .as(StepVerifier::create)
                .expectError(DataIntegrityViolationException.class)
                .verify();
    }

    @Test
    void save_withValidUser_return_success() {
        UserEntity user = new UserEntity()
                .setId(null)
                .setFirstName("user4_firstName")
                .setLastName("user4_lastName")
                .setEmail("testEmail4@mail.com")
                .setPassword("1234567890");
        userRepository.save(user)
                .as(StepVerifier::create)
                .expectNextMatches(savedUser -> savedUser.getId() != null
                        && savedUser.getFirstName().equals(user.getFirstName())
                        && savedUser.getLastName().equals(user.getLastName())
                        && savedUser.getEmail().equals(user.getEmail()))
                .verifyComplete();
    }
}
