package dev.wanheng.springjwtlogin.service.seckill;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.wanheng.springjwtlogin.domain.LocalTxEvent;
import dev.wanheng.springjwtlogin.mapper.LocalTxEventMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LocalTxEventService {

    @Resource
    private LocalTxEventMapper localTxEventMapper;

    public static LocalTxEvent buildEvent(String eventId, String eventType, Long orderId, Long userId,
                                          Long productId, Integer quantity, BigDecimal amount) {
        LocalTxEvent event = new LocalTxEvent();
        event.setEventId(eventId);
        event.setEventType(eventType);
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setProductId(productId);
        event.setQuantity(quantity);
        event.setAmount(amount);
        event.setStatus(LocalTxEvent.STATUS_NEW);
        event.setRetryCount(0);
        event.setNextRetryTime(LocalDateTime.now());
        return event;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveEvent(LocalTxEvent event) {
        localTxEventMapper.insert(event);
    }

    public List<LocalTxEvent> fetchPending(int size) {
        return localTxEventMapper.selectList(
                new LambdaQueryWrapper<LocalTxEvent>()
                        .eq(LocalTxEvent::getStatus, LocalTxEvent.STATUS_NEW)
                        .le(LocalTxEvent::getNextRetryTime, LocalDateTime.now())
                        .orderByAsc(LocalTxEvent::getId)
                        .last("limit " + size));
    }

    public void markSent(Long id) {
        LocalTxEvent update = new LocalTxEvent();
        update.setId(id);
        update.setStatus(LocalTxEvent.STATUS_SENT);
        localTxEventMapper.updateById(update);
    }

    public void markRetry(Long id, int retryCount) {
        LocalTxEvent update = new LocalTxEvent();
        update.setId(id);
        update.setRetryCount(retryCount);
        update.setNextRetryTime(LocalDateTime.now().plusSeconds(Math.min(60, 2L * retryCount)));
        localTxEventMapper.updateById(update);
    }
}
