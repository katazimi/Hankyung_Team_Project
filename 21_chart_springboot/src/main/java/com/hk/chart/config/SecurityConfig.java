package com.hk.chart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    // 비밀번호 암호화 객체 빈 등록 (UserServiceImpl에서도 주입받아 사용)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        http
            // CSRF 설정 (필요에 따라 활성화/비활성화, 여기서는 개발 편의상 disable 할 수도 있으나 기본값 권장)
            // .csrf(csrf -> csrf.disable()) 
            
            // 1. 인가(Authorization) 설정
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스, 로그인, 회원가입 페이지는 누구나 접근 가능
                .requestMatchers("/css/**", "/js/**", "/images/**", "/user/login", "/user/join", "/user/joinProc").permitAll()
                // 그 외 모든 요청(예: /chart)은 인증된 사용자만 접근 가능
                .anyRequest().authenticated()
            )
            
            // 2. 로그인 설정 (Form Login)
            .formLogin(form -> form
                .loginPage("/user/login")       // 사용자 정의 로그인 페이지 경로 (UserController의 @GetMapping 참고)
                .loginProcessingUrl("/user/loginProc") // HTML Form의 th:action="@{/user/loginProc}"와 일치해야 함
                .usernameParameter("username")        // HTML Form의 <input name="id"> 와 일치해야 함
                .passwordParameter("password")  // HTML Form의 <input name="password"> 와 일치해야 함
                .defaultSuccessUrl("/chart", true) // 로그인 성공 시 이동할 페이지
                .failureUrl("/user/login?error=true") // 로그인 실패 시 이동할 페이지 (파라미터로 에러 표시)
                .permitAll()
            )
            
            // 3. 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/user/logout") // 로그아웃 요청 URL
                .logoutSuccessUrl("/user/login") // 로그아웃 성공 시 이동할 페이지
                .invalidateHttpSession(true) // 세션 무효화
                .deleteCookies("JSESSIONID") // 쿠키 삭제
            );

        return http.build();
    }
}