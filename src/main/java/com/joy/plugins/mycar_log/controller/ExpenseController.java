package com.joy.plugins.mycar_log.controller;

import com.joy.plugins.mycar_log.dto.ExpenseDto;
import com.joy.plugins.mycar_log.entity.Car;
import com.joy.plugins.mycar_log.entity.Expense;
import com.joy.plugins.mycar_log.entity.MaintenanceItem;
import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.entity.enums.ExpenseCategory;
import com.joy.plugins.mycar_log.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/car/{carId}/expense")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CarService carService;
    private final MileageService mileageService;
    private final MaintenanceService maintenanceService;
    private final UserService userService;

    public ExpenseController(ExpenseService expenseService,
                             CarService carService,
                             MileageService mileageService,
                             MaintenanceService maintenanceService,
                             UserService userService) {
        this.expenseService = expenseService;
        this.carService = carService;
        this.mileageService = mileageService;
        this.maintenanceService = maintenanceService;
        this.userService = userService;
    }

    @GetMapping("/{date}")
    public String dayDetail(@PathVariable Long carId,
                            @PathVariable String date,
                            HttpSession session,
                            Model model) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());
        if (car.isEmpty()) {
            return "redirect:/";
        }

        LocalDate expenseDate = LocalDate.parse(date);
        List<Expense> expenses = expenseService.getExpensesByDate(carId, expenseDate);

        // Filter out MILEAGE expenses from the list (mileage is shown separately)
        List<ExpenseDto> expenseDtos = expenses.stream()
                .filter(e -> e.getCategory() != ExpenseCategory.MILEAGE)
                .map(ExpenseDto::new)
                .toList();

        // Calculate total amount (excluding MILEAGE)
        BigDecimal totalAmount = expenses.stream()
                .filter(e -> e.getCategory() != ExpenseCategory.MILEAGE)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get mileage info for this date
        Long previousMileage = mileageService.getPreviousMileage(carId, expenseDate);
        Long todayMileage = expenseService.getMileageForDate(carId, expenseDate);

        model.addAttribute("car", car.get());
        model.addAttribute("carId", carId);
        model.addAttribute("date", expenseDate);
        model.addAttribute("dateStr", date);
        model.addAttribute("expenses", expenseDtos);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("previousMileage", previousMileage);
        model.addAttribute("todayMileage", todayMileage);
        model.addAttribute("currentPage", "home");
        return "expense/day-detail";
    }

    @GetMapping("/{date}/add")
    public String selectCategory(@PathVariable Long carId,
                                 @PathVariable String date,
                                 HttpSession session,
                                 Model model) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());
        if (car.isEmpty()) {
            return "redirect:/";
        }

        // Exclude MILEAGE from expense categories (mileage is handled separately)
        List<ExpenseCategory> categories = java.util.Arrays.stream(ExpenseCategory.values())
                .filter(c -> c != ExpenseCategory.MILEAGE)
                .toList();

        model.addAttribute("carId", carId);
        model.addAttribute("date", date);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", "home");
        return "expense/select-category";
    }

    @GetMapping("/{date}/add/{category}")
    public String addForm(@PathVariable Long carId,
                          @PathVariable String date,
                          @PathVariable ExpenseCategory category,
                          HttpSession session,
                          Model model) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());
        if (car.isEmpty()) {
            return "redirect:/";
        }

        ExpenseDto dto = new ExpenseDto();
        dto.setExpenseDate(LocalDate.parse(date));
        dto.setCategory(category);
        dto.setAmount(BigDecimal.ZERO);

        model.addAttribute("car", car.get());
        model.addAttribute("carId", carId);
        model.addAttribute("expenseDto", dto);
        model.addAttribute("date", date);
        model.addAttribute("category", category);
        model.addAttribute("previousMileage", mileageService.getPreviousMileage(carId, LocalDate.parse(date)));
        model.addAttribute("currentPage", "home");

        if (category == ExpenseCategory.MAINTENANCE) {
            List<MaintenanceItem> items = maintenanceService.getAllActiveItems(carId, car.get().getCarType());
            model.addAttribute("maintenanceItems", items);
        }

        return "expense/form";
    }

    @PostMapping("/{date}/add/{category}")
    public String add(@PathVariable Long carId,
                      @PathVariable String date,
                      @PathVariable ExpenseCategory category,
                      @Valid @ModelAttribute ExpenseDto expenseDto,
                      BindingResult bindingResult,
                      HttpSession session,
                      Model model,
                      RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());
        if (car.isEmpty()) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("car", car.get());
            model.addAttribute("carId", carId);
            model.addAttribute("date", date);
            model.addAttribute("category", category);
            model.addAttribute("previousMileage", mileageService.getPreviousMileage(carId, LocalDate.parse(date)));
            model.addAttribute("currentPage", "home");

            if (category == ExpenseCategory.MAINTENANCE) {
                List<MaintenanceItem> items = maintenanceService.getAllActiveItems(carId, car.get().getCarType());
                model.addAttribute("maintenanceItems", items);
            }
            return "expense/form";
        }

        Expense expense = expenseDto.toEntity();
        expense.setCategory(category);
        expense.setExpenseDate(LocalDate.parse(date));

        if (category == ExpenseCategory.MAINTENANCE && expenseDto.getMaintenanceItemId() != null) {
            MaintenanceItem item = maintenanceService.findById(expenseDto.getMaintenanceItemId()).orElse(null);
            expense.setMaintenanceItem(item);
        }

        expenseService.createExpense(carId, expense);
        return "redirect:/car/" + carId + "/expense/" + date;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long carId,
                           @PathVariable Long id,
                           HttpSession session,
                           Model model) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());
        if (car.isEmpty()) {
            return "redirect:/";
        }

        Optional<Expense> expense = expenseService.findById(id);
        if (expense.isEmpty()) {
            return "redirect:/car/" + carId;
        }

        ExpenseDto dto = new ExpenseDto(expense.get());
        String date = expense.get().getExpenseDate().toString();

        model.addAttribute("car", car.get());
        model.addAttribute("carId", carId);
        model.addAttribute("expenseDto", dto);
        model.addAttribute("date", date);
        model.addAttribute("category", expense.get().getCategory());
        model.addAttribute("isEdit", true);
        model.addAttribute("previousMileage", mileageService.getPreviousMileage(carId, expense.get().getExpenseDate()));
        model.addAttribute("currentPage", "home");

        if (expense.get().getCategory() == ExpenseCategory.MAINTENANCE) {
            List<MaintenanceItem> items = maintenanceService.getAllActiveItems(carId, car.get().getCarType());
            model.addAttribute("maintenanceItems", items);
        }

        return "expense/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long carId,
                       @PathVariable Long id,
                       @Valid @ModelAttribute ExpenseDto expenseDto,
                       BindingResult bindingResult,
                       HttpSession session,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        Optional<Car> car = carService.findByIdAndUserId(carId, user.getId());
        if (car.isEmpty()) {
            return "redirect:/";
        }

        Optional<Expense> existingExpense = expenseService.findById(id);
        if (existingExpense.isEmpty()) {
            return "redirect:/car/" + carId;
        }

        String date = existingExpense.get().getExpenseDate().toString();

        if (bindingResult.hasErrors()) {
            model.addAttribute("car", car.get());
            model.addAttribute("carId", carId);
            model.addAttribute("date", date);
            model.addAttribute("category", existingExpense.get().getCategory());
            model.addAttribute("isEdit", true);
            model.addAttribute("previousMileage", mileageService.getPreviousMileage(carId, existingExpense.get().getExpenseDate()));
            model.addAttribute("currentPage", "home");

            if (existingExpense.get().getCategory() == ExpenseCategory.MAINTENANCE) {
                List<MaintenanceItem> items = maintenanceService.getAllActiveItems(carId, car.get().getCarType());
                model.addAttribute("maintenanceItems", items);
            }
            return "expense/form";
        }

        Expense expense = existingExpense.get();
        expenseDto.updateEntity(expense);

        if (expense.getCategory() == ExpenseCategory.MAINTENANCE && expenseDto.getMaintenanceItemId() != null) {
            MaintenanceItem item = maintenanceService.findById(expenseDto.getMaintenanceItemId()).orElse(null);
            expense.setMaintenanceItem(item);
        }

        expenseService.updateExpense(id, expense);
        return "redirect:/car/" + carId + "/expense/" + date;
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

        Optional<Expense> expense = expenseService.findById(id);
        if (expense.isEmpty()) {
            return "redirect:/car/" + carId;
        }

        String date = expense.get().getExpenseDate().toString();
        expenseService.deleteExpense(id);
        return "redirect:/car/" + carId + "/expense/" + date;
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
