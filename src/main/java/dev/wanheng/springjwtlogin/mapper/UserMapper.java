package dev.wanheng.springjwtlogin.mapper;
import org.apache.ibatis.annotations.Param;

import dev.wanheng.springjwtlogin.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface UserMapper extends BaseMapper<User> {
    User findOneByUsername(@Param("username") String username);
}




