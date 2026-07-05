package ru.practicum.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения Explore With Me.
 * Запускает Spring Boot приложение и инициализирует контекст.
 * Сканирует пакеты для основного сервиса.
 *
 * @author Goose
 * @version 1.0
 * @since 2026-06-26
 */

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.server",
        "ru.practicum.stats",
        "ru.practicum.stats.client",
        "ru.practicum.stats.dto",
})
public class ExploreApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExploreApplication.class, args);
    }
}
