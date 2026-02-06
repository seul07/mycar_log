package com.joy.plugins.mycar_log.controller;

import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.MaintenanceItem;
import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.service.CarService;
import com.joy.plugins.mycar_log.service.MaintenanceService;
import com.joy.plugins.mycar_log.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/car/{carId}/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final CarService carService;
    private final UserService userService;

    public MaintenanceController(MaintenanceService maintenanceService, CarService carService, UserService userService) {
        this.maintenanceService = maintenanceService;
        this.carService = carService;
        this.userService = userService;
    }

    @GetMapping
    public String list(@PathVariable Long carId, HttpSession session, Model model) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return "redirect:/";
        }

        List<MaintenanceItem> defaultItems = maintenanceService.getDefaultItems(car.get().getCarType());
        List<MaintenanceItem> customItems = maintenanceService.getCustomItems(car.get().getId());

        model.addAttribute("car", car.get());
        model.addAttribute("carId", carId);
        model.addAttribute("defaultItems", defaultItems);
        model.addAttribute("customItems", customItems);
        model.addAttribute("currentPage", "settings");
        return "maintenance/list";
    }

    @GetMapping("/add")
    public String addForm(@PathVariable Long carId, HttpSession session, Model model) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("carId", carId);
        model.addAttribute("currentPage", "settings");
        return "maintenance/add";
    }

    @PostMapping("/add")
    public String add(@PathVariable Long carId,
                      @RequestParam String title,
                      @RequestParam(required = false) String description,
                      HttpSession session,
                      RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return "redirect:/";
        }

        if (title == null || title.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Title is required");
            return "redirect:/car/" + carId + "/maintenance/add";
        }

        maintenanceService.createCustomItem(car.get().getId(), title.trim(),
                description != null ? description.trim() : null);
        return "redirect:/car/" + carId + "/maintenance";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long carId,
                         @PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return "redirect:/";
        }

        boolean deleted = maintenanceService.deleteItem(id);

        if (!deleted) {
            redirectAttributes.addFlashAttribute("error", "maintenance.delete.inuse");
        }

        return "redirect:/car/" + carId + "/maintenance";
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
