package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsViewDtoJsonTest {
    private final JacksonTester<StatsViewDto> json;

    @Test
    public void statsViewDtoTest() throws Exception {
        String app = "ewm-main-service";
        String uri = "/events/1";
        long hits = 10L;

        StatsViewDto statsViewDto = StatsViewDto.builder()
                .app(app)
                .uri(uri)
                .hits(hits)
                .build();

        JsonContent<StatsViewDto> content = json.write(statsViewDto);

        assertThat(content).extractingJsonPathStringValue("$.app").isEqualTo(app);
        assertThat(content).extractingJsonPathStringValue("$.uri").isEqualTo(uri);
        assertThat(content).extractingJsonPathNumberValue("$.hits").isEqualTo(10);
    }
}
