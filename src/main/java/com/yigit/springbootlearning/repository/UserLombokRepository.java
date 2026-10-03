package com.yigit.springbootlearning.repository;

import com.yigit.springbootlearning.entity.UserLombok;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLombokRepository extends JpaRepository<UserLombok, Long> {

    Optional<UserLombok> findByEmail(String email);

    boolean existsByEmail(String email);
}
