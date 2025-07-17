package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class StatsHitDto {
    long id;

    @Size(max = 100)
    String app;

    @Size(max = 100)
    String uri;

    @Size(max = 100)
    String ip;

    @JsonFormat(pattern = Constants.DATE_PATTERN)
    LocalDateTime timestamp;
}
