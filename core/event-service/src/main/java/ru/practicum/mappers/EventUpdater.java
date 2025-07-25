package ru.practicum.mappers;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.enums.EventActionState;
import ru.practicum.dto.event.enums.EventActionStateAdmin;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.model.Event;

@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface EventUpdater {
    EventUpdater INSTANCE = Mappers.getMapper(EventUpdater.class);

    @Mapping(target = "state", source = "stateAction")
    void update(@MappingTarget Event baseEvent, UpdateEventUserRequest updateEventUserRequest);

    @Mapping(target = "state", source = "stateAction")
    void update(@MappingTarget Event baseEvent, UpdateEventAdminRequest updateEventAdminRequest);

    @ValueMapping(target = "PENDING", source = "SEND_TO_REVIEW")
    @ValueMapping(target = "CANCELED", source = "CANCEL_REVIEW")
    EventState toEventState(EventActionState eventActionState);

    @ValueMapping(target = "PUBLISHED", source = "PUBLISH_EVENT")
    @ValueMapping(target = "CANCELED", source = "REJECT_EVENT")
    EventState toEventState(EventActionStateAdmin eventActionStateAdmin);
}
