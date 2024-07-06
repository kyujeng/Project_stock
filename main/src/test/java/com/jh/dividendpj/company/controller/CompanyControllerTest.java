package com.jh.dividendpj.company.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.dividendpj.company.dto.CompanyDto;
import com.jh.dividendpj.company.dto.CreateCompanyDto;
import com.jh.dividendpj.company.exception.CompanyErrorCode;
import com.jh.dividendpj.company.service.CompanyService;
import com.jh.dividendpj.member.dto.MemberAuthDto;
import com.jh.dividendpj.scraper.exception.ScraperErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyControllerTest {
    @Autowired
    private CompanyService companyService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    String companyName = "Coca-Cola Consolidated, Inc.";
    String ticker = "coke";

    CreateCompanyDto.Request createRequest;
    CompanyDto.Request autoCompleteRequest;

    String token;

    @BeforeEach
    void before() throws Exception {
        createRequest = CreateCompanyDto.Request.builder()
                .ticker(ticker)
                .build();
        autoCompleteRequest = CompanyDto.Request.builder()
                .prefix("c")
                .build();

        MemberAuthDto.SignUp signUp = MemberAuthDto.SignUp.builder()
                .roles(List.of("ROLE_READ", "ROLE_WRITE"))
                .password("test1234")
                .username("test")
                .build();
        MemberAuthDto.SignIn signIn = MemberAuthDto.SignIn.builder()
                .username("test")
                .password("test1234")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUp)))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signIn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        token = jsonNode.get("result").asText();
    }

    @Test
    @DisplayName("회사 생성 컨트롤러")
    void createController() throws Exception {
        mockMvc.perform(post("/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result.companyName").value(companyName))
                .andExpect(jsonPath("$.result.ticker").value(ticker));
    }

    @Test
    @DisplayName("회사 생성 컨트롤러 실패 - 유효성 검증 실패")
    void failCreateController() throws Exception {
        mockMvc.perform(post("/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest.toBuilder().ticker("").build()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("회사 생성 컨트롤러 실패 - 스크랩 실패")
    void failCreateController2() throws Exception {
        mockMvc.perform(post("/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest.toBuilder().ticker("가").build()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(ScraperErrorCode.NOT_FOUND_TICKER.getMessage()));
    }

    @Test
    @DisplayName("회사 생성 컨트롤러 실패 - 회원 인증 실패")
    void failCreateController3() throws Exception {
        mockMvc.perform(post("/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest.toBuilder().ticker("가").build())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회사 삭제 컨트롤러")
    void deleteController() throws Exception {
        companyService.createCompany(createRequest);

        mockMvc.perform(delete("/company/" + ticker)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("회사 삭제 컨트롤러 실패 - 유효성 검증 실패(빈 문자)")
    void failDeleteController() throws Exception {
        mockMvc.perform(delete("/company/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(CompanyErrorCode.EMPTY_TICKER.getMessage()));
    }

    @Test
    @DisplayName("회사 삭제 컨트롤러 실패 - 유효성 검증 실패(공백)")
    void failDeleteController2() throws Exception {
        mockMvc.perform(delete("/company/ ")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(CompanyErrorCode.EMPTY_TICKER.getMessage()));
    }

    @Test
    @DisplayName("회사 삭제 컨트롤러 실패 - 저장된 회사 없음")
    void failDeleteController3() throws Exception {
        mockMvc.perform(delete("/company/" + ticker)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(CompanyErrorCode.NOT_FOUND_TICKER.getMessage()));
    }

    @Test
    @DisplayName("회사 삭제 컨트롤러 실패 - 회원 인증 실패")
    void failDeleteController4() throws Exception {
        mockMvc.perform(delete("/company/" + ticker))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("자동 완성 기능 컨트롤러")
    void autoCompleteController() throws Exception {
        companyService.createCompany(createRequest);

        mockMvc.perform(get("/company/autocomplete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(autoCompleteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result[0].ticker").value(ticker));
    }

    @Test
    @DisplayName("자동 완성 기능 컨트롤러 실패 - 유효성 검증 실패(빈 문자)")
    void autoCompleteController2() throws Exception {
        mockMvc.perform(get("/company/autocomplete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(autoCompleteRequest.toBuilder().prefix("").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("자동 완성 기능 컨트롤러 실패 - 유효성 검증 실패(공백으로 시작하는 문자)")
    void autoCompleteController3() throws Exception {
        mockMvc.perform(get("/company/autocomplete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(autoCompleteRequest.toBuilder().prefix(" ").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("회사 정보와 배당금 정보 조회 컨트롤러")
    void getCompanyWithDividend() throws Exception {
        companyService.createCompany(createRequest);

        mockMvc.perform(get("/finance/dividend/" + companyName)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.result.name").value(companyName))
                .andExpect(jsonPath("$.result.dividendDtoList").exists());
    }

    @Test
    @DisplayName("회사 정보와 배당금 정보 조회 컨트롤러 실패 - 유효성 검증 실패(빈 문자)")
    void failGetCompanyWithDividend() throws Exception {
        mockMvc.perform(get("/finance/dividend/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(CompanyErrorCode.EMPTY_COMPANY_NAME.getMessage()));
    }

    @Test
    @DisplayName("회사 정보와 배당금 정보 조회 컨트롤러 실패 - 유효성 검증 실패(공백 문자로 시작)")
    void failGetCompanyWithDividend2() throws Exception {
        mockMvc.perform(get("/finance/dividend/ ")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(CompanyErrorCode.EMPTY_COMPANY_NAME.getMessage()));
    }

    @Test
    @DisplayName("회사 정보와 배당금 정보 조회 컨트롤러 실패 - 저장된 회사 정보 없음")
    void failGetCompanyWithDividend3() throws Exception {
        mockMvc.perform(get("/finance/dividend/" + companyName)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(CompanyErrorCode.NOT_FOUND_NAME.getMessage()));
    }

    @Test
    @DisplayName("회사 정보와 배당금 정보 조회 컨트롤러 실패 - 회원 인증 실패")
    void failGetCompanyWithDividend4() throws Exception {
        mockMvc.perform(get("/finance/dividend/" + companyName))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("현재 관리하고 있는 모든 회사 리스트 조회 컨트롤러")
    void getAllCompany() throws Exception {
        companyService.createCompany(createRequest);

        mockMvc.perform(get("/company")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.result.content").exists());
    }

    @Test
    @DisplayName("현재 관리하고 있는 모든 회사 리스트 조회 컨트롤러 - 결과 없음")
    void getAllCompany2() throws Exception {
        mockMvc.perform(get("/company")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.result.content").isEmpty());
    }

    @Test
    @DisplayName("현재 관리하고 있는 모든 회사 리스트 조회 컨트롤러 실패 - 회원 인증 실패")
    void failGetAllCompany() throws Exception {
        mockMvc.perform(get("/company"))
                .andExpect(status().isForbidden());
    }
}