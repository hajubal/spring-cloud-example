package me.synology.hajubal.gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {
    public GlobalFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Global Filter baseMessage: {}", config.getBaseMessage());

            if(config.isPreLogger()) {
                log.info("Global filter start: request request uri -> {}", request.getId());
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if(config.isPostLogger()) {
                    response.getHeaders().forEach((s, strings) -> System.out.println("s = " + s + ", strings = " + strings));

                    List<String> location = response.getHeaders().get("Location");

                    System.out.println(">>>>>   " + request.getURI().getPath());

                    if (location != null) {
                        for (String loc : location) {

                        }
                    }

                    log.info("Global filter end: response code -> {}", response.getStatusCode());
                }
            }));
        });
    }

    @Data
    static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
