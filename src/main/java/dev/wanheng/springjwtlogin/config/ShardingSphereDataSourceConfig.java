package dev.wanheng.springjwtlogin.config;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

//ShardingSphere-JDBC秒杀订单逻辑表按user_id分库、按订单主键id分表；user、product为单表，路由至主库message
@Configuration
@ConditionalOnProperty(name = "app.datasource.mode", havingValue = "sharding")
public class ShardingSphereDataSourceConfig {

    @Bean
    @Primary
    public DataSource shardingSphereDataSource(
            @Value("${spring.datasource.username:root}") String username,
            @Value("${spring.datasource.password:}") String password,
            @Value("${spring.datasource.master.url}") String masterUrl,
            @Value("${app.datasource.shard0-url:}") String shard0UrlProp,
            @Value("${app.datasource.shard1-url:}") String shard1UrlProp) throws SQLException, IOException {
        String commonUrl = masterUrl;
        String shard0Url = shard0UrlProp.isBlank() ? replaceDatabaseName(masterUrl, "message_s0") : shard0UrlProp;
        String shard1Url = shard1UrlProp.isBlank() ? replaceDatabaseName(masterUrl, "message_s1") : shard1UrlProp;
        String yaml = buildYaml(
                escapeYamlScalar(username),
                escapeYamlScalar(password),
                escapeYamlScalar(commonUrl),
                escapeYamlScalar(shard0Url),
                escapeYamlScalar(shard1Url));
        return YamlShardingSphereDataSourceFactory.createDataSource(yaml.getBytes(StandardCharsets.UTF_8));
    }

    static String replaceDatabaseName(String jdbcUrl, String newDb) {
        int q = jdbcUrl.indexOf('?');
        String pathPart = q < 0 ? jdbcUrl : jdbcUrl.substring(0, q);
        String query = q < 0 ? "" : jdbcUrl.substring(q);
        int slash = pathPart.lastIndexOf('/');
        if (slash < 0 || slash >= pathPart.length() - 1) {
            throw new IllegalArgumentException("无法从 JDBC URL 解析库名: " + jdbcUrl);
        }
        return pathPart.substring(0, slash + 1) + newDb + query;
    }

    private static String escapeYamlScalar(String s) {
        if (s == null) {
            return "\"\"";
        }
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String buildYaml(String username, String password, String commonUrl, String shard0Url, String shard1Url) {
        return """
                databaseName: goodsystem

                mode:
                  type: Standalone

                dataSources:
                  common:
                    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                    driverClassName: com.mysql.cj.jdbc.Driver
                    jdbcUrl: %s
                    username: %s
                    password: %s
                    maximumPoolSize: 20
                    minimumIdle: 5
                  shard0:
                    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                    driverClassName: com.mysql.cj.jdbc.Driver
                    jdbcUrl: %s
                    username: %s
                    password: %s
                    maximumPoolSize: 20
                    minimumIdle: 5
                  shard1:
                    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                    driverClassName: com.mysql.cj.jdbc.Driver
                    jdbcUrl: %s
                    username: %s
                    password: %s
                    maximumPoolSize: 20
                    minimumIdle: 5

                rules:
                - !SHARDING
                  tables:
                    seckill_order:
                      actualDataNodes: shard$->{0..1}.seckill_order_$->{0..1}
                      databaseStrategy:
                        standard:
                          shardingColumn: user_id
                          shardingAlgorithmName: algo-user-db
                      tableStrategy:
                        standard:
                          shardingColumn: id
                          shardingAlgorithmName: algo-order-table
                  shardingAlgorithms:
                    algo-user-db:
                      type: INLINE
                      props:
                        algorithm-expression: shard$->{user_id %% 2}
                    algo-order-table:
                      type: INLINE
                      props:
                        algorithm-expression: seckill_order_$->{id %% 2}
                - !SINGLE
                  tables:
                    - common.user
                    - common.product

                props:
                  sql-show: false
                """.formatted(
                commonUrl, username, password,
                shard0Url, username, password,
                shard1Url, username, password);
    }
}
