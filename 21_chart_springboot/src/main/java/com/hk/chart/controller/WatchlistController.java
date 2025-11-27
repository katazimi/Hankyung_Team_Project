package com.hk.chart.controller;

import com.hk.chart.dto.WatchlistDto;
import com.hk.chart.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController // ⭐️ 모든 응답이 JSON이 됩니다 (@ResponseBody 생략 가능)
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    // 1. 내 관심종목 목록 조회
    @GetMapping
    public List<WatchlistDto> getMyWatchlist(Principal principal) {
        if (principal == null) return List.of();
        return watchlistService.getUserWatchlist(principal.getName());
    }

    // 2. 관심종목 추가
    @PostMapping
    public ResponseEntity<String> addWatchlist(
            @RequestParam String code,
            @RequestParam String name,
            Principal principal) {
        
        if (principal == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        
        try {
            watchlistService.addWatchlist(principal.getName(), code, name);
            return ResponseEntity.ok("추가되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. 관심종목 삭제
    @DeleteMapping("/{code}")
    public ResponseEntity<String> removeWatchlist(
            @PathVariable String code,
            Principal principal) {
        
        if (principal == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        
        watchlistService.removeWatchlist(principal.getName(), code);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    // 4. 관심종목 여부 확인 (별 버튼 색상용)
    @GetMapping("/check/{code}")
    public boolean checkWatchlist(@PathVariable String code, Principal principal) {
        if (principal == null) return false;
        return watchlistService.isWatched(principal.getName(), code);
    }
}