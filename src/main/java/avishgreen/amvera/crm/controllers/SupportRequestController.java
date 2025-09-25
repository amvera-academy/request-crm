package avishgreen.amvera.crm.controllers;

import avishgreen.amvera.crm.dto.SupportRequestDto;
import avishgreen.amvera.crm.dto.UserNoteDto;
import avishgreen.amvera.crm.entities.AppUser;
import avishgreen.amvera.crm.services.AppUserService;
import avishgreen.amvera.crm.services.ReviewService;
import avishgreen.amvera.crm.services.UserNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SupportRequestController {
    private final ReviewService reviewService;
    private final AppUserService appUserService;
    private final UserNoteService userNoteService;

    @GetMapping("/support-request/{id}")
    public String getSupportRequest(@PathVariable("id") Long requestId, Model model, Authentication authentication) {

        // Выносим получение SupportRequestDto и добавление pageTitle за пределы if-else
        SupportRequestDto supportRequestDto = reviewService.getSupportRequestDtoById(requestId);
        if (supportRequestDto == null) {
            return "redirect:/error";
        }

        model.addAttribute("pageTitle", "Обращение " + requestId);
        model.addAttribute("request", supportRequestDto);

        if (authentication != null && authentication.isAuthenticated()) {
            var username = authentication.getName();
            model.addAttribute("username", username);
            AppUser appUser = appUserService.loadUserByUsername(username);
            model.addAttribute("appUser", appUser);

            Long currentUserId = appUser.getId();

            Long authorId = supportRequestDto.authorId();
            List<UserNoteDto> allNotes = userNoteService.getNotesByAuthorId(authorId);

            List<UserNoteDto> myNotes = allNotes.stream()
                    .filter(note -> note.creatorId().equals(currentUserId))
                    .collect(Collectors.toList());

            List<UserNoteDto> otherNotes = allNotes.stream()
                    .filter(note -> !note.creatorId().equals(currentUserId))
                    .collect(Collectors.toList());

            model.addAttribute("myNotes", myNotes);
            model.addAttribute("otherNotes", otherNotes);
            model.addAttribute("pastRequests", reviewService.getPreviousRequestsByAuthor(authorId, requestId));

        }

        return "support-request";
    }
}