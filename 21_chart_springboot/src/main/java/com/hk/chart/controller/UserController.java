package com.hk.chart.controller;

import com.hk.chart.dto.Login;
import com.hk.chart.dto.Register;
import com.hk.chart.entity.User;
import com.hk.chart.service.UserService;
import jakarta.servlet.http.HttpSession;
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

    // 로그인 페이지
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("login", new Login());
        return "login";     // templates/login.html
    }

    // 로그인 처리
    @PostMapping("/loginProc")
    public String loginProcess(@Valid Login login,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model) {
    	System.out.println("BindingResult has errors? " + bindingResult.hasErrors());
    	System.out.println("Login DTO: id=" + login.getUsername() + ", password=" + login.getPassword());

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "입력값을 확인해주세요.");
            return "login";
        }

        var userOpt = userService.login(login.getUsername(), login.getPassword());
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login";
        }

        // 로그인 성공 → 세션에 저장
        User user = userOpt.get();
        session.setAttribute("mdto", user);

        return "redirect:/chart";   // 로그인 후 차트 페이지로 이동
    }

    // 회원가입 페이지
    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("register", new Register());
        return "join";  // templates/join.html
    }

    // 회원가입 처리
    @PostMapping("/joinProc")
    public String joinProcess(@Valid Register register,
                              BindingResult bindingResult,
                              Model model) {

        System.out.println("회원가입 폼 submit: " + register); // <--- DTO 값 확인
        System.out.println("BindingResult has errors? " + bindingResult.hasErrors());

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "입력값을 확인해주세요.");
            return "join";
        }

        try {
            User savedUser = userService.register(register);
            System.out.println("DB 저장 완료: " + savedUser.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "join";
        }

        return "redirect:/user/login";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }
}