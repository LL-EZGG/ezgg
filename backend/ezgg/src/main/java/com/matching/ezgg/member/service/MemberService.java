package com.matching.ezgg.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matching.ezgg.api.domain.memberInfo.service.MemberInfoService;
import com.matching.ezgg.api.service.ApiService;
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
	private final MemberInfoService memberInfoService;
	private final ApiService apiService;

	private final PasswordEncoder passwordEncoder;

	@Transactional
	public SignupResponse signup(SignupRequest signupRequest) {

		log.info("아이디 : {}", signupRequest.getMemberUsername());

		String password = passwordEncoder.encode(signupRequest.getPassword());

		validateDuplicateMember(signupRequest);

		Member newMember = Member.builder()
			.memberUsername(signupRequest.getMemberUsername())
			.password(password)
			.email(signupRequest.getEmail())
			.riotUsername(signupRequest.getRiotUsername())
			.riotTag(signupRequest.getRiotTag())
			.role("ROLE_USER") // 기본 역할 설정
			.build();

		//Member 엔티티 생성 후 저장
		Member member = memberRepository.save(newMember);

		//MemberInfo 엔티티 생성 후 저장
		String newPuuid = apiService.getMemberPuuid(signupRequest.getRiotUsername(), signupRequest.getRiotTag());
		memberInfoService.createNewMemberInfo(member.getId(), member.getRiotUsername(), member.getRiotTag(), newPuuid);

		return SignupResponse.builder()
			.memberUsername(member.getMemberUsername())
			.email(member.getEmail())
			.riotUsername(member.getRiotUsername())
			.riotTag(member.getRiotTag())
			.build();
	}

	//TODO 외부 api 호출(puuid찾기)을 @Transactional 안에서 하게되면 1)외부 api 데이터를 기다리는 시간동안 db 커넥션을 물고 있게 됨 + 2) 불필요하게 큰 단위의 서비스가 롤백될 가능성이 있음
	// 따라서 아래와 같은 방식으로 refactor 해볼 수도 있지 않을까? 해서 올려봅니당
	// 밑의 코드를 그대로 두면 self-invocation 상태이기 때문에 실제로는 Transaction이 안걸립니다. saveMemberWithInfo() 메서드를 다른 클래스로 옮겨서 사용해야 합니다
	// 만약 밑의 방식을 채택 하실 거라면 SignupFacade 혹은 SignupTransactionalService 클래스를 만들어서 saveMemberWithInfo() 메서드를 옮겨 사용하시면 됩니다!

	// public SignupResponse signup(SignupRequest signupRequest) {
	// 	log.info("아이디 : {}", signupRequest.getMemberUsername());
	//
	// 	String password = passwordEncoder.encode(signupRequest.getPassword());
	// 	validateDuplicateMember(signupRequest);
	//
	// 	// 외부 API 트랜잭션 밖에서 호출
	// 	String newPuuid = apiService.getMemberPuuid(signupRequest.getRiotUsername(), signupRequest.getRiotTag());
	//
	// 	// 트랜잭션 안에서 db 저장 파트 처리
	// 	return saveMemberWithInfo(signupRequest, password, newPuuid);
	// }
	//
	// @Transactional
	// public SignupResponse saveMemberWithInfo(SignupRequest signupRequest, String password, String puuid) {
	//
	// 	//Member 엔티티 생성 후 저장
	// 	Member member = memberRepository.save(Member.builder()
	// 		.memberUsername(signupRequest.getMemberUsername())
	// 		.password(password)
	// 		.email(signupRequest.getEmail())
	// 		.riotUsername(signupRequest.getRiotUsername())
	// 		.riotTag(signupRequest.getRiotTag())
	// 		.role("ROLE_USER")
	// 		.build());
	//
	// 	//MemberInfo 엔티티 생성 후 저장
	// 	memberInfoService.createNewMemberInfo(member.getId(), member.getRiotUsername(), member.getRiotTag(), puuid);
	//
	// 	return SignupResponse.builder()
	// 		.memberUsername(member.getMemberUsername())
	// 		.email(member.getEmail())
	// 		.riotUsername(member.getRiotUsername())
	// 		.riotTag(member.getRiotTag())
	// 		.build();
	// }

	private void validateDuplicateMember(SignupRequest signupRequest) {
		// 이미 존재하는 회원인지 확인
		if (memberRepository.existsByMemberUsername((signupRequest.getMemberUsername()))) {
			throw new ExistMemberIdException();
		}

		// 이메일 중복 확인
		if (memberRepository.existsByEmail(signupRequest.getEmail())) {
			throw new ExistEmailException();
		}

		// 소환사명 중복 확인
		if (memberRepository.existsByRiotUsername(signupRequest.getRiotUsername())) {
			throw new ExistRiotUsernamException();
		}

		// 소환사 태그 중복 확인
		if (memberRepository.existsByRiotTag(signupRequest.getRiotTag())) {
			throw new ExistRiotTagException();
		}
	}
}
