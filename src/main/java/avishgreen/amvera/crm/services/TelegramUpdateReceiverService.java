package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.enums.TelegramUpdateType;
import avishgreen.amvera.crm.services.telegramhandlers.TelegramAnispamHandler;
import avishgreen.amvera.crm.services.telegramhandlers.TelegramMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@Slf4j
public class TelegramUpdateReceiverService implements SpringLongPollingBot, LongPollingUpdateConsumer {
    private final String botToken;
    private final TelegramMessageHandler messageHandler;
    private final TelegramAnispamHandler anispamHandler;

    // ЯВНЫЙ КОНСТРУКТОР
    public TelegramUpdateReceiverService(
            @Value("${application.telegram.token}") String botToken,
            TelegramMessageHandler messageHandler,
            TelegramAnispamHandler anispamHandler
    ) {
        this.botToken = botToken;
        this.messageHandler = messageHandler;
        this.anispamHandler = anispamHandler;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(update ->
            {
                log.debug("UPDATE received. Id {}", update.getUpdateId());
                handleUpdate(update);
            }
        );
    }

    /**
     * Центральный метод для обработки всех типов обновлений.
     * @param update Объект обновления Telegram.
     */
    @Async
    public void handleUpdate(Update update) {
        //проверим, не спам ли это сообщение
        var isSpam = anispamHandler.isSpam(update.getUpdateId());
        if (isSpam) {
            return;
        }

        var type = TelegramUpdateType.getType(update);
        switch(type){
            case MESSAGE ->  messageHandler.handleMessage(update);//messageHandler.handle(update);
            case CALLBACK_QUERY -> {}//callbackQueryHandler.handle(update);
            case CHAT_MEMBER -> {}//handleChatMember(update);
            case MY_CHAT_MEMBER -> {}//handleMyChatMember(update);
            case CHANNEL_POST -> {}
            //default не будем делать, чтобы компилятор проверял полноту обработки этого енум
    //            default -> log.warn("UNSUPPORTED type of telegram update received! %s".formatted(type));
        }

    }
}

