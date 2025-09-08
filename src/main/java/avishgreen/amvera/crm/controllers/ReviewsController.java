package avishgreen.amvera.crm.controllers;

import avishgreen.amvera.crm.dto.SupportRequestDto;
import avishgreen.amvera.crm.enums.SupportRequestStatus;
import avishgreen.amvera.crm.services.ReviewRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReviewsController {

    private final ReviewRequestService reviewRequestService;

    @GetMapping("/reviews")
    public String getReviews(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        model.addAttribute("pageTitle", "Обращения");

        // Получаем списки обращений по статусам
        List<SupportRequestDto> requiresAttention = reviewRequestService.getRequestsByStatus(SupportRequestStatus.REQUIRES_ATTENTION);
        List<SupportRequestDto> unanswered = reviewRequestService.getRequestsByStatus(SupportRequestStatus.UNANSWERED);
        List<SupportRequestDto> archive = reviewRequestService.getArchivedRequests(100);

        model.addAttribute("requiresAttention", requiresAttention);
        model.addAttribute("unanswered", unanswered);
        model.addAttribute("archive", archive);

        return "reviews";
    }
}