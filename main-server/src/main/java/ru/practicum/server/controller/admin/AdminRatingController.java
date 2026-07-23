package ru.practicum.server.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.server.controller.BaseController;
import ru.practicum.server.repository.rating.EventRatingRepository;

@RestController
@RequestMapping("/admin/ratings")
@RequiredArgsConstructor
public class AdminRatingController extends BaseController {

    private final EventRatingRepository ratingRepository;

    @DeleteMapping("/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearAllRatings() {
        ratingRepository.deleteAll();
    }
}