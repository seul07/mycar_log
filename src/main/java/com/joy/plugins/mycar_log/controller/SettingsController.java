package com.joy.plugins.mycar_log.controller;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.service.CarService;
import com.joy.plugins.mycar_log.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String index(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        List<Car> cars = carService.getCarsByUser(user);
        model.addAttribute("cars", cars);
        model.addAttribute("currentPage", "settings");
        return "settings/index";
    }

    @GetMapping("/car/{carId}/settings")
    public String carSettings(@PathVariable Long carId, HttpSession session, Model model) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("car", car.get());
        model.addAttribute("carId", carId);
        model.addAttribute("currentPage", "settings");
        return "settings/car";
    }

    private User getCurrentUser(HttpSession session) {
        String firebaseUid = (String) session.getAttribute("firebaseUid");
        if (firebaseUid == null) {
            firebaseUid = "anon_" + session.getId();
            session.setAttribute("firebaseUid", firebaseUid);
        }
        return userService.getOrCreateUser(firebaseUid);
    }
}
