package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    long id;

    @Column(name = "lat")
    Double lat;

    @Column(name = "lon")
    Double lon;
}
