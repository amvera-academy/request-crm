package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.entities.TelegramMessage;
import avishgreen.amvera.crm.entities.TelegramUser;
import avishgreen.amvera.crm.enums.SupportRequestStatusType;
import avishgreen.amvera.crm.repositories.SupportRequestRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
                    Hibernate.initialize(message.getSender());
                    // Или просто вызов getter'а, чтобы инициировать загрузку:
//                    message.getSender().getFirstName();
                }
            });
//        }
        return request;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public SupportRequest findRequestByIdNoCache(Long id) {
        // Используем явно объявленный метод с хинтом
        return supportRequestRepository.findByIdBypassingCache(id)
                .orElseThrow(() -> new RuntimeException("Request not found after commit: " + id));
    }

    @Transactional
    public SupportRequest processNewMessage(@NonNull Message message,
                                            @NonNull User user) {
        TelegramUser sender = userService.getOrCreateIfNeed(user);
        TelegramMessage telegramMessage = messageService.saveNewMessage(message, user);

        // Если сообщение уже привязано к заявке, просто возвращаем ее.
        if (telegramMessage.getSupportRequest() != null) {
            // Мы уже обработали это сообщение. Возвращаем существующую заявку.
            return telegramMessage.getSupportRequest();
        }

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
                log.warn("new request {} user [{}] sender [{}] reply [{}]",supportRequest.getId(),user,sender,message.getReplyToMessage());
            }
        } else {
            // Это не ответ, используем старую логику
            Set<SupportRequestStatusType> closedStatuses = EnumSet.of(
                    SupportRequestStatusType.COMPLETED,
                    SupportRequestStatusType.IGNORE
            );
            var chatId =  message.getChatId();
            List<SupportRequest> existingRequests = supportRequestRepository.findByAuthorIdAndChatIdAndStatusNotIn(user.getId(), chatId, closedStatuses);

            if (existingRequests.isEmpty()) {
                log.error("CRITICAL SEARCH FAIL (ELSE section): No active requests found for user {} in chat {}. Statuses checked: {}.",
                        user.getId(), chatId, closedStatuses);
                log.info("Creating new support request for sender object: {}", sender);
                // Попробуем найти ВСЕ запросы, чтобы увидеть, где находится старый
                List<SupportRequest> allRequests = supportRequestRepository.findByAuthorIdAndChatId(user.getId(), chatId);
                log.error("DEBUG: Found ALL requests for this user: {}", allRequests.stream()
                        .map(r -> r.getId() + ":" + r.getStatus())
                        .collect(Collectors.joining(", ")));
                supportRequest = createNewSupportRequest(sender, message.getChatId());
                log.info("Created new support request PRE-SAVE. Author ID: {}, Chat ID: {}",
                        (supportRequest.getAuthor() != null ? supportRequest.getAuthor().getId() : "NULL"),
                        supportRequest.getChatId());
            } else {
                supportRequest = existingRequests.stream()
                        .filter(Objects::nonNull) // Не забываем про null-элементы
                        .max(Comparator.comparing(SupportRequest::getLastMessageAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .orElseThrow(() -> new IllegalStateException("Список запросов пуст или неожиданно пуст."));
            }
        }

        // Обновление обращения
        telegramMessage.setSupportRequest(supportRequest);
//        telegramMessage = messageService.updateMessage(telegramMessage);

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
