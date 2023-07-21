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
    public List<RatingMpa> getMpa() {
        return ratingMpaService.getAllRatingMpa();
    }

    @GetMapping("/{id}")
    public RatingMpa getMpaById(@PathVariable Long id) {
        return ratingMpaService.getRatingMpaById(id);
    }
}
