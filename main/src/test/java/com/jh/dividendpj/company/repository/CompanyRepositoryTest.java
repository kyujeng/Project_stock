package com.jh.dividendpj.company.repository;

import com.jh.dividendpj.company.domain.Company;
import com.jh.dividendpj.company.exception.CompanyErrorCode;
import com.jh.dividendpj.company.exception.CompanyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CompanyRepositoryTest {
    @Autowired
    private CompanyRepository companyRepository;

    Company company;

    @BeforeEach
    void before() {
        Company build1 = Company.builder()
                .name("test")
                .ticker("ticker")
                .build();
        company = companyRepository.save(build1);

        Company build2 = Company.builder()
                .ticker("ticker2")
                .name("abc")
                .build();
        companyRepository.save(build2);
    }

    @Test
    @DisplayName("ticker를 통해 회사 찾기")
    void findByTicker() {
        Company find = companyRepository.findByTicker("ticker").orElseThrow(() -> new CompanyException(CompanyErrorCode.NOT_FOUND_TICKER, CompanyErrorCode.NOT_FOUND_TICKER.getMessage()));

        assertThat(find.getName()).isEqualTo("test");
    }

    @Test
    @DisplayName("ticker를 통해 회사 찾기 실패")
    void failFindByTicker() {
        Company find = companyRepository.findByTicker("ti").orElse(null);

        assertThat(find).isNull();
    }

    @Test
    @DisplayName("회사 이름을 통해 회사 찾기")
    void findByName() {
        Company find = companyRepository.findByName("test").orElse(null);

        assertThat(find.getTicker()).isEqualTo("ticker");
    }

    @Test
    @DisplayName("회사 이름을 통해 회사 찾기 실패")
    void failFindByName() {
        Company find = companyRepository.findByName("te").orElse(null);

        assertThat(find).isNull();
    }

    @Test
    @DisplayName("자동완성을 위한 찾기 기능")
    void autoComplete() {
        List<Company> list = companyRepository.findTop10ByNameStartingWithIgnoreCaseOrNameContainingIgnoreCaseOrderByNameDesc("t", "t");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("test");
    }

    @Test
    @DisplayName("자동완성을 위한 찾기 기능 실패")
    void failAutoComplete() {
        List<Company> list = companyRepository.findTop10ByNameStartingWithIgnoreCaseOrNameContainingIgnoreCaseOrderByNameDesc("d", "d");

        assertThat(list).hasSize(0);
    }
}