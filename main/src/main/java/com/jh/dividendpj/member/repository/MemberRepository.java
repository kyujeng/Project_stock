package com.jh.dividendpj.member.repository;

import com.jh.dividendpj.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String userName);

    boolean existsByUsername(String userName);
}
