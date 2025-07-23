package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import dto.category.CategoryDto;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.Constants;


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
    Long category;
    Long confirmedRequests;

    @JsonFormat(pattern = Constants.DATE_PATTERN)
    LocalDateTime eventDate;
    Long initiator;
    Boolean paid;
    String title;
    Long views;
}
