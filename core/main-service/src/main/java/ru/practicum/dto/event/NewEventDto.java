package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
public class NewEventDto {
    @Size(min = 20, max = 2000)
    @NotBlank
    String annotation;

    CategoryDto category;

    @Size(min = 20, max = 7000)
    @NotBlank
    String description;

    @JsonFormat(pattern = Constants.DATE_PATTERN)
    LocalDateTime eventDate;

    @NotNull
    @Valid
    LocationDto location;

    //set default value = false
    Boolean paid = false;

    //set default value = 0
    @PositiveOrZero
    Integer participantLimit = 0;

    //set default value = true
    Boolean requestModeration = true;

    @Size(min = 3, max = 120)
    @NotBlank
    String title;
}
