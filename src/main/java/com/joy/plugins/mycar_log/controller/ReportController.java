package com.joy.plugins.mycar_log.controller;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.entity.enums.ExpenseCategory;
import com.joy.plugins.mycar_log.service.CarService;
import com.joy.plugins.mycar_log.service.ExpenseService;
import com.joy.plugins.mycar_log.service.MileageService;
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
@RequestMapping("/report")
public class ReportController {

    private final CarService carService;
    private final ExpenseService expenseService;
    private final MileageService mileageService;
    private final UserService userService;

    public ReportController(CarService carService, ExpenseService expenseService,
                            MileageService mileageService, UserService userService) {
        this.carService = carService;
        this.expenseService = expenseService;
        this.mileageService = mileageService;
        this.userService = userService;
    }

    @GetMapping
    public String index(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        List<Car> cars = carService.getCarsByUser(user);

        model.addAttribute("cars", cars);
        model.addAttribute("currentPage", "report");
        return "report/index";
    }

    @GetMapping("/car/{carId}")
    public String carReport(@PathVariable Long carId,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                            HttpSession session,
                            Model model) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return "redirect:/report";
        }

        // Default date range: current month
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Get expense data by category
        Map<ExpenseCategory, BigDecimal> categoryTotals = expenseService.getCategoryTotals(carId, startDate, endDate);
        Optional<BigDecimal> totalAmount = expenseService.getTotalByDateRange(carId, startDate, endDate);

        // Get mileage statistics
        Map<String, Long> mileageStats = mileageService.getMileageStatsForDateRange(carId, startDate, endDate);
        Long totalMileage = mileageService.getTotalMileage(carId);

        model.addAttribute("car", car.get());
        model.addAttribute("carId", carId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("categoryTotals", categoryTotals);
        model.addAttribute("totalAmount", totalAmount.orElse(BigDecimal.ZERO));
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("mileageStats", mileageStats);
        model.addAttribute("totalMileage", totalMileage);
        model.addAttribute("currentPage", "report");

        return "report/car";
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
