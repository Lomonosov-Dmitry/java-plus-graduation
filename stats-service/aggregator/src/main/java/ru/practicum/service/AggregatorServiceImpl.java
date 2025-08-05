package ru.practicum.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.Event;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class AggregatorServiceImpl implements AggregatorService {
    private final Map<Long, Event> weightMap;
    private final Map<Long, Double> weighSumMap;
    private final Map<Long, Map<Long, Double>> minWeightsSums;
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    @Value("${kafka.topic.similarity.v1}")
    private String similarityTopic;

    public AggregatorServiceImpl(KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate) {
        this.weightMap = new HashMap<>();
        this.weighSumMap = new HashMap<>();
        this.minWeightsSums = new HashMap<>();
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void processAction(UserActionAvro actionAvro) {
        long eventId = actionAvro.getEventId();
        long userId = actionAvro.getUserId();
        double newWeight = getActionRating(actionAvro.getActionType());
        if (!weightMap.containsKey(eventId)) {
            Event newOne = new Event();
            newOne.setEventId(eventId);
            newOne.setUsersWeight(Map.of(userId, newWeight));
            weightMap.put(eventId, newOne);
            weighSumMap.put(eventId, newWeight);
            minWeightsSums.put(eventId, new HashMap<>());
        } else {
            Event event = weightMap.get(eventId);
            if (!event.getUsersWeight().containsKey(userId)) {
                event.getUsersWeight().put(userId, newWeight);
            } else {
                double weight = event.getUsersWeight().get(actionAvro.getUserId());
                if (weight < newWeight) {
                    double sum = newWeight - weight;
                    event.getUsersWeight().replace(userId, newWeight);
                    weighSumMap.replace(eventId, weight + sum);
                }
            }
        }
        for (Long eventForUpdate : weightMap.keySet()) {
            double sum = get(eventId, eventForUpdate);
            if (sum != 0) {
                double min = weightMap.get(eventForUpdate).getUsersWeight().get(userId);
                if (min > newWeight)
                    put(eventId, eventForUpdate, newWeight);
                double similarity = get(eventId, eventForUpdate) /
                        Math.sqrt(weighSumMap.get(eventId) * Math.sqrt(weighSumMap.get(eventForUpdate)));
                sendToKafka(eventId, eventForUpdate, similarity, actionAvro.getTimestamp());
            }
        }
    }

    private Double getActionRating(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    private double get(long eventA, long eventB) {
        long first  = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private void put(long eventA, long eventB, double sum) {
        long first  = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    private void sendToKafka(Long eventA, Long eventB, Double sim, Instant ts) {
        EventSimilarityAvro event = new EventSimilarityAvro();
        event.setEventAId(eventA);
        event.setEventBId(eventB);
        event.setScore(sim);
        event.setTimestamp(ts);

        kafkaTemplate.send(similarityTopic, event);
    }
}
