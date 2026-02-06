package com.joy.plugins.mycar_log.service;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.repository.CarRepository;
import com.joy.plugins.mycar_log.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MileageService {

    private final ExpenseRepository expenseRepository;
    private final CarRepository carRepository;

    public MileageService(ExpenseRepository expenseRepository, CarRepository carRepository) {
        this.expenseRepository = expenseRepository;
        this.carRepository = carRepository;
    }

    public Long getCurrentMileage(Long carId) {
        Optional<Long> latestMileage = expenseRepository.findLatestMileage(carId);
        if (latestMileage.isPresent()) {
            return latestMileage.get();
        }

        return carRepository.findById(carId)
                .map(Car::getInitialMileage)
                .orElse(0L);
    }

    public Long getPreviousMileage(Long carId, LocalDate date) {
        Optional<Long> previousMileage = expenseRepository.findPreviousMileage(carId, date);
        if (previousMileage.isPresent()) {
            return previousMileage.get();
        }

        return carRepository.findById(carId)
                .map(Car::getInitialMileage)
                .orElse(0L);
    }

    public Long calculateDrivenDistance(Long carId, Long currentMileage, LocalDate date) {
        Long previousMileage = getPreviousMileage(carId, date);
        return currentMileage - previousMileage;
    }

    /**
     * Get mileage statistics for a date range
     * @return Map containing startMileage, endMileage, drivenDistance
     */
    public Map<String, Long> getMileageStatsForDateRange(Long carId, LocalDate startDate, LocalDate endDate) {
        Map<String, Long> stats = new HashMap<>();

        // Get mileage before the start date (or initial mileage)
        Optional<Long> mileageBeforeStart = expenseRepository.findMileageBeforeDate(carId, startDate);
        Long startMileage = mileageBeforeStart.orElseGet(() ->
                carRepository.findById(carId).map(Car::getInitialMileage).orElse(0L));

        // Get max mileage in the date range
        Optional<Long> maxMileageInRange = expenseRepository.findMaxMileageInDateRange(carId, startDate, endDate);
        Long endMileage = maxMileageInRange.orElse(startMileage);

        // Calculate driven distance in the period
        Long drivenDistance = endMileage - startMileage;

        stats.put("startMileage", startMileage);
        stats.put("endMileage", endMileage);
        stats.put("drivenDistance", drivenDistance > 0 ? drivenDistance : 0L);

        return stats;
    }

    /**
     * Get total mileage (current mileage of the car)
     */
    public Long getTotalMileage(Long carId) {
        Optional<Long> latestMileage = expenseRepository.findLatestMileage(carId);
        if (latestMileage.isPresent()) {
            return latestMileage.get();
        }
        return carRepository.findById(carId)
                .map(car -> car.getCurrentMileage() != null ? car.getCurrentMileage() : car.getInitialMileage())
                .orElse(0L);
    }
}
