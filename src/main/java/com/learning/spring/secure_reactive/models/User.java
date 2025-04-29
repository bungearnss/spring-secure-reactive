package com.learning.spring.secure_reactive.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Album> albums;
}
