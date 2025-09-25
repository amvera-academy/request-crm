package avishgreen.amvera.crm.controllers;

import avishgreen.amvera.crm.dto.UserNoteDto;
import avishgreen.amvera.crm.enums.SupportRequestStatusType;
import avishgreen.amvera.crm.services.ReviewService;
import avishgreen.amvera.crm.services.UserNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class AJAXController {

    private final ReviewService reviewService;
    private final UserNoteService userNoteService;


    @GetMapping("/support-request/update-status")
    public RedirectView updateStatus(@RequestParam Long id, @RequestParam SupportRequestStatusType status) {
        reviewService.updateRequestStatus(id, status);

        // Перенаправляем пользователя обратно на ту же страницу
        return new RedirectView("/support-request/" + id);
    }

    @PostMapping("/support-request/update-note")
    public ResponseEntity<Map<String, String>> saveNote(@RequestBody UserNoteDto noteDto) {
        userNoteService.saveNote(noteDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Note saved successfully");

        return ResponseEntity.ok(response);
    }

}