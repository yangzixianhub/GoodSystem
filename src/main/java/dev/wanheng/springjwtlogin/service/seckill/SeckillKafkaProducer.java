package dev.wanheng.springjwtlogin.service.seckill;

import dev.wanheng.springjwtlogin.messaging.SeckillOrderMessage;
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

    @Value("${seckill.kafka.topic}")
    private String topic;

    public void sendOrderCreate(SeckillOrderMessage message) throws Exception {
        CompletableFuture<?> future =
                kafkaTemplate.send(topic, String.valueOf(message.getOrderId()), message);
        future.get(10, TimeUnit.SECONDS);
    }
}
