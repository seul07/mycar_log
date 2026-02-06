package com.joy.plugins.mycar_log.repository;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByUserOrderByCreatedAtDesc(User user);

    List<Car> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Car> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
