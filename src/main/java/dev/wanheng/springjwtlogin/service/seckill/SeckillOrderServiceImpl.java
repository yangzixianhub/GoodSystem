package dev.wanheng.springjwtlogin.service.seckill;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.wanheng.springjwtlogin.domain.LocalTxEvent;
import dev.wanheng.springjwtlogin.domain.PaymentRecord;
import dev.wanheng.springjwtlogin.domain.Product;
import dev.wanheng.springjwtlogin.domain.SeckillOrder;
import dev.wanheng.springjwtlogin.dto.SeckillOrderViewDto;
import dev.wanheng.springjwtlogin.dto.SeckillPlaceOrderResponse;
import dev.wanheng.springjwtlogin.dto.UserDto;
import dev.wanheng.springjwtlogin.mapper.PaymentRecordMapper;
import dev.wanheng.springjwtlogin.mapper.ProductMapper;
import dev.wanheng.springjwtlogin.mapper.SeckillOrderMapper;
import dev.wanheng.springjwtlogin.messaging.ConsistencyEventType;
import dev.wanheng.springjwtlogin.service.UserService;
import dev.wanheng.springjwtlogin.util.SnowflakeIdGenerator;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    private static final String PROCESSING = "PROCESSING";

    @Resource
    private UserService userService;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private SeckillOrderMapper seckillOrderMapper;
    @Resource
    private SeckillStockRedisService seckillStockRedisService;
    @Resource
    private SeckillOrderTxnService seckillOrderTxnService;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private PaymentRecordMapper paymentRecordMapper;
    @Resource
    private LocalTxEventService localTxEventService;

    private static boolean isNumericOrderId(String value) {
        return value != null && !value.isEmpty() && value.chars().allMatch(Character::isDigit);
    }

    @Override
    public SeckillPlaceOrderResponse placeOrder(String username, Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId 不能为空");
        }
        UserDto user = userService.getUserByUsername(username);
        if (user == null || user.getId() == null) {
            throw new IllegalStateException("用户不存在");
        }
        Long userId = user.getId();
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        String idempKey = SeckillStockRedisService.idempotentKey(userId, productId);
        String existing = redisTemplate.opsForValue().get(idempKey);
        if (isNumericOrderId(existing)) {
            return new SeckillPlaceOrderResponse(Long.parseLong(existing), "重复提交，返回已有订单号");
        }

        Boolean locked = redisTemplate.opsForValue().setIfAbsent(idempKey, PROCESSING, Duration.ofMinutes(2));
        if (Boolean.FALSE.equals(locked)) {
            String again = redisTemplate.opsForValue().get(idempKey);
            if (isNumericOrderId(again)) {
                return new SeckillPlaceOrderResponse(Long.parseLong(again), "重复提交，返回已有订单号");
            }
            throw new IllegalStateException("请求处理中，请稍后再试");
        }

        try {
            seckillStockRedisService.ensureStockCached(productId);
            long deduct = seckillStockRedisService.tryAtomicDeduct(productId);
            if (deduct == -1) {
                seckillStockRedisService.ensureStockCached(productId);
                deduct = seckillStockRedisService.tryAtomicDeduct(productId);
            }
            if (deduct == 0) {
                redisTemplate.delete(idempKey);
                throw new IllegalStateException("库存不足");
            }

            long orderId = snowflakeIdGenerator.nextId();
            seckillOrderTxnService.createOrderAndOutbox(orderId, userId, productId, product.getPrice());

            redisTemplate.opsForValue().set(idempKey, String.valueOf(orderId), Duration.ofDays(7));
            return new SeckillPlaceOrderResponse(orderId, "已受理，订单异步落库中，可通过订单号查询");
        } catch (RuntimeException e) {
            if (PROCESSING.equals(redisTemplate.opsForValue().get(idempKey))) {
                redisTemplate.delete(idempKey);
            }
            throw e;
        }
    }

    @Override
    public SeckillOrderViewDto getOrderForUser(Long orderId, Long currentUserId) {
        SeckillOrder order = seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getId, orderId)
                        .eq(SeckillOrder::getUserId, currentUserId));
        if (order == null) {
            return null;
        }
        return toView(order);
    }

    @Override
    public List<SeckillOrderViewDto> listOrdersByUser(Long userId, Long currentUserId) {
        if (!Objects.equals(userId, currentUserId)) {
            throw new SecurityException("无权查询其他用户订单");
        }
        List<SeckillOrder> list = seckillOrderMapper.selectList(
                new LambdaQueryWrapper<SeckillOrder>().eq(SeckillOrder::getUserId, userId)
                        .orderByDesc(SeckillOrder::getCreatedAt));
        return list.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String username, Long orderId) {
        UserDto user = userService.getUserByUsername(username);
        if (user == null || user.getId() == null) {
            throw new IllegalStateException("用户不存在");
        }
        SeckillOrder order = seckillOrderMapper.selectOne(new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getId, orderId)
                .eq(SeckillOrder::getUserId, user.getId()));
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (Objects.equals(order.getStatus(), SeckillOrder.STATUS_PAID)) {
            return;
        }
        if (!Objects.equals(order.getStatus(), SeckillOrder.STATUS_WAIT_PAY)) {
            throw new IllegalStateException("订单状态不支持支付");
        }
        if (paymentRecordMapper.selectCount(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOrderId, orderId)
                .eq(PaymentRecord::getUserId, user.getId())) == 0) {
            PaymentRecord record = new PaymentRecord();
            record.setOrderId(orderId);
            record.setUserId(user.getId());
            record.setAmount(order.getUnitPrice());
            record.setPayStatus(1);
            record.setCreatedAt(LocalDateTime.now());
            paymentRecordMapper.insert(record);
        }
        LocalTxEvent event = LocalTxEventService.buildEvent(
                UUID.randomUUID().toString(),
                ConsistencyEventType.PAY_SUCCESS,
                orderId,
                user.getId(),
                order.getProductId(),
                1,
                order.getUnitPrice());
        localTxEventService.saveEvent(event);
    }

    private SeckillOrderViewDto toView(SeckillOrder o) {
        SeckillOrderViewDto dto = new SeckillOrderViewDto();
        dto.setOrderId(o.getId());
        dto.setUserId(o.getUserId());
        dto.setProductId(o.getProductId());
        dto.setUnitPrice(o.getUnitPrice());
        dto.setStatus(o.getStatus());
        dto.setCreatedAt(o.getCreatedAt());
        return dto;
    }
}
