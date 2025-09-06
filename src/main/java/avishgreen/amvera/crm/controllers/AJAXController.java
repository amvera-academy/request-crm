package avishgreen.amvera.crm.controllers;

import avishgreen.amvera.crm.enums.SupportRequestStatus;
import avishgreen.amvera.crm.services.ReviewRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
public class AJAXController {

    private final ReviewRequestService reviewRequestService;

    @GetMapping("/support/update-status")
    public RedirectView updateStatus(@RequestParam Long id, @RequestParam SupportRequestStatus status) {
        reviewRequestService.updateRequestStatus(id, status);

        // Перенаправляем пользователя обратно на ту же страницу
        return new RedirectView("/support/" + id);
    }
}