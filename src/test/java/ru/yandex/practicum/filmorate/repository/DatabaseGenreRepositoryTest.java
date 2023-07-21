package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@Import(DatabaseGenreRepository.class)
@DisplayName("Хранилище жанров в базе данных должно:")
public class DatabaseGenreRepositoryTest {
    @Autowired
    private DatabaseGenreRepository genreRepository;

    @DisplayName("возвращать все значения")
    @Test
    public void shouldGetAll() {
        List<Genre> genres = genreRepository.read();
        assertNotNull(genres);
        assertEquals(6, genres.size());
        Genre genre = genres.get(0);
        assertNotNull(genre);
        assertEquals(1L, genre.getId(), "идентификатор1");
        assertEquals("Комедия", genre.getName(), "название1");
        genre = genres.get(1);
        assertNotNull(genre);
        assertEquals(2L, genre.getId(), "идентификатор2");
        assertEquals("Драма", genre.getName(), "название2");
        genre = genres.get(2);
        assertNotNull(genre);
        assertEquals(3L, genre.getId(), "идентификатор3");
        assertEquals("Мультфильм", genre.getName(), "название3");
        genre = genres.get(3);
        assertNotNull(genre);
        assertEquals(4L, genre.getId(), "идентификатор4");
        assertEquals("Триллер", genre.getName(), "название4");
        genre = genres.get(4);
        assertNotNull(genre);
        assertEquals(5L, genre.getId(), "идентификатор5");
        assertEquals("Документальный", genre.getName(), "название5");
        genre = genres.get(5);
        assertNotNull(genre);
        assertEquals(6L, genre.getId(), "идентификатор6");
        assertEquals("Боевик", genre.getName(), "название6");
    }

    @DisplayName("возвращать название по id")
    @Test
    public void shouldGetById() {
        Genre genre = genreRepository.getById(1L);
        assertNotNull(genre);
        assertEquals(1L, genre.getId(), "идентификатор");
        assertEquals("Комедия", genre.getName(), "название");
    }

    @DisplayName("возвращать названия по нескольким id")
    @Test
    public void shouldGetByIds() {
        List<Genre> genres = genreRepository.getByIds(List.of(1L, 2L, 3L));
        assertNotNull(genres);
        assertEquals(3, genres.size());
        Genre genre = genres.get(0);
        assertNotNull(genre);
        assertEquals(1L, genre.getId(), "идентификатор1");
        assertEquals("Комедия", genre.getName(), "название1");
        genre = genres.get(1);
        assertNotNull(genre);
        assertEquals(2L, genre.getId(), "идентификатор2");
        assertEquals("Драма", genre.getName(), "название2");
        genre = genres.get(2);
        assertNotNull(genre);
        assertEquals(3L, genre.getId(), "идентификатор3");
        assertEquals("Мультфильм", genre.getName(), "название3");
    }
}