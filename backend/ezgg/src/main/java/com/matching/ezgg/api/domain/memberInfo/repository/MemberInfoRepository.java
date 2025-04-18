package com.matching.ezgg.api.domain.memberInfo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.matching.ezgg.api.domain.memberInfo.entity.MemberInfo;

@Repository
public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {
	// riotUsername 중복 검사
	boolean existsByRiotUsername(String riotUsername);

	// riotTag 중복 검사
	boolean existsByRiotTag(String riotTag);

	// riotUsername과 riotTag를 동시에 일치하는 회원이 존재하는 지 확인
	boolean existsByRiotUsernameAndRiotTag(String riotUsename, String riotTag);
}
