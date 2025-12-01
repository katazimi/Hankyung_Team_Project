package com.hk.chart.controller;

import com.hk.chart.dto.PortfolioDto;
import com.hk.chart.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    // 1. 포트폴리오 페이지 화면
    @GetMapping("/portfolio")
    public String portfolioPage(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "portfolio"; // portfolio.html 반환
    }

    // 2. [API] 포트폴리오 데이터 조회
    @GetMapping("/api/portfolio")
    @ResponseBody
    public List<PortfolioDto> getMyPortfolio(Principal principal) {
        if (principal == null) return List.of();
        return portfolioService.getMyPortfolio(principal.getName());
    }

    // 3. [API] 포트폴리오 추가
    @PostMapping("/api/portfolio")
    @ResponseBody
    public ResponseEntity<String> addPortfolio(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam int price,
            @RequestParam int quantity,
            Principal principal) {
        
        portfolioService.addPortfolio(principal.getName(), code, name, price, quantity);
        return ResponseEntity.ok("추가되었습니다.");
    }

    // 4. [API] 삭제
    @DeleteMapping("/api/portfolio/{id}")
    @ResponseBody
    public ResponseEntity<String> deletePortfolio(@PathVariable Long id, Principal principal) {
        portfolioService.deletePortfolio(principal.getName(), id);
        return ResponseEntity.ok("삭제되었습니다.");
    }
}