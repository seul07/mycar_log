package com.joy.plugins.mycar_log.service;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.Expense;
import com.joy.plugins.mycar_log.entity.enums.ExpenseCategory;
import com.joy.plugins.mycar_log.repository.CarRepository;
import com.joy.plugins.mycar_log.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CarRepository carRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CarRepository carRepository) {
        this.expenseRepository = expenseRepository;
        this.carRepository = carRepository;
    }

    public List<Expense> getExpensesByDate(Long carId, LocalDate date) {
        return expenseRepository.findByCarIdAndExpenseDateOrderByCreatedAtDesc(carId, date);
    }

    public Map<String, Object> getMonthlyCalendarData(Long carId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByCarIdAndExpenseDateBetweenOrderByExpenseDateAsc(
                carId, startDate, endDate);

        Map<String, List<String>> result = new HashMap<>();

        for (Expense expense : expenses) {
            String dateKey = expense.getExpenseDate().toString();
            result.computeIfAbsent(dateKey, k -> new ArrayList<>());

            String category = expense.getCategory().name();
            if (!result.get(dateKey).contains(category)) {
                result.get(dateKey).add(category);
            }
        }

        return Map.of("expenses", result);
    }

    @Transactional
    public Expense createExpense(Long carId, Expense expense) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + carId));
        expense.setCar(car);

        // Update car's current mileage if this is a mileage entry
        if (expense.getCategory() == ExpenseCategory.MILEAGE && expense.getCurrentMileage() != null) {
            if (car.getCurrentMileage() == null || expense.getCurrentMileage() > car.getCurrentMileage()) {
                car.setCurrentMileage(expense.getCurrentMileage());
                carRepository.save(car);
            }
        }

        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense updateExpense(Long id, Expense updatedExpense) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + id));

        expense.setExpenseDate(updatedExpense.getExpenseDate());
        expense.setCategory(updatedExpense.getCategory());
        expense.setAmount(updatedExpense.getAmount());
        expense.setCurrentMileage(updatedExpense.getCurrentMileage());
        expense.setPricePerLiter(updatedExpense.getPricePerLiter());
        expense.setLiters(updatedExpense.getLiters());
        expense.setMaintenanceItem(updatedExpense.getMaintenanceItem());
        expense.setInsuranceType(updatedExpense.getInsuranceType());
        expense.setDescription(updatedExpense.getDescription());

        // Update car's current mileage if this is a mileage entry
        if (expense.getCategory() == ExpenseCategory.MILEAGE && expense.getCurrentMileage() != null) {
            Car car = expense.getCar();
            // Recalculate max mileage from all mileage entries
            Optional<Long> maxMileage = expenseRepository.findLatestMileage(car.getId());
            if (maxMileage.isPresent() && (car.getCurrentMileage() == null || maxMileage.get() > car.getCurrentMileage())) {
                car.setCurrentMileage(maxMileage.get());
                carRepository.save(car);
            }
        }

        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    public Optional<Expense> findById(Long id) {
        return expenseRepository.findById(id);
    }

    public Long getMileageForDate(Long carId, LocalDate date) {
        return expenseRepository.findMileageByCarIdAndDate(carId, date).orElse(null);
    }

    @Transactional
    public void saveOrUpdateMileage(Long carId, LocalDate date, Long currentMileage) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + carId));

        // Find existing mileage expense for this date
        Optional<Expense> existingMileage = expenseRepository.findByCarIdAndExpenseDateAndCategory(
                carId, date, ExpenseCategory.MILEAGE);

        Expense expense;
        if (existingMileage.isPresent()) {
            expense = existingMileage.get();
            expense.setCurrentMileage(currentMileage);
        } else {
            expense = new Expense();
            expense.setCar(car);
            expense.setExpenseDate(date);
            expense.setCategory(ExpenseCategory.MILEAGE);
            expense.setCurrentMileage(currentMileage);
            expense.setAmount(BigDecimal.ZERO);
        }

        expenseRepository.save(expense);

        // Update car's current mileage if this is higher
        if (car.getCurrentMileage() == null || currentMileage > car.getCurrentMileage()) {
            car.setCurrentMileage(currentMileage);
            carRepository.save(car);
        }
    }

    public Optional<BigDecimal> getTotalByDateRange(Long carId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.sumAmountByCarIdAndDateRange(carId, startDate, endDate);
    }

    public Map<ExpenseCategory, BigDecimal> getCategoryTotals(Long carId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByCarIdAndExpenseDateBetweenOrderByExpenseDateAsc(
                carId, startDate, endDate);

        return expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
    }
}
