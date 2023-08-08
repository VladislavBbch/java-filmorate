package ru.yandex.practicum.filmorate.model.event;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder(toBuilder = true)
public class Event {
    @NotNull
    private final Long timestamp;
    @Positive
    private final Long userId;
    @NotNull
    private final EventType eventType;
    @NotNull
    private final Operation operation;
    @Positive
    private final Long eventId;
    @Positive
    private final Long entityId;
}
