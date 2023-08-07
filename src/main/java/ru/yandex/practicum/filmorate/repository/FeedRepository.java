package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.util.List;

public interface FeedRepository {

    void createEvent(Long userId, Long entityId, EventType eventType, Operation operation);

    List<Event> getUserFeed(Long userId);

}
