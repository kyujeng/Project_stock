package com.jh.dividendpj.company.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class CreateCompanyDto {
    @Getter
    @Setter // 테스트용
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    @ToString
    public static class Request {
        @NotBlank(message = "ticker를 입력해주세요.")
        private String ticker;
    }

    @Getter
    @Setter // 테스트용
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    @ToString
    public static class Response {
        private String companyName;
        private String ticker;
    }
}
