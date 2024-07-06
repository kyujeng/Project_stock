package com.jh.dividendpj.config;

import com.jh.dividendpj.company.domain.Company;
import com.jh.dividendpj.company.repository.CompanyRepository;
import com.jh.dividendpj.dividend.repository.DividendRepository;
import com.jh.dividendpj.scraper.YahooScraper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScrapJobConfigTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRegistry jobRegistry;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DividendRepository dividendRepository;

    @Autowired
    private YahooScraper yahooScraper;

    @BeforeEach
    void before(){
        Company company = yahooScraper.getCompany("coke");
        companyRepository.save(company);
    }

    @Test
    void batchTest(){
        long beforeCount = dividendRepository.count();

        String time = LocalDateTime.now().toString();
        try{
            Job scrapJob = jobRegistry.getJob("scrapJob");
            JobParametersBuilder jobParam = new JobParametersBuilder().addString("time", time);
            jobLauncher.run(scrapJob, jobParam.toJobParameters());
        }catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
                JobParametersInvalidException | JobRestartException e){
            throw new RuntimeException(e);
        }

        long afterCount = dividendRepository.count();

        assertThat(beforeCount).isEqualTo(0);
        assertThat(afterCount).isNotEqualTo(0);
    }
}