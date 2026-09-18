package com.yigit.springbootlearning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank @Size(max=100) String name,
        @NotBlank @Size(max=100) String surname
) {
}
