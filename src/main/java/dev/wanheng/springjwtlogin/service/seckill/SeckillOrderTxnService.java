package dev.wanheng.springjwtlogin.service.seckill;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.wanheng.springjwtlogin.domain.SeckillOrder;
import dev.wanheng.springjwtlogin.mapper.SeckillOrderMapper;
import dev.wanheng.springjwtlogin.messaging.SeckillOrderMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//分片订单落库，查询带user_id，供ShardingSphere定位分库
@Service
public class SeckillOrderTxnService {

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    public boolean existsByOrderIdAndUserId(Long orderId, Long userId) {
        return seckillOrderMapper.selectCount(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getId, orderId)
                        .eq(SeckillOrder::getUserId, userId)) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertSeckill(SeckillOrderMessage msg) {
        SeckillOrder order = new SeckillOrder();
        order.setId(msg.getOrderId());
        order.setUserId(msg.getUserId());
        order.setProductId(msg.getProductId());
        order.setUnitPrice(msg.getUnitPrice());
        order.setStatus(SeckillOrder.STATUS_CONFIRMED);
        seckillOrderMapper.insert(order);
    }
}
