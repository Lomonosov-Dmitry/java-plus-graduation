package ru.practicum.controller.privateapi;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.service.EventService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventController {
    @Autowired
    EventService eventService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto addEvent(@PathVariable long userId,
                             @RequestBody @Valid NewEventDto newEventDto) {
        log.info("adding new event from user {} with body: {}", userId, newEventDto);
        return eventService.save(userId, newEventDto);
    }

    @GetMapping("/{userId}/events")
    public Collection<EventShortDto> getEvents(@PathVariable long userId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        log.info("getting events for user {}", userId);
        return eventService.findEvents(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventDto getEvent(@PathVariable long userId,
                             @PathVariable long eventId) {
        log.info("getting event {} for user {}", eventId, userId);
        return eventService.findEvent(eventId, userId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getEvent(@PathVariable long userId,
                             @PathVariable long eventId,
                             @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("updating event {} for user {} with body {}", eventId, userId, updateEventUserRequest);
        return eventService.updateEvent(eventId, userId, updateEventUserRequest);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto newRequest(@PathVariable long userId,
                                              @RequestParam long eventId) {
        log.info("Creating request on event {} from user {}", eventId, userId);
        return eventService.newRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable long userId,
                                                 @PathVariable long requestId) {
        log.info("Cancelling request {} from user {}", requestId, userId);
        return eventService.cancelRequest(userId, requestId);
    }

    @GetMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public Collection<ParticipationRequestDto> findAllByUserId(@PathVariable long userId) {
        log.info("Get all requests from user {}", userId);
        return eventService.findAllRequestsByUserId(userId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public Collection<ParticipationRequestDto> findAllByUserIdAndEventId(@PathVariable long userId,
                                                                         @PathVariable long eventId) {
        log.info("Get all requests on event {} created by user {}", eventId, userId);
        return eventService.findAllRequestsByEventId(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateStatus(@PathVariable long userId,
                                                       @PathVariable long eventId,
                                                       @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Editing requests {} on event {} created by {}", request, eventId, userId);
        return eventService.updateRequestsStatus(userId, eventId, request);
    }
}
