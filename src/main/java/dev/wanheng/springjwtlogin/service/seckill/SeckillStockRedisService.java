package dev.wanheng.springjwtlogin.service.seckill;

import dev.wanheng.springjwtlogin.domain.Product;
import dev.wanheng.springjwtlogin.mapper.ProductMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

//Redis缓存秒杀库存，Lua保证查询与扣减原子，避免超卖
@Service
public class SeckillStockRedisService {

    public static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String DEDUCT_SCRIPT = """
            local v = redis.call('GET', KEYS[1])
            if v == false then
              return -1
            end
            local n = tonumber(v)
            if n == nil or n < 1 then
              return 0
            end
            redis.call('DECR', KEYS[1])
            return 1
            """;

    private final DefaultRedisScript<Long> deductScript = new DefaultRedisScript<>();
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ProductMapper productMapper;

    public SeckillStockRedisService() {
        deductScript.setScriptText(DEDUCT_SCRIPT);
        deductScript.setResultType(Long.class);
    }

    public static String stockKey(Long productId) {
        return STOCK_KEY_PREFIX + productId;
    }

    public static String idempotentKey(Long userId, Long productId) {
        return "seckill:idemp:" + userId + ":" + productId;
    }

    public void ensureStockCached(Long productId) {
        String key = stockKey(productId);
        Boolean has = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(has)) {
            return;
        }
        Product p = productMapper.selectById(productId);
        if (p == null) {
            return;
        }
        int stock = p.getStock() == null ? 0 : p.getStock();
        redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(stock));
    }

    //1表示扣减成功，0表示库存不足，-1表示缓存未初始化
    public long tryAtomicDeduct(Long productId) {
        String key = stockKey(productId);
        List<String> keys = Collections.singletonList(key);
        Long r = redisTemplate.execute(deductScript, keys);
        return r == null ? 0L : r;
    }

    public void incrementCachedStock(Long productId, int delta) {
        if (delta <= 0) {
            return;
        }
        String key = stockKey(productId);
        redisTemplate.opsForValue().increment(key, delta);
    }

    public void deleteIdempotentKey(Long userId, Long productId) {
        redisTemplate.delete(idempotentKey(userId, productId));
    }
}
