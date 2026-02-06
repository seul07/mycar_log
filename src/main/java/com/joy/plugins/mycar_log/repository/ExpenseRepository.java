package com.joy.plugins.mycar_log.repository;

import com.joy.plugins.mycar_log.entity.Expense;
import com.joy.plugins.mycar_log.entity.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCarIdAndExpenseDateOrderByCreatedAtDesc(Long carId, LocalDate date);

    List<Expense> findByCarIdAndExpenseDateBetweenOrderByExpenseDateAsc(Long carId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT e.expenseDate FROM Expense e WHERE e.car.id = :carId AND e.expenseDate BETWEEN :startDate AND :endDate")
    List<LocalDate> findDistinctExpenseDatesByCarIdAndDateRange(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT e.category FROM Expense e WHERE e.car.id = :carId AND e.expenseDate = :date")
    List<ExpenseCategory> findCategoriesByCarIdAndDate(
            @Param("carId") Long carId,
            @Param("date") LocalDate date);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.car.id = :carId AND e.expenseDate BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumAmountByCarIdAndDateRange(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT e.currentMileage FROM Expense e WHERE e.car.id = :carId AND e.category = 'MILEAGE' AND e.expenseDate < :date ORDER BY e.expenseDate DESC, e.createdAt DESC LIMIT 1")
    Optional<Long> findPreviousMileage(@Param("carId") Long carId, @Param("date") LocalDate date);

    @Query("SELECT e.currentMileage FROM Expense e WHERE e.car.id = :carId AND e.category = 'MILEAGE' ORDER BY e.expenseDate DESC, e.createdAt DESC LIMIT 1")
    Optional<Long> findLatestMileage(@Param("carId") Long carId);

    @Query("SELECT e.currentMileage FROM Expense e WHERE e.car.id = :carId AND e.expenseDate = :date AND e.category = 'MILEAGE'")
    Optional<Long> findMileageByCarIdAndDate(@Param("carId") Long carId, @Param("date") LocalDate date);

    Optional<Expense> findByCarIdAndExpenseDateAndCategory(Long carId, LocalDate expenseDate, ExpenseCategory category);

    @Query("SELECT MIN(e.currentMileage) FROM Expense e WHERE e.car.id = :carId AND e.category = 'MILEAGE' AND e.expenseDate BETWEEN :startDate AND :endDate")
    Optional<Long> findMinMileageInDateRange(@Param("carId") Long carId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT MAX(e.currentMileage) FROM Expense e WHERE e.car.id = :carId AND e.category = 'MILEAGE' AND e.expenseDate BETWEEN :startDate AND :endDate")
    Optional<Long> findMaxMileageInDateRange(@Param("carId") Long carId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e.currentMileage FROM Expense e WHERE e.car.id = :carId AND e.category = 'MILEAGE' AND e.expenseDate < :startDate ORDER BY e.expenseDate DESC, e.createdAt DESC LIMIT 1")
    Optional<Long> findMileageBeforeDate(@Param("carId") Long carId, @Param("startDate") LocalDate startDate);
}
