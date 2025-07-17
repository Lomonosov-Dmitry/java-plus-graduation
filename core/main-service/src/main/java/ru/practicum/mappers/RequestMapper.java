package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.event.ParticipationRequestDto;
import ru.practicum.model.Request;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RequestMapper {
    RequestMapper INSTANCE = Mappers.getMapper(RequestMapper.class);

    Request toRequest(ParticipationRequestDto dto);

    @Mapping(target = "requester", expression = "java(request.getUserId().getId())")
    @Mapping(target = "event", expression = "java(request.getEventId().getId())")
    @Mapping(target = "created", source = "createdOn")
    ParticipationRequestDto toParticipationRequestDto(Request request);
}
