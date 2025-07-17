package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.Constants;
import ru.practicum.dto.event.enums.RequestStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ParticipationRequestDto {
    Long id;
    Long requester;
    Long event;

    @JsonFormat(pattern = Constants.DATE_PATTERN)
    LocalDateTime created;
    RequestStatus status;
}
