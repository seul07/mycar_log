package com.joy.plugins.mycar_log.service;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.MaintenanceItem;
import com.joy.plugins.mycar_log.entity.enums.CarType;
import com.joy.plugins.mycar_log.repository.CarRepository;
import com.joy.plugins.mycar_log.repository.MaintenanceItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MaintenanceService {

    private final MaintenanceItemRepository maintenanceItemRepository;
    private final CarRepository carRepository;

    public MaintenanceService(MaintenanceItemRepository maintenanceItemRepository,
                              CarRepository carRepository) {
        this.maintenanceItemRepository = maintenanceItemRepository;
        this.carRepository = carRepository;
    }

    public List<MaintenanceItem> getDefaultItems(CarType carType) {
        return maintenanceItemRepository.findByCarTypeAndIsDefaultTrueAndIsActiveTrueOrderByTitleAsc(carType);
    }

    public List<MaintenanceItem> getCustomItems(Long carId) {
        return maintenanceItemRepository.findByCarIdAndIsDefaultFalseAndIsActiveTrueOrderByTitleAsc(carId);
    }

    public List<MaintenanceItem> getAllActiveItems(Long carId, CarType carType) {
        return maintenanceItemRepository.findAllActiveItems(carId, carType);
    }

    @Transactional
    public MaintenanceItem createCustomItem(Long carId, String title, String description) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + carId));

        MaintenanceItem item = new MaintenanceItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setCarType(car.getCarType());
        item.setIsDefault(false);
        item.setCar(car);
        item.setIsActive(true);

        return maintenanceItemRepository.save(item);
    }

    @Transactional
    public boolean deleteItem(Long itemId) {
        Optional<MaintenanceItem> item = maintenanceItemRepository.findById(itemId);
        if (item.isEmpty()) {
            return false;
        }

        if (item.get().getIsDefault()) {
            return false;
        }

        if (isItemInUse(itemId)) {
            return false;
        }

        MaintenanceItem maintenanceItem = item.get();
        maintenanceItem.setIsActive(false);
        maintenanceItemRepository.save(maintenanceItem);
        return true;
    }

    public boolean isItemInUse(Long itemId) {
        return maintenanceItemRepository.isItemInUse(itemId);
    }

    public Optional<MaintenanceItem> findById(Long id) {
        return maintenanceItemRepository.findById(id);
    }

    @Transactional
    public MaintenanceItem createDefaultItem(CarType carType, String title, String description) {
        MaintenanceItem item = new MaintenanceItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setCarType(carType);
        item.setIsDefault(true);
        item.setIsActive(true);
        return maintenanceItemRepository.save(item);
    }

    public long countDefaultItems() {
        return maintenanceItemRepository.count();
    }
}
