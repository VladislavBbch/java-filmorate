package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public List<Genre> getGenres() {
        return genreRepository.read();
    }

    public Genre getGenreById(Long genreId) {
        Genre genre = genreRepository.getById(genreId);
        if (genre == null) {
            throw new ObjectNotFoundException("Несуществующий id жанра: " + genreId);
        }
        return genre;
    }

}
