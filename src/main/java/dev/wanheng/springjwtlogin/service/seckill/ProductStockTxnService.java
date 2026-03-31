package dev.wanheng.springjwtlogin.service.seckill;

import dev.wanheng.springjwtlogin.mapper.ProductMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//商品库存事务，与分片订单分库时拆开调用，避免跨库单事务依赖
@Service
public class ProductStockTxnService {

    @Resource
    private ProductMapper productMapper;

    @Transactional(rollbackFor = Exception.class)
    public int tryDecrease(Long productId, int qty) {
        return productMapper.decreaseStock(productId, qty);
    }

    @Transactional(rollbackFor = Exception.class)
    public void increase(Long productId, int qty) {
        productMapper.increaseStock(productId, qty);
    }
}
