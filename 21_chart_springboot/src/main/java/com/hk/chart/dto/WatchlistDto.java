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
    private Integer price;    // 현재가 (DB 최근 종가)
    private Double change;    // 등락률 (전일 대비)
}