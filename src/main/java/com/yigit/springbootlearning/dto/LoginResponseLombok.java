package com.yigit.springbootlearning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseLombok {

    private final String login;
    private final UserResponseLombok user;
}
