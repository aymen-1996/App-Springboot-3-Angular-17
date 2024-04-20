package com.chou.app.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RegistrationRequest {

    private String firstname;

    private String lastname;
    @Email(message = "Email is not well formatted")
    private String email;
    private String password;
    private LocalDateTime createdDate;
}
