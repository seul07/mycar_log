package com.joy.plugins.mycar_log.controller;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.service.CarService;
import com.joy.plugins.mycar_log.service.ExpenseService;
import com.joy.plugins.mycar_log.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class HomeController {

    private final CarService carService;
    private final ExpenseService expenseService;
    private final UserService userService;

    public HomeController(CarService carService, ExpenseService expenseService, UserService userService) {
        this.carService = carService;
        this.expenseService = expenseService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        List<Car> cars = carService.getCarsByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("cars", cars);
        model.addAttribute("currentPage", "home");
        return "index";
    }

    @GetMapping("/car/{carId}")
    public String carDetail(@PathVariable Long carId, HttpSession session, Model model) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return "redirect:/";
        }

        LocalDate now = LocalDate.now();
        Map<String, Object> monthlyExpenses = expenseService.getMonthlyCalendarData(
                carId, now.getYear(), now.getMonthValue());

        model.addAttribute("user", user);
        model.addAttribute("car", car.get());
        model.addAttribute("monthlyExpenses", monthlyExpenses);
        model.addAttribute("currentPage", "home");
        return "car/detail";
    }

    @GetMapping("/car/{carId}/calendar/{year}/{month}")
    @ResponseBody
    public Map<String, Object> getCalendarData(@PathVariable Long carId,
                                                @PathVariable int year,
                                                @PathVariable int month,
                                                HttpSession session) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return Map.of();
        }

        return expenseService.getMonthlyCalendarData(carId, year, month);
    }

    private User getCurrentUser(HttpSession session) {
        String sessionId = session.getId();
        String firebaseUid = (String) session.getAttribute("firebaseUid");

        if (firebaseUid == null) {
            // Generate anonymous UID for development
            firebaseUid = "anon_" + sessionId;
            session.setAttribute("firebaseUid", firebaseUid);
        }

        return userService.getOrCreateUser(firebaseUid);
    }
}
