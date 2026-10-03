package com.yigit.springbootlearning.service;

import com.yigit.springbootlearning.dto.ChangePasswordRequestLombok;
import com.yigit.springbootlearning.dto.CreateUserRequestLombok;
import com.yigit.springbootlearning.dto.UpdateUserRequestLombok;
import com.yigit.springbootlearning.dto.UserLoginRequestLombok;
import com.yigit.springbootlearning.entity.UserLombok;
import com.yigit.springbootlearning.exception.EmailAlreadyExistsException;
import com.yigit.springbootlearning.exception.InvalidCredentialsException;
import com.yigit.springbootlearning.exception.UserNotFoundException;
import com.yigit.springbootlearning.repository.UserLombokRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserLombokService {

    private final UserLombokRepository userLombokRepository;
    private final PasswordEncoder passwordEncoder;

    public UserLombok createUser(CreateUserRequestLombok request) {
        String email = normalizeEmail(request.getEmail());

        if (userLombokRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        UserLombok user = UserLombok.builder()
                .name(request.getName().strip())
                .surname(request.getSurname().strip())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(email)
                .build();

        return userLombokRepository.save(user);
    }

    public UserLombok login(UserLoginRequestLombok request) {
        UserLombok user = userLombokRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    public List<UserLombok> getAllUsers() {
        return userLombokRepository.findAll();
    }

    public UserLombok updateUser(Long id, UpdateUserRequestLombok request) {
        UserLombok user = getUserById(id);
        user.setName(request.getName().strip());
        user.setSurname(request.getSurname().strip());
        return userLombokRepository.save(user);
    }

    public UserLombok changePassword(ChangePasswordRequestLombok request) {
        UserLombok user = getUserById(request.getId());
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        return userLombokRepository.save(user);
    }

    public void deleteUser(Long id) {
        userLombokRepository.delete(getUserById(id));
    }

    public UserLombok getUserById(Long id) {
        return userLombokRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
