package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import dto.user.NewUserDto;
import dto.user.UserDto;
import dto.user.UserShortDto;
import ru.practicum.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User getUser(NewUserDto newUserDto);

    UserDto getUserDto(User user);

    UserShortDto getUserShortDto(User user);
}
