package dev.wanheng.springjwtlogin.controller;

import dev.wanheng.springjwtlogin.dto.LoginResponseDto;
import dev.wanheng.springjwtlogin.dto.PlainResult;
import dev.wanheng.springjwtlogin.dto.UserDto;
import dev.wanheng.springjwtlogin.service.UserService;
import dev.wanheng.springjwtlogin.util.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public PlainResult<Void> register(@RequestBody UserDto userDto) {
        UserDto user = userService.getUserByUsername(userDto.getUsername());
        if (user != null) {
            return PlainResult.fail(400, "用户名已存在");
        }
        if (userDto.getPhone() == null || userDto.getPhone().isEmpty()) {
            return PlainResult.fail(400, "手机号不能为空");
        }
        userService.register(userDto);
        return PlainResult.success(null);
    }

    @PostMapping("/login")
    public PlainResult<LoginResponseDto> login(@RequestBody UserDto userDto) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken
                        (userDto.getUsername(), userDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        LoginResponseDto loginResponseDto = new LoginResponseDto();
        loginResponseDto.setToken(jwtUtil.generateToken(userDto.getUsername(), userDto.getPhone()));
        return PlainResult.success(loginResponseDto);
    }
}


