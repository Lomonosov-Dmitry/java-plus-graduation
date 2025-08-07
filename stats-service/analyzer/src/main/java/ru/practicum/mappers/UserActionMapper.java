package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.UserAction;

@Mapper(componentModel = "spring")
public interface UserActionMapper {
    UserActionMapper INSTANCE = Mappers.getMapper(UserActionMapper.class);

    UserAction toAction(UserActionAvro actionAvro);

}
