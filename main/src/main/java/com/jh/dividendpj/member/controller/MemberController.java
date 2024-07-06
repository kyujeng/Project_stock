package com.jh.dividendpj.member.controller;

import com.jh.dividendpj.auth.TokenProvider;
import com.jh.dividendpj.config.GlobalApiResponse;
import com.jh.dividendpj.member.domain.Member;
import com.jh.dividendpj.member.dto.MemberAuthDto;
import com.jh.dividendpj.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    // 회원가입을 위한 api
    @PostMapping("/signup")
    public ResponseEntity<GlobalApiResponse<MemberAuthDto.Response>> signUp(@RequestBody MemberAuthDto.SignUp request) {
        MemberAuthDto.Response register = memberService.register(request);
        GlobalApiResponse<MemberAuthDto.Response> response = GlobalApiResponse.toGlobalApiResponse(register);
        return ResponseEntity.ok(response);
    }

    // 로그인을 위한 api
    @PostMapping("/signin")
    public ResponseEntity<GlobalApiResponse<String>> signIn(@RequestBody MemberAuthDto.SignIn request) {
        Member authenticate = memberService.authenticate(request);
        String token = tokenProvider.generateToken(authenticate.getUsername(), authenticate.getRoles());
        GlobalApiResponse<String> response = GlobalApiResponse.toGlobalApiResponse(token);
        return ResponseEntity.ok(response);
    }
}
