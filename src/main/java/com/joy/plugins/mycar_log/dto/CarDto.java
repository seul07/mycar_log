package com.joy.plugins.mycar_log.dto;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.enums.CarType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CarDto {

    private Long id;

    @NotBlank(message = "{validation.required}")
    private String manufacturer;

    @NotBlank(message = "{validation.required}")
    private String modelName;

    @NotNull(message = "{validation.required}")
    @Min(value = 1900, message = "{validation.positive}")
    private Integer modelYear;

    @NotNull(message = "{validation.required}")
    @Min(value = 0, message = "{validation.positive}")
    private Long initialMileage;

    @NotNull(message = "{validation.required}")
    private CarType carType;

    public CarDto() {}

    public CarDto(Car car) {
        this.id = car.getId();
        this.manufacturer = car.getManufacturer();
        this.modelName = car.getModelName();
        this.modelYear = car.getModelYear();
        this.initialMileage = car.getInitialMileage();
        this.carType = car.getCarType();
    }

    public Car toEntity() {
        Car car = new Car();
        car.setManufacturer(this.manufacturer);
        car.setModelName(this.modelName);
        car.setModelYear(this.modelYear);
        car.setInitialMileage(this.initialMileage);
        car.setCurrentMileage(this.initialMileage); // Set current = initial when creating
        car.setCarType(this.carType);
        return car;
    }

    public void updateEntity(Car car) {
        car.setManufacturer(this.manufacturer);
        car.setModelName(this.modelName);
        car.setModelYear(this.modelYear);
        car.setInitialMileage(this.initialMileage);
        car.setCarType(this.carType);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public Long getInitialMileage() {
        return initialMileage;
    }

    public void setInitialMileage(Long initialMileage) {
        this.initialMileage = initialMileage;
    }

    public CarType getCarType() {
        return carType;
    }

    public void setCarType(CarType carType) {
        this.carType = carType;
    }
}
