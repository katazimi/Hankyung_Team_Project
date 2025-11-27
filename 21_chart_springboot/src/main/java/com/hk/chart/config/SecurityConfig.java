package com.hk.chart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    // 비밀번호 암호화 객체 빈 등록
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        http
            // 개발 편의성을 위해 CSRF 비활성화 (필요 시 활성화)
            .csrf(csrf -> csrf.disable()) 
            
            // 1. 인가(Authorization) 설정
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스 및 누구나 접근 가능한 페이지 설정
                // /api/** 경로도 테스트를 위해 열어두거나 필요에 따라 조정하세요.
                .requestMatchers("/css/**", "/js/**", "/favicon.ico","/images/**", "/", "/user/login", "/user/signUp", "/user/joinProc", "/api/stock/search", "/chart", "/api/**").permitAll()
                // 그 외 모든 요청은 인증된 사용자만 접근 가능
                .anyRequest().authenticated()
            )
            
            // 2. 로그인 설정 (Form Login) - Security가 직접 처리
            .formLogin(form -> form
                .loginPage("/user/login")       // 사용자 정의 로그인 페이지 경로
                .loginProcessingUrl("/user/loginProc") // HTML Form의 action URL
                .usernameParameter("username")        // HTML Form의 name="username"
                .passwordParameter("password")        // HTML Form의 name="password"
                .defaultSuccessUrl("/", true)         // ⭐️ 로그인 성공 시 메인으로 강제 이동
                .failureUrl("/user/login?error=true") // 로그인 실패 시 이동 경로
                .permitAll()
            )
            
            // 3. 로그아웃 설정
            .logout(logout -> logout
                // GET 방식으로 로그아웃 허용 (HTML a태그나 onclick 이동 시 필요)
                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout")) 
                .logoutSuccessUrl("/user/login") // 로그아웃 성공 시 이동할 페이지
                .invalidateHttpSession(true) // 세션 무효화
                .deleteCookies("JSESSIONID") // 쿠키 삭제
            );

        return http.build();
    }
}