package com.yigit.springbootlearning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link User} sinifinin Lombok ile yazilmis egitim karsiligi.
 *
 * <p>JPA entity'lerinde {@code @Data} kullanmak yerine ihtiyac duyulan
 * anotasyonlari tek tek secmek daha guvenlidir. Boylece id icin setter ve
 * iliskileri dolaşan equals/hashCode/toString metotlari uretilmez.</p>
 */
@Entity
@Table(name = "users_lombok")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLombok {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Setter
    @NotBlank
    @Column(nullable = false, length = 100)
    private String surname;

    @Setter
    @NotBlank
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Setter
    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Builder
    public UserLombok(String name, String surname, String passwordHash, String email) {
        this.name = name;
        this.surname = surname;
        this.passwordHash = passwordHash;
        this.email = email;
    }

}
