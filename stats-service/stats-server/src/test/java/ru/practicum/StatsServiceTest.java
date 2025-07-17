package ru.practicum;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dal.StatRepository;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.StatsMapper;
import ru.practicum.model.StatItem;
import ru.practicum.service.StatsServiceImpl;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatsServiceTest {
    @InjectMocks
    StatsServiceImpl statsService;

    @Mock
    StatRepository statRepository;

    StatsHitDto statsHitDto;
    StatsViewDto statsViewDtoUnique;
    StatsViewDto statsViewDtoNotUnique;
    String start;
    String end;
    String uri;

    @BeforeEach
    public void dataPreparation() {
        String app = "ewm-main-service";
        uri = "/events/1";
        String ip = "192.163.0.1";
        LocalDateTime timestamp = LocalDateTime.now();
        start = "2022-09-06 11:00:23";
        end = "2022-09-07 11:00:23";

        statsHitDto = StatsHitDto.builder()
                .id(1L)
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp)
                .build();

        statsViewDtoUnique = StatsViewDto.builder()
                .app(app)
                .uri(uri)
                .hits(1L)
                .build();

        statsViewDtoNotUnique = StatsViewDto.builder()
                .app(app)
                .uri(uri)
                .hits(2L)
                .build();
    }

    @Test
    public void saveHitTest() {
        when(statRepository.save(Mockito.any(StatItem.class)))
                .thenReturn(StatsMapper.INSTANCE.getStatItem(statsHitDto));

        StatsHitDto result = statsService.saveHit(statsHitDto);

        assertEquals(statsHitDto, result);

        verify(statRepository, Mockito.times(1)).save(Mockito.any(StatItem.class));
    }

    @Test
    public void getStatsWithUrisUnique() {
        when(statRepository.getStatsWithUrisUnique(Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(List.class)))
                .thenReturn(List.of(statsViewDtoUnique));

        Collection<StatsViewDto> result = statsService.getStats(start, end, List.of(uri), true);

        assertArrayEquals(List.of(statsViewDtoUnique).toArray(), result.toArray());

        verify(statRepository, Mockito.times(1))
                .getStatsWithUrisUnique(Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(List.class));
    }

    @Test
    public void getStatsWithUrisNotUnique() {
        when(statRepository.getStatsWithUrisNotUnique(Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(List.class)))
                .thenReturn(List.of(statsViewDtoNotUnique));

        Collection<StatsViewDto> result = statsService.getStats(start, end, List.of(uri), false);

        assertArrayEquals(List.of(statsViewDtoNotUnique).toArray(), result.toArray());

        verify(statRepository, Mockito.times(1))
                .getStatsWithUrisNotUnique(Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(List.class));
    }

    @Test
    public void getStatsWithoutUrisNotUnique() {
        when(statRepository.getStatsWithoutUrisNotUnique(Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(statsViewDtoNotUnique));

        Collection<StatsViewDto> result = statsService.getStats(start, end, null, false);

        assertArrayEquals(List.of(statsViewDtoNotUnique).toArray(), result.toArray());

        verify(statRepository, Mockito.times(1))
                .getStatsWithoutUrisNotUnique(Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class));
    }

    @Test
    public void getStatsWithoutUrisUnique() {
        when(statRepository.getStatsWithoutUrisUnique(Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(statsViewDtoUnique));

        Collection<StatsViewDto> result = statsService.getStats(start, end, null, true);

        assertArrayEquals(List.of(statsViewDtoUnique).toArray(), result.toArray());

        verify(statRepository, Mockito.times(1))
                .getStatsWithoutUrisUnique(Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class));
    }

    @Test
    public void getStatsWithNullStart() {
        assertThrows(ValidationException.class,
                () -> statsService.getStats(null, end, null, false));

        verifyNoInteractions(statRepository);
    }

    @Test
    public void getStatsWithNullEnd() {
        assertThrows(ValidationException.class,
                () -> statsService.getStats(start, null, null, false));

        verifyNoInteractions(statRepository);
    }

    @Test
    public void getStatsWithIncorrectDateFormat() {
        assertThrows(ValidationException.class,
                () -> statsService.getStats("2022-09-06Т11:00:23", end, null, false));

        verifyNoInteractions(statRepository);
    }

}
