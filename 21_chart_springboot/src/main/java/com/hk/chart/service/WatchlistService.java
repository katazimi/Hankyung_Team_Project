package com.hk.chart.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hk.chart.dto.RankingDto;
import com.hk.chart.dto.WatchlistDto;
import com.hk.chart.entity.StockCandle;
import com.hk.chart.entity.User;
import com.hk.chart.entity.Watchlist;
import com.hk.chart.repository.StockCandleRepository;
import com.hk.chart.repository.UserRepository;
import com.hk.chart.repository.WatchlistRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final StockCandleRepository candleRepository;
    private final KisMarketService kisMarketService;

    // 관심종목 목록 조회 (가격 정보 포함)
    @Transactional(readOnly = true)
    public List<WatchlistDto> getUserWatchlist(String username) {
        System.out.println(">>> [Watchlist] 조회 요청 들어옴: " + username);

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        List<Watchlist> list = watchlistRepository.findAllByUser(user);
        
        List<WatchlistDto> result = new ArrayList<>();
        
        for (Watchlist w : list) {
            String currentPrice = "-";
            String changeAmount = "0";
            String changeRate = "0.00";
            
            // 1. API 호출 시도
            try {
                // KisMarketService에 fetchCurrentPrice가 구현되어 있어야 함
                RankingDto apiData = kisMarketService.fetchCurrentPrice(w.getStockCode());
                
                if (apiData != null) {
                    currentPrice = apiData.getPrice(); // 이미 String
                    changeAmount = apiData.getChange();
                    changeRate = apiData.getRate();
                    System.out.println(">>> [Watchlist] API 성공 (" + w.getStockName() + "): " + currentPrice);
                } else {
                    System.out.println(">>> [Watchlist] API 응답 없음 (" + w.getStockName() + ") -> DB 조회 시도");
                    throw new Exception("API data null");
                }
            } catch (Exception e) {
                // 2. 실패 시 DB(Candle) 값 사용
                List<StockCandle> candles = candleRepository.findRecentCandles(w.getStockCode(), 2);
                if (!candles.isEmpty()) {
                    currentPrice = String.valueOf(candles.get(0).getClose());
                    if (candles.size() > 1) {
                        int prev = candles.get(1).getClose();
                        int curr = candles.get(0).getClose();
                        double rate = ((double)(curr - prev) / prev) * 100;
                        changeAmount = String.valueOf(curr - prev);
                        changeRate = String.format("%.2f", rate);
                    }
                }
            }

            result.add(WatchlistDto.builder()
                    .code(w.getStockCode())
                    .name(w.getStockName())
                    .price(currentPrice) // String 타입
                    .change(changeAmount)
                    .rate(changeRate)
                    .build());
        }
        
        // 0.05초 대기 (API 부하 방지)
        try { Thread.sleep(50); } catch (Exception e) {}
        
        return result;
    }

    // 관심종목 추가
    @Transactional
    public void addWatchlist(String username, String code, String name) {
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!watchlistRepository.existsByUserAndStockCode(user, code)) {
            watchlistRepository.save(Watchlist.builder()
                    .user(user)
                    .stockCode(code)
                    .stockName(name)
                    .build());
        }
    }

    // 관심종목 삭제
    @Transactional
    public void removeWatchlist(String username, String code) {
        User user = userRepository.findByUsername(username).orElseThrow();
        watchlistRepository.deleteByUserAndStockCode(user, code);
    }
    
    // 관심종목 여부 확인
    public boolean isWatched(String username, String code) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return watchlistRepository.existsByUserAndStockCode(user, code);
    }
}