package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@Import(DatabaseRatingMpaRepository.class)
@DisplayName("Хранилище значений рейтинга MPA в базе данных должно:")
public class DatabaseRatingMpaRepositoryTest {
    @Autowired
    private DatabaseRatingMpaRepository ratingMpaRepository;

    @DisplayName("возвращать по id")
    @Test
    public void shouldGetById() {
        RatingMpa ratingMpa = ratingMpaRepository.getById(1L);
        assertNotNull(ratingMpa);
        assertEquals(1L, ratingMpa.getId(), "идентификатор");
        assertEquals("G", ratingMpa.getName(), "название");
    }

    @DisplayName("возвращать все значения")
    @Test
    public void shouldGetAll() {
        List<RatingMpa> ratings = ratingMpaRepository.read();
        assertNotNull(ratings);
        assertEquals(5, ratings.size());
        RatingMpa rating = ratings.get(0);
        assertNotNull(rating);
        assertEquals(1L, rating.getId(), "идентификатор1");
        assertEquals("G", rating.getName(), "название1");
        rating = ratings.get(1);
        assertNotNull(rating);
        assertEquals(2L, rating.getId(), "идентификатор2");
        assertEquals("PG", rating.getName(), "название2");
        rating = ratings.get(2);
        assertNotNull(rating);
        assertEquals(3L, rating.getId(), "идентификатор3");
        assertEquals("PG-13", rating.getName(), "название3");
        rating = ratings.get(3);
        assertNotNull(rating);
        assertEquals(4L, rating.getId(), "идентификатор4");
        assertEquals("R", rating.getName(), "название4");
        rating = ratings.get(4);
        assertNotNull(rating);
        assertEquals(5L, rating.getId(), "идентификатор5");
        assertEquals("NC-17", rating.getName(), "название5");
    }
}
