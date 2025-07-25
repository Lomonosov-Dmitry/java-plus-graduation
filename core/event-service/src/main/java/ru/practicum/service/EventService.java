package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.enums.SortingOptions;

import java.util.Collection;
import java.util.List;

public interface EventService {
    EventDto save(long userId, NewEventDto newEventDto);

    EventDto findEvent(long eventId, long userId);

    EventDto findEventById(long eventId);

    Boolean categoryCheck(long categoryId);

    EventDto increaseConfirmed(long eventId, long quantity);

    List<EventShortDto> findEvents(long userId, int from, int size);

    EventDto updateEvent(long eventId, long userId, UpdateEventUserRequest updateEventUserRequest);

    EventDto updateEventAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventDto> findEventsByFilter(List<Long> users,
                                      List<String> states,
                                      List<Long> categories,
                                      String rangeStart,
                                      String rangeEnd,
                                      int from,
                                      int size);

    List<EventShortDto> findEventsByFilterPublic(String text,
                                                 List<Long> categories,
                                                 Boolean paid,
                                                 String rangeStart,
                                                 String rangeEnd,
                                                 Boolean onlyAvailable,
                                                 SortingOptions sortingOptions,
                                                 int from,
                                                 int size,
                                                 HttpServletRequest request);

    EventDto findEventPublic(long eventId, HttpServletRequest request);
}

