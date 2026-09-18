package com.yigit.springbootlearning.controller;


import com.yigit.springbootlearning.dto.*;
import com.yigit.springbootlearning.entity.User;
import com.yigit.springbootlearning.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){this.userService=userService;}



    @PostMapping("/users/add")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request){
        User user=userService.createUser(request);
        return UserResponse.fromEntity(user);
    }

    @PostMapping("/users/login")
    public LoginResponse userLogin(@Valid @RequestBody UserLoginRequest request){
        User user=userService.login(request);
        return new LoginResponse("success",UserResponse.fromEntity(user));
    }

    @GetMapping("/users/get")
    public List<UserResponse> getUsers(){
        return userService.getAllUsers().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
            ){

        User user=userService.updateUser(id,request);
        return  UserResponse.fromEntity(user);
    }

    @PatchMapping("/users/change-password")
    public UserResponse changePassword(@Valid @RequestBody ChangePasswordRequest request){

        User user= userService.changePassword(request);
        return UserResponse.fromEntity(user);
    }

    @GetMapping("/users/{id}")
    public UserResponse getUserById(@PathVariable Long id){
        return UserResponse.fromEntity(userService.getUserById(id));
    }





    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id){userService.deleteUser(id);}



}
