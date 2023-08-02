package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository repository;

    public List<Director> getAll() {
        return repository.read();
    }

    public Director getById(Long id) {
        return repository.getById(id);
    }

    public Director addDirector(Director director) {
        return repository.create(director);
    }

    public Director updateDirector(Director director) {
        repository.getById(director.getId());
        return repository.update(director);
    }
}
