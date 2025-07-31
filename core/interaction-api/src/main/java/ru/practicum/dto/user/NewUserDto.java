package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class NewUserDto {
    @Size(min = 2, max = 250)
    @NotBlank
    String name;

    @Size(min = 6, max = 254)
    @Email
    @NotBlank
    String email;
}
