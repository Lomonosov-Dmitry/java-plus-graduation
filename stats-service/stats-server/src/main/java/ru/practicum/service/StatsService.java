package ru.practicum.service;

import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;

import java.util.Collection;
import java.util.List;

public interface StatsService {

    StatsHitDto saveHit(StatsHitDto hitDto);

    Collection<StatsViewDto> getStats(String start,
                                      String end,
                                      List<String> uris,
                                      Boolean unique);
}
