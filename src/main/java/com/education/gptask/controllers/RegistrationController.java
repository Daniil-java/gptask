    package com.education.gptask.controllers;

    import com.education.gptask.dtos.UserDto;
    import com.education.gptask.services.UserService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.validation.BindingResult;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.ModelAttribute;
    import org.springframework.web.bind.annotation.PostMapping;

    @Controller
    @RequiredArgsConstructor
    public class RegistrationController {

        private final UserService userService;

        @GetMapping("/registration")
        public String registration(Model model) {
            model.addAttribute("userForm", new UserDto());

            return "registration";
        }

        @PostMapping("/registration")
        public String addUser(@ModelAttribute("userForm") @Valid UserDto userForm, BindingResult bindingResult, Model model) {

            if (!userService.registerUser(userForm)){
                model.addAttribute("usernameError", "Пользователь с таким именем уже существует");
                return "registration";
            }

            return "redirect:/";
        }
    }
