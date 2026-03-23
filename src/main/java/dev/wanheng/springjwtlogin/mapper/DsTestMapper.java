package dev.wanheng.springjwtlogin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

//用于读写分离测试,查询当前连接的MySQL server_id
@Mapper
public interface DsTestMapper {
    @Select("SELECT @@server_id")
    Long getServerId();
}
