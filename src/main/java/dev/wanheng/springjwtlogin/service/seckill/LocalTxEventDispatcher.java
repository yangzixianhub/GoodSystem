package dev.wanheng.springjwtlogin.service.seckill;

import dev.wanheng.springjwtlogin.domain.LocalTxEvent;
import dev.wanheng.springjwtlogin.messaging.ConsistencyEventMessage;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LocalTxEventDispatcher {

    @Resource
    private LocalTxEventService localTxEventService;
    @Resource
    private SeckillKafkaProducer seckillKafkaProducer;

    @Scheduled(fixedDelayString = "${seckill.event.dispatch-interval-ms:1000}")
    public void dispatch() {
        for (LocalTxEvent e : localTxEventService.fetchPending(50)) {
            try {
                ConsistencyEventMessage message = new ConsistencyEventMessage();
                message.setEventId(e.getEventId());
                message.setEventType(e.getEventType());
                message.setOrderId(e.getOrderId());
                message.setUserId(e.getUserId());
                message.setProductId(e.getProductId());
                message.setQuantity(e.getQuantity());
                message.setAmount(e.getAmount());
                seckillKafkaProducer.sendConsistencyEvent(message);
                localTxEventService.markSent(e.getId());
            } catch (Exception ex) {
                int next = e.getRetryCount() == null ? 1 : e.getRetryCount() + 1;
                localTxEventService.markRetry(e.getId(), next);
            }
        }
    }
}
