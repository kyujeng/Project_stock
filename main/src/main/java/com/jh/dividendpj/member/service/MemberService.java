package com.jh.dividendpj.member.service;

import com.jh.dividendpj.member.domain.Member;
import com.jh.dividendpj.member.dto.MemberAuthDto;
import com.jh.dividendpj.member.exception.MemberErrorCode;
import com.jh.dividendpj.member.exception.MemberException;
import com.jh.dividendpj.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(MemberErrorCode.NOT_FOUND_USERNAME.getMessage() + " -> " + username));
    }

    // 회원가입
    // 비밀번호 암호화
    public MemberAuthDto.Response register(MemberAuthDto.SignUp signUp) {
        boolean isExist = memberRepository.existsByUsername(signUp.getUsername());
        if (isExist) {
            throw new MemberException(MemberErrorCode.ALREADY_EXIST_USERNAME);
        }

        MemberAuthDto.SignUp encryptionPWD = signUp.toBuilder()
                .password(passwordEncoder.encode(signUp.getPassword()))
                .build();
        Member save = memberRepository.save(encryptionPWD.toEntity());
        return save.toResponse();
    }

    // 회원 인증
    public Member authenticate(MemberAuthDto.SignIn signIn) {
        Member member = memberRepository.findByUsername(signIn.getUsername())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_USERNAME));
        if (!passwordEncoder.matches(signIn.getPassword(), member.getPassword())) {
            throw new MemberException(MemberErrorCode.NOT_MATCH_PASSWORD);
        }

        return member;
    }
}
