package ru.yandex.practicum.filmorate.exception;

public class InvalidValueException extends RuntimeException {
    public InvalidValueException(final String message) {
        super(message);
    }
}
