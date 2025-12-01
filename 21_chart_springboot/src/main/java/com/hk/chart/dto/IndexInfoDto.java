package com.hk.chart.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndexInfoDto {
    private String price;  // 현재 지수 (예: 2500.12)
    private String sign;   // 전일대비 부호 (1:상한, 2:상승, 3:보합, 4:하한, 5:하락)
    private String change; // 전일대비 값 (예: 10.5)
    private String rate;   // 등락률 (예: 0.45)
}