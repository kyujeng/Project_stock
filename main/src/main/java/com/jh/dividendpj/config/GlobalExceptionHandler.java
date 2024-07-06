package com.jh.dividendpj.config;

import com.jh.dividendpj.company.exception.CompanyException;
import com.jh.dividendpj.member.exception.MemberException;
import com.jh.dividendpj.scraper.exception.ScraperException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<List<GlobalApiResponse<?>>> handleValidException(MethodArgumentNotValidException e) {
        log.error("request 유효성 검사 실패");

        List<GlobalApiResponse<?>> list = new ArrayList<>();
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldError = bindingResult.getFieldErrors();
        for (FieldError error : fieldError) {
            GlobalApiResponse<?> response = GlobalApiResponse.builder()
                    .status(400)
                    .message(error.getDefaultMessage())
                    .build();
            list.add(response);
        }

        return new ResponseEntity<>(list, HttpStatus.resolve(e.getStatusCode().value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<GlobalApiResponse<?>> handleArgumentException(IllegalArgumentException e) {
        log.error("pathVariable 유효성 검사 실패");

        GlobalApiResponse<?> response = GlobalApiResponse.builder()
                .message(e.getMessage())
                .status(400)
                .build();
        return new ResponseEntity<>(response, HttpStatus.resolve(400));
    }

    @ExceptionHandler(CompanyException.class)
    private ResponseEntity<GlobalApiResponse<?>> handleCompanyException(CompanyException e) {
        log.error(e.getMessage());
        GlobalApiResponse<?> response = GlobalApiResponse.builder()
                .status(e.getCompanyErrorCode().getStatus())
                .message(e.getCompanyErrorCode().getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.resolve(e.getCompanyErrorCode().getStatus()));
    }

    @ExceptionHandler(ScraperException.class)
    private ResponseEntity<GlobalApiResponse<?>> handleScraperException(ScraperException e) {
        log.error(e.getMessage());
        GlobalApiResponse<?> response = GlobalApiResponse.builder()
                .status(e.getScraperErrorCode().getStatus())
                .message(e.getScraperErrorCode().getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.resolve(e.getScraperErrorCode().getStatus()));
    }

    @ExceptionHandler(MemberException.class)
    private ResponseEntity<GlobalApiResponse<?>> handleMemberException(MemberException e) {
        log.error(e.getMessage());

        GlobalApiResponse<?> response = GlobalApiResponse.builder()
                .status(e.getMemberErrorCode().getStatus())
                .message(e.getMemberErrorCode().getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.resolve(e.getMemberErrorCode().getStatus()));
    }

    // 예상하지 않은 에러에 대한 처리
    @ExceptionHandler(Exception.class)
    private ResponseEntity<GlobalApiResponse<?>> handleNotExpectedException(Exception e) {
        GlobalApiResponse<?> response = GlobalApiResponse.builder()
                .status(500)
                .message("예상치 못한 문제가 발생했습니다. 서버에 문의해주세요.")
                .build();
        return ResponseEntity.ok(response);
    }
}
