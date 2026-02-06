package com.joy.plugins.mycar_log.repository;

import com.joy.plugins.mycar_log.entity.MaintenanceItem;
import com.joy.plugins.mycar_log.entity.enums.CarType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceItemRepository extends JpaRepository<MaintenanceItem, Long> {

    List<MaintenanceItem> findByCarTypeAndIsDefaultTrueAndIsActiveTrueOrderByTitleAsc(CarType carType);

    List<MaintenanceItem> findByCarIdAndIsDefaultFalseAndIsActiveTrueOrderByTitleAsc(Long carId);

    @Query("SELECT m FROM MaintenanceItem m WHERE m.isActive = true AND " +
           "((m.isDefault = true AND m.carType = :carType) OR (m.isDefault = false AND m.car.id = :carId)) " +
           "ORDER BY m.isDefault DESC, m.title ASC")
    List<MaintenanceItem> findAllActiveItems(@Param("carId") Long carId, @Param("carType") CarType carType);

    @Query("SELECT COUNT(e) > 0 FROM Expense e WHERE e.maintenanceItem.id = :itemId")
    boolean isItemInUse(@Param("itemId") Long itemId);
}
