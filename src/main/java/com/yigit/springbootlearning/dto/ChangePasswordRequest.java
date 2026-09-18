package com.yigit.springbootlearning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotNull Long id,
        @NotBlank @Size(min=8,max=72) String newPassword
) {
}
