package dev.wanheng.springjwtlogin.messaging;

import dev.wanheng.springjwtlogin.service.seckill.SeckillOrderConsumerService;
import jakarta.annotation.Resource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class SeckillOrderKafkaListener {

    @Resource
    private SeckillOrderConsumerService seckillOrderConsumerService;

    @KafkaListener(
            topics = "${seckill.kafka.topic}",
            containerFactory = "seckillKafkaListenerContainerFactory")
    public void onMessage(SeckillOrderMessage message, Acknowledgment acknowledgment) {
        seckillOrderConsumerService.handleMessage(message);
        acknowledgment.acknowledge();
    }
}
