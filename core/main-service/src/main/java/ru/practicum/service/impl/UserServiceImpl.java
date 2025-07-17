package ru.practicum.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dal.UserRepository;
import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mappers.UserMapper;
import ru.practicum.model.User;
import ru.practicum.service.UserService;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Autowired
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserDto newUser(NewUserDto dto) {
        User user = userRepository.save(userMapper.getUser(dto));
        log.info("Added new user with id {}", user.getId());
        return userMapper.getUserDto(user);
    }

    @Override
    public Collection<UserDto> getAllUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.ASC, "id"));
        return userRepository.findAllByFilter(ids, pageable).stream()
                .map(userMapper::getUserDto)
                .toList();
    }

    @Transactional
    @Override
    public void deleteUser(long userId) {
        checkUser(userId);
        userRepository.deleteById(userId);
        log.info("User {} deleted", userId);
    }

    private void checkUser(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %d not found!".formatted(userId)));
    }
}
