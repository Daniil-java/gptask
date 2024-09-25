package com.education.gptask.controllers;


import com.education.gptask.services.UserService;
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
    public void handleTelegramAuth(@RequestParam Map<String, String> queryParams) {
        userService.handleTelegramAuth(queryParams);
    }


}
