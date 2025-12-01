package com.hk.chart.service;

import com.hk.chart.dto.PortfolioDto;
import com.hk.chart.dto.RankingDto;
import com.hk.chart.entity.Portfolio;
import com.hk.chart.entity.User;
import com.hk.chart.repository.PortfolioRepository;
import com.hk.chart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final KisMarketService kisMarketService; // 실시간 시세용

    // 포트폴리오 조회 (실시간 계산 포함)
    @Transactional(readOnly = true)
    public List<PortfolioDto> getMyPortfolio(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<Portfolio> list = portfolioRepository.findAllByUser(user);
        List<PortfolioDto> result = new ArrayList<>();

        for (Portfolio p : list) {
            // 1. 실시간 현재가 조회
            int currentPrice = p.getAveragePrice(); // 실패 시 기본값은 매수가로 (수익률 0%)
            RankingDto priceInfo = kisMarketService.fetchCurrentPrice(p.getStockCode());
            
            if (priceInfo != null) {
                currentPrice = Integer.parseInt(priceInfo.getPrice());
            }

            // 2. 수익률 계산
            long totalInvested = (long) p.getAveragePrice() * p.getQuantity();
            long totalValue = (long) currentPrice * p.getQuantity();
            long profit = totalValue - totalInvested;
            double profitRate = 0.0;
            
            if (totalInvested > 0) {
                profitRate = ((double) profit / totalInvested) * 100;
            }

            result.add(PortfolioDto.builder()
                    .id(p.getId())
                    .code(p.getStockCode())
                    .name(p.getStockName())
                    .averagePrice(p.getAveragePrice())
                    .quantity(p.getQuantity())
                    .totalInvested(totalInvested)
                    .currentPrice(currentPrice)
                    .totalValue(totalValue)
                    .profit(profit)
                    .profitRate(Math.round(profitRate * 100) / 100.0)
                    .build());
            
            // API 부하 방지
            try { Thread.sleep(50); } catch (Exception e) {}
        }
        return result;
    }

    // 종목 추가
    @Transactional
    public void addPortfolio(String username, String code, String name, int price, int quantity) {
        User user = userRepository.findByUsername(username).orElseThrow();
        
        portfolioRepository.save(Portfolio.builder()
                .user(user)
                .stockCode(code)
                .stockName(name)
                .averagePrice(price)
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .build());
    }

    // 삭제
    @Transactional
    public void deletePortfolio(String username, Long id) {
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow();
        if (!portfolio.getUser().getUsername().equals(username)) {
            throw new RuntimeException("권한이 없습니다.");
        }
        portfolioRepository.delete(portfolio);
    }
}