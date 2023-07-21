package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Рейтинг MPA должен:")
public class RatingMpaTest {
    private Set<ConstraintViolation<RatingMpa>> validates;
    private ConstraintViolation<RatingMpa> validate;
    private RatingMpa ratingMpa;
    private static final Validator validator;

    static {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.usingContext().getValidator();
        }
    }

    @DisplayName("валидировать идентификатор")
    @Test
    public void shouldValidateId() {
        ratingMpa = new RatingMpa(null, null);
        validates = validator.validate(ratingMpa);
        validate = validates.iterator().next();
        assertEquals(NotNull.class, validate.getConstraintDescriptor().getAnnotation().annotationType(), "валидация на null");
        assertEquals("id", validate.getPropertyPath().toString(), "валидация на null, property");

        ratingMpa = new RatingMpa(-1L, null);
        validates = validator.validate(ratingMpa);
        validate = validates.iterator().next();
        assertEquals(Positive.class, validate.getConstraintDescriptor().getAnnotation().annotationType(), "идентификатор <= 0");
        assertEquals("id", validate.getPropertyPath().toString(), "идентификатор <= 0, property");
    }
}
