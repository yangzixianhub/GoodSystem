package dev.wanheng.springjwtlogin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@MapperScan("dev.wanheng.springjwtlogin.mapper")
@EnableElasticsearchRepositories(basePackages = "dev.wanheng.springjwtlogin.repository")
public class SpringJwtLoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringJwtLoginApplication.class, args);
    }

}
