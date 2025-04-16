package com.matching.ezgg.global.config;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.matching.ezgg.global.jwt.filter.JWTFilter;
import com.matching.ezgg.global.jwt.filter.JWTUtil;
import com.matching.ezgg.global.jwt.filter.LoginFilter;
import com.matching.ezgg.global.jwt.repository.RefreshRepository;
import com.matching.ezgg.global.jwt.sevice.CustomUserDetailService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JWTFilter jwtFilter;
	private final JWTUtil jwtUtil;
	private final RefreshRepository refreshRepository;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring()
			.requestMatchers("/favicon.ico")
			.requestMatchers("/error")
			.requestMatchers(toH2Console());
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
		LoginFilter loginFilter = new LoginFilter(jwtUtil, refreshRepository);
		loginFilter.setAuthenticationManager(authenticationManager);

		// 기본 설정 비활성화
		http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable);

		// URL 접근 권한 설정
		http.authorizeHttpRequests((auth) -> auth
			.requestMatchers("/auth/**", "/login", "/refresh").permitAll() // 해당 요청 은 인증 없이 접근 가능
			.anyRequest().hasAnyAuthority("ROLE_USER")); // 나머지 요청은 ROLE_USER 권한이 있어야 접근 가능

		// 세션 관리 설정
		http.sessionManagement((session) ->
			session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// JWT 필터 적용
		http.addFilterBefore(jwtFilter, LoginFilter.class)
			.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

}
