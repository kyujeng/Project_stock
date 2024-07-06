package com.jh.dividendpj.dividend.service;

import com.jh.dividendpj.company.domain.Company;
import com.jh.dividendpj.dividend.domain.Dividend;
import com.jh.dividendpj.scraper.YahooScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DividendService {
    private final YahooScraper yahooScraper;

    public List<Dividend> getDividendInfo(Company company) {
        return yahooScraper.getDividendList(company);
    }
}
