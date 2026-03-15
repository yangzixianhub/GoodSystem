package dev.wanheng.springjwtlogin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.wanheng.springjwtlogin.domain.Product;
import dev.wanheng.springjwtlogin.mapper.ProductMapper;
import dev.wanheng.springjwtlogin.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {

    private static final String CACHE_KEY_PREFIX = "product:detail:";
    private static final String NULL_PLACEHOLDER = "NULL";
    private static final String LOCK_KEY_PREFIX = "product:lock:";
    //空值缓存时间
    private static final long NULL_CACHE_SECONDS = 120;
    //基础TTL30分钟，再叠加随机时间
    private static final long BASE_CACHE_SECONDS = 30 * 60;
    private static final long RANDOM_CACHE_SECONDS = 10 * 60;
    //锁过期时间
    private static final long LOCK_SECONDS = 10;

    @Resource
    private ProductMapper productMapper;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Product getDetailById(Long id) {
        if (id == null) return null;
        String cacheKey = CACHE_KEY_PREFIX + id;
        String lockKey = LOCK_KEY_PREFIX + id;

        //查缓存
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (NULL_PLACEHOLDER.equals(cached)) return null; //防穿透,曾缓存过的空结果
            return parseProduct(cached);
        }

        //尝试加锁，只有拿到锁的线程查库并回填缓存
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_SECONDS, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(locked)) {
            try {
                Product product = productMapper.selectById(id);
                if (product == null) {
                    //防穿透
                    redisTemplate.opsForValue().set(cacheKey, NULL_PLACEHOLDER, NULL_CACHE_SECONDS, TimeUnit.SECONDS);
                    return null;
                }
                //防雪崩
                long ttl = BASE_CACHE_SECONDS + (long) (Math.random() * RANDOM_CACHE_SECONDS);
                redisTemplate.opsForValue().set(cacheKey, toJson(product), ttl, TimeUnit.SECONDS);
                return product;
            } finally {
                redisTemplate.delete(lockKey);
            }
        }

        for (int i = 0; i < 20; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return productMapper.selectById(id);
            }
            cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return NULL_PLACEHOLDER.equals(cached) ? null : parseProduct(cached);
            }
        }
        return productMapper.selectById(id);
    }

    private String toJson(Product p) {
        try {
            return objectMapper.writeValueAsString(p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Product parseProduct(String json) {
        try {
            return objectMapper.readValue(json, Product.class);
        } catch (Exception e) {
            return null;
        }
    }
}
