package avishgreen.amvera.crm.controllers;

import avishgreen.amvera.crm.dto.SupportRequestDto;
import avishgreen.amvera.crm.services.ReviewRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SupportController {
    private final ReviewRequestService reviewRequestService;

    @GetMapping("/support/{id}")
    public String getSupportRequest(@PathVariable("id") Long requestId, Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        model.addAttribute("pageTitle", "Обращение " + requestId);

        SupportRequestDto supportRequestDto = reviewRequestService.getSupportRequestModelById(requestId);

        if (supportRequestDto == null) {
            return "redirect:/error";
        }

        // Получаем предыдущие обращения того же автора
        List<SupportRequestDto> previousRequests = reviewRequestService.getPreviousRequestsByAuthor(supportRequestDto.author(), supportRequestDto.id());

        model.addAttribute("request", supportRequestDto);
        model.addAttribute("pastRequests", previousRequests); // Добавляем список предыдущих обращений в модель

        return "support";
    }
}