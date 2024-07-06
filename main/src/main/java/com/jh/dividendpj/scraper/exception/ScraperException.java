package com.jh.dividendpj.scraper.exception;

import lombok.Getter;

@Getter
public class ScraperException extends RuntimeException {
    private String message;
    private ScraperErrorCode scraperErrorCode;

    public ScraperException(ScraperErrorCode scraperErrorCode, String message) {
        super(message);
        this.message = message;
        this.scraperErrorCode = scraperErrorCode;
    }
}
