package com.hk.chart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistDto {
    private String code;
    private String name;
    private String price;  // 현재가 (예: "78,000")
    private String change; // 전일대비 (예: "-500")
    private String rate;   // 등락률 (예: "-0.64")
}