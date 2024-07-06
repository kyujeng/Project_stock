package com.jh.dividendpj.config;

import lombok.*;

@Getter
@Setter // 테스트용
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@ToString
public class GlobalApiResponse<T> {
    private int status;
    private String message;
    private T result;

    // api 응답 성공 시 apiResponse로 반환 메소드
    public static <T> GlobalApiResponse<T> toGlobalApiResponse(T data) {
        return GlobalApiResponse.<T>builder()
                .message("성공")
                .status(200)
                .result(data)
                .build();
    }
}
