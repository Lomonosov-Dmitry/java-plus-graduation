package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import dto.user.UserShortDto;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.Constants;
import ru.practicum.dto.category.CategoryDto;


import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EventShortDto {
    Long id;
    String annotation;
    CategoryDto category;
    Long confirmedRequests;

    @JsonFormat(pattern = Constants.DATE_PATTERN)
    LocalDateTime eventDate;
    Long initiator;
    Boolean paid;
    String title;
    Long views;
}
