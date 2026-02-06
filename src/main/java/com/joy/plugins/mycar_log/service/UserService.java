package com.joy.plugins.mycar_log.service;

import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid);
    }

    @Transactional
    public User getOrCreateUser(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> {
                    User user = new User();
                    user.setFirebaseUid(firebaseUid);
                    return userRepository.save(user);
                });
    }

    @Transactional
    public User getOrCreateAnonymousUser(String sessionId) {
        // For development without Firebase, use session-based anonymous user
        String anonymousUid = "anon_" + sessionId;
        return getOrCreateUser(anonymousUid);
    }

    @Transactional
    public String generateAnonymousUid() {
        return "anon_" + UUID.randomUUID().toString();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
