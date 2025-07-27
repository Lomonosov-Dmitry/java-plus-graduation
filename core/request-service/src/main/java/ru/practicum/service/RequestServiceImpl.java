package ru.practicum.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
import java.util.Collection;

@Slf4j
@Service
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;

    private final UserClient userClient;

    private final EventClient eventClient;

    public RequestServiceImpl(RequestRepository requestRepository, UserClient userClient, EventClient eventClient) {
        this.requestRepository = requestRepository;
        this.userClient = userClient;
        this.eventClient = eventClient;
    }

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
        return requestRepository.findAllByUserId(userId).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList();
    }

    @Override
    public Collection<ParticipationRequestDto> findAllRequestsByEventId(long userId, long eventId) {
        return  requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList();
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
        }
        requestRepository.saveAll(requests);
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
}
