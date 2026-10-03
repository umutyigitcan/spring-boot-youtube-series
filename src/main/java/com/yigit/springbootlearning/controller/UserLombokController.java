package com.yigit.springbootlearning.controller;

import com.yigit.springbootlearning.dto.ChangePasswordRequestLombok;
import com.yigit.springbootlearning.dto.CreateUserRequestLombok;
import com.yigit.springbootlearning.dto.LoginResponseLombok;
import com.yigit.springbootlearning.dto.UpdateUserRequestLombok;
import com.yigit.springbootlearning.dto.UserLoginRequestLombok;
import com.yigit.springbootlearning.dto.UserResponseLombok;
import com.yigit.springbootlearning.entity.UserLombok;
import com.yigit.springbootlearning.service.UserLombokService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lombok/users")
@RequiredArgsConstructor
public class UserLombokController {

    private final UserLombokService userLombokService;

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseLombok createUser(@Valid @RequestBody CreateUserRequestLombok request) {
        return UserResponseLombok.fromEntity(userLombokService.createUser(request));
    }

    @PostMapping("/login")
    public LoginResponseLombok login(@Valid @RequestBody UserLoginRequestLombok request) {
        UserLombok user = userLombokService.login(request);
        return new LoginResponseLombok("success", UserResponseLombok.fromEntity(user));
    }

    @GetMapping("/get")
    public List<UserResponseLombok> getUsers() {
        return userLombokService.getAllUsers().stream()
                .map(UserResponseLombok::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponseLombok getUserById(@PathVariable Long id) {
        return UserResponseLombok.fromEntity(userLombokService.getUserById(id));
    }

    @PutMapping("/{id}")
    public UserResponseLombok updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestLombok request
    ) {
        return UserResponseLombok.fromEntity(userLombokService.updateUser(id, request));
    }

    @PatchMapping("/change-password")
    public UserResponseLombok changePassword(
            @Valid @RequestBody ChangePasswordRequestLombok request
    ) {
        return UserResponseLombok.fromEntity(userLombokService.changePassword(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userLombokService.deleteUser(id);
    }
}
