package dev.wanheng.springjwtlogin.controller;

import dev.wanheng.springjwtlogin.dto.PlainResult;
import dev.wanheng.springjwtlogin.dto.UserDto;
import dev.wanheng.springjwtlogin.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {


    @Resource
    private UserService userService;

    @GetMapping("/users")
    public PlainResult<List<UserDto>> users() {
        List<UserDto> userList = userService.getUserList();
        return PlainResult.success(userList);
    }
}
