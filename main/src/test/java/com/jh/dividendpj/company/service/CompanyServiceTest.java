package com.jh.dividendpj.company.service;

import com.jh.dividendpj.company.domain.Company;
import com.jh.dividendpj.company.dto.CompanyDto;
import com.jh.dividendpj.company.dto.CompanyWithDividendDto;
import com.jh.dividendpj.company.dto.CreateCompanyDto;
import com.jh.dividendpj.company.exception.CompanyErrorCode;
import com.jh.dividendpj.company.exception.CompanyException;
import com.jh.dividendpj.company.repository.CompanyRepository;
import com.jh.dividendpj.scraper.exception.ScraperErrorCode;
import com.jh.dividendpj.scraper.exception.ScraperException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CompanyServiceTest {
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyService companyService;

    String ticker;
    CreateCompanyDto.Request createRequest;
    String companyName;
    CompanyDto.Request autoCompleteRequest;
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));

    @BeforeEach
    void before() {
        ticker = "coke";
        companyName = "Coca-Cola Consolidated, Inc.";

        createRequest = CreateCompanyDto.Request.builder()
                .ticker("coke")
                .build();

        autoCompleteRequest = CompanyDto.Request.builder()
                .prefix("c")
                .build();
    }

    @Test
    @DisplayName("회사 생성")
    void createCompany() {
        CreateCompanyDto.Response company = companyService.createCompany(createRequest);

        assertThat(company.getCompanyName()).isEqualTo(companyName);
    }

    @Test
    @DisplayName("회사 생성 실패 - 스크랩 실패")
    void failCreateCompany() {
        try {
            companyService.createCompany(createRequest.toBuilder().ticker("가").build());
        } catch (ScraperException e) {
            assertThat(e.getScraperErrorCode().getMessage()).isEqualTo(ScraperErrorCode.NOT_FOUND_TICKER.getMessage());
        }
    }

    @Test
    @DisplayName("회사 생성 실패 - 이미 db에 있는 회사")
    void failCreateCompany2() {
        companyService.createCompany(createRequest);
        try {
            companyService.createCompany(createRequest);
        } catch (CompanyException e) {
            assertThat(e.getCompanyErrorCode().getMessage()).isEqualTo(CompanyErrorCode.ALREADY_EXIST_COMPANY.getMessage());
        }
    }

    @Test
    @DisplayName("회사 삭제")
    void deleteCompany() {
        companyService.createCompany(createRequest);
        companyService.deleteCompany(ticker);
        Company company = companyRepository.findByTicker(ticker).orElse(null);

        assertThat(company).isNull();
    }

    @Test
    @DisplayName("회사 삭제 실패")
    void failDeleteCompany() {
        try {
            companyService.deleteCompany("test");
        } catch (CompanyException e) {
            assertThat(e.getCompanyErrorCode().getMessage()).isEqualTo(CompanyErrorCode.NOT_FOUND_TICKER.getMessage());
        }
    }

    @Test
    @DisplayName("회사 정보와 배당금 정보 조회")
    void getCompanyInfo() {
        companyService.createCompany(createRequest);
        CompanyWithDividendDto.Response companyInfo = companyService.getCompanyInfo(companyName);

        assertThat(companyInfo.getDividendDtoList()).isNotEmpty();
    }

    @Test
    @DisplayName("회사 정보와 배당금 정보 조회 실패")
    void failGetCompanyInfo() {
        companyService.createCompany(createRequest);
        try {
            companyService.getCompanyInfo("test");
        } catch (CompanyException e) {
            assertThat(e.getCompanyErrorCode().getMessage()).isEqualTo(CompanyErrorCode.NOT_FOUND_NAME.getMessage());
        }
    }

    @Test
    @DisplayName("자동완성을 위한 기능")
    void autoComplete() {
        companyService.createCompany(createRequest);
        List<CompanyDto.Response> autoComplete = companyService.getAutoComplete(autoCompleteRequest);

        assertThat(autoComplete).hasSize(1);
    }

    @Test
    @DisplayName("현재 관리하고있는 모든 회사 리스트를 페이징하여 조회")
    void getAllCompany() {
        companyService.createCompany(createRequest);
        Page<CompanyDto.Response> allCompany = companyService.getAllCompany(pageable);

        assertThat(allCompany.getTotalElements()).isEqualTo(1);
        assertThat(allCompany.getTotalPages()).isEqualTo(1);
    }
}