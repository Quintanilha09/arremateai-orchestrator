package com.arremateai.orchestrator.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient propertyCatalogWebClient(
            @Value("${services.property-catalog.url}") String baseUrl) {
        return criarWebClient(baseUrl, 3);
    }

    @Bean
    public WebClient userProfileWebClient(
            @Value("${services.user-profile.url}") String baseUrl) {
        return criarWebClient(baseUrl, 2);
    }

    @Bean
    public WebClient vendorWebClient(
            @Value("${services.vendor.url}") String baseUrl) {
        return criarWebClient(baseUrl, 3);
    }

    @Bean
    public WebClient notificationWebClient(
            @Value("${services.notification.url}") String baseUrl) {
        return criarWebClient(baseUrl, 5);
    }

    private WebClient criarWebClient(String baseUrl, int timeoutSegundos) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSegundos * 1000)
                .responseTimeout(Duration.ofSeconds(timeoutSegundos));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }
}
