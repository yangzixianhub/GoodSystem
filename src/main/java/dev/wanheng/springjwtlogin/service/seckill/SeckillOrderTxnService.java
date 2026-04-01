package dev.wanheng.springjwtlogin.service.seckill;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.wanheng.springjwtlogin.domain.LocalTxEvent;
import dev.wanheng.springjwtlogin.domain.SeckillOrder;
import dev.wanheng.springjwtlogin.mapper.SeckillOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

//分片订单落库与状态流转
@Service
public class SeckillOrderTxnService {

    @Resource
    private SeckillOrderMapper seckillOrderMapper;
    @Resource
    private LocalTxEventService localTxEventService;

    public boolean existsByOrderIdAndUserId(Long orderId, Long userId) {
        return seckillOrderMapper.selectCount(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getId, orderId)
                        .eq(SeckillOrder::getUserId, userId)) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOrderAndOutbox(Long orderId, Long userId, Long productId, BigDecimal amount) {
        SeckillOrder order = new SeckillOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setUnitPrice(amount);
        order.setStatus(SeckillOrder.STATUS_PENDING);
        seckillOrderMapper.insert(order);
        LocalTxEvent event = LocalTxEventService.buildEvent(
                UUID.randomUUID().toString(),
                dev.wanheng.springjwtlogin.messaging.ConsistencyEventType.ORDER_CREATED,
                orderId, userId, productId, 1, amount);
        localTxEventService.saveEvent(event);
    }

    @Transactional(rollbackFor = Exception.class)
    public int markWaitPay(Long orderId, Long userId) {
        SeckillOrder update = new SeckillOrder();
        update.setStatus(SeckillOrder.STATUS_WAIT_PAY);
        return seckillOrderMapper.update(update, new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getId, orderId)
                .eq(SeckillOrder::getUserId, userId)
                .eq(SeckillOrder::getStatus, SeckillOrder.STATUS_PENDING));
    }

    @Transactional(rollbackFor = Exception.class)
    public int markFailed(Long orderId, Long userId) {
        SeckillOrder update = new SeckillOrder();
        update.setStatus(SeckillOrder.STATUS_FAILED);
        return seckillOrderMapper.update(update, new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getId, orderId)
                .eq(SeckillOrder::getUserId, userId)
                .in(SeckillOrder::getStatus, SeckillOrder.STATUS_PENDING, SeckillOrder.STATUS_WAIT_PAY));
    }

    @Transactional(rollbackFor = Exception.class)
    public int markPaid(Long orderId, Long userId) {
        SeckillOrder update = new SeckillOrder();
        update.setStatus(SeckillOrder.STATUS_PAID);
        return seckillOrderMapper.update(update, new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getId, orderId)
                .eq(SeckillOrder::getUserId, userId)
                .eq(SeckillOrder::getStatus, SeckillOrder.STATUS_WAIT_PAY));
    }

    @Transactional(rollbackFor = Exception.class)
    public int markCancelledIfWaitPay(Long orderId, Long userId) {
        SeckillOrder update = new SeckillOrder();
        update.setStatus(SeckillOrder.STATUS_CANCELLED);
        return seckillOrderMapper.update(update, new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getId, orderId)
                .eq(SeckillOrder::getUserId, userId)
                .eq(SeckillOrder::getStatus, SeckillOrder.STATUS_WAIT_PAY));
    }
}
