package dev.wanheng.springjwtlogin.controller;

import dev.wanheng.springjwtlogin.dto.PlainResult;
import dev.wanheng.springjwtlogin.dto.SeckillOrderViewDto;
import dev.wanheng.springjwtlogin.dto.SeckillPlaceOrderRequest;
import dev.wanheng.springjwtlogin.dto.SeckillPlaceOrderResponse;
import dev.wanheng.springjwtlogin.dto.UserDto;
import dev.wanheng.springjwtlogin.service.UserService;
import dev.wanheng.springjwtlogin.service.seckill.SeckillOrderService;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Resource
    private SeckillOrderService seckillOrderService;
    @Resource
    private UserService userService;

    //秒杀下单：Redis预扣库存，Kafka异步落库，削峰填谷
    @PostMapping("/order")
    public PlainResult<SeckillPlaceOrderResponse> placeOrder(@RequestBody SeckillPlaceOrderRequest request) {
        try {
            String username = currentUsername();
            SeckillPlaceOrderResponse res = seckillOrderService.placeOrder(username, request.getProductId());
            return PlainResult.success(res);
        } catch (IllegalArgumentException e) {
            return PlainResult.fail(400, e.getMessage());
        } catch (IllegalStateException e) {
            if ("未登录".equals(e.getMessage())) {
                return PlainResult.fail(401, e.getMessage());
            }
            return PlainResult.fail(409, e.getMessage());
        } catch (Exception e) {
            return PlainResult.fail(500, "系统异常");
        }
    }

    //按订单ID查询
    @GetMapping("/order/{orderId}")
    public PlainResult<SeckillOrderViewDto> getOrder(@PathVariable Long orderId) {
        try {
            Long uid = requireUserId();
            SeckillOrderViewDto dto = seckillOrderService.getOrderForUser(orderId, uid);
            if (dto == null) {
                return PlainResult.fail(404, "订单不存在或无权查看");
            }
            return PlainResult.success(dto);
        } catch (IllegalStateException e) {
            return PlainResult.fail(401, e.getMessage());
        }
    }

    //按当前登录用户ID查询订单列表，路径中的userId须与登录用户一致，防止越权
    @GetMapping("/orders/user/{userId}")
    public PlainResult<List<SeckillOrderViewDto>> listByUser(@PathVariable Long userId) {
        try {
            Long uid = requireUserId();
            List<SeckillOrderViewDto> list = seckillOrderService.listOrdersByUser(userId, uid);
            return PlainResult.success(list);
        } catch (IllegalStateException e) {
            return PlainResult.fail(401, e.getMessage());
        } catch (SecurityException e) {
            return PlainResult.fail(403, e.getMessage());
        }
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new IllegalStateException("未登录");
        }
        return auth.getName();
    }

    private Long requireUserId() {
        UserDto u = userService.getUserByUsername(currentUsername());
        if (u == null || u.getId() == null) {
            throw new IllegalStateException("用户不存在");
        }
        return u.getId();
    }
}
