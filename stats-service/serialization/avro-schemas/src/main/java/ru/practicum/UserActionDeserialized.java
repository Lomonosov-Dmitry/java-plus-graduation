package ru.practicum;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserActionDeserialized extends BaseAvroDeserializer<UserActionAvro> {
    public UserActionDeserialized() {
        super(UserActionAvro.getClassSchema());
    }
}
