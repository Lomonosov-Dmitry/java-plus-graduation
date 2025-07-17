package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.practicum.Constants;
import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;
import ru.practicum.dal.StatRepository;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.StatsMapper;
import ru.practicum.model.StatItem;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatRepository statRepository;

    @Override
    public StatsHitDto saveHit(StatsHitDto hitDto) {
        StatItem statItem = statRepository.save(StatsMapper.INSTANCE.getStatItem(hitDto));

        return StatsMapper.INSTANCE.getStatsHitDto(statItem);
    }

    @Override
    public Collection<StatsViewDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        if (start == null || end == null) {
            throw new ValidationException("start and end parameters is mandatory");
        }

        LocalDateTime startDate;
        LocalDateTime endDate;

        try {
            startDate = LocalDateTime.parse(start,
                    Constants.DATE_TIME_FORMATTER);

            endDate = LocalDateTime.parse(end,
                    Constants.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            throw new ValidationException("incorrect date format. expected format = " + Constants.DATE_PATTERN);
        }

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("start date must be before end date");
        }

        Collection<StatsViewDto> stats;
        if (!CollectionUtils.isEmpty(uris)) {
            if (unique) {
                stats = statRepository.getStatsWithUrisUnique(startDate, endDate, uris);
            } else {
                stats = statRepository.getStatsWithUrisNotUnique(startDate, endDate, uris);
            }
        } else {
            if (unique) {
                stats = statRepository.getStatsWithoutUrisUnique(startDate, endDate);
            } else {
                stats = statRepository.getStatsWithoutUrisNotUnique(startDate, endDate);
            }
        }

        return stats;
    }

}
