package org.joksin.springoauth2.webapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/api/users/token")
    public Map<String, String> getToken(HttpServletRequest httpServletRequest) {
        var authorizationHeader = httpServletRequest.getHeader("Authorization");

        return Map.of("token", authorizationHeader.replace("Bearer ", ""));
    }

}

