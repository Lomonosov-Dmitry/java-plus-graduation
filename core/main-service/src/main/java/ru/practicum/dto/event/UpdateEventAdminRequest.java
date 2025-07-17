package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.Constants;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.enums.EventActionStateAdmin;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000)
    String annotation;

    CategoryDto category;

    @Size(min = 20, max = 7000)
    String description;

    @JsonFormat(pattern = Constants.DATE_PATTERN)
    LocalDateTime eventDate;

    @Valid
    LocationDto location;

    Boolean paid;

    @PositiveOrZero
    Integer participantLimit;

    Boolean requestModeration;

    EventActionStateAdmin stateAction;

    @Size(min = 3, max = 120)
    String title;
}
