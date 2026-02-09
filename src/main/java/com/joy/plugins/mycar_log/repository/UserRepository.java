package com.joy.plugins.mycar_log.repository;

import com.joy.plugins.mycar_log.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthId(String oauthId);

    boolean existsByOauthId(String oauthId);
}
