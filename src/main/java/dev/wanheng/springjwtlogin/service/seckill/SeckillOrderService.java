package dev.wanheng.springjwtlogin.service.seckill;

import dev.wanheng.springjwtlogin.dto.SeckillOrderViewDto;
import dev.wanheng.springjwtlogin.dto.SeckillPlaceOrderResponse;

import java.util.List;

public interface SeckillOrderService {

    SeckillPlaceOrderResponse placeOrder(String username, Long productId);

    SeckillOrderViewDto getOrderForUser(Long orderId, Long currentUserId);

    List<SeckillOrderViewDto> listOrdersByUser(Long userId, Long currentUserId);

    void payOrder(String username, Long orderId);
}
