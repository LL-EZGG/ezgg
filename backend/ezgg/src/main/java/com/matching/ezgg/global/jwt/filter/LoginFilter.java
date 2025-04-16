package com.matching.ezgg.global.jwt.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.ezgg.global.jwt.dto.LoginRequest;
import com.matching.ezgg.global.jwt.entity.Refresh;
import com.matching.ezgg.global.jwt.repository.RefreshRepository;
import com.matching.ezgg.global.response.ErrorResponse;
import com.matching.ezgg.global.response.SuccessResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

	private final JWTUtil jwtUtil;
	private final RefreshRepository refreshRepository;

	// 로그인 요청을 처리하는 메서드
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		log.info(">>>>> 로그인 요청 들어옴");
		LoginRequest loginRequest = new LoginRequest();

		// x-www-form-urlencoded 방식의 로그인 요청을 json 데이터로 받기 위해 설정
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ServletInputStream inputStream = request.getInputStream();
			String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
			loginRequest = objectMapper.readValue(messageBody, LoginRequest.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String memberId = loginRequest.getMemberId();
		String password = loginRequest.getPassword();

		log.info(">>>>> 로그인 요청 memberId: {}", memberId);
		log.info(">>>>> 로그인 요청 password: {}", password);

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(memberId, password);

		return this.getAuthenticationManager().authenticate(token);
	}

	// 로그인 성공 시 호출되는 메서드
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authResult) throws IOException, ServletException {
		log.info(">>>>> 로그인 성공 {}", authResult.getName());

		String memberId = authResult.getName();
		Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
		Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
		GrantedAuthority auth = iterator.next();
		String role = auth.getAuthority();

		String accessToken = jwtUtil.createJwt("access", memberId, role, 60 * 60 * 1000L); // 1시간 유효
		String refreshToken = jwtUtil.createJwt("refresh", memberId, role, 24 * 60 * 60 * 1000L); // 1일 유효
		// todo jwtUtil로 옮기기 - 주입을 JwtUtil만 받을 수 있음
		addRefreshEntity(memberId, refreshToken, 24 * 60 * 60 * 1000L);

		response.setHeader("Authorization", accessToken);
		response.addCookie(createCookie("Refresh", refreshToken));

		response.setStatus(HttpStatus.OK.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		SuccessResponse<Object> successResponse = SuccessResponse.builder()
			.code("200")
			.message("로그인 성공")
			.build();

		response.getWriter().write(new ObjectMapper().writeValueAsString(successResponse));
	}

	// 로그인 실패 시 호출되는 메서드
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException, ServletException {
		log.info(">>>>> 로그인 실패 {}", failed.getMessage());

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code("401")
			.message("로그인 실패")
			.build();

		response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
	}

	private void addRefreshEntity(String memberId, String refreshToken, Long expiredMs) {
		Refresh refresh = Refresh.builder()
			.memberId(memberId)
			.refresh(refreshToken)
			.expiration(String.valueOf(new Date(System.currentTimeMillis() + expiredMs)))
			.build();

		refreshRepository.save(refresh);
	}

	private Cookie createCookie(String key, String value) {
		Cookie cookie = new Cookie(key, value);
		cookie.setHttpOnly(true); // JavaScript에서 접근 불가
		cookie.setMaxAge(24 * 60 * 60); // 1일

		return cookie;
	}
}
