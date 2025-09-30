package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.configs.AppConfig;
import avishgreen.amvera.crm.enums.TelegramUpdateType;
import avishgreen.amvera.crm.services.telegramhandlers.TelegramAntispamHandler;
import avishgreen.amvera.crm.services.telegramhandlers.TelegramMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramUpdateReceiverService implements SpringLongPollingBot, LongPollingUpdateConsumer {
    private final TelegramMessageHandler messageHandler;
    private final AppConfig appConfig;

    @Override
    public String getBotToken() {
//        log.info(appConfig.getTelegram().getToken());
        return appConfig.getTelegram().getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(update ->
            {
//                log.info("UPDATE received. Id {}", update.getUpdateId());
                handleUpdate(update);
            }
        );
    }

    /**
     * Центральный метод для обработки всех типов обновлений.
     * @param update Объект обновления Telegram.
     */
    public void handleUpdate(Update update) {

        var type = TelegramUpdateType.getType(update);
        Integer messageId=null;
        try {
            messageId = update.getMessage().getMessageId();
        }catch (Exception ex){
            log.error("Update %s has no message. Error %s"
                    .formatted(update.getUpdateId()
                            ,ex.getMessage()),ex);
        }
        log.info("Received Update Type: {} for message {}", type, messageId);
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

