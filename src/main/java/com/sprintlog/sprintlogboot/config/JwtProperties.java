package com.sprintlog.sprintlogboot.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("sprintlog.jwt")
public class JwtProperties {

  /** HS256 서명 비밀키. 최소 32바이트(256비트). 운영에서는 환경 변수로 주입. */
  private String secret;

  /** Access Token 유효 시간 (yml 의 30m, 1h 표기가 Duration 으로 바인딩). */
  private Duration accessTokenValidity = Duration.ofMinutes(30);

  /** 발급자(iss 클레임) - "이 토큰은 우리 서버가 발급했다" 는 표식. */
  private String issuer = "sprintlog";



}
