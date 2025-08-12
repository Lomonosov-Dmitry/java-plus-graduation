package ru.practicum.service;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dal.EventSimilarityRepository;
import ru.practicum.dal.UserActionsRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.event.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.practicum.grpc.stats.event.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.event.UserPredictionsRequestProto;
import ru.practicum.mappers.EventSimilarityMapper;
import ru.practicum.model.UserAction;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyzerServiceImpl implements AnalyzerService {
    private final UserActionsRepository userActionsRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    @Override
    public void saveUserAction(UserActionAvro actionAvro) {
        UserAction userAction = new UserAction();
        userAction.setUserId(actionAvro.getUserId());
        userAction.setEventId(actionAvro.getEventId());
        userAction.setWeight(getActionRating(actionAvro.getActionType()));
        userAction.setInteractAt(LocalDateTime.from(actionAvro.getTimestamp()));
        userActionsRepository.save(userAction);
    }

    @Override
    public void saveEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        eventSimilarityRepository.save(EventSimilarityMapper.INSTANCE.toSimilarity(eventSimilarityAvro));
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        List<RecommendedEventProto> proto = eventSimilarityRepository.
                findRecommendationsForUser(request.getUserId(), request.getMaxResults()).stream()
                .map(EventSimilarityMapper::toRecommended)
                .toList();
        sendResponse(proto, responseObserver);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        List<RecommendedEventProto> proto = eventSimilarityRepository
                .findSimilarityEvents(request.getEventId(), request.getUserId(), request.getMaxResults()).stream()
                .map(EventSimilarityMapper::toRecommended)
                .toList();
        sendResponse(proto, responseObserver);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        List<RecommendedEventProto> proto = userActionsRepository.findInteractionsCount(request.getEventId());
        sendResponse(proto, responseObserver);
    }

    private void sendResponse(List<RecommendedEventProto> response, StreamObserver<RecommendedEventProto> responseObserver) {
        response.forEach(responseObserver::onNext);
    }

    private Double getActionRating(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
