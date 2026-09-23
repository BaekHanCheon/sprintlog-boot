package com.sprintlog.sprintlogboot.dto.response;

public record TokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {
    public static TokenResponse bearer(String accessToken, long expiresInSeconds) {

        return new TokenResponse(accessToken, "Bearer", expiresInSeconds);
    }
}
