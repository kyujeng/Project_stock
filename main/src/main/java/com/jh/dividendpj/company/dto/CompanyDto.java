package com.jh.dividendpj.company.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class CompanyDto {
    @Getter
    @Setter // 테스트용
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    @ToString
    public static class Request {
        @NotBlank(message = "검색할 단어를 입력하세요.")
        private String prefix;
    }

    @Getter
    @Setter // 테스트용
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    @ToString
    public static class Response {
        private String name;
        private String ticker;
    }
}
