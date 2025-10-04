package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.configs.AppConfig;
import avishgreen.amvera.crm.enums.TelegramUpdateType;
import avishgreen.amvera.crm.services.telegramhandlers.TelegramMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        updates.forEach(this::handleUpdate);
    }

    /**
     * Центральный метод для обработки всех типов обновлений.
     * @param update Объект обновления Telegram.
     */
    public void handleUpdate(Update update) {

        var type = TelegramUpdateType.getType(update);

        boolean switchResult = switch(type){
            case MESSAGE -> {
                messageHandler.handleMessage(update);
                yield true;
            }
            case CALLBACK_QUERY,
                 CHAT_MEMBER,
                 MY_CHAT_MEMBER,
                 CHANNEL_POST,
                 EDITED_MESSAGE,
                 INLINE_QUERY,
                 UNKNOWN -> {
                yield false;
                //callbackQueryHandler.handle(update);
            }
        };
        var i=0;
        if(!switchResult){
            log.info("Received unhandled Update Type: {}", type);
        }else{i++;//do nothing}

    }
}

