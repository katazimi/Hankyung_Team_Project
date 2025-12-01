package com.hk.chart.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioDto {
    private Long id;            // 포트폴리오 ID (삭제/수정용)
    private String code;        // 종목코드
    private String name;        // 종목명
    
    // 입력 데이터
    private int averagePrice;   // 평단가
    private int quantity;       // 수량
    private long totalInvested; // 총 매수금액 (평단가 * 수량)

    // 실시간 데이터
    private int currentPrice;   // 현재가
    private long totalValue;    // 평가금액 (현재가 * 수량)
    private long profit;        // 평가손익 (평가금액 - 매수금액)
    private double profitRate;  // 수익률
}