package com.swisspost.cryptowallet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User(String username, String fullName, LocalDateTime createdAt){
        this.username=username;
        this.fullName=fullName;
        this.createdAt=createdAt;
    }
}
