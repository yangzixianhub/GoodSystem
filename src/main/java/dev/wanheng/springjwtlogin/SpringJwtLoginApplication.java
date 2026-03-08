package dev.wanheng.springjwtlogin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("dev.wanheng.springjwtlogin.mapper")
public class SpringJwtLoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringJwtLoginApplication.class, args);
    }

}
