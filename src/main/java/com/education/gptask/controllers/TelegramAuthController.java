package com.education.gptask.controllers;


import com.education.gptask.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


@RequiredArgsConstructor
@Controller("/auth")
public class TelegramAuthController {

    private final UserService userService;

    @PostMapping("/telegram")
    public String handleTelegramAuth(@RequestParam Map<String, String> queryParams, HttpServletRequest request) {
        if (userService.handleTelegramAuth(queryParams)) {
            request.getSession(true);
            return "redirect:/";
        } else {
            return "login";
        }
    }
}
