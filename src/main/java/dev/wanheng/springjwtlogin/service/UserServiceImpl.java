package dev.wanheng.springjwtlogin.service;

import dev.wanheng.springjwtlogin.domain.User;
import dev.wanheng.springjwtlogin.dto.UserDto;
import dev.wanheng.springjwtlogin.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public void register(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setPhone(userDto.getPhone());
        userMapper.insert(user);
    }

    @Override
    public UserDto getUserByUsername(String username) {
        User user = userMapper.findOneByUsername(username);
        if (user == null) {
            return null;
        }
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setPhone(user.getPhone());
        return userDto;
    }

    @Override
    public List<UserDto> getUserList() {
        return userMapper.selectList(null).stream().map(user -> {
            UserDto userDto = new UserDto();
            userDto.setUsername(user.getUsername());
            userDto.setCreatedAt(user.getCreatedAt());
            userDto.setPhone(user.getPhone());
            return userDto;
        }).collect(Collectors.toList());
    }
}
