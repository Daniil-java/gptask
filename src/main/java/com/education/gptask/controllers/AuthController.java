    package com.education.gptask.controllers;

    import lombok.RequiredArgsConstructor;
    import org.springframework.security.core.Authentication;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.GetMapping;

    @RequiredArgsConstructor
    @Controller
    public class AuthController {

        @GetMapping("/login")
        public String login(Authentication authentication) {
            return "login";
        }
    }
