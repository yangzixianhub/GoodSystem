package dev.wanheng.gateway;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GatewayFallbackController {

    @RequestMapping(value = "/_fallback", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> fallback() {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("code", 503);
        body.put("msg", "网关降级：服务繁忙或不可用，请稍后重试");
        body.put("data", null);
        return Mono.just(ResponseEntity.status(503).body(body));
    }
}

