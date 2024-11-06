package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j

public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {

        log.info("Пользователь из тела POST запроса - {}", user);

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.info("Не указан имейл - {}", user.getEmail());
            throw new ValidationException("Не указан имейл");
        }

        isContainsEmail(user.getEmail());
        emailValidate(user.getEmail());

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.info("Логин не может быть пустым - {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым");
        }

        loginValidate(user.getLogin());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        birthdayValidate(user.getBirthday());

        user.setId(getNextId());
        log.info("Пользователь перед сохранением при создании - {}", user);
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Пользователь из тела PUT запроса - {}", user);

        if (user.getId() == null) {
            log.info("Не указан id пользователя");
            throw new ValidationException("Не указан id пользователя");
        }

        if (!users.containsKey(user.getId())) {
            log.info("Неверно указан id пользователя - {}", user.getId());
            throw new NotFoundException("Неверно указан id пользователя");
        }

        User oldUser = users.get(user.getId());

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            user.setEmail(oldUser.getEmail());
        } else if (!user.getEmail().equals(oldUser.getEmail())) {
            emailValidate(user.getEmail());
            isContainsEmail(user.getEmail());
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            user.setLogin(oldUser.getLogin());
        }

        if (user.getName() == null || user.getName().isBlank()) {
            if (oldUser.getLogin().equals(oldUser.getName())) {
                user.setName(user.getLogin());
            } else {
                user.setName(oldUser.getName());
            }
        }

        if (user.getBirthday() == null) {
            user.setBirthday(oldUser.getBirthday());
        } else {
            birthdayValidate(user.getBirthday());
        }

        log.info("Пользователь перед сохранением при изменении - {}", user);
        users.put(user.getId(), user);
        return user;
    }

    private boolean isContainsEmail(String userEmail) {
        boolean isContains = users.values().stream()
                .map(User::getEmail)
                .anyMatch(userEmail::equals);
        if (isContains) {
            log.info("Пользователь с таким имейлом уже существует - {}", userEmail);
            throw new ValidationException("Пользователь с таким имейлом уже существует");
        } else {
            return false;
        }
    }

    private boolean emailValidate(String email) {
        if (!email.matches("\\w+([\\-._]?\\w+)*@\\w+([\\-.]\\w+)*\\.[A-Za-z]{2,4}")) {
            log.info("Указан некорректный имейл - {}", email);
            throw new ValidationException("Указан некорректный имейл");
        } else {
            return true;
        }
    }

    private boolean loginValidate(String login) {
        if (login.contains(" ")) {
            log.info("Логин не должен содержать пробелов, {}", login);
            throw new ValidationException("Логин не должен содержать пробелов");
        } else {
            return true;
        }
    }

    private boolean birthdayValidate(LocalDate birthday) {
        if (birthday.isAfter(LocalDate.now())) {
            log.info("Дата рождения не может быть в будущем - {}", birthday);
            throw new ValidationException("Дата рождения не может быть в будущем");
        } else {
            return true;
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.info("Текущий максимальный id пользователя - {}", currentMaxId);
        return ++currentMaxId;
    }
}
