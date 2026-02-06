package com.joy.plugins.mycar_log.service;

import com.joy.plugins.mycar_log.dto.CarDto;
import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.repository.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<Car> getCarsByUser(User user) {
        return carRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Car> getCarsByUserId(Long userId) {
        return carRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Car> findByIdAndUserId(Long carId, Long userId) {
        return carRepository.findByIdAndUserId(carId, userId);
    }

    public boolean hasAnyCar(Long userId) {
        return carRepository.countByUserId(userId) > 0;
    }

    @Transactional
    public Car registerCar(CarDto dto, User user) {
        Car car = dto.toEntity();
        car.setUser(user);
        return carRepository.save(car);
    }

    @Transactional
    public Car updateCar(CarDto dto, Long userId) {
        Car car = carRepository.findByIdAndUserId(dto.getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + dto.getId()));
        dto.updateEntity(car);
        return carRepository.save(car);
    }

    @Transactional
    public void deleteCar(Long carId, Long userId) {
        Car car = carRepository.findByIdAndUserId(carId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + carId));
        carRepository.delete(car);
    }

    public Optional<Car> findById(Long id) {
        return carRepository.findById(id);
    }
}
