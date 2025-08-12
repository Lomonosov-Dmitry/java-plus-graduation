package ru.practicum.service.impl;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import ru.practicum.client.AnalyzerClient;
import ru.practicum.client.CollectorClient;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.UserClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.Constants;
import ru.practicum.dal.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.enums.EventActionStateAdmin;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.dto.event.enums.SortingOptions;
import ru.practicum.exception.ValidationException;
import ru.practicum.grpc.stats.event.*;
import ru.practicum.mappers.CommentMapper;
import ru.practicum.mappers.EventMapper;
import ru.practicum.mappers.EventUpdater;
import ru.practicum.model.*;
import ru.practicum.service.EventService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    @Autowired
    LocationRepository locationRepository;

    @Autowired
    UserClient userClient;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private final AnalyzerClient analyzerClient;

    @Autowired
    private final CollectorClient collectorClient;

    @Override
    public EventDto save(long userId, NewEventDto newEventDto) {
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (newEventDto.getEventDate() != null && newEventDto.getEventDate().isBefore(validDate)) {
            throw new ValidationException("Event date should be after two hours after now");
        }

        Long initiator = userClient.getUserById(userId).getId();

        Event event = EventMapper.INSTANCE.getEvent(newEventDto);

        Location location = locationRepository.save(event.getLocation());

        event.setInitiator(initiator);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(location);

        return EventMapper.INSTANCE.getEventDto(eventRepository.save(event));
    }

    @Override
    public EventDto findEventById(long eventId) {
        return EventMapper.INSTANCE.getEventDto(
                eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId)));
    }

    @Override
    public EventDto findEvent(long eventId, long userId) {
        Long user = userClient.getUserById(userId).getId();

        return EventMapper.INSTANCE.getEventDto(
                eventRepository.findByIdAndUserId(eventId, userId)
                        .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId))
        );
    }

    @Override
    public List<EventShortDto> findEvents(long userId, int from, int size) {
        Long user = userClient.getUserById(userId).getId();

        Pageable pageable = PageRequest.of(from, size);

        return eventRepository.findByUserId(userId, pageable).stream()
                .map(EventMapper.INSTANCE::getEventShortDto)
                .toList();
    }

    @Override
    public EventDto increaseConfirmed(long eventId, long quantity) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));
        event.setConfirmedRequests(quantity);
        return EventMapper.INSTANCE.getEventDto(eventRepository.save(event));
    }

    @Override
    public Boolean categoryCheck(long categoryId) {
        return eventRepository.findAllByCategory(categoryId).isEmpty();
    }

    @Transactional
    @Override
    public EventDto updateEvent(long eventId, long userId, UpdateEventUserRequest updateEventUserRequest) {
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (updateEventUserRequest.getEventDate() != null && updateEventUserRequest.getEventDate().isBefore(validDate)) {
            throw new ValidationException("Event date should be after two hours after now");
        }

        Long user = userClient.getUserById(userId).getId();

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
            event.setRating(0.0);
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
            String sort = sortingOptions == SortingOptions.EVENT_DATE ? "eventDate" : "rating";//"views";
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

        List<Event> events = eventRepository.findAllByFilterPublic(text, categories, paid, start, end, onlyAvailable,
                EventState.PUBLISHED, pageable);

        for (Event event : events) {
            event.setRating(getEventRating(event.getId()));
        }
        /*List<String> uris = events.stream()
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
        });*/
        eventRepository.saveAll(events);
        return events.stream()
                .map(EventMapper.INSTANCE::getEventShortDto)
                .toList();
    }

    @Override
    public EventDto findEventPublic(long eventId, long userId) {
        Event baseEvent = eventRepository.findByIdAndStatus(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("published event is not found with id = " + eventId));
        sendStats(userId, eventId);
        /*List<StatsViewDto> views = statsClient.getStats(baseEvent.getPublishedOn()
                        .format(Constants.DATE_TIME_FORMATTER),
                LocalDateTime.now().format(Constants.DATE_TIME_FORMATTER),
                List.of(request.getRequestURI()),
                true);
        log.debug("received from stats client list of StatsViewDto: {}", views);
        baseEvent.setViews(views.get(0).getHits());*/
        baseEvent.setRating(getEventRating(baseEvent.getId()));
        eventRepository.save(baseEvent);
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        if (!comments.isEmpty()) {
            return EventMapper.INSTANCE.getEventDtoWithComments(baseEvent, commentMapper.toCommentDtoList(comments));
        }
        return EventMapper.INSTANCE.getEventDto(baseEvent);
    }

    @Override
    public List<EventDto> getRecommendations(long userId) {
        List<EventDto> dto = new ArrayList<>();
        List<RecommendedEventProto> proto = analyzerClient.getRecommendationsForUser(UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(5)
                .build());
        if (proto != null) {
            for (RecommendedEventProto recommendedEventProto : proto) {
                dto.add(EventMapper.INSTANCE.getEventDto(eventRepository.findById(recommendedEventProto.getEventId()).get()));
            }
        }
        return dto;
    }

    @Override
    public void addLike(long eventId, long userId) {
        log.debug("Сохраняем лайк от пользователя = {}", userId);
        collectorClient.newUserAction(UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_LIKE)
                .setTimestamp(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano()).build())
                .build());
    }

    private void sendStats(long userId, long eventId) {
        log.debug("Сохраняем просмотр от пользователя = {}", userId);
        collectorClient.newUserAction(UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_VIEW)
                .setTimestamp(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano()).build())
                .build());
    }

    private double getEventRating(long eventId) {
        return analyzerClient.getInteractionsCount(InteractionsCountRequestProto.newBuilder()
                .setEventId(eventId)
                .build()).get(0).getScore();
    }
}
