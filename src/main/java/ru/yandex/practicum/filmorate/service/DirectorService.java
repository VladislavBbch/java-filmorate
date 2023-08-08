package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
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
        Director director = repository.getById(id);
        if (director != null) {
            return director;
        } else throw new ObjectNotFoundException("Несуществующий id режиссера: id");
    }

    public Director addDirector(Director director) {
        return repository.create(director);
    }

    public Director updateDirector(Director director) {
        getById(director.getId());
        return repository.update(director);
    }

    public void deleteDirector(Long id) {
        repository.getById(id);
        repository.delete(id);
    }
}
