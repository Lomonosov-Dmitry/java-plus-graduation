package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.client.CollectorClient;
import ru.practicum.feign.CategoryClient;
import ru.practicum.feign.EventClient;
import ru.practicum.feign.UserClient;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableDiscoveryClient
@EnableFeignClients(clients = {UserClient.class, EventClient.class})
public class RequestServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApp.class, args);
    }
}