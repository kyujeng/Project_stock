package com.jh.dividendpj.company.exception;

import lombok.Getter;

@Getter
public class CompanyException extends RuntimeException {
    private CompanyErrorCode companyErrorCode;
    private String message;

    public CompanyException(CompanyErrorCode companyErrorCode, String message) {
        super(message);
        this.message = message;
        this.companyErrorCode = companyErrorCode;
    }
}
