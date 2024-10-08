package com.microservices.api.reactive.elastic.query.web.client.config;

import com.microservices.api.config.ElasticQueryWebClientConfigData;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    private static final Logger LOG = LoggerFactory.getLogger(WebClientConfig.class);
    private final ElasticQueryWebClientConfigData.WebClient webClientConfig;
    public WebClientConfig(ElasticQueryWebClientConfigData clientConfigData) {
        this.webClientConfig = clientConfigData.getWebClient();
        LOG.info("WebClientConfig loaded: {}", this.webClientConfig);
    }

    @Bean("webClient")
    WebClient webClient() {
        return WebClient.builder()
                .baseUrl(webClientConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, webClientConfig.getContentType())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(getTcpClient())))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(webClientConfig.getMaxInMemorySize()))
                .build();
    }

    private TcpClient getTcpClient() {
        return TcpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, webClientConfig.getConnectTimeoutMs())
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(webClientConfig.getReadTimeoutMs(),
                            TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(webClientConfig.getWriteTimeoutMs(),
                            TimeUnit.MILLISECONDS));
                });
    }
}
