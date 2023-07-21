package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Positive;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Жанр должен:")
public class GenreTest {
    private Set<ConstraintViolation<Genre>> validates;
    private ConstraintViolation<Genre> validate;
    private Genre genre;
    private static final Validator validator;

    static {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.usingContext().getValidator();
        }
    }

    @DisplayName("валидировать идентификатор")
    @Test
    public void shouldValidateId() {
        genre = new Genre(-1L, null);
        validates = validator.validate(genre);
        validate = validates.iterator().next();
        assertEquals(Positive.class, validate.getConstraintDescriptor().getAnnotation().annotationType(), "идентификатор <= 0");
        assertEquals("id", validate.getPropertyPath().toString(), "идентификатор <= 0, property");
    }
}