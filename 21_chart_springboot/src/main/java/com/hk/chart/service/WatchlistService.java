package com.hk.chart.service;

import com.hk.chart.dto.WatchlistDto;
import com.hk.chart.entity.StockCandle;
import com.hk.chart.entity.User;
import com.hk.chart.entity.Watchlist;
import com.hk.chart.repository.StockCandleRepository;
import com.hk.chart.repository.UserRepository;
import com.hk.chart.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final StockCandleRepository candleRepository;

    // 관심종목 목록 조회 (가격 정보 포함)
    @Transactional(readOnly = true)
    public List<WatchlistDto> getUserWatchlist(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        List<Watchlist> list = watchlistRepository.findAllByUser(user);
        
        List<WatchlistDto> result = new ArrayList<>();
        
        for (Watchlist w : list) {
            // 해당 종목의 최신 데이터 2개를 가져와서 등락률 계산 (오늘, 어제)
            List<StockCandle> candles = candleRepository.findRecentCandles(w.getStockCode(), 2);
            
            int currentPrice = 0;
            double changeRate = 0.0;

            if (!candles.isEmpty()) {
                currentPrice = candles.get(0).getClose(); // 최신 종가
                
                if (candles.size() > 1) {
                    int prevClose = candles.get(1).getClose();
                    changeRate = ((double)(currentPrice - prevClose) / prevClose) * 100;
                }
            }

            result.add(WatchlistDto.builder()
                    .code(w.getStockCode())
                    .name(w.getStockName())
                    .price(currentPrice)
                    .change(Math.round(changeRate * 100) / 100.0) // 소수점 2자리 반올림
                    .build());
        }
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