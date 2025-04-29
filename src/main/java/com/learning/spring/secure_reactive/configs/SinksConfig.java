package com.learning.spring.secure_reactive.configs;

import com.learning.spring.secure_reactive.models.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class SinksConfig {

    @Bean
    public Sinks.Many<User> userSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}
