package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsHitDtoJsonTest {
    private final JacksonTester<StatsHitDto> json;

    @Test
    public void statsHitDtoTest() throws Exception {
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

        JsonContent<StatsHitDto> content = json.write(statsHitDto);

        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(content).extractingJsonPathStringValue("$.app").isEqualTo(app);
        assertThat(content).extractingJsonPathStringValue("$.uri").isEqualTo(uri);
        assertThat(content).extractingJsonPathStringValue("$.ip").isEqualTo(ip);
        assertThat(content).extractingJsonPathStringValue("$.timestamp")
                .isEqualTo(timestamp.format(Constants.DATE_TIME_FORMATTER));
    }
}
