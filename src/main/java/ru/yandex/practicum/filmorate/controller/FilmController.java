package ru.yandex.practicum.filmorate.controller;


import ch.qos.logback.classic.Logger;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private static final Logger log = (Logger) LoggerFactory.getLogger(FilmController.class);

    private final Map<Long, Film> films = new HashMap<>();

    public final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    public final int MAX_DESCRIPTION_LENGTH = 200;

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {

        log.info("Фильм из тела POST запроса - {}", film);

        if (film.getName() == null || film.getName().isBlank()) {
            log.info("Название фильма не должно быть пустым - {}", film.getName());
            throw new ValidationException("Название фильма не должно быть пустым");
        }
        descriptionLengthValidate(film);
        dateReleaseValidate(film);
        durationValidate(film);

        Film newFilm = film.toBuilder()
                .id(getNextId())
                .build();

        log.info("Фильм перед сохранением при создании - {}", film);
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {

        log.info("Фильм из тела PUT запроса - {}", film);

        if (film.getId() == null) {
            log.info("Не указан id фильма");
            throw new ValidationException("Не указан id фильма");
        }
        if (!films.containsKey(film.getId())) {
            log.info("Неверно указан id фильма - {}", film.getId());
            throw new NotFoundException("Неверно указан id фильма");
        }

        Film oldFilm = films.get(film.getId());

        if (film.getName() == null || film.getName().isBlank()) {
            film.setName(oldFilm.getName());
        }
        if (film.getDescription() == null || film.getDescription().isBlank()) {
            film.setDescription(oldFilm.getDescription());
        } else {
            descriptionLengthValidate(film);
        }
        if (film.getReleaseDate() == null) {
            film.setReleaseDate(oldFilm.getReleaseDate());
        } else {
            dateReleaseValidate(film);
        }
        if (film.getDuration() == null) {
            film.setDuration(oldFilm.getDuration());
        } else {
            durationValidate(film);
        }

        log.info("Фильм перед сохранением при изменении - {}", film);
        films.put(film.getId(), film);
        return film;
    }

    private void durationValidate(Film film) {
        if (film.getDuration() <= 0) {
            log.info("Продолжительность фильма должна быть положительным числом - {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private void dateReleaseValidate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.info("Дата релиза должна быть не раньше {} - {}", MIN_RELEASE_DATE, film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше " + MIN_RELEASE_DATE);
        }
    }

    private void descriptionLengthValidate(Film film) {
        if (film.getDescription().length() > 200) {
            log.info("Длина описания фильма не может быть больше {} символов - {}",
                    MAX_DESCRIPTION_LENGTH, film.getDescription().length());
            throw new ValidationException("Длина описания фильма не может быть больше "
                    + MAX_DESCRIPTION_LENGTH + " символов");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.info("Текущий максимальный id фильма - {}", currentMaxId);
        return ++currentMaxId;
    }
}
