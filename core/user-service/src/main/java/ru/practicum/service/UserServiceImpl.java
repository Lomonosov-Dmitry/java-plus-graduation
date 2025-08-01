package ru.practicum.service;

import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dal.UserRepository;
import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    @Override
    public UserDto newUser(NewUserDto dto) {
        if (userRepository.findUserByEmail(dto.getEmail()).isPresent())
            throw new ConflictException("User with email %s exists!".formatted(dto.getEmail()));
        User user = userRepository.save(userMapper.getUser(dto));
        log.info("Added new user with id {}", user.getId());
        return UserMapper.INSTANCE.getUserDto(user);
    }

    @Override
    public Collection<UserDto> getAllUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.ASC, "id"));
        return userRepository.findAllByFilter(ids, pageable).stream()
                .map(UserMapper.INSTANCE::getUserDto)
                .toList();
    }

    @Transactional
    @Override
    public void deleteUser(long userId) {
        if (getUserById(userId) == null)
            throw new NotFoundException("User with id %d not found!".formatted(userId));
        userRepository.deleteById(userId);
        log.info("User {} deleted", userId);
    }

    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.INSTANCE.getUserDto(userRepository.findById(userId).orElse(null));
    }
}
