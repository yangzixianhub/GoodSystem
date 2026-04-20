package dev.wanheng.springjwtlogin.dto;

import lombok.Data;

@Data
public class LoginResponseDto {
    private String token;
    private Long userId;
    private String username;
}
