package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.repositories.TelegramMessageRepository;
import avishgreen.amvera.crm.entities.TelegramMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramMessageService {
    private final TelegramMessageRepository messageRepository;
    private final TelegramUserService userService;

    @Transactional
    public TelegramMessage saveNewMessage(Message message, User user){
        var sender = userService.getOrCreateIfNeed(user);
        // Получаем ID сообщения, на которое был дан ответ
        Integer replyToMessageId = (message.getReplyToMessage() != null)
                ? message.getReplyToMessage().getMessageId()
                : null;

        TelegramMessage telegramMessage = TelegramMessage.builder()
                .telegramMessageId(message.getMessageId())
                .messageText(message.getText())
                .sentAt(Instant.now())
                .sender(sender)
                .chatId(message.getChatId())
                .replyToMessageId(replyToMessageId) // Сохраняем ID ответа
                .build();
        messageRepository.save(telegramMessage);
        return telegramMessage;
    }

    @Transactional
    public TelegramMessage updateMessage(TelegramMessage message){
        messageRepository.save(message);
        return message;
    }

    public Optional<String> getLastMessageText(Long supportRequestId) {
        return messageRepository.findTopBySupportRequestIdOrderBySentAtDesc(supportRequestId)
                .map(TelegramMessage::getMessageText);
    }

    public TelegramMessage findRootMessage(Integer messageId) {
        Optional<TelegramMessage> currentMessageOpt = findByMessageId(messageId);

        // Итеративно идем по цепочке, пока не найдем сообщение,
        // у которого нет replyToMessageId
        while (currentMessageOpt.isPresent() && currentMessageOpt.get().getReplyToMessageId() != null) {
            currentMessageOpt = findByMessageId(currentMessageOpt.get().getReplyToMessageId());
        }
        return currentMessageOpt.orElse(null);
    }
    public Optional<TelegramMessage> findByMessageId(Integer messageId) {
        return messageRepository.findByTelegramMessageId(messageId);
    }

}
