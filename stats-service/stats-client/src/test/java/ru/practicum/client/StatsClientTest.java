package ru.practicum.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.Constants;
import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;
import ru.practicum.client.configuration.StatsClientConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

// решение по тестированию RestClient:
// https://www.dreaminghk.com/2024/09/14/mastering-spring-restclient-and-restclienttest-for-api-integration-and-testing/

@RestClientTest(StatsClient.class)
@Import(StatsClientConfiguration.class)
class StatsClientTest {
    private static final String PREP_POST_REQUEST = "http://localhost:9090/hit";
    private static final String PREP_GET_REQUEST = "http://localhost:9090/stats?start=2025-02-15%2015:00:00&" +
            "end=2025-02-15%2015:15:00&uris=test%20uri&unique=true";
    @Autowired
    private StatsClient statsClient;

    @Autowired
    private MockRestServiceServer mockServer;
    @Autowired
    private ObjectMapper mapper;

    private final StatsHitDto statsHitDto = StatsHitDto.builder()
            .id(1L)
            .app("test app")
            .uri("test uri")
            .ip("127.0.0.1")
            .timestamp(LocalDateTime.now())
            .build();

    private final StatsViewDto statsViewDto = StatsViewDto.builder()
            .uri("test uri")
            .app("test app")
            .hits(1L)
            .build();

    @Test
    void testHit() throws JsonProcessingException {
        mockServer
                .expect(ExpectedCount.once(), requestTo(PREP_POST_REQUEST))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(statsHitDto)));
        StatsHitDto result = statsClient.hit(statsHitDto);
        mockServer.verify();
        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getApp(), equalTo("test app"));
        assertThat(result.getUri(), equalTo("test uri"));
        assertThat(result.getIp(), equalTo("127.0.0.1"));
        assertThat(result.getTimestamp().format(Constants.DATE_TIME_FORMATTER),
                equalTo(statsHitDto.getTimestamp().format(Constants.DATE_TIME_FORMATTER)));
    }

    @Test
    void testGetStats() throws JsonProcessingException {
        mockServer
                .expect(requestTo(PREP_GET_REQUEST))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(List.of(statsViewDto))));
        var result = statsClient.getStats("2025-02-15 15:00:00",
                "2025-02-15 15:15:00", List.of("test uri"), true);
        mockServer.verify();
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0).getUri(), equalTo("test uri"));
        assertThat(result.get(0).getApp(), equalTo("test app"));
        assertThat(result.get(0).getHits(), equalTo(1L));
    }
}