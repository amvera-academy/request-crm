package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.dto.DisplayMediaDto;
import avishgreen.amvera.crm.dto.SupportRequestDto;
import avishgreen.amvera.crm.dto.TelegramMediaDto;
import avishgreen.amvera.crm.dto.TelegramMessageDto;
import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.entities.TelegramMessage;
import avishgreen.amvera.crm.entities.TelegramUser;
import avishgreen.amvera.crm.enums.SupportRequestStatusType;
import avishgreen.amvera.crm.enums.TelegramMediaUsageType;
import avishgreen.amvera.crm.mappers.DisplayMediaMapper;
import avishgreen.amvera.crm.mappers.SupportRequestMapper;
import avishgreen.amvera.crm.mappers.TelegramMessageMapper;
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
    private final TelegramMessageMapper messageMapper;
    private final SupportRequestMapper supportRequestMapper;
    private final DisplayMediaMapper displayMediaMapper;

    public SupportRequest getSupportRequestById(Long id) {
        var request = supportRequestRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Wrong support request id [%s]".formatted(id))
                );
        // Принудительная инициализация списка сообщений и отправителей
        request.getMessages().forEach(message -> {
            if (message.getSender() != null) {
                Hibernate.initialize(message.getSender());
            }
            // Также нужно инициализировать медиафайлы!
            if (message.getMediaFiles() != null) {
                Hibernate.initialize(message.getMediaFiles());
            }
        });
        return request;    }

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

        SupportRequest supportRequest=null;
        Set<SupportRequestStatusType> closedStatuses = EnumSet.of(
                SupportRequestStatusType.COMPLETED,
                SupportRequestStatusType.IGNORE
        );

        //попробуем привязать к обращению из реплая
        if (message.getReplyToMessage() != null) {
            // Это ответ. Ищем самое первое сообщение в цепочке
            Integer originalMessageId = message.getReplyToMessage().getMessageId();

            // Находим корневое сообщение
            TelegramMessage rootMessage = messageService.findRootMessage(originalMessageId);

            if (rootMessage != null && rootMessage.getSupportRequest() != null) {
                SupportRequest foundRequest = rootMessage.getSupportRequest();
                if (!closedStatuses.contains(foundRequest.getStatus())) {
                    supportRequest = foundRequest;
                } else {
                    log.warn("Reply found, but request status is closed ({}). Will use casual logic.", foundRequest.getStatus());
                    // supportRequest останется null, и мы перейдем к "casual search logic"
                }
            } else {
                // Цепочка не найдена или не привязана к обращению
                log.warn("NULL root message, will use casual logic. user [{}] sender [{}] reply [{}]",user,sender,message.getReplyToMessage());
            }
        }

        //casual search logic
        if (supportRequest == null) {
            // Это не ответ, используем старую логику - ищем открытые обращения и повторно их используем
            var chatId =  message.getChatId();
            List<SupportRequest> existingRequests = supportRequestRepository.findByAuthorIdAndChatIdAndStatusNotIn(user.getId(), chatId, closedStatuses);

            if (!existingRequests.isEmpty()) {
                supportRequest = existingRequests.stream()
                        .filter(Objects::nonNull) // Не забываем про null-элементы
                        .max(Comparator.comparing(SupportRequest::getLastMessageAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .orElseThrow(() -> new IllegalStateException("Список запросов пуст или неожиданно пуст."));
            } else {
                log.warn("No active requests found for user {} in chat {}. Statuses checked: {}.",
                        user.getId(), chatId, closedStatuses);
            }
        }

        //совсем совсем ничего подходящего не нашли, просто создадим новое обращение
        if(supportRequest == null) {
            supportRequest = createNewSupportRequest(sender, message.getChatId());
            log.info("Created new support request. Author ID: {}, Chat ID: {}",
                    (supportRequest.getAuthor() != null ? supportRequest.getAuthor().getId() : "NULL"),
                    supportRequest.getChatId());

        }

        // Обновление обращения
        telegramMessage.setSupportRequest(supportRequest);
//        telegramMessage = messageService.updateMessage(telegramMessage);

        supportRequest.getMessages().add(telegramMessage);
        supportRequest.setLastMessageAt(telegramMessage.getSentAt());
        supportRequest.setLastMessage(telegramMessage);
        //supportRequest.getParticipants().add(sender);

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

    // ----------------------------------------------------------------------
    // --- ЛОГИКА ДЛЯ ГРУППИРОВКИ АЛЬБОМОВ (для отображения в CRM) ---
    // ----------------------------------------------------------------------

    /**
     * Преобразует список сущностей сообщений в список DTO, объединяя медиа-альбомы
     * в одно логическое DTO для корректного отображения в CRM.
     * * @param rawMessageEntities Список сущностей TelegramMessage.
     * @return Список TelegramMessageDto, готовый для передачи в Thymeleaf.
     */
    @Transactional(readOnly = true)
    public List<TelegramMessageDto> processAndGroupMessages(List<TelegramMessage> rawMessageEntities) {
        if (rawMessageEntities == null || rawMessageEntities.isEmpty()) {
            return List.of();
        }

// 1. Создаем DTO сообщений и агрегируем медиафайлы (DisplayMediaDto)
        List<TelegramMessageDto> processedMessagesDto = rawMessageEntities.stream()
                .map(messageEntity -> {
                    // А. Маппим сущность сообщения в его DTO, игнорируя List<TelegramMedia>
                    TelegramMessageDto messageDto = messageMapper.toDto(messageEntity); // mediaFiles = null

                    // Б. АГРЕГАЦИЯ МЕДИА: Преобразуем List<TelegramMedia> (сущности) в List<DisplayMediaDto>
                    // mediaFiles - это поле сущности TelegramMessage
                    List<DisplayMediaDto> displayMedia = displayMediaMapper.mapToDisplayDto(messageEntity.getMediaFiles());

                    // В. Устанавливаем агрегированный список в DTO
                    return messageDto.withMediaFiles(displayMedia);
                })
                .collect(Collectors.toList());

        // 2. Группируем DTO по mediaGroupId
        Map<String, List<TelegramMessageDto>> groupedByMediaId = processedMessagesDto.stream()
                .collect(Collectors.groupingBy(
                        // Используем mediaGroupId, или "null" для одиночных сообщений
                        msg -> msg.mediaGroupId() != null ? msg.mediaGroupId() : "null"
                ));

        List<TelegramMessageDto> processedMessages = new ArrayList<>();

        // 3. Обрабатываем каждую группу
        for (Map.Entry<String, List<TelegramMessageDto>> entry : groupedByMediaId.entrySet()) {
            String groupId = entry.getKey();
            List<TelegramMessageDto> groupMessages = entry.getValue();

            if ("null".equals(groupId) || groupMessages.size() == 1) {
                // Это не альбом или одиночное сообщение: просто добавляем его.
                processedMessages.addAll(groupMessages);
            } else {
                // Это альбом (группа сообщений с одинаковым mediaGroupId)
                TelegramMessageDto mergedMessage = mergeAlbumMessages(groupMessages);
                if (mergedMessage != null) {
                    processedMessages.add(mergedMessage);
                }
            }
        }

        // 4. Сортируем по времени, так как группировка могла нарушить порядок
        processedMessages.sort(Comparator.comparing(TelegramMessageDto::sentAt).reversed());

        return processedMessages;
    }

    /**
     * Объединяет список DTO-сообщений Telegram, принадлежащих к одному альбому.
     */
    private TelegramMessageDto mergeAlbumMessages(List<TelegramMessageDto> albumMessages) {
        if (albumMessages.isEmpty()) {
            return null;
        }

        // --- СЛУЖЕБНЫЙ ТЕКСТ В БД, КОТОРЫЙ НУЖНО ИГНОРИРОВАТЬ ---
        //Это те изображения галереи, которые в оригинале в телеграм не содержат подписей
        final String SERVICE_CAPTION = "[картинка без подписи]";

        // 1. Находим сообщение, которое будет "основным" (то, где есть оригинальная подпись)

        // Ищем ПЕРВОЕ сообщение, у которого текст существует И НЕ РАВЕН служебной фразе.
        TelegramMessageDto messageWithOriginalCaption = albumMessages.stream()
                .filter(msg -> msg.messageText() != null
                        && !msg.messageText().trim().isEmpty()
                        && !msg.messageText().trim().equals(SERVICE_CAPTION)) // <-- ИСКЛЮЧАЕМ СЛУЖЕБНЫЙ ТЕКСТ
                .findFirst()
                .orElse(null);

        // Если подпись найдена, используем это сообщение как основное.
        // Если не найдена (вся группа без подписи или со служебной подписью), берем первое сообщение.
        TelegramMessageDto primaryMessage = (messageWithOriginalCaption != null)
                ? messageWithOriginalCaption
                : albumMessages.get(0);

        // 2. Собираем все медиафайлы (DisplayMediaDto) из всех сообщений альбома
        // Здесь мы собираем агрегированный DisplayMediaDto, а не старый TelegramMediaDto!
            List<DisplayMediaDto> allMedia = albumMessages.stream()
                .flatMap(msg -> {
                    // ИСПОЛЬЗУЕМ Stream.empty() ВМЕСТО null
                    if (msg.mediaFiles() != null) {
                        return msg.mediaFiles().stream();
                    } else {
                        return java.util.stream.Stream.empty();
                    }
                })
                .filter(Objects::nonNull) // Оставим на всякий случай, но теперь он не нужен
                .collect(Collectors.toList());

        // 3. Строим объединенное DTO (используем конструктор record)
        return new TelegramMessageDto(
                primaryMessage.telegramMessageId(),
                primaryMessage.messageText(),
                primaryMessage.sender(),
                primaryMessage.authorName(),
                primaryMessage.chatId(),
                primaryMessage.sentAt(),
                primaryMessage.isEdited(),
                true, // Это альбом, поэтому isMedia = true
                primaryMessage.supportRequestId(),
                primaryMessage.replyToMessageId(),
                primaryMessage.mediaGroupId(),
                allMedia // <--- Передаем объединенный список DisplayMediaDto
        );
    }

    /**
     * Основной метод для получения SupportRequestDto, готового для Thymeleaf.
     */
    @Transactional(readOnly = true)
    public SupportRequestDto getSupportRequestDtoForDisplay(Long requestId) {
        // 1. Загружаем сущность с инициализированными сообщениями и медиа.
        SupportRequest request = getSupportRequestById(requestId); // Используем ваш метод загрузки!

        // 2. Получаем сырой список сущностей сообщений
        List<TelegramMessage> rawMessages = request.getMessages();

        // 3. ОБРАБАТЫВАЕМ И ГРУППИРУЕМ СООБЩЕНИЯ
        List<TelegramMessageDto> processedMessages = processAndGroupMessages(rawMessages);

        // 4. Маппим основную сущность в DTO (без сообщений, т.к. мы игнорировали их в маппере)
        SupportRequestDto requestDto = supportRequestMapper.toDto(request);

        // 5. Создаем финальную DTO, добавляя обработанный список сообщений
        // (Этот метод withMessages() должен быть в вашем record SupportRequestDto)
        var finalDto = requestDto.withMessages(processedMessages);
        return finalDto;
    }
}
