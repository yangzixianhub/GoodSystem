package dev.wanheng.springjwtlogin.service.seckill;

import dev.wanheng.springjwtlogin.messaging.ConsistencyEventMessage;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class SeckillKafkaProducer {

    @SuppressWarnings("rawtypes")
    @Resource
    private KafkaTemplate kafkaTemplate;

    @Value("${seckill.kafka.consistency-topic}")
    private String consistencyTopic;

    public void sendConsistencyEvent(ConsistencyEventMessage message) throws Exception {
        CompletableFuture<?> future =
                kafkaTemplate.send(consistencyTopic, message.getEventId(), message);
        future.get(10, TimeUnit.SECONDS);
    }
}
