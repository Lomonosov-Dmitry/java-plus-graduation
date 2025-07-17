package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.Event;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    Event getEvent(NewEventDto newEventDto);

    EventDto getEventDto(Event event);

    @Mapping(target = "comments", source = "commentDtos")
    EventDto getEventDtoWithComments(Event event, List<CommentDto> commentDtos);

    EventShortDto getEventShortDto(Event event);
}
