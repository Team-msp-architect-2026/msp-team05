package com.baseball.ticket.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank @Email
    private String email;

    @NotBlank
    private String username;

    @NotBlank @Size(min = 8)
    private String passwd;

    @NotBlank
    private String phonenum;

    private String address;
}
