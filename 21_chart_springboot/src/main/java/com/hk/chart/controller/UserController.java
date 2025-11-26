package com.hk.chart.controller;

import com.hk.chart.dto.UserDto;
import com.hk.chart.dto.RegisterDto;
import com.hk.chart.entity.User;
import com.hk.chart.service.UserService;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 로그인 페이지 (화면만 제공)
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("login", new UserDto());
        return "login";     // templates/login.html
    }

    // ❌ loginProc (POST) 삭제됨 -> SecurityConfig가 처리

    // 회원가입 페이지
    @GetMapping("/signUp")
    public String joinForm(Model model) {
        model.addAttribute("register", new RegisterDto());
        return "signUp";  // templates/signUp.html
    }

    // 회원가입 처리 (여전히 Controller가 담당)
    @PostMapping("/joinProc")
    public String joinProcess(@Valid RegisterDto register,
                              BindingResult bindingResult,
                              Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "입력값을 확인해주세요.");
            return "signUp";
        }

        try {
            userService.register(register);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "signUp";
        }

        return "redirect:/user/login";
    }

    // ❌ logout (GET) 삭제됨 -> SecurityConfig가 처리
}