package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Data
public class Director {
    @Positive
    private final long id;
    @NotBlank
    private final String name;
}
