package com.jh.dividendpj.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberErrorCode {
    NOT_FOUND_USERNAME(404, "존재하지 않는 아이디입니다."),
    NOT_MATCH_PASSWORD(404, "비밀번호가 일치하지 않습니다."),
    ALREADY_EXIST_USERNAME(400, "이미 사용중인 아이디입니다.");

    private int status;
    private String message;
}
