package dev.wanheng.springjwtlogin.service;

import dev.wanheng.springjwtlogin.mapper.DsTestMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//读写分离效果测试
@Service
public class DataSourceTestService {

    @Resource
    private DsTestMapper dsTestMapper;

    @Transactional(readOnly = true)
    public Long getReadServerId() {
        return dsTestMapper.getServerId();
    }

    @Transactional(readOnly = false)
    public Long getWriteServerId() {
        return dsTestMapper.getServerId();
    }
}
