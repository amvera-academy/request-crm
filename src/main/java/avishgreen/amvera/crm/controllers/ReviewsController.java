package avishgreen.amvera.crm.controllers;

import avishgreen.amvera.crm.dto.SupportRequestReviewDto;
import avishgreen.amvera.crm.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReviewsController {

    private final ReviewService reviewService;

    @GetMapping("/reviews")
    public String getReviews(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        model.addAttribute("pageTitle", "Обращения");

        // Получаем списки обращений по статусам
        List<SupportRequestReviewDto> requiresAttention = reviewService.getRequiresAttentionRequests();
        List<SupportRequestReviewDto> unanswered = reviewService.getUnansweredRequests();
        List<SupportRequestReviewDto> archive = reviewService.getArchivedRequests();

        model.addAttribute("requiresAttention", requiresAttention);
        model.addAttribute("unanswered", unanswered);
        model.addAttribute("archive", archive);

        return "reviews";
    }
}