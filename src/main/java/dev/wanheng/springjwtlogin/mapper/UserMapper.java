package dev.wanheng.springjwtlogin.mapper;
import org.apache.ibatis.annotations.Param;

import dev.wanheng.springjwtlogin.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author yao
* @description 针对表【user】的数据库操作Mapper
* @createDate 2024-06-24 09:29:06
* @Entity generator.domain.User
*/
public interface UserMapper extends BaseMapper<User> {
    User findOneByUsername(@Param("username") String username);
}




