package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.controller.StatsController;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
public class ControllerTest {
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    StatsService statsService;

    @Autowired
    MockMvc mockMvc;

    @Test
    public void saveHitTest() throws Exception {
        String app = "ewm-main-service";
        String uri = "/events/1";
        String ip = "192.163.0.1";
        LocalDateTime timestamp = LocalDateTime.now();

        StatsHitDto statsHitDto = StatsHitDto.builder()
                .id(1L)
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp)
                .build();

        when(statsService.saveHit(Mockito.any(StatsHitDto.class)))
                .thenReturn(statsHitDto);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statsHitDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(statsHitDto.getId()), Long.class))
                .andExpect(jsonPath("$.app", is(app)))
                .andExpect(jsonPath("$.uri", is(uri)))
                .andExpect(jsonPath("$.ip", is(ip)))
                .andExpect(jsonPath("$.timestamp", is(timestamp.format(Constants.DATE_TIME_FORMATTER))));

        verify(statsService, Mockito.times(1))
                .saveHit(Mockito.any(StatsHitDto.class));
    }

    @Test
    public void getStatTest() throws Exception {
        String app = "ewm-main-service";
        String uri = "/events/1";
        long hits = 10L;

        StatsViewDto statsViewDto = StatsViewDto.builder()
                .app(app)
                .uri(uri)
                .hits(hits)
                .build();

        when(statsService.getStats(Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(List.class),
                Mockito.anyBoolean()))
                .thenReturn(List.of(statsViewDto));

        mockMvc.perform(get("/stats")
                        .param("start", LocalDateTime.now().format(Constants.DATE_TIME_FORMATTER))
                        .param("end", LocalDateTime.now().format(Constants.DATE_TIME_FORMATTER))
                        .param("uris", uri, uri)
                        .param("unique", Boolean.toString(true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].app", is(app)))
                .andExpect(jsonPath("$.[0].uri", is(uri)))
                .andExpect(jsonPath("$.[0].hits", is(hits), Long.class));


        verify(statsService, Mockito.times(1))
                .getStats(Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.any(List.class),
                        Mockito.anyBoolean());
    }
}
