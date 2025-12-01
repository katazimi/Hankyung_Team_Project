package com.hk.chart.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RankingDto {
    private String code;   // 종목코드 (stck_shrn_iscd)
    private String name;   // 종목명 (hts_kor_isnm)
    private String price;  // 현재가 (stck_prpr)
    private String change; // 전일대비 (prdy_vrss)
    private String rate;   // 등락률 (prdy_ctrt)
    private String rank;   // 순위 (data_rank)
}