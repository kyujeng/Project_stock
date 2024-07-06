package com.jh.dividendpj.company.controller;

import com.jh.dividendpj.company.dto.CompanyDto;
import com.jh.dividendpj.company.dto.CompanyWithDividendDto;
import com.jh.dividendpj.company.dto.CreateCompanyDto;
import com.jh.dividendpj.company.exception.CompanyErrorCode;
import com.jh.dividendpj.company.service.CompanyService;
import com.jh.dividendpj.config.CacheKey;
import com.jh.dividendpj.config.GlobalApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class CompanyController {
    private final CompanyService companyService;
    private final CacheManager redisCacheManager;

    // 회사 생성
    @PostMapping("/company")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<GlobalApiResponse<CreateCompanyDto.Response>> create(@Valid @RequestBody CreateCompanyDto.Request request) {
        log.info("입력받은 ticker={}", request.getTicker());

        request = request.toBuilder()
                .ticker(request.getTicker().trim())
                .build();
        CreateCompanyDto.Response company = companyService.createCompany(request);
        return ResponseEntity.ok(GlobalApiResponse.toGlobalApiResponse(company));
    }

    // 회사 삭제
    @DeleteMapping(value = {"/company/", "/company/{ticker}"})
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<GlobalApiResponse<?>> delete(@PathVariable Optional<String> ticker) {
        log.info("삭제할 ticker={}", ticker);

        String notEmptyTicker = ticker
                .orElseThrow(() -> new IllegalArgumentException(CompanyErrorCode.EMPTY_TICKER.getMessage()))
                .trim();
        if (notEmptyTicker.isEmpty()) {
            throw new IllegalArgumentException(CompanyErrorCode.EMPTY_TICKER.getMessage());
        }

        String companyName = companyService.deleteCompany(notEmptyTicker);
        clearFinanceCache(companyName);
        GlobalApiResponse<?> response = GlobalApiResponse.builder()
                .message("성공")
                .status(200)
                .build();
        return ResponseEntity.ok(response);
    }

    // 자동완성 기능을 위한 API
    @GetMapping("/company/autocomplete")
    public ResponseEntity<GlobalApiResponse<List<CompanyDto.Response>>> autoComplete(@Valid @RequestBody CompanyDto.Request request) {
        log.info("검색한 단어={}", request.getPrefix());

        request = request.toBuilder()
                .prefix(request.getPrefix().trim())
                .build();
        List<CompanyDto.Response> autoComplete = companyService.getAutoComplete(request);
        return ResponseEntity.ok(GlobalApiResponse.toGlobalApiResponse(autoComplete));
    }

    // 회사 정보와 배당금 정보 조회 컨트롤러
    @GetMapping(value = {"/finance/dividend/", "/finance/dividend/{companyName}"})
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<GlobalApiResponse<CompanyWithDividendDto.Response>> getCompanyWithDividend(@PathVariable Optional<String> companyName) {
        log.info("조회할 회사명={}", companyName);

        String notEmptyCompanyName = companyName
                .orElseThrow(() -> new IllegalArgumentException(CompanyErrorCode.EMPTY_COMPANY_NAME.getMessage()))
                .trim();
        if (notEmptyCompanyName.isEmpty()) {
            throw new IllegalArgumentException(CompanyErrorCode.EMPTY_COMPANY_NAME.getMessage());
        }

        CompanyWithDividendDto.Response companyInfo = companyService.getCompanyInfo(notEmptyCompanyName);
        return ResponseEntity.ok(GlobalApiResponse.toGlobalApiResponse(companyInfo));
    }

    // 현재 관리하고 있는 모든 회사 리스트 조회
    @GetMapping("/company")
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<GlobalApiResponse<Page<CompanyDto.Response>>> getAllCompany(@PageableDefault(size = 10, sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CompanyDto.Response> allCompany = companyService.getAllCompany(pageable);
        return ResponseEntity.ok(GlobalApiResponse.toGlobalApiResponse(allCompany));
    }

    // db에서 회사 삭제시 캐시에서도 삭제
    private void clearFinanceCache(String companyName) {
        Cache cache = redisCacheManager.getCache(CacheKey.KEY_FINANCE);
        if (cache != null) {
            log.info("캐시에도 데이터가 있어 삭제합니다.");
            cache.evict(companyName);
        }
    }
}
