package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.enums.SortingOptions;

import java.util.Collection;
import java.util.List;

public interface RequestService {

    ParticipationRequestDto newRequest(long userId, long eventId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);

    Collection<ParticipationRequestDto> findAllRequestsByUserId(long userId);

    Collection<ParticipationRequestDto> findAllRequestsByEventId(long userId, long eventId);

    EventRequestStatusUpdateResult updateRequestsStatus(long userId,
                                                        long eventId,
                                                        EventRequestStatusUpdateRequest request);

}

