package ru.practicum.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestController {
    @Autowired
    RequestService requestService;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto newRequest(@PathVariable long userId,
                                              @RequestParam long eventId) {
        log.info("Creating request on event {} from user {}", eventId, userId);
        return requestService.newRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable long userId,
                                                 @PathVariable long requestId) {
        log.info("Cancelling request {} from user {}", requestId, userId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public Collection<ParticipationRequestDto> findAllByUserId(@PathVariable long userId) {
        log.info("Get all requests from user {}", userId);
        return requestService.findAllRequestsByUserId(userId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public Collection<ParticipationRequestDto> findAllByUserIdAndEventId(@PathVariable long userId,
                                                                         @PathVariable long eventId) {
        log.info("Get all requests on event {} created by user {}", eventId, userId);
        return requestService.findAllRequestsByEventId(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateStatus(@PathVariable long userId,
                                                       @PathVariable long eventId,
                                                       @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Editing requests {} on event {} created by {}", request, eventId, userId);
        return requestService.updateRequestsStatus(userId, eventId, request);
    }
}
