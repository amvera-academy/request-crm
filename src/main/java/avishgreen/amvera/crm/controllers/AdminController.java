package avishgreen.amvera.crm.controllers;

import avishgreen.amvera.crm.entities.AppUser;
import avishgreen.amvera.crm.services.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AppUserService appUserService;

    /**
     * Эндпойнт для корневого пути /admin.
     * Осуществляет редирект на основной раздел управления пользователями.
     */
    @GetMapping
    public String adminIndex() {
        return "redirect:/admin/users-list";
    }

    /**
     * Основной эндпойнт для отображения списка пользователей.
     * Использует users-list.html
     */
    @GetMapping("/users-list")
    public String listUsers(Model model, Authentication authentication) {
        // 1. Получаем имя текущего пользователя (администратора) для шапки
        String currentUsername = authentication != null ? authentication.getName() : "Гость";

        // 2. Получаем список пользователей через сервисный слой
        List<AppUser> users = appUserService.findAll();

        // 3. Добавляем данные в модель для Thymeleaf
        model.addAttribute("pageTitle", "Управление Пользователями");
        model.addAttribute("username", currentUsername);
        model.addAttribute("users", users);

        // 4. Возвращаем имя шаблона
        return "users-list";
    }

    /**
     * Эндпойнт для отображения формы редактирования пользователя.
     * GET /admin/users/{id}/edit
     */
    @GetMapping("/user/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model, Authentication authentication) {
        // Получаем пользователя по ID через сервисный слой
        AppUser user = appUserService.findById(id);

        model.addAttribute("pageTitle", "Редактирование пользователя: " + user.getUsername());
        model.addAttribute("username", authentication != null ? authentication.getName() : "Гость");
        model.addAttribute("user", user);

        // Список доступных ролей для выпадающего списка
        model.addAttribute("availableRoles", List.of("ROLE_ADMIN", "ROLE_USER"));

        return "users-edit";
    }

    /**
     * Эндпойнт для обработки сохранения изменений пользователя.
     * POST /admin/users/{id}/edit
     */
    @PostMapping("/user/{id}/edit")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") AppUser updatedUser) {
        try {
            appUserService.updateUser(id, updatedUser);
            // После успешного сохранения, перенаправляем обратно к списку
            return "redirect:/admin/users-list?success=updated";
        } catch (UsernameNotFoundException e) {
            // В случае ошибки (пользователь не найден), можно вернуть на страницу с ошибкой
            return "redirect:/admin/users-list?error=notfound";
        }
    }
}