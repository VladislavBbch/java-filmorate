package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder(toBuilder = true)
public class Review {
    @Positive
    private final Long reviewId;
    @NotNull
    private Long userId;
    @NotNull
    private Long filmId;
    @NotBlank
    private String content;
    @NotNull
    private Boolean isPositive;
    @JsonProperty("useful")
    private Integer usefulness;
}
