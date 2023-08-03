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
        log.info("Обработка запроса списка режиссеров");
        return service.getAll();
    }
    @GetMapping("/{id}")
    public Director getById(@PathVariable Long id) {
        log.info("Обработка запроса получение режиссера {}", id);
        return service.getById(id);
    }
    @PostMapping
    public Director addDirector(@RequestBody @Valid Director director) {
        log.info("Обработка запроса добавления режиссера");
        return service.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@RequestBody @Valid Director director) {
        log.info("Обработка запроса обновления режиссера");
        return service.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable Long id) {
        log.info("Обработка запроса удаления режиссера");
        service.deleteDirector(id);
    }

}
