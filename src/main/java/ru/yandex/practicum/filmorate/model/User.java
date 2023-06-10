package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.UserLogin;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class User {
    private final int id;
    @NotNull
    @Email
    private String email;
    @NotNull
    @NotBlank
    @UserLogin
    private String login;
    private String name;
    @NotNull
    @Past
    private LocalDate birthday;
}
