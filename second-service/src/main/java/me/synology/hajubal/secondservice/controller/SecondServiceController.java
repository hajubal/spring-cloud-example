package me.synology.hajubal.secondservice.controller;

import lombok.extern.slf4j.Slf4j;
import me.synology.hajubal.secondservice.service.FeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/second-service")
@RestController
public class SecondServiceController {

    @Autowired
    private FeignService feignService;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome second service";
    }

    @GetMapping("/message")
    public String message(@RequestHeader("second-request") String header) {
        return "Second-request header value: " + header;
    }

    @GetMapping("/check")
    public String check() {
        log.info("Second service check.");
        return "Hi, there. This is a message form Second Service.";
    }

    @GetMapping("ok")
    public String ok() {
        return feignService.ok();
    }
}
