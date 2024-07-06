package com.jh.dividendpj.company.dto;

import com.jh.dividendpj.dividend.dto.JoinDividendDto;
import lombok.*;

import java.util.List;

public class CompanyWithDividendDto {
    @Getter
    @Setter // 테스트용
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    @ToString
    public static class Response {
        private String name;
        private String ticker;
        private List<JoinDividendDto.Response> dividendDtoList;
    }
}
