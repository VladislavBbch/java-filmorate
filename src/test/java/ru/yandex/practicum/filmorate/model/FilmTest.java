package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.*;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Фильм должен:")
public class FilmTest {
    private Set<ConstraintViolation<Film>> validates;
    private boolean isValidationFailed;
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
        isValidationFailed = false;
        for (ConstraintViolation<Film> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.NotNull.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "валидация на null");

        film = new Film(1, "", "Description", LocalDate.parse("1990-12-24"), 120);
        validates = validator.validate(film);
        isValidationFailed = false;
        for (ConstraintViolation<Film> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.NotBlank.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "валидация на empty");

        film = new Film(1, "   ", "Description", LocalDate.parse("1990-12-24"), 120);
        validates = validator.validate(film);
        isValidationFailed = false;
        for (ConstraintViolation<Film> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.NotBlank.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "валидация на blank");
    }

    @DisplayName("валидировать описание")
    @Test
    public void shouldValidateDescription() {
        film = new Film(1, "Name", null, LocalDate.parse("1990-12-24"), 120);

        validates = validator.validate(film);
        isValidationFailed = false;
        for (ConstraintViolation<Film> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.NotNull.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "валидация на null");

        film = new Film(1, "Name", "DescriptionDescriptionDescriptionDescriptionDescription" +
                "DescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescription" +
                "DescriptionDescriptionDescriptionDescriptionDescription", LocalDate.parse("1990-12-24"), 120);
        validates = validator.validate(film);
        isValidationFailed = false;
        for (ConstraintViolation<Film> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.Size.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "описание более 200 символов");
    }

    @DisplayName("валидировать дату релиза")
    @Test
    public void shouldValidateReleaseDate() {
        film = new Film(1, "Name", "Description", null, 120);
        final ValidationException e = assertThrows(ValidationException.class,
                () -> validator.validate(film));
        assertEquals(e.getMessage(), "HV000028: Unexpected exception during isValid call.", "валидация на null");

        film = new Film(1, "Name", "Description", LocalDate.parse("2100-12-24"), 120);
        validates = validator.validate(film);
        isValidationFailed = false;
        for (ConstraintViolation<Film> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.Past.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "будущая дата");

        film = new Film(1, "Name", "Description", LocalDate.parse("1800-12-24"), 120);
        validates = validator.validate(film);
        isValidationFailed = false;
        for (ConstraintViolation<Film> validate : validates) {
            if (validate.getMessageTemplate().equals("Дата релиза фильма должна быть после дня рождения кино (1895-12-28)")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "дата релиза фильма до дня рождения кино (1895-12-28)");
    }

    @DisplayName("валидировать продолжительность")
    @Test
    public void shouldValidateDuration() {
        film = new Film(1, "Name", "Description", LocalDate.parse("1990-12-24"), -1);
        validates = validator.validate(film);
        isValidationFailed = false;
        for (ConstraintViolation<Film> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.Positive.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "продолжительность <= 0");

        film = new Film(1, "Name", "Description", LocalDate.parse("1990-12-24"), 0);
        validates = validator.validate(film);
        isValidationFailed = false;
        for (ConstraintViolation<Film> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.Positive.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "продолжительность <= 0");
    }
}
