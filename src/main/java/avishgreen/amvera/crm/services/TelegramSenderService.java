package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.entities.TelegramMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramSenderService {

    private final TelegramApiService botApi;
    private final SupportRequestService supportRequestService;
    private final TelegramMessageService telegramMessageService;


    /**
     * Отправляет ответное сообщение пользователю Telegram из CRM.
     * @param supportRequestId ID обращения в поддержку.
     * @param text Текст сообщения.
     * @throws TelegramApiException Если произошла ошибка при взаимодействии с Telegram API.
     */
    @Transactional
    public void sendResponseToUser(Long supportRequestId, String text) throws TelegramApiException {

        // найдем обращение
        SupportRequest request = supportRequestService.getSupportRequestByIdWithMessages(supportRequestId);

    /*
        // получим текущего пользователя CRM (модератора)
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser senderUser = userService.loadUserByUsername(currentUsername);

        if (senderUser == null) {
            throw new IllegalStateException("Не удалось найти текущего пользователя CRM.");
        }
     */

        // Получаем все сообщения пользователя, отсортированные от НОВОГО к СТАРОМУ
        List<TelegramMessage> userMessages = request.getMessages().stream()
                .filter(msg -> msg.getSender().getId().equals(request.getAuthor().getId()))
                .sorted(Comparator.comparing(TelegramMessage::getSentAt).reversed()) // Самое новое - первое
                .toList();

        // Если сообщений от пользователя нет, то ничего не отправляем
        if (userMessages.isEmpty()) {
            log.warn("Cannot send reply: No messages found from the user in request ID {}.", supportRequestId);
            return;
        }

        Message sentMessage = null;
        Integer finalReplyToMessageId = null;
        int successfullyTriedCount = 0;

        // 2. ИТЕРИРУЕМ по сообщениям, пока не найдем рабочее
        for (TelegramMessage userMessage : userMessages) {
            Integer currentReplyId = userMessage.getTelegramMessageId();

            try {
                // Пытаемся отправить ответ на ТЕКУЩЕЕ сообщение
                sentMessage = botApi.answerMessage(request.getChatId(), text, currentReplyId);

                // Если отправка успешна, сохраняем ID и выходим из цикла
                finalReplyToMessageId = currentReplyId;
                successfullyTriedCount++;
                break;

            } catch (IllegalArgumentException e) {
                // 3. Поймали ошибку: сообщение с ID currentReplyId удалено.
                // Эта ошибка выбрасывается из TelegramApiService при 400 Bad Request.
                log.warn("Message ID {} was deleted. Trying previous message...", currentReplyId);
                successfullyTriedCount++;
                continue; // Пробуем следующее (предыдущее) сообщение

            } catch (Exception e) {
                // 4. Поймали другую, не связанную с удалением сообщения, ошибку (например, ошибка БД).
                log.error("Failed to send message to chat {} for unexpected reason.", request.getChatId(), e);
                // Бросаем системное исключение
                throw new IllegalStateException("Произошла неожиданная ошибка при отправке ответа.", e);
            }
        }

        // 5. Проверка результата после цикла
        if (sentMessage != null) {
            // Успешно отправлено: сохраняем в БД
            TelegramMessage crmMessage = new TelegramMessage();
            crmMessage.setMessageText(text);
            crmMessage.setChatId(request.getChatId());
            crmMessage.setReplyToMessageId(finalReplyToMessageId); // Используем ID, который сработал
            crmMessage.setSentAt(Instant.now());
            crmMessage.setTelegramMessageId(sentMessage.getMessageId());
            crmMessage.setSupportRequest(request);

            telegramMessageService.saveBotMessage(crmMessage);
            log.info("Reply successfully sent for request ID {} after {} attempt(s).", supportRequestId, successfullyTriedCount);
        } else {
            // Если sentMessage == null, значит, все сообщения удалены.
            log.warn("Could not send reply. All {} user messages were likely deleted.", userMessages.size());

            // Выбрасываем исключение, которое поймает контроллер (400 Bad Request)
            throw new IllegalArgumentException(
                    "Невозможно отправить ответ. Все сообщения пользователя в этом обращении были удалены.");
        }
    }
}