package com.jh.dividendpj.scraper;

import com.jh.dividendpj.company.domain.Company;
import com.jh.dividendpj.dividend.domain.Dividend;
import com.jh.dividendpj.scraper.exception.ScraperErrorCode;
import com.jh.dividendpj.scraper.exception.ScraperException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class YahooScraper implements ScraperInterface<Company, Dividend> {
    private static final String DIVIDEND_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&frequency=1mo";
    private static final String COMPANY_URL = "https://finance.yahoo.com/quote/%s/p=%s";
    private static final long START_TIME = 86400; // 하루 (60 * 60 * 24)

    @Override
    public Company getCompany(String ticker) {
        Company company;
        try {
            String url = String.format(COMPANY_URL, ticker, ticker);
            Connection connect = Jsoup.connect(url);
            Document document = connect.get();
            Elements titleEle = document.getElementsByTag("h1");
            if (titleEle.isEmpty()) {
                log.error("회사 정보 스크랩 실패");
                throw new ScraperException(ScraperErrorCode.NOT_FOUND_TICKER, ScraperErrorCode.NOT_FOUND_TICKER.getMessage());
            }
            Element element = titleEle.get(1);
            String title = element.text().replaceAll("\\(.*\\)", "").trim();

            company = Company.builder()
                    .name(title)
                    .ticker(ticker)
                    .build();
        } catch (IOException e) {
            log.error("스크랩을 통해 회사 정보 가져오기 실패!! = {}", e.getMessage());
            company = null;
        }
        return company;
    }

    @Override
    public List<Dividend> getDividendList(Company company) {
        List<Dividend> list = new ArrayList<>();
        try {
            long end = System.currentTimeMillis() / 1000; // 현재 시간을 밀리초로 가져와 초로 변경
            String url = String.format(DIVIDEND_URL, company.getTicker(), START_TIME, end);
            Connection connect = Jsoup.connect(url);
            Document document = connect.get();

            Elements elements = document.getElementsByClass("table svelte-ewueuo");
            Element element = elements.get(0);

            Element tbody = element.children().get(1);
            for (Element child : tbody.children()) {
                String text = child.text();
                if (!text.endsWith("Dividend")) {
                    continue;
                }
                String[] split = text.split(" ");
                int month = Month.stringToMonth(split[0]);
                int day = Integer.parseInt(split[1].replace(",", ""));
                int year = Integer.parseInt(split[2]);
                String dividend = split[3];
                if (month == -1) {
                    throw new ScraperException(ScraperErrorCode.NOT_FOUND_MONTH, ScraperErrorCode.NOT_FOUND_MONTH.getMessage());
                }

                Dividend dividendBuild = Dividend.builder()
                        .dividend(dividend)
                        .date(LocalDateTime.of(year, month, day, 0, 0))
                        .company(company)
                        .build();
                list.add(dividendBuild);
            }
        } catch (IOException e) {
            log.error("스크랩을 통해 배당금 정보 가져오기 실패!! = {}", e.getMessage());
        }
        return list;
    }
}
