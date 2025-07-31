package ru.practicum.dto.category;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CategoryDto {
    Long id;
    String name;

    public CategoryDto(Long id) {
        this.id = id;
    }
}
