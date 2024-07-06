package com.jh.dividendpj.company.service;

import com.jh.dividendpj.company.domain.Company;
import com.jh.dividendpj.company.dto.CompanyDto;
import com.jh.dividendpj.company.dto.CompanyWithDividendDto;
import com.jh.dividendpj.company.dto.CreateCompanyDto;
import com.jh.dividendpj.company.exception.CompanyErrorCode;
import com.jh.dividendpj.company.exception.CompanyException;
import com.jh.dividendpj.company.repository.CompanyRepository;
import com.jh.dividendpj.config.CacheKey;
import com.jh.dividendpj.dividend.domain.Dividend;
import com.jh.dividendpj.dividend.service.DividendService;
import com.jh.dividendpj.scraper.YahooScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final YahooScraper yahooScraper;
    private final DividendService dividendService;

    /**
     * 회사 정보 생성
     *
     * @param request 회사의 ticker
     * @return 생성된 회사 정보
     */
    public CreateCompanyDto.Response createCompany(CreateCompanyDto.Request request) {
        String ticker = request.getTicker();
        Company company = companyRepository.findByTicker(ticker).orElse(null);
        if (company != null) {
            throw new CompanyException(CompanyErrorCode.ALREADY_EXIST_COMPANY, CompanyErrorCode.ALREADY_EXIST_COMPANY.getMessage());
        }
        company = yahooScraper.getCompany(ticker);
        companyRepository.save(company);

        List<Dividend> dividendInfo = dividendService.getDividendInfo(company);
        Company withDividend = company.toBuilder()
                .devidendList(dividendInfo)
                .build();
        Company save = companyRepository.save(withDividend);
        return save.toCreateResponseDto();
    }

    /**
     * 회사 삭제
     *
     * @param ticker 삭제할 회사의 ticker
     */
    public String deleteCompany(String ticker) {
        Company company = companyRepository.findByTicker(ticker).orElseThrow(() -> new CompanyException(CompanyErrorCode.NOT_FOUND_TICKER, CompanyErrorCode.NOT_FOUND_TICKER.getMessage()));
        companyRepository.delete(company);
        return company.getName();
    }

    /**
     * 회사 정보와 배당금 정보 조회
     *
     * @param companyName 조회할 회사 이름
     * @return 조회된 회사 정보와 배당금 정보
     */
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    @Transactional(readOnly = true)
    public CompanyWithDividendDto.Response getCompanyInfo(String companyName) {
        log.info("redis에 데이터가 없어 스크랩하여 가져옵니다. company : {}", companyName);

        Company company = companyRepository.findByName(companyName)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.NOT_FOUND_NAME, CompanyErrorCode.NOT_FOUND_NAME.getMessage()));
        return company.toCompanyWithDividendDto();
    }

    /**
     * 검색한 단어에 해당하는 회사명 중 10개를 정렬하여 조회
     *
     * @param request 검색할 회사명
     * @return 조회된 회사 리스트
     */
    @Transactional(readOnly = true)
    public List<CompanyDto.Response> getAutoComplete(CompanyDto.Request request) {
        String prefix = request.getPrefix();
        return companyRepository.findTop10ByNameStartingWithIgnoreCaseOrNameContainingIgnoreCaseOrderByNameDesc(prefix, prefix)
                .stream().map(Company::toCompanyResponseDto).toList();
    }

    /**
     * 현재 관리하고있는 모든 회사 리스트를 페이징하여 조회
     *
     * @param pageable 페이징 처리
     * @return 페이징 처리된 모든 회사 리스트
     */
    @Transactional(readOnly = true)
    public Page<CompanyDto.Response> getAllCompany(Pageable pageable) {
        Page<Company> all = companyRepository.findAll(pageable);
        List<CompanyDto.Response> list = all.getContent().stream()
                .map(Company::toCompanyResponseDto)
                .toList();
        return new PageImpl<>(list, pageable, list.size());
    }
}
