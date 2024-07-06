package com.jh.dividendpj.config;

import com.jh.dividendpj.company.domain.Company;
import com.jh.dividendpj.company.repository.CompanyRepository;
import com.jh.dividendpj.dividend.domain.Dividend;
import com.jh.dividendpj.dividend.repository.DividendRepository;
import com.jh.dividendpj.scraper.YahooScraper;
import com.jh.dividendpj.scraper.exception.ScraperErrorCode;
import com.jh.dividendpj.scraper.exception.ScraperException;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ScrapJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final YahooScraper yahooScraper;

    @Value("${job.scrap.job-name}")
    private String jobName;

    @Value("${job.scrap.step-name}")
    private String stepName;

    @Bean
    public Job companyJob(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return new JobBuilder(jobName, jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(scrapStep(jobRepository, transactionManager))
                .end()
                .build();
    }

    @Bean
    @JobScope
    public Step scrapStep(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return new StepBuilder(stepName, jobRepository)
                .<Company, Company>chunk(10, transactionManager)
                .reader(scrapItemReader())
                .processor(scrapItemProcessor())
                .writer(scrapItemWriter())
                .listener(stepExecutionListener())
                .faultTolerant()
                .skip(ScraperException.class)
                .skipLimit(2)
                .build();
    }

    // step 실행 전 조건 체크(조건 : 저장된 company 가 있는 경우에만 실행)
    @Bean
    public StepExecutionListener stepExecutionListener(){
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                long companyCount = companyRepository.count();
                if(companyCount == 0){
                    log.info("DB에 저장된 회사 엔티티가 없으므로 batch 를 실행하지 않습니다.");
                    stepExecution.setExitStatus(ExitStatus.COMPLETED);
                }
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                return null;
            }
        };
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Company> scrapItemReader(){
        return new JpaPagingItemReaderBuilder<Company>()
                .pageSize(10)
                .queryString("SELECT c FROM Company c ORDER BY id ASC")
                .entityManagerFactory(entityManagerFactory)
                .name("JpaPagingItemReader")
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Company, Company> scrapItemProcessor(){
        return company -> {
            try{
                Thread.sleep(3000);
            }catch (InterruptedException e){
                log.error("Thread sleep error: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }

            log.info("batch 작업 실행 중 Processor : {} 회사에 대한 배당금 정보 스크랩 하는 중", company.getName());
            List<Dividend> dividendList = yahooScraper.getDividendList(company);
            if(dividendList.isEmpty()){
                throw new ScraperException(ScraperErrorCode.NOT_FOUND_DIVIDEND, ScraperErrorCode.NOT_FOUND_DIVIDEND.getMessage());
            }

            List<Dividend> companyDividendList = company.getDevidendList();
            for (Dividend dividend : dividendList) {
                boolean exist = dividendRepository.existsByCompanyAndDate(dividend.getCompany(), dividend.getDate());
                if(!exist){
                    log.info("새롭게 업데이트 할 배당금 정보 -> 날짜 : {}, 배당금 : {}", dividend.getDate(), dividend.getDividend());
                    companyDividendList.add(dividend);
                }
            }
            return company.toBuilder()
                    .devidendList(companyDividendList)
                    .build();
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Company> scrapItemWriter(){
        return items -> {
            log.info("batch 작업 실행 중 Writer : company 엔티티 저장 실행");
            for (Company item : items) {
                log.info("{} 회사에 대한 엔티티 저장 중", item.getName());
                companyRepository.save(item);
            }
        };
    }
}
