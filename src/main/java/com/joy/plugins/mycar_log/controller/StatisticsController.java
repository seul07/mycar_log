package com.joy.plugins.mycar_log.controller;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.entity.enums.ExpenseCategory;
import com.joy.plugins.mycar_log.service.CarService;
import com.joy.plugins.mycar_log.service.ExpenseService;
import com.joy.plugins.mycar_log.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/statistics")
public class StatisticsController {

    private final CarService carService;
    private final ExpenseService expenseService;
    private final UserService userService;

    public StatisticsController(CarService carService, ExpenseService expenseService, UserService userService) {
        this.carService = carService;
        this.expenseService = expenseService;
        this.userService = userService;
    }

    @GetMapping
    public String index(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        List<Car> cars = carService.getCarsByUser(user);

        model.addAttribute("cars", cars);
        model.addAttribute("currentPage", "statistics");
        return "statistics/index";
    }

    @GetMapping("/car/{carId}")
    public String carStatistics(@PathVariable Long carId,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                 HttpSession session,
                                 Model model) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return "redirect:/statistics";
        }

        // Default date range: current month
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Map<ExpenseCategory, BigDecimal> categoryTotals = expenseService.getCategoryTotals(carId, startDate, endDate);
        Optional<BigDecimal> totalAmount = expenseService.getTotalByDateRange(carId, startDate, endDate);

        model.addAttribute("car", car.get());
        model.addAttribute("carId", carId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("categoryTotals", categoryTotals);
        model.addAttribute("totalAmount", totalAmount.orElse(BigDecimal.ZERO));
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("currentPage", "statistics");

        return "statistics/car";
    }

    @GetMapping("/car/{carId}/data")
    @ResponseBody
    public Map<String, Object> getStatisticsData(@PathVariable Long carId,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                  HttpSession session) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return Map.of();
        }

        Map<ExpenseCategory, BigDecimal> categoryTotals = expenseService.getCategoryTotals(carId, startDate, endDate);
        Optional<BigDecimal> totalAmount = expenseService.getTotalByDateRange(carId, startDate, endDate);

        return Map.of(
                "categoryTotals", categoryTotals,
                "totalAmount", totalAmount.orElse(BigDecimal.ZERO)
        );
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
