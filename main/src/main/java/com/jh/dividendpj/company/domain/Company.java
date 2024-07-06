package com.jh.dividendpj.company.domain;

import com.jh.dividendpj.company.dto.CompanyDto;
import com.jh.dividendpj.company.dto.CompanyWithDividendDto;
import com.jh.dividendpj.company.dto.CreateCompanyDto;
import com.jh.dividendpj.dividend.domain.Dividend;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter // 테스트용
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@SQLDelete(sql = "UPDATE company SET del_date = now() WHERE id=?")
@SQLRestriction("del_date IS NULL")
@ToString
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<Dividend> devidendList;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String ticker;

    @Column
    private LocalDateTime delDate;

    // Company -> CreateCompanyDto.Response
    public CreateCompanyDto.Response toCreateResponseDto() {
        return CreateCompanyDto.Response.builder()
                .companyName(name)
                .ticker(ticker)
                .build();
    }

    // Company -> AutoCompleteDto.Response
    public CompanyDto.Response toCompanyResponseDto() {
        return CompanyDto.Response.builder()
                .ticker(ticker)
                .name(name)
                .build();
    }

    // Company -> CompanyWithDividendDto.Response
    public CompanyWithDividendDto.Response toCompanyWithDividendDto() {
        return CompanyWithDividendDto.Response.builder()
                .dividendDtoList(devidendList.stream().map(Dividend::toJoinDividendDto).toList())
                .ticker(ticker)
                .name(name)
                .build();
    }
}
