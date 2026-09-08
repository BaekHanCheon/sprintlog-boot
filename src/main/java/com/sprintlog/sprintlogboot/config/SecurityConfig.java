package com.sprintlog.sprintlogboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprintlog.sprintlogboot.filter.RequestIdFilter;
import com.sprintlog.sprintlogboot.filter.RequestLoggingFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity // 생략가능 관례적 등록
@EnableMethodSecurity // 서비스의 @PreAuthorize를 동작시키기 위한 어노테이션
public class SecurityConfig {

  /*
  SecurityFilterChain - Spring Security의 요청 처리 규칙을 정의하는 빈
  Spring bot의 기본 자동 설정 대신 '우리 규칙'이 적용됨
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationEntryPoint restAuthenticationEntryPoint, //빈등록 로직이 활성화 되어있으므로 받음
                                                  AccessDeniedHandler restAccessDeniedHandler) throws Exception {
    http
        //REST API 는 브라우저 세션 폼이 아니라 클라이언트가 직접 요청하므로 지금단계에서는 CSRF 보호를 끈다. (세션 / 폼 기반으로 넘어갈 때 다시 다룸)
        .csrf(csrf -> csrf.disable())
        // 등록한 CORS 규칙을 보안 필터에 연결해 사전 요청도 처리한다.
        .cors(Customizer.withDefaults())

        //xss 방어를 돕는 보안 응답 헤더 - Content-Security-Policy
        //default-src 'self' = 기본적으로 같은 출처의 리소스만 허용 -> 외부 악성 스크립트 주입을 완화
        .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")))

        //서버로 들어오는 요청 중 어떤 요청을 허용할 것인가에 대한 설정
        //경로별 인증 및 궈한 체크 진행이 가능
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/me/**").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/v1/activities/**","/api/activities/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/v1/activities/**","/api/activities/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/v1/activities/**","/api/activities/**").authenticated()
            .anyRequest().permitAll()
        )
        //필터단에서 발생한 커스텀 예외 처리 등록 로직
        .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint).accessDeniedHandler(restAccessDeniedHandler))

        //http basic인증을 켠다 (Authorization 헤더에 Basic <email:password(Base64)> 형식으로 전달되면 DaoAuthenticationProvider를 통해 로그인 검증을 수행하고 SecurityContext에 인증을 채운다.
        //매 요청마다 자격증명을 실어 보내는 무상태 방식
        .httpBasic(Customizer.withDefaults())


        // 폼 로그인 (세션 기반)을 준다.
        // 한번 로그인하면 서버가 세션을 만들고 JSESSIONID 쿠키를 발급
        // 이후 요청은 그 쿠키만으로 인증 유지된다 - 상태 유지 (stateful) 방식
        .formLogin(form ->
            form.loginPage("/login.html") // 우리가 만들 로그인 페이지
                .loginProcessingUrl("/login") //폼이 POST 처리되는 URL(Spring이 가로챔)
                .defaultSuccessUrl("/api/v1/auth/whoami", true) //로그인 성공하면 이쪽 url로 이동
                .permitAll() // 로그인 요청은 누구나 접근 가능함
        )
        .logout(logout ->
            logout.logoutUrl("/logout").logoutSuccessUrl("/login.html")
                .logoutSuccessUrl("/login.html?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
        )

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

  // 미인증401 응답을 ProblemDetail JSON으로 커스텀할 수 있는 객체
  @Bean
  AuthenticationEntryPoint restAuthenticationEntryPoint(ObjectMapper objectMapper) {
    return (request, response, authException) ->
    {writeProblem(objectMapper, response, HttpStatus.UNAUTHORIZED, "AUTH_401","인증이 필요합니다. 로그인 후 다시 시도하세요");};
  }
  // 권한 부족 403 응답을 ProblemDetail JSON으로 커스텀 할 수 있는 객체
  @Bean
  AccessDeniedHandler restAccessDeniedHandler(ObjectMapper objectMapper) {
    return (request, response, deniedException) ->
    {writeProblem(objectMapper, response, HttpStatus.FORBIDDEN, "AUTH_403","작업을 수행할 권한이 없습니다.");};
  }

  /** 401/403 공통 — ProblemDetail 을 JSON 으로 직접 응답 본문에 쓴다. */
  private static void writeProblem(ObjectMapper objectMapper, HttpServletResponse response,
      HttpStatus status, String code, String detail) throws IOException {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setProperty("code", code);
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), pd);
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    //허용할 출처 (운영에서는 실제 프론트 도메인 주소)
    config.setAllowedOrigins(List.of("http://localhost:63342", "http://localhost:3000"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    // 모든 URL에 위 규칙을 적용한다.
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

}
