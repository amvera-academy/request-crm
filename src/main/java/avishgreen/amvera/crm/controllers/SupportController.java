package avishgreen.amvera.crm.controllers;

import avishgreen.amvera.crm.dto.SupportRequestDto;
import avishgreen.amvera.crm.models.SupportRequestModel;
import avishgreen.amvera.crm.services.ReviewRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SupportController {
    private final ReviewRequestService reviewRequestService;

    @GetMapping("/support/{id}")
    public String getSupportRequest(@PathVariable("id") Long requestId, Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        // Вместо генерации нового списка, получаем конкретное обращение по ID из сервиса
        SupportRequestDto supportRequestDto = reviewRequestService.getSupportRequestModelById(requestId);

        // Здесь можно добавить проверку, если request == null, чтобы обработать ошибку
        if (supportRequestDto == null) {
            // Например, перенаправить на страницу 404
            return "redirect:/error";
        }

        model.addAttribute("request", supportRequestDto); // Передаём найденный объект в шаблон
        return "support";
    }
}