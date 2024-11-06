package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode()
public class Film {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
}
