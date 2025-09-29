package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.entities.TelegramMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
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

        // определим ID сообщения для ответа
        Integer replyToMessageId = request.getMessages().stream()
                // Фильтр: ищем только сообщения, где отправитель равен автору запроса (пользователю Telegram)
                .filter(msg -> msg.getSender().getId().equals(request.getAuthor().getId()))
                // Находим самое последнее из них
                .max(Comparator.comparing(TelegramMessage::getSentAt))
                // Получаем TelegramMessageId
                .map(TelegramMessage::getTelegramMessageId)
                .orElse(null);
        // Если сообщений от пользователя нет, то ничего не отправляем
        if (replyToMessageId == null) {
            return;
        }

        // AbsSender.execute() отправляет сообщение и возвращает объект Message
        Message sentMessage = botApi.answerMessage(request.getChatId(),text,replyToMessageId);

        // сохраним исходящее сообщение в БД
        TelegramMessage crmMessage = new TelegramMessage();
        crmMessage.setMessageText(text);
        crmMessage.setChatId(request.getChatId());
        crmMessage.setReplyToMessageId(replyToMessageId);
        crmMessage.setSentAt(Instant.now());
        crmMessage.setTelegramMessageId(sentMessage.getMessageId()); // Сохраняем ID, присвоенный Telegram
        crmMessage.setSupportRequest(request);

        telegramMessageService.saveBotMessage(crmMessage);
    }
}