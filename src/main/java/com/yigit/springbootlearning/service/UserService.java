package com.yigit.springbootlearning.service;

import com.yigit.springbootlearning.dto.ChangePasswordRequest;
import com.yigit.springbootlearning.dto.CreateUserRequest;
import com.yigit.springbootlearning.dto.UpdateUserRequest;
import com.yigit.springbootlearning.dto.UserLoginRequest;
import com.yigit.springbootlearning.entity.User;
import com.yigit.springbootlearning.exception.EmailAlreadyExistsException;
import com.yigit.springbootlearning.exception.InvalidCredentialsException;
import com.yigit.springbootlearning.exception.UserNotFoundException;
import com.yigit.springbootlearning.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User(
                request.name().strip(),
                request.surname().strip(),
                passwordEncoder.encode(request.password()),
                email
        );

        return userRepository.save(user);
    }

    public User login(UserLoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }



    public User updateUser(Long id, UpdateUserRequest request) {
        User user = getUserById(id);

        user.setName(request.name().strip());
        user.setSurname(request.surname().strip());
        return userRepository.save(user);
    }


    public User changePassword(ChangePasswordRequest request){
        User user=getUserById(request.id());

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));

        return userRepository.save(user);
    }

    public void deleteUser(Long id ){
        User user=getUserById(id);
        userRepository.delete(user);
    }


    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException(id));
    }

    private String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
