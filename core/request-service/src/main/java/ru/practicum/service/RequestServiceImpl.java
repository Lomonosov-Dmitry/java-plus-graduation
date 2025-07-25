package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.StatsHitDto;
import ru.practicum.client.StatsClient;
import ru.practicum.dal.RequestRepository;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.dto.event.enums.RequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.feign.EventClient;
import ru.practicum.feign.UserClient;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    @Autowired
    RequestRepository requestRepository;

    @Autowired
    UserClient userClient;

    @Autowired
    EventClient eventClient;

    @Autowired
    private StatsClient statsClient;

    @Transactional
    @Override
    public ParticipationRequestDto newRequest(long userId, long eventId) {
        if (requestRepository.findByUserIdAndEventId(userId, eventId).isEmpty()) {
            Long user = userClient.getUserById(userId).getId();
            EventDto eventDto = eventClient.getEvent(eventId);
            if (eventDto.getInitiator() == userId)
                throw new ConflictException("Event initiator can't make a request");
            if (eventDto.getState() != EventState.PUBLISHED)
                throw new ConflictException("Event with id = " + eventId + " is not published yet");
            if ((eventDto.getParticipantLimit() != 0) && (eventDto.getParticipantLimit() <= eventDto.getConfirmedRequests()))
                throw new ConflictException("Limit of requests reached on event with id = " + eventDto);
            Request request = new Request();
            request.setUserId(user);
            request.setEventId(eventId);
            request.setCreatedOn(LocalDateTime.now());
            if (eventDto.getParticipantLimit() != 0 && eventDto.getRequestModeration())
                request.setStatus(RequestStatus.PENDING);
            else {
                request.setStatus(RequestStatus.CONFIRMED);
                eventClient.increaseConfirmed(eventId, eventDto.getConfirmedRequests() + 1);
            }
            return RequestMapper.INSTANCE.toParticipationRequestDto(requestRepository.save(request));
        } else
            throw new ConflictException("Request from user with id = " + userId +
                    " on event with id = " + eventId + " already exists");
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id = " + requestId + " not found"));
        if (request.getUserId() != userId)
            throw new ConflictException("User with id = " + userId + " is not an initializer of request with id = " + requestId);
        requestRepository.delete(request);
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.INSTANCE.toParticipationRequestDto(request);
    }

    @Override
    public Collection<ParticipationRequestDto> findAllRequestsByUserId(long userId) {
        Long user = userClient.getUserById(userId).getId();
        Collection<ParticipationRequestDto> result = new ArrayList<>();
        result = requestRepository.findAllByUserId(userId).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList();
        return result;
    }

    @Override
    public Collection<ParticipationRequestDto> findAllRequestsByEventId(long userId, long eventId) {
        Long user = userClient.getUserById(userId).getId();
        Collection<ParticipationRequestDto> result = new ArrayList<>();
        result = requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList();
        return result;
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(long userId,
                                                               long eventId,
                                                               EventRequestStatusUpdateRequest request) {
        Long user = userClient.getUserById(userId).getId();
        EventDto eventDto = eventClient.getEvent(eventId);
        if (!eventDto.getInitiator().equals(user))
            throw new ValidationException("User with id = " + userId + " is not a initiator of event with id = " + eventId);

        Collection<Request> requests = requestRepository.findAllRequestsOnEventByIds(eventId,
                request.getRequestIds());
        int limit = eventDto.getParticipantLimit() - eventDto.getConfirmedRequests().intValue();
        int confirmed = eventDto.getConfirmedRequests().intValue();
        if (limit == 0)
            throw new ConflictException("Limit of participant reached");
        for (Request req : requests) {
            if (!req.getStatus().equals(RequestStatus.PENDING))
                throw new ConflictException("Status of the request with id = " + req.getId() + " is " + req.getStatus());
            if (request.getStatus().equals(RequestStatus.REJECTED)) {
                req.setStatus(RequestStatus.REJECTED);
            } else if (eventDto.getParticipantLimit() == 0 || !eventDto.getRequestModeration()) {
                req.setStatus(RequestStatus.CONFIRMED);
                confirmed++;
            } else if (limit == 0) {
                req.setStatus(RequestStatus.REJECTED);
            } else {
                req.setStatus(RequestStatus.CONFIRMED);
                limit--;
            }
            requestRepository.save(req);
        }
        if (eventDto.getParticipantLimit() != 0)
            eventDto.setConfirmedRequests((long) eventDto.getParticipantLimit() - limit);
        else
            eventDto.setConfirmedRequests((long) confirmed);
        eventClient.increaseConfirmed(eventId, eventDto.getConfirmedRequests());
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(requestRepository.findAllRequestsOnEventByIdsAndStatus(eventId,
                        RequestStatus.CONFIRMED,
                        request.getRequestIds()).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList());
        result.setRejectedRequests(requestRepository.findAllRequestsOnEventByIdsAndStatus(eventId,
                        RequestStatus.REJECTED,
                        request.getRequestIds()).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList());
        return result;
    }

    private void sendStats(HttpServletRequest request) {
        log.debug("save stats hit, uri = {}", request.getRequestURI());
        log.debug("save stats hit, remoteAddr = {}", request.getRemoteAddr());
        statsClient.hit(StatsHitDto.builder()
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }
}
