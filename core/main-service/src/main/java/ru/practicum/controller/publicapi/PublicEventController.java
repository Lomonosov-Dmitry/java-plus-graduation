package ru.practicum.controller.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.enums.SortingOptions;
import ru.practicum.service.EventService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicEventController {
    @Autowired
    EventService eventService;

    @GetMapping
    public Collection<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                               @RequestParam(required = false) List<Long> categories,
                                               @RequestParam(required = false) Boolean paid,
                                               @RequestParam(required = false) String rangeStart,
                                               @RequestParam(required = false) String rangeEnd,
                                               @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                               @RequestParam(required = false) SortingOptions sortingOptions,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               HttpServletRequest request) {
        log.info("getting events public");
        return eventService.findEventsByFilterPublic(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sortingOptions, from, size, request);
    }

    @GetMapping("/{eventId}")
    public EventDto getEventById(@PathVariable long eventId, HttpServletRequest request) {
        log.info("getting event {} public", eventId);
        return eventService.findEventPublic(eventId, request);
    }
}
