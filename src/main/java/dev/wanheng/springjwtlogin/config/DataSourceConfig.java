package dev.wanheng.springjwtlogin.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

//MySQL读写分离
@Configuration
@ConditionalOnProperty(name = "app.datasource.mode", havingValue = "routing", matchIfMissing = true)
public class DataSourceConfig {

    @Value("${spring.datasource.master.url}")
    private String masterUrl;
    @Value("${spring.datasource.slave.url}")
    private String slaveUrl;
    @Value("${spring.datasource.username:root}")
    private String username;
    @Value("${spring.datasource.password:}")
    private String password;

    private static DruidDataSource createDruid(String url, String username, String password) {
        DruidDataSource ds = new DruidDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setInitialSize(5);
        ds.setMinIdle(5);
        ds.setMaxActive(20);
        ds.setValidationQuery("select 1");
        ds.setTestWhileIdle(true);
        return ds;
    }

    @Bean("masterDataSource")
    public DataSource masterDataSource() {
        return createDruid(masterUrl, username, password);
    }

    @Bean("slaveDataSource")
    public DataSource slaveDataSource() {
        return createDruid(slaveUrl, username, password);
    }

    @Bean("dynamicDataSource")
    @Primary
    public DataSource dynamicDataSource(
            @Qualifier("masterDataSource") DataSource master,
            @Qualifier("slaveDataSource") DataSource slave) {
        AbstractRoutingDataSource ds = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
                return readOnly ? "slave" : "master";
            }
        };
        ds.setDefaultTargetDataSource(master);
        ds.setTargetDataSources(Map.of("master", master, "slave", slave));
        return ds;
    }
}
