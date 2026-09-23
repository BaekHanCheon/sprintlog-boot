package com.sprintlog.sprintlogboot.security;

import com.sprintlog.sprintlogboot.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  /** EntryPoint 에 실패 사유를 전달하는 request attribute 키. */
  public static final String ATTR_JWT_ERROR = "jwt.error";
  public static final String ERROR_EXPIRED = "expired";
  public static final String ERROR_INVALID = "invalid";

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtProvider jwtProvider;
  //private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token = resolveToken(request);

    //로그인을 안 했을 경우에 요청 (토큰의 유무, 검증의 실패여부에 상관없이 필터 체인은 계속 진행되어야 함)
    if(token != null){
      try {
        Claims claims = jwtProvider.parseClaims(token);
        String username = claims.getSubject();
        Role role = jwtProvider.getRole(claims);

        // 권한을 세팅하는 것을 DB가 아닌 토큰에서 만든다
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + (role.name())));

        JwtPrincipal principal = new JwtPrincipal(jwtProvider.getUserId(claims), username, role);

        //사용자 로드 -> DB로 실존 확인
        //UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        /**
        Security Context에 '인증 완료' 상태의 Authentication을 채운다. @PreAuthorize, AuthorizationFiler는
        인증이 어디서 왔는지 세션인지 토큰인지 모른 채 똑같이 동작함
         */
        //UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken
        //    .authenticated(userDetails, null, userDetails.getAuthorities());
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken
            .authenticated(principal, null, authorities);

        // 들어온 HTTP 요청으로부터 인증과 관련된 부가적인 웹 메타데이터를 추출해서 인증 정보에 세팅하는 로직(IP 주소, 세션ID 등)
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // 빈 컨텍스트에 인증정보 정장
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        log.debug("[JWT] 인증 성공 user: {}, role: {}", username, role);

        //에러의 원인을 request 객체에 담아놓음 -> EntryPoint가 request 객체를 받아서 응답에 직접 활용 예정
      }catch (ExpiredJwtException e) {
        request.setAttribute(ATTR_JWT_ERROR, ERROR_EXPIRED);
        log.debug("[JWT] 만료된 토큰으로 접근 - {}", e.getMessage());
      }catch (JwtException | IllegalArgumentException e) {
        request.setAttribute(ATTR_JWT_ERROR, ERROR_INVALID);
        log.debug("[JWT] 유효하지 않은 토큰으로 접근 - {}", e.getMessage());
      }//catch (UsernameNotFoundException e) {
       // request.setAttribute(ATTR_JWT_ERROR, ERROR_INVALID);
        //log.debug("[JWT] 토큰의 사용자가 존재하지 않음 - {}", e.getMessage());
     // }
    }

    filterChain.doFilter(request, response);

  }

  private String resolveToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
