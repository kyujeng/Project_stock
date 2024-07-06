package com.jh.dividendpj.dividend.domain;

import com.jh.dividendpj.company.domain.Company;
import com.jh.dividendpj.dividend.dto.JoinDividendDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter // 테스트용
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@ToString
@SQLRestriction("del_date IS NULL")
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"company_id", "date"}
        )
)
public class Dividend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private String dividend;

    @Column
    private LocalDateTime delDate;

    // Dividend -> JoinDividendDto.Response
    public JoinDividendDto.Response toJoinDividendDto() {
        return JoinDividendDto.Response.builder()
                .dividend(dividend)
                .date(date)
                .build();
    }
}
