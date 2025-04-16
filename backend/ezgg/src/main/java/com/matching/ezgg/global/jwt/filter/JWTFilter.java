package com.matching.ezgg.global.jwt.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.ezgg.global.jwt.dto.CustomUserDetails;
import com.matching.ezgg.global.response.ErrorResponse;
import com.matching.ezgg.member.entity.Member;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {

	private final JWTUtil jwtUtil;
	private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 변환을 위한 ObjectMapper

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String accessToken = extractToken(request);

		if (!StringUtils.hasText(accessToken)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			if (!jwtUtil.isExpired(accessToken)) {
				setAuthenticationToContext(accessToken);
			}
			filterChain.doFilter(request, response);
		} catch (ExpiredJwtException e) {
			handleJwtException(response, "토큰이 만료되었습니다. 다시 로그인해주세요.", HttpServletResponse.SC_UNAUTHORIZED);
		} catch (SignatureException e) {
			handleJwtException(response, "유효하지 않은 JWT 서명입니다.", HttpServletResponse.SC_UNAUTHORIZED);
		} catch (MalformedJwtException e) {
			handleJwtException(response, "잘못된 JWT 형식입니다.", HttpServletResponse.SC_UNAUTHORIZED);
		} catch (UnsupportedJwtException e) {
			handleJwtException(response, "지원되지 않는 JWT 토큰입니다.", HttpServletResponse.SC_UNAUTHORIZED);
		} catch (JwtException e) {
			handleJwtException(response, "JWT 처리 중 오류가 발생했습니다.", HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	// 토큰을 추출하는 메서드
	private String extractToken(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		log.info("Authorization Header: {}", token);
		if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
			return token.substring(7);
		}
		return token;
	}

	// JWT를 검증하고 인증 정보를 SecurityContext에 설정하는 메서드
	private void setAuthenticationToContext(String accessToken) {
		String memberId = jwtUtil.getMemberId(accessToken);
		String role = jwtUtil.getRole(accessToken);

		Member member = Member.builder()
			.memberId(memberId)
			.role(role)
			.build();
		CustomUserDetails userDetails = new CustomUserDetails(member);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
			userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void handleJwtException(HttpServletResponse response, String message, int status) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(status);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(String.valueOf(status))
			.message(message)
			.build();

		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}

}
