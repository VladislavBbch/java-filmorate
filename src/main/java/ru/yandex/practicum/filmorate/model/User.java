package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class User {
    private final Long id;
    @NotEmpty
    @Email
    private String email;
    @NotNull
    @Pattern(regexp = "\\S+")
    private String login;
    private String name;
    @NotNull
    @PastOrPresent
    private LocalDate birthday;

    private Set<Long> friends;
}
