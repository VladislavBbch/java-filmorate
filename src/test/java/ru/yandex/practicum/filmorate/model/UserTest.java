package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.*;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Пользователь должен:")
public class UserTest {
    private Set<ConstraintViolation<User>> validates;
    private boolean isValidationFailed;
    private User user;
    private static final Validator validator;

    static {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.usingContext().getValidator();
        }
    }

    @DisplayName("валидировать email")
    @Test
    public void shouldValidateEmail() {
        user = new User(1, null, "Login", "Name", LocalDate.parse("1990-12-24"));

        validates = validator.validate(user);
        isValidationFailed = false;
        for (ConstraintViolation<User> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.NotNull.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "валидация на null");

        user = new User(1, "test.ru", "Login", "Name", LocalDate.parse("1990-12-24"));
        validates = validator.validate(user);
        isValidationFailed = false;
        for (ConstraintViolation<User> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.Email.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "не совпадает формат адреса электронной почты");
    }

    @DisplayName("валидировать логин")
    @Test
    public void shouldValidateLogin() {
        user = new User(1, "test@test.ru", null, "Name", LocalDate.parse("1990-12-24"));

        final ValidationException e = assertThrows(ValidationException.class,
                () -> validator.validate(user));
        assertEquals(e.getMessage(), "HV000028: Unexpected exception during isValid call.", "валидация на null");

        user = new User(1, "test@test.ru", " ", "Name", LocalDate.parse("1990-12-24"));
        validates = validator.validate(user);
        isValidationFailed = false;
        for (ConstraintViolation<User> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.NotBlank.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "валидация на blank");

        user = new User(1, "test@test.ru", "Login with spaces", "Name", LocalDate.parse("1990-12-24"));
        validates = validator.validate(user);
        isValidationFailed = false;
        for (ConstraintViolation<User> validate : validates) {
            if (validate.getMessageTemplate().equals("Логин пользователя содержит пробелы")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "содержит пробелы");
    }

    @DisplayName("валидировать дату рождения")
    @Test
    public void shouldValidateBirthday() {
        user = new User(1, "test@test.ru", "Login", "Name", null);

        validates = validator.validate(user);
        isValidationFailed = false;
        for (ConstraintViolation<User> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.NotNull.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "валидация на null");

        user = new User(1, "test@test.ru", "Login", "Name", LocalDate.parse("2100-12-24"));
        validates = validator.validate(user);
        isValidationFailed = false;
        for (ConstraintViolation<User> validate : validates) {
            if (validate.getMessageTemplate().equals("{javax.validation.constraints.Past.message}")) {
                isValidationFailed = true;
                break;
            }
        }
        assertTrue(isValidationFailed, "будущая дата");
    }
}
