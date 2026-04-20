package dev.wanheng.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayGovernanceConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = "unknown";
            if (exchange.getRequest() != null && exchange.getRequest().getRemoteAddress() != null) {
                ip = String.valueOf(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
            }
            return Mono.just(ip);
        };
    }
}

