package com.sprintlog.sprintlogboot.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "")
    String email,
    @NotBlank
    String password
){

}
