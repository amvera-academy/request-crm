package avishgreen.amvera.crm.controllers;

import avishgreen.amvera.crm.dto.SendMessageRequestDto;
import avishgreen.amvera.crm.dto.SupportRequestReviewDto;
import avishgreen.amvera.crm.dto.UserNoteDto;
import avishgreen.amvera.crm.enums.SupportRequestStatusType;
import avishgreen.amvera.crm.services.ReviewService;
import avishgreen.amvera.crm.services.UserNoteService;
import avishgreen.amvera.crm.services.TelegramSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@Slf4j
public class AJAXController {

    private final ReviewService reviewService;
    private final UserNoteService userNoteService;
    private final TelegramSenderService telegramSenderService;


    @GetMapping("/support-request/update-status")
    public RedirectView updateStatus(@RequestParam Long id, @RequestParam SupportRequestStatusType status) {
        reviewService.updateRequestStatus(id, status);

        // Перенаправляем пользователя обратно на ту же страницу
        return new RedirectView("/support-request/" + id);
    }

    @PostMapping("/support-request/update-note")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateNote(@RequestBody UserNoteDto noteDto) {
        userNoteService.saveNote(noteDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Note saved successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Обрабатывает POST-запрос на отправку ответного сообщения пользователю
     * из веб-интерфейса CRM.
     * * @param dto DTO с ID обращения и текстом сообщения.
     * @return 200 OK при успешной отправке, 500 INTERNAL_SERVER_ERROR при ошибке.
     */
    @PostMapping("/support-request/answer-to-request")
    @ResponseBody
    public ResponseEntity<Void> sendResponseToUser(@RequestBody SendMessageRequestDto dto) {
        log.info("Received request to send message for SupportRequest ID: {}", dto.supportRequestId());

        // Используем поля record (supportRequestId() и text())
        try {
            telegramSenderService.sendResponseToUser(dto.supportRequestId(), dto.text());
            log.info("Message successfully sent for request ID: {}", dto.supportRequestId());
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            // Если обращение не найдено или другие ошибки данных
            log.warn("Failed to send message: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (TelegramApiException e) {
            // Если возникла проблема с API Telegram (например, неправильный Chat ID)
            log.error("Telegram API error while sending message for ID {}: {}", dto.supportRequestId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (Exception e) {
            // Общая ошибка
            log.error("Unexpected error while sending message for ID {}", dto.supportRequestId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/request-reviews/unanswered-fragment")
    public String getUnansweredRequestsFragment(Model model) {
        // Используем существующий сервисный метод
        List<SupportRequestReviewDto> unanswered = reviewService.getUnansweredRequests();

        // Передаем данные в шаблон
        model.addAttribute("unanswered", unanswered);

        // Возвращаем имя фрагмента для рендеринга
        // Синтаксис: "название_файла :: название_фрагмента"
        return "fragments/unanswered-requests :: unansweredListFragment";
    }

    // Возвращает счетчик неотвеченных сообщений
    @GetMapping("/request-reviews/unanswered-count")
    @ResponseBody // Важно! Возвращаем чистый объект (число)
    public int getUnansweredRequestsCount() {
        return reviewService.getUnansweredRequests().size();
    }
}