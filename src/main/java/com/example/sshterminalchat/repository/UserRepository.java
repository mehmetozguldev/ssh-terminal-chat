package com.example.sshterminalchat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sshterminalchat.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
