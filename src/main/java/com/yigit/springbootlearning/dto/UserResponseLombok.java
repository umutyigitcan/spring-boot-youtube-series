package com.yigit.springbootlearning.dto;

import com.yigit.springbootlearning.entity.UserLombok;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseLombok {

    private final Long id;
    private final String name;
    private final String surname;
    private final String email;

    public static UserResponseLombok fromEntity(UserLombok user) {
        return new UserResponseLombok(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail()
        );
    }
}
