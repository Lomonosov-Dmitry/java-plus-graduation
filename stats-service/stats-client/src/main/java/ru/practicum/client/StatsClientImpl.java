package ru.practicum.client;

import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;

import java.util.List;
import java.util.Optional;

@SpringBootConfiguration
@Slf4j
@Service
public class StatsClientImpl implements StatsClient {
    private final String getStatsServiceId;
    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;

    public StatsClientImpl(String getStatsServiceId, DiscoveryClient discoveryClient) {
        this.getStatsServiceId = getStatsServiceId;
        this.discoveryClient = discoveryClient;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public StatsHitDto hit(StatsHitDto statsHitDto) {
        log.info("save statistics for {}", statsHitDto.toString());
        try {
            return restClient.post()
                    .uri(getBaseUri() + "/hit")
                    .body(statsHitDto)
                    .retrieve()
                    .body(StatsHitDto.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public List<StatsViewDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("retrieve statistics with params: start = {}, end = {}, uris = {}, unique ={}",
                start, end, uris, unique);
        try {
            UriComponents uriComponents = UriComponentsBuilder
                    .fromUriString(getBaseUri() + "/stats")
                    .queryParam("start", start)
                    .queryParam("end", end)
                    .queryParamIfPresent("uris", Optional.ofNullable(uris))
                    .queryParam("unique", unique)
                    .encode()
                    .build();
            log.debug("uriComponents encoded {}", uriComponents.toUri());
            return restClient.get()
                    .uri(uriComponents.toUri())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StatsViewDto>>() {
                    });
        } catch (Exception e) {
            log.error(e.getMessage());
            return List.of();
        }
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances(getStatsServiceId)
                    .get(0);
        } catch (Exception exception) {
            throw new NotFoundException(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + getStatsServiceId,
                    exception
            );
        }
    }

    private String getBaseUri() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance());
        return "http://" + instance.getHost() + ":" + instance.getPort();
    }
}
