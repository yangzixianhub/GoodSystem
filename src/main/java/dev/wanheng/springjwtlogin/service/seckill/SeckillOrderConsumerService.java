package dev.wanheng.springjwtlogin.service.seckill;

import dev.wanheng.springjwtlogin.messaging.SeckillOrderMessage;
import jakarta.annotation.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

//Kafka消费,先扣主库商品库存，再写入分片订单表
@Service
public class SeckillOrderConsumerService {

    @Resource
    private SeckillOrderTxnService seckillOrderTxnService;
    @Resource
    private ProductStockTxnService productStockTxnService;
    @Resource
    private SeckillStockRedisService seckillStockRedisService;

    public void handleMessage(SeckillOrderMessage msg) {
        if (seckillOrderTxnService.existsByOrderIdAndUserId(msg.getOrderId(), msg.getUserId())) {
            return;
        }
        int rows = productStockTxnService.tryDecrease(msg.getProductId(), 1);
        if (rows == 0) {
            seckillStockRedisService.incrementCachedStock(msg.getProductId(), 1);
            return;
        }
        try {
            seckillOrderTxnService.insertSeckill(msg);
        } catch (DataIntegrityViolationException ex) {
            productStockTxnService.increase(msg.getProductId(), 1);
            seckillStockRedisService.incrementCachedStock(msg.getProductId(), 1);
        }
    }
}
