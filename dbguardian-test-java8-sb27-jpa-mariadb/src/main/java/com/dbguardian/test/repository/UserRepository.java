package com.dbguardian.test.repository;

import java.util.Optional;

import com.dbguardian.test.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query(value = "SELECT @@GLOBAL.server_id", nativeQuery = true)
    Number findServerId();
}
