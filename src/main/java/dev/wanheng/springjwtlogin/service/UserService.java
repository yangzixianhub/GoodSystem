package dev.wanheng.springjwtlogin.service;

import dev.wanheng.springjwtlogin.dto.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    /**
     * 用户注册
     * @param userDto
     */
    void register(UserDto userDto);

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    UserDto getUserByUsername(String username);

    /**
     * 用户列表
     * @return
     */
    List<UserDto> getUserList();
}
