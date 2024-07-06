package com.jh.dividendpj.dividend.repository;

import com.jh.dividendpj.company.domain.Company;
import com.jh.dividendpj.dividend.domain.Dividend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DividendRepository extends JpaRepository<Dividend, Long> {
    boolean existsByCompanyAndDate(Company company, LocalDateTime date);
}
