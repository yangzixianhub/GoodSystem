package dev.wanheng.springjwtlogin.service.seckill;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.wanheng.springjwtlogin.domain.LocalTxEvent;
import dev.wanheng.springjwtlogin.domain.ProcessedEvent;
import dev.wanheng.springjwtlogin.mapper.ProcessedEventMapper;
import dev.wanheng.springjwtlogin.messaging.ConsistencyEventMessage;
import dev.wanheng.springjwtlogin.messaging.ConsistencyEventType;
import jakarta.annotation.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

//Kafka消费：订单与库存服务通过事件编排实现最终一致
@Service
public class SeckillOrderConsumerService {

    @Resource
    private SeckillOrderTxnService seckillOrderTxnService;
    @Resource
    private ProductStockTxnService productStockTxnService;
    @Resource
    private SeckillStockRedisService seckillStockRedisService;
    @Resource
    private ProcessedEventMapper processedEventMapper;
    @Resource
    private LocalTxEventService localTxEventService;

    public void handleEvent(ConsistencyEventMessage msg) {
        if (alreadyProcessed(msg.getEventId())) {
            return;
        }
        switch (msg.getEventType()) {
            case ConsistencyEventType.ORDER_CREATED -> onOrderCreated(msg);
            case ConsistencyEventType.STOCK_RESERVED -> seckillOrderTxnService.markWaitPay(msg.getOrderId(), msg.getUserId());
            case ConsistencyEventType.STOCK_REJECTED -> {
                seckillOrderTxnService.markFailed(msg.getOrderId(), msg.getUserId());
                seckillStockRedisService.incrementCachedStock(msg.getProductId(), 1);
                seckillStockRedisService.deleteIdempotentKey(msg.getUserId(), msg.getProductId());
            }
            case ConsistencyEventType.PAY_SUCCESS -> seckillOrderTxnService.markPaid(msg.getOrderId(), msg.getUserId());
            case ConsistencyEventType.ORDER_CANCEL_TIMEOUT -> {
                int updated = seckillOrderTxnService.markCancelledIfWaitPay(msg.getOrderId(), msg.getUserId());
                if (updated > 0) {
                    LocalTxEvent cancelEvent = LocalTxEventService.buildEvent(
                            UUID.randomUUID().toString(),
                            ConsistencyEventType.ORDER_CANCELLED,
                            msg.getOrderId(), msg.getUserId(), msg.getProductId(), 1, msg.getAmount());
                    localTxEventService.saveEvent(cancelEvent);
                }
            }
            case ConsistencyEventType.ORDER_CANCELLED -> {
                productStockTxnService.increase(msg.getProductId(), 1);
                seckillStockRedisService.incrementCachedStock(msg.getProductId(), 1);
            }
            default -> {
                return;
            }
        }
        markProcessed(msg.getEventId());
    }

    private void onOrderCreated(ConsistencyEventMessage msg) {
        int rows = productStockTxnService.tryDecrease(msg.getProductId(), 1);
        LocalTxEvent next;
        if (rows == 0) {
            next = LocalTxEventService.buildEvent(
                    UUID.randomUUID().toString(),
                    ConsistencyEventType.STOCK_REJECTED,
                    msg.getOrderId(), msg.getUserId(), msg.getProductId(), 1, msg.getAmount());
        } else {
            next = LocalTxEventService.buildEvent(
                    UUID.randomUUID().toString(),
                    ConsistencyEventType.STOCK_RESERVED,
                    msg.getOrderId(), msg.getUserId(), msg.getProductId(), 1, msg.getAmount());
        }
        try {
            localTxEventService.saveEvent(next);
        } catch (DataIntegrityViolationException ex) {
            // rare duplicate, ignore
        }
    }

    private boolean alreadyProcessed(String eventId) {
        return processedEventMapper.selectCount(new LambdaQueryWrapper<ProcessedEvent>()
                .eq(ProcessedEvent::getEventId, eventId)
                .eq(ProcessedEvent::getConsumerName, "consistency-consumer")) > 0;
    }

    private void markProcessed(String eventId) {
        ProcessedEvent p = new ProcessedEvent();
        p.setEventId(eventId);
        p.setConsumerName("consistency-consumer");
        p.setCreatedAt(LocalDateTime.now());
        try {
            processedEventMapper.insert(p);
        } catch (DataIntegrityViolationException ignore) {
            // dedup
        }
    }
}
