package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;
import ru.practicum.service.StatsService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public StatsHitDto saveHit(@RequestBody StatsHitDto hitDto) {
        log.info("POST/hit with body {}", hitDto);

        return statsService.saveHit(hitDto);
    }

    @GetMapping("/stats")
    public Collection<StatsViewDto> getStat(@RequestParam String start,
                                            @RequestParam String end,
                                            @RequestParam(required = false) List<String> uris,
                                            @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        log.info("GET/stats with params start = {}, end = {}, uris = {}, unique = {}",
                start, end, uris, unique);
        return statsService.getStats(start, end, uris, unique);
    }
}
