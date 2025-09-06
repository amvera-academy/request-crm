package avishgreen.amvera.crm.controllers;

import avishgreen.amvera.crm.dto.SupportRequestDto;
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
    public String reviews(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        List<SupportRequestDto> supportRequestsList = reviewRequestService.getReviewRequestsForView();
        model.addAttribute("requests", supportRequestsList);

        return "reviews";
    }
}