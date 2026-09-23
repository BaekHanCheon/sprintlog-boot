// 세션·쿠키 인증 학습용 보존 코드. JWT 전환 후에는 컴파일·빈 등록·테스트 실행에서 제외합니다.
// package com.sprintlog.sprintlogboot.security;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ProblemDetail;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.web.authentication.AuthenticationFailureHandler;
// import org.springframework.stereotype.Component;
//
// import java.io.IOException;
//
// @RequiredArgsConstructor
// public class LoginFailureHandler implements AuthenticationFailureHandler {
//
//     private final ObjectMapper objectMapper;
//
//     @Override
//     public void onAuthenticationFailure(HttpServletRequest request,
//                                         HttpServletResponse response,
//                                         AuthenticationException exception) throws IOException, ServletException {
//         ProblemDetail pd = ProblemDetail.forStatusAndDetail(
//                 HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."
//         );
//         pd.setProperty("code", "AUTH_LOGIN_FAILED");
//
//         response.setStatus(HttpStatus.UNAUTHORIZED.value());
//         response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
//         response.setCharacterEncoding("UTF-8");
//         objectMapper.writeValue(response.getWriter(), pd);
//     }
//
//
// }
//
//
//
//
//
//
//
//
//
