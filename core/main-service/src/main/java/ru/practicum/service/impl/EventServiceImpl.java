package ru.practicum.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.Constants;
import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;
import ru.practicum.client.StatsClient;
import ru.practicum.dal.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.enums.EventActionStateAdmin;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.dto.event.enums.RequestStatus;
import ru.practicum.dto.event.enums.SortingOptions;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.CommentMapper;
import ru.practicum.mappers.EventMapper;
import ru.practicum.mappers.EventUpdater;
import ru.practicum.mappers.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventServiceImpl implements EventService {
    @Autowired
    LocationRepository locationRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    private StatsClient statsClient;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public EventDto save(long userId, NewEventDto newEventDto) {
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (newEventDto.getEventDate() != null && newEventDto.getEventDate().isBefore(validDate)) {
            throw new ValidationException("Event date should be after two hours after now");
        }

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));

        Event event = EventMapper.INSTANCE.getEvent(newEventDto);

        Location location = locationRepository.save(event.getLocation());

        event.setInitiator(initiator);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(location);

        return EventMapper.INSTANCE.getEventDto(eventRepository.save(event));
    }

    @Override
    public EventDto findEvent(long eventId, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));

        return EventMapper.INSTANCE.getEventDto(
                eventRepository.findByIdAndUserId(eventId, userId)
                        .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId))
        );
    }

    @Override
    public List<EventShortDto> findEvents(long userId, int from, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));

        Pageable pageable = PageRequest.of(from, size);

        return eventRepository.findByUserId(userId, pageable).stream()
                .map(EventMapper.INSTANCE::getEventShortDto)
                .toList();
    }

    @Transactional
    @Override
    public EventDto updateEvent(long eventId, long userId, UpdateEventUserRequest updateEventUserRequest) {
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (updateEventUserRequest.getEventDate() != null && updateEventUserRequest.getEventDate().isBefore(validDate)) {
            throw new ValidationException("Event date should be after two hours after now");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));

        Event baseEvent = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));

        if (baseEvent.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot updated published event");
        }

        EventUpdater.INSTANCE.update(baseEvent, updateEventUserRequest);

        return EventMapper.INSTANCE.getEventDto(baseEvent);
    }

    @Transactional
    @Override
    public EventDto updateEventAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (updateEventAdminRequest.getEventDate() != null && updateEventAdminRequest.getEventDate().isBefore(validDate)) {
            throw new ValidationException("Event date should be after two hours after now");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));

        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction() == EventActionStateAdmin.REJECT_EVENT &&
                    event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Cannot cancel published event");
            }

            if (updateEventAdminRequest.getStateAction() == EventActionStateAdmin.PUBLISH_EVENT &&
                    event.getState() != EventState.PENDING) {
                throw new ConflictException("Cannot publish event not in status Pending");
            }

            if (updateEventAdminRequest.getStateAction() == EventActionStateAdmin.PUBLISH_EVENT &&
                    event.getEventDate().minusHours(1L).isBefore(LocalDateTime.now())) {
                throw new ConflictException("Cannot publish event less than 1 hour before start");
            }
        }

        EventUpdater.INSTANCE.update(event, updateEventAdminRequest);

        if (event.getState() == EventState.PUBLISHED) {
            event.setPublishedOn(LocalDateTime.now());
            event.setConfirmedRequests(0L);
            event.setViews(0L);
        }
        return EventMapper.INSTANCE.getEventDto(event);
    }

    @Override
    public List<EventDto> findEventsByFilter(List<Long> users,
                                             List<String> states,
                                             List<Long> categories,
                                             String rangeStart,
                                             String rangeEnd,
                                             int from,
                                             int size) {
        Pageable pageable = PageRequest.of(from, size);

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (rangeStart != null) {
            start = LocalDateTime.parse(rangeStart, Constants.DATE_TIME_FORMATTER);
        }

        if (rangeEnd != null) {
            end = LocalDateTime.parse(rangeEnd, Constants.DATE_TIME_FORMATTER);
        }

        return eventRepository.findAllByFilter(users, states, categories, start, end, pageable).stream()
                .map(EventMapper.INSTANCE::getEventDto)
                .toList();
    }

    @Override
    public List<EventShortDto> findEventsByFilterPublic(String text, List<Long> categories, Boolean paid,
                                                        String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                                        SortingOptions sortingOptions, int from, int size,
                                                        HttpServletRequest request) {
        Pageable pageable;
        if (sortingOptions != null) {
            String sort = sortingOptions == SortingOptions.EVENT_DATE ? "eventDate" : "views";
            pageable = PageRequest.of(from, size, Sort.by(sort).descending());
        } else {
            pageable = PageRequest.of(from, size);
        }

        LocalDateTime start;
        LocalDateTime end = null;

        if (rangeStart != null) {
            start = LocalDateTime.parse(rangeStart, Constants.DATE_TIME_FORMATTER);
        } else {
            start = LocalDateTime.now();
        }

        if (rangeEnd != null) {
            end = LocalDateTime.parse(rangeEnd, Constants.DATE_TIME_FORMATTER);
            if (end.isBefore(start)) {
                throw new ValidationException("End is before start");
            }
        }

        sendStats(request);

        List<Event> events = eventRepository.findAllByFilterPublic(text, categories, paid, start, end, onlyAvailable,
                EventState.PUBLISHED, pageable);

        List<String> uris = events.stream()
                .map(x -> "/event/" + x.getId())
                .toList();

        String startStatsDate = events.stream()
                .map(Event::getPublishedOn)
                .min(LocalDateTime::compareTo).get().format(Constants.DATE_TIME_FORMATTER);
        String endStatsDate = LocalDateTime.now().format(Constants.DATE_TIME_FORMATTER);

        List<StatsViewDto> statViews = statsClient.getStats(startStatsDate, endStatsDate, uris, false);
        Map<String, Long> eventViews = statViews.stream()
                .collect(Collectors.toMap(StatsViewDto::getUri, StatsViewDto::getHits));
        eventViews.forEach((uri, hits) -> {
            String[] uriSplit = "/".split(uri);
            long partUri = Long.parseLong(uriSplit[uriSplit.length - 1]);
            events.stream()
                    .filter(x -> x.getId() == partUri)
                    .findFirst()
                    .ifPresent(x -> x.setViews(hits));
        });
        eventRepository.saveAll(events);
        return events.stream()
                .map(EventMapper.INSTANCE::getEventShortDto)
                .toList();
    }

    @Override
    public EventDto findEventPublic(long eventId, HttpServletRequest request) {
        Event baseEvent = eventRepository.findByIdAndStatus(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("published event is not found with id = " + eventId));
        sendStats(request);
        List<StatsViewDto> views = statsClient.getStats(baseEvent.getPublishedOn()
                        .format(Constants.DATE_TIME_FORMATTER),
                LocalDateTime.now().format(Constants.DATE_TIME_FORMATTER),
                List.of(request.getRequestURI()),
                true);
        log.debug("received from stats client list of StatsViewDto: {}", views);
        baseEvent.setViews(views.get(0).getHits());
        eventRepository.save(baseEvent);
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        if (!comments.isEmpty()) {
            return EventMapper.INSTANCE.getEventDtoWithComments(baseEvent, commentMapper.toCommentDtoList(comments));
        }
        return EventMapper.INSTANCE.getEventDto(baseEvent);
    }

    @Transactional
    @Override
    public ParticipationRequestDto newRequest(long userId, long eventId) {
        if (requestRepository.findByUserIdAndEventId(userId, eventId).isEmpty()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));
            if (event.getInitiator().getId() == userId)
                throw new ConflictException("Event initiator can't make a request");
            if (event.getState() != EventState.PUBLISHED)
                throw new ConflictException("Event with id = " + eventId + " is not published yet");
            if ((event.getParticipantLimit() != 0) && (event.getParticipantLimit() <= event.getConfirmedRequests()))
                throw new ConflictException("Limit of requests reached on event with id = " + event);
            Request request = new Request();
            request.setUserId(user);
            request.setEventId(event);
            request.setCreatedOn(LocalDateTime.now());
            if (event.getParticipantLimit() != 0 && event.getRequestModeration())
                request.setStatus(RequestStatus.PENDING);
            else {
                request.setStatus(RequestStatus.CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                eventRepository.save(event);
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
        if (request.getUserId().getId() != userId)
            throw new ConflictException("User with id = " + userId + " is not an initializer of request with id = " + requestId);
        requestRepository.delete(request);
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.INSTANCE.toParticipationRequestDto(request);
    }

    @Override
    public Collection<ParticipationRequestDto> findAllRequestsByUserId(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));
        Collection<ParticipationRequestDto> result = new ArrayList<>();
        result = requestRepository.findAllByUserId(userId).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList();
        return result;
    }

    @Override
    public Collection<ParticipationRequestDto> findAllRequestsByEventId(long userId, long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));
        if (!event.getInitiator().equals(user))
            throw new ValidationException("User with id = " + userId + " is not a initiator of event with id = " + eventId);

        Collection<Request> requests = requestRepository.findAllRequestsOnEventByIds(eventId,
                request.getRequestIds());
        int limit = event.getParticipantLimit() - event.getConfirmedRequests().intValue();
        int confirmed = event.getConfirmedRequests().intValue();
        if (limit == 0)
            throw new ConflictException("Limit of participant reached");
        for (Request req : requests) {
            if (!req.getStatus().equals(RequestStatus.PENDING))
                throw new ConflictException("Status of the request with id = " + req.getId() + " is " + req.getStatus());
            if (request.getStatus().equals(RequestStatus.REJECTED)) {
                req.setStatus(RequestStatus.REJECTED);
            } else if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
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
        if (event.getParticipantLimit() != 0)
            event.setConfirmedRequests((long) event.getParticipantLimit() - limit);
        else
            event.setConfirmedRequests((long) confirmed);
        eventRepository.save(event);
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
