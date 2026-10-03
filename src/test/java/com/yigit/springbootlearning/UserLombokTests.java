package com.yigit.springbootlearning;

import com.yigit.springbootlearning.entity.UserLombok;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserLombokTests {

    @Test
    void lombokBuilderGetterVeSetterMetotlariniUretir() {
        UserLombok user = UserLombok.builder()
                .name("Ada")
                .surname("Lovelace")
                .passwordHash("hash")
                .email("ada@example.com")
                .build();

        user.setName("Augusta Ada");

        assertThat(user.getName()).isEqualTo("Augusta Ada");
        assertThat(user.getSurname()).isEqualTo("Lovelace");
        assertThat(user.getEmail()).isEqualTo("ada@example.com");
    }
}
