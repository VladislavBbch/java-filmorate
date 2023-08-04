package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
@Validated
public class DirectorController {

    private final DirectorService service;

    @GetMapping
    public List<Director> getDirectorsList() {
        log.info("Начало обработки запроса на получение списка всех режиссеров");
        List<Director> getAll = service.getAll();
        log.info("Окончание обработки запроса на получение списка всех режиссеров");
        return getAll;
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable Long id) {
        log.info("Начало обработки запроса на получение режиссера {}", id);
        Director getById = service.getById(id);
        log.info("Окончание обработки запроса на получение режиссера {}", id);
        return getById;
    }

    @PostMapping
    public Director addDirector(@RequestBody @Valid Director director) {
        log.info("Начало обработки запроса на добавление режиссера {}", director);
        Director addedDirector = service.addDirector(director);
        log.info("Окончание обработки запроса на добавление режиссера {}", addedDirector);
        return addedDirector;
    }

    @PutMapping
    public Director updateDirector(@RequestBody @Valid Director director) {
        log.info("Начало обработки запроса на обновление режиссера {}", director);
        Director updatedDirector = service.updateDirector(director);
        log.info("Окончание обработки запроса на обновление режиссера {}", director);
        return updatedDirector;
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable Long id) {
        log.info("Начало обработки запроса на удаление режиссера {}", id);
        service.deleteDirector(id);
        log.info("Окончание обработки запроса на удаление режиссера {}", id);
    }
}