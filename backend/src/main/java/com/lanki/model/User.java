package com.lanki.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column
    private String leetcodeUsername;

    @Column(length = 2000)
    private String leetcodeSession; // Encrypted session token

    @Column
    private String csrfToken;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
