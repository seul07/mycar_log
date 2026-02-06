package com.joy.plugins.mycar_log.controller;

import com.joy.plugins.mycar_log.dto.CarDto;
import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.entity.enums.CarType;
import com.joy.plugins.mycar_log.service.CarService;
import com.joy.plugins.mycar_log.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/car")
public class CarController {

    private final CarService carService;
    private final UserService userService;

    public CarController(CarService carService, UserService userService) {
        this.carService = carService;
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("carDto", new CarDto());
        model.addAttribute("carTypes", CarType.values());
        model.addAttribute("currentPage", "home");
        return "car/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute CarDto carDto,
                           BindingResult bindingResult,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("carTypes", CarType.values());
            model.addAttribute("currentPage", "home");
            return "car/register";
        }

        User user = getCurrentUser(session);
        Car car = carService.registerCar(carDto, user);
        return "redirect:/car/" + car.getId();
    }

    @GetMapping("/{carId}/edit")
    public String editForm(@PathVariable Long carId, HttpSession session, Model model) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());

        if (car.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("carDto", new CarDto(car.get()));
        model.addAttribute("carTypes", CarType.values());
        model.addAttribute("currentPage", "settings");
        return "car/register";
    }

    @PostMapping("/{carId}/edit")
    public String edit(@PathVariable Long carId,
                       @Valid @ModelAttribute CarDto carDto,
                       BindingResult bindingResult,
                       HttpSession session,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);

        if (bindingResult.hasErrors()) {
            model.addAttribute("carTypes", CarType.values());
            model.addAttribute("currentPage", "settings");
            return "car/register";
        }

        carDto.setId(carId);
        carService.updateCar(carDto, user.getId());
        return "redirect:/car/" + carId;
    }

    @PostMapping("/{carId}/delete")
    public String delete(@PathVariable Long carId, HttpSession session) {
        User user = getCurrentUser(session);
        carService.deleteCar(carId, user.getId());
        return "redirect:/";
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
