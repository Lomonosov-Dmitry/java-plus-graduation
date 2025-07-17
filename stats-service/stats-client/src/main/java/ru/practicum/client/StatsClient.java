package ru.practicum.client;

import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;

import java.util.List;

public interface StatsClient {
    StatsHitDto hit(StatsHitDto statsHitDto);

    List<StatsViewDto> getStats(String start, String end, List<String> uris, Boolean unique);
}
