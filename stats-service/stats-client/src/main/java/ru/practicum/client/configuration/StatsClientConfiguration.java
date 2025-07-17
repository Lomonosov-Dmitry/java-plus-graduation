package ru.practicum.client.configuration;

import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class StatsClientConfiguration {

    @Value("${stats-server.name}")
    String statsServiceId;

    @Bean
    String getStatsServiceId() {
        return statsServiceId;
    }

    //@Autowired
    //private DiscoveryClient discoveryClient;

    /*@Bean
    RestClient restClient(RestClient.Builder builder) {

        /*RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        ServiceInstance instance = retryTemplate.execute(cxt -> {
            try {
                return discoveryClient
                        .getInstances(statsServiceId)
                        .get(0);
            } catch (Exception exception) {
                throw new NotFoundException(
                        "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId,
                        exception
                );
            }
        });

        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return builder
                .uriBuilderFactory(defaultUriBuilderFactory)
                .build();
    }

    /*private ServiceInstance getInstance() {
        try {
            DiscoveryClient discoveryClient = null;
            return discoveryClient
                    .getInstances(statsServiceId)
                    .get(0);
        } catch (Exception exception) {
            throw new NotFoundException(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId,
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
    }*/
}