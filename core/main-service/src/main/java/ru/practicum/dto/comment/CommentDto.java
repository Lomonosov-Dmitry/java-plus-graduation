package ru.practicum.dto.comment;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.user.UserShortDto;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    Long id;

    String message;

    UserShortDto author;
}
