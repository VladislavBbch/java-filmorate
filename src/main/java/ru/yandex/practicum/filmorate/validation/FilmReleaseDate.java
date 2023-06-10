package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FilmReleaseDateValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FilmReleaseDate {
    String message() default "Дата релиза фильма должна быть после дня рождения кино (1895-12-28)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}