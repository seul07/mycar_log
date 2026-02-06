package com.joy.plugins.mycar_log.dto;

import com.joy.plugins.mycar_log.entity.Expense;
import com.joy.plugins.mycar_log.entity.MaintenanceItem;
import com.joy.plugins.mycar_log.entity.enums.ExpenseCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseDto {

    private Long id;

    @NotNull(message = "{validation.required}")
    private LocalDate expenseDate;

    @NotNull(message = "{validation.required}")
    private ExpenseCategory category;

    @NotNull(message = "{validation.required}")
    @Min(value = 0, message = "{validation.positive}")
    private BigDecimal amount;

    private Long currentMileage;

    private BigDecimal pricePerLiter;

    private Double liters;

    private Long maintenanceItemId;

    private String insuranceType;

    private String description;

    // For display purposes
    private String maintenanceItemTitle;
    private Long drivenDistance;

    public ExpenseDto() {}

    public ExpenseDto(Expense expense) {
        this.id = expense.getId();
        this.expenseDate = expense.getExpenseDate();
        this.category = expense.getCategory();
        this.amount = expense.getAmount();
        this.currentMileage = expense.getCurrentMileage();
        this.pricePerLiter = expense.getPricePerLiter();
        this.liters = expense.getLiters();
        if (expense.getMaintenanceItem() != null) {
            this.maintenanceItemId = expense.getMaintenanceItem().getId();
            this.maintenanceItemTitle = expense.getMaintenanceItem().getTitle();
        }
        this.insuranceType = expense.getInsuranceType();
        this.description = expense.getDescription();
    }

    public Expense toEntity() {
        Expense expense = new Expense();
        expense.setExpenseDate(this.expenseDate);
        expense.setCategory(this.category);
        expense.setAmount(this.amount);
        expense.setCurrentMileage(this.currentMileage);
        expense.setPricePerLiter(this.pricePerLiter);
        expense.setLiters(this.liters);
        expense.setInsuranceType(this.insuranceType);
        expense.setDescription(this.description);
        return expense;
    }

    public void updateEntity(Expense expense) {
        expense.setExpenseDate(this.expenseDate);
        expense.setCategory(this.category);
        expense.setAmount(this.amount);
        expense.setCurrentMileage(this.currentMileage);
        expense.setPricePerLiter(this.pricePerLiter);
        expense.setLiters(this.liters);
        expense.setInsuranceType(this.insuranceType);
        expense.setDescription(this.description);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getCurrentMileage() {
        return currentMileage;
    }

    public void setCurrentMileage(Long currentMileage) {
        this.currentMileage = currentMileage;
    }

    public BigDecimal getPricePerLiter() {
        return pricePerLiter;
    }

    public void setPricePerLiter(BigDecimal pricePerLiter) {
        this.pricePerLiter = pricePerLiter;
    }

    public Double getLiters() {
        return liters;
    }

    public void setLiters(Double liters) {
        this.liters = liters;
    }

    public Long getMaintenanceItemId() {
        return maintenanceItemId;
    }

    public void setMaintenanceItemId(Long maintenanceItemId) {
        this.maintenanceItemId = maintenanceItemId;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaintenanceItemTitle() {
        return maintenanceItemTitle;
    }

    public void setMaintenanceItemTitle(String maintenanceItemTitle) {
        this.maintenanceItemTitle = maintenanceItemTitle;
    }

    public Long getDrivenDistance() {
        return drivenDistance;
    }

    public void setDrivenDistance(Long drivenDistance) {
        this.drivenDistance = drivenDistance;
    }
}
