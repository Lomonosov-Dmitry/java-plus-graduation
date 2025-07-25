package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.ParticipationRequestDto;

import java.util.Collection;

@FeignClient(name = "request-service", path = "/users")
public interface RequestClient {

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto newRequest(@PathVariable long userId,
                                              @RequestParam long eventId);

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    ParticipationRequestDto cancelRequest(@PathVariable long userId,
                                                 @PathVariable long requestId);

    @GetMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    Collection<ParticipationRequestDto> findAllByUserId(@PathVariable long userId);

    @GetMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    Collection<ParticipationRequestDto> findAllByUserIdAndEventId(@PathVariable long userId,
                                                                         @PathVariable long eventId);

    @PatchMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    EventRequestStatusUpdateResult updateStatus(@PathVariable long userId,
                                                       @PathVariable long eventId,
                                                       @RequestBody EventRequestStatusUpdateRequest request);
}
