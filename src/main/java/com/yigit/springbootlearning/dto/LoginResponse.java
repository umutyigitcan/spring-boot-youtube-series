package com.yigit.springbootlearning.dto;

public record LoginResponse(

        String login,
        UserResponse user

) {
}
