package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.validation.FilmReleaseDate;

import javax.validation.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Фильм должен:")
public class FilmTest {
    private Set<ConstraintViolation<Film>> validates;
    private ConstraintViolation<Film> validate;
    private Film film;
    private static final Validator validator;

    static {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.usingContext().getValidator();
        }
    }

    @DisplayName("валидировать имя")
    @Test
    public void shouldValidateName() {
        film = new Film(1, null, "Description", LocalDate.parse("1990-12-24"), 120);
        validates = validator.validate(film);
        validate = validates.iterator().next();
        assertEquals(NotBlank.class, validate.getConstraintDescriptor().getAnnotation().annotationType(), "валидация на null");
        assertEquals("name", validate.getPropertyPath().toString(), "валидация на null, property");

        film = new Film(1, "", "Description", LocalDate.parse("1990-12-24"), 120);
        validates = validator.validate(film);
        validate = validates.iterator().next();
        assertEquals(NotBlank.class, validate.getConstraintDescriptor().getAnnotation().annotationType(), "валидация на empty");
        assertEquals("name", validate.getPropertyPath().toString(), "валидация на empty, property");

        film = new Film(1, "   ", "Description", LocalDate.parse("1990-12-24"), 120);
        validates = validator.validate(film);
        validate = validates.iterator().next();
        assertEquals(NotBlank.class, validate.getConstraintDescriptor().getAnnotation().annotationType(), "валидация на blank");
        assertEquals("name", validate.getPropertyPath().toString(), "валидация на blank, property");
    }

    @DisplayName("валидировать описание")
    @Test
    public void shouldValidateDescription() {
        film = new Film(1, "Name", null, LocalDate.parse("1990-12-24"), 120);
        validates = validator.validate(film);
        validate = validates.iterator().next();
        assertEquals(NotNull.class, validate.getConstraintDescriptor().getAnnotation().annotationType(), "валидация на null");
        assertEquals("description", validate.getPropertyPath().toString(), "валидация на null, property");

        film = new Film(1, "Name", "DescriptionDescriptionDescriptionDescriptionDescription" +
                "DescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescription" +
                "DescriptionDescriptionDescriptionDescriptionDescription", LocalDate.parse("1990-12-24"), 120);
        validates = validator.validate(film);
        validate = validates.iterator().next();
        assertEquals(Size.class, validate.getConstraintDescriptor().getAnnotation().annotationType(), "описание более 200 символов");
        assertEquals("description", validate.getPropertyPath().toString(), "описание более 200 символов, property");
    }

    @DisplayName("валидировать дату релиза")
    @Test
    public void shouldValidateReleaseDate() {
        film = new Film(1, "Name", "Description", null, 120);
        validates = validator.validate(film);
        validate = validates.iterator().next();
        assertEquals(FilmReleaseDate.class, validate.getConstraintDescriptor().getAnnotation().annotationType(), "валидация на null");
        assertEquals("releaseDate", validate.getPropertyPath().toString(), "валидация на null, property");

        film = new Film(1, "Name", "Description", LocalDate.parse("1800-12-24"), 120);
        validates = validator.validate(film);
        validate = validates.iterator().next();
        assertEquals(FilmReleaseDate.class, validate.getConstraintDescriptor().getAnnotation().annotationType(),
                "дата релиза фильма до дня рождения кино (1895-12-28)");
        assertEquals("releaseDate", validate.getPropertyPath().toString(),
                "дата релиза фильма до дня рождения кино (1895-12-28), property");
    }

    @DisplayName("валидировать продолжительность")
    @Test
    public void shouldValidateDuration() {
        film = new Film(1, "Name", "Description", LocalDate.parse("1990-12-24"), -1);
        validates = validator.validate(film);
        validate = validates.iterator().next();
        assertEquals(Positive.class, validate.getConstraintDescriptor().getAnnotation().annotationType(), "продолжительность <= 0");
        assertEquals("duration", validate.getPropertyPath().toString(), "продолжительность <= 0, property");

        film = new Film(1, "Name", "Description", LocalDate.parse("1990-12-24"), 0);
        validates = validator.validate(film);
        validate = validates.iterator().next();
        assertEquals(Positive.class, validate.getConstraintDescriptor().getAnnotation().annotationType(), "продолжительность <= 0");
        assertEquals("duration", validate.getPropertyPath().toString(), "продолжительность <= 0, property");
    }
}
