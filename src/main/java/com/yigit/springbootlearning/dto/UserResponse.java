package com.yigit.springbootlearning.dto;

import com.yigit.springbootlearning.entity.User;

public record UserResponse(
        Long id,
        String name,
        String surname,
        String email
) {

    public static UserResponse fromEntity(User user){

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail()
        );

    }


}
