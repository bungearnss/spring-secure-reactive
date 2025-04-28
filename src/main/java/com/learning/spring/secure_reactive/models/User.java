package com.learning.spring.secure_reactive.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class User {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}
