package ru.practicum.service;

import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService {

    UserDto newUser(NewUserDto dto);

    UserDto getUserById(Long userId);

    Collection<UserDto> getAllUsers(List<Long> ids, int from, int size);

    void deleteUser(long userId);
}
