package com.matching.ezgg.member.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matching.ezgg.api.domain.memberInfo.entity.MemberInfo;
import com.matching.ezgg.api.domain.memberInfo.repository.MemberInfoRepository;
import com.matching.ezgg.global.exception.ExistEmailException;
import com.matching.ezgg.global.exception.ExistMemberIdException;
import com.matching.ezgg.global.exception.ExistRiotTagException;
import com.matching.ezgg.global.exception.ExistRiotUsernamException;
import com.matching.ezgg.member.dto.SignupRequest;
import com.matching.ezgg.member.dto.SignupResponse;
import com.matching.ezgg.member.entity.Member;
import com.matching.ezgg.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final MemberInfoRepository memberInfoRepository;

	private final PasswordEncoder passwordEncoder;

	@Transactional
	public SignupResponse signup(SignupRequest signupRequest) {

		log.info("아이디 : {}", signupRequest.getMemberUsername());

		String password = passwordEncoder.encode(signupRequest.getPassword());

		vaildateDuplicateMember(signupRequest);

		Member newMember = Member.builder()
			.memberUsername(signupRequest.getMemberUsername())
			.password(password)
			.email(signupRequest.getEmail())
			.role("ROLE_USER") // 기본 역할 설정
			.build();

		Member member = memberRepository.save(newMember);

		MemberInfo newMemberInfo = MemberInfo.builder()
			.memberId(member.getId())
			.riotUsername(signupRequest.getRiotUsername())
			.riotTag(signupRequest.getRiotTag())
			.build();


		MemberInfo memberInfo = memberInfoRepository.save(newMemberInfo);

		return SignupResponse.builder()
			.memberUsername(member.getMemberUsername())
			.email(member.getEmail())
			.riotUsername(memberInfo.getRiotUsername())
			.riotTag(memberInfo.getRiotTag())
			.build();
	}

	private void vaildateDuplicateMember(SignupRequest signupRequest) {
		// 이미 존재하는 회원인지 확인
		if (memberRepository.existsByMemberUsername((signupRequest.getMemberUsername()))) {
			throw new ExistMemberIdException();
		}

		// 이메일 중복 확인
		if (memberRepository.existsByEmail(signupRequest.getEmail())) {
			throw new ExistEmailException();
		}

		// riotUsername and riotTag 중복 검사
		if (memberInfoRepository.existsByRiotUsernameAndRiotTag(signupRequest.getRiotUsername(),
			signupRequest.getRiotTag())) {
			throw new ExistRiotUsernamException();
		}
	}
}
