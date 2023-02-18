package me.synology.hajubal.gateway.filter;

import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;


@Slf4j
@Component
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

    @Autowired
    private WebClient.Builder builder;


    public CustomFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Custom PRE filter: request request uri -> {}", request.getId());

            WebClient client = builder.baseUrl("http://FIRST-SERVICE:8888/ok").build();

            client.get().exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                    .subscribe(response1 -> {
                        if(Objects.equals(response1.getBody(), "ok")) {
                            System.out.println("response1 = " + response1);
                        }
                    });

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                log.info("Custom POST filter: response code -> {}", response.getStatusCode());
            }));
        });
    }

    static class Config {

    }
}
