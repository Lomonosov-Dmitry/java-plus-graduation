package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.event.ActionTypeProto;
import ru.practicum.grpc.stats.event.UserActionProto;

import java.time.Instant;

@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {
    @Value("${collector.topic.stats.v1}")
    private String USER_ACTION_TOPIC;

    private final KafkaTemplate<String, UserActionAvro> kafkaTemplate;

    @Override
    public void newUserAction(UserActionProto actionProto) {
        UserActionAvro actionAvro = new UserActionAvro();
        actionAvro.setEventId(actionProto.getEventId());
        actionAvro.setUserId(actionAvro.getUserId());
        actionAvro.setActionType(getAvroType(actionProto.getActionType()));
        actionAvro.setTimestamp(Instant.now());
        kafkaTemplate.send(USER_ACTION_TOPIC, actionAvro);
    }

    private ActionTypeAvro getAvroType(ActionTypeProto proto) {
        return switch (proto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            default -> throw new IllegalArgumentException("Неизвестное действие: " + proto);
        };
    }
}
