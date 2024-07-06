package com.jh.dividendpj.scraper.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScraperErrorCode {
    NOT_FOUND_TICKER(400, "해당 사이트에 ticker로 등록된 회사 정보가 없어 스크랩할 수 없습니다."),
    NOT_FOUND_MONTH(400, "Month enum에서 해당하는 month를 찾을 수 없습니다."),
    NOT_FOUND_DIVIDEND(400, "해당 사이트에 해당 회사의 배당금 정보가 없어 스크랩할 수 없습니다.");

    private int status;
    private String message;
}
