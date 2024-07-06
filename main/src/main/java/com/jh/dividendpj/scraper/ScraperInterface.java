package com.jh.dividendpj.scraper;

import java.util.List;

public interface ScraperInterface<T, U> {
    T getCompany(String ticker);

    List<U> getDividendList(T company);
}
