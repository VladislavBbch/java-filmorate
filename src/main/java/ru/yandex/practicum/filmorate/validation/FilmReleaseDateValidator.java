package ru.yandex.practicum.filmorate.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class FilmReleaseDateValidator implements ConstraintValidator<FilmReleaseDate, LocalDate> {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.parse("1895-12-28");

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value.isAfter(CINEMA_BIRTHDAY);
    }
}
