package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(of = {"email", "login"})
public class User {

    private Long id;
    @Email
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
