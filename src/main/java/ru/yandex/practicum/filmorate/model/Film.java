package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.FilmReleaseDate;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class Film {
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private final int id;
    @NotBlank
    private String name;
    @NotNull
    @Size(max = MAX_DESCRIPTION_LENGTH)
    private String description;
    @FilmReleaseDate
    private LocalDate releaseDate;
    @Positive
    private int duration;
}
