package dev.wanheng.springjwtlogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillPlaceOrderResponse {
    private Long orderId;
    private String hint;
}
