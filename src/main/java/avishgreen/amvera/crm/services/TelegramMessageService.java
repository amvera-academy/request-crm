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
    private final TelegramMediaService telegramMediaService;


    @Transactional
    public void saveBotMessage(TelegramMessage message){
        var sender = userService.getOrCreateBot();
        message.setSender(sender);

        messageRepository.save(message);
    }

    @Transactional
    public TelegramMessage saveNewMessage(Message message, User user){
        var sender = userService.getOrCreateIfNeed(user);

        // Получаем ID сообщения, на которое был дан ответ
        Integer replyToMessageId = null;
        Message repliedMessage = message.getReplyToMessage();

        if (repliedMessage != null) {
            Integer originalReplyId = repliedMessage.getMessageId();

            // Ищем родительское сообщение в нашей базе
            Optional<TelegramMessage> existingReply = findByMessageId(originalReplyId);

            if (existingReply.isPresent()) {
                // Родительское сообщение найдено, можно безопасно на него ссылаться.
                replyToMessageId = originalReplyId;
            }
            // else: Родительское сообщение не найдено.
            // replyToMessageId останется null, что предотвратит ошибку внешнего ключа.
            // Это делает текущее сообщение "корневым" в этой цепочке для нашей системы.
        }

        //Проверяем, существует ли сообщение уже в базе
        TelegramMessage telegramMessage;
        Optional<TelegramMessage> existingMessage = findByMessageId(message.getMessageId());
        String messageText = null;
        String messageCaption = null;
        if(message.hasText()){messageText = message.getText();}
        if(message.hasCaption()){messageCaption = message.getCaption();}

        final String messageTextOrCaption;

        if (messageText != null) {
            // Обычное текстовое сообщение
            messageTextOrCaption = messageText;
        } else if (messageCaption != null) {
            // Медиафайл с подписью (Caption)
            messageTextOrCaption = "[картинка] ".concat(messageCaption);
        } else {
            // Медиафайл без текста и без подписи
            messageTextOrCaption = "[картинка без подписи]";
        }

        if (existingMessage.isEmpty()) {
            telegramMessage = TelegramMessage.builder()
                    .telegramMessageId(message.getMessageId())
                    .messageText(messageTextOrCaption)
                    .sentAt(Instant.now())
                    .sender(sender)
                    .chatId(message.getChatId())
                    .replyToMessageId(replyToMessageId) // Сохраняем ID ответа
                    .build();

        }else{
            telegramMessage = existingMessage.get();
        }
        messageRepository.save(telegramMessage);

        // Если сообщение содержит фото, обрабатываем и сохраняем метаданные медиа.
        if (message.hasPhoto()) {
            telegramMediaService.processMessageMedia(message, telegramMessage);
        }

        return telegramMessage;
    }

    @Transactional
    public TelegramMessage updateMessage(TelegramMessage message){
        messageRepository.save(message);
        return message;
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
