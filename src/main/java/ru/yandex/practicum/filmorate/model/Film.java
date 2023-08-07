package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.FilmReleaseDate;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class Film {
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    @Positive
    private final Long id;
    @NotBlank
    private String name;
    @NotNull
    @Size(max = MAX_DESCRIPTION_LENGTH)
    private String description;
    @FilmReleaseDate
    private LocalDate releaseDate;
    @Positive
    private int duration;
    @NotNull
    @JsonProperty("mpa")
    @Valid
    private RatingMpa ratingMpa;
    private Set<Genre> genres;
    private Set<Director> directors;
}
