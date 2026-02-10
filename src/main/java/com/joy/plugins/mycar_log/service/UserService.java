package com.joy.plugins.mycar_log.service;

import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByOauthId(String oauthId) {
        return userRepository.findByOauthId(oauthId);
    }

    @Transactional
    public User getOrCreateUser(String oauthId) {
        return userRepository.findByOauthId(oauthId)
                .orElseGet(() -> {
                    User user = new User();
                    user.setOauthId(oauthId);
                    return userRepository.save(user);
                });
    }

    @Transactional
    public User getOrCreateOAuthUser(String oauthId, String email, String displayName) {
        return userRepository.findByOauthId(oauthId)
                .map(user -> {
                    boolean updated = false;
                    if (email != null && !email.equals(user.getEmail())) {
                        user.setEmail(email);
                        updated = true;
                    }
                    if (user.getDisplayName() == null && displayName != null) {
                        user.setDisplayName(displayName);
                        updated = true;
                    }
                    return updated ? userRepository.save(user) : user;
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setOauthId(oauthId);
                    user.setEmail(email);
                    user.setDisplayName(displayName);
                    return userRepository.save(user);
                });
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
