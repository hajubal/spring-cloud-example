package me.synology.hajubal.secondservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "first-service")
public interface FeignService {

    @GetMapping("/ok")
    String ok();
}
