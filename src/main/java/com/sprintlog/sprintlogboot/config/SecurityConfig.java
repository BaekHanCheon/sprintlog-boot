package com.sprintlog.sprintlogboot.config;

import com.sprintlog.sprintlogboot.filter.RequestIdFilter;
import com.sprintlog.sprintlogboot.filter.RequestLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // 생략가능 관례적 등록
public class SecurityConfig {

  /*
  SecurityFilterChain - Spring Security의 요청 처리 규칙을 정의하는 빈
  Spring bot의 기본 자동 설정 대신 '우리 규칙'이 적용됨
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        //REST API 는 브라우저 세션 폼이 아니라 클라이언트가 직접 요청하므로 지금단계에서는 CSRF 보호를 끈다. (세션 / 폼 기반으로 넘어갈 때 다시 다룸)
        .csrf(csrf -> csrf.disable())

        //서버로 들어오는 요청 중 어떤 요청을 허용할 것인가에 대한 설정
        //경로별 인증 및 궈한 체크 진행이 가능
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/me/**").hasRole("USER")
            .anyRequest().permitAll()
        )
        .httpBasic(Customizer.withDefaults())
        .addFilterBefore(new RequestIdFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(new RequestLoggingFilter(), RequestIdFilter.class);

    return http.build();
  }

  /*
  BCryptPasswordEncoder 비밀번호를 단방향 해시로 인코딩 / 검증하는 빈
  평문 저장 금지 원칙을 코드로 실현할 준비물
   */

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  /*
  static인 이유 : 이 계층 빈은 보안 인프라가 초기화되는 설정 단계에 확실하게 잡혀야 한다.
  static으로 선언한면 다른 빈의 조기 초기화 부작용 없이 보장가능함
  나중에 컨트롤러에서 @PreAuthorize(메소드 보안) 도입 시 static을 붙이지 않으면 동작하지 않는 사례가 있음
   */
  @Bean
  static RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix().role("ADMIN").implies("USER").build();
  }


}
