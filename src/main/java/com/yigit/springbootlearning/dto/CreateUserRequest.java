package com.yigit.springbootlearning.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max=100) String name,
        @NotBlank @Size(max=100) String surname,
        @NotBlank @Size(min=8,max=72) String password,
        @NotBlank @Email @Size(max=254) String email
) {

}
