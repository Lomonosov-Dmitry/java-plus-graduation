package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.practicum.model.EventSimitarity;

@Mapper(componentModel = "spring")
public interface EventSimilarityMapper {
    EventSimilarityMapper INSTANCE = Mappers.getMapper(EventSimilarityMapper.class);

    EventSimitarity toSimilarity(EventSimilarityAvro eventSimilarityAvro);

    static RecommendedEventProto toRecommended(EventSimitarity eventSimitarity) {
        return RecommendedEventProto.newBuilder()
                .setEventId(eventSimitarity.getEventBId())
                .setScore(eventSimitarity.getScore())
                .build();
    }
}
