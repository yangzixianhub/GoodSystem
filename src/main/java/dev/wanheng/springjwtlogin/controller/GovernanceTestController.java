package dev.wanheng.springjwtlogin.controller;

import dev.wanheng.springjwtlogin.dto.PlainResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class GovernanceTestController {

    @GetMapping("/sleep")
    public PlainResult<String> sleep(@RequestParam(defaultValue = "0") long ms) throws InterruptedException {
        if (ms > 0) {
            Thread.sleep(ms);
        }
        return PlainResult.success("slept " + ms + "ms");
    }

    @GetMapping("/fail")
    public PlainResult<String> fail() {
        throw new RuntimeException("demo fail");
    }
}

