package dev.wanheng.springjwtlogin.service.seckill;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.wanheng.springjwtlogin.domain.LocalTxEvent;
import dev.wanheng.springjwtlogin.domain.SeckillOrder;
import dev.wanheng.springjwtlogin.mapper.SeckillOrderMapper;
import dev.wanheng.springjwtlogin.messaging.ConsistencyEventType;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class OrderTimeoutCancelScheduler {

    @Resource
    private SeckillOrderMapper seckillOrderMapper;
    @Resource
    private LocalTxEventService localTxEventService;

    @Scheduled(fixedDelayString = "${seckill.order-timeout.scan-interval-ms:5000}")
    public void scanAndPublishTimeoutCancel() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(15);
        List<SeckillOrder> orders = seckillOrderMapper.selectList(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getStatus, SeckillOrder.STATUS_WAIT_PAY)
                        .lt(SeckillOrder::getCreatedAt, timeout)
                        .last("limit 100"));
        for (SeckillOrder order : orders) {
            LocalTxEvent event = LocalTxEventService.buildEvent(
                    UUID.randomUUID().toString(),
                    ConsistencyEventType.ORDER_CANCEL_TIMEOUT,
                    order.getId(),
                    order.getUserId(),
                    order.getProductId(),
                    1,
                    order.getUnitPrice());
            localTxEventService.saveEvent(event);
        }
    }
}
