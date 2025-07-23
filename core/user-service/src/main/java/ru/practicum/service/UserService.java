package ru.practicum.service;

import dto.user.NewUserDto;
import dto.user.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService {

    UserDto newUser(NewUserDto dto);

    UserDto getUserById(Long userId);

    Collection<UserDto> getAllUsers(List<Long> ids, int from, int size);

    void deleteUser(long userId);
}
