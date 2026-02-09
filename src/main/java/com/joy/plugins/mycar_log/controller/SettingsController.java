package com.joy.plugins.mycar_log.controller;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.service.CarService;
import com.joy.plugins.mycar_log.service.UserService;
import com.joy.plugins.mycar_log.util.AuthUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class SettingsController {

    private final CarService carService;
    private final UserService userService;

    public SettingsController(CarService carService, UserService userService) {
        this.carService = carService;
        this.userService = userService;
    }

    @GetMapping("/settings")
    public String index(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = getCurrentUser(principal);
        List<Car> cars = carService.getCarsByUser(user);
        model.addAttribute("cars", cars);
        model.addAttribute("currentPage", "settings");
        return "settings/index";
    }

    @GetMapping("/car/{carId}/settings")
    public String carSettings(@PathVariable Long carId, @AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = getCurrentUser(principal);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("car", car.get());
        model.addAttribute("carId", carId);
        model.addAttribute("currentPage", "settings");
        return "settings/car";
    }

    private User getCurrentUser(OAuth2User principal) {
        String oauthId = AuthUtil.getOauthId(principal);
        return userService.getOrCreateUser(oauthId);
    }
}
