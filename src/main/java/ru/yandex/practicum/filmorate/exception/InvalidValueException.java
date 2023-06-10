package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidValueException extends RuntimeException {
    public InvalidValueException(final String message) {
        super(message);
        log.error(message);
    }
}
