package dev.wanheng.springjwtlogin.security;

import dev.wanheng.springjwtlogin.dto.UserDto;
import dev.wanheng.springjwtlogin.service.UserService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    private UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDto user = userService.getUserByUsername(username);
        if (user == null) {
            return null;
        }
        return new User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }

}
