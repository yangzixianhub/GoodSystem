package dev.wanheng.springjwtlogin.controller;

import dev.wanheng.springjwtlogin.dto.PlainResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
@RefreshScope
public class ConfigDemoController {

    @Value("${demo.greeting:你好，Nacos}")
    private String greeting;

    @GetMapping("/greeting")
    public PlainResult<String> greeting() {
        return PlainResult.success(greeting);
    }
}

