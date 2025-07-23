package ru.practicum.dto.comment;

import dto.user.UserShortDto;
import lombok.*;
import lombok.experimental.FieldDefaults;


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
