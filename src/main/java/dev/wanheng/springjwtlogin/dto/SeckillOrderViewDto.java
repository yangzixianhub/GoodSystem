package dev.wanheng.springjwtlogin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillOrderViewDto {
    private Long orderId;
    private Long userId;
    private Long productId;
    private BigDecimal unitPrice;
    private Integer status;
    private LocalDateTime createdAt;
}
