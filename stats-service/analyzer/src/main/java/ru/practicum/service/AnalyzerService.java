package ru.practicum.service;

import io.grpc.stub.StreamObserver;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.event.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.practicum.grpc.stats.event.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.event.UserPredictionsRequestProto;

public interface AnalyzerService {

    void saveUserAction(UserActionAvro actionAvro);

    void saveEventSimilarity(EventSimilarityAvro eventSimilarityAvro);

    void getRecommendationsForUser(UserPredictionsRequestProto request,
                                   StreamObserver<RecommendedEventProto> responseObserver);

    void getSimilarEvents(SimilarEventsRequestProto request,
                          StreamObserver<RecommendedEventProto> responseObserver);

    void getInteractionsCount(InteractionsCountRequestProto request,
                              StreamObserver<RecommendedEventProto> responseObserver);
}
