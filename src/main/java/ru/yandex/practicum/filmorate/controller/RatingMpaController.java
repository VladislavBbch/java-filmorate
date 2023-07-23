package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.service.RatingMpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class RatingMpaController {
    private final RatingMpaService ratingMpaService;

    @GetMapping
    public List<RatingMpa> getAllRatingMpa() {
        log.info("Начало обработки запроса на получение всех значений рейтинга MPA");
        List<RatingMpa> ratings = ratingMpaService.getAllRatingMpa();
        log.info("Окончание обработки запроса на получение всех значений рейтинга MPA");
        return ratings;
    }

    @GetMapping("/{id}")
    public RatingMpa getRatingMpaById(@PathVariable Long id) {
        log.info("Начало обработки запроса на получение значения рейтинга MPA по id: {}", id);
        RatingMpa ratingMpa = ratingMpaService.getRatingMpaById(id);
        log.info("Окончание обработки запроса на получение значения рейтинга MPA по id: {}", id);
        return ratingMpa;
    }
}
