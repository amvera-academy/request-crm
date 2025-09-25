package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.entities.TelegramUser;
import avishgreen.amvera.crm.entities.TelegramMessage;
import avishgreen.amvera.crm.enums.SupportRequestStatusType;
import avishgreen.amvera.crm.repositories.SupportRequestRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.EnumSet;
import java.util.List;
import java.util.Comparator;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SupportRequestService {
    private final TelegramUserService userService;
    private final TelegramMessageService messageService;
    private final SupportRequestRepository supportRequestRepository;
    private final ModeratorsService moderatorsService;

    public SupportRequest getSupportRequestById(Long id) {
        return supportRequestRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Wrong supportrequest id [%s]".formatted(id))
                );
    }

    public SupportRequest getSupportRequestByIdWithMessages(Long id) {
        var request = supportRequestRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Wrong support request id [%s]".formatted(id))
                );
        // Принудительная инициализация списка сообщений и отправителей
//        if (request.getMessages() != null) {
            request.getMessages().forEach(message -> {
                // Инициализируем отправителя для каждого сообщения
                if (message.getSender() != null) {
                    // Hibernate.initialize(message.getSender()); // Если вы используете пакет Hibernate
                    // Или просто вызов getter'а, чтобы инициировать загрузку:
                    message.getSender().getFirstName();
                }
            });
//        }
        return request;
    }

    @Transactional
    public SupportRequest processNewMessage(@NonNull Message message,
                                            @NonNull User user) {
        TelegramUser sender = userService.getOrCreateIfNeed(user);
        TelegramMessage telegramMessage = messageService.saveNewMessage(message, user);

        SupportRequest supportRequest;

        if (message.getReplyToMessage() != null) {
            // Это ответ. Ищем самое первое сообщение в цепочке
            Integer originalMessageId = message.getReplyToMessage().getMessageId();

            // Находим корневое сообщение
            TelegramMessage rootMessage = messageService.findRootMessage(originalMessageId);

            if (rootMessage != null && rootMessage.getSupportRequest() != null) {
                supportRequest = rootMessage.getSupportRequest();
            } else {
                // Цепочка не найдена или не привязана к обращению
                supportRequest = createNewSupportRequest(sender, message.getChatId());
            }
        } else {
            // Это не ответ, используем старую логику
            Set<SupportRequestStatusType> closedStatuses = EnumSet.of(
                    SupportRequestStatusType.COMPLETED,
                    SupportRequestStatusType.IGNORE
            );
            var chatId =  message.getChatId();
            List<SupportRequest> existingRequests = supportRequestRepository.findByAuthorIdAndChatIdAndStatusNotIn(user.getId(), chatId, closedStatuses);

            if (!existingRequests.isEmpty()) {
                supportRequest = existingRequests.stream()
                        .max(Comparator.comparing(SupportRequest::getLastMessageAt))
                        .orElseThrow(() -> new IllegalStateException("Unexpectedly found a request list that is not empty but has no maximum element."));
            } else {
                supportRequest = createNewSupportRequest(sender, message.getChatId());
            }
        }

        // Обновление обращения
        telegramMessage.setSupportRequest(supportRequest);
        telegramMessage = messageService.updateMessage(telegramMessage);

        supportRequest.getMessages().add(telegramMessage);
        supportRequest.setLastMessageAt(telegramMessage.getSentAt());
        supportRequest.setLastMessage(telegramMessage);
        supportRequest.getParticipants().add(sender);

        // Логика смены статуса
        if(moderatorsService.isModerator(sender.getId())){
            if(supportRequest.getStatus().equals(SupportRequestStatusType.UNANSWERED)){
                supportRequest.setStatus(SupportRequestStatusType.ANSWERED);
            }
        }else{
            if(supportRequest.getStatus().equals(SupportRequestStatusType.ANSWERED)){
                supportRequest.setStatus(SupportRequestStatusType.UNANSWERED);
            }
        }

        return supportRequestRepository.save(supportRequest);
    }

    private SupportRequest createNewSupportRequest(TelegramUser sender, Long chatId) {
        return supportRequestRepository.save(SupportRequest.builder()
                .author(sender)
                .status(SupportRequestStatusType.UNANSWERED)
                .chatId(chatId)
                .build());
    }

    public List<SupportRequest> getRequestsByStatus(SupportRequestStatusType status) {
        return supportRequestRepository.findByStatusOrderByLastMessageAtDesc(status);
    }

    public List<SupportRequest> getRequestsByStatuses(Set<SupportRequestStatusType> statuses) {
        return supportRequestRepository.findByStatusInOrderByLastMessageAtDesc(statuses);
    }

    public List<SupportRequest> getPreviousRequestsByAuthor(Long authorId, Long currentRequestId) {
        return supportRequestRepository.findByAuthorIdAndIdNotOrderByLastMessageAtDesc(authorId, currentRequestId);
    }
}
