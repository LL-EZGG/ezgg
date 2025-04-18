package com.matching.ezgg.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matching.ezgg.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByMemberUsername(String memberUsername);

	Optional<Member> findByEmail(String email);

	boolean existsByMemberUsername(String memberUsername);

	boolean existsByEmail(String email);
}

