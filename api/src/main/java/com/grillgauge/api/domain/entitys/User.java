package com.grillgauge.api.domain.entitys;

import org.springframework.lang.NonNull;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @NonNull
    private String email;

    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    @NonNull
    private String passwordHash;

    public User(final String email, final String firstName, final String lastName, final String passwordHash) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
    }
}
