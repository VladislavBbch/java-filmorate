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
        List<Director> directors = repository.read();
        if (directors != null) {
            return directors;
        } else throw new ObjectNotFoundException("Directors Not Found");
    }

    public Director getById(Long id) {
        Director director = repository.getById(id);
        if (director != null) {
            return director;
        } else throw new ObjectNotFoundException("Director Not Found");
    }

    public Director addDirector(Director director) {
        Director createdDirector = repository.create(director);
        if (createdDirector == null) {
            throw new ObjectNotFoundException("Director Not Found");
        }
        return createdDirector;
    }

    public Director updateDirector(Director director) {
        if (repository.getById(director.getId()) != null) {
            return repository.update(director);
        } else throw new ObjectNotFoundException("Update Director Exception");
    }


    public void deleteDirector(Long id) {
        repository.getById(id);
        repository.delete(id);
    }
}
