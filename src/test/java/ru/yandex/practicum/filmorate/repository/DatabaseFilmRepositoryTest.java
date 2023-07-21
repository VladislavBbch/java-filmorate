package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@Import(DatabaseFilmRepository.class)
@DisplayName("Хранилище фильмов в базе данных должно:")
public class DatabaseFilmRepositoryTest {
    @Autowired
    private FilmRepository filmRepository;

    @DisplayName("возвращать по id")
    @Test
    public void shouldGetById() {
        Film film = filmRepository.getById(1L);
        assertNotNull(film);
        assertEquals(1L, film.getId(), "идентификатор");
        assertEquals("Фильм1", film.getName(), "название");
        assertEquals("Описание1", film.getDescription(), "описание");
        assertEquals(LocalDate.parse("2001-05-05"), film.getReleaseDate(), "дата релиза");
        assertEquals(120, film.getDuration(), "продолжительность");
        assertEquals(1L, film.getRatingMpa().getId(), "рейтинг");
        List<Genre> genres = new ArrayList<>(film.getGenres());
        assertNotNull(genres);
        assertEquals(2, genres.size(), "жанры");
        Genre genre = genres.get(0);
        assertEquals(1L, genre.getId(), "идентификатор жанра1");
        assertEquals("Комедия", genre.getName(), "название жанра1");
        genre = genres.get(1);
        assertEquals(2L, genre.getId(), "идентификатор жанра2");
        assertEquals("Драма", genre.getName(), "название жанра2");
    }

    @DisplayName("создавать")
    @Test
    public void shouldCreate() {
        Film film = filmRepository.create(Film.builder()
                .name("Фильм7")
                .description("Описание7")
                .releaseDate(LocalDate.parse("2007-05-05"))
                .duration(130)
                .ratingMpa(new RatingMpa(3L, null))
                .genres(new HashSet<>(Set.of(new Genre(3L, null), new Genre(4L, null))))
                .build());
        assertNotNull(film);
        assertEquals(7L, film.getId(), "идентификатор");
        assertEquals("Фильм7", film.getName(), "название");
        assertEquals("Описание7", film.getDescription(), "описание");
        assertEquals(LocalDate.parse("2007-05-05"), film.getReleaseDate(), "дата релиза");
        assertEquals(130, film.getDuration(), "продолжительность");
        assertEquals(3L, film.getRatingMpa().getId(), "рейтинг");
        List<Genre> genres = new ArrayList<>(film.getGenres());
        assertNotNull(genres);
        assertEquals(2, genres.size(), "жанры");
        Genre genre = genres.get(0);
        assertEquals(4L, genre.getId(), "идентификатор жанра1");
        assertEquals("Триллер", genre.getName(), "название жанра1");
        genre = genres.get(1);
        assertEquals(3L, genre.getId(), "идентификатор жанра2");
        assertEquals("Мультфильм", genre.getName(), "название жанра2");
    }

    @DisplayName("возвращать всех")
    @Test
    public void shouldGetAll() {
        List<Film> films = filmRepository.read();
        assertNotNull(films);
        assertEquals(6, films.size());
        Film film = films.get(0);
        assertNotNull(film);
        assertEquals(1L, film.getId(), "идентификатор1");
        film = films.get(1);
        assertNotNull(film);
        assertEquals(2L, film.getId(), "идентификатор2");
        film = films.get(2);
        assertNotNull(film);
        assertEquals(3L, film.getId(), "идентификатор3");
        film = films.get(3);
        assertNotNull(film);
        assertEquals(4L, film.getId(), "идентификатор4");
        film = films.get(4);
        assertNotNull(film);
        assertEquals(5L, film.getId(), "идентификатор5");
        film = films.get(5);
        assertNotNull(film);
        assertEquals(6L, film.getId(), "идентификатор5");
    }

    @DisplayName("обновлять")
    @Test
    public void shouldUpdate() {
        Film film = filmRepository.update(Film.builder()
                .id(1L)
                .name("Обновленный")
                .description("ОбновленноеОписание")
                .releaseDate(LocalDate.parse("2008-05-05"))
                .duration(140)
                .ratingMpa(new RatingMpa(4L, null))
                .genres(new HashSet<>(Set.of(new Genre(5L, null), new Genre(6L, null))))
                .build());
        assertNotNull(film);
        assertEquals(1L, film.getId(), "идентификатор");
        assertEquals("Обновленный", film.getName(), "название");
        assertEquals("ОбновленноеОписание", film.getDescription(), "описание");
        assertEquals(LocalDate.parse("2008-05-05"), film.getReleaseDate(), "дата релиза");
        assertEquals(140, film.getDuration(), "продолжительность");
        assertEquals(4L, film.getRatingMpa().getId(), "рейтинг");
        List<Genre> genres = new ArrayList<>(film.getGenres());
        assertNotNull(genres);
        assertEquals(2, genres.size(), "жанры");
        Genre genre = genres.get(0);
        assertEquals(6L, genre.getId(), "идентификатор жанра1");
        assertEquals("Боевик", genre.getName(), "название жанра1");
        genre = genres.get(1);
        assertEquals(5L, genre.getId(), "идентификатор жанра2");
        assertEquals("Документальный", genre.getName(), "название жанра2");
    }

    @DisplayName("возвращать популярные фильмы")
    @Test
    public void shouldGetMostPopularFilms() {
        List<Film> popularFilms = filmRepository.getMostPopularFilms(3);
        assertNotNull(popularFilms);
        assertEquals(3, popularFilms.size());
        Film film = popularFilms.get(0);
        assertNotNull(film);
        assertEquals(5L, film.getId(), "идентификатор5");
        film = popularFilms.get(1);
        assertNotNull(film);
        assertEquals(4L, film.getId(), "идентификатор4");
        film = popularFilms.get(2);
        assertNotNull(film);
        assertEquals(3L, film.getId(), "идентификатор3");
    }
}