package me.synology.hajubal.userservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/")
public class HomeController {

    @RequestMapping("/")
    public String index() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Login user info: {}", authentication.getPrincipal());

        return "index";
    }
}
