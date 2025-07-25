package ru.practicum.controller.adminapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.service.EventService;

import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    @Autowired
    EventService eventService;

    @GetMapping
    public Collection<EventDto> getEvents(@RequestParam(required = false) List<Long> users,
                                          @RequestParam(required = false) List<String> states,
                                          @RequestParam(required = false) List<Long> categories,
                                          @RequestParam(required = false) String rangeStart,
                                          @RequestParam(required = false) String rangeEnd,
                                          @RequestParam(defaultValue = "0") Integer from,
                                          @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Admin getting events");
        return eventService.findEventsByFilter(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @GetMapping("/{eventId}")
    public EventDto getEvent(@PathVariable Long eventId) {
        log.info("Get event by id {}", eventId);
        return eventService.findEventById(eventId);
    }

    @GetMapping("/cat/{categoryId}")
    public Boolean categoryCheck(@PathVariable Long categoryId) {
        log.info("Check events on category {}", categoryId);
        return eventService.categoryCheck(categoryId);
    }

    @PostMapping("/{eventId}")
    public EventDto increaseConfirmed(@PathVariable long eventId, @RequestParam long quantity) {
        log.info("Increasing confirmed in event {}", eventId);
        return eventService.increaseConfirmed(eventId, quantity);
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEventAdmin(@PathVariable long eventId,
                                     @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Admin updating event {}", eventId);
        return eventService.updateEventAdmin(eventId, updateEventAdminRequest);
    }

}
