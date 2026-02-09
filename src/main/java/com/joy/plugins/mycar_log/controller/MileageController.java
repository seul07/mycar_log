package com.joy.plugins.mycar_log.controller;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.service.CarService;
import com.joy.plugins.mycar_log.service.ExpenseService;
import com.joy.plugins.mycar_log.service.MileageService;
import com.joy.plugins.mycar_log.service.UserService;
import com.joy.plugins.mycar_log.util.AuthUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/car/{carId}/mileage")
public class MileageController {

    private final CarService carService;
    private final ExpenseService expenseService;
    private final MileageService mileageService;
    private final UserService userService;

    public MileageController(CarService carService, ExpenseService expenseService,
                             MileageService mileageService, UserService userService) {
        this.carService = carService;
        this.expenseService = expenseService;
        this.mileageService = mileageService;
        this.userService = userService;
    }

    @PostMapping("/{date}")
    public String saveMileage(@PathVariable Long carId,
                              @PathVariable String date,
                              @RequestParam(required = false) Long currentMileage,
                              @AuthenticationPrincipal OAuth2User principal) {
        User user = getCurrentUser(principal);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return "redirect:/";
        }

        // Only save if mileage value is provided
        if (currentMileage != null && currentMileage > 0) {
            LocalDate mileageDate = LocalDate.parse(date);
            expenseService.saveOrUpdateMileage(carId, mileageDate, currentMileage);
        }

        return "redirect:/car/" + carId + "/expense/" + date;
    }

    private User getCurrentUser(OAuth2User principal) {
        String oauthId = AuthUtil.getOauthId(principal);
        return userService.getOrCreateUser(oauthId);
    }
}
