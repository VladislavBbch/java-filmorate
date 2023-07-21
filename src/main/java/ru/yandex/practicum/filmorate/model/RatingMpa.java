package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class RatingMpa {
    @NotNull
    @Positive
    private final Long id;
    private final String name;
}
