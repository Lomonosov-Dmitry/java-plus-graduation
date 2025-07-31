package ru.practicum.feign;

import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserClient {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserDto newUser(@RequestBody @Valid NewUserDto dto);

    @GetMapping("/{userId}")
    UserDto getUserById(@PathVariable Long userId);

    @GetMapping
    Collection<UserDto> findAllUsers(@RequestParam(required = false) List<Long> ids,
                                            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                            @Positive @RequestParam(name = "size", defaultValue = "10") int size);

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(@PathVariable Long userId);
}
