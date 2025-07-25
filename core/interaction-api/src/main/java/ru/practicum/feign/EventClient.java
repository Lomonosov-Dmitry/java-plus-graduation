package ru.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;

@FeignClient(name = "event-service", path = "/admin/events")
public interface EventClient {

    @GetMapping("/{eventId}")
    EventDto getEvent(@PathVariable Long eventId);

    @PatchMapping("/{eventId}")
    EventDto updateEventAdmin(@PathVariable long eventId,
                              @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest);

    @PostMapping("/{eventId}")
    EventDto increaseConfirmed(@PathVariable long eventId, @RequestParam long quantity);

    @GetMapping("/cat/{categoryId}")
    Boolean categoryCheck(@PathVariable Long categoryId);
}
