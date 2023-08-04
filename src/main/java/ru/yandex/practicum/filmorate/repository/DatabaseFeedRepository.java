package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

@Component
@RequiredArgsConstructor
public class DatabaseFeedRepository implements FeedRepository {

    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    @Override
    public void createEvent(Long userId, Long entityId, EventType eventType, Operation operation) {
        Long eventTypeId = getEventTypeId(eventType);
        Long operationId = getOperationId(operation);
        if (eventTypeId == null)
            throw new RuntimeException("Несуществующий в базе тип операции: " + eventType);
        if (operationId == null)
            throw new RuntimeException("Несуществующая в базе операция: " + operation);
        parameterJdbcTemplate.update(
                "INSERT INTO EVENTS (USER_ID, ENTITY_ID, EVENT_TIMESTAMP, EVENT_TYPE_ID, OPERATION_ID) " +
                        "VALUES (:userId, :entityId, :eventTimestamp, :eventTypeId, :operationId)",
                Map.ofEntries(
                        entry("userId", userId),
                        entry("entityId", entityId),
                        entry("eventTimestamp", Instant.now().toEpochMilli()),
                        entry("eventTypeId", eventTypeId),
                        entry("operationId", operationId)));
    }

    @Override
    public List<Event> getUserFeed(Long userId) {
        List<Event> feed = new ArrayList<>();
        SqlRowSet feedRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT E.ID, " +
                        "E.USER_ID, " +
                        "E.ENTITY_ID," +
                        "E.EVENT_TIMESTAMP, " +
                        "ET.NAME AS EVENT_TYPE_NAME, " +
                        "O.NAME AS OPERATION_NAME " +
                        "FROM EVENTS AS E " +
                        "INNER JOIN EVENT_TYPES AS ET ON E.EVENT_TYPE_ID = ET.ID " +
                        "INNER JOIN OPERATIONS AS O ON E.OPERATION_ID = O.ID " +
                        "WHERE E.USER_ID = :userId", Map.of("userId", userId));
        while (feedRow.next()) {
            feed.add(mapRowToEvent(feedRow));
        }
        return feed;
    }

    private Long getEventTypeId(EventType eventType) {
        SqlRowSet idRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT ID " +
                        "FROM EVENT_TYPES " +
                        "WHERE NAME = :name", Map.of("name", eventType.toString()));
        if (idRow.next()) {
            return idRow.getLong("ID");
        }
        return null;
    }

    private Long getOperationId(Operation operation) {
        SqlRowSet idRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT ID " +
                        "FROM OPERATIONS " +
                        "WHERE NAME = :name", Map.of("name", operation.toString())
        );
        if (idRow.next()) {
            return idRow.getLong("ID");
        }
        return null;
    }

    private Event mapRowToEvent(SqlRowSet eventRow) {
        return Event.builder()
                .eventId(eventRow.getLong("ID"))
                .timestamp(eventRow.getLong("EVENT_TIMESTAMP"))
                .userId(eventRow.getLong("USER_ID"))
                .entityId(eventRow.getLong("ENTITY_ID"))
                .eventType(EventType.valueOf(eventRow.getString("EVENT_TYPE_NAME")))
                .operation(Operation.valueOf(eventRow.getString("OPERATION_NAME")))
                .build();
    }
}
