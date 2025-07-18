package ru.practicum.client.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatsClientConfiguration {

    @Value("${stats-server.name}")
    String statsServiceId;

    @Bean
    String getStatsServiceId() {
        return statsServiceId;
    }
}